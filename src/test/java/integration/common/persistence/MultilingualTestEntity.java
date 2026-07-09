package integration.common.persistence;

import ch.agridata.common.persistence.TranslationPersistenceDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "multilingual_test_entity")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MultilingualTestEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public String code;
  public String category;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "name")
  public TranslationPersistenceDto name;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "description")
  public TranslationPersistenceDto description;
}
