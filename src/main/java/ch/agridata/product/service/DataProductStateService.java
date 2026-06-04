package ch.agridata.product.service;

import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;

import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.common.utils.ValidationSchemaGenerator;
import ch.agridata.product.dto.DataProductDto;
import ch.agridata.product.dto.DataProductStateEnum;
import ch.agridata.product.mapper.DataProductMapper;
import ch.agridata.product.persistence.DataProductEntity;
import ch.agridata.product.persistence.DataProductRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.ws.rs.NotFoundException;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Service class responsible for handling the state transitions of data products.
 *
 * @CommentLastReviewed 2026-06-11
 */

@ApplicationScoped
@RequiredArgsConstructor
public class DataProductStateService {
  private final DataProductRepository dataProductRepository;
  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final DataProductMapper dataProductMapper;
  private final Validator validator;

  @Transactional
  @RolesAllowed(PROVIDER_ROLE)
  public DataProductDto setStateAsProvider(UUID dataProductId, DataProductStateEnum newState) {
    DataProductEntity dataProduct =
        dataProductRepository.findByIdAndDataProviderUidOptional(dataProductId, agridataSecurityIdentity.getUidOrElseThrow())
            .orElseThrow(() -> new NotFoundException(dataProductId.toString()));
    return setStateTo(dataProduct, newState);
  }

  @Transactional
  @RolesAllowed(ADMIN_ROLE)
  public DataProductDto setStateAsAdmin(UUID dataProductId, DataProductStateEnum newState) {
    DataProductEntity dataProduct = dataProductRepository.findByIdOptional(dataProductId)
        .orElseThrow(() -> new NotFoundException(dataProductId.toString()));
    return setStateTo(dataProduct, newState);
  }

  private DataProductDto setStateTo(DataProductEntity dataProduct, DataProductStateEnum newState) {
    if (dataProduct.getStateCode() != ch.agridata.product.persistence.DataProductStateEnum.DRAFT
        || newState != DataProductStateEnum.ACTIVE) {
      throw new IllegalStateException("Unable to transition from state " + dataProduct.getStateCode() + " to " + newState);
    }
    validate(dataProductMapper.toUpdateDto(dataProduct), ValidationSchemaGenerator.Submit.class);
    dataProduct.setStateCode(dataProductMapper.toPersistenceDataProductStateEnum(newState));
    return dataProductMapper.toDto(dataProduct);
  }

  private <T> void validate(T object, Class<?>... groups) {
    Set<ConstraintViolation<T>> violations = validator.validate(object, groups);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }
}
