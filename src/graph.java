// CanCom
// Anders Helbo
// Morten Ambrosius
// References will be attached to all lines not written by us.
// [1] http://www.tutorialspoint.com/jfreechart/jfreechart_xy_chart.htm

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import org.jfree.chart.ChartPanel; 
import org.jfree.chart.JFreeChart; 
import org.jfree.data.xy.XYDataset; 
import org.jfree.data.xy.XYSeries; 
import org.jfree.ui.RefineryUtilities; 
import org.jfree.chart.plot.XYPlot; 
import org.jfree.chart.ChartFactory; 
import org.jfree.chart.plot.PlotOrientation; 
import org.jfree.data.xy.XYSeriesCollection; 
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

public class graph extends JFrame {
	
	private static final long serialVersionUID = 1636407518418488660L;
	private static String name1 = "graph1";
	private static String name2 = "graph2";
	private static String name3 = "graph3";
	private static String name4 = "graph4";
	
	static XYSeries plot1 = new XYSeries( name1 ); 
	static XYSeries plot2 = new XYSeries( name2 );
	static XYSeries plot3 = new XYSeries( name3 );
	static XYSeries plot4 = new XYSeries( name4 );
	
	static int startTime;
	
	public graph(){ // [1]
		super("Data plots"); // [1] made changes
		JFreeChart xylineChart = ChartFactory.createXYLineChart( // [1] made changes
			"" ,
			"Time [s]" ,
			"Value" ,
			createDataset() ,
			PlotOrientation.VERTICAL ,
			true , true , false);
         
		ChartPanel chartPanel = new ChartPanel( xylineChart ); // [1]
		chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) ); // [1]
		final XYPlot plot = xylineChart.getXYPlot( ); // [1]
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer( ); // [1]
		renderer.setSeriesPaint( 0 , Color.RED ); // [1]
		renderer.setSeriesPaint( 1 , Color.BLUE ); // [1]
		renderer.setSeriesPaint( 2 , Color.GREEN ); // [1]
		renderer.setSeriesPaint( 3 , Color.YELLOW );
      	plot.setRenderer( renderer );  // [1]
      	setContentPane( chartPanel );  // [1]
      	
      	// make the window close without killing the program
      	setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      	addWindowListener( new WindowAdapter() {
    	    public void windowClosing(WindowEvent e) {
    	        JFrame frame = (JFrame)e.getSource();
    	        dataset.removeAllSeries();
    	        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	    }
    	});
	}

	final static XYSeriesCollection dataset = new XYSeriesCollection( );  // [1]
	private XYDataset createDataset( ){   // [1]
		dataset.addSeries( plot1 );     // [1] made changes
		dataset.addSeries( plot2 );     // [1] made changes
		dataset.addSeries( plot3 );		// [1] made changes
		dataset.addSeries( plot4 );		
		return dataset;					// [1]
	}
	
	// adds a new data point to the right graph
	public static void addDataPoint(String data, double from, double to, int plot, int time){
		data = data.replaceAll("\\s","");
		int a = (int) (from * 2 - 2);
		int b = (int) (to * 2);
		data = data.substring(a, b);
		int value = Integer.decode("0x" + data);
		switch(plot){
		case 0: plot1.add(time, value);
				break;
		case 1: plot2.add(time, value);
				break;
		case 2: plot3.add(time, value);
				break;
		case 3: plot4.add(time, value);
				break;
		}	
	}
	
	// Sets up the selected plots
	public static void addplot(String ID[], double from[], double to[]) {
		plot1 = new XYSeries( ID[0] + ": " + from[0] + "-" + to[0] );
		if(ID[1] != null){	plot2 = new XYSeries( ID[1] + ": " + from[1] + "-" + to[1] ); }
		if(ID[2] != null){	plot3 = new XYSeries( ID[2] + ": " + from[2] + "-" + to[2] ); }
		if(ID[3] != null){	plot4 = new XYSeries( ID[3] + ": " + from[3] + "-" + to[3] ); }
		
		graph chart = new graph();
		chart.pack( );          
		RefineryUtilities.centerFrameOnScreen( chart );          
		chart.setVisible( true );
	}
}