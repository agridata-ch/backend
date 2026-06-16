
package ch.ech.xmlns.ech_0108._5;

import java.util.ArrayList;
import java.util.List;
import ch.ech.xmlns.ech_0097._5.UidStructureType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * &lt;p&gt;Java-Klasse für uidregInformationType complex type.&lt;/p&gt;
 * 
 * &lt;p&gt;Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.&lt;/p&gt;
 * 
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="uidregInformationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="uidregStatusEnterpriseDetail" type="{http://www.ech.ch/xmlns/eCH-0108/5}uidregStatusEnterpriseDetailType" minOccurs="0"/&gt;
 *         &lt;element name="uidregPublicStatus" type="{http://www.ech.ch/xmlns/eCH-0108/5}uidregPublicStatusType" minOccurs="0"/&gt;
 *         &lt;element name="uidregOrganisationType" type="{http://www.ech.ch/xmlns/eCH-0108/5}uidregOrganisationTypeType" minOccurs="0"/&gt;
 *         &lt;element name="uidregLiquidationReason" type="{http://www.ech.ch/xmlns/eCH-0108/5}uidregLiquidationReasonType" minOccurs="0"/&gt;
 *         &lt;element name="uidregSource" type="{http://www.ech.ch/xmlns/eCH-0108/5}uidRegSourceType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="uidReplacement" type="{http://www.ech.ch/xmlns/eCH-0097/5}uidStructureType" minOccurs="0"/&gt;
 *         &lt;element name="uidregUidService" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="uidregTranslation" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "uidregInformationType", propOrder = {
    "uidregStatusEnterpriseDetail",
    "uidregPublicStatus",
    "uidregOrganisationType",
    "uidregLiquidationReason",
    "uidregSource",
    "uidReplacement",
    "uidregUidService",
    "uidregTranslation"
})
public class UidregInformationType {

    protected String uidregStatusEnterpriseDetail;
    protected String uidregPublicStatus;
    protected String uidregOrganisationType;
    protected String uidregLiquidationReason;
    protected List<UidRegSourceType> uidregSource;
    protected UidStructureType uidReplacement;
    protected Boolean uidregUidService;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String uidregTranslation;

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
     * Ruft den Wert der uidregPublicStatus-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUidregPublicStatus() {
        return uidregPublicStatus;
    }

    /**
     * Legt den Wert der uidregPublicStatus-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUidregPublicStatus(String value) {
        this.uidregPublicStatus = value;
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

    /**
     * Ruft den Wert der uidregLiquidationReason-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUidregLiquidationReason() {
        return uidregLiquidationReason;
    }

    /**
     * Legt den Wert der uidregLiquidationReason-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUidregLiquidationReason(String value) {
        this.uidregLiquidationReason = value;
    }

    /**
     * Gets the value of the uidregSource property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a {@code set} method for the uidregSource property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getUidregSource().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link UidRegSourceType }
     * </p>
     * 
     * 
     * @return
     *     The value of the uidregSource property.
     */
    public List<UidRegSourceType> getUidregSource() {
        if (uidregSource == null) {
            uidregSource = new ArrayList<>();
        }
        return this.uidregSource;
    }

    /**
     * Ruft den Wert der uidReplacement-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UidStructureType }
     *     
     */
    public UidStructureType getUidReplacement() {
        return uidReplacement;
    }

    /**
     * Legt den Wert der uidReplacement-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UidStructureType }
     *     
     */
    public void setUidReplacement(UidStructureType value) {
        this.uidReplacement = value;
    }

    /**
     * Ruft den Wert der uidregUidService-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isUidregUidService() {
        return uidregUidService;
    }

    /**
     * Legt den Wert der uidregUidService-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUidregUidService(Boolean value) {
        this.uidregUidService = value;
    }

    /**
     * Ruft den Wert der uidregTranslation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUidregTranslation() {
        return uidregTranslation;
    }

    /**
     * Legt den Wert der uidregTranslation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUidregTranslation(String value) {
        this.uidregTranslation = value;
    }

}
