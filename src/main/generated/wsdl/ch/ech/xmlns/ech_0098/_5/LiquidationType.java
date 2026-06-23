
package ch.ech.xmlns.ech_0098._5;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java-Klasse für liquidationType complex type.&lt;/p&gt;
 * 
 * &lt;p&gt;Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.&lt;/p&gt;
 * 
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="liquidationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="liquidationDate" type="{http://www.ech.ch/xmlns/eCH-0098/5}datePartiallyKnownType" minOccurs="0"/&gt;
 *         &lt;element name="liquidationStartDate" type="{http://www.ech.ch/xmlns/eCH-0098/5}datePartiallyKnownType" minOccurs="0"/&gt;
 *         &lt;element name="liquidationReason" type="{http://www.ech.ch/xmlns/eCH-0098/5}liquidationReasonType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "liquidationType", propOrder = {
    "liquidationDate",
    "liquidationStartDate",
    "liquidationReason"
})
public class LiquidationType {

    protected DatePartiallyKnownType liquidationDate;
    protected DatePartiallyKnownType liquidationStartDate;
    protected String liquidationReason;

    /**
     * Ruft den Wert der liquidationDate-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DatePartiallyKnownType }
     *     
     */
    public DatePartiallyKnownType getLiquidationDate() {
        return liquidationDate;
    }

    /**
     * Legt den Wert der liquidationDate-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DatePartiallyKnownType }
     *     
     */
    public void setLiquidationDate(DatePartiallyKnownType value) {
        this.liquidationDate = value;
    }

    /**
     * Ruft den Wert der liquidationStartDate-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DatePartiallyKnownType }
     *     
     */
    public DatePartiallyKnownType getLiquidationStartDate() {
        return liquidationStartDate;
    }

    /**
     * Legt den Wert der liquidationStartDate-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DatePartiallyKnownType }
     *     
     */
    public void setLiquidationStartDate(DatePartiallyKnownType value) {
        this.liquidationStartDate = value;
    }

    /**
     * Ruft den Wert der liquidationReason-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLiquidationReason() {
        return liquidationReason;
    }

    /**
     * Legt den Wert der liquidationReason-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLiquidationReason(String value) {
        this.liquidationReason = value;
    }

}
