
package ch.admin.uid.xmlns.uid_wse._5;

import java.util.ArrayList;
import java.util.List;
import ch.admin.uid.xmlns.uid_wse_shared._2.ServiceFault;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * &lt;p&gt;Java-Klasse für duplicateFault complex type.&lt;/p&gt;
 * 
 * &lt;p&gt;Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.&lt;/p&gt;
 * 
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="duplicateFault"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.uid.admin.ch/xmlns/uid-wse-shared/2}serviceFault"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="candidates" type="{http://www.uid.admin.ch/xmlns/uid-wse/5}duplicateCandidateType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="duplicateOverrideCode" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "duplicateFault", propOrder = {
    "candidates",
    "duplicateOverrideCode"
})
public class DuplicateFault
    extends ServiceFault
{

    protected List<DuplicateCandidateType> candidates;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String duplicateOverrideCode;

    /**
     * Gets the value of the candidates property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a {@code set} method for the candidates property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getCandidates().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DuplicateCandidateType }
     * </p>
     * 
     * 
     * @return
     *     The value of the candidates property.
     */
    public List<DuplicateCandidateType> getCandidates() {
        if (candidates == null) {
            candidates = new ArrayList<>();
        }
        return this.candidates;
    }

    /**
     * Ruft den Wert der duplicateOverrideCode-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDuplicateOverrideCode() {
        return duplicateOverrideCode;
    }

    /**
     * Legt den Wert der duplicateOverrideCode-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDuplicateOverrideCode(String value) {
        this.duplicateOverrideCode = value;
    }

}
