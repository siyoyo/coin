package gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import obj.Input;
import obj.Output;
import obj.Transaction;
import obj.Transaction.TransactionException;
import obj.TransactionReference;
import util.BlockExplorer;
import util.BlockExplorer.BlockExplorerException;
import util.Filename;
import util.UTXOExplorer;
import util.WalletExplorer;

@SuppressWarnings("serial")
public class InputsOutputs extends JFrame {
	
	private static InputsOutputs instance = null;
	private Transaction transaction;
	
	// Files & explorers
	public final static String DIR = "dat/";
	public final static String BLOCKCHAIN = "blockchain";
	public final static String UTXO = "utxolist";
	public final static String WALLET = "wallet";
	public final static String EXT = ".xml";
	private BlockExplorer blockExplorer;
	private UTXOExplorer utxoExplorer;
	private WalletExplorer walletExplorer;
	
	// Messaging
	private String hostname;
	private int port;
	private String msgType = "6";
	private String msgSeparator = "MSG";
	
	// GUI elements
	private final static Insets STANDARDINSETS = new Insets(5, 5, 5, 5);
	
	private JFrame small;
	private Container panel;
	
	private JLabel lInput, lInputAddress;
	private JTextField tInputAddress;
	
	private JLabel lOutput, lAmount, lOutputAddress;
	private JTextField tAmount, tOutputAddress;
	
	private JLabel lFee, lConfirmation;
	
	private final String[] inHeaders = {"Input address"};
	private final String[] outHeaders = {"Amount", "Output address"};
	private JTable tbInputs, tbOutputs;
	private JScrollPane scrInputs, scrOutputs;
	private String[][] currentInputs;
	private String[][] currentOutputs;
	
	private JButton bAddInput, bRemoveInput, bAddOutput, bRemoveOutput, bFee, bTransaction;
	private ButtonListener bListener;
	
	private JSeparator sHorizontal, sVertical;
	
	private GridBagConstraints 
		clInput, clInputAddress, ctInputAddress, cbAddInput, cbRemoveInput, cscrInputs,
		clOutput, clAmount, clOutputAddress, ctAmount, ctOutputAddress, cbAddOutput, cbRemoveOutput, cscrOutputs,
		clFee, cbFee, cbTransaction, clConfirmation, csHorizontal, csVertical;
	
	
	public static InputsOutputs getInstance(Transaction transaction, String hostname, int port) {
		
		if (instance == null) {
			
			instance = new InputsOutputs();
			instance.transaction = transaction;
			instance.hostname = hostname;
			instance.port = port;
			
			Filename blockchainFile = new Filename(DIR, BLOCKCHAIN, EXT, instance.hostname, String.valueOf(instance.port));
			Filename utxoFile = new Filename(DIR, UTXO, EXT, instance.hostname, String.valueOf(instance.port));
			Filename walletFile = new Filename(DIR, WALLET, EXT, instance.hostname, String.valueOf(instance.port));
			
			instance.initialiseExplorers(blockchainFile, utxoFile, walletFile);
		}
		return instance;
	}
	
