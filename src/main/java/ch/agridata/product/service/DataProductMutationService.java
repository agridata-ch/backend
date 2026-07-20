package ch.agridata.product.service;

import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;

import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.common.utils.ValidationSchemaGenerator;
import ch.agridata.product.dto.DataProductDto;
import ch.agridata.product.dto.DataProductUpdateDto;
import ch.agridata.product.mapper.DataProductMapper;
import ch.agridata.product.persistence.DataProductEntity;
import ch.agridata.product.persistence.DataProductRepository;
import ch.agridata.product.persistence.DataProductStateEnum;
import ch.agridata.product.persistence.DataSourceSystemRepository;
import ch.agridata.product.persistence.RestClientRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.NotFoundException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

/**
 * Service class for managing and mutating data product entities. Provides functionality for adding,
 * updating, and patching data product drafts either as a data provider or as an administrator.
 * Enforces role-based access control and performs necessary validations for consistency and security.
 *
 * @CommentLastReviewed 2026-07-22
 */

@ApplicationScoped
@RequiredArgsConstructor
public class DataProductMutationService {
  private final RestClientRepository restClientRepository;
  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final DataProductMapper dataProductMapper;
  private final DataProductRepository dataProductRepository;
  private final DataSourceSystemRepository dataSourceSystemRepository;
  private final Validator validator;

  @Transactional
  @RolesAllowed(PROVIDER_ROLE)
  public DataProductDto addDataProductDraftAsProvider(DataProductUpdateDto updateDto) {
    var dataSourceSystem =
        resolveForCurrentProvider(updateDto.dataSourceSystemId(), dataSourceSystemRepository::findByIdAndProviderUidOptional);
    var restClient = resolveForCurrentProvider(updateDto.restClientId(), restClientRepository::findByIdAndProviderUidOptional);

    var entity = DataProductEntity.builder()
        .dataSourceSystem(dataSourceSystem)
        .dataProviderUid(agridataSecurityIdentity.getUidOrElseThrow())
        .restClient(restClient)
        .stateCode(DataProductStateEnum.DRAFT)
        .build();

    return applyAndPersist(updateDto, entity);
  }

  @Transactional
  @RolesAllowed(ADMIN_ROLE)
  public DataProductDto addDataProductDraftAsAdmin(DataProductUpdateDto updateDto) {
    var dataSourceSystem = resolve(updateDto.dataSourceSystemId(), dataSourceSystemRepository::findByIdOptional);
    var restClient = resolve(updateDto.restClientId(), restClientRepository::findByIdOptional);

    var entity = DataProductEntity.builder()
        .dataSourceSystem(dataSourceSystem)
        .dataProviderUid(dataSourceSystem != null ? dataSourceSystem.getDataProvider().getUid() : null)
        .restClient(restClient)
        .stateCode(DataProductStateEnum.DRAFT)
        .build();

    return applyAndPersist(updateDto, entity);
  }

  @Transactional
  @RolesAllowed(PROVIDER_ROLE)
  public DataProductDto patchDataProductAsProvider(@NotNull UUID dataProductId, @NotNull DataProductUpdateDto updateDto) {
    validate(updateDto, ValidationSchemaGenerator.PatchAsProvider.class);

    var entity = resolveForCurrentProvider(dataProductId, dataProductRepository::findByIdAndDataProviderUidOptional);

    verifyState(entity, DataProductStateEnum.ACTIVE);

    if (updateDto.restClientId() != null) {
      var restClient = resolveForCurrentProvider(updateDto.restClientId(), restClientRepository::findByIdAndProviderUidOptional);
      entity.setRestClient(restClient);
    }

    return patch(updateDto, entity);
  }

  @Transactional
  @RolesAllowed(ADMIN_ROLE)
  public DataProductDto patchDataProductAsAdmin(@NotNull UUID dataProductId, @NotNull DataProductUpdateDto updateDto) {
    validate(updateDto, ValidationSchemaGenerator.PatchAsAdmin.class);

    var entity = resolve(dataProductId, dataProductRepository::findByIdOptional);

    verifyState(entity, DataProductStateEnum.ACTIVE);

    if (updateDto.restClientId() != null) {
      var restClient = resolve(updateDto.restClientId(), restClientRepository::findByIdOptional);
      entity.setRestClient(restClient);
    }

    return patch(updateDto, entity);
  }

