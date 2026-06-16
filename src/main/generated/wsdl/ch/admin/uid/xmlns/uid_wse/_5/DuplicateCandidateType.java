
package ch.admin.uid.xmlns.uid_wse._5;

import ch.ech.xmlns.ech_0097._5.UidStructureType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * &lt;p&gt;Java-Klasse für duplicateCandidateType complex type.&lt;/p&gt;
 * 
 * &lt;p&gt;Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.&lt;/p&gt;
 * 
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="duplicateCandidateType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="rating" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="uid" type="{http://www.ech.ch/xmlns/eCH-0097/5}uidStructureType" minOccurs="0"/&gt;
 *         &lt;element name="organisationName" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;element name="organisationAdditionalName" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;element name="street" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;element name="swissZipCode" type="{http://www.w3.org/2001/XMLSchema}unsignedInt"/&gt;
 *         &lt;element name="town" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;element name="uidregStatusEnterpriseDetail" type="{http://www.ech.ch/xmlns/eCH-0108/5}uidregStatusEnterpriseDetailType"/&gt;
 *         &lt;element name="uidregOrganisationType" type="{http://www.ech.ch/xmlns/eCH-0108/5}uidregOrganisationTypeType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "duplicateCandidateType", propOrder = {
    "rating",
    "uid",
    "organisationName",
    "organisationAdditionalName",
    "street",
    "swissZipCode",
    "town",
    "uidregStatusEnterpriseDetail",
    "uidregOrganisationType"
})
public class DuplicateCandidateType {

    protected int rating;
    protected UidStructureType uid;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String organisationName;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String organisationAdditionalName;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String street;
    @XmlSchemaType(name = "unsignedInt")
    protected long swissZipCode;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String town;
    @XmlElement(required = true)
    protected String uidregStatusEnterpriseDetail;
    @XmlElement(required = true)
    protected String uidregOrganisationType;

    /**
     * Ruft den Wert der rating-Eigenschaft ab.
     * 
     */
    public int getRating() {
        return rating;
    }

    /**
     * Legt den Wert der rating-Eigenschaft fest.
     * 
     */
    public void setRating(int value) {
        this.rating = value;
    }

    /**
     * Ruft den Wert der uid-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UidStructureType }
     *     
     */
    public UidStructureType getUid() {
        return uid;
    }

    /**
     * Legt den Wert der uid-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UidStructureType }
     *     
     */
    public void setUid(UidStructureType value) {
        this.uid = value;
    }

    /**
     * Ruft den Wert der organisationName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrganisationName() {
        return organisationName;
    }

    /**
     * Legt den Wert der organisationName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrganisationName(String value) {
        this.organisationName = value;
    }

    /**
     * Ruft den Wert der organisationAdditionalName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrganisationAdditionalName() {
        return organisationAdditionalName;
    }

    /**
     * Legt den Wert der organisationAdditionalName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrganisationAdditionalName(String value) {
        this.organisationAdditionalName = value;
    }

    /**
     * Ruft den Wert der street-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStreet() {
        return street;
    }

    /**
     * Legt den Wert der street-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStreet(String value) {
        this.street = value;
    }

    /**
     * Ruft den Wert der swissZipCode-Eigenschaft ab.
     * 
     */
    public long getSwissZipCode() {
        return swissZipCode;
    }

    /**
     * Legt den Wert der swissZipCode-Eigenschaft fest.
     * 
     */
    public void setSwissZipCode(long value) {
        this.swissZipCode = value;
    }

    /**
     * Ruft den Wert der town-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTown() {
        return town;
    }

    /**
     * Legt den Wert der town-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTown(String value) {
        this.town = value;
    }

    /**
     * Ruft den Wert der uidregStatusEnterpriseDetail-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUidregStatusEnterpriseDetail() {
        return uidregStatusEnterpriseDetail;
    }

    /**
     * Legt den Wert der uidregStatusEnterpriseDetail-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUidregStatusEnterpriseDetail(String value) {
        this.uidregStatusEnterpriseDetail = value;
    }

    /**
     * Ruft den Wert der uidregOrganisationType-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUidregOrganisationType() {
        return uidregOrganisationType;
    }

    /**
     * Legt den Wert der uidregOrganisationType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUidregOrganisationType(String value) {
        this.uidregOrganisationType = value;
    }

}