	private InputsOutputs() {
		
		panel = this.getContentPane();
		panel.setLayout(new GridBagLayout());
		
		this.setMinimumSize(new Dimension(800, 400));
		this.getContentPane().setBackground(Color.WHITE);
		this.setTitle("New discoCoin Transaction");
		
		// Error pop-up window
		small = new JFrame();
		
		// ButtonListener
		bListener = new ButtonListener();
		
		// Input
		lInputAddress = new JLabel("Input address");
		tInputAddress = new JTextField("");
		lInput = new JLabel("Inputs");
		
		clInputAddress = new GridBagConstraints();
		ctInputAddress = new GridBagConstraints();
		clInput = new GridBagConstraints();
		
		bAddInput = new JButton("Add input");
		bAddInput.addActionListener(bListener);
		bAddInput.setActionCommand("AddInput");
		
		cbAddInput = new GridBagConstraints();
		
		bRemoveInput = new JButton("Remove input");
		bRemoveInput.addActionListener(bListener);
		bRemoveInput.setActionCommand("RemoveInput");
		
		cbRemoveInput = new GridBagConstraints();
		
		tbInputs = new JTable(5, 1);
		scrInputs = new JScrollPane(tbInputs);
		cscrInputs = new GridBagConstraints();
		
		// Output
		lAmount = new JLabel("Amount");
		lOutputAddress = new JLabel("Output address");
		lOutput = new JLabel("Outputs");
		
		clAmount = new GridBagConstraints();
		clOutputAddress = new GridBagConstraints();
		clOutput = new GridBagConstraints();
		
		tAmount = new JTextField("");
		tOutputAddress = new JTextField("");
		
		ctAmount = new GridBagConstraints();
		ctOutputAddress = new GridBagConstraints();
		
		bAddOutput = new JButton("Add output");
		bAddOutput.addActionListener(bListener);
		bAddOutput.setActionCommand("AddOutput");
		
		cbAddOutput = new GridBagConstraints();
		
		bRemoveOutput = new JButton("Remove output");
		bRemoveOutput.addActionListener(bListener);
		bRemoveOutput.setActionCommand("RemoveOutput");
		
		cbRemoveOutput = new GridBagConstraints();
		
		tbOutputs = new JTable(5, 1);
		scrOutputs = new JScrollPane(tbOutputs);
		cscrOutputs = new GridBagConstraints();
		
		// Transaction fee
		lFee = new JLabel("");
		
		clFee = new GridBagConstraints();
		
		bFee = new JButton("Calculate fee");
		bFee.addActionListener(bListener);
		bFee.setActionCommand("CalculateFee");
		
		cbFee = new GridBagConstraints();
		
		// Transaction confirmation
		bTransaction = new JButton("Confirm transaction");
		bTransaction.addActionListener(bListener);
		bTransaction.setActionCommand("ConfirmTransaction");
	
		cbTransaction = new GridBagConstraints();
		
		lConfirmation = new JLabel("");
		clConfirmation = new GridBagConstraints();
		
		// Separators
		sHorizontal = new JSeparator(JSeparator.HORIZONTAL);
		sVertical = new JSeparator(JSeparator.VERTICAL);
		
		csHorizontal = new GridBagConstraints();
		csVertical = new GridBagConstraints();
		
		draw();
		
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		
	}
	
