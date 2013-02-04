package com.rawr.slappin;

/* Save for later use
    <!--  GCM Permission -->
    <permission android:name="com.rawr.slappin.permission.C2D_MESSAGE" android:protectionLevel="signature" />
    <uses-permission android:name="com.rawr.slappin.permission.C2D_MESSAGE" /> 
    <!-- App receives GCM messages. -->
	<uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />
	<!-- GCM connects to Google Services. -->
	<uses-permission android:name="android.permission.INTERNET" /> 
	<!-- GCM requires a Google account. -->
	<uses-permission android:name="android.permission.GET_ACCOUNTS" />
	<!-- Keeps the processor from sleeping when a message is received. -->
	<uses-permission android:name="android.permission.WAKE_LOCK" />
			    
		<receiver android:name="com.google.android.gcm.GCMBroadcastReceiver" android:permission="com.google.android.c2dm.permission.SEND" >
			<intent-filter>
			    <action android:name="com.google.android.c2dm.intent.RECEIVE" />
			    <action android:name="com.google.android.c2dm.intent.REGISTRATION" />
			    <category android:name="com.rawr.slappin" />
		  	</intent-filter>
		</receiver>
		<service android:name=".GCMIntentService" />  
 */

import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
//import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
//import android.widget.Toast;
import android.widget.ArrayAdapter;
//import com.google.android.gcm.GCMBaseIntentService;
import com.rawr.slappin.R;

public class SlapActivity extends Activity implements SensorEventListener {

	private SensorManager sensorManager;
	private Sensor accelerometer;
	private float gravity[] = new float[3];
	private float linear_acceleration[] = new float[3];
	private float lastx, lasty, lastz;
	private boolean peakFound = false;
	private boolean chill = false; // used to stop sounds when looking at instructions
	MediaPlayer slap, bonk, currentPlayer;
	private Spinner spinner;
	String[] list = { "Slap", "Bong" };

	@SuppressLint("ShowToast")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		showInstructions();
		setContentView(R.layout.activity_slap);
		// sensors to detect amount of force on phone
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_FASTEST);
		slap = MediaPlayer.create(SlapActivity.this, R.raw.slap);
		bonk = MediaPlayer.create(SlapActivity.this, R.raw.bong);

		// SPINERRRRR... aka dat drop-down.
		spinner = (Spinner) findViewById(R.id.sounds);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, list);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				int item = spinner.getSelectedItemPosition();
				if (item == 0)
					currentPlayer = slap;
				else if (item == 1)
					currentPlayer = bonk;
			}

			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.how_do:
			showInstructions();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	private void showInstructions() {
		chill = true;
		new AlertDialog.Builder(this)
				.setMessage(
						"Step 1. Find someone you like\nStep 2. Swat phone at them\nStep 3. ???\nStep 4. Profit!")
				.setTitle("The what-how!")
				.setNeutralButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								chill = false;
							}
						}).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_slap, menu);
		return true;
	}

	protected void onResume() {
		super.onResume();
		chill = false;
		currentPlayer = (spinner.getSelectedItemPosition() == 0) ? slap : bonk;
	}

	protected void onPause() {
		super.onPause();
		chill = true;
		currentPlayer.stop();
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// REMOVE THE GRAVITY
		if (!chill) { //none of this slapping nonsense when they're looking at instructions
			final float alpha = 0.1f;
			// really just copypasta code
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

			//Convert meters per second squared to centimeters per second squared
			linear_acceleration[0] *= 100;
			linear_acceleration[1] *= 100;
			linear_acceleration[2] *= 100;

			// Check if we've reached the peak... or, by peak, I mean if we've
			// gone from an increasing amount of force to a decreasing amount of
			// force. Too lazy to change at night. However, need to set it so
			// that I wait until the "slap" is finished being in motion before
			// playing the sound
			if ((Math.abs(lastz) > Math.abs(linear_acceleration[2]))
					&& !(Math.abs(lastz) <= 5) && !peakFound) {

				if (Math.abs(lastz) > 90) {
					currentPlayer.setVolume(1f, 1f);
					Log.d("MediaPlayer Volume", "Full blast");
					currentPlayer.start();
					// toast.show();
				} else if (Math.abs(lastz) > 30) {
					currentPlayer.setVolume(0.5f, 0.5f);
					Log.d("MediaPlayer Volume", "Half blast");
					currentPlayer.start();
					// toast.show();
				} else if (Math.abs(lastz) > 5)
					currentPlayer.setVolume(0, 0);

				//whenever I get a tablet to play with...
				// if(isTabletDevice())
				// hardSlap.start();
				// else
				// slap.start();

				Log.d("Math floor lastz", "" + Math.abs(lastz));
				Log.d("Math floor current z",
						"" + Math.abs(linear_acceleration[2]));
				peakFound = true;
				Log.d("Status", "Registering peak speed of the motion");
				Log.d("Slap speed z: ", lastz + " cm/s^");
				Log.d("Slap speed y: ", lasty + " cm/s^2");
				Log.d("Current y: ", linear_acceleration[1] + " cm/s^2");
				Log.d("Slap speed x: ", lastx + " cm/s^2");
				Log.d("Current x: ", linear_acceleration[0] + " cm/s^2");

			} else if (Math.abs(linear_acceleration[2]) <= 5 && peakFound) {

				peakFound = false;
			}

			// Log.d("Count", ""+count);
			Log.d("Last x: ", "" + lastx);
			Log.d("Last y: ", "" + lasty);
			Log.d("Last z: ", "" + lastz);
			Log.d("Current x: ", "" + linear_acceleration[0] + " cm/s^2");
			Log.d("Current y: ", "" + linear_acceleration[1] + " cm/s^2");
			Log.d("Current z: ", "" + linear_acceleration[2] + " cm/s^2");
		}

	}

	//Future use whenever I actually end up playing with a tablet
	private boolean isTabletDevice() {
		if (android.os.Build.VERSION.SDK_INT >= 11) { // honeycomb
			// test screen size, use reflection because isLayoutSizeAtLeast is
			// only available since 11
			Configuration con = getResources().getConfiguration();
			try {
				Method mIsLayoutSizeAtLeast = con.getClass().getMethod(
						"isLayoutSizeAtLeast", int.class);
				Boolean r = (Boolean) mIsLayoutSizeAtLeast.invoke(con,
						0x00000004); // Configuration.SCREENLAYOUT_SIZE_XLARGE
				return r;
			} catch (Exception x) {
				x.printStackTrace();
				return false;
			}
		}
		return false;
	}

}
