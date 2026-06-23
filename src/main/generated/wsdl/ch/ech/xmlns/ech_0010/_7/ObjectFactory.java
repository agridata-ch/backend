
package ch.ech.xmlns.ech_0010._7;

import javax.xml.namespace.QName;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ch.ech.xmlns.ech_0010._7 package. 
 * <p>An ObjectFactory allows you to programmatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private static final QName _AddressInformationTypeForeignZipCode_QNAME = new QName("http://www.ech.ch/xmlns/eCH-0010/7", "foreignZipCode");
    private static final QName _AddressInformationTypeSwissZipCode_QNAME = new QName("http://www.ech.ch/xmlns/eCH-0010/7", "swissZipCode");
    private static final QName _AddressInformationTypeSwissZipCodeAddOn_QNAME = new QName("http://www.ech.ch/xmlns/eCH-0010/7", "swissZipCodeAddOn");
    private static final QName _AddressInformationTypeSwissZipCodeId_QNAME = new QName("http://www.ech.ch/xmlns/eCH-0010/7", "swissZipCodeId");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ch.ech.xmlns.ech_0010._7
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MailAddressType }
     * 
     * @return
     *     the new instance of {@link MailAddressType }
     */
    public MailAddressType createMailAddressType() {
        return new MailAddressType();
    }

    /**
     * Create an instance of {@link OrganisationMailAddressInfoType }
     * 
     * @return
     *     the new instance of {@link OrganisationMailAddressInfoType }
     */
    public OrganisationMailAddressInfoType createOrganisationMailAddressInfoType() {
        return new OrganisationMailAddressInfoType();
    }

    /**
     * Create an instance of {@link PersonMailAddressInfoType }
     * 
     * @return
     *     the new instance of {@link PersonMailAddressInfoType }
     */
    public PersonMailAddressInfoType createPersonMailAddressInfoType() {
        return new PersonMailAddressInfoType();
    }

    /**
     * Create an instance of {@link AddressInformationType }
     * 
     * @return
     *     the new instance of {@link AddressInformationType }
     */
    public AddressInformationType createAddressInformationType() {
        return new AddressInformationType();
    }

    /**
     * Create an instance of {@link CountryType }
     * 
     * @return
     *     the new instance of {@link CountryType }
     */
    public CountryType createCountryType() {
        return new CountryType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.ech.ch/xmlns/eCH-0010/7", name = "foreignZipCode", scope = AddressInformationType.class)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public JAXBElement<String> createAddressInformationTypeForeignZipCode(String value) {
        return new JAXBElement<>(_AddressInformationTypeForeignZipCode_QNAME, String.class, AddressInformationType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Long }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.ech.ch/xmlns/eCH-0010/7", name = "swissZipCode", scope = AddressInformationType.class)
    public JAXBElement<Long> createAddressInformationTypeSwissZipCode(Long value) {
        return new JAXBElement<>(_AddressInformationTypeSwissZipCode_QNAME, Long.class, AddressInformationType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.ech.ch/xmlns/eCH-0010/7", name = "swissZipCodeAddOn", scope = AddressInformationType.class)
    public JAXBElement<String> createAddressInformationTypeSwissZipCodeAddOn(String value) {
        return new JAXBElement<>(_AddressInformationTypeSwissZipCodeAddOn_QNAME, String.class, AddressInformationType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.ech.ch/xmlns/eCH-0010/7", name = "swissZipCodeId", scope = AddressInformationType.class)
    public JAXBElement<Integer> createAddressInformationTypeSwissZipCodeId(Integer value) {
        return new JAXBElement<>(_AddressInformationTypeSwissZipCodeId_QNAME, Integer.class, AddressInformationType.class, value);
    }

}
