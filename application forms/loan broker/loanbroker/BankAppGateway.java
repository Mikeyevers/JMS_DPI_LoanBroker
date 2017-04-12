package loanbroker;

import messaging.gateways.MessageReceiverGateway;
import messaging.gateways.MessageSenderGateway;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;
import model.loan.LoanReply;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

/**
 * Created by Mike on 10/04/2017.
 */
public class BankAppGateway {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;
    private LoanBrokerFrame loanBrokerFrame;

    public BankAppGateway(LoanBrokerFrame loanBrokerFrame){
        this.loanBrokerFrame = loanBrokerFrame;
        this.sender = new MessageSenderGateway("bankInterestRequestQueue");
        this.receiver = new MessageReceiverGateway("bankInterestReplyQueue");
        this.receiver.setListener(this::handleReplyMessage);
    }

    public void handleReplyMessage(Message message){
        if (message instanceof ObjectMessage) {
            try {
                String correlationId = message.getJMSCorrelationID();
                Object object = ((ObjectMessage) message).getObject();
                BankInterestReply bankInterestReply = (BankInterestReply) object;
                this.loanBrokerFrame.add(bankInterestReply, correlationId);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendBankInterestRequest(BankInterestRequest bankInterestRequest, String correlationId){
        sender.send(bankInterestRequest, correlationId);
    }

}
