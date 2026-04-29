
package ch.admin.uid.xmlns.uid_wse_shared._2;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java-Klasse für searchConfiguration complex type.&lt;/p&gt;
 * 
 * &lt;p&gt;Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.&lt;/p&gt;
 * 
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="searchConfiguration"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="searchMode" type="{http://www.uid.admin.ch/xmlns/uid-wse-shared/2}searchMode"/&gt;
 *         &lt;element name="maxNumberOfRecords" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="searchNameAndAddressHistory" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "searchConfiguration", propOrder = {
    "searchMode",
    "maxNumberOfRecords",
    "searchNameAndAddressHistory"
})
public class SearchConfiguration {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected SearchMode searchMode;
    protected int maxNumberOfRecords;
    protected boolean searchNameAndAddressHistory;

    /**
     * Ruft den Wert der searchMode-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SearchMode }
     *     
     */
    public SearchMode getSearchMode() {
        return searchMode;
    }

    /**
     * Legt den Wert der searchMode-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SearchMode }
     *     
     */
    public void setSearchMode(SearchMode value) {
        this.searchMode = value;
    }

    /**
     * Ruft den Wert der maxNumberOfRecords-Eigenschaft ab.
     * 
     */
    public int getMaxNumberOfRecords() {
        return maxNumberOfRecords;
    }

    /**
     * Legt den Wert der maxNumberOfRecords-Eigenschaft fest.
     * 
     */
    public void setMaxNumberOfRecords(int value) {
        this.maxNumberOfRecords = value;
    }

    /**
     * Ruft den Wert der searchNameAndAddressHistory-Eigenschaft ab.
     * 
     */
    public boolean isSearchNameAndAddressHistory() {
        return searchNameAndAddressHistory;
    }

    /**
     * Legt den Wert der searchNameAndAddressHistory-Eigenschaft fest.
     * 
     */
    public void setSearchNameAndAddressHistory(boolean value) {
        this.searchNameAndAddressHistory = value;
    }

}
