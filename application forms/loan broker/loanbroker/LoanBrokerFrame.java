package loanbroker;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import model.bank.*;
import model.loan.LoanReply;
import model.loan.LoanRequest;


public class LoanBrokerFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private DefaultListModel<JListLine> listModel = new DefaultListModel<JListLine>();
	private JList<JListLine> list;

    private static LoanClientAppGateway loanClientAppGateway;
    private static BankAppGateway bankAppGateway;
	
	public static void main(String[] args) {
		System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES","*");
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoanBrokerFrame frame = new LoanBrokerFrame();
                    loanClientAppGateway = new LoanClientAppGateway(frame);
                    bankAppGateway = new BankAppGateway(frame);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


	/**
	 * Create the frame.
	 */
	public LoanBrokerFrame() {
		setTitle("Loan Broker");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{46, 31, 86, 30, 89, 0};
		gbl_contentPane.rowHeights = new int[]{233, 23, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 7;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		contentPane.add(scrollPane, gbc_scrollPane);
		
		list = new JList<JListLine>(listModel);
		scrollPane.setViewportView(list);
	}
	
	 private JListLine getRequestReply(LoanRequest request){    
	     
	     for (int i = 0; i < listModel.getSize(); i++){
	    	 JListLine rr =listModel.get(i);
	    	 if (rr.getLoanRequest() == request){
	    		 return rr;
	    	 }
	     }
	     
	     return null;
	 }

	private JListLine getRequestReply(String correlationId){
		for (int i = 0; i < listModel.getSize(); i++){
			JListLine rr = listModel.get(i);
			if (rr.getCorrelationId().equals(correlationId)){
				return rr;
			}
		}

		return null;
	}
	
	public void add(LoanRequest loanRequest, String correlationId){
		JListLine rr = new JListLine(loanRequest);
		rr.setCorrelationId(correlationId);
		listModel.addElement(rr);

        BankInterestRequest bankInterestRequest = new BankInterestRequest(loanRequest.getAmount(), loanRequest.getTime());
        bankAppGateway.sendBankInterestRequest(bankInterestRequest, correlationId);
        this.add(loanRequest, bankInterestRequest);

	}

	public void add(LoanRequest loanRequest, BankInterestRequest bankRequest){
		JListLine rr = getRequestReply(loanRequest);
		if (rr!= null && bankRequest != null){
			rr.setBankRequest(bankRequest);
            list.repaint();
		}		
	}
	
	public void add(BankInterestReply bankInterestReply, String correlationId){
		JListLine rr = getRequestReply(correlationId);
		if (rr!= null && bankInterestReply != null){
			rr.setBankReply(bankInterestReply);
            list.repaint();
		}

        LoanReply loanReply = new LoanReply(bankInterestReply.getInterest(), bankInterestReply.getQuoteId());
        loanClientAppGateway.sendLoanReply(loanReply, correlationId);
	}
}
