package com.terrestrial.pelconner.helper;

import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;

public class ImageInfo
{
	/**
	 * Used for passing the object between activities
	 */
	public static final String URI = "ImageUri";
	public static final String TITLE = "Title";
	public static final String DISPLAY_NAME = "DisplayName";
	public static final String DESCRIPTION = "Description";
	public static final String LATITUDE = "Latitude";
	public static final String LONGITUDE = "Longitude";
	
	private Uri _uriImageFile;
	private String _title;
	private String _displayName;
	private String _description;
	private Location _location;
	
	public ImageInfo()
	{
		_uriImageFile = Uri.EMPTY;
		_location = new Location(LocationManager.GPS_PROVIDER);
	}
	
	public void setImageFileUri(Uri imageFile)
	{
		_uriImageFile = imageFile;
	}
	
	public Uri getImageFileUri()
	{
		return _uriImageFile;
	}
	
	public void setTitle(String title)
	{
		_title = title;
	}
	
	public String getTitle()
	{
		return _title;
	}
	
	public void setDisplayName(String displayName)
	{
		_displayName = displayName;
	}
	
	public String getDisplayName()
	{
		return _displayName;
	}
	
	public void setDescription(String description)
	{
		_description = description;
	}
	
	public String getDescription()
	{
		return _description;
	}
	
	public void setLocation(Location location)
	{
		_location = new Location(location);
	}
	
	public Location getLocation()
	{
		return _location;
	}
	
	public void setLatitude(double latitude)
	{
		_location.setLatitude(latitude);
	}
	
	public double getLatitude()
	{
		return _location.getLatitude();
	}
	
	public void setLongitude(double longitude)
	{
		_location.setLongitude(longitude);
	}
	
	public double getLongitude()
	{
		return _location.getLongitude();
	}
}
