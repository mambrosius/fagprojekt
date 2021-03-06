// CanGui
// Anders Helbo
// Morten Ambrosius

// Libraies for GUI
import java.awt.*; 				// for Dimension and layout managers 
import javax.swing.*;
import javax.swing.table.*;

public class CanGui {
	
	// Setup input console
	private static JTextField console = new JTextField(60);
	private static JTextArea consoleTextArea = new JTextArea(4,100);
	
	// Setup Table
	private static Object[] columnNames = new Object[]{"time", "id", "rtr", "ext", "length", "data", "error code"};
	private static DefaultTableModel logModel = new DefaultTableModel(columnNames, 0);
	private static JTable log = new JTable(logModel); 
	private static JTextArea sortedTextArea = new JTextArea();
	
	// Setup filter
	private static JPanel filterPanel = new JPanel(new GridLayout(1,5));
	private static JButton filterButton = new JButton("set filter");
	private static JLabel filterMinLabel = new JLabel("type min value:", SwingConstants.CENTER);
	private static JLabel filterMaxLabel = new JLabel("type max value:", SwingConstants.CENTER);
	private static JTextField filterMin = new JTextField();
	private static JTextField filterMax = new JTextField();

	public static void buildGui() {
		
		// Setup main frame
		JFrame frame = new JFrame();
		frame.setSize(1366, 768);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		log.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		Font font = new Font("COURIER", 0, 12);
		log.setFont(font);


		// Setup filter
		filterPanel.add(filterMinLabel);
		filterPanel.add(filterMin);
		filterPanel.add(filterMaxLabel);
		filterPanel.add(filterMax);
		filterPanel.add(filterButton);

		// TextArea behavior
		consoleTextArea.setEditable(false);
		consoleTextArea.setLineWrap(true);
		consoleTextArea.setWrapStyleWord(true);
		sortedTextArea.setEditable(false);
		sortedTextArea.setLineWrap(true);
		sortedTextArea.setWrapStyleWord(true);
		sortedTextArea.setFont(font);
		
		// Setup scroll panels
		JScrollPane leftScrollPane = new JScrollPane(sortedTextArea);
		JScrollPane rightScrollPane = new JScrollPane(log);
		rightScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		leftScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		// Piecing it together
		JPanel consoleSplit = new JPanel(new BorderLayout());
		consoleSplit.add(console, BorderLayout.NORTH);
		consoleSplit.add(consoleTextArea);

		JSplitPane logSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,rightScrollPane,consoleSplit);
		logSplit.setDividerLocation(597);

		JPanel rightSplit = new JPanel(new BorderLayout());
		rightSplit.add(filterPanel, BorderLayout.NORTH);
		rightSplit.add(logSplit);
		
		//JSplitPane rightsplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,filterPanel, logSplit);
		
		JSplitPane splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,leftScrollPane, rightSplit);
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

	public static JButton getFilterButton() {

		return filterButton;
	}

	public static JTextField getFilterMinField() {

		return filterMin;
	}

	public static JTextField getFilterMaxField() {

		return filterMax;
	}	
}