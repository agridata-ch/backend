package ch.agridata.agreement.service;

import ch.agridata.agreement.dto.ContractRevisionPdfDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.transform.TransformerFactory;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.configuration.DefaultConfigurationBuilder;

/**
 * Configuration producer for PDF generation components.
 * This class initializes and exposes the necessary beans
 * required for Apache FOP (Formatting Objects Processor) operations,
 * XML transformations, and JAXB marshalling.
 *
 * @CommentLastReviewed: 2026-04-17
 */

@ApplicationScoped
public class ContractRevisionFopConfig {

  private static final String FOP_CONFIG_RESOURCE = "pdf/fop.xconf";

  @Produces
  @ApplicationScoped
  public FopFactory fopFactory() {
    try {
      ClassLoader classLoader = getClass().getClassLoader();

      URI baseUri = resolvePdfBaseUri(classLoader);
      Configuration configuration = loadFopConfiguration(classLoader);

      return new FopFactoryBuilder(baseUri)
          .setConfiguration(configuration)
          .build();

    } catch (Exception e) {
      throw new IllegalStateException("Failed to initialize FopFactory", e);
    }
  }

  /**
   * Resolves the FOP base URI to the directory that contains {@code fop.xconf} (and, alongside it,
   * the embedded fonts, ICC output profile and images). FOP resolves the relative references in
   * {@code fop.xconf} / the XSL templates against this base via {@code new URL(base, ...)}, which
   * works for both {@code file:} URLs (local, exploded classpath) and {@code jar:} URLs (packaged
   * deployment).
   */
  private static URI resolvePdfBaseUri(ClassLoader classLoader) throws URISyntaxException {
    URL configUrl = classLoader.getResource(FOP_CONFIG_RESOURCE);

    if (configUrl == null) {
      throw new IllegalStateException("Missing resource: " + FOP_CONFIG_RESOURCE);
    }

    String configUri = configUrl.toURI().toString();
    String baseUri = configUri.substring(0, configUri.lastIndexOf('/') + 1);

    return new URI(baseUri);
  }

  private static Configuration loadFopConfiguration(ClassLoader classLoader)
      throws ConfigurationException, IOException {
    try (InputStream fopConfigStream = classLoader.getResourceAsStream(FOP_CONFIG_RESOURCE)) {
      if (fopConfigStream == null) {
        throw new IllegalStateException("Missing resource: " + FOP_CONFIG_RESOURCE);
      }

      return new DefaultConfigurationBuilder().build(fopConfigStream);
    }
  }

  @Produces
  @ApplicationScoped
  public TransformerFactory transformerFactory() {
    var factory = TransformerFactory.newInstance();

    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

    return factory;
  }

  @Produces
  @ApplicationScoped
  public JAXBContext jaxbContext() throws JAXBException {
    return JAXBContext.newInstance(ContractRevisionPdfDto.class);
  }
}
