package ch.agridata.agreement.service;

import ch.agridata.agreement.dto.ContractRevisionPdfDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

      URI baseUri = getPdfBaseResource(classLoader).toURI();
      Configuration configuration = loadFopConfiguration(classLoader);

      return new FopFactoryBuilder(baseUri)
          .setConfiguration(configuration)
          .build();

    } catch (Exception e) {
      throw new IllegalStateException("Failed to initialize FopFactory", e);
    }
  }

  private static URL getPdfBaseResource(ClassLoader classLoader) {
    URL configResource = classLoader.getResource(FOP_CONFIG_RESOURCE);

    if (configResource == null) {
      throw new IllegalStateException("Missing resource: " + FOP_CONFIG_RESOURCE);
    }

    try {
      return configResource.toURI().resolve(".").toURL();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to resolve PDF base resource from " + FOP_CONFIG_RESOURCE, e);
    }
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