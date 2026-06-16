
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
 *         &lt;element name="ValidateVatNumberResult" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
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
    "validateVatNumberResult"
})
@XmlRootElement(name = "ValidateVatNumberResponse")
public class ValidateVatNumberResponse {

    @XmlElement(name = "ValidateVatNumberResult")
    protected boolean validateVatNumberResult;

    /**
     * Ruft den Wert der validateVatNumberResult-Eigenschaft ab.
     * 
     */
    public boolean isValidateVatNumberResult() {
        return validateVatNumberResult;
    }

    /**
     * Legt den Wert der validateVatNumberResult-Eigenschaft fest.
     * 
     */
    public void setValidateVatNumberResult(boolean value) {
        this.validateVatNumberResult = value;
    }

}
