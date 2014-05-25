package com.terrestrial.pelconner.adapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import com.terrestrial.pelconner.R;
import com.terrestrial.pelconner.PelconnerGalleryActivity;
import com.terrestrial.pelconner.helper.ImageInfo;

import android.R.color;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class PelconnerGalleryImageAdapter extends BaseAdapter {

	private final String TAG = "PelconnerGalleryImageAdapter";
	
	private int _galleryItemBackground;
	private Context _context;
	private ArrayList<ImageInfo> _storedImages;
	private FilenameFilter _filenameFilter;
	
	public PelconnerGalleryImageAdapter(Context context) {
		_context = context;
		TypedArray typedArray = context.obtainStyledAttributes(R.styleable.Gallery);
        _galleryItemBackground = typedArray.getResourceId(R.styleable.Gallery_android_galleryItemBackground, 0);
        typedArray.recycle();		
        
        _storedImages = PelconnerGalleryActivity.getStoredImages();
        
        // TODO: Not used, should be removed.
        _filenameFilter = new FilenameFilter(){

			@Override
			public boolean accept(File folder, String filename) {
				String[] imageExtensions = {".jpg", ".png", ".bmp"};
				//return (filename.endsWith(".jpg") || filename.endsWith(".png") || filename.endsWith(".bmp") || filename.endsWith(".gif"));
	            try              
	            {                  
	            	//Checking only directories, since we are checking for files within                  
	            	//a directory                  
	            	if(folder.isDirectory())                  
	            	{                      
	            		File[] listOfFiles = folder.listFiles();                       
	            		if (listOfFiles == null) 
	            			return false;                       
	            		//For each file in the directory...                      
	            		for (File file : listOfFiles)                      
	            		{                                                     
	            			//Check if the extension is one of the supported filetypes                                                    
	            			//imageExtensions is a String[] containing image filetypes (e.g. "png")        
	            			for (String ext : imageExtensions)                          
	            			{                              
	            				if (file.getName().endsWith("." + ext))
	            				{
	            					return true;                          
	            				}                      
	            			}                  
	            			return false;              
	            		}
	            	}
	            }
	            catch (SecurityException e)              
	            {                  
	            	return false;              
	            }
				return false;
			}
		};
		
		// Transfered into PelconnerGalleryActivity --> list is already initialized at this point
		//initializeImageList();
	}

	/**
	 * Gets list of images from all nested directories, starting from the given one
	 * @param startingFolder
	 * Directory from which to start the search
	 * @return
	 * List of 
	 */
	// Transfered in PelconnerGalleryActivity
	/*private ArrayList<Uri> getImagesFromDirectory(File startingFolder)
	{
		ArrayList<Uri> listOfImages = new ArrayList<Uri>();
		ArrayList<Uri> listOfNestedImages = new ArrayList<Uri>();
		String[] imageExtensions = {".jpg", ".png", ".bmp"};
		
		if(startingFolder.isDirectory())
		{
    		File[] listOfFiles = startingFolder.listFiles();                       
    		if (listOfFiles != null && listOfFiles.length > 0)
    		{
    			for (File file : listOfFiles)
    			{
    				if(file.isDirectory())
    				{
    					listOfNestedImages = getImagesFromDirectory(file);
    					for(Uri uri : listOfNestedImages)
    					{
    						listOfImages.add(uri);
    					}
    				}
    				else
    				{
            			for (String ext : imageExtensions)                          
            			{                              
            				if (file.getName().endsWith(ext))
            				{
            					//Log.d(TAG, "Found file: " + file.getName());
            					
            					//listOfImages.add(Uri.fromFile(file)); // Does not work! Logs error "resolveUri failed on bad bitmap uri: file:///"
            					listOfImages.add(Uri.parse(file.getAbsolutePath())); // This works fine.
            					break;
            				}                      
            			}    					
    				}
    			}
    		}
		}
		
		return  listOfImages;
	}*/
	
	
	@Override
	public int getCount() {
		int count = _storedImages.size();
		return count;
	}

	@Override
	public Object getItem(int position) {
		// Should return drawable from the system
		return _storedImages.get(position);
	}

	@Override
	public long getItemId(int position) {
		/**
		 * Not important here - returning the selected position
		 */
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView = new ImageView(_context);
		//imageView.setImageResource(_imageIds[position]);
		ImageInfo imageInfo = _storedImages.get(position);
		File file = new File(imageInfo.getImageFileUri().toString());
		BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
    	//bitmapFactoryOptions.inSampleSize = 8;
		bitmapFactoryOptions.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(), bitmapFactoryOptions);
		int bitmapHeight = bitmapFactoryOptions.outHeight;
		int bitmapWidth = bitmapFactoryOptions.outWidth;
		
		float aspectRatio = (float)bitmapWidth / (float)bitmapHeight;
		float layoutHeight = 100F; // 120F
		float layoutWidth = aspectRatio * layoutHeight;
		
    	int heightRatio = (int)Math.ceil(bitmapHeight / (float)layoutHeight);
    	int widthRatio = (int)Math.ceil(bitmapWidth / (float)layoutWidth);		
		
    	if(heightRatio > 1 && widthRatio > 1)
    	{
    		if(heightRatio > widthRatio)
    		{
    			bitmapFactoryOptions.inSampleSize = heightRatio;
    		}
    		else
    		{
    			bitmapFactoryOptions.inSampleSize = widthRatio;
    		}
    	}
    	
    	bitmapFactoryOptions.inJustDecodeBounds = false;
    	
    	bitmap = BitmapFactory.decodeFile(file.getPath(), bitmapFactoryOptions);
		
		
		/**
		 * Landscape:
		 * 	width = 150 
		 * Portrait:
		 * 	height = 120
		 */
		// TODO: Check what's wrong with the gallery thumbnails - from some reason, thumbnail has some added space at the top and the bottom, if correct aspect ratio is used.
		//float layoutHeight = (height > width) ? (120F) : ((150F / aspectRatio)/* * (float)1.2*/);
		//float layoutWidth = (height > width) ? (120F * aspectRatio) : (150F);
		/*float layoutHeight = (height > width) ? (120F) : (100F);
		float layoutWidth = (height > width) ? (100F) : (150F);*/

		

    	//Bitmap bitmap = BitmapFactory.decodeFile(_selectedImage.getPath(), bitmapFactoryOptions);

    	
    	/*float scale = layoutHeight/ bitmapHeight;
    	
    	Bitmap finalBitmap = Bitmap.createBitmap((int)layoutWidth, (int)layoutHeight, bitmap.getConfig());
    	Canvas canvas = new Canvas(finalBitmap);
    	Paint paint = new Paint();
    	//paint.setAntiAlias(true);
    	Matrix matrix = new Matrix();
    	matrix.preScale(scale, scale);
    	canvas.drawBitmap(bitmap, matrix, paint);*/
    	
    	/*Matrix matrix = new Matrix();
    	float scaleX, scaleY;
    	scaleX = scaleY = 0.0F;
    	matrix.setScale(scaleX, scaleY);*/
    	//Bitmap alteredBitmap = Bitmap.createBitmap(bitmap, 0, 0, 100, 150);
    	
    	
    	
		//imageView.setImageURI(uri);
    	
    	
    	imageView.setImageBitmap(bitmap/*finalBitmap*/);
		imageView.setLayoutParams(new Gallery.LayoutParams(Math.round(layoutWidth), (int)layoutHeight));
		//imageView.setBackgroundColor(R.color.activity_gallery_thumbnail_background);
		//imageView.setAdjustViewBounds(true);
		imageView.setScaleType(ImageView.ScaleType.FIT_XY);
		//imageView.setBackgroundResource(_galleryItemBackground);
		
		//imageView.setBackgroundResource(resid)
		imageView.setBackgroundColor(Color.TRANSPARENT);
		imageView.refreshDrawableState();

		// This doesn't work!
		/*
		int height = imageView.getHeight();
		int width = imageView.getWidth();
		Matrix matrix = new Matrix();
		if(height > width)
		{
			//matrix.postRotate(90);
			matrix.setRotate(90);
		}		
		
		imageView.setImageMatrix(matrix);
		*/
		
		return imageView;
	}

	// Transfered in PelconnerGalleryActivity
	/*private void initializeImageList()
	{
		 // Get the images stored on the SD and populate the list with their URIs
		 
		
		_storedImages = getImagesFromDirectory(Environment.getExternalStorageDirectory());
		
		
		Log.v(TAG, "# of stored images is: " + _storedImages.size());
	}*/
}
