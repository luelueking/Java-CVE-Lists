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

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.qom.And;
import javax.jcr.query.qom.BindVariableValue;
import javax.jcr.query.qom.ChildNode;
import javax.jcr.query.qom.ChildNodeJoinCondition;
import javax.jcr.query.qom.Column;
import javax.jcr.query.qom.Comparison;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DescendantNode;
import javax.jcr.query.qom.DescendantNodeJoinCondition;
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.EquiJoinCondition;
import javax.jcr.query.qom.FullTextSearch;
import javax.jcr.query.qom.FullTextSearchScore;
import javax.jcr.query.qom.Join;
import javax.jcr.query.qom.JoinCondition;
import javax.jcr.query.qom.Length;
import javax.jcr.query.qom.Literal;
import javax.jcr.query.qom.LowerCase;
import javax.jcr.query.qom.NodeLocalName;
import javax.jcr.query.qom.NodeName;
import javax.jcr.query.qom.Not;
import javax.jcr.query.qom.Or;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.PropertyExistence;
import javax.jcr.query.qom.PropertyValue;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.SameNode;
import javax.jcr.query.qom.SameNodeJoinCondition;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.Source;
import javax.jcr.query.qom.StaticOperand;
import javax.jcr.query.qom.UpperCase;

public class ProxyQueryObjectModelFactory extends ProxyWrapper<QueryObjectModelFactory> implements QueryObjectModelFactory {
    private final QueryObjectModelFactory delegate2;

    public ProxyQueryObjectModelFactory(ProxySession<?> mountSession, QueryObjectModelFactory delegate, QueryObjectModelFactory delegate2) {
        super(mountSession, delegate);
        this.delegate2 = delegate2;
    }

    public QueryObjectModel createQuery(Source source, Constraint constraint, Ordering[] orderings, Column[] columns) throws InvalidQueryException, RepositoryException {
        if (delegate2 != null) {
            return new ProxyQueryObjectModel(this.mountSession, delegate.createQuery(source, constraint, orderings, columns),
                    delegate2.createQuery(source, constraint, orderings, columns));
        } else {
            return new ProxyQueryObjectModel(this.mountSession, delegate.createQuery(source, constraint, orderings, columns),
                    null);
        }
    }

    public Selector selector(String nodeTypeName, String selectorName) throws InvalidQueryException, RepositoryException {
        return delegate.selector(nodeTypeName, selectorName);
    }

    public Join join(Source left, Source right, String joinType, JoinCondition joinCondition) throws InvalidQueryException, RepositoryException {
        return delegate.join(left, right, joinType, joinCondition);
    }

    public EquiJoinCondition equiJoinCondition(String selector1Name, String property1Name, String selector2Name, String property2Name) throws InvalidQueryException, RepositoryException {
        return delegate.equiJoinCondition(selector1Name, property1Name, selector2Name, property2Name);
    }

    public SameNodeJoinCondition sameNodeJoinCondition(String selector1Name, String selector2Name, String selector2Path) throws InvalidQueryException, RepositoryException {
        return delegate.sameNodeJoinCondition(selector1Name, selector2Name, selector2Path);
    }

    public ChildNodeJoinCondition childNodeJoinCondition(String childSelectorName, String parentSelectorName) throws InvalidQueryException, RepositoryException {
        return delegate.childNodeJoinCondition(childSelectorName, parentSelectorName);
    }

    public DescendantNodeJoinCondition descendantNodeJoinCondition(String descendantSelectorName, String ancestorSelectorName) throws InvalidQueryException, RepositoryException {
        return delegate.descendantNodeJoinCondition(descendantSelectorName, ancestorSelectorName);
    }

    public And and(Constraint constraint1, Constraint constraint2) throws InvalidQueryException, RepositoryException {
        return delegate.and(constraint1, constraint2);
    }

    public Or or(Constraint constraint1, Constraint constraint2) throws InvalidQueryException, RepositoryException {
        return delegate.or(constraint1, constraint2);
    }

    public Not not(Constraint constraint) throws InvalidQueryException, RepositoryException {
        return delegate.not(constraint);
    }

    public Comparison comparison(DynamicOperand operand1, String operator, StaticOperand operand2) throws InvalidQueryException, RepositoryException {
        return delegate.comparison(operand1, operator, operand2);
    }

    public PropertyExistence propertyExistence(String selectorName, String propertyName) throws InvalidQueryException, RepositoryException {
        return delegate.propertyExistence(selectorName, propertyName);
    }

    public FullTextSearch fullTextSearch(String selectorName, String propertyName, StaticOperand fullTextSearchExpression) throws InvalidQueryException, RepositoryException {
        return delegate.fullTextSearch(selectorName, propertyName, fullTextSearchExpression);
    }

    public SameNode sameNode(String selectorName, String path) throws InvalidQueryException, RepositoryException {
        return delegate.sameNode(selectorName, path);
    }

    public ChildNode childNode(String selectorName, String path) throws InvalidQueryException, RepositoryException {
        return delegate.childNode(selectorName, path);
    }

    public DescendantNode descendantNode(String selectorName, String path) throws InvalidQueryException, RepositoryException {
        return delegate.descendantNode(selectorName, path);
    }

    public PropertyValue propertyValue(String selectorName, String propertyName) throws InvalidQueryException, RepositoryException {
        return delegate.propertyValue(selectorName, propertyName);
    }

    public Length length(PropertyValue propertyValue) throws InvalidQueryException, RepositoryException {
        return delegate.length(propertyValue);
    }

    public NodeName nodeName(String selectorName) throws InvalidQueryException, RepositoryException {
        return delegate.nodeName(selectorName);
    }

    public NodeLocalName nodeLocalName(String selectorName) throws InvalidQueryException, RepositoryException {
        return delegate.nodeLocalName(selectorName);
    }

    public FullTextSearchScore fullTextSearchScore(String selectorName) throws InvalidQueryException, RepositoryException {
        return delegate.fullTextSearchScore(selectorName);
    }

    public LowerCase lowerCase(DynamicOperand operand) throws InvalidQueryException, RepositoryException {
        return delegate.lowerCase(operand);
    }

    public UpperCase upperCase(DynamicOperand operand) throws InvalidQueryException, RepositoryException {
        return delegate.upperCase(operand);
    }

    public BindVariableValue bindVariable(String bindVariableName) throws InvalidQueryException, RepositoryException {
        return delegate.bindVariable(bindVariableName);
    }

    public Literal literal(Value literalValue) throws InvalidQueryException, RepositoryException {
        return delegate.literal(literalValue);
    }

    public Ordering ascending(DynamicOperand operand) throws InvalidQueryException, RepositoryException {
        return delegate.ascending(operand);
    }

    public Ordering descending(DynamicOperand operand) throws InvalidQueryException, RepositoryException {
        return delegate.descending(operand);
    }

    public Column column(String selectorName, String propertyName, String columnName) throws InvalidQueryException, RepositoryException {
        return delegate.column(selectorName, propertyName, columnName);
    }
}
