import java.awt.Color;
import java.util.Scanner;
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

public class graph extends ApplicationFrame {
	//
	private static int graphNumber = 0; 
	
	private static String name1 = "graph1";
	private static String name2 = "graph2";
	private static String name3 = "graph3";
	private static String name4 = "graph4";
	
	static XYSeries plot1 = new XYSeries( name1 ); 
	static XYSeries plot2 = new XYSeries( name2 );
	static XYSeries plot3 = new XYSeries( name3 );
	static XYSeries plot4 = new XYSeries( name4 );
	
	public graph(){
		super("Data plots");
		JFreeChart xylineChart = ChartFactory.createXYLineChart(
			"" ,
			"Value" ,
			"Minutes after start" ,
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
   }
   
   final static XYSeriesCollection dataset = new XYSeriesCollection( );
   private XYDataset createDataset( ){  
      dataset.addSeries( plot1 );          
      dataset.addSeries( plot2 );          
      dataset.addSeries( plot3 );
      dataset.addSeries( plot4 );
      return dataset;
   }
   /*
   private static void addDatapoint(double t, double y){
	   plot1.add(t,y);
   }
   */
   private static double getTime(int row){
	   String startTime = (String) CanGui.getLog().getValueAt(CanGui.getLog().getRowCount()-1,0);
	   String startSplit[] = startTime.split(":");
	   double startminute = (Integer.decode("0x" + startSplit[0]) * 60) + Integer.decode("0x" + startSplit[1]) + (Integer.decode("0x" + startSplit[2]) / 60);
	   
	   String dataTime = (String) CanGui.getLog().getValueAt(row,0);
	   String dataSplit[] = dataTime.split(":");
	   double dataminute = (Integer.decode("0x" + dataSplit[0]) * 60) + Integer.decode("0x" + dataSplit[1]) + (Integer.decode("0x" + dataSplit[2]) / 60.0);
	   
	   return dataminute - startminute;
   }
   
   private static void addDataPoint(String ID, double from, double to, XYSeries plot){
	   for(int i = CanGui.getLog().getRowCount() - 1; i >= 0 ; i--){
		   if(( (String) CanGui.getLog().getValueAt(i,1)).equalsIgnoreCase(ID)){
			   String data = ((String) CanGui.getLog().getValueAt(i,5)).replaceAll("\\s","");
			   int a = (int) (from * 2 - 2);
			   int b = (int) (to * 2);
			   data = data.substring(a, b);
			   int value = Integer.decode("0x" + data);
			   plot.add(getTime(i), value);
		   }
	   }
   }
   
   public static void addplot(String ID, double from, double to) {
	   
	   if(graphNumber == 0){
		   name1 = ID;
		   plot1 = new XYSeries( name1 ); 
		   addDataPoint(ID,from,to,plot1);
	
	   } else if(graphNumber == 1){
		   name2 = ID;
		   plot2 = new XYSeries( name2 );
		   addDataPoint(ID,from,to,plot2);
	   } else if(graphNumber == 2){
		   name3 = ID;
		   plot3 = new XYSeries( name3 );
		   //addDataPoint(ID,from,to,plot3);
	   } else if(graphNumber == 3){
		   name4 = ID;
		   plot4 = new XYSeries( name4 );
		   //addDataPoint(ID,from,to,plot4);
	   }
	   
	   graph chart = new graph();
	   chart.pack( );          
	   RefineryUtilities.centerFrameOnScreen( chart );          
	   chart.setVisible( true );
	   graphNumber++;
	   if(graphNumber == 4){
		   graphNumber = 0;
	   }
	   System.out.println(graphNumber);
   }
}