/**
 * Copyright (C) 2009, 2010 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.signnalstrengthrecorder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;

/**
 * Average temperature demo chart.
 */
public class AverageTemperatureChart extends AbstractDemoChart {
	/**
	 * Returns the chart name.
	 * 
	 * @return the chart name
	 */
	public String getName() {
		return "Average temperature";
	}

	/**
	 * Returns the chart description.
	 * 
	 * @return the chart description
	 */
	public String getDesc() {
		return "The average temperature in 4 Greek islands (line chart)";
	}

	/**
	 * Executes the chart demo.
	 * 
	 * @param context
	 *            the context
	 * @return the built intent
	 */
	public Intent execute(Context context) {
		// 设置曲线名称
		String[] titles = new String[] { "Signal Strength" };

		// // 设置X轴坐标
		// List<double[]> x = new ArrayList<double[]>();
		// for (int i = 0; i < titles.length; i++) {
		// x.add(new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 });
		// }

		// 从日志文件中读取信号强度
		File log = new File("/sdcard/phone_state_log.csv");
		String line = null;
		BufferedReader br = null;

		int size = 10000;

		StringBuffer SignalDataBuffer=new StringBuffer();
		String SignalData[] = null;
		
		double SignalStrength[] = new double[size];
		double x[]=new double[size];
		int index = 0;

		//获取记录文档中的所有数据
		try {
			br = new BufferedReader(new FileReader(log));
			while ((line = br.readLine()) != null) {
				SignalDataBuffer.append(line+"@");
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SignalData=SignalDataBuffer.toString().split("@");
		if(SignalData.length<size)
		{
			for(int i=0;i<SignalData.length;i++)
			{
				x[i]=i;
				SignalStrength[i]=Double.parseDouble(SignalData[i].split(",")[2]);
			}
		}
		else
		{
		//如果数据量大于能够显示的大小，则取最近的数据
			int delta=SignalData.length-size;
			for(int i=0;i<size;i++)
			{
				x[i]=i;
				SignalStrength[i]=Double.parseDouble(SignalData[i+delta].split(",")[2]);
			}
		}
		
		
		
		// 设置X轴坐标
		List<double[]> X_value = new ArrayList<double[]>();
		X_value.add(x);

		// 设置Y轴坐标
		List<double[]> values = new ArrayList<double[]>();
		values.add(SignalStrength);

		// // 设置Y轴坐标
		// List<double[]> values = new ArrayList<double[]>();
		// values.add(new double[] { 12.3, 12.5, 13.8, 16.8, 20.4, 24.4, 26.4,
		// 26.1, 23.6, 20.3, 17.2, 13.9 });
		// values.add(new double[] { 10, 10, 12, 15, 20, 24, 26, 26, 23, 18, 14,
		// 11 });
		// values.add(new double[] { 5, 5.3, 8, 12, 17, 22, 24.2, 24, 19, 15, 9,
		// 6 });
		// values.add(new double[] { 9, 10, 11, 15, 19, 23, 26, 25, 22, 18, 13,
		// 10 });

		// 设置线条颜色
		int[] colors = new int[] { Color.BLUE};

		// 设置线条风格
		PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE};
//				PointStyle.DIAMOND, PointStyle.TRIANGLE, PointStyle.SQUARE };

		XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
		int length = renderer.getSeriesRendererCount();
		for (int i = 0; i < length; i++) {
			((XYSeriesRenderer) renderer.getSeriesRendererAt(i))
					.setFillPoints(true);
		}

		// 设置图标说明文字,设置X轴Y轴范围
		setChartSettings(renderer, "Time Varing Signal Strength", "Time",
				"Signal Strength", 0, x.length, -110, -50, Color.LTGRAY,
				Color.LTGRAY);

		renderer.setXLabels(12);
		renderer.setYLabels(10);
		renderer.setShowGrid(true);
		renderer.setXLabelsAlign(Align.RIGHT);
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setZoomButtonsVisible(true);
		renderer.setPanLimits(new double[] { -10, 20, -10, 40 });
		renderer.setZoomLimits(new double[] { -10, 20, -10, 40 });

		Intent intent = ChartFactory.getLineChartIntent(context,
				buildDataset(titles, X_value, values), renderer,
				"Time Varing Signal Strength");
		return intent;
	}

}
