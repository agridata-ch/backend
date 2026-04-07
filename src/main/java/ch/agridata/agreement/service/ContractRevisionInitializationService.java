package ch.agridata.agreement.service;

import ch.agridata.agreement.mapper.ContractRevisionMapper;
import ch.agridata.agreement.persistence.ContractRevisionEntity;
import ch.agridata.agreement.persistence.ContractRevisionRepository;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.product.api.DataProductApi;
import ch.agridata.product.dto.DataSourceSystemDto;
import ch.agridata.uidregister.api.UidRegisterServiceApi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.math.BigInteger;
import lombok.RequiredArgsConstructor;

/**
 * Initializes a new contract revision for a data request. After initialization, the contract revision is assigned to the request.
 *
 * @CommentLastReviewed 2026-03-16
 */

@ApplicationScoped
@RequiredArgsConstructor
public class ContractRevisionInitializationService {

  private final ContractRevisionRepository contractRevisionRepository;
  private final ContractRevisionMapper contractRevisionMapper;
  private final DataProductApi dataproductApi;
  private final UidRegisterServiceApi uidRegisterServiceApi;

  @Transactional
  public ContractRevisionEntity createAndAssignInitialRevision(DataRequestEntity dataRequest) {
    if (dataRequest.getCurrentContractRevisionId() != null) {
      throw new IllegalStateException("Data request already has a contract revision");
    }

    DataSourceSystemDto dataSourceSystem = dataproductApi.getDataSourceSystem(dataRequest.getDataSourceSystemId());
    var uid = dataSourceSystem.dataProvider().uid();
    var uidWithoutPrefix = new BigInteger(uid.replace("CHE", ""));
    var dataProvider = uidRegisterServiceApi.getByUid(uidWithoutPrefix);

    ContractRevisionEntity revision =
        contractRevisionMapper.toInitialEntity(
            dataRequest,
            dataProvider
        );

    contractRevisionRepository.persist(revision);
    dataRequest.setCurrentContractRevisionId(revision.getId());

    return revision;
  }
}
