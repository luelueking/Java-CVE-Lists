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

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ChainedIterator<T> implements Iterator<T> {

    private T nextElement;

    private final Iterator<Iterator<T>> iterators;

    private Iterator<T> currentIterator;

    public ChainedIterator(final Iterator<Iterator<T>> iterators) {
        this.iterators = iterators;
    }

    protected T seek() {
        while (true) {
            if (currentIterator == null) {
                if (!iterators.hasNext()) {
                    return null;
                }
                currentIterator = iterators.next();
                continue;
            }
            if (currentIterator.hasNext()) {
                return currentIterator.next();
            } else {
                currentIterator = null;
            }
        }
    }

    @Override
    public boolean hasNext() {
        if (nextElement == null) {
            nextElement = seek();
        }
        return nextElement != null;
    }

    @Override
    public T next() {
        if (nextElement == null && !hasNext()) {
            throw new NoSuchElementException();
        }
        final T result = nextElement;
        nextElement = null;
        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
