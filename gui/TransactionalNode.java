package gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import obj.Transaction;

@SuppressWarnings("serial")
public class TransactionalNode extends JFrame {

	private final static Insets STANDARDINSETS = new Insets(5, 5, 5, 5);
	
	private Socket socket;
	private Transaction transaction;
	
	// GUI elements
	private JFrame small;
	private Container panel;
	private JLabel lDisco;
	private JButton bTransaction;
	private GridBagConstraints clDisco, cbTransaction;
	private ButtonListener bListener;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				
				if (args.length != 2) {
					System.err.println("Please provide two arguments: hostname and port of the NetworkNode to connect to");
					System.exit(1);
				}
				
				try {
					new TransactionalNode(args);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public TransactionalNode(String[] args) {
		
		small = new JFrame();
		
		try {
			socket = new Socket(args[0], Integer.valueOf(args[1]));
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(small, "Invalid port number");
			e.printStackTrace();
			System.exit(0);
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(small, "Invalid host name");
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(small, "Unknown error.\nPlease try re-launching\nthe application.");
			e.printStackTrace();
			System.exit(0);
		}
		
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

		@Override
		public void actionPerformed(ActionEvent e) {
			transaction = new Transaction();
			InputsOutputs.getInstance(socket, transaction);
			JFrame frame = (JFrame) e.getSource(); 
			frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
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