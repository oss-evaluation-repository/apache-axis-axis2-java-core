package org.apache.axis2.rpc;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.util.BeanSerializerUtil;
import org.apache.axis2.AxisFault;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.util.Calendar;
import java.util.ArrayList;
import java.io.ByteArrayInputStream;
/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*
*/

/**
 * Author: Deepal Jayasinghe
 * Date: Oct 11, 2005
 * Time: 10:29:05 PM
 */
public class RPCServiceClass {

    public MyBean editBean(MyBean bean , int a){
        bean.setAge(a);
        return bean;
    }

    public MyBean echoBean(MyBean bean){
        return bean;
    }

    public String echoString(String in){
        return in;
    }

    public int echoInt(int i){
        return i;
    }

    public int add(int a , int b){
        return a+b;
    }

    public boolean echoBool(boolean b){
        return b;
    }

    public byte echoByte(byte b){
        return b;
    }
    public OMElement echoOM(OMElement b){
        SOAPFactory fac =   OMAbstractFactory.getSOAP12Factory();
        OMNamespace ns = fac.createOMNamespace(
                "http://soapenc/", "res");
        OMElement bodyContent = fac.createOMElement(
                "echoOMResponse", ns);
        OMElement child = fac.createOMElement("return", null);
        child.addChild(fac.createText(child, b.getText()));
        bodyContent.addChild(child);
//        bodyContent.addChild(b);
        return bodyContent;
    }

    public double divide(double a , double b){
        return (a/b);
    }

    public Calendar echoCalander(Calendar in){
        return in;
    }

    public OMElement multireturn(OMElement ele) throws XMLStreamException {
        SOAPFactory fac =   OMAbstractFactory.getSOAP12Factory();
        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "res");
        OMElement method = fac.createOMElement("multiretuenResponse", omNs);
        OMElement value1 = fac.createOMElement("return0", null);
        value1.addChild(
                fac.createText(value1, "10"));
        method.addChild(value1);
        OMElement value2 = fac.createOMElement("return1", null);
        value2.addChild(
                fac.createText(value2, "foo"));
        method.addChild(value2);
        return   method;
    }


    /**
     * This methods return mutiple object , so it creat an Object array and retuen that
     * so , if a method want to return mutiple value , this way can be used
     * @param obj
     * @return Object []
     */
    public Object[] mulReturn(OMElement obj){
        ArrayList objs= new ArrayList();
        objs.add(new Integer(100));
        MyBean bean = new MyBean();
        bean.setAge(100);
        bean.setName("Deepal");
        bean.setValue(false);
        AddressBean ab = new AddressBean();
        ab.setNumber(1010);
        ab.setTown("Colombo3");
        bean.setAddress(ab);
        objs.add(bean);
        return objs.toArray();
    }

    public MyBean beanOM(OMElement element, int val) throws AxisFault {
        MyBean bean =(MyBean)BeanSerializerUtil.deserialize(MyBean.class,element);
        bean.setAge(val);
        return bean;
    }

    public boolean omrefs(OMElement element, OMElement element2) throws AxisFault {
        MyBean bean =(MyBean)BeanSerializerUtil.deserialize(MyBean.class,element);
        MyBean bean2 =(MyBean)BeanSerializerUtil.deserialize(MyBean.class,element2);
        if(bean2 !=null && bean !=null){
            return true;
        } else{
            return false;
        }
    }
}
