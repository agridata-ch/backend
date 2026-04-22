package integration.testutils;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkus.arc.Arc;
import io.quarkus.cache.CacheManager;
import io.quarkus.test.junit.callback.QuarkusTestBeforeEachCallback;
import io.quarkus.test.junit.callback.QuarkusTestMethodContext;
import java.lang.reflect.Field;
import org.flywaydb.core.Flyway;

public class BeforeEachTestCallback implements QuarkusTestBeforeEachCallback {

  @Override
  public void beforeEach(QuarkusTestMethodContext context) {
    resetDatabaseData(Arc.container().instance(Flyway.class).get());
    resetCaches(Arc.container().instance(CacheManager.class).get());
    resetWireMock(context.getTestInstance());
  }

  private void resetDatabaseData(Flyway flyway) {
    flyway.migrate();
  }

  private void resetCaches(CacheManager cacheManager) {
    for (String cacheName : cacheManager.getCacheNames()) {
      cacheManager.getCache(cacheName).get().invalidateAll().await().indefinitely();
    }
  }

  private void resetWireMock(Object testInstance) {
    for (Field field : testInstance.getClass().getDeclaredFields()) {
      if (field.getType().equals(WireMock.class)) {
        field.setAccessible(true);
        try {
          WireMock wireMock = (WireMock) field.get(testInstance);
          if (wireMock != null) {
            wireMock.resetToDefaultMappings();
          }
        } catch (IllegalAccessException e) {
          throw new RuntimeException("Could not access WireMock field", e);
        }
        return;
      }
    }
  }
}
