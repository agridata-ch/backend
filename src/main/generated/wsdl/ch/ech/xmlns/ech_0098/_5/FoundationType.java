
package ch.ech.xmlns.ech_0098._5;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java-Klasse für foundationType complex type.&lt;/p&gt;
 * 
 * &lt;p&gt;Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.&lt;/p&gt;
 * 
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="foundationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="foundationDate" type="{http://www.ech.ch/xmlns/eCH-0098/5}datePartiallyKnownType" minOccurs="0"/&gt;
 *         &lt;element name="foundationReason" type="{http://www.ech.ch/xmlns/eCH-0098/5}foundationReasonType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "foundationType", propOrder = {
    "foundationDate",
    "foundationReason"
})
public class FoundationType {

    protected DatePartiallyKnownType foundationDate;
    protected String foundationReason;

    /**
     * Ruft den Wert der foundationDate-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DatePartiallyKnownType }
     *     
     */
    public DatePartiallyKnownType getFoundationDate() {
        return foundationDate;
    }

    /**
     * Legt den Wert der foundationDate-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DatePartiallyKnownType }
     *     
     */
    public void setFoundationDate(DatePartiallyKnownType value) {
        this.foundationDate = value;
    }

    /**
     * Ruft den Wert der foundationReason-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFoundationReason() {
        return foundationReason;
    }

    /**
     * Legt den Wert der foundationReason-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFoundationReason(String value) {
        this.foundationReason = value;
    }

}
