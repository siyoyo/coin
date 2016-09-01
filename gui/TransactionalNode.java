package gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import obj.Transaction;

@SuppressWarnings("serial")
public class TransactionalNode extends JFrame {

	private final static Insets STANDARDINSETS = new Insets(5, 5, 5, 5);
	
	private static TransactionalNode instance = null;
	
	private Transaction transaction;
	private String hostname;
	private int port;
	
	// GUI elements
	private Container panel;
	private JLabel lDisco;
	private JButton bTransaction;
	private GridBagConstraints clDisco, cbTransaction;
	private ButtonListener bListener;

	public static void main(String[] args) {
		if (args.length == 2) TransactionalNode.getInstance(args[0], Integer.valueOf(args[1]));
		else System.out.println("Arguments: [hostname] [port]");
	}
	
	public static TransactionalNode getInstance(String hostname, int port) {
		if (instance == null) instance = new TransactionalNode(hostname, port);
		return instance;
	}
	
	private TransactionalNode(String hostname, int port) {
		
		this.hostname = hostname;
		this.port = port;
		
		this.setMinimumSize(new Dimension(500, 500));
		this.getContentPane().setBackground(Color.WHITE);
		
		// lDisco
		ImageIcon original = new ImageIcon("/Users/yinyee/Documents/workspace/Cryptocurrency/src/gui/logo.jpg");
		Image unscaled = original.getImage();
		Image scaled = unscaled.getScaledInstance(400, 300, java.awt.Image.SCALE_SMOOTH);
		ImageIcon logo = new ImageIcon(scaled);
		lDisco = new JLabel(logo);
		
		// bTransaction
		bTransaction = new JButton("New Transaction");
		bListener = new ButtonListener();
		bTransaction.addActionListener(bListener);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		
		draw();
		
	}
	
	private class ButtonListener implements ActionListener {

		@Override // TODO
		public void actionPerformed(ActionEvent e) {
			transaction = new Transaction();
			InputsOutputs.getInstance(transaction, hostname, port);
			instance.dispose();
		}
	}
	
	/* -----------------------------------------------------------------
	 * 							Private methods
	 * -----------------------------------------------------------------
	 */
	
	private void draw() {
		
		panel = this.getContentPane();
		panel.setLayout(new GridBagLayout());
		
		// clDisco
		clDisco = new GridBagConstraints();
		clDisco.gridx = 0;
		clDisco.gridy = 0;
		clDisco.gridwidth = 3;
		clDisco.gridheight = 1;
		clDisco.insets = new Insets(25, 50, 5, 50);
		
		// cbTransaction
		cbTransaction = new GridBagConstraints();
		cbTransaction.gridx = 2;
		cbTransaction.gridy = 1;
		cbTransaction.gridwidth = 1;
		cbTransaction.gridheight = 1;
		cbTransaction.anchor = GridBagConstraints.CENTER;
		cbTransaction.insets = STANDARDINSETS;
		
		panel.add(lDisco, clDisco);
		panel.add(bTransaction, cbTransaction);
		
		this.pack();
	}

}