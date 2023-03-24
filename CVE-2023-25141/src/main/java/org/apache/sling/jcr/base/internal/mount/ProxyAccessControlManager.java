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
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.security.AccessControlException;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.AccessControlPolicyIterator;
import javax.jcr.security.Privilege;
import javax.jcr.version.VersionException;

public class ProxyAccessControlManager<T extends AccessControlManager> extends ProxyWrapper<T> implements AccessControlManager {
    final T mount;

    public ProxyAccessControlManager(ProxySession<?> mountSession, T delegate, T mount) {
        super(mountSession, delegate);
        this.mount = mount;
    }

    public Privilege[] getSupportedPrivileges(String absPath) throws PathNotFoundException, RepositoryException {
        if (mountSession.isMount(absPath)) {
            return mount.getSupportedPrivileges(absPath);
        }
        return delegate.getSupportedPrivileges(absPath);
    }

    public Privilege privilegeFromName(String privilegeName) throws AccessControlException, RepositoryException {
        try {
            return delegate.privilegeFromName(privilegeName);
        } catch (AccessControlException ex) {
            return mount.privilegeFromName(privilegeName);
        }
    }

    public boolean hasPrivileges(String absPath, Privilege[] privileges) throws PathNotFoundException, RepositoryException {
        if (mountSession.isMount(absPath)) {
            return mount.hasPrivileges(absPath, privileges);
        }
        return delegate.hasPrivileges(absPath, privileges);
    }

    public Privilege[] getPrivileges(String absPath) throws PathNotFoundException, RepositoryException {
        if (mountSession.isMount(absPath)) {
            return mount.getPrivileges(absPath);
        }
        return delegate.getPrivileges(absPath);
    }

    public AccessControlPolicy[] getPolicies(String absPath) throws PathNotFoundException, AccessDeniedException, RepositoryException {
        if (mountSession.isMount(absPath)) {
            return mount.getPolicies(absPath);
        }
        return delegate.getPolicies(absPath);
    }

    public AccessControlPolicy[] getEffectivePolicies(String absPath) throws PathNotFoundException, AccessDeniedException, RepositoryException {
        if (mountSession.isMount(absPath)) {
            return mount.getEffectivePolicies(absPath);
        }
        return delegate.getEffectivePolicies(absPath);
    }

    public AccessControlPolicyIterator getApplicablePolicies(String absPath) throws PathNotFoundException, AccessDeniedException, RepositoryException {
        if (mountSession.isMount(absPath)) {
            return mount.getApplicablePolicies(absPath);
        }
        return delegate.getApplicablePolicies(absPath);
    }

    public void setPolicy(String absPath, AccessControlPolicy policy) throws PathNotFoundException, AccessControlException, AccessDeniedException, LockException, VersionException, RepositoryException {
        if (mountSession.isMountParent(absPath) || mountSession.isMount(absPath)) {
            mount.setPolicy(absPath, policy);
        }
        if (!mountSession.isMount(absPath)) {
            delegate.setPolicy(absPath, policy);
        }
    }

    public void removePolicy(String absPath, AccessControlPolicy policy) throws PathNotFoundException, AccessControlException, AccessDeniedException, LockException, VersionException, RepositoryException {
        if (mountSession.isMountParent(absPath) || mountSession.isMount(absPath)) {
            mount.removePolicy(absPath, policy);
        }
        if (!mountSession.isMount(absPath)) {
            delegate.removePolicy(absPath, policy);
        }
    }
}
