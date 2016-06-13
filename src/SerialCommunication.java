// CanCom
// Anders Helbo
// Morten Ambrosius

import gnu.io.*;
import java.io.*;
import java.util.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.text.SimpleDateFormat;

public class SerialCommunication implements SerialPortEventListener {
	
	SerialPort serialPort;
    // The port we're normally going to use, for Windows and Mac
	private static final String PORT_NAMES[] = { "COM1","COM2","COM3","COM4","COM5","COM6","COM7","/dev/tty.SLAB_USBtoUART","/dev/tty.SLAB_USBtoUART2"};
	
	// A BufferedReader which will be fed by a InputStreamReader converting the bytes into characters 
	// making the displayed results codepage independent
	private BufferedReader input;
	private static OutputStream output;				// The output stream to the port
	private static final int TIME_OUT = 2000;		// Milliseconds to block while waiting for port open 
	private static final int DATA_RATE = 115200; 	// Default bits per second for COM port. 
	
	public static void main(String[] args) throws Exception {
		
		System.out.println("new start");

		CanGui.buildGui();
		consoleEvent();
		filterEvent();
	}

	public void initSerial() {

		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		// First, find an instance of serial port as set in PORT_NAMES.
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
			CanGui.getConsoleTextArea().insert("Could not find COM port." + "\n",0);
			return;
		}

		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			
			// Guess 
			serialPort.disableReceiveTimeout();
			serialPort.enableReceiveThreshold(1);
			
