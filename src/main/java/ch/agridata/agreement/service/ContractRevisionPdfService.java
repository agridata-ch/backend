package ch.agridata.agreement.service;

import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;

import ch.agridata.agreement.dto.ContractRevisionPdfDto;
import ch.agridata.agreement.mapper.ContractRevisionPdfMapper;
import ch.agridata.agreement.persistence.ContractRevisionEntity;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.util.JAXBSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import lombok.RequiredArgsConstructor;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.xmlgraphics.util.MimeConstants;

/**
 * Service responsible for transforming contract revision data into PDF documents.
 * This service utilizes Apache FOP (Formatting Objects Processor) and XSLT to
 * render {@link ContractRevisionPdfDto} into a PDF byte stream.
 *
 * @CommentLastReviewed: 2026-04-23
 */

@ApplicationScoped
@RequiredArgsConstructor
public class ContractRevisionPdfService {
  private final FopFactory fopFactory;
  private final TransformerFactory transformerFactory;
  private final ContractRevisionPdfMapper contractRevisionPdfMapper;
  private final ContractRevisionQueryService contractRevisionQueryService;
  private final JAXBContext jaxbContext;
  private final ContractRevisionStorageService contractRevisionStorageService;

  @PostConstruct
  void init() {
    transformerFactory.setURIResolver(classpathResolver());
  }

  @RolesAllowed({ADMIN_ROLE, PROVIDER_ROLE, CONSUMER_ROLE})
  public byte[] getPdf(UUID contractRevisionId) {
    ContractRevisionEntity entity = contractRevisionQueryService.getWithAccessCheck(contractRevisionId);

    return contractRevisionStorageService.download(entity.getId());
  }

  public void generateAndUploadPdf(ContractRevisionEntity contractRevisionEntity) {
    byte[] pdf = generatePdf(contractRevisionEntity);
    contractRevisionStorageService.upload(contractRevisionEntity.getId(), pdf);
  }

  private byte[] generatePdf(ContractRevisionEntity contractRevisionEntity) {
    ContractRevisionPdfDto pdfDto = contractRevisionPdfMapper.toPdfDto(contractRevisionEntity);

    try (ByteArrayOutputStream out = new ByteArrayOutputStream();
         InputStream xsltIn = getClass().getClassLoader()
             .getResourceAsStream("pdf/contractRevision.fo.xsl")) {

      if (xsltIn == null) {
        throw new IllegalStateException("Missing resource: pdf/contractRevision.fo.xsl");
      }

      Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);
      Transformer transformer = transformerFactory.newTransformer(new StreamSource(xsltIn));

      Source xmlSource = new JAXBSource(jaxbContext, pdfDto);
      SAXResult fopResult = new SAXResult(fop.getDefaultHandler());

      transformer.transform(xmlSource, fopResult);

      return out.toByteArray();
    } catch (Exception e) {
      throw new IllegalStateException("PDF generation failed", e);
    }
  }

  private URIResolver classpathResolver() {
    return (href, base) -> {
      String path = "pdf/" + href;

      InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path);

      if (inputStream == null) {
        throw new IllegalStateException("Missing XSL resource: " + path);
      }

      return new StreamSource(inputStream);
    };
  }
}
