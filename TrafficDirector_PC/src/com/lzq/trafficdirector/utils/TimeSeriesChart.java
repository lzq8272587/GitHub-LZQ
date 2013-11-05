package com.lzq.trafficdirector.utils;
import java.awt.Color;
import java.text.SimpleDateFormat;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Month;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;


public class TimeSeriesChart extends ApplicationFrame{
	TimeSeriesCollection dataset=null;//图标中的数据
	JFreeChart chart=null;//图表
	
	TimeSeries UncompressedData_Sendto_Terminal=null;
	TimeSeries CompressedData_Sendto_Terminal=null;
	
	TimeSeries UncompressedData_Sendto_Server=null;
	TimeSeries CompressedData_Sendto_Server=null;
	
	//图表相关文字信息
	Comparable<String> TimeSeriesName1;
	Comparable<String> TimeSeriesName2;
	String ChartTitle;
	
	public TimeSeriesChart(String title,Comparable<String> Name1,Comparable<String> Name2,String ChartTitle) {
		super(title);
		ChartPanel chartPanel = (ChartPanel) createDemoPanel();
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);
		this.TimeSeriesName1=Name1;
		this.TimeSeriesName2=Name2;
		this.ChartTitle=ChartTitle;
	}
	
	public  JPanel createDemoPanel() {
		generateDataet();
		generateChart(dataset);
		ChartPanel panel = new ChartPanel(chart);
		panel.setFillZoomRectangle(true);
		panel.setMouseWheelEnabled(true);
		return panel;
	}
	//填充好全局变量dataset中的数据
	private void generateDataet() {

		UncompressedData_Sendto_Terminal = new TimeSeries("CDF of uncompressed data");
		UncompressedData_Sendto_Terminal.add(new Second(), 0);

		CompressedData_Sendto_Terminal = new TimeSeries("CDF of compressed data");
		CompressedData_Sendto_Terminal.add(new Second(), 0);

		dataset = new TimeSeriesCollection();
		dataset.addSeries(UncompressedData_Sendto_Terminal);
		dataset.addSeries(CompressedData_Sendto_Terminal);


	}
	
	//将全局变量chart绘制好
	private void generateChart(XYDataset dataset) {

	   chart = ChartFactory.createTimeSeriesChart(ChartTitle, // title
							"Date", // x-axis label
							"Price Per Unit", // y-axis label
							dataset, // data
							true, // create legend?
							true, // generate tooltips?
							false // generate URLs?
							);

		chart.setBackgroundPaint(Color.white);

		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);
		

		XYItemRenderer r = plot.getRenderer();
		if (r instanceof XYLineAndShapeRenderer) {
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
			renderer.setBaseShapesVisible(true);
			renderer.setBaseShapesFilled(true);
			renderer.setDrawSeriesLineAsPath(true);
		}

		DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setDateFormatOverride(new SimpleDateFormat("ss-mm"));
	}
	
				
	public Plot getPlot()
	{
	    if(chart==null)
	    	return null;
	    return chart.getPlot();
	}
	
	public void refresh(double s1,double s2)
	{

		Second now=new Second();
		//System.err.println("refresh data: "+now+"s1: "+s1+"s2 "+s2);
		UncompressedData_Sendto_Terminal.addOrUpdate(now,s1);
		CompressedData_Sendto_Terminal.addOrUpdate(now,s2);
	}
	
	
}
