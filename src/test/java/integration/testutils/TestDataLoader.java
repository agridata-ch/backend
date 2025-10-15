package integration.testutils;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.ws.rs.NotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TestDataLoader<E, R extends PanacheRepositoryBase<E, UUID>> {

  private final R repository;

  public static <E, R extends PanacheRepositoryBase<E, UUID>> TestDataLoader<E, R> of(R repository) {
    return new TestDataLoader<>(repository);
  }

  public E load(UUID uuid) {
    return repository.findByIdOptional(uuid)
        .orElseThrow(() -> new NotFoundException("Test data not found: " + uuid));
  }

  public List<E> load(UUID... uuids) {
    return Arrays.stream(uuids).map(this::load).toList();
  }

  public E load(TestDataIdentifiers.Identifier<E> identifier) {
    return repository.findByIdOptional(identifier.uuid())
        .orElseThrow(() -> new NotFoundException("Test data not found: " + identifier.uuid()));
  }

  @SafeVarargs
  public final List<E> load(TestDataIdentifiers.Identifier<E>... identifiers) {
    return Arrays.stream(identifiers).map(this::load).toList();
  }

}

