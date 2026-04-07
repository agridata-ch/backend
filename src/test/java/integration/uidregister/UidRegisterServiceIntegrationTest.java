package integration.uidregister;

import static integration.testutils.TestDataConstants.UID_BIO_SUISSE_WITHOUT_PREFIX;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import ch.agridata.uidregister.service.UidRegisterService;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class UidRegisterServiceIntegrationTest {

  private final UidRegisterService uidRegisterService;

  @Test
  void getByUid() {
    var result = uidRegisterService.getByUid(UID_BIO_SUISSE_WITHOUT_PREFIX);

    assertThat(result).isNotNull().satisfies(organisation -> {
      assertThat(organisation.name()).isEqualTo("INTERNATIONAL CERTIFICATION BIO SUISSE AG");
      assertThat(organisation.legalName()).isEqualTo("INTERNATIONAL CERTIFICATION BIO SUISSE AG");
      assertThat(organisation.uid()).isEqualTo("CHE101708094");
      assertThat(organisation.address()).satisfies(address -> {
        assertThat(address.street()).isEqualTo("Peter Merian-Str. 34");
        assertThat(address.zip()).isEqualTo("4052");
        assertThat(address.city()).isEqualTo("Basel");
        assertThat(address.country()).isEqualTo("CH");

      });
    });

  }
}
