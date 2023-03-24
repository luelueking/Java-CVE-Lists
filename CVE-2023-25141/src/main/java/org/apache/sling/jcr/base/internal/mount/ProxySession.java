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
import java.io.OutputStream;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.Credentials;
import javax.jcr.InvalidItemStateException;
import javax.jcr.InvalidSerializedDataException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.NamespaceException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.jcr.retention.RetentionManager;
import javax.jcr.security.AccessControlManager;
import javax.jcr.version.VersionException;

import org.apache.jackrabbit.api.security.JackrabbitAccessControlManager;
import org.apache.jackrabbit.commons.iterator.NodeIteratorAdapter;
import org.apache.jackrabbit.oak.commons.PathUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class ProxySession<T extends Session> implements Session {
    private final ProxyRepository repository;
    public final T jcr;
    protected final Session mount;
    private final Set<String> mountPoints;

    public ProxySession(ProxyRepository repository, T jcr, Session mount, Set<String> mountPoints) {
        this.repository = repository;
        this.jcr = jcr;
        this.mount = mount;
        this.mountPoints = mountPoints;
    }

    boolean isMount(String path) {
        return path != null && (mountPoints.contains(path) || mountPoints.stream().anyMatch(mountPoint -> path.startsWith(mountPoint + "/")));
    }

    boolean isMountParent(String path) {
        return mountPoints.stream().anyMatch(mountPoint -> mountPoint.startsWith((path + "/").replace("//", "/")));
    }

    boolean isMountDirectParent(String path) {
        return mountPoints.stream().anyMatch(mountPoint -> PathUtils.getParentPath(mountPoint).equals(path));
    }

    public <F> F wrap(F source) {
        if (source instanceof ProxyWrapper) {
            return source;
        }
        return (F) (source instanceof Node ? new ProxyNode(this, (Node) source) :
                source instanceof Property ? new ProxyProperty(this, (Property) source) :
                        source instanceof Item ? new ProxyItem<>(this, (Item) source) :
                                source instanceof Lock ? new ProxyLock(this, (Lock) source) :
                                        source instanceof QueryResult ? new ProxyQueryResult(this, (QueryResult) source) :
                                                source);
    }

    public <F> F unwrap(F source) {
        return (F) (source instanceof ProxyWrapper ? ((ProxyWrapper) source).delegate : source);
    }

    public NodeIterator wrap(final NodeIterator iter) {
        return new NodeIteratorAdapter(new Iterator<Node>() {
            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public Node next() {
                return wrap(iter.nextNode());
            }

            @Override
            public void remove() {
                iter.remove();
            }
        });
    }

    public PropertyIterator wrap(final PropertyIterator iter) {
        return new PropertyIterator() {
            @Override
            public Property nextProperty() {
                return wrap(iter.nextProperty());
            }

            @Override
            public void skip(long skipNum) {
                iter.skip(skipNum);
            }

            @Override
            public long getSize() {
                return iter.getSize();
            }

            @Override
            public long getPosition() {
                return iter.getPosition();
            }

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public void remove() {
                iter.remove();
            }

            @Override
            public Object next() {
                return wrap(iter.next());
            }
        };
    }

    public RowIterator wrap(final RowIterator iter) {
        return new RowIterator() {

            @Override
            public Row nextRow() {
                final Row row = iter.nextRow();

                return new Row() {
                    @Override
                    public Value[] getValues() throws RepositoryException {
                        return row.getValues();
                    }

                    @Override
                    public Value getValue(String s) throws ItemNotFoundException, RepositoryException {
                        return row.getValue(s);
                    }

                    @Override
                    public Node getNode() throws RepositoryException {
                        return wrap(row.getNode());
                    }

                    @Override
                    public Node getNode(String s) throws RepositoryException {
                        return wrap(row.getNode(s));
                    }

                    @Override
                    public String getPath() throws RepositoryException {
                        return row.getPath();
                    }

                    @Override
                    public String getPath(String s) throws RepositoryException {
                        return row.getPath(s);
                    }

                    @Override
                    public double getScore() throws RepositoryException {
                        return row.getScore();
                    }

                    @Override
                    public double getScore(String s) throws RepositoryException {
                        return row.getScore(s);
                    }
                };
            }

            @Override
            public void skip(long l) {
                iter.skip(l);
            }

            @Override
            public long getSize() {
                return iter.getSize();
            }

            @Override
            public long getPosition() {
                return iter.getPosition();
            }

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public Object next() {
                return nextRow();
            }

            @Override
            public void remove() {
                iter.remove();
            }
        };
    }

    @Override
    public Repository getRepository() {
        return this.repository;
    }

    @Override
    public String getUserID() {
        return this.jcr.getUserID();
    }

    @Override
    public String[] getAttributeNames() {
        return this.jcr.getAttributeNames();
    }

    @Override
    public Object getAttribute(String name) {
        return this.jcr.getAttribute(name);
    }

    @Override
    public Node getRootNode() throws RepositoryException {
        return wrap(this.jcr.getRootNode());
    }

    public NodeIterator getNodes(String path, NodeIterator childs) throws RepositoryException {
        if (isMountDirectParent(path)) {
            List<Node> buffer = new ArrayList<>();
            while (childs.hasNext()) {
                Node child = childs.nextNode();
                if (!isMount(child.getPath())) {
                    buffer.add(child);
                }
            }
            for (String mountPoint : this.mountPoints) {
                if (PathUtils.getParentPath(mountPoint).equals(path)) {
                    buffer.add(this.mount.getNode(mountPoint));
                }
            }
            childs = new NodeIteratorAdapter(buffer);
        }
        return wrap(childs);
    }

    public boolean hasNodes(Node node) throws RepositoryException {
        return isMountDirectParent(node.getPath()) || node.hasNodes();
    }

    @Override
    public Session impersonate(Credentials credentials) throws LoginException, RepositoryException {
        return this.repository.impersonate(credentials, this.jcr, this.mount);
    }

    @Override
    public Node getNodeByUUID(String uuid) throws ItemNotFoundException, RepositoryException {
        try {
            return wrap(this.jcr.getNodeByUUID(uuid));
        } catch (RepositoryException ex) {
            try {
                return wrap(this.mount.getNodeByUUID(uuid));
            } catch (RepositoryException ignore) {
                throw ex;
            }
        }
    }

    @Override
    public Node getNodeByIdentifier(String id) throws ItemNotFoundException, RepositoryException {
        try {
            return wrap(this.jcr.getNodeByIdentifier(id));
        } catch (RepositoryException ex) {
            try {
                return wrap(this.mount.getNodeByIdentifier(id));
            } catch (RepositoryException ignore) {
                throw ex;
            }
        }
    }

    @Override
    public Item getItem(String absPath) throws PathNotFoundException, RepositoryException {
        return wrap(isMount(absPath) ? this.mount.getItem(absPath) : this.jcr.getItem(absPath));
    }

    @Override
    public Node getNode(String absPath) throws PathNotFoundException, RepositoryException {
        return wrap(isMount(absPath) ? this.mount.getNode(absPath) : this.jcr.getNode(absPath));
    }

    @Override
    public Property getProperty(String absPath) throws PathNotFoundException, RepositoryException {
        return wrap(isMount(absPath) ? this.mount.getProperty(absPath) : this.jcr.getProperty(absPath));
    }

    @Override
    public boolean itemExists(String absPath) throws RepositoryException {
        return isMount(absPath) ? this.mount.itemExists(absPath) : this.jcr.itemExists(absPath);
    }

    @Override
    public boolean nodeExists(String absPath) throws RepositoryException {
        return isMount(absPath) ? this.mount.nodeExists(absPath) : this.jcr.nodeExists(absPath);
    }

    @Override
    public boolean propertyExists(String absPath) throws RepositoryException {
        return isMount(absPath) ? this.mount.propertyExists(absPath) : this.jcr.propertyExists(absPath);
    }

    @Override
    public void removeItem(String absPath) throws VersionException, LockException, ConstraintViolationException, AccessDeniedException, RepositoryException {
        if (sync != null) {
            sync.remove(absPath);
        }
        if (isMount(absPath)) {
            this.mount.removeItem(absPath);
        } else {
            this.jcr.removeItem(absPath);
            if (isMountParent(absPath)) {
                for (String mountPoint : this.mountPoints) {
                    if (mountPoint.startsWith((absPath + "/").replace("//", "/"))) {
                        for (NodeIterator iter = this.mount.getNode(mountPoint).getNodes(); iter.hasNext(); ) {
                            iter.nextNode().remove();
                        }
                    }
                }
            }
        }
    }

    private volatile Set<String> sync;

    private final static List<String> ignore = Arrays.asList("jcr:primaryType", "jcr:created", "jcr:createdBy");

    @Override
    public void save() throws AccessDeniedException, ItemExistsException, ReferentialIntegrityException, ConstraintViolationException, InvalidItemStateException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        if (sync != null) {
            for (String path : sync) {
                if (this.jcr.nodeExists(path)) {
                    Node jcrNode = jcr.getNode(path);
                    Node mountNode = mount.nodeExists(path) ?
                            mount.getNode(path) :
                            mount.getNode(PathUtils.getParentPath(path)).addNode(PathUtils.getName(path), jcrNode.getPrimaryNodeType().getName());
                    for (PropertyIterator iter = jcrNode.getProperties(); iter.hasNext(); ) {
                        Property property = iter.nextProperty();
                        try {
                            if (property.isMultiple()) {
                                mountNode.setProperty(property.getName(), property.getValues());
                            } else {
                                mountNode.setProperty(property.getName(), property.getValue());
                            }
                        } catch (ConstraintViolationException ex) {
                        }
                    }
                }
            }
            sync = null;
        }

        this.jcr.save();

        this.mount.save();
    }

    @Override
    public void refresh(boolean keepChanges) throws RepositoryException {
        sync = null;
        this.jcr.refresh(keepChanges);
        this.mount.refresh(keepChanges);
    }

    public void refresh(String path, Item item, boolean keepChanges) throws RepositoryException {
        sync = null;
        item.refresh(keepChanges);
        if (!isMount(path) && isMountParent(path)) {
            this.mount.getRootNode().refresh(keepChanges);
        }
    }

    @Override
    public boolean hasPendingChanges() throws RepositoryException {
        return this.jcr.hasPendingChanges() || this.mount.hasPendingChanges();
    }

    @Override
    public ValueFactory getValueFactory() throws UnsupportedRepositoryOperationException, RepositoryException {
        return this.jcr.getValueFactory();
    }

    @Override
    public boolean hasPermission(String absPath, String actions) throws RepositoryException {
        return isMount(absPath) ? true : this.jcr.hasPermission(absPath, actions);
    }

    @Override
    public void checkPermission(String absPath, String actions) throws AccessControlException, RepositoryException {
        if (!isMount(absPath)) {
            this.jcr.checkPermission(absPath, actions);
        }
    }

    @Override
    public boolean hasCapability(String methodName, Object target, Object[] arguments) throws RepositoryException {
        return this.jcr.hasCapability(methodName, target, arguments);
    }

    @Override
    public ContentHandler getImportContentHandler(String parentAbsPath, int uuidBehavior) throws PathNotFoundException, ConstraintViolationException, VersionException, LockException, RepositoryException {
        if (isMount(parentAbsPath)) {
            return this.mount.getImportContentHandler(parentAbsPath, uuidBehavior);
        } else {
            return this.jcr.getImportContentHandler(parentAbsPath, uuidBehavior);
        }
    }

    @Override
    public void importXML(String parentAbsPath, InputStream in, int uuidBehavior) throws IOException, PathNotFoundException, ItemExistsException, ConstraintViolationException, VersionException, InvalidSerializedDataException, LockException, RepositoryException {
        if (isMount(parentAbsPath)) {
            this.mount.importXML(parentAbsPath, in, uuidBehavior);
        } else {
            this.jcr.importXML(parentAbsPath, in, uuidBehavior);
        }
    }

    @Override
    public void exportSystemView(String absPath, ContentHandler contentHandler, boolean skipBinary, boolean noRecurse) throws PathNotFoundException, SAXException, RepositoryException {
        if (isMount(absPath)) {
            this.mount.exportSystemView(absPath, contentHandler, skipBinary, noRecurse);
        } else {
            this.jcr.exportSystemView(absPath, contentHandler, skipBinary, noRecurse);
        }
    }

    @Override
    public void exportSystemView(String absPath, OutputStream out, boolean skipBinary, boolean noRecurse) throws IOException, PathNotFoundException, RepositoryException {
        if (isMount(absPath)) {
            this.mount.exportSystemView(absPath, out, skipBinary, noRecurse);
        } else {
            this.jcr.exportSystemView(absPath, out, skipBinary, noRecurse);
        }
    }

    @Override
    public void exportDocumentView(String absPath, ContentHandler contentHandler, boolean skipBinary, boolean noRecurse) throws PathNotFoundException, SAXException, RepositoryException {
        if (isMount(absPath)) {
            this.mount.exportDocumentView(absPath, contentHandler, skipBinary, noRecurse);
        } else {
            this.jcr.exportDocumentView(absPath, contentHandler, skipBinary, noRecurse);
        }
    }

    @Override
    public void exportDocumentView(String absPath, OutputStream out, boolean skipBinary, boolean noRecurse) throws IOException, PathNotFoundException, RepositoryException {
        if (isMount(absPath)) {
            this.mount.exportDocumentView(absPath, out, skipBinary, noRecurse);
        } else {
            this.jcr.exportDocumentView(absPath, out, skipBinary, noRecurse);
        }
    }

    @Override
    public void setNamespacePrefix(String prefix, String uri) throws NamespaceException, RepositoryException {
        this.jcr.setNamespacePrefix(prefix, uri);
        this.mount.setNamespacePrefix(prefix, uri);
    }

    @Override
    public String[] getNamespacePrefixes() throws RepositoryException {
        return this.jcr.getNamespacePrefixes();
    }

    @Override
    public String getNamespaceURI(String prefix) throws NamespaceException, RepositoryException {
        return this.jcr.getNamespaceURI(prefix);
    }

    @Override
    public String getNamespacePrefix(String uri) throws NamespaceException, RepositoryException {
        return this.jcr.getNamespacePrefix(uri);
    }

    @Override
    public void logout() {
        this.jcr.logout();
        this.mount.logout();
    }

    @Override
    public boolean isLive() {
        return this.jcr.isLive();
    }

    @Override
    public void addLockToken(String lt) {
        this.jcr.addLockToken(lt);
    }

    @Override
    public String[] getLockTokens() {
        return this.jcr.getLockTokens();
    }

    @Override
    public void removeLockToken(String lt) {
        this.jcr.removeLockToken(lt);
    }

    @Override
    public AccessControlManager getAccessControlManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        AccessControlManager manager = this.jcr.getAccessControlManager();
        return manager instanceof JackrabbitAccessControlManager ?
                new ProxyJackrabbitAccessControlManager(this, (JackrabbitAccessControlManager) manager, (JackrabbitAccessControlManager) this.mount.getAccessControlManager()) :
                new ProxyAccessControlManager<>(this, manager, this.mount.getAccessControlManager());
    }

    @Override
    public RetentionManager getRetentionManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        return this.jcr.getRetentionManager();
    }

    @Override
    public void move(String srcAbsPath, String destAbsPath) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        if (isMount(srcAbsPath) && isMount(destAbsPath)) {
            this.mount.move(srcAbsPath, destAbsPath);
        } else if (!isMount(srcAbsPath) && !isMount(destAbsPath)) {
            this.jcr.move(srcAbsPath, destAbsPath);
        } else {
            throw new IllegalStateException("Move between jcr and mount not supported");
        }
    }

    @Override
    public Workspace getWorkspace() {
        return new ProxyWorkspace(this, this.jcr.getWorkspace(), this.mount.getWorkspace());
    }

    public Node addNode(String parent, String path, String name) throws RepositoryException {
        if (isMount(path)) {
            return wrap(this.mount.getNode(parent).addNode(name));
        }
        if (isMountParent(path)) {
            this.mount.getNode(parent).addNode(name);
            if (sync == null) {
                sync = new HashSet<>();
            }
            sync.add(path);
        }
        return wrap(this.jcr.getNode(parent).addNode(name));
    }

    public Node addNode(String parent, String path, String name, String type) throws RepositoryException {
        if (isMount(path)) {
            return wrap(this.mount.getNode(parent).addNode(name, type));
        }
        if (isMountParent(path)) {
            this.mount.getNode(parent).addNode(name, type);
            if (sync == null) {
                sync = new HashSet<>();
            }
            sync.add(path);
        }
        return wrap(this.jcr.getNode(parent).addNode(name, type));
    }
}
