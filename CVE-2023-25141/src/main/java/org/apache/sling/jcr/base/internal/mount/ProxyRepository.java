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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;

import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.sling.jcr.base.spi.RepositoryMount;

public class ProxyRepository<T extends Repository> implements Repository {
    public final T jcr;
    final T mount;
    final Set<String> mountPoints;

    public ProxyRepository(T jcr, T mount, Set<String> mountPoint) {
        this.jcr = jcr;
        this.mount = mount;
        this.mountPoints = new HashSet<>(mountPoint);
    }


    @Override
    public String[] getDescriptorKeys() {
        return jcr.getDescriptorKeys();
    }

    @Override
    public boolean isStandardDescriptor(String key) {
        return jcr.isStandardDescriptor(key);
    }

    @Override
    public boolean isSingleValueDescriptor(String key) {
        return jcr.isSingleValueDescriptor(key);
    }

    @Override
    public Value getDescriptorValue(String key) {
        return jcr.getDescriptorValue(key);
    }

    @Override
    public Value[] getDescriptorValues(String key) {
        return jcr.getDescriptorValues(key);
    }

    @Override
    public String getDescriptor(String key) {
        return jcr.getDescriptor(key);
    }

    @Override
    public Session login(Credentials credentials, String workspaceName) throws LoginException, NoSuchWorkspaceException, RepositoryException {
        Session jcrSession = jcr.login(credentials, workspaceName);

        Session mountSession;
        if (mount instanceof JackrabbitRepository) {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(RepositoryMount.PARENT_SESSION_KEY, jcrSession);
            mountSession = ((JackrabbitRepository) mount).login(credentials, workspaceName, attributes);
        }
        else {
            mountSession = mount.login(credentials, workspaceName);
        }
        return jcrSession instanceof JackrabbitSession ?
                new ProxyJackrabbitSession(this, (JackrabbitSession) jcrSession, mountSession, this.mountPoints) :
                new ProxySession<>(this, jcrSession, mountSession, this.mountPoints);
    }

    @Override
    public Session login(Credentials credentials) throws LoginException, RepositoryException {
        return login(credentials, null);
    }

    @Override
    public Session login(String workspaceName) throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return login(null, workspaceName);
    }

    @Override
    public Session login() throws LoginException, RepositoryException {
        return login(null, null);
    }

    public Session wrap(Session session) throws RepositoryException {
        if (session instanceof ProxySession) {
            return session;
        }

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(RepositoryMount.PARENT_SESSION_KEY, session);
        Session mountSession = ((JackrabbitRepository) mount).login(new SimpleCredentials(session.getUserID(), new char[0]),session.getWorkspace().getName(), attributes );

        return session instanceof JackrabbitSession ?
                new ProxyJackrabbitSession(this, (JackrabbitSession) session, mountSession, this.mountPoints) :
                new ProxySession<>(this, session, mountSession, this.mountPoints);
    }

    Session impersonate(Credentials credentials, Session jcr, Session mount) throws RepositoryException {
        return jcr instanceof JackrabbitSession ?
                new ProxyJackrabbitSession(this, (JackrabbitSession) jcr.impersonate(credentials), mount.impersonate(credentials), this.mountPoints) :
                new ProxySession<>(this, jcr.impersonate(credentials), mount.impersonate(credentials), this.mountPoints);
    }
}
