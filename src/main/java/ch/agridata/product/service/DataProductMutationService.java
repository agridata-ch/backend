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
import ch.agridata.product.persistence.DataSourceSystemEntity;
import ch.agridata.product.persistence.DataSourceSystemRepository;
import ch.agridata.product.persistence.RestClientEntity;
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
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

/**
 * Service class for managing and mutating data product entities. Provides functionality for adding,
 * updating, patching and deleting data product drafts either as a data provider or as an administrator.
 * Deleting a draft cascades to its documents (see {@link DataProductDocumentService}).
 * Enforces role-based access control and performs necessary validations for consistency and security.
 *
 * @CommentLastReviewed 2026-07-29
 */

@ApplicationScoped
@RequiredArgsConstructor
public class DataProductMutationService {
  private final RestClientRepository restClientRepository;
  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final DataProductMapper dataProductMapper;
  private final DataProductRepository dataProductRepository;
  private final DataSourceSystemRepository dataSourceSystemRepository;
  private final DataProductDocumentService dataProductDocumentService;
  private final Validator validator;

  @Transactional
  @RolesAllowed(PROVIDER_ROLE)
  public DataProductDto addDataProductDraftAsProvider(DataProductUpdateDto updateDto) {
    var dataSourceSystem = asCurrentProvider().dataSourceSystem().apply(updateDto.dataSourceSystemId());
    var restClient = asCurrentProvider().restClient().apply(updateDto.restClientId());

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
    var dataSourceSystem = asAdmin().dataSourceSystem().apply(updateDto.dataSourceSystemId());
    var restClient = asAdmin().restClient().apply(updateDto.restClientId());

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
  public DataProductDto patchActiveDataProductAsProvider(@NotNull UUID dataProductId, @NotNull DataProductUpdateDto updateDto) {
    return patchActive(dataProductId, updateDto, asCurrentProvider(), ValidationSchemaGenerator.PatchAsProvider.class);
  }

  @Transactional
  @RolesAllowed(ADMIN_ROLE)
  public DataProductDto patchActiveDataProductAsAdmin(@NotNull UUID dataProductId, @NotNull DataProductUpdateDto updateDto) {
    return patchActive(dataProductId, updateDto, asAdmin(), ValidationSchemaGenerator.PatchAsAdmin.class);
  }

  @Transactional
  @RolesAllowed(PROVIDER_ROLE)
  public DataProductDto updateDataProductDraftAsProvider(@NotNull UUID dataProductId, @NotNull DataProductUpdateDto updateDto) {
    return updateDraft(dataProductId, updateDto, asCurrentProvider());
  }

  @Transactional
  @RolesAllowed(ADMIN_ROLE)
  public DataProductDto updateDataProductDraftAsAdmin(@NotNull UUID dataProductId, @NotNull DataProductUpdateDto updateDto) {
    return updateDraft(dataProductId, updateDto, asAdmin());
  }

  private DataProductDto patchActive(UUID dataProductId, DataProductUpdateDto updateDto, Resolver resolver, Class<?> validationGroup) {
    validate(updateDto, validationGroup);

    var entity = resolver.dataProduct().apply(dataProductId);
    verifyState(entity, DataProductStateEnum.ACTIVE);

    if (updateDto.restClientId() != null) {
      entity.setRestClient(resolver.restClient().apply(updateDto.restClientId()));
    }

    return patch(updateDto, entity);
  }

  private DataProductDto updateDraft(UUID dataProductId, DataProductUpdateDto updateDto, Resolver resolver) {
    var entity = resolver.dataProduct().apply(dataProductId);
    verifyState(entity, DataProductStateEnum.DRAFT);

    var dataSourceSystem = resolver.dataSourceSystem().apply(updateDto.dataSourceSystemId());
    var restClient = resolver.restClient().apply(updateDto.restClientId());

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

  private void validate(DataProductUpdateDto dto, Class<?> group) {
    Set<ConstraintViolation<DataProductUpdateDto>> violations = validator.validate(dto, group);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }

  @Transactional
  @RolesAllowed(PROVIDER_ROLE)
  public void deleteDataProductDraftAsProvider(@NotNull UUID dataProductId) {
    deleteDataProductDraft(asCurrentProvider().dataProduct().apply(dataProductId));
  }

  @Transactional
  @RolesAllowed(ADMIN_ROLE)
  public void deleteDataProductDraftAsAdmin(@NotNull UUID dataProductId) {
    deleteDataProductDraft(asAdmin().dataProduct().apply(dataProductId));
  }

  private void deleteDataProductDraft(DataProductEntity entity) {
    verifyState(entity, DataProductStateEnum.DRAFT);
    dataProductDocumentService.deleteAllDataProductDocuments(entity.getId());
    dataProductRepository.delete(entity);
  }

  private record Resolver(
      Function<UUID, DataProductEntity> dataProduct,
      Function<UUID, DataSourceSystemEntity> dataSourceSystem,
      Function<UUID, RestClientEntity> restClient) {
  }

  private Resolver asCurrentProvider() {
    var uid = agridataSecurityIdentity.getUidOrElseThrow();
    return new Resolver(
        id -> resolve(id, i -> dataProductRepository.findByIdAndDataProviderUidOptional(i, uid)),
        id -> resolve(id, i -> dataSourceSystemRepository.findByIdAndProviderUidOptional(i, uid)),
        id -> resolve(id, i -> restClientRepository.findByIdAndProviderUidOptional(i, uid)));
  }

  private Resolver asAdmin() {
    return new Resolver(
        id -> resolve(id, dataProductRepository::findByIdOptional),
        id -> resolve(id, dataSourceSystemRepository::findByIdOptional),
        id -> resolve(id, restClientRepository::findByIdOptional));
  }

  private <T> T resolve(UUID id, Function<UUID, Optional<T>> finder) {
    if (id == null) {
      return null;
    }
    return finder.apply(id).orElseThrow(() -> new NotFoundException(id.toString()));
  }
}
