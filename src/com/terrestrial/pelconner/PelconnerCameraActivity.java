package com.terrestrial.pelconner;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.terrestrial.pelconner.R;
import com.terrestrial.pelconner.dialogs.PelconnerDialog;
import com.terrestrial.pelconner.dialogs.PelconnerSaveDialog;
import com.terrestrial.pelconner.dialogs.PelconnerSnapshotDialog;
import com.terrestrial.pelconner.helper.ImageData;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.OrientationEventListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class PelconnerCameraActivity extends PelconnerActivity implements OnDismissListener, SurfaceHolder.Callback, Camera.PictureCallback, Camera.ErrorCallback, OnClickListener 
{

	// NOTE: Opposite to the Gallery activity, activity's layout is the same for regular and extended camera.
	// The difference exists in the Camera Snapshot dialog that is being opened when snapshot is taken.
	// Extended dialog has one additional button, Share, and Regular does not.
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
	
	private final int DIALOG_SNAPSHOT_ID = 0;
	private final int DIALOG_SAVE_ID = 1;
	
	// Class fields
	private SurfaceView _cameraView;
	private FrameLayout _frameLayoutCamera;

	private SurfaceHolder _surfaceHolder;
	private Camera _camera;
	private OrientationEventListener _orientationEventListener;
	private int _orientation = -1;
	private Button _buttonSnapshot;
	private Bitmap _bitmapSnapshot;
	private byte[] _byteSnapshot;
	private Type _type;
	private PelconnerSnapshotDialog _dialogSnapshot;
	private PelconnerSaveDialog _dialogSave;
	private int _initialOrientation;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // Removing title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR | ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        /**
         * TODO Detect this state:
         * java.lang.RuntimeException: Fail to connect to camera service
         */
        
        setContentView(R.layout.camera);
        
        /**
         * Basic initialization
         */
        _bitmapSnapshot = null;
        _byteSnapshot = null;
        _cameraView = (SurfaceView)this.findViewById(R.id.surfaceViewCamera);
        _surfaceHolder = _cameraView.getHolder();
        // TODO SURFACE_TYPE_PUSH_BUFFERS is deprecated! Check if this is needed!
        _surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        _surfaceHolder.addCallback(this);
        _frameLayoutCamera = (FrameLayout)findViewById(R.id.frameLayoutCamera);
        
        // Initially, preview will be rotated
        _initialOrientation = this.getWindowManager().getDefaultDisplay().getOrientation();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        _cameraView.setFocusable(true);
        _cameraView.setFocusableInTouchMode(true);
        _cameraView.setClickable(true);
        _cameraView.setOnClickListener(this);
        
        _buttonSnapshot = (Button)findViewById(R.id.buttonSnapshot);
        _buttonSnapshot.setOnClickListener(this);
        
        initCameraType();
        
        
        /*_orientationListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
			
			@Override
			public void onOrientationChanged(int orientation) {
				Log.d(TAG, "Orientation: " + orientation);
			}
		};*/
        
		// In the case that Camera's onCreate was called because of some external action such as orientation change
		
		
		PelconnerActivity.addReference(this);
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		_initialOrientation = newConfig.orientation;
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		
		//_orientationEventListener.disable();
		
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		/*
		if(_orientationEventListener ==null)
		{
			
		}
		if(_orientationEventListener.canDetectOrientation())
		{
			_orientationEventListener.enable();
		}*/
		
		/*if(_dialogSnapshot != null)
		{
			_dialogSnapshot.show();
		}
		
		if(_dialogSave != null)
		{
			_dialogSave.show();
		}
		*/
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	void initCameraType()
	{
		Bundle extras = this.getIntent().getExtras();
		if(extras != null)
		{
			int requestCode = extras.getInt(PelconnerActivity.ACTIVITY_REQUEST_CODE);
	        
	        switch(requestCode)
	        {
	        case PelconnerActivity.ACTIVITY_REQUEST_CODE_CAMERA_EXTENDED:
				//Toast.makeText(this, "CAMERA WITH SHARE", Toast.LENGTH_SHORT).show();
				
				_type = Type.EXTENDED;
				
				// TODO: Perform action
	
	        	break;
	        case PelconnerActivity.ACTIVITY_REQUEST_CODE_CAMERA_REGULAR:
	        	//Toast.makeText(this, "CAMERA WITHOUT SHARE", Toast.LENGTH_SHORT).show();
	        	
	        	_type = Type.REGULAR;
	        	
	        	// TODO: Perform action
	        	
	        	break;
	        default:
	        	// Illegal code is received
	        	_bitmapSnapshot = null;
	        	_byteSnapshot = null;
	        }
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#finish()
	 */
	@Override
	public void finish() {
		// TODO Remove the activity from the list and finish with it.
		/**
		 * Called when user selects appropriate option (finishes with Import/Share)
		 */
		// TODO Check if something additional should be performed at this stage.
		boolean bResult = PelconnerActivity.removeReference(this);
		if(!bResult)
		{
			//Log.e(TAG, "Reference not removed from the list!");
		}
		
        // Finishing with this activity
		super.finish();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//super.onActivityResult(requestCode, resultCode, data);
		Intent intentResult = null;
		switch(resultCode)
		{
		case PelconnerActivity.ACTIVITY_REQUEST_CODE_CAMERA_REGULAR:
			//Toast.makeText(this, "ACTIVITY_REQUEST_CODE_GALLERY_EXTENDED", Toast.LENGTH_SHORT).show();
			break;
		case PelconnerActivity.ACTIVITY_REQUEST_CODE_CAMERA_EXTENDED:
			//Toast.makeText(this, "ACTIVITY_REQUEST_CODE_CAMERA_EXTENDED", Toast.LENGTH_SHORT).show();
			break;
		case PelconnerSaveActivity.RESULT_CODE_IMPORT:
			if(_byteSnapshot != null)
			{
				if(_type == Type.EXTENDED) // Called from Main activity; Set the result, and start Picture activity
				{
		        	// Set the response code/data and finish with this activity
					intentResult = new Intent(PelconnerCameraActivity.this, PelconnerPictureActivity.class);
					intentResult.putExtra(PelconnerActivity.ACTIVITY_RESPONSE_SNAPSHOT_BYTES, _byteSnapshot);
					setResult(RESULT_OK, intentResult);
					startActivity(intentResult);
					finish();
				}
				else // Called from Picture activity; Only return the result.
				{
		        	// Set the response code/data and finish with this activity
					intentResult = new Intent(PelconnerCameraActivity.this, PelconnerPictureActivity.class);
		        	intentResult.putExtra(PelconnerActivity.ACTIVITY_RESPONSE_SNAPSHOT_BYTES, _byteSnapshot);
					setResult(RESULT_OK, intentResult);
					finish();				
				}
			}
			else
			{
				setResult(RESULT_CANCELED);
				finish();					
			}			
			break;
		case PelconnerSaveActivity.RESULT_CODE_SAVE:
			if(_byteSnapshot != null)
			{
	        	// Set the response code/data and finish with this activity
				
				intentResult = new Intent(PelconnerCameraActivity.this, PelconnerSaveActivity.class);
				intentResult.putExtra(PelconnerActivity.ACTIVITY_RESPONSE_SNAPSHOT_BYTES, _byteSnapshot);
				setResult(RESULT_OK, intentResult);
				startActivity(intentResult);
				finish();
				
				// Using dialog instead of activity
				/*_dialogSave = new PelconnerSaveDialog(this, _bitmapSnapshot); 
				_dialogSave.setOnDismissListener(this);
				_dialogSave.show();*/
			}
			else
			{
				setResult(RESULT_CANCELED);
				finish();					
			}
			break;
		case PelconnerSaveActivity.RESULT_CODE_SHARE:
			if(_byteSnapshot != null)
			{
	        	// Set the response code/data and finish with this activity
				/*intentResult = new Intent(PelconnerCameraActivity.this, PelconnerShareActivity.class);
				intentResult.putExtra(PelconnerActivity.ACTIVITY_RESPONSE_SNAPSHOT_BYTES, _byteSnapshot);
				setResult(RESULT_OK, intentResult);
				startActivity(intentResult);*/
				
				shareIt(_byteSnapshot);
				
				//finish();
				_camera.startPreview();
			}
			else
			{
				setResult(RESULT_CANCELED);
				finish();					
			}
			break;
		case RESULT_CANCELED:
			setResult(RESULT_CANCELED);
			//_camera.startPreview();
			break;
		case RESULT_OK:
			// Only reached by clicking on Save button on Save dialog
			//_camera = Camera.open();
			_camera.startPreview();	
			//,m,m,
			break;
		default:
			setResult(RESULT_CANCELED);
			_camera.startPreview();	
			break;
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		// TODO Return to parent activity with result RESULT_CANCELED if no snapshot was taken
		if(_byteSnapshot/*_bitmapSnapshot*/ != null)
		{
			// TODO: Open a dialog stating "You have taken a snapshot, do you want to discard it?", with answers Yes/No
		}
		
		super.onBackPressed();
		finish();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.camera, menu);
		
		return true;
	}*/

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	/*@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch(itemId)
		{
		case R.id.camera_menu_item_snapshot:
			// Take snapshot
			takeSnapshot();
			return true;
		case R.id.camera_menu_item_cancel:
			// Return RESULT_CANCELED to calling activity (either Main or Gallery).
			setResult(RESULT_CANCELED);
			finish();
		case R.id.camera_menu_item_help:
			// Start Help activity/dialog
			startActivity(new Intent(PelconnerCameraActivity.this, PelconnerHelpActivity.class));
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);	
		}
	}*/

	@Override
	public void onClick(View sender) {
		if(sender == _buttonSnapshot)
		{
			takeSnapshot();
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO When touched, a snapshot should be taken.
		takeSnapshot();
		return true;//super.onTouchEvent(event);
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		//Log.d(TAG, "onPictureTaken called!");
		
		_byteSnapshot = data;
		
		Intent intent = new Intent(this, PelconnerSaveActivity.class);
		// Starting Snapshot dialog
		if(_type == Type.REGULAR)
		{
			intent.putExtra(PelconnerActivity.ACTIVITY_REQUEST_CODE, PelconnerActivity.ACTIVITY_REQUEST_CODE_CAMERA_REGULAR);
			intent.putExtra(PelconnerActivity.ACTIVITY_REQUEST_CODE_ORIENTATION, _initialOrientation);
			intent.putExtra(PelconnerActivity.ACTIVITY_RESPONSE_SNAPSHOT_BYTES, _byteSnapshot);
			startActivityForResult(intent, PelconnerActivity.ACTIVITY_REQUEST_CODE_CAMERA_REGULAR);
		}
		else
		{
			intent.putExtra(PelconnerActivity.ACTIVITY_REQUEST_CODE, PelconnerActivity.ACTIVITY_REQUEST_CODE_CAMERA_EXTENDED);
			intent.putExtra(PelconnerActivity.ACTIVITY_REQUEST_CODE_ORIENTATION, _initialOrientation);
			intent.putExtra(PelconnerActivity.ACTIVITY_RESPONSE_SNAPSHOT_BYTES, _byteSnapshot);
			startActivityForResult(intent, PelconnerActivity.ACTIVITY_REQUEST_CODE_CAMERA_EXTENDED);			
		}
		
		//startActivity(new Intent(PelconnerCameraActivity.this, PelconnerSaveActivity.class));
		
		/*
		_byteSnapshot = data;
		_bitmapSnapshot = BitmapFactory.decodeByteArray(data, 0, data.length);

		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		showDialog(DIALOG_SNAPSHOT_ID);
		*/
		
		
		// Moved to onCreateDialog!
		/*
		_dialogSnapshot = new PelconnerSnapshotDialog(this, _type, _bitmapSnapshot); 
		_dialogSnapshot.setOnDismissListener(this);
		
		_dialogSnapshot.show();
		*/

	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		//return super.onCreateDialog(id);
		//PelconnerDialog pelconnerDialog = null;
		switch(id)
		{
		case DIALOG_SNAPSHOT_ID:
			
			_dialogSnapshot = new PelconnerSnapshotDialog(this, _type, _bitmapSnapshot); 
			_dialogSnapshot.setOnDismissListener(this);
			
			_dialogSnapshot.show();
			return _dialogSnapshot;
		case DIALOG_SAVE_ID:
			break;
		default:
			// Not implemented!	
		}
		return null;
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		
		// Try to open the Camera
		try
		{
			_camera = Camera.open();
		}
    	catch(RuntimeException e)
    	{
    		Toast.makeText(this, R.string.camera_unavailable, Toast.LENGTH_LONG).show();
    		setResult(RESULT_CANCELED);
    		finish();
    		return;
    	}
		
    	try 
    	{
    		
    		//_camera.setPreviewDisplay(surfaceHolder);
    		
    		Camera.Parameters parameters = _camera.getParameters();
    		boolean rotated = false;
    		if(this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
    		{
    			// This is an undocumented although widely known feature
    			parameters.set("orientation", "portrait");
    			//parameters.setPreviewSize(30, 30);
    			//parameters.setJpegQuality(100);
    			
    			// For Android 2.2 and above
    			// camera.setDisplayOrientation(90);
    			
    			// For Android 2.0 and above
    			//parameters.setRotation(90); // rotate for 90 degrees
    			//parameters.set("rotation", 90);
    			rotated = true;
    		}
    		else
    		{
    			// This is an undocumentted although widely known feature
    			parameters.set("orientation", "landscape");
    			
    			// For Android 2.2 and above
    			// camera.setDisplayOrientation(0);
    			
    			// For Android 2.0 and above
    			parameters.setRotation(0);
    		}
    		
    		// Color effects
    		/**
    		 * NOTE At this moment, no color effects are going to take place.
    		 */
    		/*
    		List<String> colorEffects = parameters.getSupportedColorEffects();
    		if(colorEffects != null)
    		{
	    		Iterator<String> cei = colorEffects.iterator();
	    		while(cei.hasNext())
	    		{
	    			String currentEffect = cei.next();
	    			Log.v("SNAPSHOT", "Checking " + currentEffect);
	    			if(currentEffect.equals(Camera.Parameters.EFFECT_SOLARIZE))
	    			{
	    				Log.v("SNAPSHOT", "Using SOLARIZE");
	    				parameters.setColorEffect(Camera.Parameters.EFFECT_SOLARIZE);
	    				break;
	    			}
	    		}
	    		Log.v("SNAPSHOT", "Using Effect: " + parameters.getColorEffect());
    		}
    		else
    		{
    			Toast.makeText(this, "Color effects not supported!", Toast.LENGTH_SHORT).show();
    		}
    		*/
    		
    		
    		Display display = getWindowManager().getDefaultDisplay();
    		int maxHeight = display.getHeight();
    		int maxWidth = display.getWidth();
    		
    		// Sizes
    		int bestWidth = 0;
    		int bestHeight = 0;

    		List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
    		if(previewSizes != null && previewSizes.size() > 1)
    		{
    			Iterator<Camera.Size> cei = previewSizes.iterator();
    			while(cei.hasNext())
    			{
    				Camera.Size aSize = cei.next();
    				//Log.v("SNAPSHOT", "Checking " + aSize.width + " x " + aSize.height);
    				
    				if((aSize.width > bestWidth && aSize.height > bestHeight) && 
    						rotated ? (aSize.height <= maxWidth && aSize.width <= maxHeight) : (aSize.width <= maxWidth && aSize.height <= maxHeight))
    				{
    					// So far it is the biggest without going over the screen dimensions
    					if(rotated)
    					{
    						bestWidth = aSize.height;
    						bestHeight = aSize.width;    						
    					}
    					else
    					{
    						bestWidth = aSize.width;
    						bestHeight = aSize.height;
    					}
    					
    					if(bestWidth == maxWidth && bestHeight == maxHeight)
    						break;
    				}				
    			}
    			if(bestHeight != 0 && bestWidth != 0)
    			{
    				//Log.v("SNAPSHOT", "Using " + bestWidth + " x " + bestHeight);
    				//parameters.setPreviewSize(bestWidth, bestHeight);
    				if(rotated)
    				{
    					//_frameLayoutCamera.setLayoutParams(new FrameLayout.LayoutParams(bestHeight, bestWidth));
    					//_frameLayoutCamera.requestLayout();
    					_cameraView.setLayoutParams(new /*LinearLayout*/FrameLayout.LayoutParams(bestHeight, bestWidth));
    					
    				}
    				else
    				{
    					_cameraView.setLayoutParams(new /*LinearLayout*/FrameLayout.LayoutParams(bestWidth, bestHeight));
    				}
    				
    			}
    		}
    		

			//parameters.setPreviewSize(bestHeight, bestWidth);
			//_cameraView.setLayoutParams(new /*LinearLayout*/FrameLayout.LayoutParams(bestWidth, bestHeight));
			

    		_camera.setParameters(parameters);
    		
    		_camera.setPreviewDisplay(surfaceHolder);

    		_camera.startPreview();	
		} 
    	catch(IOException e) 
    	{
    		Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
    		_camera.release();
		}
	}

	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		if(_camera != null) // Camera Service was available and Camera.open() passed
		{
	    	_camera.stopPreview();
	    	_camera.release();
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) 
	{
		// TODO: Read the selected option and perform appropriate action!
		
		String tag = ((PelconnerDialog)dialog).getTag();
		if(tag.equalsIgnoreCase(PelconnerDialog.DIALOG_TAG_SNAPSHOT))
		{
			PelconnerSnapshotDialog dialogSnapshot = (PelconnerSnapshotDialog)dialog;
			PelconnerSnapshotDialog.Option selectedOption = dialogSnapshot.getSelectedOption(); 
			_dialogSnapshot = null; // Signaling that we've finished with this dialog
			Intent intentResult = null;
			switch(selectedOption)
			{
			case IMPORT:
	        	
				if(_byteSnapshot != null)
				{
					if(_type == Type.EXTENDED) // Called from Main activity; Set the result, and start Picture activity
					{
			        	// Set the response code/data and finish with this activity
						intentResult = new Intent(PelconnerCameraActivity.this, PelconnerPictureActivity.class);
						intentResult.putExtra(PelconnerActivity.ACTIVITY_RESPONSE_SNAPSHOT_BYTES, _byteSnapshot);
						setResult(RESULT_OK, intentResult);
						startActivity(intentResult);
						finish();
					}
					else // Called from Picture activity; Only return the result.
					{
			        	// Set the response code/data and finish with this activity
						intentResult = new Intent(PelconnerCameraActivity.this, PelconnerPictureActivity.class);
			        	intentResult.putExtra(PelconnerActivity.ACTIVITY_RESPONSE_SNAPSHOT_BYTES, _byteSnapshot);
						setResult(RESULT_OK, intentResult);
						finish();				
					}
				}
				else
				{
					setResult(RESULT_CANCELED);
					finish();					
				}
				break;
			case SAVE:
				if(_byteSnapshot != null)
				{
		        	// Set the response code/data and finish with this activity
					
					intentResult = new Intent(PelconnerCameraActivity.this, PelconnerSaveActivity.class);
					intentResult.putExtra(PelconnerActivity.ACTIVITY_RESPONSE_SNAPSHOT_BYTES, _byteSnapshot);
					setResult(RESULT_OK, intentResult);
					startActivity(intentResult);
					finish();
					
					// Using dialog instead of activity
					
					/*_dialogSave = new PelconnerSaveDialog(this, _bitmapSnapshot); 
					_dialogSave.setOnDismissListener(this);
					_dialogSave.show();*/
				}
				else
				{
					setResult(RESULT_CANCELED);
					finish();					
				}
				break;
			case SHARE:
				if(_byteSnapshot != null)
				{
		        	// Set the response code/data and finish with this activity
					/*intentResult = new Intent(PelconnerCameraActivity.this, PelconnerShareActivity.class);
					intentResult.putExtra(PelconnerActivity.ACTIVITY_RESPONSE_SNAPSHOT_BYTES, _byteSnapshot);
					setResult(RESULT_OK, intentResult);
					startActivity(intentResult);*/
					
					shareIt(_byteSnapshot);
					
					//finish();
					_camera.startPreview();
				}
				else
				{
					setResult(RESULT_CANCELED);
					finish();					
				}
				break;
			case CANCEL:
				setResult(RESULT_CANCELED);
				_camera.startPreview();	
				break;
			case NONE:
				setResult(RESULT_CANCELED);
				_camera.startPreview();	
				break;
			}
		}
		else if(tag.equalsIgnoreCase(PelconnerDialog.DIALOG_TAG_SAVE))
		{
			PelconnerSaveDialog dialogSave = (PelconnerSaveDialog)dialog;
			ImageData imageData = dialogSave.getImageData();
			//PelconnerSnapshotDialog.Option selectedOption = dialogSave.getSelectedOption(); 
			_dialogSave = null; // Signaling that we've finished with this dialog
			if(imageData.getHeight() > 0 && imageData.getWidth() > 0)
			{
				// Save the image
				saveIt(imageData, _byteSnapshot);
			}
			_camera.startPreview();
		}
	}
	
	/**
	 * Captures the snapshot and opens up the Thumbnail/Import/Share dialog
	 */
	void takeSnapshot()
	{
		// TODO Check if this call is correct or some arguments should be corrected!
		_camera.takePicture(null, null, null, this);	
	}

	@Override
	public void onError(int error, Camera camera) {
		//Toast.makeText(this, "Error occurred! (" + error + ")", Toast.LENGTH_SHORT);
		// CAMERA_ERROR_SERVER_DIED = 100
		// CAMERA_ERROR_UNKNOWN = 1
	}
}
