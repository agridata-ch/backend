package ch.agridata.agreement.persistence;

import ch.agridata.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Defines the persistence representation of a consent request. It captures identifiers, states, and related attributes.
 *
 * @CommentLastReviewed 2026-02-26
 */
@Entity
@Table(name = "consent_request",
    indexes = {
        @Index(name = "idx_consent_request_data_producer_uid", columnList = "data_producer_uid")
    })
@SQLDelete(sql = "UPDATE consent_request SET archived = true WHERE id = ?")
@SQLRestriction("archived = false")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsentRequestEntity extends AuditableEntity {
  @Id
  @Column(name = "id", nullable = false, updatable = false)
  @GeneratedValue
  private UUID id;

  @Column(name = "request_date")
  private LocalDateTime requestDate;

  @Column(name = "state_code", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  private StateEnum stateCode;

  @Column(name = "last_state_change_date")
  private LocalDateTime lastStateChangeDate;

  @Column(name = "data_producer_uid", nullable = false, length = 50)
  private String dataProducerUid;

  @Column(name = "data_producer_bur", length = 50)
  private String dataProducerBur;

  @Column(name = "migrated_from_maf_date")
  private LocalDateTime migratedFromMafDate;

  @Column(name = "uid_bur_relation_since")
  private LocalDateTime uidBurRelationSince;

  @Column(name = "uid_bur_relation_until")
  private LocalDateTime uidBurRelationUntil;

  @Formula("""
      CASE
        WHEN state_code = 'GRANTED' AND data_producer_bur IS NULL THEN DATE '1970-01-01'
        WHEN state_code = 'GRANTED' AND data_producer_bur IS NOT NULL THEN uid_bur_relation_since::date
      END
      """)
  private LocalDate grantedDataPeriodFrom;

  @Formula("""
      CASE
        WHEN state_code = 'GRANTED' AND data_producer_bur IS NULL THEN DATE '9999-12-31'
        WHEN state_code = 'GRANTED' AND data_producer_bur IS NOT NULL
          THEN COALESCE(uid_bur_relation_until::date, DATE '9999-12-31')
      END
      """)
  private LocalDate grantedDataPeriodTo;

  @ManyToOne
  @JoinColumn(name = "data_request_id", nullable = false)
  private DataRequestEntity dataRequest;

  public void setStateCode(StateEnum stateCode) {
    this.lastStateChangeDate = LocalDateTime.now();
    this.stateCode = stateCode;
  }

  public boolean isShowStateAsMigrated() {
    if (migratedFromMafDate == null) {
      return false;
    }
    return lastStateChangeDate == null || lastStateChangeDate.isBefore(migratedFromMafDate);
  }

  /**
   * Lists the possible states of a consent request.
   *
   * @CommentLastReviewed 2025-08-25
   */
  public enum StateEnum {
    GRANTED,
    OPENED,
    DECLINED
  }
}
