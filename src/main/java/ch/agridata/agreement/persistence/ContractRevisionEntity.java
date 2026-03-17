package ch.agridata.agreement.persistence;

import ch.agridata.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Defines the persistence representation for contract revisions. It contains fields for storing values of
 * specific data request-fields at the time of contract revision initialization.
 *
 * @CommentLastReviewed: 2026-03-16
 */

@Entity
@Table(name = "contract_revision")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContractRevisionEntity extends AuditableEntity {
  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "data_request_id", nullable = false)
  private DataRequestEntity dataRequest;

  // DataRequest information

  @Column(name = "data_consumer_name", nullable = false)
  private String dataConsumerName;

  @Column(name = "data_consumer_city", nullable = false)
  private String dataConsumerCity;

  @Column(name = "data_provider_name", nullable = false)
  private String dataProviderName;

}
