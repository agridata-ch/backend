
package ch.admin.uid.xmlns.uid_wse._5;

import ch.ech.xmlns.ech_0108._5.OrganisationType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java-Klasse für uidEntitySearchResultItem complex type.&lt;/p&gt;
 * 
 * &lt;p&gt;Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.&lt;/p&gt;
 * 
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="uidEntitySearchResultItem"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="organisation" type="{http://www.ech.ch/xmlns/eCH-0108/5}organisationType" minOccurs="0"/&gt;
 *         &lt;element name="rating" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="isHistoryMatch" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="extension" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "uidEntitySearchResultItem", propOrder = {
    "organisation",
    "rating",
    "isHistoryMatch",
    "extension"
})
public class UidEntitySearchResultItem {

    protected OrganisationType organisation;
    protected int rating;
    protected boolean isHistoryMatch;
    protected Object extension;

    /**
     * Ruft den Wert der organisation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link OrganisationType }
     *     
     */
    public OrganisationType getOrganisation() {
        return organisation;
    }

    /**
     * Legt den Wert der organisation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganisationType }
     *     
     */
    public void setOrganisation(OrganisationType value) {
        this.organisation = value;
    }

    /**
     * Ruft den Wert der rating-Eigenschaft ab.
     * 
     */
    public int getRating() {
        return rating;
    }

    /**
     * Legt den Wert der rating-Eigenschaft fest.
     * 
     */
    public void setRating(int value) {
        this.rating = value;
    }

    /**
     * Ruft den Wert der isHistoryMatch-Eigenschaft ab.
     * 
     */
    public boolean isIsHistoryMatch() {
        return isHistoryMatch;
    }

    /**
     * Legt den Wert der isHistoryMatch-Eigenschaft fest.
     * 
     */
    public void setIsHistoryMatch(boolean value) {
        this.isHistoryMatch = value;
    }

    /**
     * Ruft den Wert der extension-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getExtension() {
        return extension;
    }

    /**
     * Legt den Wert der extension-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setExtension(Object value) {
        this.extension = value;
    }

}
