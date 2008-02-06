package magellan.plugin.memorywatch;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import magellan.client.Client;
import magellan.library.utils.MemoryManagment;
import magellan.library.utils.logging.Logger;

public class ShowMemory implements MemoryWatchAction, Runnable{
	private static final Logger log = Logger.getInstance(ConnectorPlugin.class);
	JDialog memoryDLG = null;
	JTextArea txtOutput = null;
	JScrollPane sp = null;
	Thread t = null;
	boolean threadMayRun = true;
	Client client = null;
	long lastFree = 0;
	
	public ShowMemory(Client client){
		this.client = client;
	}
	
	/**
	 * The Name to display in MenuList
	 * @return
	 */
	public String getName(){
		return "actual memory status";
	}
	
	/**
	 * Action to be called if MenuEntry is selected
	 * @param client
	 */
	public void activate(Client client){
		if (memoryDLG==null){
			init(client);
			threadMayRun=true;
			t = new Thread(this);
			t.start();
		} else {
			memoryDLG.dispose();
			memoryDLG = null;
			log.warn("Memory Watch deactivated");
			threadMayRun = false;
		}
	}
	
	
	private void init(Client client){
		
		memoryDLG = new JDialog(client,"Memory",false);
		memoryDLG.setSize(270,140);
		memoryDLG.setDefaultCloseOperation(
                WindowConstants.DISPOSE_ON_CLOSE);
		
		memoryDLG.getContentPane().add(getMainPanel());
		memoryDLG.getContentPane().validate();
		memoryDLG.setVisible(true);
		log.warn("Memory Watch activated");
		
	}
	
	private Container getMainPanel() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		txtOutput = new JTextArea("start");
		sp = new JScrollPane(txtOutput,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		sp.setPreferredSize(new Dimension(250,125));
		txtOutput.setEditable(false);
		mainPanel.add(sp,BorderLayout.CENTER);
		return mainPanel;
	}
	
	/**
	 * threadstart
	 *
	 */
	public void run(){
		Date today;
		String output;
		SimpleDateFormat formatter;

		
		if (memoryDLG==null){
			init(this.client);
		}
		while (this.threadMayRun && memoryDLG!=null && memoryDLG.isVisible()){
			formatter = new SimpleDateFormat("dd.MM.yyyy H:mm:ss");
			today = new Date();
			output = formatter.format(today);
			long isFree = MemoryManagment.r.freeMemory();
			output += "\n" + "free : " + NumberFormat.getNumberInstance().format(isFree) + " Byte";
			output += "\n" + "total: " + NumberFormat.getNumberInstance().format(MemoryManagment.r.totalMemory()) + " Byte";
			output += "\n" + "max  : " + NumberFormat.getNumberInstance().format(MemoryManagment.r.maxMemory()) + " Byte";
			output += "\n" + "dFree: " + NumberFormat.getNumberInstance().format(isFree - lastFree) + " Byte";
			lastFree = isFree;
			txtOutput.setText(output);
			try {
				t.sleep(1000);
			}
			catch (InterruptedException e){
				// do nothing
			}
		}
		log.warn("Memory Watch Thread exiting");
	}
	
}
