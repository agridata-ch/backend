
package ch.ech.xmlns.ech_0098._5;

import java.util.ArrayList;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;
import ch.ech.xmlns.ech_0007._6.CantonAbbreviationType;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlElementRefs;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * &lt;p&gt;Java-Klasse für organisationAddressType complex type.&lt;/p&gt;
 * 
 * &lt;p&gt;Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.&lt;/p&gt;
 * 
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="organisationAddressType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="addressCategory" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;element name="addressLine1" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;element name="addressLine2" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;element name="street" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;element name="houseNumber" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;element name="dwellingNumber" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;element name="postOfficeBoxNumber" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" minOccurs="0"/&gt;
 *         &lt;element name="town" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="swissZipCode" type="{http://www.w3.org/2001/XMLSchema}unsignedInt"/&gt;
 *           &lt;element name="swissZipCodeAddOn" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *           &lt;element name="municipalityId" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *           &lt;element name="cantonAbbreviation" type="{http://www.ech.ch/xmlns/eCH-0007/6}cantonAbbreviationType"/&gt;
 *           &lt;element name="EGID" type="{http://www.w3.org/2001/XMLSchema}unsignedInt"/&gt;
 *           &lt;element name="foreignZipCode" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="countryIdISO2" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;element name="deliverableYesNo" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="dateOfLastCheck" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "organisationAddressType", propOrder = {
    "addressCategory",
    "addressLine1",
    "addressLine2",
    "street",
    "houseNumber",
    "dwellingNumber",
    "postOfficeBoxNumber",
    "town",
    "swissZipCodeOrSwissZipCodeAddOnOrMunicipalityId",
    "countryIdISO2",
    "deliverableYesNo",
    "dateOfLastCheck"
})
public class OrganisationAddressType {

    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String addressCategory;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String addressLine1;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String addressLine2;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String street;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String houseNumber;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String dwellingNumber;
    @XmlSchemaType(name = "unsignedInt")
    protected Long postOfficeBoxNumber;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String town;
    @XmlElementRefs({
        @XmlElementRef(name = "swissZipCode", namespace = "http://www.ech.ch/xmlns/eCH-0098/5", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "swissZipCodeAddOn", namespace = "http://www.ech.ch/xmlns/eCH-0098/5", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "municipalityId", namespace = "http://www.ech.ch/xmlns/eCH-0098/5", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "cantonAbbreviation", namespace = "http://www.ech.ch/xmlns/eCH-0098/5", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "EGID", namespace = "http://www.ech.ch/xmlns/eCH-0098/5", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "foreignZipCode", namespace = "http://www.ech.ch/xmlns/eCH-0098/5", type = JAXBElement.class, required = false)
    })
    protected List<JAXBElement<?>> swissZipCodeOrSwissZipCodeAddOnOrMunicipalityId;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String countryIdISO2;
    protected Boolean deliverableYesNo;
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar dateOfLastCheck;

    /**
     * Ruft den Wert der addressCategory-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddressCategory() {
        return addressCategory;
    }

    /**
     * Legt den Wert der addressCategory-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddressCategory(String value) {
        this.addressCategory = value;
    }

    /**
     * Ruft den Wert der addressLine1-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddressLine1() {
        return addressLine1;
    }

    /**
     * Legt den Wert der addressLine1-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddressLine1(String value) {
        this.addressLine1 = value;
    }

    /**
     * Ruft den Wert der addressLine2-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddressLine2() {
        return addressLine2;
    }

    /**
     * Legt den Wert der addressLine2-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddressLine2(String value) {
        this.addressLine2 = value;
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
     * Ruft den Wert der houseNumber-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHouseNumber() {
        return houseNumber;
    }

    /**
     * Legt den Wert der houseNumber-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHouseNumber(String value) {
        this.houseNumber = value;
    }

    /**
     * Ruft den Wert der dwellingNumber-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDwellingNumber() {
        return dwellingNumber;
    }

    /**
     * Legt den Wert der dwellingNumber-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDwellingNumber(String value) {
        this.dwellingNumber = value;
    }

    /**
     * Ruft den Wert der postOfficeBoxNumber-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getPostOfficeBoxNumber() {
        return postOfficeBoxNumber;
    }

    /**
     * Legt den Wert der postOfficeBoxNumber-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setPostOfficeBoxNumber(Long value) {
        this.postOfficeBoxNumber = value;
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
     * Gets the value of the swissZipCodeOrSwissZipCodeAddOnOrMunicipalityId property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a {@code set} method for the swissZipCodeOrSwissZipCodeAddOnOrMunicipalityId property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getSwissZipCodeOrSwissZipCodeAddOnOrMunicipalityId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link CantonAbbreviationType }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link Long }{@code >}
     * {@link JAXBElement }{@code <}{@link Long }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * </p>
     * 
     * 
     * @return
     *     The value of the swissZipCodeOrSwissZipCodeAddOnOrMunicipalityId property.
     */
    public List<JAXBElement<?>> getSwissZipCodeOrSwissZipCodeAddOnOrMunicipalityId() {
        if (swissZipCodeOrSwissZipCodeAddOnOrMunicipalityId == null) {
            swissZipCodeOrSwissZipCodeAddOnOrMunicipalityId = new ArrayList<>();
        }
        return this.swissZipCodeOrSwissZipCodeAddOnOrMunicipalityId;
    }

    /**
     * Ruft den Wert der countryIdISO2-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountryIdISO2() {
        return countryIdISO2;
    }

    /**
     * Legt den Wert der countryIdISO2-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCountryIdISO2(String value) {
        this.countryIdISO2 = value;
    }

    /**
     * Ruft den Wert der deliverableYesNo-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDeliverableYesNo() {
        return deliverableYesNo;
    }

    /**
     * Legt den Wert der deliverableYesNo-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDeliverableYesNo(Boolean value) {
        this.deliverableYesNo = value;
    }

    /**
     * Ruft den Wert der dateOfLastCheck-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateOfLastCheck() {
        return dateOfLastCheck;
    }

    /**
     * Legt den Wert der dateOfLastCheck-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateOfLastCheck(XMLGregorianCalendar value) {
        this.dateOfLastCheck = value;
    }

}
