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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.qom.QueryObjectModelFactory;

public class ProxyQueryManager extends ProxyWrapper<QueryManager> implements QueryManager {
    private final QueryManager delegate2;

    public ProxyQueryManager(ProxySession<?> mountSession, QueryManager delegate, QueryManager delegate2) {
        super(mountSession, delegate);
        this.delegate2 = delegate2;
    }

    @Override
    public Query createQuery(String statement, String language) throws InvalidQueryException, RepositoryException {
        return new ProxyQuery(this.mountSession, delegate.createQuery(statement, language), delegate2.createQuery(statement, language));
    }

    @Override
    public QueryObjectModelFactory getQOMFactory() {
        return new ProxyQueryObjectModelFactory(this.mountSession, delegate.getQOMFactory(), delegate2.getQOMFactory());
    }

    @Override
    public Query getQuery(Node node) throws InvalidQueryException, RepositoryException {
        return this.mountSession.wrap(delegate.getQuery(this.mountSession.unwrap(node)));
    }

    @Override
    public String[] getSupportedQueryLanguages() throws RepositoryException {
        return delegate.getSupportedQueryLanguages();
    }
}
