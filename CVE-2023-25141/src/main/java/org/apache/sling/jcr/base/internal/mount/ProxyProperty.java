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

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;

import javax.jcr.Binary;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.VersionException;

public class ProxyProperty extends ProxyItem<Property> implements Property {
    public ProxyProperty(ProxySession mountSession, Property delegate) {
        super(mountSession, delegate);
    }

    public void setValue(Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        delegate.setValue(value);
    }

    public void setValue(Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        delegate.setValue(values);
    }

    public void setValue(String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        delegate.setValue(value);
    }

    public void setValue(String[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        delegate.setValue(values);
    }

    public void setValue(InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        delegate.setValue(value);
    }

    public void setValue(Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        delegate.setValue(value);
    }

    public void setValue(long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        delegate.setValue(value);
    }

    public void setValue(double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        delegate.setValue(value);
    }

    public void setValue(BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        delegate.setValue(value);
    }

    public void setValue(Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        delegate.setValue(value);
    }

    public void setValue(boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        delegate.setValue(value);
    }

    public void setValue(Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        delegate.setValue(value);
    }

    public Value getValue() throws ValueFormatException, RepositoryException {
        return delegate.getValue();
    }

    public Value[] getValues() throws ValueFormatException, RepositoryException {
        return delegate.getValues();
    }

    public String getString() throws ValueFormatException, RepositoryException {
        return delegate.getString();
    }

    public InputStream getStream() throws ValueFormatException, RepositoryException {
        return delegate.getStream();
    }

    public Binary getBinary() throws ValueFormatException, RepositoryException {
        return delegate.getBinary();
    }

    public long getLong() throws ValueFormatException, RepositoryException {
        return delegate.getLong();
    }

    public double getDouble() throws ValueFormatException, RepositoryException {
        return delegate.getDouble();
    }

    public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
        return delegate.getDecimal();
    }

    public Calendar getDate() throws ValueFormatException, RepositoryException {
        return delegate.getDate();
    }

    public boolean getBoolean() throws ValueFormatException, RepositoryException {
        return delegate.getBoolean();
    }

    public Node getNode() throws ItemNotFoundException, ValueFormatException, RepositoryException {
        return this.mountSession.getNode(delegate.getNode().getPath());
    }

    public Property getProperty() throws ItemNotFoundException, ValueFormatException, RepositoryException {
        return this.mountSession.getProperty(delegate.getProperty().getPath());
    }

    public long getLength() throws ValueFormatException, RepositoryException {
        return delegate.getLength();
    }

    public long[] getLengths() throws ValueFormatException, RepositoryException {
        return delegate.getLengths();
    }

    public PropertyDefinition getDefinition() throws RepositoryException {
        return delegate.getDefinition();
    }

    public int getType() throws RepositoryException {
        return delegate.getType();
    }

    public boolean isMultiple() throws RepositoryException {
        return delegate.isMultiple();
    }
}
