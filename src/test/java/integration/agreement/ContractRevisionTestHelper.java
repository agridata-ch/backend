package integration.agreement;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ContractRevisionTestHelper {

  private final EntityManager entityManager;

  @Transactional
  public void setContractTemplateVersion(UUID contractRevisionId, String contractTemplateVersion) {
    entityManager.createNativeQuery("UPDATE contract_revision SET contract_template_version = :version WHERE id = :id")
        .setParameter("version", contractTemplateVersion)
        .setParameter("id", contractRevisionId)
        .executeUpdate();
  }
}
