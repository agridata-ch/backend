package ch.agridata.common.utils;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import io.quarkus.panache.common.Sort;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JpaUtilTest {

  @Test
  void givenMultipleTokens_createContainsWhereClause_requireEveryTokenInAnyField() {
    var whereClause = JpaUtil.createContainsWhereClause(" Anna  Maria Muster ", List.of("field1", "field2"));
    assertThat(whereClause.clause()).isEqualTo(
        "(LOWER(field1) LIKE :param0 OR LOWER(field2) LIKE :param0) "
            + "AND (LOWER(field1) LIKE :param1 OR LOWER(field2) LIKE :param1) "
            + "AND (LOWER(field1) LIKE :param2 OR LOWER(field2) LIKE :param2)");
    assertThat(whereClause.parameters()).containsExactlyInAnyOrderEntriesOf(Map.of(
        "param0", "%anna%",
        "param1", "%maria%",
        "param2", "%muster%"
    ));
  }

  @Test
  void givenRepeatedTokens_createContainsWhereClause_bindEachTokenOnce() {
    var whereClause = JpaUtil.createContainsWhereClause("s c h s c h", List.of("field1", "field2"));
    assertThat(whereClause.parameters()).containsExactlyInAnyOrderEntriesOf(Map.of(
        "param0", "%s%",
        "param1", "%c%",
        "param2", "%h%"
    ));
  }

  @Test
  void givenNoFields_createContainsWhereClause_createDummy() {
    var whereClause = JpaUtil.createContainsWhereClause(" test  input search ", List.of());
    assertThat(whereClause.parameters()).isEmpty();
    assertThat(whereClause.clause()).isEqualTo(JpaUtil.DUMMY_WHERE_CLAUSE);
  }

  @Test
  void parseSort() {
    var sort = JpaUtil.parseSort(List.of("field1", "-field2", " field3 ", " ", "- field4"));
    assertThat(sort).isNotNull();
    assertThat(sort.getColumns()).hasSize(4).extracting(Sort.Column::getName, Sort.Column::getDirection).containsExactlyInAnyOrder(
        tuple("field1", Sort.Direction.Ascending),
        tuple("field2", Sort.Direction.Descending),
        tuple("field3", Sort.Direction.Ascending),
        tuple("field4", Sort.Direction.Descending)
    );

  }
}
