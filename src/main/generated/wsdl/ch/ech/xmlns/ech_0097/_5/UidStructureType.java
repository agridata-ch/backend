
package ch.ech.xmlns.ech_0097._5;

import java.math.BigInteger;
import ch.ech.xmlns.ech_0116._4.ReportingRegister;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java-Klasse für uidStructureType complex type.&lt;/p&gt;
 * 
 * &lt;p&gt;Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.&lt;/p&gt;
 * 
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="uidStructureType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="uidOrganisationIdCategorie" type="{http://www.ech.ch/xmlns/eCH-0097/5}uidOrganisationIdCategorieType"/&gt;
 *         &lt;element name="uidOrganisationId" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "uidStructureType", propOrder = {
    "uidOrganisationIdCategorie",
    "uidOrganisationId"
})
@XmlSeeAlso({
    ReportingRegister.class
})
public class UidStructureType {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected UidOrganisationIdCategorieType uidOrganisationIdCategorie;
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger uidOrganisationId;

    /**
     * Ruft den Wert der uidOrganisationIdCategorie-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UidOrganisationIdCategorieType }
     *     
     */
    public UidOrganisationIdCategorieType getUidOrganisationIdCategorie() {
        return uidOrganisationIdCategorie;
    }

    /**
     * Legt den Wert der uidOrganisationIdCategorie-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UidOrganisationIdCategorieType }
     *     
     */
    public void setUidOrganisationIdCategorie(UidOrganisationIdCategorieType value) {
        this.uidOrganisationIdCategorie = value;
    }

    /**
     * Ruft den Wert der uidOrganisationId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getUidOrganisationId() {
        return uidOrganisationId;
    }

    /**
     * Legt den Wert der uidOrganisationId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setUidOrganisationId(BigInteger value) {
        this.uidOrganisationId = value;
    }

}
