package loanbroker;

import messaging.gateways.MessageReceiverGateway;
import messaging.gateways.MessageSenderGateway;
import model.bank.BankInterestRequest;
import model.loan.LoanReply;
import model.loan.LoanRequest;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

/**
 * Created by Mike on 10/04/2017.
 */
public class LoanClientAppGateway {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;
    private LoanBrokerFrame loanBrokerFrame;

    public LoanClientAppGateway(LoanBrokerFrame loanBrokerFrame) {
        this.loanBrokerFrame = loanBrokerFrame;
        this.sender = new MessageSenderGateway("loanReplyQueue");
        this.receiver = new MessageReceiverGateway("loanRequestQueue");
        this.receiver.setListener(this::handleRequestMessage);
    }

    public void handleRequestMessage(Message message){
        if(message instanceof ObjectMessage){
            try{
                String correlationId = message.getJMSCorrelationID();
                Object object = ((ObjectMessage) message).getObject();
                LoanRequest loanRequest = (LoanRequest) object;

                this.loanBrokerFrame.add(loanRequest, correlationId);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendLoanReply(LoanReply loanReply, String correlationId){
        sender.send(loanReply, correlationId);
    }
}
