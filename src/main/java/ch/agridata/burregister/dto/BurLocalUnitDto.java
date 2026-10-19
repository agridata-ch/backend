package ch.agridata.burregister.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

/**
 * JAXB model of the {@code GetLocalUnitByUidResult} returned by BurWeb V1.8, reduced to the fields we use. Unknown elements are ignored
 * and elements marked {@code xsi:nil="true"} are unmarshalled as {@code null} (requires {@code nillable = true}).
 *
 * @CommentLastReviewed 2026-10-12
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class BurLocalUnitDto {

  public static final String NS = "http://schemas.datacontract.org/2004/07/CH.Admin.BIT.BurWeb.Services.External.V1_8";

  @XmlElement(namespace = NS, nillable = true)
  private String name;
  @XmlElement(namespace = NS, nillable = true)
  private String legalName;
  @XmlElement(namespace = NS, nillable = true)
  private String street;
  @XmlElement(namespace = NS, nillable = true)
  private String houseNumber;
  @XmlElement(namespace = NS, nillable = true)
  private String swissZipCode;
  @XmlElement(namespace = NS, nillable = true)
  private String foreignZipCode;
  @XmlElement(namespace = NS, nillable = true)
  private String town;
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName") // element name defined by BurWeb
  @XmlElement(namespace = NS, nillable = true)
  private String countryIdISO2;
  @XmlElement(namespace = NS, nillable = true)
  private Uid uid;

  /**
   * UID of the local unit, e.g. category {@code CHE} and id {@code 113614519}.
   *
   * @CommentLastReviewed 2026-10-12
   */
  @Data
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Uid {
    @XmlElement(namespace = NS, nillable = true)
    private String uidOrganisationId;
    @XmlElement(namespace = NS, nillable = true)
    private String uidOrganisationIdCategorie;
  }
}
