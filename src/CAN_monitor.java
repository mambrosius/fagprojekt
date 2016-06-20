// CanCom
// Anders Helbo
// Morten Ambrosius
// References will be attached to all lines not written by us.
// [1] from http://playground.arduino.cc/Interfacing/Java

import gnu.io.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;

public class CAN_monitor implements SerialPortEventListener { //[1] 

	public static void main(String[] args) throws Exception {
		System.out.println("new start");
		CanGui.buildGui();
		consoleEvent();
		filterEvent();
	}
	
	SerialPort serialPort;	//[1]
    // Ports to test when opening a connection.
	private static final String PORT_NAMES[] = { "COM1","COM2","COM3","COM4","COM5","COM6","COM7","/dev/tty.SLAB_USBtoUART","/dev/tty.SLAB_USBtoUART2"}; //[1]
	
	// A BufferedReader which will be fed by a InputStreamReader converting the bytes into characters //[1]
	// making the displayed results codepage independent //[1]
	private BufferedReader input;	//[1]
	private static OutputStream output;				// The output stream to the port //[1]
	private static final int TIME_OUT = 2000;		// Milliseconds to block while waiting for port open //[1]
	private static final int DATA_RATE = 115200; 	// Default bits per second for COM port. //[1]

	public void initSerial() { //[1]

		CommPortIdentifier portId = null; //[1]
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers(); //[1]

		// First, find an instance of serial port as set in PORT_NAMES. //[1]
		while (portEnum.hasMoreElements()) { //[1]
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement(); //[1]
			for (String portName : PORT_NAMES) { //[1]
				if (currPortId.getName().equals(portName)) { //[1]
					portId = currPortId; //[1]
					break; //[1]
				}
			}
		}
		if (portId == null) { //[1]
			CanGui.getConsoleTextArea().insert("Could not find COM port." + "\n",0);
			return; //[1]
		}

		try { //[1]
			// open serial port, and use class name for the appName. //[1]
			serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT); //[1]

			// set port parameters //[1]
			serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE); //[1]
			
			// Guess //[1] 
			serialPort.disableReceiveTimeout(); //[1]
			serialPort.enableReceiveThreshold(1); //[1]
			
			// open the streams //[1]
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream())); //[1]
			output = serialPort.getOutputStream(); //[1]

			// add event listeners //[1]
			serialPort.addEventListener(this); //[1]
			serialPort.notifyOnDataAvailable(true); //[1]
			CanGui.getConsoleTextArea().insert("Serialcommunication started" + "\n", 0); //[1]

		} catch (Exception e) { //[1]
			CanGui.getConsoleTextArea().insert(e.toString() + "\n",0); //[1]
		}
	}

	// This should be called when you stop using the port. This will prevent port locking on platforms like Linux.//[1]
	public synchronized void close() { //[1]
		if (serialPort != null) { //[1]
			serialPort.removeEventListener(); //[1]
			serialPort.close(); //[1]
		}
	}
	
	// setting a flag and a time for plotting.
	static int time = 0;
	static boolean flag[] = new boolean[4];
	static Runnable setFlag = new Runnable(){
		public void run(){
			for(int i = 0; i < 4; i++){
				flag[i] = true;
			}
			time++;
		}
	};
	
	private static ArrayList<CANcode> sortedCodes = new ArrayList<CANcode>(); // array of all identifiers and their sorted data fields
	// constants to be used when plotting
	private static String[] ID = new String[4];
	private static double[] from = new double[4];
	private static double[] to = new double[4];
	
	// Handle an event on the serial port. Read the data and print it. //[1]
	public synchronized void serialEvent(SerialPortEvent oEvent) { //[1]

		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) { //[1]
			
			try {
				// get data, add time stamp, split the data into array.
				String inputLine = input.readLine(); 
				Calendar cal = Calendar.getInstance(); 
			    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
				String logString = sdf.format(cal.getTime()) + "_" + inputLine;
				String[] logrow = logString.split("_");
				int identifiervalue = Integer.decode("0x"+logrow[1].replaceAll("\\s","")); 

				String[] data = logrow[5].split(" ");
				logrow[5] = "";
				
				// fill out missing 0's in data field
				for(int i = 0; i < data.length; i++){
					if(data[i].length() == 1){
						data[i] = "0" + data[i];
					}
					logrow[5] = logrow[5] + data[i];
					if(i < data.length - 1){
						logrow[5] = logrow[5] + " ";
					}
				}
				
				CanGui.getLogModel().insertRow(0,logrow);      // print log
				
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
				
				// Create text for the sorted text area.
				StringBuilder sortedText = new StringBuilder("");
				for(int i = 0; i < sortedCodes.size(); i++){
					sortedText.append(sortedCodes.get(i).toString(""));
				}
				CanGui.getSortedTextArea().setText(sortedText.toString());
				
				// Plot selected data
				for(int i = 0; i < 4; i++){
					if(logrow[1].equalsIgnoreCase(ID[i]) && flag[i]){
						graph.addDataPoint(logrow[5], from[i], to[i], i, time);
						flag[i] = false;
					}
				}
				
			} catch (Exception e) {
				CanGui.getConsoleTextArea().insert("Read fail"+e.toString() + "\n",0);
			}
		}
	}
	
	public static CAN_monitor main = new CAN_monitor();
	
	// Handle 
	public static void consoleEvent() {

		CanGui.getConsole().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				
				String[] consoleInput = CanGui.getConsole().getText().split("\\s+");
				CanGui.getConsole().setText("");
				
				if(consoleInput[0].equals("start")){
					main.initSerial();

				} else if(consoleInput[0].equals("stop")){
					main.close();
					CanGui.getConsoleTextArea().insert("Serialcommunication closed" + "\n", 0);

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

				} else if(consoleInput[0].equalsIgnoreCase("plot")){
					int n = consoleInput.length;
					ID[0] = consoleInput[1].toUpperCase();
					from[0] = Double.parseDouble(consoleInput[2]);
					to[0] = Double.parseDouble(consoleInput[3]);
					
					if( n > 4 ){
						ID[1] = consoleInput[4].toUpperCase();
						from[1] = Double.parseDouble(consoleInput[5]);
						to[1] = Double.parseDouble(consoleInput[6]);
					}
					if( n > 7){
						ID[2] = consoleInput[7].toUpperCase();
						from[2] = Double.parseDouble(consoleInput[8]);
						to[2] = Double.parseDouble(consoleInput[9]);
					}
					if( n > 10){
						ID[3] = consoleInput[10].toUpperCase();
						from[3] = Double.parseDouble(consoleInput[11]);
						to[3] = Double.parseDouble(consoleInput[12]);
					}
					graph.addplot(ID,from,to);
					// Setup timing
					ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
					time = 0;
					executor.scheduleAtFixedRate(setFlag, 0, 1, TimeUnit.SECONDS);
					
				} else {
					CanGui.getConsoleTextArea().insert("Command not recognized" + "\n", 0);
				}
			}
		});
	}

	// filter constants used by more than 1 method
	private static int minFilterValue = 0;
	private static int maxFilterValue = Integer.decode("0xFFF");

	// Method that creates a filter on incoming messages.
	public static void filterEvent() {
		//set listener on button
		CanGui.getFilterButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Collect text from interface
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
						else{
							sortedCodes.get(i).showIdentifier();
						}
					}
				    try{
						output.write((minFilterValue + " " + maxFilterValue).getBytes());
					} catch(IOException fejl){
						CanGui.getConsoleTextArea().insert("Unable to send message to serialport" + "\n", 0);
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