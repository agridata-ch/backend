
package ch.ech.xmlns.ech_0098._5;

import java.util.ArrayList;
import java.util.List;
import ch.ech.xmlns.ech_0046._5.ContactType;
import ch.ech.xmlns.ech_0097._5.OrganisationIdentificationType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * &lt;p&gt;Java-Klasse für organisationType complex type.&lt;/p&gt;
 * 
 * &lt;p&gt;Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.&lt;/p&gt;
 * 
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="organisationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="organisationIdentification" type="{http://www.ech.ch/xmlns/eCH-0097/5}organisationIdentificationType" minOccurs="0"/&gt;
 *         &lt;element name="uidBrancheText" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;element name="NOGACode" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;element name="foundation" type="{http://www.ech.ch/xmlns/eCH-0098/5}foundationType" minOccurs="0"/&gt;
 *         &lt;element name="liquidation" type="{http://www.ech.ch/xmlns/eCH-0098/5}liquidationType" minOccurs="0"/&gt;
 *         &lt;element name="address" type="{http://www.ech.ch/xmlns/eCH-0098/5}organisationAddressType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="contact" type="{http://www.ech.ch/xmlns/eCH-0046/5}contactType" minOccurs="0"/&gt;
 *         &lt;element name="languageOfCorrespondance" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "organisationType", propOrder = {
    "organisationIdentification",
    "uidBrancheText",
    "nogaCode",
    "foundation",
    "liquidation",
    "address",
    "contact",
    "languageOfCorrespondance"
})
public class OrganisationType {

    protected OrganisationIdentificationType organisationIdentification;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String uidBrancheText;
    @XmlElement(name = "NOGACode")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String nogaCode;
    protected FoundationType foundation;
    protected LiquidationType liquidation;
    protected List<OrganisationAddressType> address;
    protected ContactType contact;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String languageOfCorrespondance;

    /**
     * Ruft den Wert der organisationIdentification-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link OrganisationIdentificationType }
     *     
     */
    public OrganisationIdentificationType getOrganisationIdentification() {
        return organisationIdentification;
    }

    /**
     * Legt den Wert der organisationIdentification-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganisationIdentificationType }
     *     
     */
    public void setOrganisationIdentification(OrganisationIdentificationType value) {
        this.organisationIdentification = value;
    }

    /**
     * Ruft den Wert der uidBrancheText-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUidBrancheText() {
        return uidBrancheText;
    }

    /**
     * Legt den Wert der uidBrancheText-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUidBrancheText(String value) {
        this.uidBrancheText = value;
    }

    /**
     * Ruft den Wert der nogaCode-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNOGACode() {
        return nogaCode;
    }

    /**
     * Legt den Wert der nogaCode-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNOGACode(String value) {
        this.nogaCode = value;
    }

    /**
     * Ruft den Wert der foundation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FoundationType }
     *     
     */
    public FoundationType getFoundation() {
        return foundation;
    }

    /**
     * Legt den Wert der foundation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FoundationType }
     *     
     */
    public void setFoundation(FoundationType value) {
        this.foundation = value;
    }

    /**
     * Ruft den Wert der liquidation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link LiquidationType }
     *     
     */
    public LiquidationType getLiquidation() {
        return liquidation;
    }

    /**
     * Legt den Wert der liquidation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link LiquidationType }
     *     
     */
    public void setLiquidation(LiquidationType value) {
        this.liquidation = value;
    }

    /**
     * Gets the value of the address property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a {@code set} method for the address property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getAddress().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OrganisationAddressType }
     * </p>
     * 
     * 
     * @return
     *     The value of the address property.
     */
    public List<OrganisationAddressType> getAddress() {
        if (address == null) {
            address = new ArrayList<>();
        }
        return this.address;
    }

    /**
     * Ruft den Wert der contact-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ContactType }
     *     
     */
    public ContactType getContact() {
        return contact;
    }

    /**
     * Legt den Wert der contact-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ContactType }
     *     
     */
    public void setContact(ContactType value) {
        this.contact = value;
    }

    /**
     * Ruft den Wert der languageOfCorrespondance-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLanguageOfCorrespondance() {
        return languageOfCorrespondance;
    }

    /**
     * Legt den Wert der languageOfCorrespondance-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLanguageOfCorrespondance(String value) {
        this.languageOfCorrespondance = value;
    }

}
