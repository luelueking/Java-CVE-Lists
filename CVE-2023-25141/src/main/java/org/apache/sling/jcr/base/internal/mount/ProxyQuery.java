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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.jcr.version.VersionException;

public class ProxyQuery extends ProxyWrapper<Query> implements Query {
    private final Query delegate2;

    public ProxyQuery(ProxySession<?> mountSession, Query delegate, Query delegate2) {
        super(mountSession, delegate);
        this.delegate2 = delegate2;
    }

    public QueryResult execute() throws InvalidQueryException, RepositoryException {
        final QueryResult result1 = delegate.execute();
        QueryResult result2temp = null;
        if (delegate2 != null) {
            result2temp = delegate2.execute();
        }
        final QueryResult result2 = result2temp;

        return this.mountSession.wrap(new QueryResult() {
            @Override
            public String[] getColumnNames() throws RepositoryException {
            	return result1.getColumnNames();
            }

            @Override
            public RowIterator getRows() throws RepositoryException {
            	final RowIterator i1 = result1.getRows();
            	RowIterator i2 = null;
            	if (result2 != null) {
                    i2 = result2.getRows();
                }
            	if ( i2 == null || !i2.hasNext() ) {
            		return i1;
            	}
            	if ( !i1.hasNext() ) {
            		return i2;
            	}
                final List<RowIterator> list = new ArrayList<>();
                list.add(i1);
                list.add(i2);
                @SuppressWarnings({ "unchecked", "rawtypes" })
				final Iterator<Row> iter = new ChainedIterator(list.iterator());
                return new RowIterator() {
					private volatile long position = 0;
					@Override
					public Object next() {
					    position++;
					    return iter.next();
					}

					@Override
					public boolean hasNext() {
						return iter.hasNext();
					}

					@Override
					public void skip(long skipNum) {
						while (skipNum-- > 0) {
						    next();
						}
					}

					@Override
					public long getSize() {
						return -1;
					}

					@Override
					public long getPosition() {
						return position;
					}

					@Override
					public Row nextRow() {
						position++;
						return iter.next();
					}
				};
            }

            @Override
            public NodeIterator getNodes() throws RepositoryException {
            	final NodeIterator i1 = result1.getNodes();
                NodeIterator i2 = null;
                if (result2 != null) {
                    i2 = result2.getNodes();
                }
            	if ( i2 == null || !i2.hasNext() ) {
            		return i1;
            	}
            	if ( !i1.hasNext() ) {
            		return i2;
            	}
                final List<NodeIterator> list = new ArrayList<>();
                list.add(i1);
                list.add(i2);
                @SuppressWarnings({ "unchecked", "rawtypes" })
				final Iterator<Node> iter = new ChainedIterator(list.iterator());
                return new NodeIterator() {
					private volatile long position = 0;
					@Override
					public Object next() {
					    position++;
					    return iter.next();
					}

					@Override
					public boolean hasNext() {
						return iter.hasNext();
					}

					@Override
					public void skip(long skipNum) {
						while (skipNum-- > 0) {
						    next();
						}
					}

					@Override
					public long getSize() {
						return -1;
					}

					@Override
					public long getPosition() {
						return position;
					}

					@Override
					public Node nextNode() {
					    position++;
					    return iter.next();
					}
				};
            }

            @Override
            public String[] getSelectorNames() throws RepositoryException {
                return result1.getSelectorNames();
            }
        });
    }

    public void setLimit(long limit) {
        delegate.setLimit(limit);
        if (delegate2 != null) {
            delegate2.setLimit(limit);
        }
    }

    public void setOffset(long offset) {
        delegate.setOffset(offset);
        if (delegate2 != null) {
            delegate2.setOffset(2);
        }
    }

    public String getStatement() {
        return delegate.getStatement();
    }

    public String getLanguage() {
        return delegate.getLanguage();
    }

    public String getStoredQueryPath() throws ItemNotFoundException, RepositoryException {
        try {
            return delegate.getStoredQueryPath();
        } catch (ItemNotFoundException ex) {
           try {
                if (delegate2 != null) {
                    return delegate2.getStoredQueryPath();
                } else {
                    return "";
                }
            } catch (ItemNotFoundException ignore) {
                throw ex;
            }
        }
    }

    public Node storeAsNode(String absPath) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, UnsupportedRepositoryOperationException, RepositoryException {
        return this.mountSession.wrap(this.mountSession.isMount(absPath) ? delegate2.storeAsNode(absPath) : delegate.storeAsNode(absPath));
    }

    public void bindValue(String varName, Value value) throws IllegalArgumentException, RepositoryException {
        delegate.bindValue(varName, value);
        if (delegate2 != null) {
            delegate2.bindValue(varName, value);
        }
    }

    public String[] getBindVariableNames() throws RepositoryException {
        return delegate.getBindVariableNames();
    }
}
