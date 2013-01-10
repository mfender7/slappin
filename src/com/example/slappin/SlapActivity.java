package com.example.slappin;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.hardware.*;

public class SlapActivity extends Activity implements SensorEventListener{//, MediaPlayer.OnCompletionListener{
	
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private float gravity[] = new float[3];
	private float linear_acceleration[] = new float[3];
	private float lastx, lasty, lastz;
	private boolean peakFound = false;
	private boolean start = true;
	MediaPlayer slap;
	private int count = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_slap);
		
		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this, accelerometer, sensorManager.SENSOR_DELAY_NORMAL);
		slap = MediaPlayer.create(SlapActivity.this, R.raw.slap);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_slap, menu);
		return true;
	}
	
	protected void onResume(){
		super.onResume();
	}
	
	protected void onPause(){
		super.onPause();
		slap.stop();
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		//REMOVE THE GRAVITY PART OF THIS
		
		//slap.stop();
		final float alpha = 0.1f;
		//really just copypasta code
		lastx = linear_acceleration[0];
		lasty = linear_acceleration[1];
		lastz = linear_acceleration[2];
		
		// Isolate the force of gravity with the low-pass filter.
		gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
		gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
		gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

		
		// Remove the gravity contribution with the high-pass filter.
		linear_acceleration[0] = event.values[0] - gravity[0];
		linear_acceleration[1] = event.values[1] - gravity[1];
		linear_acceleration[2] = event.values[2] - gravity[2];
		
		linear_acceleration[0] *= 100;
		linear_acceleration[1] *= 100;
		linear_acceleration[2] *= 100;
		
		
		//Check if we've reached the peak... or, by peak, I mean if we've gone from an increasing amount of force to a decreasing amount of 
		//force. Too lazy to change at night. However, need to set it so that I wait until the "slap" is finished being in motion before
		//playing the sound 
		if((Math.abs(lastz) > Math.abs(linear_acceleration[2])) && !(Math.abs(lastz) <= 5) && !peakFound && !start){
			//if(Math.abs(lastz) > 30){
				slap.start();
			//}
			Log.d("Math floor lastz", ""+ Math.abs(lastz));
			Log.d("Math floor current z", ""+ Math.abs(linear_acceleration[2]));
			peakFound = true;
			Log.d("Status", "Registering peak speed of the motion");
			Log.d("Slap speed z: ", lastz + " cm/s^");
			Log.d("Slap speed y: ", lasty + " cm/s^2");
			Log.d("Current y: ", linear_acceleration[1] + " cm/s^2");
			Log.d("Slap speed x: ", lastx + " cm/s^2");
			Log.d("Current x: ", linear_acceleration[0] + " cm/s^2");

			//Log.d("Did it work?...", "It works!!!!!! YUS!");
			//play sound accordingly...jk
			
			
		}
		else if(Math.abs(linear_acceleration[2]) <= 5 && peakFound){

			peakFound = false;	
		}
		
		if(start && count > 2)
			start = false;
		else
			count += 1;
		
		Log.d("Count", ""+count);
		Log.d("Last x: ", ""+lastx);
		Log.d("Last y: ", ""+lasty);
		Log.d("Last z: ", ""+lastz);
		//Log.d("Current x: ", ""+linear_acceleration[0] + " m/s^2");
		//Log.d("Current y: ", ""+linear_acceleration[1] + " m/s^2");
		//Log.d("Current z: ", ""+linear_acceleration[2] + " m/s^2");
		Log.d("Current x: ", ""+linear_acceleration[0] + " cm/s^2");
		Log.d("Current y: ", ""+linear_acceleration[1] + " cm/s^2");
		Log.d("Current z: ", ""+linear_acceleration[2] + " cm/s^2");
		
		
	}

}
