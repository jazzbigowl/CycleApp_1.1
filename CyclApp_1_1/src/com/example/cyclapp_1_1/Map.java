package com.example.cyclapp_1_1;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import android.os.Bundle;

public class Map extends MapActivity {
	private MapView myMap;
	private MapController controller;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		initMapView();
		initMyLocation();
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		// Required by MapActivity
		return false;
	}
	
	private void initMapView() {
		myMap = (MapView) findViewById(R.id.map);
		controller = myMap.getController();
		myMap.setSatellite(true);
		myMap.setBuiltInZoomControls(true);
	}
	
	private void initMyLocation() {
		final MyLocationOverlay overlay = new MyLocationOverlay(this, myMap);
		overlay.enableMyLocation();
		// overlay.enableCompass(); // does not work in emulator
		overlay.runOnFirstFix(new Runnable() {
			public void run() {
				// Zoom in to current location
				controller.setZoom(18);
				controller.animateTo(overlay.getMyLocation());
			}
		});
		myMap.getOverlays().add(overlay);
	}

}
