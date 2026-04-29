
package ch.ech.xmlns.ech_0046._5;

import ch.ech.xmlns.ech_0010._7.MailAddressType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * &lt;p&gt;Java-Klasse für addressType complex type.&lt;/p&gt;
 * 
 * &lt;p&gt;Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.&lt;/p&gt;
 * 
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="addressType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="addressCategory" type="{http://www.ech.ch/xmlns/eCH-0046/5}addressCategoryType"/&gt;
 *           &lt;element name="otherAddressCategory" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="postalAddress" type="{http://www.ech.ch/xmlns/eCH-0010/7}mailAddressType" minOccurs="0"/&gt;
 *         &lt;element name="validity" type="{http://www.ech.ch/xmlns/eCH-0046/5}dateRangeType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "addressType", propOrder = {
    "addressCategory",
    "otherAddressCategory",
    "postalAddress",
    "validity"
})
public class AddressType {

    protected String addressCategory;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String otherAddressCategory;
    protected MailAddressType postalAddress;
    protected DateRangeType validity;

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
     * Ruft den Wert der otherAddressCategory-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOtherAddressCategory() {
        return otherAddressCategory;
    }

    /**
     * Legt den Wert der otherAddressCategory-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOtherAddressCategory(String value) {
        this.otherAddressCategory = value;
    }

    /**
     * Ruft den Wert der postalAddress-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link MailAddressType }
     *     
     */
    public MailAddressType getPostalAddress() {
        return postalAddress;
    }

    /**
     * Legt den Wert der postalAddress-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link MailAddressType }
     *     
     */
    public void setPostalAddress(MailAddressType value) {
        this.postalAddress = value;
    }

    /**
     * Ruft den Wert der validity-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DateRangeType }
     *     
     */
    public DateRangeType getValidity() {
        return validity;
    }

    /**
     * Legt den Wert der validity-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DateRangeType }
     *     
     */
    public void setValidity(DateRangeType value) {
        this.validity = value;
    }

}
