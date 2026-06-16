
package ch.admin.uid.xmlns.uid_wse._5;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * &lt;p&gt;Java-Klasse für uidEntityPublicSearchParameters complex type.&lt;/p&gt;
 * 
 * &lt;p&gt;Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.&lt;/p&gt;
 * 
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="uidEntityPublicSearchParameters"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="organisationName" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;element name="personName" type="{http://www.uid.admin.ch/xmlns/uid-wse/5}personNameType" minOccurs="0"/&gt;
 *         &lt;element name="address" type="{http://www.uid.admin.ch/xmlns/uid-wse/5}addressSearchType" minOccurs="0"/&gt;
 *         &lt;element name="legalForm" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="uidregInformation" type="{http://www.uid.admin.ch/xmlns/uid-wse/5}uidregInformationPublicSearchType" minOccurs="0"/&gt;
 *         &lt;element name="commercialRegisterInformation" type="{http://www.uid.admin.ch/xmlns/uid-wse/5}commercialRegisterInformationSearchType" minOccurs="0"/&gt;
 *         &lt;element name="vatRegisterInformation" type="{http://www.uid.admin.ch/xmlns/uid-wse/5}vatRegisterInformationSearchType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "uidEntityPublicSearchParameters", propOrder = {
    "organisationName",
    "personName",
    "address",
    "legalForm",
    "uidregInformation",
    "commercialRegisterInformation",
    "vatRegisterInformation"
})
public class UidEntityPublicSearchParameters {

    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String organisationName;
    protected PersonNameType personName;
    protected AddressSearchType address;
    protected List<String> legalForm;
    protected UidregInformationPublicSearchType uidregInformation;
    protected CommercialRegisterInformationSearchType commercialRegisterInformation;
    protected VatRegisterInformationSearchType vatRegisterInformation;

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
     * Ruft den Wert der personName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link PersonNameType }
     *     
     */
    public PersonNameType getPersonName() {
        return personName;
    }

    /**
     * Legt den Wert der personName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link PersonNameType }
     *     
     */
    public void setPersonName(PersonNameType value) {
        this.personName = value;
    }

    /**
     * Ruft den Wert der address-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link AddressSearchType }
     *     
     */
    public AddressSearchType getAddress() {
        return address;
    }

    /**
     * Legt den Wert der address-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link AddressSearchType }
     *     
     */
    public void setAddress(AddressSearchType value) {
        this.address = value;
    }

    /**
     * Gets the value of the legalForm property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a {@code set} method for the legalForm property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getLegalForm().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * </p>
     * 
     * 
     * @return
     *     The value of the legalForm property.
     */
    public List<String> getLegalForm() {
        if (legalForm == null) {
            legalForm = new ArrayList<>();
        }
        return this.legalForm;
    }

    /**
     * Ruft den Wert der uidregInformation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UidregInformationPublicSearchType }
     *     
     */
    public UidregInformationPublicSearchType getUidregInformation() {
        return uidregInformation;
    }

    /**
     * Legt den Wert der uidregInformation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UidregInformationPublicSearchType }
     *     
     */
    public void setUidregInformation(UidregInformationPublicSearchType value) {
        this.uidregInformation = value;
    }

    /**
     * Ruft den Wert der commercialRegisterInformation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CommercialRegisterInformationSearchType }
     *     
     */
    public CommercialRegisterInformationSearchType getCommercialRegisterInformation() {
        return commercialRegisterInformation;
    }

    /**
     * Legt den Wert der commercialRegisterInformation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CommercialRegisterInformationSearchType }
     *     
     */
    public void setCommercialRegisterInformation(CommercialRegisterInformationSearchType value) {
        this.commercialRegisterInformation = value;
    }

    /**
     * Ruft den Wert der vatRegisterInformation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link VatRegisterInformationSearchType }
     *     
     */
    public VatRegisterInformationSearchType getVatRegisterInformation() {
        return vatRegisterInformation;
    }

    /**
     * Legt den Wert der vatRegisterInformation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link VatRegisterInformationSearchType }
     *     
     */
    public void setVatRegisterInformation(VatRegisterInformationSearchType value) {
        this.vatRegisterInformation = value;
    }

}
