
package ch.admin.uid.xmlns.uid_wse_shared._2;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * &lt;p&gt;Java-Klasse für searchMode.&lt;/p&gt;
 * 
 * &lt;p&gt;Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.&lt;/p&gt;
 * &lt;pre&gt;{&#064;code
 * &lt;simpleType name="searchMode"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="Auto"/&gt;
 *     &lt;enumeration value="Normal"/&gt;
 *     &lt;enumeration value="Fuzzy"/&gt;
 *     &lt;enumeration value="FuzzyPerson"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * }&lt;/pre&gt;
 * 
 */
@XmlType(name = "searchMode")
@XmlEnum
public enum SearchMode {

    @XmlEnumValue("Auto")
    AUTO("Auto"),
    @XmlEnumValue("Normal")
    NORMAL("Normal"),
    @XmlEnumValue("Fuzzy")
    FUZZY("Fuzzy"),
    @XmlEnumValue("FuzzyPerson")
    FUZZY_PERSON("FuzzyPerson");
    private final String value;

    SearchMode(String v) {
        value = v;
    }

    /**
     * Gets the value associated to the enum constant.
     * 
     * @return
     *     The value linked to the enum.
     */
    public String value() {
        return value;
    }

    /**
     * Gets the enum associated to the value passed as parameter.
     * 
     * @param v
     *     The value to get the enum from.
     * @return
     *     The enum which corresponds to the value, if it exists.
     * @throws IllegalArgumentException
     *     If no value matches in the enum declaration.
     */
    public static SearchMode fromValue(String v) {
        for (SearchMode c: SearchMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
