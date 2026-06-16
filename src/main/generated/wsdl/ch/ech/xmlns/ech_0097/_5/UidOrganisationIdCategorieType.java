
package ch.ech.xmlns.ech_0097._5;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * &lt;p&gt;Java-Klasse für uidOrganisationIdCategorieType.&lt;/p&gt;
 * 
 * &lt;p&gt;Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.&lt;/p&gt;
 * &lt;pre&gt;{&#064;code
 * &lt;simpleType name="uidOrganisationIdCategorieType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="CHE"/&gt;
 *     &lt;enumeration value="ADM"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * }&lt;/pre&gt;
 * 
 */
@XmlType(name = "uidOrganisationIdCategorieType")
@XmlEnum
public enum UidOrganisationIdCategorieType {

    CHE,
    ADM;

    public String value() {
        return name();
    }

    public static UidOrganisationIdCategorieType fromValue(String v) {
        return valueOf(v);
    }

}
