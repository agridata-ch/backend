
package ch.admin.uid.xmlns.uid_wse;

import javax.xml.namespace.QName;
import ch.admin.uid.xmlns.uid_wse_shared._2.BusinessFault;
import ch.admin.uid.xmlns.uid_wse_shared._2.InfrastructureFault;
import ch.admin.uid.xmlns.uid_wse_shared._2.SecurityFault;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ch.admin.uid.xmlns.uid_wse package. 
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

    private static final QName _SecurityFault_QNAME = new QName("http://www.uid.admin.ch/xmlns/uid-wse", "securityFault");
    private static final QName _BusinessFault_QNAME = new QName("http://www.uid.admin.ch/xmlns/uid-wse", "businessFault");
    private static final QName _InfrastructureFault_QNAME = new QName("http://www.uid.admin.ch/xmlns/uid-wse", "infrastructureFault");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ch.admin.uid.xmlns.uid_wse
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetOrganisationSample }
     * 
     * @return
     *     the new instance of {@link GetOrganisationSample }
     */
    public GetOrganisationSample createGetOrganisationSample() {
        return new GetOrganisationSample();
    }

    /**
     * Create an instance of {@link GetOrganisationSampleResponse }
     * 
     * @return
     *     the new instance of {@link GetOrganisationSampleResponse }
     */
    public GetOrganisationSampleResponse createGetOrganisationSampleResponse() {
        return new GetOrganisationSampleResponse();
    }

    /**
     * Create an instance of {@link GetByUID }
     * 
     * @return
     *     the new instance of {@link GetByUID }
     */
    public GetByUID createGetByUID() {
        return new GetByUID();
    }

    /**
     * Create an instance of {@link GetByUIDResponse }
     * 
     * @return
     *     the new instance of {@link GetByUIDResponse }
     */
    public GetByUIDResponse createGetByUIDResponse() {
        return new GetByUIDResponse();
    }

    /**
     * Create an instance of {@link ArrayOfOrganisationType }
     * 
     * @return
     *     the new instance of {@link ArrayOfOrganisationType }
     */
    public ArrayOfOrganisationType createArrayOfOrganisationType() {
        return new ArrayOfOrganisationType();
    }

    /**
     * Create an instance of {@link Search }
     * 
     * @return
     *     the new instance of {@link Search }
     */
    public Search createSearch() {
        return new Search();
    }

    /**
     * Create an instance of {@link SearchResponse }
     * 
     * @return
     *     the new instance of {@link SearchResponse }
     */
    public SearchResponse createSearchResponse() {
        return new SearchResponse();
    }

    /**
     * Create an instance of {@link ValidateVatNumber }
     * 
     * @return
     *     the new instance of {@link ValidateVatNumber }
     */
    public ValidateVatNumber createValidateVatNumber() {
        return new ValidateVatNumber();
    }

    /**
     * Create an instance of {@link ValidateVatNumberResponse }
     * 
     * @return
     *     the new instance of {@link ValidateVatNumberResponse }
     */
    public ValidateVatNumberResponse createValidateVatNumberResponse() {
        return new ValidateVatNumberResponse();
    }

    /**
     * Create an instance of {@link ValidateUID }
     * 
     * @return
     *     the new instance of {@link ValidateUID }
     */
    public ValidateUID createValidateUID() {
        return new ValidateUID();
    }

    /**
     * Create an instance of {@link ValidateUIDResponse }
     * 
     * @return
     *     the new instance of {@link ValidateUIDResponse }
     */
    public ValidateUIDResponse createValidateUIDResponse() {
        return new ValidateUIDResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SecurityFault }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link SecurityFault }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.uid.admin.ch/xmlns/uid-wse", name = "securityFault")
    public JAXBElement<SecurityFault> createSecurityFault(SecurityFault value) {
        return new JAXBElement<>(_SecurityFault_QNAME, SecurityFault.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BusinessFault }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link BusinessFault }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.uid.admin.ch/xmlns/uid-wse", name = "businessFault")
    public JAXBElement<BusinessFault> createBusinessFault(BusinessFault value) {
        return new JAXBElement<>(_BusinessFault_QNAME, BusinessFault.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InfrastructureFault }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link InfrastructureFault }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.uid.admin.ch/xmlns/uid-wse", name = "infrastructureFault")
    public JAXBElement<InfrastructureFault> createInfrastructureFault(InfrastructureFault value) {
        return new JAXBElement<>(_InfrastructureFault_QNAME, InfrastructureFault.class, null, value);
    }

}
