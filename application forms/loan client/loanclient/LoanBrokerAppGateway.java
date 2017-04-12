package loanclient;

import loanclient.LoanClientFrame;
import messaging.gateways.MessageReceiverGateway;
import messaging.gateways.MessageSenderGateway;
import model.loan.LoanReply;
import model.loan.LoanRequest;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

/**
 * Created by Mike on 10/04/2017.
 */
public class LoanBrokerAppGateway {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;
    private LoanClientFrame loanClientFrame;

    public LoanBrokerAppGateway(LoanClientFrame loanClientFrame) {
        this.loanClientFrame = loanClientFrame;
        this.sender = new MessageSenderGateway("loanRequestQueue");
        this.receiver = new MessageReceiverGateway("loanReplyQueue");
        this.receiver.setListener(this::handleReplyMessage);
    }

    public void applyForLoan(LoanRequest request, String correlationId){
        sender.send(request, correlationId);
    }


    private void handleReplyMessage(Message message){
        if(message instanceof ObjectMessage){
            try{
                String correlationId = message.getJMSCorrelationID();

                Object object = ((ObjectMessage) message).getObject();
                LoanReply loanReply = (LoanReply) object;

                loanClientFrame.addReply(loanReply, correlationId);
                loanClientFrame.repaintReplyList();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
