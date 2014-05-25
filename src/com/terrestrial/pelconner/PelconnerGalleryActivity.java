package com.terrestrial.pelconner;

import java.io.File;
import java.util.ArrayList;

import com.terrestrial.pelconner.R;
import com.terrestrial.pelconner.adapter.PelconnerGalleryImageAdapter;
import com.terrestrial.pelconner.helper.ImageInfo;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class PelconnerGalleryActivity extends PelconnerActivity implements OnClickListener, OnItemClickListener, OnItemSelectedListener
{
	// TODO: Opens in two ways: from Main activity, import&share are enabled, and from Picture activity, only import enabled.
	
	public static enum Type
	{
		/**
		 * Activity will only be used for image import.
		 */
		REGULAR,
		/**
		 * Activity is going to be used for advanced requests.
		 */
		EXTENDED
	};
	
	
	// Class fields - interface
	private ImageInfo _selectedImage;
	private Gallery _gallery;
	private ImageView _imageViewDisplay;
	private TextView _textViewInfo;
	private Button _buttonImport;
	private Button _buttonShare; // NOTE: Exists only with EXTENDED type of PelconnerGalleryActivity!
	Intent _intentResult;

	// Class fields
	private Type _type;
	private boolean _galleryEmpty;
	
	// Static fields
	//public static final String TAG = "PelconnerGalleryActivity";

	/**
	 * List of the stored images
	 * <b>NOTE: </b>Use <i>getStoredImages()</i> method to obtain the list!
	 */
	private static ArrayList<ImageInfo> _storedImages;
	
	/**
	 * Gets the list of stored images
	 * @return
	 * List of URIs of the stored images
	 */
	public static ArrayList<ImageInfo> getStoredImages()
	{
		if(_storedImages != null && _storedImages.size() > 0)
			return _storedImages;
		else
			return new ArrayList<ImageInfo>();
	}
	
	// TODO: Should be called in these occasions:
	// 1. During the Splash activity, while initializing the application
	// 2. During the Save action, to update the list
	public static void initializeImageList(Activity activitySender)
	{
		/**
		 * Get the images stored on the SD and populate the list with their URIs
		 */
		
		/**
		 * Wont use mediastore queries for now
		String[] columns = {Media.DATA, Media._ID, Media._COUNT, Media.TITLE, Media.DISPLAY_NAME};
		Cursor cursor = managedQuery(Media.EXTERNAL_CONTENT_URI, columns, null, null, null);
		*/

		// TODO: Check if usage of MediaStore is faster than this
		//_storedImages = getImagesFromDirectory(Environment.getExternalStorageDirectory());
		if(activitySender == null)
		{
			_storedImages = getImagesFromDirectory(Environment.getExternalStorageDirectory());
		}
		else
		{
			_storedImages = getImagesFromMediaStore(activitySender);
		}
		
		//Log.v(PelconnerActivity.ACTIVITY_TAG_GALLERY, "# of stored images is: " + _storedImages.size());
	}
	
	protected static ArrayList<ImageInfo> getImagesFromMediaStore(Activity activitySender)
	{
		ArrayList<ImageInfo> listOfImages = new ArrayList<ImageInfo>();
		String[] columns = {
				Media._ID,
				Media.DATA, 
				Media.TITLE, 
				Media.DISPLAY_NAME,
				Media.DESCRIPTION,
				Media.LATITUDE,
				Media.LONGITUDE
		};
		Cursor cursor = activitySender.managedQuery(Media.EXTERNAL_CONTENT_URI, columns, null, null, null);
		
		int cursorIndexId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
		int cursorIndexFileColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		int cursorIndexTitle = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
		int cursorIndexDisplayName = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
		int cursorIndexDescription = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DESCRIPTION);
		int cursorIndexLatitude = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.LATITUDE);
		int cursorIndexLongitude = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.LONGITUDE);
		
		if(cursor.moveToFirst())
		{
			do
			{
				ImageInfo imageInfo = new ImageInfo();
				imageInfo.setImageFileUri(Uri.parse(cursor.getString(cursorIndexFileColumn)));
				imageInfo.setTitle(cursor.getString(cursorIndexTitle));
				imageInfo.setDisplayName(cursor.getString(cursorIndexDisplayName));
				imageInfo.setDescription(cursor.getString(cursorIndexDescription));
				imageInfo.setLatitude(cursor.getDouble(cursorIndexLatitude));
				imageInfo.setLongitude(cursor.getDouble(cursorIndexLongitude));
				
				listOfImages.add(imageInfo);
			}
			while(cursor.moveToNext());
		}
		
		
		return listOfImages;
	}
	
	/**
	 * Gets list of images from all nested directories, starting from the given one
	 * @param startingFolder
	 * Directory from which to start the search
	 * @return
	 * List of 
	 */
	protected static ArrayList<ImageInfo> getImagesFromDirectory(File startingFolder)
	{
		ArrayList<ImageInfo> listOfImages = new ArrayList<ImageInfo>();
		ArrayList<ImageInfo> listOfNestedImages = new ArrayList<ImageInfo>();
		String[] imageExtensions = {".jpg", ".png", ".bmp"};
				
		if(startingFolder.isDirectory() && !startingFolder.getPath().contains("/.")) // Avoiding system/private folders, such as .thumbnails
		{
    		File[] listOfFiles = startingFolder.listFiles();                       
    		if (listOfFiles != null && listOfFiles.length > 0)
    		{
    			for (File file : listOfFiles)
    			{
    				if(file.isDirectory() && !startingFolder.getPath().contains("/."))
    				{
    					listOfNestedImages = getImagesFromDirectory(file);
    					for(ImageInfo imageInfo : listOfNestedImages)
    					{
    						listOfImages.add(imageInfo);
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
            					ImageInfo imageInfo = new ImageInfo();
            					imageInfo.setImageFileUri(Uri.parse(file.getAbsolutePath()));
            					listOfImages.add(imageInfo); // This works fine.
            					break;
            				}                      
            			}    					
    				}
    			}
    		}
		}
		
		return  listOfImages;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		TAG = PelconnerActivity.ACTIVITY_TAG_GALLERY;
		
        // Removing title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        
        /**
         * Initialize Gallery type before setting the appropriate content view.
         */
        initGalleryType();
        
        switch(_type)
        {
        case REGULAR:
        	setContentView(R.layout.gallery);
        	break;
        case EXTENDED:
        	setContentView(R.layout.gallery_extended);
        	break;
        }
        
        
        /**
         * Basic initialization
         */
        _selectedImage = new ImageInfo();
        _galleryEmpty = (getStoredImages().size() == 0);
        if(_galleryEmpty)
        {
        	initializeImageList(this);
            _galleryEmpty = (getStoredImages().size() == 0);
        }
        _gallery = (Gallery)findViewById(R.id.gallery);
        _imageViewDisplay = (ImageView)findViewById(R.id.imageViewDisplay);
        _textViewInfo = (TextView)findViewById(R.id.textViewInfo);
        _buttonImport = (Button)findViewById(R.id.buttonImport);
        if(_type == Type.EXTENDED)
        {
        	_buttonShare = (Button)findViewById(R.id.buttonShare);
        }
        
        /**
         * Setting gallery adapter and "on item" listener
         */
        _gallery.setAdapter(new PelconnerGalleryImageAdapter(this));
        _gallery.setOnItemClickListener(this);
        
        //_gallery.setCallbackDuringFling(true);
        
        /**
         * Setting button listeners
         */
        _buttonImport.setOnClickListener(this);
        if(_type == Type.EXTENDED)
        {
        	_buttonShare.setOnClickListener(this);
        }
        
        /**
         * Setting default image resource
         * 	- Display the first item found on system
         *  - Otherwise, a generic "No photo available" image should be displayed
         */
        if(_galleryEmpty)
        {
        	_imageViewDisplay.setImageResource(R.drawable.ic_no_photos);
        }
        else
        {
        	_selectedImage = (ImageInfo)_gallery.getItemAtPosition(0);
        	
        	displaySelectedImage();
        }
        
        PelconnerActivity.addReference(this);
	}

	void initGalleryType()
	{
		int requestCode = this.getIntent().getExtras().getInt(PelconnerActivity.ACTIVITY_REQUEST_CODE);
        
        switch(requestCode)
        {
        case PelconnerActivity.ACTIVITY_REQUEST_CODE_GALLERY_EXTENDED:
			//Toast.makeText(this, "GALLERY WITH SHARE", Toast.LENGTH_SHORT).show();
			
			_type = Type.EXTENDED;
			
			// TODO: Perform action

        	break;
        case PelconnerActivity.ACTIVITY_REQUEST_CODE_GALLERY_REGULAR:
        	//Toast.makeText(this, "GALLERY WITHOUT SHARE", Toast.LENGTH_SHORT).show();
        	
        	_type = Type.REGULAR;
        	
        	// TODO: Perform action
        	
        	break;
        default:
        	// Illegal code is received
        	_selectedImage = new ImageInfo();
        }
	}
	
	private void displaySelectedImage()
	{
		Display display = getWindowManager().getDefaultDisplay();
		float displayHeight = display.getHeight() - _gallery.getHeight();
		float displayWidth = display.getWidth();
		

		displayHeight -= 5;
		displayWidth -= 5;
		
		
		BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
    	//bitmapFactoryOptions.inSampleSize = 8;
    	bitmapFactoryOptions.inJustDecodeBounds = true;
    	Bitmap bitmap = BitmapFactory.decodeFile(_selectedImage.getImageFileUri().getPath(), bitmapFactoryOptions);
    	
    	int heightRatio = (int)Math.ceil((float)bitmapFactoryOptions.outHeight / (float)displayHeight);
    	int widthRatio = (int)Math.ceil((float)bitmapFactoryOptions.outWidth / (float)displayWidth);
    	
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
    	
    	float bitmapHeight, bitmapWidth, aspectRatio, aspectRatioDisplay;    	
    	
    	try
    	{
    		bitmap = BitmapFactory.decodeFile(_selectedImage.getImageFileUri().getPath(), bitmapFactoryOptions);
    	}
    	catch(Exception e)
    	{
    		bitmap = Bitmap.createBitmap((int)displayWidth, (int)displayHeight, Config.ALPHA_8);
    	}

    	if(bitmap == null)
    	{
    		bitmap = Bitmap.createBitmap((int)displayWidth, (int)displayHeight, Config.ALPHA_8);
    	}
    	
    	bitmapHeight = bitmap.getHeight();
    	bitmapWidth = bitmap.getWidth();
    	

    	aspectRatio = bitmapWidth / bitmapHeight;
    	aspectRatioDisplay = displayWidth / displayHeight;
    	
    	if(aspectRatio > aspectRatioDisplay)
    	{
    		displayHeight = displayWidth / aspectRatio;
    	}
    	else
    	{
    		displayWidth = displayHeight * aspectRatio;
    	}
    	
    	//paint.setColor(Color.TRANSPARENT);
    	
    	float scale;
    	
    	scale = displayWidth / bitmapWidth;
    	
    	Bitmap bitmapDisplayed = null;
    	try
    	{
    		bitmapDisplayed = Bitmap.createBitmap((int)displayWidth, (int)displayHeight, bitmap.getConfig());
    	}
    	catch(Exception e)
    	{
    		bitmapDisplayed = Bitmap.createBitmap((int)displayWidth, (int)displayHeight, Config.ALPHA_8);
    	}
    	
    	Canvas canvas = new Canvas(bitmapDisplayed);
    	canvas.drawColor(Color.TRANSPARENT);
    	Matrix matrix = new Matrix();
    	Paint paint = new Paint();
    	paint.setAntiAlias(true);
    	paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
    	
    	matrix.preScale(scale, scale);
    	canvas.drawBitmap(bitmap, matrix, paint);
    	
    	_imageViewDisplay.setImageBitmap(bitmapDisplayed);			
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		_selectedImage = (ImageInfo)_gallery.getItemAtPosition(position);
		
		displaySelectedImage();
		
		//_textViewInfo.setText("onItemClick - (" + String.valueOf(position) + ")");
		
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// TODO: Add additional image adjustments if needed
		//_imageViewDisplay.setImageResource((Integer)_gallery.getItemAtPosition(position));
		
		
		_selectedImage = (ImageInfo)_gallery.getItemAtPosition(position);
		_imageViewDisplay.setImageURI(_selectedImage.getImageFileUri());
		_imageViewDisplay.refreshDrawableState();
		_imageViewDisplay.setBackgroundColor(Color.TRANSPARENT);
		
		//_textViewInfo.setText("onItemSelected - (" + String.valueOf(position) + ")");
		
		/*
		ImageInfo imageInfo = _storedImages.get(position);
		File file = new File(imageInfo.getImageFileUri().toString());
		Log.d(TAG, "File: " + file.getAbsolutePath());
		BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
    	//bitmapFactoryOptions.inSampleSize = 8;
		bitmapFactoryOptions.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(), bitmapFactoryOptions);
		
		_imageViewDisplay.setImageBitmap(bitmap);
		//imageView.setBackgroundResource(_galleryItemBackground);
		
		//imageView.setBackgroundResource(resid)
		_imageViewDisplay.setBackgroundColor(Color.TRANSPARENT);
		_imageViewDisplay.refreshDrawableState();
		*/
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO: Add additional image adjustments if needed
		_imageViewDisplay.setImageResource(R.drawable.ic_menu_help);
		_textViewInfo.setText("No items selected!");
	}

	@Override
	public void onClick(View viewSender) {
		if(viewSender == _buttonImport)
		{
			onImportClicked();
		}
		else if(_type == Type.EXTENDED && viewSender == _buttonShare)
		{
			onShareClicked();
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#finish()
	 */
	@Override
	public void finish() {// Remove the reference from the list
		boolean bResult = PelconnerActivity.removeReference(PelconnerGalleryActivity.this);
		
		if(!bResult)
		{
			//Log.e(TAG, "Reference not removed from the list!");
		}
		
        // Finishing with this activity
		super.finish();
	}
	
	void onImportClicked()
	{
		if(_selectedImage.getImageFileUri() != Uri.EMPTY)
		{
			Bundle bundle = new Bundle();
        	bundle.putString(ImageInfo.URI, _selectedImage.getImageFileUri().toString());
        	bundle.putString(ImageInfo.TITLE, _selectedImage.getTitle());
        	bundle.putString(ImageInfo.DISPLAY_NAME, _selectedImage.getDisplayName());
        	bundle.putString(ImageInfo.DESCRIPTION, _selectedImage.getDescription());
        	bundle.putDouble(ImageInfo.LATITUDE, _selectedImage.getLatitude());
        	bundle.putDouble(ImageInfo.LONGITUDE, _selectedImage.getLongitude());
			
			if(_type == Type.EXTENDED) // Called from Main activity; Set the result, and start Picture activity
			{
	        	// Set the response code/data and finish with this activity
	        	_intentResult = new Intent(PelconnerGalleryActivity.this, PelconnerPictureActivity.class);
	        	

	        	
	        	_intentResult.putExtra(PelconnerActivity.ACTIVITY_RESPONSE_SELECTED_IMAGE, bundle);
				setResult(RESULT_OK, _intentResult);
				startActivity(_intentResult);
				finish();				
			}
			else // Called from Picture activity; Only return the result.
			{
	        	// Set the response code/data and finish with this activity
	        	_intentResult = new Intent();
	        	_intentResult.putExtra(PelconnerActivity.ACTIVITY_RESPONSE_SELECTED_IMAGE, bundle);
				setResult(RESULT_OK, _intentResult);
				finish();				
			}
		}
		else
		{
			setResult(RESULT_CANCELED);
			finish();			
		}		
	}
	
	void onShareClicked()
	{
		if(_selectedImage.getImageFileUri() != Uri.EMPTY)
		{
        	// Set the response code/data and finish with this activity
        	/*_intentResult = new Intent(PelconnerGalleryActivity.this, PelconnerShareActivity.class);
        	_intentResult.putExtra(PelconnerActivity.ACTIVITY_RESPONSE_SELECTED_IMAGE, _selectedImage.toString());
			setResult(RESULT_OK, _intentResult);
			startActivity(_intentResult);
			finish();*/

			//Bitmap bitmap = Images.Media.getBitmap(getContentResolver(), _selectedImage);
			shareIt(_selectedImage.getImageFileUri());
		}
		else
		{
			setResult(RESULT_CANCELED);
			finish();			
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		if(_type == Type.REGULAR)
		{
			//menuInflater.inflate(R.menu.gallery, menu);
		}
		else
		{
			//menuInflater.inflate(R.menu.gallery_extended, menu);
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch(itemId)
		{
		case R.id.gallery_menu_item_import:
			onImportClicked();
			return true;
		case R.id.gallery_menu_item_share:
			onShareClicked();
			return true;
		case R.id.gallery_menu_item_cancel:
			setResult(RESULT_CANCELED);
			finish();
			return true;
		case R.id.gallery_menu_item_help:
			// TODO: Start Help activity/dialog
			return true;
		default:
			return super.onOptionsItemSelected(item);	
		}		
	}
}
