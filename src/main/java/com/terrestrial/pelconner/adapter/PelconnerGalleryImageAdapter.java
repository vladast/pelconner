package com.terrestrial.pelconner.adapter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import com.terrestrial.pelconner.R;
import com.terrestrial.pelconner.PelconnerGalleryActivity;
import com.terrestrial.pelconner.helper.ImageInfo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class PelconnerGalleryImageAdapter extends BaseAdapter {

	private final String TAG = PelconnerGalleryImageAdapter.class.getSimpleName();

	private Context mContext;
	private ArrayList<ImageInfo> mStoredImages;
	
	public PelconnerGalleryImageAdapter(Context context) {
		mContext = context;
        mStoredImages = PelconnerGalleryActivity.getStoredImages();
	}

	@Override
	public int getCount() {
		return mStoredImages.size();
	}

	@Override
	public Object getItem(int position) {
		// Should return drawable from the system
		return mStoredImages.get(position);
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
		ImageView imageView = new ImageView(mContext);
		ImageInfo imageInfo = mStoredImages.get(position);
		File file = new File(imageInfo.getImageFileUri().toString());
		BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
		bitmapFactoryOptions.inJustDecodeBounds = true;
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

        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(), bitmapFactoryOptions);
		
		
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
		//imageView.setBackgroundResource(mGalleryItemBackground);
		
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
		 
		
		mStoredImages = getImagesFromDirectory(Environment.getExternalStorageDirectory());
		
		
		Log.v(TAG, "# of stored images is: " + mStoredImages.size());
	}*/
}
