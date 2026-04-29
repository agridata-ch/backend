
package ch.admin.uid.xmlns.uid_wse;

import ch.ech.xmlns.ech_0108._5.OrganisationType;
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
 *         &lt;element name="GetOrganisationSampleResult" type="{http://www.ech.ch/xmlns/eCH-0108/5}organisationType" minOccurs="0"/&gt;
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
    "getOrganisationSampleResult"
})
@XmlRootElement(name = "GetOrganisationSampleResponse")
public class GetOrganisationSampleResponse {

    @XmlElement(name = "GetOrganisationSampleResult")
    protected OrganisationType getOrganisationSampleResult;

    /**
     * Ruft den Wert der getOrganisationSampleResult-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link OrganisationType }
     *     
     */
    public OrganisationType getGetOrganisationSampleResult() {
        return getOrganisationSampleResult;
    }

    /**
     * Legt den Wert der getOrganisationSampleResult-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganisationType }
     *     
     */
    public void setGetOrganisationSampleResult(OrganisationType value) {
        this.getOrganisationSampleResult = value;
    }

}
