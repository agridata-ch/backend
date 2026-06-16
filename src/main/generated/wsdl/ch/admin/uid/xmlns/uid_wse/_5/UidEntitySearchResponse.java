
package ch.admin.uid.xmlns.uid_wse._5;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java-Klasse für uidEntitySearchResponse complex type.&lt;/p&gt;
 * 
 * &lt;p&gt;Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.&lt;/p&gt;
 * 
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="uidEntitySearchResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="uidEntitySearchResultItem" type="{http://www.uid.admin.ch/xmlns/uid-wse/5}uidEntitySearchResultItem" maxOccurs="unbounded" minOccurs="0"/&gt;
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
@XmlType(name = "uidEntitySearchResponse", propOrder = {
    "uidEntitySearchResultItem",
    "extension"
})
public class UidEntitySearchResponse {

    protected List<UidEntitySearchResultItem> uidEntitySearchResultItem;
    protected Object extension;

    /**
     * Gets the value of the uidEntitySearchResultItem property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a {@code set} method for the uidEntitySearchResultItem property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getUidEntitySearchResultItem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link UidEntitySearchResultItem }
     * </p>
     * 
     * 
     * @return
     *     The value of the uidEntitySearchResultItem property.
     */
    public List<UidEntitySearchResultItem> getUidEntitySearchResultItem() {
        if (uidEntitySearchResultItem == null) {
            uidEntitySearchResultItem = new ArrayList<>();
        }
        return this.uidEntitySearchResultItem;
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
