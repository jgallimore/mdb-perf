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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import static javax.ejb.LockType.READ;
import static javax.ejb.LockType.WRITE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Lock(READ)
@Singleton
@Path("/controller")
@Produces(APPLICATION_JSON)
public class Controller {

    private final Logger logger = Logger.getLogger(Controller.class.getName());

    private AtomicReference<TestState> stateRef = null;

    @GET
    @Path("start")
    @Lock(WRITE)
    public Long start() throws Exception {
        long startMessageCount = getQueueDepth();
        logger.info("Starting new test with " + startMessageCount + " in the queue.");

        stateRef = new AtomicReference<>(TestState.start(startMessageCount));
        startEndpoint();

        return startMessageCount;
    }

    @Path("stop")
    @Lock(WRITE)
    public void stop() throws Exception {
        if (stateRef == null) {
            throw new IllegalStateException("Test has not been started");
        }

        final TestState ts = stateRef.get();
        if (ts.complete() && ts.getCompletionTime() == 0) {
            logState(stateRef.updateAndGet(TestState::stop));
        }

        stopEndpoint();
    }

    private void logState(final TestState state) {
        final long timeElapsed = state.getTimeElapsed();
        long seconds = TimeUnit.SECONDS.convert(timeElapsed, TimeUnit.NANOSECONDS);
        final double rate = seconds == 0 ? 0 : ((double) state.getMessagesProcessed() / (double) seconds);

        logger.info("Test completed. " + state.getMessagesProcessed() + " at a rate of " + rate + " messages/sec.");
    }

    @GET
    public Stats stats() {
        final TestState ts = stateRef.get();

        final long timeElapsed = ts.getTimeElapsed();
        long seconds = TimeUnit.SECONDS.convert(timeElapsed, TimeUnit.NANOSECONDS);

        final double rate = seconds == 0 ? 0 : ((double) ts.getMessagesProcessed() / (double) seconds);
        return new Stats(
                timeElapsed,
                ts.getMessagesProcessed(),
                ts.getTotalMessages() - ts.getMessagesProcessed(),
                rate
        );
    }

    // READ lock
    public void messageProcessed() throws Exception {
        if (stateRef != null) {
            // we could call stop() from here, but this method should not
            // have any side-effects, and may be called multiple times
            // by updateAndGet().
            final TestState updated = this.stateRef.updateAndGet(TestState::incrementMessagesProcessed);

            if (updated.complete() && updated.getCompletionTime() == 0) {
                logState(this.stateRef.updateAndGet(TestState::stop));
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

    private long getQueueDepth() throws MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        final ObjectName objectName = new ObjectName("org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=test");
        final Set<ObjectInstance> objectInstances = mBeanServer.queryMBeans(objectName, null);

        if (objectInstances == null || objectInstances.isEmpty()) {
            throw new RuntimeException("Unable to find Queue MDB");
        }

        return (long) mBeanServer.getAttribute(objectName, "QueueSize");
    }
}
