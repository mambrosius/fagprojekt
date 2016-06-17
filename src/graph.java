import java.awt.Color;

import javax.swing.JFrame;

import java.awt.BasicStroke; 
import org.jfree.chart.ChartPanel; 
import org.jfree.chart.JFreeChart; 
import org.jfree.data.xy.XYDataset; 
import org.jfree.data.xy.XYSeries; 
import org.jfree.ui.ApplicationFrame; 
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
	
	public graph(){
		super("Data plots");
		JFreeChart xylineChart = ChartFactory.createXYLineChart(
			"" ,
			"Time [s]" ,
			"Value" ,
			createDataset() ,
			PlotOrientation.VERTICAL ,
			true , true , false);
         
		ChartPanel chartPanel = new ChartPanel( xylineChart );
		chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
		final XYPlot plot = xylineChart.getXYPlot( );
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer( );
		renderer.setSeriesPaint( 0 , Color.RED );
		renderer.setSeriesPaint( 1 , Color.BLUE );
		renderer.setSeriesPaint( 2 , Color.GREEN );
		renderer.setSeriesPaint( 3 , Color.YELLOW );
		renderer.setSeriesStroke( 0 , new BasicStroke( 2.0f ) );
      	renderer.setSeriesStroke( 1 , new BasicStroke( 2.0f ) );
      	renderer.setSeriesStroke( 2 , new BasicStroke( 2.0f ) );
      	renderer.setSeriesStroke( 3 , new BasicStroke( 2.0f ) );
      	plot.setRenderer( renderer ); 
      	setContentPane( chartPanel ); 
      	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	final static XYSeriesCollection dataset = new XYSeriesCollection( );
	private XYDataset createDataset( ){  
		dataset.addSeries( plot1 );          
		dataset.addSeries( plot2 );          
		dataset.addSeries( plot3 );
		dataset.addSeries( plot4 );
		return dataset;
	}
	
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