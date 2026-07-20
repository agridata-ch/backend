# Type-safe search repositories: string-based vs. `Restriction`/`Order`

A comparison of two ways to implement the unified **paginate + filter + sort** query behind
`ResourceQueryDto` → `PageResponseDto`, using `UserRepository.findByRoleAtLastLogin` as the worked example.

- **Old** — `BaseSearchRepository` + `JpaUtil`: builds HQL fragments and `Sort` from **strings**.
- **New** — Hibernate 7 `SelectionSpecification` + `Restriction`/`Order`: builds the filter and sort from the
  **static metamodel** (`UserEntity_`), so they are compile-time checked.

Stack: Quarkus 3.37.2 · Hibernate ORM 7.4.3 · Jakarta Data 1.0.1.

> **Verdict:** the new approach makes the *dynamic, client-driven* parts (search terms, `sortBy`) refactoring-safe
> and injection-proof. The *JSONB predicate* cannot be expressed either way and stays an HQL string in both. It is a
> clear win for filter/sort over real columns; it is **not** a reason to rewrite JSONB-heavy repositories.

---

## 1. The entry method

### Old

```java
private static final List<String> FILTER_FIELDS = List.of("email", "givenName", "familyName", "phoneNumber");
private static final List<List<String>> COMBINED_FIELDS = List.of(List.of("givenName", "familyName"));

public PageResponseDto<UserEntity> findByRoleAtLastLogin(ResourceQueryDto query, String role) {
  return findPage(
      query,
      "function('jsonb_exists', rolesAtLastLogin, :role) = true", // base WHERE (string)
      Map.of("role", role),                                       // base params (string keys)
      FILTER_FIELDS,                                              // fields as strings
      COMBINED_FIELDS);                                           // fields as strings
}
```

### New

```java
private static final String ROLE_AT_LAST_LOGIN =
    "from UserEntity where function('jsonb_exists', rolesAtLastLogin, :role) = true"; // JSONB stays HQL

public PageResponseDto<UserEntity> findByRoleAtLastLogin(ResourceQueryDto query, String role) {
  var spec = SelectionSpecification.create(UserEntity.class, ROLE_AT_LAST_LOGIN)
      .restrict(searchRestriction(query.searchTerm()));           // type-safe filter
  for (var order : toOrders(query.sortParams())) {                // type-safe sort
    spec = spec.sort(order);
  }
  var jpaQuery = spec.createQuery(getEntityManager()).setParameter("role", role);

  var totalItems = jpaQuery.getResultCount();
  var items = jpaQuery.setPage(Page.page(query.size(), query.page())).getResultList();
  var totalPages = (int) Math.ceil((double) totalItems / query.size());
  return new PageResponseDto<>(items, totalItems, totalPages, query.page(), query.size());
}
```

Both produce identical results — verified by `integration.user.GetUsersTest` (4/4), including the multi-token search
`"sm Jo"` → *John Smith* and the `-lastLoginDate` sort.

---

## 2. Field references — strings vs. metamodel

The core difference. Rename `UserEntity.email` → `emailAddress`:

| | Old | New |
|---|---|---|
| Reference | `"email"` | `UserEntity_.email` |
| After rename | compiles, **fails at runtime** (`QueryException`) | **does not compile** |
| IDE rename refactor | misses the string | updates the reference |
| Typo (`"emial"`) | silent until that query runs | caught by `javac` |

---

## 3. Building the filter

### Old — `JpaUtil.createContainsWhereClause` (excerpt)

String assembly: each field becomes `LOWER(field) LIKE :param`, OR-joined, with a parallel parameter map.

```java
private static String createWhereClause(String field, String parameterName) {
  return "LOWER(" + field + ") LIKE :" + parameterName;   // field & param names are strings
}
// ... permutations of combined fields joined with " AND ", all clauses joined with " OR "
// ... returns WhereClause(clause, parameters) which findPage() concatenates into the query
```

### New — `searchRestriction` (metamodel combinators)

```java
private Restriction<UserEntity> searchRestriction(String searchTerm) {
  if (StringUtils.isBlank(searchTerm)) {
    return Restriction.unrestricted();
  }
  var cleaned = clean(searchTerm);
  var tokens = List.of(cleaned.split("\\s+"));

  var perField = Stream.of(UserEntity_.email, UserEntity_.givenName, UserEntity_.familyName, UserEntity_.phoneNumber)
      .map(field -> Restriction.contains(field, cleaned, false));   // false = case-insensitive

  Stream<Restriction<UserEntity>> combined = tokens.size() < 2
      ? Stream.empty()
      : Collections2.permutations(List.of(UserEntity_.givenName, UserEntity_.familyName)).stream()
          .map(perm -> Restriction.all(IntStream.range(0, perm.size())
              .mapToObj(i -> Restriction.contains(perm.get(i), tokens.get(i), false))
              .toList()));

  return Restriction.any(Stream.concat(perField, combined).toList());
}
```

