/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.superbiz;

import javax.ejb.Lock;
import javax.ejb.Singleton;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import java.lang.management.ManagementFactory;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static javax.ejb.LockType.READ;
import static javax.ejb.LockType.WRITE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Lock(READ)
@Singleton
@Path("/controller")
@Produces(APPLICATION_JSON)
public class Controller {

    private long startTime = 0;
    private long endTime = 0;
    private long startMessageCount;
    private AtomicBoolean running = new AtomicBoolean(false);
    private CountDownLatch latch = null;

    @GET
    @Path("start")
    @Lock(WRITE)
    public void start() throws Exception {
        startTime = System.nanoTime();
        endTime = 0;
        running.set(true);
        startMessageCount = getQueueDepth();
        latch = new CountDownLatch(1000000);

        startEndpoint();
    }

    @Path("stop")
    @Lock(WRITE)
    public void stop() throws Exception {
        stopEndpoint();
        endTime = System.nanoTime();
        running.set(false);
    }

    @GET
    public Stats stats() throws Exception {
        final long queueDepth = getQueueDepth();
        final long messagesProcessed = 1000000 - queueDepth;
        final long timeElapsed = running.get() ? (System.nanoTime() - startTime) : (endTime - startTime);
        long seconds = TimeUnit.SECONDS.convert(timeElapsed, TimeUnit.NANOSECONDS);

        final double rate = seconds == 0 ? 0 : ((double)messagesProcessed / (double)seconds);
        return new Stats(timeElapsed, messagesProcessed, queueDepth, getInstanceCount(), getInstanceLimit(), rate);
    }

    public void countDown() throws Exception {
        if (latch != null) {
            latch.countDown();

            if (latch.getCount() == 0) {
                stop();
            }
        }
    }

    private void startEndpoint() throws MalformedObjectNameException, MBeanException, InstanceNotFoundException, ReflectionException {
        final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        final ObjectInstance objectInstance = getEndpointControl(mBeanServer);
        mBeanServer.invoke(objectInstance.getObjectName(), "start", new Object[0], new String[0]);
    }

    private void stopEndpoint() throws MalformedObjectNameException, MBeanException, InstanceNotFoundException, ReflectionException {
        final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        final ObjectInstance objectInstance = getEndpointControl(mBeanServer);
        mBeanServer.invoke(objectInstance.getObjectName(), "start", new Object[0], new String[0]);
    }

    private ObjectInstance getEndpointControl(final MBeanServer mBeanServer) throws MalformedObjectNameException {
        final Set<ObjectInstance> objectInstances = mBeanServer.queryMBeans(new ObjectName("default:J2EEServer=openejb,J2EEApplication=<empty>,EJBModule=*,StatelessSessionBean=SimpleMessageProcessor,j2eeType=control,name=SimpleMessageProcessor"), null);

        if (objectInstances == null || objectInstances.isEmpty()) {
            throw new RuntimeException("Unable to find Endpoint control MDB");
        }

        return objectInstances.iterator().next();
    }

    private ObjectInstance getInstanceStats(final MBeanServer mBeanServer) throws MalformedObjectNameException {
        final Set<ObjectInstance> objectInstances = mBeanServer.queryMBeans(new ObjectName("openejb.management:J2EEServer=openejb,J2EEApplication=<empty>,EJBModule=*,MessageDrivenBean=SimpleMessageProcessor,j2eeType=Instances,name=SimpleMessageProcessor"), null);

        if (objectInstances == null || objectInstances.isEmpty()) {
            throw new RuntimeException("Unable to find Endpoint control MDB");
        }

        return objectInstances.iterator().next();
    }


    private long getQueueDepth() throws MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        final ObjectName objectName = new ObjectName("org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=test");
        final Set<ObjectInstance> objectInstances = mBeanServer.queryMBeans(objectName, null);

        if (objectInstances == null || objectInstances.isEmpty()) {
            throw new RuntimeException("Unable to find Queue MDB");
        }

        final long queueSize = (long) mBeanServer.getAttribute(objectName, "QueueSize");
        return queueSize;
    }

    private int getInstanceCount() throws MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
//        final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
//        final ObjectInstance objectInstance = getInstanceStats(mBeanServer);
//        return (int) mBeanServer.getAttribute(objectInstance.getObjectName(), "InstanceCount");
        return 0;
    }

    private int getInstanceLimit() throws MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
//        final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
//        final ObjectInstance objectInstance = getInstanceStats(mBeanServer);
//        return (int) mBeanServer.getAttribute(objectInstance.getObjectName(), "InstanceLimit");
        return 0;
    }
}
