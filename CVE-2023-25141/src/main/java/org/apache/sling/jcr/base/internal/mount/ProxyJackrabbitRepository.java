/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.jcr.base.internal.mount;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.api.JackrabbitSession;

public class ProxyJackrabbitRepository extends ProxyRepository<JackrabbitRepository> implements JackrabbitRepository {
    public ProxyJackrabbitRepository(JackrabbitRepository jcr, JackrabbitRepository mount, Set<String> mountPoint) {
        super(jcr, mount, mountPoint);
    }

    @Override
    public Session login(Credentials credentials, String workspaceName, Map<String, Object> attributes) throws LoginException, NoSuchWorkspaceException, RepositoryException {
        Session jcrSession = jcr.login(credentials, workspaceName, attributes);

        if (attributes == null) {
            attributes = new HashMap<>();
        }
        attributes.put(ProxyRepository.class.getPackage().getName() + ".PARENT_SESSION", jcrSession);

        Session mountSession = mount.login(credentials, workspaceName, attributes);

        return jcrSession instanceof JackrabbitSession ?
                new ProxyJackrabbitSession(this, (JackrabbitSession) jcrSession, mountSession, this.mountPoints) :
                new ProxySession<>(this, jcrSession, mountSession, this.mountPoints);
    }

    @Override
    public void shutdown() {

    }
}
