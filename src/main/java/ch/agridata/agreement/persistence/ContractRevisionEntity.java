package ch.agridata.agreement.persistence;

import ch.agridata.common.persistence.AuditableEntity;
import ch.agridata.common.persistence.TranslationPersistenceDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
  @Column(name = "id", nullable = false, updatable = false)
  @Builder.Default
  // UUID is pre-assigned at construction time so it is available before persist, allowing it to be used as the S3 key for the contract PDF.
  private UUID id = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "data_request_id", nullable = false)
  private DataRequestEntity dataRequest;

  // DataRequest information

  @Column(name = "data_consumer_name", nullable = false)
  private String dataConsumerName;

  @Column(name = "data_consumer_street", nullable = false)
  private String dataConsumerStreet;

  @Column(name = "data_consumer_zip", length = 10, nullable = false)
  private String dataConsumerZip;

  @Column(name = "data_consumer_city", nullable = false)
  private String dataConsumerCity;

  @Column(name = "data_provider_name", nullable = false)
  private String dataProviderName;

  @Column(name = "data_provider_street", nullable = false)
  private String dataProviderStreet;

  @Column(name = "data_provider_zip", length = 10, nullable = false)
  private String dataProviderZip;

  @Column(name = "data_provider_city", nullable = false)
  private String dataProviderCity;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "title", nullable = false)
  @Valid
  private TranslationPersistenceDto title;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "description", nullable = false)
  @Valid
  private TranslationPersistenceDto description;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "purpose", nullable = false)
  @Valid
  private TranslationPersistenceDto purpose;

  @Column(name = "contact_phone_number", length = 50, nullable = false)
  private String contactPhoneNumber;

  @Column(name = "contact_email_address", length = 255, nullable = false)
  private String contactEmailAddress;

  @Column(name = "target_group", length = 150, nullable = false)
  private String targetGroup;

  @Column(name = "data_consumer_uid", length = 20, nullable = false)
  private String dataConsumerUid;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "system_name", nullable = false)
  private TranslationPersistenceDto systemName;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "data_product", nullable = false)
  private List<TranslationPersistenceDto> dataProducts;

  // Signature information

  @Column(name = "consumer_signature_user_id1")
  private UUID consumerSignatureUserId1;

  @Column(name = "consumer_signature_name1")
  private String consumerSignatureName1;

  @Column(name = "consumer_signature_timestamp1")
  private LocalDateTime consumerSignatureTimestamp1;

  @Column(name = "consumer_signature_user_id2")
  private UUID consumerSignatureUserId2;

  @Column(name = "consumer_signature_name2")
  private String consumerSignatureName2;

  @Column(name = "consumer_signature_timestamp2")
  private LocalDateTime consumerSignatureTimestamp2;

  @Column(name = "provider_signature_user_id1")
  private UUID providerSignatureUserId1;

  @Column(name = "provider_signature_name1")
  private String providerSignatureName1;

  @Column(name = "provider_signature_timestamp1")
  private LocalDateTime providerSignatureTimestamp1;

  @Column(name = "provider_signature_user_id2")
  private UUID providerSignatureUserId2;

  @Column(name = "provider_signature_name2")
  private String providerSignatureName2;

  @Column(name = "provider_signature_timestamp2")
  private LocalDateTime providerSignatureTimestamp2;

  @Column(name = "seal_state", nullable = false)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private SealAttemptState sealState = SealAttemptState.NOT_STARTED;

  @Column(name = "seal_started_at")
  private LocalDateTime sealStartedAt;

  @Column(name = "consumer_signature_type")
  @Enumerated(EnumType.STRING)
  private SignatureTypeEnum consumerSignatureType;

  @Column(name = "provider_signature_type")
  @Enumerated(EnumType.STRING)
  private SignatureTypeEnum providerSignatureType;

  /**
   * Represents the state of a seal attempt.
   *
   * @CommentLastReviewed 2026-04-14
   */
  public enum SealAttemptState {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    FAILED
  }
}
