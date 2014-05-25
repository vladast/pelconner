package com.terrestrial.pelconner.helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import com.terrestrial.pelconner.R;
import com.terrestrial.pelconner.PelconnerCameraActivity;
import com.terrestrial.pelconner.PelconnerGalleryActivity;

import android.app.Activity;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore.Images.Media;

public class ImageSaverThread extends Thread {

	private ImageData _imageData;
	private int _orientation;
	private Locator _locator;
	private Activity _senderActivity;
	private byte[] _bitmapData;
	private boolean _running;
	private Uri _imageFileUri;
	
	public ImageSaverThread(Activity sender, ImageData imageData, byte[] data, int orientation)
	{
		super();
		_senderActivity = sender;
		_imageData = imageData;
		_orientation = orientation;
		_bitmapData = data;
		_running = false;
		_imageFileUri = Uri.EMPTY;
	}
	
	public ImageSaverThread(Activity sender, ImageData imageData, byte[] data, int orientation, Locator locator)
	{
		super();
		_senderActivity = sender;
		_imageData = imageData;
		_orientation = orientation;
		_locator = locator;
		_bitmapData = data;
		_running = false;
		_imageFileUri = Uri.EMPTY;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() 
	{
		_running = true;
		if(_imageData.getHeight() > 0 && _imageData.getWidth() > 0)
		{
			// TODO: resize the image if requested
			
			Location location = null;
			if(_locator != null && _imageData.isSaveLocation())
			{
				location = _locator.getCurrentLocation();
			}
			
			//String imagePath = Environment.getExternalStorageDirectory().toString() + File.separator + "DCIM" + File.separator + "Camera";
			String ext, mime;
			mime = "image/";
			Bitmap.CompressFormat compressFormat;
			if(_imageData.getFormat() == ImageData.Format.JPG)
			{
				compressFormat = Bitmap.CompressFormat.JPEG;
				ext = ".jpg";
				mime += "jpeg";
			}
			else if(_imageData.getFormat() == ImageData.Format.PNG)
			{
				compressFormat = Bitmap.CompressFormat.PNG;
				ext = ".png";
				mime += "png";
			}
			else
			{
				compressFormat = Bitmap.CompressFormat.PNG;
				ext = ".bmp";
				mime += "bmp";
			}				
			//imagePath = imagePath + File.separator + "something"/*imageData.getName()*/ + ext;
			
			ContentValues contentValues = new ContentValues(8);
			// TODO: Get the strings from strings.xml!!!
			contentValues.put(Media.DISPLAY_NAME, _imageData.getName()); 
			contentValues.put(Media.TITLE, _imageData.getName());
			contentValues.put(Media.DESCRIPTION, _imageData.getDescription());
			contentValues.put(Media.ORIENTATION, _orientation);
			contentValues.put(Media.DATE_TAKEN, (new Date()).getTime());
			if(location != null && _imageData.isSaveLocation())
			{
				contentValues.put(Media.LATITUDE, location.getLatitude());
				contentValues.put(Media.LONGITUDE, location.getLongitude());
			}
			contentValues.put(Media.MIME_TYPE, "image/" + mime);
			
			//Uri.encode(s);
			

			Uri imageFileUri = _senderActivity.getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, contentValues);
			try
			{
				OutputStream imageFileOS = _senderActivity.getContentResolver().openOutputStream(imageFileUri);
				imageFileOS.write(_bitmapData);
				
				/*
				Bitmap bitmap = BitmapFactory.decodeByteArray(_bitmapData, 0, _bitmapData.length);
				bitmap.compress(compressFormat, 100, imageFileOS);
				*/
				
				if(compressFormat == Bitmap.CompressFormat.JPEG)
				{
					Bitmap bitmap = BitmapFactory.decodeByteArray(_bitmapData, 0, _bitmapData.length);
					Bitmap bitmapWhiteBackground = bitmap.copy(bitmap.getConfig(), true);
					Canvas canvas = new Canvas(bitmapWhiteBackground);
					canvas.drawColor(Color.WHITE);
					Paint paint = new Paint();
					//paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
					canvas.drawBitmap(bitmap, new Matrix(), paint);
					bitmapWhiteBackground.compress(compressFormat, 100, imageFileOS);
				}
				else
				{
					Bitmap bitmap = BitmapFactory.decodeByteArray(_bitmapData, 0, _bitmapData.length);
					bitmap.compress(compressFormat, 100, imageFileOS);
				}
				
				imageFileOS.flush();
				imageFileOS.close();					
			} 
			catch (FileNotFoundException e) 
			{
			} 
			catch (IOException e) 
			{
			}
			
			PelconnerGalleryActivity.initializeImageList(_senderActivity);
			

			_imageFileUri = imageFileUri;
			
		}
		_running = false;
	}
}
