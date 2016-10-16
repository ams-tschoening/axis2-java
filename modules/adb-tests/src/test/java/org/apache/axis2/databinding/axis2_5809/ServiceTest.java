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
package org.apache.axis2.databinding.axis2_5809;

import static com.google.common.truth.Truth.assertThat;

import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;

public class ServiceTest {
    @ClassRule
    public static Axis2Server server = new Axis2Server("target/repo/AXIS2-5809");
    
    @Test
    public void test() throws Exception {
        EchoServiceStub stub = new EchoServiceStub(server.getEndpoint("EchoService"));
        for (int i=0; i<500; i++) {
            Echo request = new Echo();
            request.setContent("test");
            assertThat(stub.echo(request).getContent()).isEqualTo("test");
        }
    }
}
