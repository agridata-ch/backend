package ch.agridata.agreement.service;

import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;

import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.mapper.DataRequestMapper;
import ch.agridata.agreement.persistence.DataRequestRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Provides querying capabilities for data requests. It enables consumers to search and retrieve requests based on criteria.
 *
 * @CommentLastReviewed 2025-08-25
 */

@ApplicationScoped
@RequiredArgsConstructor
public class DataRequestQueryService {

  private final DataRequestRepository dataRequestRepository;
  private final DataRequestMapper dataRequestMapper;
  private final AgridataSecurityIdentity agridataSecurityIdentity;

  @RolesAllowed(CONSUMER_ROLE)
  public List<DataRequestDto> getAllDataRequestsOfCurrentConsumer() {
    var dataRequestEntities = dataRequestRepository.findByDataConsumerUid(agridataSecurityIdentity.getUidOrElseThrow());
    return dataRequestEntities.stream()
        .map(dataRequestMapper::toDto)
        .toList();
  }

  @RolesAllowed(ADMIN_ROLE)
  public List<DataRequestDto> getAllDataRequests() {
    return dataRequestRepository.findAll().list().stream()
        .map(dataRequestMapper::toDto)
        .toList();
  }

  @RolesAllowed(CONSUMER_ROLE)
  public DataRequestDto getDataRequestOfCurrentConsumer(UUID requestId) {
    return dataRequestRepository.findByIdAndDataConsumerUid(requestId, agridataSecurityIdentity.getUidOrElseThrow())
        .map(dataRequestMapper::toDto)
        .orElseThrow(() -> new NotFoundException(requestId.toString()));
  }

  @RolesAllowed({ADMIN_ROLE})
  public DataRequestDto getDataRequest(UUID requestId) {
    return dataRequestRepository.findByIdOptional(requestId)
        .map(dataRequestMapper::toDto)
        .orElseThrow(() -> new NotFoundException(requestId.toString()));
  }

}
