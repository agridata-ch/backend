package ch.agridata.agreement.service;

import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;

import ch.agridata.agreement.dto.ContractRevisionPdfDto;
import ch.agridata.agreement.mapper.ContractRevisionPdfMapper;
import ch.agridata.agreement.persistence.ContractRevisionRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.util.JAXBSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
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
 * @CommentLastReviewed: 2026-04-17
 */

@ApplicationScoped
@RequiredArgsConstructor
public class ContractRevisionPdfService {
  private final FopFactory fopFactory;
  private final TransformerFactory transformerFactory;
  private final ContractRevisionRepository contractRevisionRepository;
  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final ContractRevisionPdfMapper contractRevisionPdfMapper;
  private final JAXBContext jaxbContext;

  @RolesAllowed({CONSUMER_ROLE})
  public byte[] generatePdf(UUID contractRevisionId) {
    ContractRevisionPdfDto pdfDto = contractRevisionRepository
        .findByIdAndDataConsumerUid(contractRevisionId, agridataSecurityIdentity.getUidOrElseThrow())
        .map(contractRevisionPdfMapper::toPdfDto)
        .orElseThrow(() -> new NotFoundException(contractRevisionId.toString()));
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
}
