package integration.agreement;

import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.agreement.service.ContractRevisionFopConfig;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.xml.bind.JAXBContext;
import javax.xml.transform.TransformerFactory;
import lombok.RequiredArgsConstructor;
import org.apache.fop.apps.FopFactory;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class ContractRevisionFopConfigTest {
  private final ContractRevisionFopConfig config;

  @Test
  void testFopFactoryInitialization() {
    FopFactory factory = config.fopFactory();
    assertThat(factory).isNotNull();
  }

  @Test
  void testTransformerFactory() {
    TransformerFactory factory = config.transformerFactory();
    assertThat(factory).isNotNull();
  }

  @Test
  void testJaxbContextInitialization() throws Exception {
    JAXBContext context = config.jaxbContext();
    assertThat(context).isNotNull();
  }
}