	private class ButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case "AddInput":
				addInput();
				draw();
				break;
			case "RemoveInput":
				removeInput();
				draw();
				break;
			case "AddOutput":
				addOutput();
				draw();
				break;
			case "RemoveOutput":
				removeOutput();
				draw();
				break;
			case "CalculateFee":
				calculateFee();
				break;
			case "ConfirmTransaction":
				confirmTransaction();
				break;
			}
		}
	}
	
	/* -----------------------------------------------------------------
	 * 							Private methods
	 * -----------------------------------------------------------------
	 */
	
	private void addInput() {
		
		String inputAddress = tInputAddress.getText();
		
		try {
			TransactionReference reference = blockExplorer.getTransactionReferenceByAddress(inputAddress);
			RSAPrivateKey privateKey = walletExplorer.privateKey(inputAddress);
			
			if (inputAddress.compareTo("") != 0) {
				
				transaction.addInput(new Input(reference, privateKey));
				tInputAddress.setText("");
				refreshInputs();
				
			} else {
				JOptionPane.showMessageDialog(small, "Please enter an input address.");
			}
			
		} catch (BlockExplorerException e) {
			
			JOptionPane.showMessageDialog(small, "Invalid input address.\nPlease try again.");
			e.printStackTrace();
			
		}
		
	}
	
	private void removeInput() {
		
		int[] rows = tbInputs.getSelectedRows();
		
		for (int i = 0; i < rows.length; i++)
			transaction.removeInput(currentInputs[rows[i]][0], blockExplorer);
		
		refreshInputs();
	}
	
	private void addOutput() {
		
		String amount = tAmount.getText();
		String outputAddress = tOutputAddress.getText();
		RSAPublicKey publicKey = walletExplorer.publicKey(outputAddress);
		
		if (amount.compareTo("") != 0 & outputAddress.compareTo("") != 0) {
			
			if (validateAmount(amount)) {
				transaction.addOutput(new Output(publicKey, amount));
				tAmount.setText("");
				tOutputAddress.setText("");
				refreshOutputs();
			}
			
		} else {
			JOptionPane.showMessageDialog(small, "Please enter an amount and an output address.");
		}
		
	}
	
	private void removeOutput() {
		
		int[] rows = tbOutputs.getSelectedRows();
		
		for (int i = 0; i < rows.length; i++)
			transaction.removeOutput(currentOutputs[rows[i]][1]);
		
		refreshOutputs();
	}
	
	private void calculateFee() {
		
		String fee = "Fee not calculated";
		int txFee = transaction.calculateTransactionFee(blockExplorer);
		
		if (txFee < 0) fee = ("Negative transaction fee");
		else fee = "Fee is " + String.valueOf(txFee) + " discoCoins";
		
		lFee.setText(fee);
	}
	
	private void confirmTransaction() {
		
		try {
			
			transaction.finalise(blockExplorer, utxoExplorer);
			
			Socket socket = new Socket(hostname, port);
			PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			String message = transaction.toString();
			writer.println(msgType + msgSeparator + message);
			System.out.println(LocalDateTime.now() + " Sent: " + msgType + msgSeparator + message);
			
			switch (reader.readLine()) {
				case "OK": {
					
					lConfirmation.setText("Transaction sent");
					
					// Create a new transaction and clear tables and fee
					// TODO Migrate to "New Transaction" button
					transaction = new Transaction();
					refreshInputs();
					refreshOutputs();
					lFee.setText("");
					
					socket.close();
					
					break;
				}
				case "FAIL": {
					lConfirmation.setText("Transmission failed.  Re-transmitting...");
					writer.println(msgType + msgSeparator + message);
					break;
				}
			}
			
		} catch (TransactionException e) {
			JOptionPane.showMessageDialog(small, e);
			e.printStackTrace();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(small, e);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initialiseExplorers(Filename blockchainFilename, Filename utxoFilename, Filename walletFilename) {
		blockExplorer = new BlockExplorer(blockchainFilename);
		utxoExplorer = new UTXOExplorer(utxoFilename);
		walletExplorer = new WalletExplorer(walletFilename);
	}
	
	private boolean validateAmount(String amount) {
		
		if (amount.charAt(0) == '0') {
			JOptionPane.showMessageDialog(small, "Please enter a valid number.");
			return false;
		}
		
		if (Double.valueOf(amount) < 0) {
			JOptionPane.showMessageDialog(small, "Amount cannot be negative.");
			return false;
		}
		
		return true;
		
	}
	
	/* -----------------------------------------------------------------
	 * 							GUI rendering
	 * -----------------------------------------------------------------
	 */
	
	private void refreshInputs() {
		
		currentInputs = transaction.inputsAsTable(blockExplorer);
		
		tbInputs = new JTable(currentInputs, inHeaders) {
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		
		tbInputs.setMinimumSize(tbInputs.getPreferredSize());
		tbInputs.setAutoCreateRowSorter(true);
		
		scrInputs.setViewportView(tbInputs);
		scrInputs.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrInputs.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		this.validate();
		
	}
	
	private void refreshOutputs() {
		
		currentOutputs = transaction.outputsAsTable();
		
		tbOutputs = new JTable(currentOutputs, outHeaders) {
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		
		tbOutputs.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tbOutputs.getColumnModel().getColumn(0).setPreferredWidth(100);
		tbOutputs.getColumnModel().getColumn(1).setPreferredWidth(500);
		tbOutputs.setMinimumSize(tbOutputs.getPreferredSize());
		tbOutputs.setAutoCreateRowSorter(true);
		
		scrOutputs.setViewportView(tbOutputs);
		scrOutputs.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrOutputs.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		this.validate();
	}
	
	private void draw() {
	
		// GRIDBAG CONSTRAINTS
		
		// lInputAddress
		clInputAddress.gridx = 0;
		clInputAddress.gridy = 0;
		clInputAddress.gridwidth = 4;
		clInputAddress.gridheight = 1;
		clInputAddress.fill = GridBagConstraints.HORIZONTAL;
		clInputAddress.insets = STANDARDINSETS;
		
		// tInputAddress
		ctInputAddress.gridx = 0;
		ctInputAddress.gridy = 1;
		ctInputAddress.gridwidth = 4;
		ctInputAddress.gridheight = 1;
		ctInputAddress.fill = GridBagConstraints.HORIZONTAL;
		ctInputAddress.insets = STANDARDINSETS;
		
		// bAddInput
		cbAddInput.gridx = 2;
		cbAddInput.gridy = 3;
		cbAddInput.gridwidth = 2;
		cbAddInput.gridheight = 1;
		cbAddInput.anchor = GridBagConstraints.LINE_END;
		cbAddInput.insets = STANDARDINSETS;

		// lInput
		clInput.gridx = 0;
		clInput.gridy = 5;
		clInput.gridwidth = 4;
		clInput.gridheight = 1;
		clInput.fill = GridBagConstraints.HORIZONTAL;
		clInput.insets = STANDARDINSETS;
		
		// scrInputs
		cscrInputs.gridx = 0;
		cscrInputs.gridy = 6;
		cscrInputs.gridwidth = 4;
		cscrInputs.gridheight = 1;
		cscrInputs.fill = GridBagConstraints.HORIZONTAL;
		cscrInputs.fill = GridBagConstraints.VERTICAL;
		cscrInputs.insets = STANDARDINSETS;
		
		// bRemoveInput
		cbRemoveInput.gridx = 2;
		cbRemoveInput.gridy = 7;
		cbRemoveInput.gridwidth = 2;
		cbRemoveInput.gridheight = 1;
		cbRemoveInput.anchor = GridBagConstraints.LINE_END;
		cbRemoveInput.insets = STANDARDINSETS;
		
		// lAmount
		clAmount.gridx = 5;
		clAmount.gridy = 2;
		clAmount.gridwidth = 1;
		clAmount.gridheight = 1;
		clAmount.fill = GridBagConstraints.HORIZONTAL;
		clAmount.insets = STANDARDINSETS;
		
		// tAmount
		ctAmount.gridx = 6;
		ctAmount.gridy = 2;
		ctAmount.gridwidth = 3;
		ctAmount.gridheight = 1;
		ctAmount.fill = GridBagConstraints.HORIZONTAL;
		ctAmount.insets = STANDARDINSETS;
		
		// loutputAddress
		clOutputAddress.gridx = 5;
		clOutputAddress.gridy = 0;
		clOutputAddress.gridwidth = 4;
		clOutputAddress.gridheight = 1;
		clOutputAddress.fill = GridBagConstraints.HORIZONTAL;
		clOutputAddress.insets = STANDARDINSETS;
		
		// tOutputAddress
		ctOutputAddress.gridx = 5;
		ctOutputAddress.gridy = 1;
		ctOutputAddress.gridwidth = 4;
		ctOutputAddress.gridheight = 1;
		ctOutputAddress.fill = GridBagConstraints.HORIZONTAL;
		ctOutputAddress.insets = STANDARDINSETS;
				
		// bAddOutput
		cbAddOutput.gridx = 7;
		cbAddOutput.gridy = 3;
		cbAddOutput.gridwidth = 2;
		cbAddOutput.gridheight = 1;
		cbAddOutput.anchor = GridBagConstraints.LINE_END;
		cbAddOutput.insets = STANDARDINSETS;
		
		// lOutput
		clOutput.gridx = 5;
		clOutput.gridy = 5;
		clOutput.gridwidth = 4;
		clOutput.gridheight = 1;
		clOutput.fill = GridBagConstraints.HORIZONTAL;
		clOutput.insets = STANDARDINSETS;
		
		// scrOutputs
		cscrOutputs.gridx = 5;
		cscrOutputs.gridy = 6;
		cscrOutputs.gridwidth = 4;
		cscrOutputs.gridheight = 1;
		cscrOutputs.fill = GridBagConstraints.HORIZONTAL;
		cscrOutputs.fill = GridBagConstraints.VERTICAL;
		cscrOutputs.insets = STANDARDINSETS;
		
		// bRemoveOutput
		cbRemoveOutput.gridx = 7;
		cbRemoveOutput.gridy = 7;
		cbRemoveOutput.gridwidth = 2;
		cbRemoveOutput.gridheight = 1;
		cbRemoveOutput.anchor = GridBagConstraints.LINE_END;
		cbRemoveOutput.insets = STANDARDINSETS;
		
		// lFee
		clFee.gridx = 5;
		clFee.gridy = 9;
		clFee.gridwidth = 2;
		clFee.gridheight = 1;
		clFee.anchor = GridBagConstraints.LINE_START;
		clFee.insets = STANDARDINSETS;
		
		// bFee
		cbFee.gridx = 7;
		cbFee.gridy = 9;
		cbFee.gridwidth = 2;
		cbFee.gridheight = 1;
		cbFee.anchor = GridBagConstraints.LINE_END;
		cbFee.insets = STANDARDINSETS;
		
		// bTransaction
		cbTransaction.gridx = 7;
		cbTransaction.gridy = 10;
		cbTransaction.gridwidth = 2;
		cbTransaction.gridheight = 1;
		cbTransaction.anchor = GridBagConstraints.LINE_END;
		cbTransaction.insets = STANDARDINSETS;
		
		// lConfirmation
		clConfirmation.gridx = 5;
		clConfirmation.gridy = 10;
		clConfirmation.gridwidth = 2;
		clConfirmation.gridheight = 1;
		clConfirmation.anchor = GridBagConstraints.LINE_START;
		clConfirmation.insets = STANDARDINSETS;
		
		// sVertical
		csVertical.gridx = 4;
		csVertical.gridy = 0;
		csVertical.gridwidth = 1;
		csVertical.gridheight = 8;
		csVertical.fill = GridBagConstraints.VERTICAL;
		csVertical.insets = STANDARDINSETS;
		
		// sHorizontal
		csHorizontal.gridx = 0;
		csHorizontal.gridy = 8;
		csHorizontal.gridwidth = 9;
		csHorizontal.gridheight = 1;
		csHorizontal.fill = GridBagConstraints.HORIZONTAL;
		csHorizontal.insets = STANDARDINSETS;
		
		panel.add(lInputAddress, clInputAddress);
		panel.add(tInputAddress, ctInputAddress);
		panel.add(bAddInput, cbAddInput);
		
		panel.add(lInput, clInput);
		panel.add(scrInputs, cscrInputs);
		panel.add(bRemoveInput, cbRemoveInput);
		
		panel.add(lAmount, clAmount);
		panel.add(tAmount, ctAmount);
		panel.add(lOutputAddress, clOutputAddress);
		panel.add(tOutputAddress, ctOutputAddress);
		panel.add(bAddOutput, cbAddOutput);
		
		panel.add(lOutput, clOutput);
		panel.add(scrOutputs, cscrOutputs);
		panel.add(bRemoveOutput, cbRemoveOutput);
		
		panel.add(lFee, clFee);
		panel.add(bFee, cbFee);
		
		panel.add(bTransaction, cbTransaction);
		panel.add(lConfirmation, clConfirmation);
		
		panel.add(sHorizontal, csHorizontal);
		panel.add(sVertical, csVertical);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		
	}

}