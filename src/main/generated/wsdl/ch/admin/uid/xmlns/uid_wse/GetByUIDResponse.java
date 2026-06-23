
package ch.admin.uid.xmlns.uid_wse;

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
 *         &lt;element name="GetByUIDResult" type="{http://www.uid.admin.ch/xmlns/uid-wse}ArrayOfOrganisationType" minOccurs="0"/&gt;
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
    "getByUIDResult"
})
@XmlRootElement(name = "GetByUIDResponse")
public class GetByUIDResponse {

    @XmlElement(name = "GetByUIDResult")
    protected ArrayOfOrganisationType getByUIDResult;

    /**
     * Ruft den Wert der getByUIDResult-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfOrganisationType }
     *     
     */
    public ArrayOfOrganisationType getGetByUIDResult() {
        return getByUIDResult;
    }

    /**
     * Legt den Wert der getByUIDResult-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfOrganisationType }
     *     
     */
    public void setGetByUIDResult(ArrayOfOrganisationType value) {
        this.getByUIDResult = value;
    }

}
