package ch.agridata.product.service;

import static ch.agridata.common.security.actingrole.ActingRoleEnum.ADMIN;
import static ch.agridata.common.security.actingrole.ActingRoleEnum.PROVIDER;
import static ch.agridata.product.persistence.DataProductStateEnum.ACTIVE;
import static ch.agridata.product.persistence.DataProductStateEnum.DRAFT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.agridata.common.security.actingrole.ActingRoleEnum;
import ch.agridata.product.dto.DataProductDescriptionDto;
import ch.agridata.product.dto.DataProductNameDto;
import ch.agridata.product.dto.DataProductUpdateDto;
import jakarta.validation.ValidationException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openapitools.jackson.nullable.JsonNullable;

class DataProductFieldPermissionTest {

  @Test
  void givenUpdateDto_whenComparedWithPermissions_thenEveryFieldHasExactlyOnePermission() {
    var dtoFields = Arrays.stream(DataProductUpdateDto.class.getRecordComponents()).map(RecordComponent::getName).toList();
    var permissionFields = Arrays.stream(DataProductFieldPermission.values()).map(permission -> permission.fieldName).toList();

    assertThat(permissionFields).containsExactlyInAnyOrderElementsOf(dtoFields);
  }

  @ParameterizedTest
  @EnumSource(DataProductFieldPermission.class)
  void givenDraft_whenCheckingAnyField_thenEditableByProviderAndAdmin(DataProductFieldPermission permission) {
    assertThat(permission.isEditableBy(PROVIDER, DRAFT)).isTrue();
    assertThat(permission.isEditableBy(ADMIN, DRAFT)).isTrue();
  }

  @ParameterizedTest
  @EnumSource(value = ActingRoleEnum.class, names = {"CONSUMER", "PRODUCER", "SUPPORT"})
  void givenNonEditingRole_whenCheckingAnyField_thenNotEditable(ActingRoleEnum role) {
    assertThat(DataProductFieldPermission.values())
        .noneMatch(permission -> permission.isEditableBy(role, DRAFT) || permission.isEditableBy(role, ACTIVE));
  }

  @Test
  void givenActiveProduct_whenCheckingLockedFields_thenMatchesRules() {
    assertThat(DataProductFieldPermission.NAME.isEditableBy(PROVIDER, ACTIVE)).isFalse();
    assertThat(DataProductFieldPermission.NAME.isEditableBy(ADMIN, ACTIVE)).isTrue();
    assertThat(DataProductFieldPermission.DESCRIPTION.isEditableBy(PROVIDER, ACTIVE)).isFalse();
    assertThat(DataProductFieldPermission.DESCRIPTION.isEditableBy(ADMIN, ACTIVE)).isTrue();
    assertThat(DataProductFieldPermission.DATA_SOURCE_SYSTEM_ID.isEditableBy(ADMIN, ACTIVE)).isFalse();
    assertThat(DataProductFieldPermission.CONSENT_REQUIRED.isEditableBy(ADMIN, ACTIVE)).isFalse();
    assertThat(DataProductFieldPermission.REST_CLIENT_ID.isEditableBy(PROVIDER, ACTIVE)).isTrue();
  }

  @Test
  void givenProviderAndActiveProduct_whenLockedFieldsPresent_thenRejectedListingAllOfThem() {
    var updateDto = DataProductUpdateDto.builder()
        .name(JsonNullable.of(new DataProductNameDto("DE", "FR", "IT")))
        .description(JsonNullable.of(null))
        .restClientPathTemplate(JsonNullable.of("/path"))
        .build();

    assertThatThrownBy(() -> DataProductFieldPermission.verifyEditable(updateDto, PROVIDER, ACTIVE))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("name")
        .hasMessageContaining("description")
        .hasMessageNotContaining("restClientPathTemplate");
  }

  @Test
  void givenProviderAndActiveProduct_whenLockedFieldsOmitted_thenAccepted() {
    var updateDto = DataProductUpdateDto.builder()
        .restClientId(JsonNullable.of(UUID.randomUUID()))
        .description(JsonNullable.undefined())
        .build();

    assertThatCode(() -> DataProductFieldPermission.verifyEditable(updateDto, PROVIDER, ACTIVE)).doesNotThrowAnyException();
  }

  @Test
  void givenProviderAndDraft_whenFieldsLockedWhileActivePresent_thenAccepted() {
    var updateDto = DataProductUpdateDto.builder()
        .name(JsonNullable.of(new DataProductNameDto("DE", "FR", "IT")))
        .description(JsonNullable.of(new DataProductDescriptionDto("DE", "FR", "IT")))
        .dataSourceSystemId(JsonNullable.of(UUID.randomUUID()))
        .consentRequired(JsonNullable.of(true))
        .build();

    assertThatCode(() -> DataProductFieldPermission.verifyEditable(updateDto, PROVIDER, DRAFT)).doesNotThrowAnyException();
  }
}
