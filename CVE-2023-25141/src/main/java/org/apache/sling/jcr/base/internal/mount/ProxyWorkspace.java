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

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.InvalidSerializedDataException;
import javax.jcr.ItemExistsException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Workspace;
import javax.jcr.lock.LockException;
import javax.jcr.lock.LockManager;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.QueryManager;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionManager;

import org.xml.sax.ContentHandler;

public class ProxyWorkspace<T extends Workspace> extends ProxyWrapper<T> implements Workspace {
    final T delegate2;

    public ProxyWorkspace(ProxySession mountSession, T delegate, T delegate2) {
        super(mountSession, delegate);
        this.delegate2 = delegate2;
    }

    @Override
    public Session getSession() {
        return this.mountSession;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public QueryManager getQueryManager() throws RepositoryException {
        return new ProxyQueryManager(this.mountSession, delegate.getQueryManager(), this.delegate2.getQueryManager());
    }

    // TODO: revisit the below

    @Override
    public ContentHandler getImportContentHandler(String parentAbsPath, int uuidBehavior) throws PathNotFoundException, ConstraintViolationException, VersionException, LockException, AccessDeniedException, RepositoryException {
        return this.mountSession.getImportContentHandler(parentAbsPath, uuidBehavior);
    }

    @Override
    public void importXML(String parentAbsPath, InputStream in, int uuidBehavior) throws IOException, VersionException, PathNotFoundException, ItemExistsException, ConstraintViolationException, InvalidSerializedDataException, LockException, AccessDeniedException, RepositoryException {
        this.mountSession.importXML(parentAbsPath, in, uuidBehavior);
    }

    @Override
    public void copy(String srcAbsPath, String destAbsPath) throws ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        if (mountSession.isMount(srcAbsPath) && mountSession.isMount(destAbsPath)) {
            delegate2.copy(srcAbsPath, destAbsPath);
        } else {
            delegate.copy(srcAbsPath, destAbsPath);
        }
    }

    @Override
    public void copy(String srcWorkspace, String srcAbsPath, String destAbsPath) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        if (mountSession.isMount(srcAbsPath) && mountSession.isMount(destAbsPath)) {
            delegate2.copy(srcWorkspace, srcAbsPath, destAbsPath);
        } else {
            delegate.copy(srcWorkspace, srcAbsPath, destAbsPath);
        }
    }

    @Override
    public void clone(String srcWorkspace, String srcAbsPath, String destAbsPath, boolean removeExisting) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        if (mountSession.isMount(srcAbsPath) && mountSession.isMount(destAbsPath)) {
            delegate2.clone(srcWorkspace, srcAbsPath, destAbsPath, removeExisting);
        } else {
            delegate.clone(srcWorkspace, srcAbsPath, destAbsPath, removeExisting);
        }
    }

    @Override
    public void move(String srcAbsPath, String destAbsPath) throws ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        if (mountSession.isMount(srcAbsPath) && mountSession.isMount(destAbsPath)) {
            delegate2.move(srcAbsPath, destAbsPath);
        } else {
            delegate.move(srcAbsPath, destAbsPath);
        }
    }

    @Override
    public void restore(Version[] versions, boolean removeExisting) throws ItemExistsException, UnsupportedRepositoryOperationException, VersionException, LockException, InvalidItemStateException, RepositoryException {
        delegate.restore(versions, removeExisting);
    }

    @Override
    public void createWorkspace(String name) throws AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
        delegate.createWorkspace(name);
    }

    @Override
    public void createWorkspace(String name, String srcWorkspace) throws AccessDeniedException, UnsupportedRepositoryOperationException, NoSuchWorkspaceException, RepositoryException {
        delegate.createWorkspace(name, srcWorkspace);
    }

    @Override
    public void deleteWorkspace(String name) throws AccessDeniedException, UnsupportedRepositoryOperationException, NoSuchWorkspaceException, RepositoryException {
        delegate.deleteWorkspace(name);
    }


    @Override
    public LockManager getLockManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        return delegate.getLockManager();
    }

    @Override
    public NamespaceRegistry getNamespaceRegistry() throws RepositoryException {
        return new ProxyNamespaceRegistry(delegate.getNamespaceRegistry(), this.delegate2.getNamespaceRegistry());
    }

    @Override
    public NodeTypeManager getNodeTypeManager() throws RepositoryException {
        return new ProxyNodeTypeManager(delegate.getNodeTypeManager(), this.delegate2.getNodeTypeManager());
    }

    @Override
    public ObservationManager getObservationManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        return delegate.getObservationManager();
    }

    @Override
    public VersionManager getVersionManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        return delegate.getVersionManager();
    }

    @Override
    public String[] getAccessibleWorkspaceNames() throws RepositoryException {
        return delegate.getAccessibleWorkspaceNames();
    }
}
