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
  void generatePdf_Success() throws Exception {
    ContractRevisionEntity contractRevisionEntity = ContractRevisionEntity.builder().build();
    ContractRevisionPdfDto contractRevisionPdfDto = ContractRevisionPdfDto.builder().build();

    when(contractRevisionQueryService.getWithAccessCheck(REVISION_ID))
        .thenReturn(contractRevisionEntity);
    when(contractRevisionPdfMapper.toPdfDto(contractRevisionEntity)).thenReturn(contractRevisionPdfDto);

    when(fopFactory.newFop(eq(MimeConstants.MIME_PDF), any(ByteArrayOutputStream.class))).thenReturn(fop);
    when(transformerFactory.newTransformer(any())).thenReturn(transformer);

    byte[] result = contractRevisionPdfService.generatePdf(REVISION_ID);

    assertThat(result).isNotNull();
    verify(transformer).transform(any(Source.class), any(Result.class));
    verify(contractRevisionQueryService).getWithAccessCheck(REVISION_ID);
  }

  @Test
  void generatePdf_NotFound_ThrowsException() {
    when(contractRevisionQueryService.getWithAccessCheck(REVISION_ID))
        .thenThrow(new NotFoundException(REVISION_ID.toString()));

    assertThatThrownBy(() -> contractRevisionPdfService.generatePdf(REVISION_ID))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void generatePdf_TransformationFails_ThrowsIllegalStateException() throws Exception {
    ContractRevisionEntity contractRevisionEntity = ContractRevisionEntity.builder().build();
    when(contractRevisionQueryService.getWithAccessCheck(REVISION_ID))
        .thenReturn(contractRevisionEntity);
    when(contractRevisionPdfMapper.toPdfDto(any())).thenReturn(new ContractRevisionPdfDto());

    when(fopFactory.newFop(eq(MimeConstants.MIME_PDF), any(ByteArrayOutputStream.class)))
        .thenReturn(fop);
    when(transformerFactory.newTransformer(any())).thenReturn(transformer);

    doThrow(new RuntimeException("XSLT Error")).when(transformer).transform(any(), any());

    IllegalStateException ex = assertThrows(IllegalStateException.class,
        () -> contractRevisionPdfService.generatePdf(REVISION_ID));
    assertThat(ex.getMessage()).contains("PDF generation failed");
  }
}
