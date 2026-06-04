package ch.agridata.product.persistence;

import ch.agridata.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Represents the persistence entity for a REST client configuration. This entity serves as a reference point for REST client
 * details used in configurations and products.
 *
 * @CommentLastReviewed 2026-06-11
 */

@Entity
@Table(name = "rest_client")
@SQLDelete(sql = "UPDATE rest_client SET archived = true WHERE id = ?")
@SQLRestriction("archived = false")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestClientEntity extends AuditableEntity {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "code", length = 50, nullable = false)
  private String code;
}
