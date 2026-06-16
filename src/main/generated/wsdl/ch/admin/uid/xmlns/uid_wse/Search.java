
package ch.admin.uid.xmlns.uid_wse;

import ch.admin.uid.xmlns.uid_wse._5.UidEntityPublicSearchRequest;
import ch.admin.uid.xmlns.uid_wse_shared._2.SearchConfiguration;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element name="searchParameters" type="{http://www.uid.admin.ch/xmlns/uid-wse/5}uidEntityPublicSearchRequest" minOccurs="0"/&gt;
 *         &lt;element name="config" type="{http://www.uid.admin.ch/xmlns/uid-wse-shared/2}searchConfiguration" minOccurs="0"/&gt;
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
    "searchParameters",
    "config"
})
@XmlRootElement(name = "Search")
public class Search {

    protected UidEntityPublicSearchRequest searchParameters;
    protected SearchConfiguration config;

    /**
     * Ruft den Wert der searchParameters-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UidEntityPublicSearchRequest }
     *     
     */
    public UidEntityPublicSearchRequest getSearchParameters() {
        return searchParameters;
    }

    /**
     * Legt den Wert der searchParameters-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UidEntityPublicSearchRequest }
     *     
     */
    public void setSearchParameters(UidEntityPublicSearchRequest value) {
        this.searchParameters = value;
    }

    /**
     * Ruft den Wert der config-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SearchConfiguration }
     *     
     */
    public SearchConfiguration getConfig() {
        return config;
    }

    /**
     * Legt den Wert der config-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SearchConfiguration }
     *     
     */
    public void setConfig(SearchConfiguration value) {
        this.config = value;
    }

}
