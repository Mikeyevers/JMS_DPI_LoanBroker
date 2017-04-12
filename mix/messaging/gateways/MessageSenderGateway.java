package messaging.gateways;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.Serializable;
import java.util.Properties;

/**
 * Created by Mike on 10/04/2017.
 */
public class MessageSenderGateway {
    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageProducer producer;
    private String channelName;

    public MessageSenderGateway(String channelName){
        this.channelName = channelName;
    }

    private ObjectMessage createObjectMessage(Object object, String correlationId) throws JMSException {
        ObjectMessage objectMessage = session.createObjectMessage();
        objectMessage.setObject((Serializable) object);
        objectMessage.setJMSCorrelationID(correlationId);

        return objectMessage;
    }

    public void send(Object object, String correlationId){
        try {
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
            props.put(("queue." + channelName), channelName);

            Context jndiContext = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            destination = (Destination) jndiContext.lookup(channelName);
            producer = session.createProducer(destination);

            ObjectMessage objectMessage = createObjectMessage(object, correlationId);
            producer.send(objectMessage);
        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }
    }
}
