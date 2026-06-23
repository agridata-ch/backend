
package ch.admin.uid.xmlns.uid_wse;

import ch.admin.uid.xmlns.uid_wse._5.UidEntitySearchResponse;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java-Klasse für anonymous complex type.&lt;/p&gt;
 * 
 * &lt;p&gt;Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.&lt;/p&gt;
 * 
 * &lt;pre&gt;{&#064;code
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="SearchResult" type="{http://www.uid.admin.ch/xmlns/uid-wse/5}uidEntitySearchResponse" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "searchResult"
})
@XmlRootElement(name = "SearchResponse")
public class SearchResponse {

    @XmlElement(name = "SearchResult")
    protected UidEntitySearchResponse searchResult;

    /**
     * Ruft den Wert der searchResult-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UidEntitySearchResponse }
     *     
     */
    public UidEntitySearchResponse getSearchResult() {
        return searchResult;
    }

    /**
     * Legt den Wert der searchResult-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UidEntitySearchResponse }
     *     
     */
    public void setSearchResult(UidEntitySearchResponse value) {
        this.searchResult = value;
    }

}
