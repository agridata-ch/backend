
package ch.ech.xmlns.ech_0108._5;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


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
 *         &lt;element name="organisation" type="{http://www.ech.ch/xmlns/eCH-0098/5}organisationType" minOccurs="0"/&gt;
 *         &lt;element name="uidregInformation" type="{http://www.ech.ch/xmlns/eCH-0108/5}uidregInformationType" minOccurs="0"/&gt;
 *         &lt;element name="commercialRegisterInformation" type="{http://www.ech.ch/xmlns/eCH-0108/5}commercialRegisterInformationType" minOccurs="0"/&gt;
 *         &lt;element name="vatRegisterInformation" type="{http://www.ech.ch/xmlns/eCH-0108/5}vatRegisterInformationType" minOccurs="0"/&gt;
 *         &lt;element name="leiRegisterInformation" type="{http://www.ech.ch/xmlns/eCH-0108/5}leiRegisterInformationType" minOccurs="0"/&gt;
 *         &lt;element name="groupRelationship" type="{http://www.ech.ch/xmlns/eCH-0108/5}groupRelationshipType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="involvedPerson" type="{http://www.ech.ch/xmlns/eCH-0108/5}involvedPersonType" maxOccurs="unbounded" minOccurs="0"/&gt;
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
    "organisation",
    "uidregInformation",
    "commercialRegisterInformation",
    "vatRegisterInformation",
    "leiRegisterInformation",
    "groupRelationship",
    "involvedPerson"
})
public class OrganisationType {

    protected ch.ech.xmlns.ech_0098._5.OrganisationType organisation;
    protected UidregInformationType uidregInformation;
    protected CommercialRegisterInformationType commercialRegisterInformation;
    protected VatRegisterInformationType vatRegisterInformation;
    protected LeiRegisterInformationType leiRegisterInformation;
    protected List<GroupRelationshipType> groupRelationship;
    protected List<InvolvedPersonType> involvedPerson;

    /**
     * Ruft den Wert der organisation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ch.ech.xmlns.ech_0098._5.OrganisationType }
     *     
     */
    public ch.ech.xmlns.ech_0098._5.OrganisationType getOrganisation() {
        return organisation;
    }

    /**
     * Legt den Wert der organisation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ch.ech.xmlns.ech_0098._5.OrganisationType }
     *     
     */
    public void setOrganisation(ch.ech.xmlns.ech_0098._5.OrganisationType value) {
        this.organisation = value;
    }

    /**
     * Ruft den Wert der uidregInformation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UidregInformationType }
     *     
     */
    public UidregInformationType getUidregInformation() {
        return uidregInformation;
    }

    /**
     * Legt den Wert der uidregInformation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UidregInformationType }
     *     
     */
    public void setUidregInformation(UidregInformationType value) {
        this.uidregInformation = value;
    }

    /**
     * Ruft den Wert der commercialRegisterInformation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CommercialRegisterInformationType }
     *     
     */
    public CommercialRegisterInformationType getCommercialRegisterInformation() {
        return commercialRegisterInformation;
    }

    /**
     * Legt den Wert der commercialRegisterInformation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CommercialRegisterInformationType }
     *     
     */
    public void setCommercialRegisterInformation(CommercialRegisterInformationType value) {
        this.commercialRegisterInformation = value;
    }

    /**
     * Ruft den Wert der vatRegisterInformation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link VatRegisterInformationType }
     *     
     */
    public VatRegisterInformationType getVatRegisterInformation() {
        return vatRegisterInformation;
    }

    /**
     * Legt den Wert der vatRegisterInformation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link VatRegisterInformationType }
     *     
     */
    public void setVatRegisterInformation(VatRegisterInformationType value) {
        this.vatRegisterInformation = value;
    }

    /**
     * Ruft den Wert der leiRegisterInformation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link LeiRegisterInformationType }
     *     
     */
    public LeiRegisterInformationType getLeiRegisterInformation() {
        return leiRegisterInformation;
    }

    /**
     * Legt den Wert der leiRegisterInformation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link LeiRegisterInformationType }
     *     
     */
    public void setLeiRegisterInformation(LeiRegisterInformationType value) {
        this.leiRegisterInformation = value;
    }

    /**
     * Gets the value of the groupRelationship property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a {@code set} method for the groupRelationship property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getGroupRelationship().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GroupRelationshipType }
     * </p>
     * 
     * 
     * @return
     *     The value of the groupRelationship property.
     */
    public List<GroupRelationshipType> getGroupRelationship() {
        if (groupRelationship == null) {
            groupRelationship = new ArrayList<>();
        }
        return this.groupRelationship;
    }

    /**
     * Gets the value of the involvedPerson property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a {@code set} method for the involvedPerson property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getInvolvedPerson().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InvolvedPersonType }
     * </p>
     * 
     * 
     * @return
     *     The value of the involvedPerson property.
     */
    public List<InvolvedPersonType> getInvolvedPerson() {
        if (involvedPerson == null) {
            involvedPerson = new ArrayList<>();
        }
        return this.involvedPerson;
    }

}
