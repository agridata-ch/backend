
package ch.ech.xmlns.ech_0097._5;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * &lt;p&gt;Java-Klasse für organisationIdentificationType complex type.&lt;/p&gt;
 * 
 * &lt;p&gt;Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.&lt;/p&gt;
 * 
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="organisationIdentificationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="uid" type="{http://www.ech.ch/xmlns/eCH-0097/5}uidStructureType" minOccurs="0"/&gt;
 *         &lt;element name="localOrganisationId" type="{http://www.ech.ch/xmlns/eCH-0097/5}namedOrganisationIdType" minOccurs="0"/&gt;
 *         &lt;element name="OtherOrganisationId" type="{http://www.ech.ch/xmlns/eCH-0097/5}namedOrganisationIdType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="organisationName" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;element name="organisationLegalName" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;element name="organisationAdditionalName" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;element name="legalForm" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "organisationIdentificationType", propOrder = {
    "uid",
    "localOrganisationId",
    "otherOrganisationId",
    "organisationName",
    "organisationLegalName",
    "organisationAdditionalName",
    "legalForm"
})
public class OrganisationIdentificationType {

    protected UidStructureType uid;
    protected NamedOrganisationIdType localOrganisationId;
    @XmlElement(name = "OtherOrganisationId")
    protected List<NamedOrganisationIdType> otherOrganisationId;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String organisationName;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String organisationLegalName;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String organisationAdditionalName;
    protected String legalForm;

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
     * Ruft den Wert der localOrganisationId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link NamedOrganisationIdType }
     *     
     */
    public NamedOrganisationIdType getLocalOrganisationId() {
        return localOrganisationId;
    }

    /**
     * Legt den Wert der localOrganisationId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link NamedOrganisationIdType }
     *     
     */
    public void setLocalOrganisationId(NamedOrganisationIdType value) {
        this.localOrganisationId = value;
    }

    /**
     * Gets the value of the otherOrganisationId property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a {@code set} method for the otherOrganisationId property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getOtherOrganisationId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NamedOrganisationIdType }
     * </p>
     * 
     * 
     * @return
     *     The value of the otherOrganisationId property.
     */
    public List<NamedOrganisationIdType> getOtherOrganisationId() {
        if (otherOrganisationId == null) {
            otherOrganisationId = new ArrayList<>();
        }
        return this.otherOrganisationId;
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
     * Ruft den Wert der organisationLegalName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrganisationLegalName() {
        return organisationLegalName;
    }

    /**
     * Legt den Wert der organisationLegalName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrganisationLegalName(String value) {
        this.organisationLegalName = value;
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
     * Ruft den Wert der legalForm-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLegalForm() {
        return legalForm;
    }

    /**
     * Legt den Wert der legalForm-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLegalForm(String value) {
        this.legalForm = value;
    }

}
