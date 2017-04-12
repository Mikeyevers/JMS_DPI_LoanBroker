package bank;

import messaging.gateways.MessageReceiverGateway;
import messaging.gateways.MessageSenderGateway;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

/**
 * Created by Mike on 12/04/2017.
 */
public class LoanBrokerAppGateway {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;
    private JMSBankFrame jmsBankFrame;

    public LoanBrokerAppGateway(JMSBankFrame jmsBankFrame) {
        this.jmsBankFrame = jmsBankFrame;
        this.sender = new MessageSenderGateway("bankInterestReplyQueue");
        this.receiver = new MessageReceiverGateway("bankInterestRequestQueue");
        this.receiver.setListener(this::handleRequestMessage);
    }

    public void sendInterestReply(BankInterestReply bankInterestReply, String correlationId){
        sender.send(bankInterestReply, correlationId);
    }

    public void handleRequestMessage(Message message){
        if(message instanceof ObjectMessage){
            try{
                String correlationId = message.getJMSCorrelationID();
                Object object = ((ObjectMessage) message).getObject();
                BankInterestRequest bankInterestRequest = (BankInterestRequest) object;
                jmsBankFrame.add(bankInterestRequest, correlationId);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
