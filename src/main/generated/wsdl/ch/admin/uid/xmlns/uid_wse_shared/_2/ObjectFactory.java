
package ch.admin.uid.xmlns.uid_wse_shared._2;

import jakarta.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ch.admin.uid.xmlns.uid_wse_shared._2 package. 
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


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ch.admin.uid.xmlns.uid_wse_shared._2
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SecurityFault }
     * 
     * @return
     *     the new instance of {@link SecurityFault }
     */
    public SecurityFault createSecurityFault() {
        return new SecurityFault();
    }

    /**
     * Create an instance of {@link BusinessFault }
     * 
     * @return
     *     the new instance of {@link BusinessFault }
     */
    public BusinessFault createBusinessFault() {
        return new BusinessFault();
    }

    /**
     * Create an instance of {@link InfrastructureFault }
     * 
     * @return
     *     the new instance of {@link InfrastructureFault }
     */
    public InfrastructureFault createInfrastructureFault() {
        return new InfrastructureFault();
    }

    /**
     * Create an instance of {@link SearchConfiguration }
     * 
     * @return
     *     the new instance of {@link SearchConfiguration }
     */
    public SearchConfiguration createSearchConfiguration() {
        return new SearchConfiguration();
    }

    /**
     * Create an instance of {@link ServiceFault }
     * 
     * @return
     *     the new instance of {@link ServiceFault }
     */
    public ServiceFault createServiceFault() {
        return new ServiceFault();
    }

}
