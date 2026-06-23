package ch.agridata.product.service;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.dto.SupportedLanguage;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.product.api.DataProductApi;
import ch.agridata.product.dto.DataProductDto;
import ch.agridata.product.dto.DataProductProviderConfigurationDto;
import ch.agridata.product.dto.DataSourceSystemDto;
import ch.agridata.product.mapper.DataProductMapper;
import ch.agridata.product.mapper.DataSourceSystemMapper;
import ch.agridata.product.persistence.DataProductRepository;
import ch.agridata.product.persistence.DataProductStateEnum;
import ch.agridata.product.persistence.DataProviderEntity;
import ch.agridata.product.persistence.DataProviderRepository;
import ch.agridata.product.persistence.DataSourceSystemRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 * Service responsible for handling queries and operations related to data products.
 * It provides support for retrieving detailed information about data products, providers, and their configurations.
 *
 * @CommentLastReviewed 2026-06-11
 */

@ApplicationScoped
@RequiredArgsConstructor
public class DataProductQueryService implements DataProductApi {
  private final DataProductRepository dataProductRepository;
  private final DataProviderRepository dataProviderRepository;
  private final DataProductMapper dataProductMapper;
  private final DataSourceSystemRepository dataSourceSystemRepository;
  private final DataSourceSystemMapper dataSourceSystemMapper;
  private final AgridataSecurityIdentity agridataSecurityIdentity;

  public DataProductDto getDataProductAsAdmin(UUID dataProductId) {
    var entity = dataProductRepository.findByIdOptional(dataProductId)
        .orElseThrow(() -> new NotFoundException(dataProductId.toString()));
    return dataProductMapper.toDto(entity);
  }

  public DataProductDto getDataProductAsProvider(UUID dataProductId) {
    var entity = dataProductRepository.findByIdAndDataProviderUidOptional(dataProductId, agridataSecurityIdentity.getUidOrElseThrow())
        .orElseThrow(() -> new NotFoundException(dataProductId.toString()));
    return dataProductMapper.toDto(entity);
  }

  public List<DataProductDto> getActiveDataProducts() {
    return dataProductRepository.findAllActive().stream().map(dataProductMapper::toDto).toList();
  }

  public PageResponseDto<DataProductDto> getDataProductsPagedAsAdmin(
      ResourceQueryDto resourceQueryDto,
      SupportedLanguage language
  ) {
    var pagedEntities = dataProductRepository.findPaged(resourceQueryDto, language);

    return dataProductMapper.toPagedDataProductDto(pagedEntities);
  }

  public PageResponseDto<DataProductDto> getDataProductsPagedAsProvider(
      ResourceQueryDto resourceQueryDto,
      String providerUid,
      SupportedLanguage language
  ) {

    Optional<DataProviderEntity> optionalProvider = dataProviderRepository.findByUidOptional(providerUid);

    if (optionalProvider.isEmpty()) {
      throw new NotFoundException(providerUid);
    }

    var pagedEntities = dataProductRepository.findPagedByProviderUid(optionalProvider.get().getUid(), resourceQueryDto, language);

    return dataProductMapper.toPagedDataProductDto(pagedEntities);
  }

  @Override
  public DataProductDto getActiveProductById(UUID productId) {
    return dataProductRepository.findActiveByIdOptional(productId)
        .map(dataProductMapper::toDto)
        .orElseThrow(
            () -> new NotFoundException(productId.toString()));
  }

  @Override
  public DataProductProviderConfigurationDto getProviderConfigurationById(UUID productId) {
    return dataProductRepository.findActiveByIdOptional(productId)
        .map(dataProductMapper::toProviderConfigurationDto)
        .orElseThrow(
            () -> new NotFoundException(productId.toString()));
  }

  @Override
  public UUID getDataSourceSystemIdOfActiveProduct(UUID productId) {
    return dataProductRepository.findDataSourceSystemIdByProductIdOptional(productId)
        .orElseThrow(() -> new NotFoundException(productId.toString()));
  }

  @Override
  public DataSourceSystemDto getDataSourceSystem(UUID dataSourceSystemId) {
    return dataSourceSystemRepository.findByIdOptional(dataSourceSystemId)
        .map(dataSourceSystemMapper::toDto)
        .orElseThrow(() ->
            new NotFoundException("DataSourceSystem not found: " + dataSourceSystemId)
        );
  }

  @Override
  public List<DataProductDto> getActiveProductsByIds(List<UUID> productIds) {
    if (productIds == null || productIds.isEmpty()) {
      return List.of();
    }

    Map<UUID, DataProductDto> byId = dataProductRepository.find("id in ?1 and stateCode = ?2", productIds, DataProductStateEnum.ACTIVE)
        .list()
        .stream()
        .map(dataProductMapper::toDto)
        .collect(Collectors.toMap(DataProductDto::id, Function.identity()));

    return productIds.stream()
        .map(byId::get)
        .filter(Objects::nonNull)
        .toList();
  }
}