			// open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
			CanGui.getConsoleTextArea().insert("Serialcommunication started" + "\n", 0);

		} catch (Exception e) {
			CanGui.getConsoleTextArea().insert(e.toString() + "\n",0);
		}
	}

	// This should be called when you stop using the port. This will prevent port locking on platforms like Linux.
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	// Handle an event on the serial port. Read the data and print it.
	private static ArrayList<CANcode> sortedCodes = new ArrayList<CANcode>();
	
	public synchronized void serialEvent(SerialPortEvent oEvent) {

		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			
			try {   
				String inputLine = input.readLine();
				Calendar cal = Calendar.getInstance();
			    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
				String logString = sdf.format(cal.getTime()) + "_" + inputLine;
				String[] logrow = logString.split("_");
				int identifiervalue = Integer.decode("0x"+logrow[1].replaceAll("\\s",""));

				// check filter
				
				if(identifiervalue < minFilterValue || maxFilterValue < identifiervalue){
					return;
				}
				
				String[] data = logrow[5].split(" ");
				logrow[5] = "";
				
				for(int i = 0; i < data.length; i++){
					if(data[i].length() == 1){
						data[i] = "0" + data[i];
					}
					logrow[5] = logrow[5] + data[i];
					if(i < data.length - 1){
						logrow[5] = logrow[5] + " ";
					}
				}
				CanGui.getLogModel().insertRow(0,logrow);
				
				// Sort identifier
				boolean unusedIdentifier = true;
				
				for(int i = 0; i < sortedCodes.size(); i++){

					String temp = sortedCodes.get(i).getIdentifier().replaceAll("\\s","");
					
					if(identifiervalue < Integer.decode("0x" + temp)){
						sortedCodes.add(i, new CANcode(Arrays.copyOfRange(logrow,1,logrow.length)));
						i = sortedCodes.size();
						unusedIdentifier = false;

					} else if (identifiervalue == Integer.decode("0x" + temp)){
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
				
				CanGui.getSortedTextArea().setText(sortedText.toString());
				
			} catch (Exception e) {
				CanGui.getConsoleTextArea().insert("Read fail"+e.toString() + "\n",0);
			}
		}
	}
	
	public static SerialCommunication main = new SerialCommunication();
	
	public static void consoleEvent() {

		CanGui.getConsole().addActionListener(new ActionListener(){

			//@Override
			public void actionPerformed(ActionEvent e){
				
				String[] consoleInput = CanGui.getConsole().getText().split("\\s+");
				CanGui.getConsole().setText("");
				
				if(consoleInput[0].equals("start")){
					main.initSerial();

				} else if(consoleInput[0].equals("stop")){
					main.close();
					CanGui.getConsoleTextArea().insert("Serialcommunication closed" + "\n", 0);

				} else if(consoleInput[0].equals("send")){
					StringBuilder send = new StringBuilder(consoleInput[1]);
					
					for(int i = 2; i < consoleInput.length; i++){
						send.append(consoleInput[i]);
					}
					try{
						output.write(send.toString().getBytes());
					} catch(IOException fejl){
						CanGui.getConsoleTextArea().insert("Unable to send message to serialport" + "\n", 0);
					}

				} else if(consoleInput[0].equals("save")){

					try{
						// Save sorted list
						PrintWriter writer = new PrintWriter(consoleInput[1] + ".txt");
						for(int i = 0; i < sortedCodes.size(); i++){
							writer.print(sortedCodes.get(i).toString("save"));
						}
						writer.close();
						CanGui.getConsoleTextArea().insert("Sorted list saved" + "\n", 0);
						
						// Save log
						PrintWriter logwriter = new PrintWriter(consoleInput[1] + "Log.txt");
						for(int i = 0; i < CanGui.getLog().getRowCount(); i++){
							for(int j = 0; j < 6; j++){
								logwriter.print(CanGui.getLog().getValueAt(i,j) + " ");			
							}
							int length = Integer.decode("0x" + CanGui.getLog().getValueAt(i, 4));
							for(int k = length; k < 8; k++){
								logwriter.print("Na ");
							}
							logwriter.println(CanGui.getLog().getValueAt(i, CanGui.getLog().getColumnCount()-1));
						}
						logwriter.close();
						CanGui.getConsoleTextArea().insert("Log saved" + "\n", 0);

					}

					catch(FileNotFoundException fejl){
						CanGui.getConsoleTextArea().insert("Save failed" + "\n", 0);
					}

				} else if(consoleInput[0].equals("load")){
					CanGui.getConsoleTextArea().insert("Function not written" + "\n", 0);

				} else if(consoleInput[0].equals("describe")){
					for(int i = 0; i < sortedCodes.size(); i++){
						if(consoleInput[1].equalsIgnoreCase(sortedCodes.get(i).getIdentifier().replaceAll("\\s",""))){
							StringBuilder description = new StringBuilder(consoleInput[2]);
							for(int j = 3; j < consoleInput.length; j++){
								description.append(" " + consoleInput[j]);
							}
							description.append("\n");
							sortedCodes.get(i).giveDescription(description.toString());
							CanGui.getConsoleTextArea().insert(description.toString(),0);
							return;
						}
					}

				} else if(consoleInput[0].equals("hidedata")){
					for(int i = 0; i < sortedCodes.size(); i++){
						if(consoleInput[1].equalsIgnoreCase(sortedCodes.get(i).getIdentifier().replaceAll("\\s",""))){
							sortedCodes.get(i).hideData();
							CanGui.getConsoleTextArea().insert(String.format("Hide data for: %s\n", consoleInput[1]),0);
							return;
						}
					}

				} else if(consoleInput[0].equals("showdata")){
					for(int i = 0; i < sortedCodes.size(); i++){
						if(consoleInput[1].equalsIgnoreCase(sortedCodes.get(i).getIdentifier().replaceAll("\\s",""))){
							sortedCodes.get(i).showData();
							CanGui.getConsoleTextArea().insert(String.format("Show data for: %s\n", consoleInput[1]),0);
							return;
						}
					}

				} else if(consoleInput[0].equalsIgnoreCase("hideID")){
					for(int i = 0; i < sortedCodes.size(); i++){
						if(consoleInput[1].equalsIgnoreCase(sortedCodes.get(i).getIdentifier().replaceAll("\\s",""))){
							sortedCodes.get(i).hideIdentifier();
							CanGui.getConsoleTextArea().insert(String.format("Hide ID: %s\n", consoleInput[1]),0);
							return;
						}
					}

				} else if(consoleInput[0].equalsIgnoreCase("showID")){
					for(int i = 0; i < sortedCodes.size(); i++){
						if(consoleInput[1].equalsIgnoreCase(sortedCodes.get(i).getIdentifier().replaceAll("\\s",""))){
							sortedCodes.get(i).showIdentifier();
							CanGui.getConsoleTextArea().insert(String.format("Show ID: %s\n", consoleInput[1]),0);
							return;
						}
					}

				} else if(consoleInput[0].equalsIgnoreCase("showAll")){
					for(int i = 0; i < sortedCodes.size(); i++){
						sortedCodes.get(i).showData();
						sortedCodes.get(i).showIdentifier();
					}

				} else if(consoleInput[0].equalsIgnoreCase("HideAll")){
					for(int i = 0; i < sortedCodes.size(); i++){
						sortedCodes.get(i).hideIdentifier();
					}

				} else {
					CanGui.getConsoleTextArea().insert("Command not recognized" + "\n", 0);
				}
			}
		});
	}

	private static int minFilterValue = 0;
	private static int maxFilterValue = Integer.decode("0xFFF");

	public static void filterEvent() {

		CanGui.getFilterButton().addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String minFilterText = CanGui.getFilterMinField().getText();
				String maxFilterText = CanGui.getFilterMaxField().getText();
				CanGui.getConsoleTextArea().insert(String.format("Collecting identifiers between %s and %s",minFilterText,maxFilterText) + "\n", 0);


				try {
				    minFilterValue = Integer.decode("0x"+minFilterText.replaceAll("\\s",""));
				    maxFilterValue = Integer.decode("0x"+maxFilterText.replaceAll("\\s",""));
				    for(int i = 0; i < sortedCodes.size(); i++){
				    	int identifierValue = Integer.decode("0x" + sortedCodes.get(i).getIdentifier());
						if(identifierValue < minFilterValue || maxFilterValue < identifierValue){
							sortedCodes.get(i).hideIdentifier();
						}
					}
				}
				catch(NumberFormatException ex)
				{
					CanGui.getConsoleTextArea().insert("Type min and max values" + "\n", 0);
				}
			}

		});
	}
}