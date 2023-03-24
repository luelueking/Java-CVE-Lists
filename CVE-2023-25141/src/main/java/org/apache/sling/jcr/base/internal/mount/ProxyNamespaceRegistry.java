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

import javax.jcr.AccessDeniedException;
import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;

public class ProxyNamespaceRegistry implements NamespaceRegistry {
    private final NamespaceRegistry jcr;
    private final NamespaceRegistry mount;

    public ProxyNamespaceRegistry(NamespaceRegistry jcr, NamespaceRegistry mount) {
        this.jcr = jcr;
        this.mount = mount;
    }

    @Override
    public void registerNamespace(String prefix, String uri) throws NamespaceException, UnsupportedRepositoryOperationException, AccessDeniedException, RepositoryException {
        jcr.registerNamespace(prefix, uri);
        mount.registerNamespace(prefix, uri);
    }

    @Override
    public void unregisterNamespace(String prefix) throws NamespaceException, UnsupportedRepositoryOperationException, AccessDeniedException, RepositoryException {
        jcr.unregisterNamespace(prefix);
        mount.unregisterNamespace(prefix);
    }

    @Override
    public String[] getPrefixes() throws RepositoryException {
        return jcr.getPrefixes();
    }

    @Override
    public String[] getURIs() throws RepositoryException {
        return jcr.getURIs();
    }

    @Override
    public String getURI(String prefix) throws NamespaceException, RepositoryException {
        return jcr.getURI(prefix);
    }

    @Override
    public String getPrefix(String uri) throws NamespaceException, RepositoryException {
        return jcr.getPrefix(uri);
    }
}
