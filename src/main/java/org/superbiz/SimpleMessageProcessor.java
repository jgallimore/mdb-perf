package org.superbiz;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * This is a simple MDB. It is a simple no-op. We're simply interested in measuring the speed of the
 * bean pool, and how recent changes have made an impact.
 */

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "test"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "MdbActiveOnStartup", propertyValue = "false"),
        @ActivationConfigProperty(propertyName = "DeliveryActive", propertyValue = "false")
})
public class SimpleMessageProcessor implements MessageListener {

    @EJB
    private Controller controller;


    @Override
    public void onMessage(final Message message) {
        // no-op
        try {
            controller.countDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
