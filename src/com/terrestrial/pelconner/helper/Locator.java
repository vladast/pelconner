package com.terrestrial.pelconner.helper;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

public class Locator implements LocationListener {

	Location _location;
	LocationManager _locationManager;
	Activity _activitySender;
	Object _syncObject;
	
	/**
	 * Locator's c-tor
	 * @param activity
	 * Calling activity
	 */
	public Locator(Activity activity)
	{
		_syncObject = new Object();
		_activitySender = activity;
		_locationManager = (LocationManager)_activitySender.getSystemService(Context.LOCATION_SERVICE);
		_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		_location = new Location(LocationManager.GPS_PROVIDER);
	}
	
	public Location getCurrentLocation()
	{
		Location location;
		synchronized(_location)
		{
			location = new Location(_location);
		}
		return location;
	}
	
	@Override
	public void onLocationChanged(Location location) {
		//Log.d("GPS", "Location changed!");
		synchronized (_location) 
		{
			_location = location;
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}

}
