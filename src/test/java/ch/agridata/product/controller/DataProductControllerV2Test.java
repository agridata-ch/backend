package ch.agridata.product.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import ch.agridata.common.security.actingrole.ActingRoleEnum;
import ch.agridata.common.security.actingrole.ActingRoleHolder;
import jakarta.ws.rs.ForbiddenException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataProductControllerV2Test {
  @Mock
  ActingRoleHolder actingRoleHolder;

  @InjectMocks
  DataProductControllerV2 controller;

  @Test
  void givenUnsupportedRole_whenPatchDataProduct_thenThrowForbiddenException() {
    when(actingRoleHolder.getRole()).thenReturn(ActingRoleEnum.CONSUMER);

    assertThrows(ForbiddenException.class, () -> controller.patchDataProduct(null, null));
  }
}
