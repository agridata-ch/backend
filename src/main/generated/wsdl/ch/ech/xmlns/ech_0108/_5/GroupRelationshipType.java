
package ch.ech.xmlns.ech_0108._5;

import java.util.ArrayList;
import java.util.List;
import ch.ech.xmlns.ech_0097._5.UidStructureType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * &lt;p&gt;Java-Klasse für groupRelationshipType complex type.&lt;/p&gt;
 * 
 * &lt;p&gt;Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.&lt;/p&gt;
 * 
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="groupRelationshipType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="groupName" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;element name="organisationMembershipRole" type="{http://www.ech.ch/xmlns/eCH-0108/5}organisationMembershipRoleType"/&gt;
 *         &lt;element name="groupParticipant" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="participant" type="{http://www.ech.ch/xmlns/eCH-0097/5}uidStructureType" minOccurs="0"/&gt;
 *                   &lt;element name="participantRole" type="{http://www.ech.ch/xmlns/eCH-0108/5}organisationMembershipRoleType" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "groupRelationshipType", propOrder = {
    "groupName",
    "organisationMembershipRole",
    "groupParticipant"
})
public class GroupRelationshipType {

    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String groupName;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected OrganisationMembershipRoleType organisationMembershipRole;
    protected List<GroupRelationshipType.GroupParticipant> groupParticipant;

    /**
     * Ruft den Wert der groupName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Legt den Wert der groupName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGroupName(String value) {
        this.groupName = value;
    }

    /**
     * Ruft den Wert der organisationMembershipRole-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link OrganisationMembershipRoleType }
     *     
     */
    public OrganisationMembershipRoleType getOrganisationMembershipRole() {
        return organisationMembershipRole;
    }

    /**
     * Legt den Wert der organisationMembershipRole-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganisationMembershipRoleType }
     *     
     */
    public void setOrganisationMembershipRole(OrganisationMembershipRoleType value) {
        this.organisationMembershipRole = value;
    }

    /**
     * Gets the value of the groupParticipant property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a {@code set} method for the groupParticipant property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getGroupParticipant().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GroupRelationshipType.GroupParticipant }
     * </p>
     * 
     * 
     * @return
     *     The value of the groupParticipant property.
     */
    public List<GroupRelationshipType.GroupParticipant> getGroupParticipant() {
        if (groupParticipant == null) {
            groupParticipant = new ArrayList<>();
        }
        return this.groupParticipant;
    }


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
     *         &lt;element name="participant" type="{http://www.ech.ch/xmlns/eCH-0097/5}uidStructureType" minOccurs="0"/&gt;
     *         &lt;element name="participantRole" type="{http://www.ech.ch/xmlns/eCH-0108/5}organisationMembershipRoleType" minOccurs="0"/&gt;
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
        "participant",
        "participantRole"
    })
    public static class GroupParticipant {

        protected UidStructureType participant;
        @XmlSchemaType(name = "string")
        protected OrganisationMembershipRoleType participantRole;

        /**
         * Ruft den Wert der participant-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link UidStructureType }
         *     
         */
        public UidStructureType getParticipant() {
            return participant;
        }

        /**
         * Legt den Wert der participant-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link UidStructureType }
         *     
         */
        public void setParticipant(UidStructureType value) {
            this.participant = value;
        }

        /**
         * Ruft den Wert der participantRole-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link OrganisationMembershipRoleType }
         *     
         */
        public OrganisationMembershipRoleType getParticipantRole() {
            return participantRole;
        }

        /**
         * Legt den Wert der participantRole-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link OrganisationMembershipRoleType }
         *     
         */
        public void setParticipantRole(OrganisationMembershipRoleType value) {
            this.participantRole = value;
        }

    }

}
