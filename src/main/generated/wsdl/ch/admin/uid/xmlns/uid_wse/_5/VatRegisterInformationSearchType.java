
package ch.admin.uid.xmlns.uid_wse._5;

import javax.xml.datatype.XMLGregorianCalendar;
import ch.ech.xmlns.ech_0097._5.UidStructureType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java-Klasse für vatRegisterInformationSearchType complex type.&lt;/p&gt;
 * 
 * &lt;p&gt;Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.&lt;/p&gt;
 * 
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="vatRegisterInformationSearchType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="vatStatus" type="{http://www.ech.ch/xmlns/eCH-0108/5}vatStatusType" minOccurs="0"/&gt;
 *         &lt;element name="vatEntryStatus" type="{http://www.ech.ch/xmlns/eCH-0108/5}vatEntryStatusType" minOccurs="0"/&gt;
 *         &lt;element name="vatEntryDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/&gt;
 *         &lt;element name="vatLiquidationDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/&gt;
 *         &lt;element name="uidVat" type="{http://www.ech.ch/xmlns/eCH-0097/5}uidStructureType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "vatRegisterInformationSearchType", propOrder = {
    "vatStatus",
    "vatEntryStatus",
    "vatEntryDate",
    "vatLiquidationDate",
    "uidVat"
})
public class VatRegisterInformationSearchType {

    protected String vatStatus;
    protected String vatEntryStatus;
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar vatEntryDate;
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar vatLiquidationDate;
    protected UidStructureType uidVat;

    /**
     * Ruft den Wert der vatStatus-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVatStatus() {
        return vatStatus;
    }

    /**
     * Legt den Wert der vatStatus-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVatStatus(String value) {
        this.vatStatus = value;
    }

    /**
     * Ruft den Wert der vatEntryStatus-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVatEntryStatus() {
        return vatEntryStatus;
    }

    /**
     * Legt den Wert der vatEntryStatus-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVatEntryStatus(String value) {
        this.vatEntryStatus = value;
    }

    /**
     * Ruft den Wert der vatEntryDate-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getVatEntryDate() {
        return vatEntryDate;
    }

    /**
     * Legt den Wert der vatEntryDate-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setVatEntryDate(XMLGregorianCalendar value) {
        this.vatEntryDate = value;
    }

    /**
     * Ruft den Wert der vatLiquidationDate-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getVatLiquidationDate() {
        return vatLiquidationDate;
    }

    /**
     * Legt den Wert der vatLiquidationDate-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setVatLiquidationDate(XMLGregorianCalendar value) {
        this.vatLiquidationDate = value;
    }

    /**
     * Ruft den Wert der uidVat-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UidStructureType }
     *     
     */
    public UidStructureType getUidVat() {
        return uidVat;
    }

    /**
     * Legt den Wert der uidVat-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UidStructureType }
     *     
     */
    public void setUidVat(UidStructureType value) {
        this.uidVat = value;
    }

}
