
package ch.admin.uid.xmlns.uid_wse._5;

import javax.xml.datatype.XMLGregorianCalendar;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java-Klasse für commercialRegisterInformationSearchType complex type.&lt;/p&gt;
 * 
 * &lt;p&gt;Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.&lt;/p&gt;
 * 
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="commercialRegisterInformationSearchType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="commercialRegisterStatus" type="{http://www.ech.ch/xmlns/eCH-0108/5}commercialRegisterStatusType" minOccurs="0"/&gt;
 *         &lt;element name="commercialRegisterEntryStatus" type="{http://www.ech.ch/xmlns/eCH-0108/5}commercialRegisterEntryStatusType" minOccurs="0"/&gt;
 *         &lt;element name="commercialRegisterEntryDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/&gt;
 *         &lt;element name="commercialRegisterLiquidationDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/&gt;
 *         &lt;element name="commercialRegisterEnterpriseType" type="{http://www.ech.ch/xmlns/eCH-0108/5}commercialRegisterEnterpriseTypeType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "commercialRegisterInformationSearchType", propOrder = {
    "commercialRegisterStatus",
    "commercialRegisterEntryStatus",
    "commercialRegisterEntryDate",
    "commercialRegisterLiquidationDate",
    "commercialRegisterEnterpriseType"
})
public class CommercialRegisterInformationSearchType {

    protected String commercialRegisterStatus;
    protected String commercialRegisterEntryStatus;
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar commercialRegisterEntryDate;
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar commercialRegisterLiquidationDate;
    protected String commercialRegisterEnterpriseType;

    /**
     * Ruft den Wert der commercialRegisterStatus-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCommercialRegisterStatus() {
        return commercialRegisterStatus;
    }

    /**
     * Legt den Wert der commercialRegisterStatus-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCommercialRegisterStatus(String value) {
        this.commercialRegisterStatus = value;
    }

    /**
     * Ruft den Wert der commercialRegisterEntryStatus-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCommercialRegisterEntryStatus() {
        return commercialRegisterEntryStatus;
    }

    /**
     * Legt den Wert der commercialRegisterEntryStatus-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCommercialRegisterEntryStatus(String value) {
        this.commercialRegisterEntryStatus = value;
    }

    /**
     * Ruft den Wert der commercialRegisterEntryDate-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCommercialRegisterEntryDate() {
        return commercialRegisterEntryDate;
    }

    /**
     * Legt den Wert der commercialRegisterEntryDate-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCommercialRegisterEntryDate(XMLGregorianCalendar value) {
        this.commercialRegisterEntryDate = value;
    }

    /**
     * Ruft den Wert der commercialRegisterLiquidationDate-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCommercialRegisterLiquidationDate() {
        return commercialRegisterLiquidationDate;
    }

    /**
     * Legt den Wert der commercialRegisterLiquidationDate-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCommercialRegisterLiquidationDate(XMLGregorianCalendar value) {
        this.commercialRegisterLiquidationDate = value;
    }

    /**
     * Ruft den Wert der commercialRegisterEnterpriseType-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCommercialRegisterEnterpriseType() {
        return commercialRegisterEnterpriseType;
    }

    /**
     * Legt den Wert der commercialRegisterEnterpriseType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCommercialRegisterEnterpriseType(String value) {
        this.commercialRegisterEnterpriseType = value;
    }

}
