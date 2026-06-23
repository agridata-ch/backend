
package ch.ech.xmlns.ech_0108._5;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * &lt;p&gt;Java-Klasse für organisationMembershipRoleType.&lt;/p&gt;
 * 
 * &lt;p&gt;Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.&lt;/p&gt;
 * &lt;pre&gt;{&#064;code
 * &lt;simpleType name="organisationMembershipRoleType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="GroupHead"/&gt;
 *     &lt;enumeration value="GroupMember"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * }&lt;/pre&gt;
 * 
 */
@XmlType(name = "organisationMembershipRoleType")
@XmlEnum
public enum OrganisationMembershipRoleType {

    @XmlEnumValue("GroupHead")
    GROUP_HEAD("GroupHead"),
    @XmlEnumValue("GroupMember")
    GROUP_MEMBER("GroupMember");
    private final String value;

    OrganisationMembershipRoleType(String v) {
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
    public static OrganisationMembershipRoleType fromValue(String v) {
        for (OrganisationMembershipRoleType c: OrganisationMembershipRoleType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
