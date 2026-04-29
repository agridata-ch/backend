
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
 *         &lt;element name="ValidateUIDResult" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
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
    "validateUIDResult"
})
@XmlRootElement(name = "ValidateUIDResponse")
public class ValidateUIDResponse {

    @XmlElement(name = "ValidateUIDResult")
    protected boolean validateUIDResult;

    /**
     * Ruft den Wert der validateUIDResult-Eigenschaft ab.
     * 
     */
    public boolean isValidateUIDResult() {
        return validateUIDResult;
    }

    /**
     * Legt den Wert der validateUIDResult-Eigenschaft fest.
     * 
     */
    public void setValidateUIDResult(boolean value) {
        this.validateUIDResult = value;
    }

}
