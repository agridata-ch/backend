
package ch.ech.xmlns.ech_0046._5;

import java.util.ArrayList;
import java.util.List;
import ch.ech.xmlns.ech_0044._4.NamedPersonIdType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java-Klasse für contactType complex type.&lt;/p&gt;
 * 
 * &lt;p&gt;Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.&lt;/p&gt;
 * 
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="contactType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="localID" type="{http://www.ech.ch/xmlns/eCH-0044/4}namedPersonIdType" minOccurs="0"/&gt;
 *         &lt;element name="address" type="{http://www.ech.ch/xmlns/eCH-0046/5}addressType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="email" type="{http://www.ech.ch/xmlns/eCH-0046/5}emailType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="phone" type="{http://www.ech.ch/xmlns/eCH-0046/5}phoneType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="internet" type="{http://www.ech.ch/xmlns/eCH-0046/5}internetType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "contactType", propOrder = {
    "localID",
    "address",
    "email",
    "phone",
    "internet"
})
public class ContactType {

    protected NamedPersonIdType localID;
    protected List<AddressType> address;
    protected List<EmailType> email;
    protected List<PhoneType> phone;
    protected List<InternetType> internet;

    /**
     * Ruft den Wert der localID-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link NamedPersonIdType }
     *     
     */
    public NamedPersonIdType getLocalID() {
        return localID;
    }

    /**
     * Legt den Wert der localID-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link NamedPersonIdType }
     *     
     */
    public void setLocalID(NamedPersonIdType value) {
        this.localID = value;
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
     * {@link AddressType }
     * </p>
     * 
     * 
     * @return
     *     The value of the address property.
     */
    public List<AddressType> getAddress() {
        if (address == null) {
            address = new ArrayList<>();
        }
        return this.address;
    }

    /**
     * Gets the value of the email property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a {@code set} method for the email property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getEmail().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EmailType }
     * </p>
     * 
     * 
     * @return
     *     The value of the email property.
     */
    public List<EmailType> getEmail() {
        if (email == null) {
            email = new ArrayList<>();
        }
        return this.email;
    }

    /**
     * Gets the value of the phone property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a {@code set} method for the phone property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getPhone().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PhoneType }
     * </p>
     * 
     * 
     * @return
     *     The value of the phone property.
     */
    public List<PhoneType> getPhone() {
        if (phone == null) {
            phone = new ArrayList<>();
        }
        return this.phone;
    }

    /**
     * Gets the value of the internet property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a {@code set} method for the internet property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getInternet().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InternetType }
     * </p>
     * 
     * 
     * @return
     *     The value of the internet property.
     */
    public List<InternetType> getInternet() {
        if (internet == null) {
            internet = new ArrayList<>();
        }
        return this.internet;
    }

}
