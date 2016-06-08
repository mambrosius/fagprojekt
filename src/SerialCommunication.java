import java.io.*;
import gnu.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultCaret;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SerialCommunication implements SerialPortEventListener {
	
	SerialPort serialPort;
        /** The port we're normally going to use. */
	private static final String PORT_NAMES[] = { "COM1","COM2","COM3","COM4","COM5"};
	/**
	* A BufferedReader which will be fed by a InputStreamReader 
	* converting the bytes into characters 
	* making the displayed results codepage independent
	*/
	private BufferedReader input;
	/** The output stream to the port */
	private static OutputStream output;
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 115200;
	
	public void initSerial() {
		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		//First, Find an instance of serial port as set in PORT_NAMES.
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			for (String portName : PORT_NAMES) {
				if (currPortId.getName().equals(portName)) {
					portId = currPortId;
					break;
				}
			}
		}
		if (portId == null) {
			consoleTextArea.insert("Could not find COM port." + "\n",0);
			return;
		}

		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
					TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
			
			// Guess
			serialPort.disableReceiveTimeout();
			serialPort.enableReceiveThreshold(1);
			
			// open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
			consoleTextArea.insert("Serialcommunication started" + "\n", 0);
		} catch (Exception e) {
			consoleTextArea.insert(e.toString() + "\n",0);
		}
	}

	/**
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 */
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */
	private static ArrayList<CANcode> sortedCodes = new ArrayList<CANcode>();
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			
			try {   
				String inputLine=input.readLine();
				Calendar cal = Calendar.getInstance();
			    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
				String logString = sdf.format(cal.getTime()) + "_" + inputLine;
				String[] logrow = logString.split("_");
				logModel.insertRow(0,logrow);
				
				//Sort identifier
				boolean unusedIdentifier = true;
				int identifiervalue = Integer.decode("0x"+logrow[1].replaceAll("\\s",""));
				for(int i = 0; i < sortedCodes.size(); i++){
					String temp = sortedCodes.get(i).getIdentifier().replaceAll("\\s","");
					if(identifiervalue < Integer.decode("0x" + temp)){
						sortedCodes.add(i, new CANcode(Arrays.copyOfRange(logrow,1,logrow.length)));
						i = sortedCodes.size();
						unusedIdentifier = false;
					}
					else if(identifiervalue == Integer.decode("0x" + temp)){
						sortedCodes.get(i).addData(Arrays.copyOfRange(logrow, 4, logrow.length));
						i = sortedCodes.size();
						unusedIdentifier = false;
					}
				}
				if(unusedIdentifier){
					sortedCodes.add(new CANcode(Arrays.copyOfRange(logrow,1,logrow.length)));
				}
				
				StringBuilder sortedText = new StringBuilder("");
				for(int i = 0; i < sortedCodes.size(); i++){
					sortedText.append(sortedCodes.get(i).toString(""));
				}
				sortedTextArea.setText(sortedText.toString());
				
			} catch (Exception e) {
				consoleTextArea.insert("Read fail"+e.toString() + "\n",0);
			}
		}
	}
	
	// Setup input console
	private static JTextField console = new JTextField(60);
	private static JTextArea consoleTextArea = new JTextArea(4,100);
	// Setup Table
	private static Object[] columnNames = new Object[]{"time", "id", "rtr", "ext", "length", "data", "error code"};
	private static DefaultTableModel logModel = new DefaultTableModel(columnNames, 0);
	private static JTable log = new JTable(logModel); 
	private static JTextArea sortedTextArea = new JTextArea();
	public static void initGUI(){
		
		// Setup frame
		JFrame frame = new JFrame();
		frame.setSize(1366, 768);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		// TextArea behavior
		consoleTextArea.setEditable(false);
		consoleTextArea.setLineWrap(true);
		consoleTextArea.setWrapStyleWord(true);
		sortedTextArea.setEditable(false);
		sortedTextArea.setLineWrap(true);
		sortedTextArea.setWrapStyleWord(true);
		Font font = new Font("Monospaced", 0, 12);
		sortedTextArea.setFont(font);
		
		// Setup scroll panels
		JScrollPane leftScrollPane = new JScrollPane(sortedTextArea);
		JScrollPane rightScrollPane = new JScrollPane(log);
		
		
		rightScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		leftScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		// Piecing it together
		JSplitPane consoleSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,console,consoleTextArea);
		consoleSplit.setDividerLocation(20);
		JSplitPane rightsplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,rightScrollPane,consoleSplit);
		rightsplit.setDividerLocation(597);
		JSplitPane splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,leftScrollPane,rightsplit);
		splitpane.setDividerLocation(1360/2);
		frame.add(splitpane);
		frame.setVisible(true);
	}
	
	public static SerialCommunication main = new SerialCommunication();
	
	public static void consoleEvent(){
		console.addActionListener(new ActionListener(){
			//@Override
			public void actionPerformed(ActionEvent e){
				
				String[] consoleInput = console.getText().split("\\s+");
				console.setText("");
				
				if(consoleInput[0].equals("start")){
					main.initSerial();
				} 
				else if(consoleInput[0].equals("stop")){
					main.close();
					consoleTextArea.insert("Serialcommunication closed" + "\n", 0);
				}
				else if(consoleInput[0].equals("send")){
					StringBuilder send = new StringBuilder(consoleInput[1]);
					for(int i = 2; i < consoleInput.length; i++){
						send.append(consoleInput[i]);
					}
					try{
						output.write(send.toString().getBytes());
					} catch(IOException fejl){
						consoleTextArea.insert("Unable to send message to serialport" + "\n", 0);
					}
				}
				else if(consoleInput[0].equals("save")){
					try{
						// Save sorted list
						PrintWriter writer = new PrintWriter(consoleInput[1] + ".txt");
						for(int i = 0; i < sortedCodes.size(); i++){
							writer.print(sortedCodes.get(i).toString("save"));
						}
						writer.close();
						consoleTextArea.insert("Sorted list saved" + "\n", 0);
						
						// Save log
						PrintWriter logwriter = new PrintWriter(consoleInput[1] + "Log.txt");
						for(int i = 0; i < log.getRowCount(); i++){
							for(int j = 0; j < 7; j++){
								logwriter.print(log.getValueAt(i,j) + "_");
							}
							logwriter.println("");
						}
						logwriter.close();
						consoleTextArea.insert("Log saved" + "\n", 0);

					}
					catch(FileNotFoundException fejl){
						consoleTextArea.insert("Save failed" + "\n", 0);
					}
				}
				else if(consoleInput[0].equals("load")){
					consoleTextArea.insert("Function not written" + "\n", 0);
				}
				else if(consoleInput[0].equals("describe")){
					for(int i = 0; i < sortedCodes.size(); i++){
						if(consoleInput[1].equalsIgnoreCase(sortedCodes.get(i).getIdentifier().replaceAll("\\s",""))){
							StringBuilder description = new StringBuilder(consoleInput[2]);
							for(int j = 3; j < consoleInput.length; j++){
								description.append(" " + consoleInput[j]);
							}
							description.append("\n");
							sortedCodes.get(i).giveDescription(description.toString());
							consoleTextArea.insert(description.toString(),0);
							return;
						}
					}
				}
				else if(consoleInput[0].equals("hidedata")){
					for(int i = 0; i < sortedCodes.size(); i++){
						if(consoleInput[1].equalsIgnoreCase(sortedCodes.get(i).getIdentifier().replaceAll("\\s",""))){
							sortedCodes.get(i).hideData();
							consoleTextArea.insert(String.format("Hide data for: %s\n", consoleInput[1]),0);
							return;
						}
					}
				}
				else if(consoleInput[0].equals("showdata")){
					for(int i = 0; i < sortedCodes.size(); i++){
						if(consoleInput[1].equalsIgnoreCase(sortedCodes.get(i).getIdentifier().replaceAll("\\s",""))){
							sortedCodes.get(i).showData();
							consoleTextArea.insert(String.format("Show data for: %s\n", consoleInput[1]),0);
							return;
						}
					}
				}
				else if(consoleInput[0].equalsIgnoreCase("hideID")){
					for(int i = 0; i < sortedCodes.size(); i++){
						if(consoleInput[1].equalsIgnoreCase(sortedCodes.get(i).getIdentifier().replaceAll("\\s",""))){
							sortedCodes.get(i).hideIdentifier();
							consoleTextArea.insert(String.format("Hide ID: %s\n", consoleInput[1]),0);
							return;
						}
					}
				}
				else if(consoleInput[0].equalsIgnoreCase("showID")){
					for(int i = 0; i < sortedCodes.size(); i++){
						if(consoleInput[1].equalsIgnoreCase(sortedCodes.get(i).getIdentifier().replaceAll("\\s",""))){
							sortedCodes.get(i).showIdentifier();
							consoleTextArea.insert(String.format("Show ID: %s\n", consoleInput[1]),0);
							return;
						}
					}
				}
				else if(consoleInput[0].equalsIgnoreCase("showAll")){
					for(int i = 0; i < sortedCodes.size(); i++){
						sortedCodes.get(i).showData();
						sortedCodes.get(i).showIdentifier();
					}
				}
				else{
					consoleTextArea.insert("Command not recognized" + "\n", 0);
				}
			}
		});
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("new start");
		
		initGUI();
		consoleEvent();
	}
}