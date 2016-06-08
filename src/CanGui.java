// CanGui
// Anders Helbo
// Morten Ambrosius

// Libraies for GUI
import java.awt.*; 				// for Dimension and layout managers 
import java.awt.event.*; 		// for action events
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.DefaultCaret; 

public class CanGui {

	// Setup input console
	private static JTextField console = new JTextField(60);
	private static JTextArea consoleTextArea = new JTextArea(4,100);
	// Setup Table
	private static Object[] columnNames = new Object[]{"time", "id", "rtr", "ext", "length", "data", "error code"};
	private static DefaultTableModel logModel = new DefaultTableModel(columnNames, 0);
	private static JTable log = new JTable(logModel); 
	private static JTextArea sortedTextArea = new JTextArea();
	
	public static void buildGui() {
		
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

	public static JTable getLog() {

		return log;
	}	

	public static JTextField getConsole() {

		return console;
	}

	public static JTextArea getConsoleTextArea() {

		return consoleTextArea;
	}

	public static JTextArea getSortedTextArea() {

		return sortedTextArea;
	}

	public static DefaultTableModel getLogModel() {

		return logModel;
	}
}