**Trade-off:** the multi-token permutation logic is inherently the same shape in both. The new version is *not* shorter —
`Restriction.any`/`all` over permutations mirrors the old `OR`/`AND` string joins. The win is the field references
(`UserEntity_.email`) and named-parameter handling, not fewer lines.

---

## 4. Building the sort

### Old — `JpaUtil.parseSort`

Accepts **any** field name the client sends and forwards it to `Sort`; an unknown/hostile name is only caught (if at
all) when the DB rejects the generated SQL.

```java
if (trimmed.startsWith("-")) {
  sort = sort.and(trimmed.substring(1).trim(), Sort.Direction.Descending); // arbitrary string
} else {
  sort = sort.and(trimmed);
}
```

### New — `toOrder` (allow-list → metamodel)

The `switch` **is** the validation: only whitelisted fields are sortable; anything else is rejected up front.

```java
private Order<UserEntity> toOrder(String token) {
  var desc = token.startsWith("-");
  var field = desc ? token.substring(1) : token;
  final SingularAttribute<UserEntity, ?> attribute = switch (field) {
    case "lastLoginDate" -> UserEntity_.lastLoginDate;
    case "email"         -> UserEntity_.email;
    case "familyName"    -> UserEntity_.familyName;
    case "givenName"     -> UserEntity_.givenName;
    default -> throw new IllegalArgumentException("Unsupported sort field: " + field);
  };
  return desc ? Order.desc(attribute) : Order.asc(attribute);
}
```

`DataProductRepository` already hand-rolls this allow-list idea (`SORT_FIELDS`); here it falls out of the type-safe API
naturally.

---

## 5. Pagination — one API gotcha

| | Old (Panache) | New (Hibernate) |
|---|---|---|
| Query object | `PanacheQuery<T>` | `SelectionQuery<T>` |
| Page call | `.page(index, size)` — **index first** | `Page.page(size, number)` — **size first**, number 0-based |
| Count | `.count()` | `.getResultCount()` |
| Page count | `.pageCount()` | computed: `ceil(total / size)` |

> ⚠️ The argument order is **reversed** between the two APIs. `Page.page(query.size(), query.page())` is correct for
> Hibernate; passing `(page, size)` out of Panache habit is a silent bug.

---

## 6. The JSONB predicate — unchanged in both

`rolesAtLastLogin` is a `jsonb` column mapped as `SingularAttribute<UserEntity, Set<String>>` — an opaque JSON blob.
There is no `Restriction.jsonContains(...)` and no metamodel entry for a JSON *path*, so the "array contains role"
check cannot be expressed type-safely. It remains an HQL string in both approaches.

Attempt to at least upgrade the raw passthrough to Hibernate 7's native `json_exists()`:

```java
// json_exists parses, but on 7.4 it throws at runtime:
//   SemanticException: tech preview JSON functions are not enabled.
//   (needs hibernate.query.hql.json_functions_enabled=true)
"from UserEntity where json_exists(rolesAtLastLogin, '$[*] ? (@ == $role)' passing :role as role)"
```

So on this version we keep `function('jsonb_exists', rolesAtLastLogin, :role) = true`. Enabling `json_exists` would
give boot-time grammar validation (and compile-time checking inside a `@Query`), but opts the whole persistence unit
into a tech-preview feature — a separate decision.

---

## 7. Summary

| Dimension | Old (string-based) | New (`Restriction`/`Order`) |
|---|---|---|
| Field references | strings | metamodel — **compile-checked** |
| Rename / typo safety | runtime failure | build failure |
| Sortable fields | any string accepted | allow-list, rejected up front |
| Injection surface | field names concatenated | no field-name strings |
| Filter verbosity | compact | similar / slightly more |
| Reusability | `BaseSearchRepository` generic base (shared today) | per-repo helpers (would need a new base to share) |
| JSONB predicate | HQL string | HQL string (no change) |
| JSONB / JSON-path sort | HQL string | HQL string (no change) |
| Pagination | `PanacheQuery.page(index, size)` | `SelectionQuery.setPage(Page.page(size, number))` |

### When to use which

- **Prefer `Restriction`/`Order`** for filter + sort over **real mapped columns** — especially client-driven `sortBy`
  and search. Pairs naturally with the `PanacheRepository.Managed` (`@Find`/`@Query`) direction.
- **Keep `BaseSearchRepository` + `JpaUtil`** where the query is dominated by **JSONB** (`DataProductRepository`'s
  `jsonb_extract_path_text` sort, this role filter) — those need HQL regardless, so the type-safe payoff is small.
- The two coexist; migration can be incremental, repository by repository.
