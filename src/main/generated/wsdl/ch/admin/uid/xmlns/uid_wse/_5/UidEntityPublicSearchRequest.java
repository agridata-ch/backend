
package ch.admin.uid.xmlns.uid_wse._5;

import ch.ech.xmlns.ech_0097._5.NamedOrganisationIdType;
import ch.ech.xmlns.ech_0097._5.UidStructureType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java-Klasse für uidEntityPublicSearchRequest complex type.&lt;/p&gt;
 * 
 * &lt;p&gt;Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.&lt;/p&gt;
 * 
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="uidEntityPublicSearchRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="uid" type="{http://www.ech.ch/xmlns/eCH-0097/5}uidStructureType" minOccurs="0"/&gt;
 *           &lt;element name="otherOrganisationId" type="{http://www.ech.ch/xmlns/eCH-0097/5}namedOrganisationIdType" minOccurs="0"/&gt;
 *           &lt;element name="uidEntitySearchParameters" type="{http://www.uid.admin.ch/xmlns/uid-wse/5}uidEntityPublicSearchParameters" minOccurs="0"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "uidEntityPublicSearchRequest", propOrder = {
    "uid",
    "otherOrganisationId",
    "uidEntitySearchParameters"
})
public class UidEntityPublicSearchRequest {

    protected UidStructureType uid;
    protected NamedOrganisationIdType otherOrganisationId;
    protected UidEntityPublicSearchParameters uidEntitySearchParameters;

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
     * Ruft den Wert der otherOrganisationId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link NamedOrganisationIdType }
     *     
     */
    public NamedOrganisationIdType getOtherOrganisationId() {
        return otherOrganisationId;
    }

    /**
     * Legt den Wert der otherOrganisationId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link NamedOrganisationIdType }
     *     
     */
    public void setOtherOrganisationId(NamedOrganisationIdType value) {
        this.otherOrganisationId = value;
    }

    /**
     * Ruft den Wert der uidEntitySearchParameters-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UidEntityPublicSearchParameters }
     *     
     */
    public UidEntityPublicSearchParameters getUidEntitySearchParameters() {
        return uidEntitySearchParameters;
    }

    /**
     * Legt den Wert der uidEntitySearchParameters-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UidEntityPublicSearchParameters }
     *     
     */
    public void setUidEntitySearchParameters(UidEntityPublicSearchParameters value) {
        this.uidEntitySearchParameters = value;
    }

}
