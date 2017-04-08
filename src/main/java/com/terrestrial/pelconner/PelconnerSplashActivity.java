package com.terrestrial.pelconner;

import java.util.List;

import com.terrestrial.pelconner.R;
import com.terrestrial.pelconner.dialogs.PelconnerNosdcardDialog;
import com.terrestrial.pelconner.helper.ImageInfo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * 
 * @author vladimir.stankovic
 * 
 */
public class PelconnerSplashActivity extends PelconnerActivity implements OnDismissListener {

	//private final String TAG = "PelconnerSplashActivity";
	
	private SharedPreferences _PhoneCapabilities;
	
	private RelativeLayout _relativeLayoutContainer;
	
	private ImageView _imageViewSplash;
	private Animation _animationFadeIn;
	private Animation _animationFadeOut;
	
	private boolean _capabilitiesDetected;
	
	private Uri _uriReceivedImage;
	
	/**
	 * Detects phone capabilities and saves them in shared preferences object.
	 * When completes, switches to Main activity.
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TAG = "PelconnerSplashActivity";
        
        // Removing title        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.splash);
        
        _relativeLayoutContainer = (RelativeLayout)findViewById(R.id.relativeLayoutContainer);
        _imageViewSplash = (ImageView)findViewById(R.id.imageViewSplash);
        
        _relativeLayoutContainer.setBackgroundColor(android.R.color.transparent);
        
        _capabilitiesDetected = false;
        
        PelconnerActivity.addReference(this);
        
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        if(action != null && action.equalsIgnoreCase(Intent.ACTION_SEND))
        {
        	_uriReceivedImage = (Uri)extras.getParcelable(Intent.EXTRA_STREAM);
        	
        }
    }
    
    /* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		fadeIn();
        
        /*
        readPhoneCapabilities();
        
        fadeOut();
        */
	}

	private void readPhoneCapabilities()
    {
    	//Log.d(TAG, "readPhoneCapabilities...");
    	
    	_PhoneCapabilities = getSharedPreferences(PHONE_CAPABILITIES, Context.MODE_PRIVATE);
        Editor editor = _PhoneCapabilities.edit();

        //editor.putBoolean(PHONE_CAPABILITIES_COMPASS, false);
        
        // Detect accelerometer
        editor.putBoolean(PHONE_CAPABILITIES_ACCELERATOR, hasAccelerometer());
                
        // Detect GPS
        editor.putBoolean(PHONE_CAPABILITIES_GPS, hasGPS());
        
        // Is network accessible
        editor.putBoolean(PHONE_CAPABILITIES_NETWORK, hasInternetConnection());
        
        // Is camera present
        editor.putBoolean(PHONE_CAPABILITIES_CAMERA, hasCamera());
        
        // Is SD card mounted
        editor.putBoolean(PHONE_CAPABILITIES_SDCARD, hasSD());
        
        DisplayMetrics displayMetrics = getDisplayMetrics();
        
        editor.putInt(PHONE_CAPABILITIES_SCREEN_WIDTH, displayMetrics.widthPixels);
        
        editor.putInt(PHONE_CAPABILITIES_SCREEN_HEIGHT, displayMetrics.heightPixels);
        
        editor.apply();
        
        _capabilitiesDetected = true;
    }
    
    boolean hasAccelerometer()
    {
        SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> listOfSensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        return (listOfSensors.size() > 0);    	
    }
    
    boolean hasGPS()
    {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return (locationManager != null);
    }
    
    boolean hasInternetConnection()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);     
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();     
        return (networkInfo != null); 
    }
    
    boolean hasCamera()
    {
    	// Assuming that camera is always present...
    	return true;
    }
    
    boolean hasSD()
    {
    	boolean bResult = false;
    	String extStorageState = Environment.getExternalStorageState();
    	if(extStorageState.equals(Environment.MEDIA_MOUNTED))
    	{
    		bResult = true;
    	}
    	return bResult;
    }
    
    DisplayMetrics getDisplayMetrics()
    {
    	DisplayMetrics metrics = new DisplayMetrics(); 
    	getWindowManager().getDefaultDisplay().getMetrics(metrics);  
    	return metrics;
    }
    
    private void fadeIn()
    {
    	//Log.d(TAG, "fadeIn...");

        _animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        
        _animationFadeIn.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation animation) {
				while(!_capabilitiesDetected)
				{
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//fadeOut();
				endSplash();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// Not used
			}

			@Override
			public void onAnimationStart(Animation animation) {
				readPhoneCapabilities();
				//PelconnerGalleryActivity.initializeImageList(PelconnerSplashActivity.this);
			}
        });
        
    	_imageViewSplash.startAnimation(_animationFadeIn);
    }
    
    /* (non-Javadoc)
	 * @see android.app.Activity#finish()
	 */
	@Override
	public void finish() {
		// Remove the reference from the list
		boolean bResult = PelconnerActivity.removeReference(PelconnerSplashActivity.this);
		
		if(!bResult)
		{
			//Log.e(TAG, "Reference not removed from the list!");
		}
		
        // Finishing with this activity
        //PelconnerSplashActivity.this.finish();
		super.finish();
	}

	private void endSplash()
	{
		if(!hasSD()) // TODO: ENABLE SAVING ON INTERNAL MEMORY (CHECK THE SIZE, AND THEN SAVE)!!!
		{
        	PelconnerNosdcardDialog pelconnerNosdcardDialog = new PelconnerNosdcardDialog(PelconnerSplashActivity.this);
        	pelconnerNosdcardDialog.setOnDismissListener(this);
        	pelconnerNosdcardDialog.show();
		}
		else
		{
			if(_uriReceivedImage != null && _uriReceivedImage != Uri.EMPTY)
			{
				Bundle bundle = new Bundle();
	        	bundle.putString(ImageInfo.URI, _uriReceivedImage.toString());
	        	bundle.putString(ImageInfo.TITLE, "");
	        	bundle.putString(ImageInfo.DISPLAY_NAME, "");
	        	bundle.putString(ImageInfo.DESCRIPTION, "");
	        	bundle.putDouble(ImageInfo.LATITUDE, 0);
	        	bundle.putDouble(ImageInfo.LONGITUDE, 0);
				
	        	Intent intentResult = new Intent(PelconnerSplashActivity.this, PelconnerPictureActivity.class);
	        		        	
	        	intentResult.putExtra(PelconnerActivity.ACTIVITY_RESPONSE_SELECTED_IMAGE, bundle);
				setResult(RESULT_OK, intentResult);
				startActivity(intentResult);
				finish();
			}
			else
			{
		        // Navigate to Main activity
				startActivity(new Intent(PelconnerSplashActivity.this, PelconnerMainActivity.class));			
			}
			
			finish();
		}	
	}
	
	private void fadeOut()
    {
    	//Log.d(TAG, "fadeOut...");
    	
		if(!hasSD())
		{
	        // If SD card is not mounted, quit.
	        if(!hasSD())
	        {
	        	PelconnerNosdcardDialog pelconnerNosdcardDialog = new PelconnerNosdcardDialog(PelconnerSplashActivity.this);
	        	pelconnerNosdcardDialog.setOnDismissListener(this);
	        	pelconnerNosdcardDialog.show();
	        }
		}
		else
		{
			_animationFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
	        
	        _animationFadeOut.setAnimationListener(new AnimationListener(){

				@Override
				public void onAnimationEnd(Animation animation) {
			        // Navigate to Main activity
					startActivity(new Intent(PelconnerSplashActivity.this, PelconnerMainActivity.class));
			        
					finish();
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// Not used
				}

				@Override
				public void onAnimationStart(Animation animation) {
					// Not used
				}
	        	
	        });
	    	
	    	_imageViewSplash.startAnimation(_animationFadeOut);			
		}
    }
	
	/* (non-Javadoc)
	 * @see com.android.pelconner.PelconnerActivity#onDismiss(android.content.DialogInterface)
	 */
	@Override
	public void onDismiss(DialogInterface dialog) {
		PelconnerSplashActivity.this.shutDown(false);
	}
}