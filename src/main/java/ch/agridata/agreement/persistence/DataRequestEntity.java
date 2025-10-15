package ch.agridata.agreement.persistence;

import ch.agridata.common.persistence.AuditableEntity;
import ch.agridata.common.persistence.TranslationPersistenceDto;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

/**
 * Models a data request for persistence. It aligns DTO structures with stored records.
 *
 * @CommentLastReviewed 2025-10-02
 */
@Entity
@Table(name = "data_request")
@SQLDelete(sql = "UPDATE data_request SET archived = true WHERE id = ?")
@SQLRestriction("archived = false")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataRequestEntity extends AuditableEntity {
  @Id
  @Column(name = "id", nullable = false, updatable = false)
  @GeneratedValue
  private UUID id;

  @NotNull
  @Column(name = "human_friendly_id", length = 4, unique = true)
  private String humanFriendlyId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "title")
  private TranslationPersistenceDto title;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "description")
  private TranslationPersistenceDto description;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "purpose")
  private TranslationPersistenceDto purpose;

  @NotNull
  @Column(name = "state_code", nullable = false)
  @Enumerated(EnumType.STRING)
  private DataRequestStateEnum stateCode;

  @Column(name = "data_consumer_display_name", length = 255)
  private String dataConsumerDisplayName;

  @Column(name = "data_consumer_legal_name", length = 255)
  private String dataConsumerLegalName;

  @Column(name = "data_consumer_uid", length = 20)
  private String dataConsumerUid;

  @Column(name = "data_consumer_city", length = 255)
  private String dataConsumerCity;

  @Column(name = "data_consumer_zip", length = 10)
  private String dataConsumerZip;

  @Column(name = "data_consumer_street", length = 255)
  private String dataConsumerStreet;

  @Column(name = "data_consumer_country", length = 2)
  private String dataConsumerCountry;

  @Column(name = "contact_phone_number", length = 50)
  private String contactPhoneNumber;

  @Column(name = "contact_email_address", length = 255)
  private String contactEmailAddress;

  @Lob
  @JdbcTypeCode(SqlTypes.BINARY)
  @Column(name = "data_consumer_logo")
  private byte[] dataConsumerLogo;

  @Column(name = "data_consumer_logo_type", length = 50)
  private String dataConsumerLogoType;

  @Column(name = "target_group", length = 150)
  private String targetGroup;

  @Column(name = "submission_date")
  private LocalDateTime submissionDate;

  @Column(name = "valid_redirect_uri_regex", length = 255)
  private String validRedirectUriRegex;

  @OneToMany(mappedBy = "dataRequest", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<DataRequestDataProductEntity> dataProducts = new ArrayList<>();

  /**
   * Lists the possible states of a data request.
   *
   * @CommentLastReviewed 2025-08-25
   */
  public enum DataRequestStateEnum {
    DRAFT, IN_REVIEW, TO_BE_SIGNED, ACTIVE
  }

}
