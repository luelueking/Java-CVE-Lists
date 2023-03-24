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
package org.apache.sling.jcr.base;

import java.util.Dictionary;
import java.util.Properties;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.sling.jcr.base.spi.RepositoryMount;
import org.apache.sling.testing.mock.jcr.MockJcr;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;

public class RepositoryMountTest
{
    @Rule
    public final SlingContext context = new SlingContext(ResourceResolverType.JCR_MOCK);

    private Repository rootRepository;
    private Repository mountRepository;
    private MockSlingRepositoryManager manager;

    @Before
    public void setup() throws RepositoryException
    {
        rootRepository = MockJcr.newRepository();
        mountRepository = MockJcr.newRepository();

        manager = new MockSlingRepositoryManager(rootRepository);
    }

    @Test
    public void testRepositoryMount() throws RepositoryException {
        Session rootSession = rootRepository.login();
        rootSession.getRootNode()
            .addNode("/root", NodeType.NT_UNSTRUCTURED)
            .addNode("test", NodeType.NT_UNSTRUCTURED)
            .setProperty("test", "root");

        Session mountSession = mountRepository.login();
        mountSession.getRootNode().addNode("/mount")
            .addNode("test", NodeType.NT_UNSTRUCTURED)
            .setProperty("test", "test");

        Assert.assertFalse(rootSession.nodeExists("/mount"));
        Assert.assertFalse(mountSession.nodeExists("/root"));

        rootSession.logout();
        mountSession.logout();

        Assert.assertTrue(manager.start(context.bundleContext(), "ws", false));

        Repository repository = context.getService(Repository.class);
        Assert.assertNotNull(repository);

        Session session = repository.login();
        Assert.assertFalse(session.nodeExists("/mount"));
        testExists(session, "/root", "root");
        session.logout();

        Properties props = new Properties();
        props.put(RepositoryMount.MOUNT_POINTS_KEY, "/mount");

        ServiceRegistration reg = context.bundleContext().registerService(RepositoryMount.class.getName(), mountRepository, (Dictionary) props);

        session = repository.login();

        testExists(session, "/root", "root");

        testExists(session, "/mount", "test");

        testTraversal(session, "/root", "root");
        testTraversal(session, "/mount", "test");

        session.logout();

        testCreate(repository, "/mount", "test2", "test3");

        testCreate(repository, "/root", "test2", "test3");

        reg.unregister();

        session = repository.login();

        Assert.assertFalse(session.nodeExists("/mount"));

        testExists(session, "/root", "root");

        session.logout();

        reg = context.bundleContext().registerService(RepositoryMount.class.getName(), mountRepository, (Dictionary) props);

        session = repository.login();

        testExists(session, "/root", "root");

        testExists(session, "/mount", "test");

        session.logout();
    }

    private void testExists(Session session, String path, String value) throws RepositoryException {
        Assert.assertTrue(session.nodeExists(path));
        Assert.assertTrue(session.nodeExists(path + "/test"));
        Assert.assertTrue(session.itemExists(path + "/test/test"));
        Assert.assertEquals(value, session.getProperty(path + "/test/test").getString());
    }

    private void testTraversal(Session session, String path, String value) throws RepositoryException {
        Assert.assertEquals(value, session.getRootNode().getNode(path).getNode("test").getProperty("test").getString());
    }

    private void testCreate(Repository repo, String start, String... path) throws RepositoryException {
        Session session = repo.login();
        Node current = session.getNode(start);
        String target = start;
        for (String next : path) {
            current = current.addNode(next, NodeType.NT_UNSTRUCTURED);
            target += "/" + next;
        }
        session.logout();
        session = repo.login();
        Assert.assertTrue(session.nodeExists(target));
        session.logout();
    }
}