  @Transactional
  @RolesAllowed(PROVIDER_ROLE)
  public DataProductDto updateDataProductDraftAsProvider(@NotNull UUID dataProductId, @NotNull DataProductUpdateDto updateDto) {
    var entity = resolveForCurrentProvider(dataProductId, dataProductRepository::findByIdAndDataProviderUidOptional);
    verifyState(entity, DataProductStateEnum.DRAFT);

    var dataSourceSystem =
        resolveForCurrentProvider(updateDto.dataSourceSystemId(), dataSourceSystemRepository::findByIdAndProviderUidOptional);
    var restClient = resolveForCurrentProvider(updateDto.restClientId(), restClientRepository::findByIdAndProviderUidOptional);

    entity.setDataSourceSystem(dataSourceSystem);
    entity.setRestClient(restClient);

    return applyAndPersist(updateDto, entity);
  }

  @Transactional
  @RolesAllowed(ADMIN_ROLE)
  public DataProductDto updateDataProductDraftAsAdmin(@NotNull UUID dataProductId, @NotNull DataProductUpdateDto updateDto) {
    var entity = resolve(dataProductId, dataProductRepository::findByIdOptional);
    verifyState(entity, DataProductStateEnum.DRAFT);

    var dataSourceSystem = resolve(updateDto.dataSourceSystemId(), dataSourceSystemRepository::findByIdOptional);
    var restClient = resolve(updateDto.restClientId(), restClientRepository::findByIdOptional);

    entity.setDataSourceSystem(dataSourceSystem);

    // dataProviderUid is only updated when a new dataSourceSystem is assigned.
    // Removing the dataSourceSystem (null) intentionally preserves the existing
    // provider association, since the product still belongs to the same provider.
    if (dataSourceSystem != null) {
      entity.setDataProviderUid(dataSourceSystem.getDataProvider().getUid());
    }
    entity.setRestClient(restClient);

    return applyAndPersist(updateDto, entity);
  }

  private DataProductDto applyAndPersist(DataProductUpdateDto updateDto, DataProductEntity entity) {
    verifyRestClientOwnership(entity);
    dataProductMapper.updateEntity(updateDto, entity);
    dataProductRepository.persist(entity);
    return dataProductMapper.toDto(entity);
  }

  private DataProductDto patch(DataProductUpdateDto updateDto, DataProductEntity entity) {
    verifyRestClientOwnership(entity);
    validate(dataProductMapper.toUpdateDto(entity), ValidationSchemaGenerator.Submit.class);
    dataProductMapper.patchEntity(updateDto, entity);
    // No .persist() called, because the entity is already managed by the persistence context.
    return dataProductMapper.toDto(entity);
  }

  private void verifyState(DataProductEntity entity, DataProductStateEnum state) {
    if (entity.getStateCode() != state) {
      throw new IllegalStateException(
          "Data product " + entity.getId() + " must be in state " + state + " to be edited, but was " + entity.getStateCode());
    }
  }

  private void verifyRestClientOwnership(DataProductEntity entity) {
    if (entity.getDataSourceSystem() != null && entity.getRestClient() != null
        && !entity.getDataSourceSystem().getDataProvider().getRestClients().contains(entity.getRestClient())) {
      throw new ValidationException(
          "Rest client " + entity.getRestClient().getId() + " is not assigned to data provider "
              + entity.getDataSourceSystem().getDataProvider().getId());
    }
  }

  private <T> T resolve(UUID id, Function<UUID, Optional<T>> finder) {
    if (id == null) {
      return null;
    }
    return finder.apply(id).orElseThrow(() -> new NotFoundException(id.toString()));
  }

  private <T> T resolveForCurrentProvider(UUID id, BiFunction<UUID, String, Optional<T>> finder) {
    if (id == null) {
      return null;
    }
    var uid = agridataSecurityIdentity.getUidOrElseThrow();
    return finder.apply(id, uid).orElseThrow(() -> new NotFoundException(id.toString()));
  }

  private void validate(DataProductUpdateDto dto, Class<?> group) {
    Set<ConstraintViolation<DataProductUpdateDto>> violations = validator.validate(dto, group);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }
}
