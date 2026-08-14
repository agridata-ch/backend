package ch.agridata.agreement.persistence;

import ch.agridata.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.SQLRestriction;

/**
 * Read-only projection over {@code consent_request} used to load consents cheaply during data transfers.
 *
 * @CommentLastReviewed 2026-08-14
 */
@Entity
@Immutable
@Table(name = "consent_request")
@SQLRestriction("archived = false")
@Getter
public class ConsentRequestFundamentalViewEntity extends AuditableEntity {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "data_request_id", nullable = false)
  private UUID dataRequestId;

  @Column(name = "data_producer_uid", nullable = false, length = 50)
  private String dataProducerUid;

  @Column(name = "data_producer_bur", length = 50)
  private String dataProducerBur;

  @Column(name = "state_code", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  private ConsentRequestEntity.StateEnum stateCode;

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
}
