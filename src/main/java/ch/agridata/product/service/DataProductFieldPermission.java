package ch.agridata.product.service;

import static ch.agridata.common.security.actingrole.ActingRoleEnum.ADMIN;
import static ch.agridata.common.security.actingrole.ActingRoleEnum.PROVIDER;

import ch.agridata.common.security.actingrole.ActingRoleEnum;
import ch.agridata.product.dto.DataProductUpdateDto;
import ch.agridata.product.persistence.DataProductStateEnum;
import jakarta.validation.ValidationException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;

/**
 * Single source of truth for which role may change which field of a data product in which state. Each constant is one field of
 * {@link DataProductUpdateDto}; the two columns name the roles allowed to change it while the product is {@code DRAFT} and while it is
 * {@code ACTIVE}.
 *
 * <p>A field counts as changed as soon as it is <em>present</em> in the request: an explicit {@code null} clears the field and is
 * therefore just as much a change as a new value. Omitted fields are never checked. If any present field is not editable, the whole
 * request is rejected.
 *
 * <p>Every field of {@link DataProductUpdateDto} must be listed here (enforced by {@code DataProductFieldPermissionTest}), so adding a
 * field forces an explicit decision about who may edit it.
 *
 * @CommentLastReviewed 2026-10-28
 */
@RequiredArgsConstructor
enum DataProductFieldPermission {
  // (editable in DRAFT by, editable in ACTIVE by, JSON field name, DTO accessor)
  NAME(Editors.ALL, Editors.ADMIN_ONLY, "name", DataProductUpdateDto::name),
  DESCRIPTION(Editors.ALL, Editors.ADMIN_ONLY, "description", DataProductUpdateDto::description),
  DATA_SOURCE_SYSTEM_ID(Editors.ALL, Editors.NOBODY, "dataSourceSystemId", DataProductUpdateDto::dataSourceSystemId),
  REST_CLIENT_ID(Editors.ALL, Editors.ALL, "restClientId", DataProductUpdateDto::restClientId),
  REST_CLIENT_PATH_TEMPLATE(Editors.ALL, Editors.ALL, "restClientPathTemplate",
      DataProductUpdateDto::restClientPathTemplate),
  REST_CLIENT_REQUEST_TEMPLATE(Editors.ALL, Editors.ALL, "restClientRequestTemplate",
      DataProductUpdateDto::restClientRequestTemplate),
  REST_CLIENT_METHOD_CODE(Editors.ALL, Editors.ALL, "restClientMethodCode", DataProductUpdateDto::restClientMethodCode),
  FLOW_CODE(Editors.ALL, Editors.ALL, "flowCode", DataProductUpdateDto::flowCode),
  REST_CLIENT_CHANGE_DETECTION_PATH_TEMPLATE(Editors.ALL, Editors.ALL, "restClientChangeDetectionPathTemplate",
      DataProductUpdateDto::restClientChangeDetectionPathTemplate),
  LINKS(Editors.ALL, Editors.ALL, "links", DataProductUpdateDto::links),
  EXTENDED_DESCRIPTION(Editors.ALL, Editors.ALL, "extendedDescription", DataProductUpdateDto::extendedDescription),
  CONSENT_REQUIRED(Editors.ALL, Editors.NOBODY, "consentRequired", DataProductUpdateDto::consentRequired),
  PAYMENT_REQUIRED(Editors.ALL, Editors.ALL, "paymentRequired", DataProductUpdateDto::paymentRequired),
  PRICING_BASIS(Editors.ALL, Editors.ALL, "pricingBasis", DataProductUpdateDto::pricingBasis);

  private final Editors draftEditors;
  private final Editors activeEditors;
  final String fieldName;
  private final Function<DataProductUpdateDto, JsonNullable<?>> accessor;

  /**
   * Rejects the request if it contains a field the given role may not change in the given state.
   *
   * @throws ValidationException listing every offending field
   */
  static void verifyEditable(DataProductUpdateDto updateDto, ActingRoleEnum role, DataProductStateEnum state) {
    var lockedFields = Arrays.stream(values())
        .filter(field -> field.isPresentIn(updateDto) && !field.isEditableBy(role, state))
        .map(field -> field.fieldName)
        .toList();
    if (!lockedFields.isEmpty()) {
      throw new ValidationException(
          "Fields " + lockedFields + " cannot be changed by " + role + " while the data product is " + state);
    }
  }

  boolean isEditableBy(ActingRoleEnum role, DataProductStateEnum state) {
    var editors = switch (state) {
      case DRAFT -> draftEditors;
      case ACTIVE -> activeEditors;
    };
    return editors.roles.contains(role);
  }

  // Null-safe for DTOs built directly (e.g. in unit tests) where an unset field is a null reference rather than JsonNullable.undefined().
  private boolean isPresentIn(DataProductUpdateDto updateDto) {
    var value = accessor.apply(updateDto);
    return value != null && value.isPresent();
  }

  /**
   * The sets of roles that can appear in a column of {@link DataProductFieldPermission}.
   *
   * @CommentLastReviewed 2026-10-28
   */
  @RequiredArgsConstructor
  private enum Editors {
    ALL(EnumSet.of(PROVIDER, ADMIN)),
    ADMIN_ONLY(EnumSet.of(ADMIN)),
    NOBODY(EnumSet.noneOf(ActingRoleEnum.class));

    private final Set<ActingRoleEnum> roles;
  }
}
