package integration.burregister;

import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.burregister.dto.BurRegisterAddressDto;
import ch.agridata.burregister.service.BurRegisterService;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class BurRegisterServiceIntegrationTest {

  private final BurRegisterService burRegisterService;

  @Test
  void getByUid() {
    var result = burRegisterService.getByUid("CHE101708094");

    assertThat(result.name()).isEqualTo("INTERNATIONAL CERTIFICATION BIO SUISSE AG");
    assertThat(result.legalName()).isEqualTo("INTERNATIONAL CERTIFICATION BIO SUISSE AG");
    assertThat(result.uid()).isEqualTo("CHE101708094");
    assertThat(result.address()).isEqualTo(new BurRegisterAddressDto("Peter Merian-Strasse 34", "4052", "Basel", "CH"));
  }

  @Test
  void getByUid_foreignOrganisation() {
    var result = burRegisterService.getByUid("CHE000000001");

    assertThat(result.name()).isEqualTo("Foreign Company");
    assertThat(result.legalName()).isEqualTo("Foreign Company Legal Name");
    assertThat(result.uid()).isEqualTo("CHE000000001");
    assertThat(result.address()).isEqualTo(new BurRegisterAddressDto("Dummy Street 10", "12345", "Dummy City", "DE"));
  }
}
