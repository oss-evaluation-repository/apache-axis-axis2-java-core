//
// Generated By:JAX-WS RI IBM 2.1.1 in JDK 6 (JAXB RI IBM JAXB 2.1.3 in JDK 1.6)
//


package org.apache.axis2.jaxws.jaxb.string;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

@WebService(name = "JAXBStringPortType", targetNamespace = "http://string.jaxb.jaxws.axis2.apache.org")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@XmlSeeAlso({
    ObjectFactory.class
})
public interface JAXBStringPortType {


    /**
     * 
     * @param echo
     * @return
     *     returns org.apache.axis2.jaxws.jaxb.string.EchoResponse
     */
    @WebMethod(action = "http://string.jaxb.jaxws.axis2.apache.org/echoString")
    @WebResult(name = "echoResponse", targetNamespace = "http://string.jaxb.jaxws.axis2.apache.org", partName = "echoResponse")
    public EchoResponse echoString(
        @WebParam(name = "echo", targetNamespace = "http://string.jaxb.jaxws.axis2.apache.org", partName = "echo")
        Echo echo);

}