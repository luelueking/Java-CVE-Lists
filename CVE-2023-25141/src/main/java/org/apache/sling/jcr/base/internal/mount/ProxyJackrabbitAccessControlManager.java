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

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.security.AccessControlException;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.Privilege;

import org.apache.jackrabbit.api.security.JackrabbitAccessControlManager;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlPolicy;

public class ProxyJackrabbitAccessControlManager extends ProxyAccessControlManager<JackrabbitAccessControlManager> implements JackrabbitAccessControlManager {
    public ProxyJackrabbitAccessControlManager(ProxySession<?> mountSession, JackrabbitAccessControlManager delegate, JackrabbitAccessControlManager mount) {
        super(mountSession, delegate, mount);
    }

    public JackrabbitAccessControlPolicy[] getApplicablePolicies(Principal principal) throws AccessDeniedException, AccessControlException, UnsupportedRepositoryOperationException, RepositoryException {
        List<JackrabbitAccessControlPolicy> result = new ArrayList<>();
        result.addAll(Arrays.asList(delegate.getApplicablePolicies(principal)));
        result.addAll(Arrays.asList(mount.getApplicablePolicies(principal)));
        return result.toArray(new JackrabbitAccessControlPolicy[0]);
    }

    public JackrabbitAccessControlPolicy[] getPolicies(Principal principal) throws AccessDeniedException, AccessControlException, UnsupportedRepositoryOperationException, RepositoryException {
        List<JackrabbitAccessControlPolicy> result = new ArrayList<>();
        result.addAll(Arrays.asList(delegate.getPolicies(principal)));
        result.addAll(Arrays.asList(mount.getPolicies(principal)));
        return result.toArray(new JackrabbitAccessControlPolicy[0]);
    }

    public AccessControlPolicy[] getEffectivePolicies(Set<Principal> principals) throws AccessDeniedException, AccessControlException, UnsupportedRepositoryOperationException, RepositoryException {
        List<AccessControlPolicy> result = new ArrayList<>();
        result.addAll(Arrays.asList(delegate.getEffectivePolicies(principals)));
        result.addAll(Arrays.asList(mount.getEffectivePolicies(principals)));
        return result.toArray(new AccessControlPolicy[0]);
    }

    public boolean hasPrivileges(String absPath, Set<Principal> principals, Privilege[] privileges) throws PathNotFoundException, AccessDeniedException, RepositoryException {
        if (mountSession.isMount(absPath)) {
            return mount.hasPrivileges(absPath, principals, privileges);
        }
        return delegate.hasPrivileges(absPath, principals, privileges);
    }

    public Privilege[] getPrivileges(String absPath, Set<Principal> principals) throws PathNotFoundException, AccessDeniedException, RepositoryException {
        if (mountSession.isMount(absPath)) {
            return mount.getPrivileges(absPath, principals);
        }
        return delegate.getPrivileges(absPath, principals);
    }
}
