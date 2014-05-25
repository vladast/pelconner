package com.terrestrial.pelconner.helper;

import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Color;

public class ImageProcessorThread extends Thread {

	private Bitmap _bitmapSource = null;
	private Bitmap _bitmapModified = null;
	private float _level = 0.0F;
	private PelconnerOption.Availables _processingType = null;
	
	// TODO: While creating the thread, the whole image, or its part is passed as an argument
	// to the c-tor.
	public ImageProcessorThread(Bitmap source, PelconnerOption.Availables processingType, float level)
	{
		_bitmapSource = source.copy(source.getConfig(), true);
		_processingType = processingType;
		_level = level;
	}

	private /*static*/ Bitmap addNoise(Bitmap bitmap, float level)
	{
		// level should go from 0 to 255
		
		Bitmap bitmapWithNoise = bitmap.copy(bitmap.getConfig(), true);
		Random random = new Random();
		double noiseValue = 0;
		int colorValue, colorRedValue, colorGreenValue, colorBlueValue;
		
		for(int i = 0; i < bitmapWithNoise.getWidth(); ++i)
		{
			for(int j = 0; j < bitmapWithNoise.getHeight(); ++j)
			{
				noiseValue = random.nextGaussian() * level;
				
				colorValue = bitmapWithNoise.getPixel(i, j);
				colorRedValue = Color.red(colorValue) + (int)noiseValue;
				colorBlueValue = Color.blue(colorValue) + (int)noiseValue;
				colorGreenValue = Color.green(colorValue) + (int)noiseValue;
				
				colorRedValue = colorRedValue > 255 ? 255 : colorRedValue;
				colorBlueValue = colorBlueValue > 255 ? 255 : colorBlueValue;
				colorGreenValue = colorGreenValue > 255 ? 255 : colorGreenValue;
				
				colorRedValue = colorRedValue < 0 ? 0 : colorRedValue;
				colorBlueValue = colorBlueValue < 0 ? 0 : colorBlueValue;
				colorGreenValue = colorGreenValue < 0 ? 0 : colorGreenValue;
				
				bitmapWithNoise.setPixel(i, j, Color.rgb(colorRedValue, colorGreenValue, colorBlueValue));
			}
		}
		
		return bitmapWithNoise;
	}	
	
	@Override
	public void run() {
		_bitmapModified	= addNoise(_bitmapSource, _level);
	}
	
	public Bitmap getBitmap()
	{
		return _bitmapModified;
	}
	
	// TODO Not used for now
	/*
	@Override
	public void interrupt() {
		super.interrupt();
	}
	
	@Override
	public synchronized void start() {
		super.start();
	}
	*/
}
