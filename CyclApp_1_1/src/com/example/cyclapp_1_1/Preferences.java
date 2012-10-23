package com.example.cyclapp_1_1;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.xml.settings);
		addPreferencesFromResource(R.xml.settings);
		
	}
}
