package com.smartapps.accel;

import java.util.ArrayList;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener,
		OnClickListener {
	private SensorManager sensorManager;
	private Button btnStart, btnStop, btnUpload;
	private boolean started = false;
	private ArrayList<AccelData> sensorData;
	private LinearLayout layout;
	private View mChart;
	private double min;
	private double max;
	private boolean cond1;
	private boolean cond2;
	private boolean cond3;
	final double UPTHRESS = 30.0;
	final double LOWTHRESS = 6.0;
	private int counter2;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		layout = (LinearLayout) findViewById(R.id.chart_container);
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		sensorData = new ArrayList<AccelData>();

		btnStart = (Button) findViewById(R.id.btnStart);
		btnStop = (Button) findViewById(R.id.btnStop);
		btnUpload = (Button) findViewById(R.id.btnUpload);
		btnStart.setOnClickListener(this);
		btnStop.setOnClickListener(this);
		btnUpload.setOnClickListener(this);
		btnStart.setEnabled(true);
		btnStop.setEnabled(false);
		max = 0;
		min = 100;
		cond1=false;
		cond2=false;
		counter2 = 0;
		
		
		if (sensorData == null || sensorData.size() == 0) {
			btnUpload.setEnabled(false);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (started == true) {
			sensorManager.unregisterListener(this);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (started) {
			double x = event.values[0];
			double y = event.values[1];
			double z = event.values[2];
			z = Math.sqrt((x*x)+(y*y)+(z*z));
			
			if (z<min)
				min=z;
			if (z>max)
				max=z;
			
			if(z > UPTHRESS) 
				cond1 = true;
			if(z < LOWTHRESS) 
				cond2 = true;
			if (9.6 < z  && z < 10.00) {
				  counter2 ++;
				  	if (counter2 >500)
					  cond3=true; 
					}else{
						counter2 = 0;
					}
			
			if (cond1 && cond2 && cond3)
			{
				Toast.makeText (getBaseContext (), "i am dropped!",
						Toast.LENGTH_LONG).show();
				cond1= false;
				cond2= false;
				cond3= false;
				counter2 = 0; 
			}
			
			
			x=0;
			y=0;
			long timestamp = System.currentTimeMillis();
			AccelData data = new AccelData(timestamp, x, y, z);
			sensorData.add(data);
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnStart:
			btnStart.setEnabled(false);
			btnStop.setEnabled(true);
			btnUpload.setEnabled(false);
			sensorData = new ArrayList<AccelData>();
			// save prev data if available
			started = true;
			Sensor accel = sensorManager
					.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			sensorManager.registerListener(this, accel,
					SensorManager.SENSOR_DELAY_FASTEST);
			
			long starttime = System.currentTimeMillis();
			String strStarttime = Long.toString(starttime);
			Toast.makeText (getBaseContext (), strStarttime,
					Toast.LENGTH_LONG).show();
			
			break;
			
		case R.id.btnStop:
			btnStart.setEnabled(true);
			btnStop.setEnabled(false);
			btnUpload.setEnabled(true);
			started = false;
			sensorManager.unregisterListener(this);
			layout.removeAllViews();
			openChart(); // show data in chart
				
			long stoptime = System.currentTimeMillis();	
			String strStoptime = Long.toString(stoptime);
			String result = "Min = " + min + " Max = " + max ;
			Toast.makeText (getBaseContext (), result,
					Toast.LENGTH_LONG).show();
			max = 0;
			min = 100;
			starttime = 0;
			stoptime = 0;
			break;
			
			
		case R.id.btnUpload:

			break;
		default:
			break;
		}

	}

	private void openChart() {
		if (sensorData != null || sensorData.size() > 0) {
			long t = sensorData.get(0).getTimestamp();
			XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

			XYSeries xSeries = new XYSeries("X");
			XYSeries ySeries = new XYSeries("Y");
			XYSeries zSeries = new XYSeries("Z");

			for (AccelData data : sensorData) {
				xSeries.add(data.getTimestamp() - t, data.getX());
				ySeries.add(data.getTimestamp() - t, data.getY());
				zSeries.add(data.getTimestamp() - t, data.getZ());
			}

			dataset.addSeries(xSeries);
			dataset.addSeries(ySeries);
			dataset.addSeries(zSeries);

			XYSeriesRenderer xRenderer = new XYSeriesRenderer();
			xRenderer.setColor(Color.RED);
			xRenderer.setPointStyle(PointStyle.CIRCLE);
			xRenderer.setFillPoints(true);
			xRenderer.setLineWidth(1);
			xRenderer.setDisplayChartValues(false);

			XYSeriesRenderer yRenderer = new XYSeriesRenderer();
			yRenderer.setColor(Color.GREEN);
			yRenderer.setPointStyle(PointStyle.CIRCLE);
			yRenderer.setFillPoints(true);
			yRenderer.setLineWidth(1);
			yRenderer.setDisplayChartValues(false);

			XYSeriesRenderer zRenderer = new XYSeriesRenderer();
			zRenderer.setColor(Color.BLUE);
			zRenderer.setPointStyle(PointStyle.CIRCLE);
			zRenderer.setFillPoints(true);
			zRenderer.setLineWidth(1);
			zRenderer.setDisplayChartValues(false);

			XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
			multiRenderer.setXLabels(0);
			multiRenderer.setLabelsColor(Color.RED);
			multiRenderer.setChartTitle("t vs (x,y,z)");
			multiRenderer.setXTitle("Sensor Data");
			multiRenderer.setYTitle("Values of Acceleration");
			multiRenderer.setZoomButtonsVisible(true);
			for (int i = 0; i < sensorData.size(); i++) {
				
				multiRenderer.addXTextLabel(i + 1, ""
						+ (sensorData.get(i).getTimestamp() - t));
			}
			for (int i = 0; i < 12; i++) {
				multiRenderer.addYTextLabel(i + 1, ""+i);
			}

			multiRenderer.addSeriesRenderer(xRenderer);
			multiRenderer.addSeriesRenderer(yRenderer);
			multiRenderer.addSeriesRenderer(zRenderer);

			// Getting a reference to LinearLayout of the MainActivity Layout
			

			// Creating a Line Chart
			mChart = ChartFactory.getLineChartView(getBaseContext(), dataset,
					multiRenderer);

			// Adding the Line Chart to the LinearLayout
			layout.addView(mChart);

		}
	}

}
