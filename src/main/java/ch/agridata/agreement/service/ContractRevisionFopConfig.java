package ch.agridata.agreement.service;

import ch.agridata.agreement.dto.ContractRevisionPdfDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
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
 * @CommentLastReviewed: 2026-07-15
 */

@ApplicationScoped
public class ContractRevisionFopConfig {

  private static final String FOP_CONFIG_RESOURCE = "pdf/fop.xconf";

  /**
   * Resources referenced (relatively) by {@code fop.xconf} and the XSL templates: the ICC output
   * profile, the embedded fonts and the logo. They are copied to a temporary directory at startup so
   * FOP can resolve them through plain {@code file:} URLs. This works identically for an exploded
   * classpath (local dev) and a packaged jar (fast-jar deployment): FOP parses the font
   * configuration with its own default resource resolver, which resolves relative references against
   * the base URI and opens them via {@code URI.toURL()} — that fails for the opaque {@code jar:}
   * base URI of a packaged deployment, hence the materialization to the filesystem.
   */
  private static final List<String> PDF_RESOURCES = List.of(
      "color/sRGB2014.icc",
      "fonts/LiberationSans-Regular.ttf",
      "fonts/LiberationSans-Bold.ttf",
      "fonts/LiberationSans-Italic.ttf",
      "fonts/LiberationSans-BoldItalic.ttf",
      "fonts/LiberationMono-Regular.ttf",
      "swiss-logo.png");

  @Produces
  @ApplicationScoped
  public FopFactory fopFactory() {
    try {
      ClassLoader classLoader = getClass().getClassLoader();

      URI baseUri = materializePdfResources(classLoader);
      Configuration configuration = loadFopConfiguration(classLoader);

      return new FopFactoryBuilder(baseUri)
          .setConfiguration(configuration)
          .build();

    } catch (Exception e) {
      throw new IllegalStateException("Failed to initialize FopFactory", e);
    }
  }

  /**
   * Copies the {@link #PDF_RESOURCES} from the classpath into a temporary directory (preserving their
   * relative layout) and returns that directory as the FOP base URI, so all relative references in
   * {@code fop.xconf} / the XSL templates resolve to {@code file:} URLs regardless of packaging.
   */
  private static URI materializePdfResources(ClassLoader classLoader) throws IOException {
    // NOSONAR java:S5443 - Files.createTempDirectory creates the directory atomically with owner-only permissions.
    // It holds only non-sensitive, read-only assets (fonts, ICC profile, logo) already bundled in the jar
    Path baseDir = Files.createTempDirectory("contract-revision-pdf");
    baseDir.toFile().deleteOnExit();

    for (String resource : PDF_RESOURCES) {
      Path target = baseDir.resolve(resource);
      Files.createDirectories(target.getParent());

      try (InputStream resourceStream = classLoader.getResourceAsStream("pdf/" + resource)) {
        if (resourceStream == null) {
          throw new IllegalStateException("Missing PDF resource: pdf/" + resource);
        }

        Files.copy(resourceStream, target, StandardCopyOption.REPLACE_EXISTING);
        target.toFile().deleteOnExit();
      }
    }

    return baseDir.toUri();
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
