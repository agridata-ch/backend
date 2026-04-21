package ch.agridata.agreement.service;

import ch.agridata.agreement.dto.ContractRevisionPdfDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.transform.TransformerFactory;
import org.apache.fop.apps.FopFactory;

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

  @Produces
  @ApplicationScoped
  public FopFactory fopFactory() {
    try {
      URL pdfBaseUrl = getClass()
          .getClassLoader()
          .getResource("pdf/");

      if (pdfBaseUrl == null) {
        throw new IllegalStateException("Missing resource directory: pdf/");
      }

      return FopFactory.newInstance(pdfBaseUrl.toURI());
    } catch (Exception e) {
      throw new IllegalStateException("Failed to initialize FopFactory", e);
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
