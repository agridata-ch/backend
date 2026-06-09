package ch.agridata.agreement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.dto.ContractRevisionPdfDto;
import ch.agridata.agreement.mapper.ContractRevisionPdfMapper;
import ch.agridata.agreement.persistence.ContractRevisionEntity;
import jakarta.ws.rs.NotFoundException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContractRevisionPdfServiceTest {
  @Mock
  private FopFactory fopFactory;
  @Mock
  private TransformerFactory transformerFactory;
  @Mock
  private ContractRevisionPdfMapper contractRevisionPdfMapper;
  @Mock
  private ContractRevisionQueryService contractRevisionQueryService;
  @Mock
  private ContractRevisionStorageService contractRevisionStorageService;
  @Mock
  private Transformer transformer;
  @Mock
  private Fop fop;
  @InjectMocks
  private ContractRevisionPdfService contractRevisionPdfService;
  @Spy
  private JAXBContext jaxbContext = JAXBContext.newInstance(ContractRevisionPdfDto.class);

  private static final UUID REVISION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  ContractRevisionPdfServiceTest() throws JAXBException {
  }

  @Test
  void getPdf_Success() {
    ContractRevisionEntity entity = ContractRevisionEntity.builder().id(REVISION_ID).build();
    byte[] expectedPdf = new byte[] {37, 80, 68, 70}; // %PDF

    when(contractRevisionQueryService.getAsConsumer(REVISION_ID)).thenReturn(entity);
    when(contractRevisionStorageService.download(REVISION_ID)).thenReturn(expectedPdf);

    byte[] result = contractRevisionPdfService.getPdfAsConsumer(REVISION_ID);

    assertThat(result).isEqualTo(expectedPdf);
    verify(contractRevisionQueryService).getAsConsumer(REVISION_ID);
    verify(contractRevisionStorageService).download(REVISION_ID);
  }

  @Test
  void getPdf_NotFound_ThrowsException() {
    when(contractRevisionQueryService.getAsConsumer(REVISION_ID))
        .thenThrow(new NotFoundException(REVISION_ID.toString()));

    assertThatThrownBy(() -> contractRevisionPdfService.getPdfAsConsumer(REVISION_ID))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void generateAndUploadPdf_Success() throws Exception {
    ContractRevisionEntity entity = ContractRevisionEntity.builder().build();
    ContractRevisionPdfDto pdfDto = ContractRevisionPdfDto.builder().build();

    when(contractRevisionPdfMapper.toPdfDto(entity)).thenReturn(pdfDto);
    when(fopFactory.newFop(eq(MimeConstants.MIME_PDF), any(ByteArrayOutputStream.class))).thenReturn(fop);
    when(transformerFactory.newTransformer(any())).thenReturn(transformer);

    contractRevisionPdfService.generateAndUploadPdf(entity);

    verify(transformer).transform(any(Source.class), any(Result.class));
    verify(contractRevisionStorageService).upload(eq(entity.getId()), any(byte[].class));
  }

  @Test
  void generateAndUploadPdf_GenerationFails_ThrowsIllegalState() throws Exception {
    ContractRevisionEntity entity = ContractRevisionEntity.builder().build();
    when(contractRevisionPdfMapper.toPdfDto(any())).thenReturn(new ContractRevisionPdfDto());

    when(fopFactory.newFop(eq(MimeConstants.MIME_PDF), any(ByteArrayOutputStream.class)))
        .thenReturn(fop);
    when(transformerFactory.newTransformer(any())).thenReturn(transformer);

    doThrow(new RuntimeException("XSLT Error")).when(transformer).transform(any(), any());

    IllegalStateException ex = assertThrows(IllegalStateException.class,
        () -> contractRevisionPdfService.generateAndUploadPdf(entity));
    assertThat(ex.getMessage()).contains("PDF generation failed");
  }
}
