package com.example.cyclapp_1_1;

import java.text.DecimalFormat;
import java.util.List;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity  implements LocationListener, OnClickListener {
	private LocationManager mgr;
	private TextView output;
	private String best;
	// Define human readable names
	private static final String[] A = { "invalid", "n/a", "fine", "coarse" };
	private static final String[] P = { "invalid", "n/a", "low", "medium", "high" };
	private static final String[] S = { "out of service", "temporarily unavailable", "available" };

	public double OldLat = 0;
	public double OldLon = 0;
	public double NewLat = 0;
	public double NewLon = 0;
	public double TimeOfOldLocation = 0;
	public double TimeOfNewLocation = 0;
	public double TimeOfFirstLocation = 0;
	public double TripDistance = 0;
	public Boolean startStop = false;
	int time = 0;
	int hours = 0;
	int minutes = 0;
	int seconds = 0;
	protected Handler taskHandler = new Handler();
	protected Boolean isComplete = false;
	protected Boolean isPaused = false;




	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main2);

		mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
		output = (TextView) findViewById(R.id.output);


		//log("Location providers:");
		//dumpProviders();

		Criteria criteria = new Criteria();
		best = mgr.getBestProvider(criteria, true);
		//log("\nBest provider is: " + best);

		//log("\nLocations (starting with the last known):");
		Location location = mgr.getLastKnownLocation(best);
		mgr.requestLocationUpdates(best, 1000, 1, this.myLocationListener);
		NewLat = location.getLatitude();
		NewLon = location.getLongitude();
		TimeOfNewLocation = System.currentTimeMillis();
		TimeOfFirstLocation = TimeOfNewLocation;
		updateLocationLabel("Lat = " + NewLat + "\n" +"Lon = " + NewLon);
		location.setSpeed((float) roundTwoDecimals(calculateSpeed()));
		updateSpeedLabel(Float.toString(location.getSpeed()));
		dumpLocation(location);

		// Set button listeners
		View findLocationButton = findViewById(R.id.map_button);
		findLocationButton.setOnClickListener(MainActivity.this);
		View startStopButton = findViewById(R.id.start_stop_button);
		startStopButton.setOnClickListener(MainActivity.this);
		View pauseButton = findViewById(R.id.pause_button);
		pauseButton.setOnClickListener(MainActivity.this);


	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.map_button:
			Intent i = new Intent(this, Map.class);
			startActivity(i);
			break;
		case R.id.start_stop_button:
			Button startBut = new Button(this);
			startBut = (Button)findViewById(R.id.start_stop_button);
			if (startBut.getText().equals("Stop")) {
				// User presses Stop
				isComplete = true;
				startStop = false;
				startBut.setText("Start");
			} else {
				// User presses Start
				setTimer();
				startStop = true;
				startBut.setText("Stop");
			}
			break;
		case R.id.pause_button:
			Button pauseBut = new Button(this);
			pauseBut = (Button)findViewById(R.id.pause_button);
			if (startStop) {
				if (pauseBut.getText().equals("Pause")) {
					isPaused = true;
					pauseBut.setText("Resume");
				} else {
					isPaused = false;
					pauseBut.setText("Pause");
				}
			}
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Start updates (doc recommends delay >= 60000 ms)
		mgr.requestLocationUpdates(best, 1000, 1, this.myLocationListener);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Stop updates to save power while app paused
		mgr.removeUpdates(this);
	}

	LocationListener myLocationListener = new LocationListener() {

		public void onLocationChanged(Location location) {
			OldLat = NewLat;
			OldLon = NewLon;
			TimeOfOldLocation = TimeOfNewLocation;
			NewLat = location.getLatitude();
			NewLon = location.getLongitude();
			TimeOfNewLocation = System.currentTimeMillis();
			updateLocationLabel("Lat = " + NewLat + "\n" +"Lon = " + NewLon);
			location.setSpeed((float) roundTwoDecimals(calculateSpeed()));
			updateSpeedLabel(Float.toString(location.getSpeed()));
			if (startStop) {
				TripDistance += calculateDistance();
				updateDistanceLabel(Double.toString(roundTwoDecimals(TripDistance)));
			}
			dumpLocation(location);
			System.out.println("LOCATION CHANGED!!!!!!!!!!!!");
		}

		public void onProviderDisabled(String provider) {
			log("\nProvider disabled: " + provider);
			System.out.println("\nProvider disabled: " + provider);
		}

		public void onProviderEnabled(String provider) {
			log("\nProvider enabled: " + provider);
			System.out.println("\nProvider enabled: " + provider);
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			log("\nProvider status changed: " + provider + ", status=" + S[status] + ", extras=" + extras);
			System.out.println("\nProvider status changed: " + provider + ", status=" + S[status] + ", extras=" + extras);
		}

	};

	// Write a string to the output window
	private void log(String string) {
		output.append(string + "\n");
	}

	// Write information from all location providers
	private void dumpProviders() {
		List<String> providers = mgr.getAllProviders();
		for (String provider : providers) {
			dumpProvider(provider);
		}
	}

	// Write information from a single location provider
	private void dumpProvider(String provider) {
		LocationProvider info = mgr.getProvider(provider);
		StringBuilder builder = new StringBuilder();
		builder.append("LocationProvider[")
		.append("name=")
		.append(info.getName())
		.append(",enabled=")
		.append(mgr.isProviderEnabled(provider))
		.append(",getAccuracy=")
		.append(A[info.getAccuracy() + 1])
		.append(",getPowerRequirement=")
		.append(P[info.getPowerRequirement() + 1])
		.append(",hasMonetaryCost=")
		.append(info.hasMonetaryCost())
		.append(",requiresCell=")
		.append(info.requiresCell())
		.append(",requiresNetwork=")
		.append(info.requiresNetwork())
		.append(",requiresSatellite=")
		.append(info.requiresSatellite())
		.append(",supportsAltitude=")
		.append(info.supportsAltitude())
		.append(",supportsBearing=")
		.append(info.supportsBearing())
		.append(",supportsSpeed=")
		.append(info.supportsSpeed())
		.append("]");
		log(builder.toString());
	}

	// Describe the given location, which might be null
	private void dumpLocation(Location location) {
		if (location == null) {
			log("\nLocation[unknown]");
		} else {
			log("\n Lat: " + location.getLatitude() + "\n Lon: " + location.getLongitude());
			log("\n Lat: " + location.getSpeed());
		}
	}

	// Method for calculating speed
	public double calculateSpeed() {
		//Using Haverside
		if (TimeOfOldLocation == 0) {
			return 0;
		} else {
			// TimeDiff in hours
			double TimeDiff   = ((TimeOfNewLocation - TimeOfOldLocation) / (1000*60*60)) % 24;
			double Distance = calculateDistance();
			double speed = (Distance / TimeDiff);
			if (getSpeedMeasurement().equals("mph")) {
				return speed;
			} else {
				return speed * 1.60934;
			}
		}

	}

	// Method for calculating Distance using Haverside
	//returns distance in miles
	// http://introcs.cs.princeton.edu/java/12types/GreatCircle.java.html
	public double calculateDistance() {

		double a = Math.pow(Math.sin((OldLat - NewLat) / 2), 2) + Math.cos(NewLat) * Math.cos(OldLat) * Math.pow(Math.sin((OldLon - NewLon)/2), 2);

		// great circle distance in radians
		double angle2 = 2 * Math.asin(Math.min(1, Math.sqrt(a)));

		// convert back to degrees
		angle2 = Math.toDegrees(angle2);

		// each degree on a great circle of Earth is 60 nautical miles
		double distance2 = 60 * angle2;

		if (getDistanceMeasurement().equals("mph")) {
			return distance2;
		} else {
			return distance2 * 1.60934;
		}

	}

	// Method to round a double to 2 dp
	// http://www.java-forums.org/advanced-java/4130-rounding-double-two-decimal-places.html
	double roundTwoDecimals(double d) {
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		return Double.valueOf(twoDForm.format(d));
	}

	// Method for Updating label displaying location
	public void updateLocationLabel(String text) {
		TextView t = new TextView(this); 
		t = (TextView)findViewById(R.id.location_label); 
		t.setText(text);
	}

	// Method for Updating label displaying distance travelled so far
	public void updateDistanceLabel(String text) {
		TextView t = new TextView(this); 
		t = (TextView)findViewById(R.id.distance_so_far_label); 
		t.setText(text + " " + getDistanceMeasurement());
	}

	// Method for Updating label displaying speed
	public void updateSpeedLabel(String text) {
		TextView t = new TextView(this); 
		t = (TextView)findViewById(R.id.speed_label); 
		t.setText(text + " " + getSpeedMeasurement());
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			startActivity(new Intent(MainActivity.this, Preferences.class));
			return true;
		case R.id.exit:
			new AlertDialog.Builder(this)
			.setMessage("Are you sure you want to exit?")
			.setCancelable(false)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					//CustomTabActivity.this.finish();
					MainActivity.this.finish();
				}
			})
			.setNegativeButton("No", null)
			.show();
			//this.finish();
			return true;
		}
		return false;
	}

	// Method that returns Measurement for speed from preferences
	private String getSpeedMeasurement() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
		String listPrefs = prefs.getString("listpref", "Please select a measurement in settings.");
		if (listPrefs.equals("Miles is selected")) {
			return "mph";
		} else if (listPrefs.equals("Kilometres is selected")) {
			return "kph";
		} else {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			// set title
			alertDialogBuilder.setTitle("No Measurement Chosen!");
			// set dialog message
			alertDialogBuilder
			.setMessage("Please choose a measurement in settings.")
			.setCancelable(false)
			.setPositiveButton("Close",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					// if this button is clicked, close
					// current activity
					dialog.cancel();	
				}
			})
			.setNegativeButton("Settings",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					// Go to Settings
					startActivity(new Intent(MainActivity.this, Preferences.class));
				}
			});
			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();
			// show it
			alertDialog.show();
			return listPrefs;
		}
	}

	// Method that returns Measurement for distance from preferences
	private String getDistanceMeasurement() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
		String listPrefs = prefs.getString("listpref", "Please select a measurement in settings.");
		if (listPrefs.equals("Miles is selected")) {
			return "mi";
		} else if (listPrefs.equals("Kilometres is selected")) {
			return "km";
		} else {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			// set title
			alertDialogBuilder.setTitle("No Measurement Chosen!");
			// set dialog message
			alertDialogBuilder
			.setMessage("Please choose a measurement in settings.")
			.setCancelable(false)
			.setPositiveButton("Close",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					// if this button is clicked, close
					// current activity
					dialog.cancel();	
				}
			})
			.setNegativeButton("Settings",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					// Go to Settings
					startActivity(new Intent(MainActivity.this, Preferences.class));
				}
			});
			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();
			// show it
			alertDialog.show();
			return listPrefs;
		}
	}

	public void onLocationChanged(Location location) {
		OldLat = NewLat;
		OldLon = NewLon;
		TimeOfOldLocation = TimeOfNewLocation;
		NewLat = location.getLatitude();
		NewLon = location.getLongitude();
		TimeOfNewLocation = System.currentTimeMillis();
		updateLocationLabel("Lat = " + NewLat + "\n" +"Lon = " + NewLon);
		location.setSpeed((float) roundTwoDecimals(calculateSpeed()));
		updateSpeedLabel(Float.toString(location.getSpeed()));
		TripDistance += calculateDistance();
		updateDistanceLabel(Double.toString(roundTwoDecimals(TripDistance)));
		dumpLocation(location);
		System.out.println("LOCATION CHANGED!!!!!!!!!!!!");
	}

	public void onProviderDisabled(String provider) {
		log("\nProvider disabled: " + provider);
		System.out.println("\nProvider disabled: " + provider);
	}

	public void onProviderEnabled(String provider) {
		log("\nProvider enabled: " + provider);
		System.out.println("\nProvider enabled: " + provider);
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		log("\nProvider status changed: " + provider + ", status=" + S[status] + ", extras=" + extras);
		System.out.println("\nProvider status changed: " + provider + ", status=" + S[status] + ", extras=" + extras);
	}

	protected void setTimer() {
		final long elapse = 1000;
		Runnable t = new Runnable() {
			public void run()
			{
				runNextTimedTask();
				if( !isComplete )
				{
					taskHandler.postDelayed( this, elapse );
				}
			}
		};
		taskHandler.postDelayed( t, elapse );

	}

	protected void runNextTimedTask() {
		if (!isPaused) {
			// run my task.
			time += 1;		
			TextView t = new TextView(this); 
			t = (TextView)findViewById(R.id.timer_label); 
			hours = time / 3600;
			minutes = (time % 3600) / 60;
			seconds = time % 60;
			String timeString = hours + ":" + minutes + ":" + seconds;
			t.setText(timeString);
		}
	}


}
