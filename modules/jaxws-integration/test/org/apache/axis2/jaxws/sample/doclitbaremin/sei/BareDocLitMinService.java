
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.jaxws.sample.doclitbaremin.sei;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class was generated by the JAXWS SI.
 * JAX-WS RI 2.0_01-b15-fcs
 * Generated source version: 2.0
 *
 */
@WebServiceClient(name = "BareDocLitMinService", targetNamespace = "http://org.test.sample.doclitbaremin", wsdlLocation = "doclitbaremin.wsdl")
public class BareDocLitMinService
    extends Service
{

    private final static URL BAREDOCLITMINSERVICE_WSDL_LOCATION;

    private static String wsdlLocation="/test/org/apache/axis2/jaxws/sample/doclitbaremin/META-INF/doclitbaremin.wsdl";
    static {
        URL url = null;
        try {
        	try{
	        	String baseDir = new File(System.getProperty("basedir",".")).getCanonicalPath();
	        	wsdlLocation = new File(baseDir + wsdlLocation).getAbsolutePath();
        	}catch(Exception e){
        		e.printStackTrace();
        	}
        	File file = new File(wsdlLocation);
        	url = file.toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        BAREDOCLITMINSERVICE_WSDL_LOCATION = url;
    }

    public BareDocLitMinService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public BareDocLitMinService() {
        super(BAREDOCLITMINSERVICE_WSDL_LOCATION, new QName("http://doclitbaremin.sample.test.org", "BareDocLitMinService"));
    }

    /**
     *
     * @return
     *     returns DocLitBarePortType
     */
    @WebEndpoint(name = "BareDocLitMinPort")
    public DocLitBareMinPortType getBareDocLitMinPort() {
        return (DocLitBareMinPortType)super.getPort(new QName("http://doclitbaremin.sample.test.org", "BareDocLitMinPort"), DocLitBareMinPortType.class);
    }

}
