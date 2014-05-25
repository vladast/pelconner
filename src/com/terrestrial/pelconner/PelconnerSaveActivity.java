package com.terrestrial.pelconner;

import com.terrestrial.pelconner.R;
import com.terrestrial.pelconner.dialogs.PelconnerDialog;
import com.terrestrial.pelconner.dialogs.PelconnerOptionDialog;
import com.terrestrial.pelconner.dialogs.PelconnerSaveDialog;
import com.terrestrial.pelconner.helper.ImageData;
import com.terrestrial.pelconner.helper.PelconnerOption;
import com.terrestrial.pelconner.helper.PelconnerOption.Availables;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class PelconnerSaveActivity extends PelconnerActivity implements	OnDismissListener {

	public static final int RESULT_CODE_IMPORT = 100;
	public static final	int RESULT_CODE_SAVE = 101;
	public static final int RESULT_CODE_SHARE = 102;
	
	private LinearLayout _linearLayoutSnapshot = null;
	private PelconnerOptionDialog _pelconnerOptionDialog = null;
	
	private byte[] _byteSnapshot = null;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		
		TAG = "PelconnerSaveActivity";
        
        // Removing title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.save_activity);
        
        _linearLayoutSnapshot = (LinearLayout)findViewById(R.id.linearLayoutSnapshot);
        
        _linearLayoutSnapshot.setBackgroundColor(Color.BLACK/*android.R.color.transparent*/);
        
        Bundle bundleExtras = this.getIntent().getExtras();
        if(bundleExtras != null)
        {
            int receivedRequestCodeOrientation = -1000;
            receivedRequestCodeOrientation = bundleExtras.getInt(PelconnerActivity.ACTIVITY_REQUEST_CODE_ORIENTATION);
            if(receivedRequestCodeOrientation != -1000)
            {
            	// Request the orientation
            	setRequestedOrientation(/*ActivityInfo.SCREEN_ORIENTATION_PORTRAIT*/receivedRequestCodeOrientation);
            }
        }
        
        PelconnerActivity.addReference(this);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
        Bundle bundleExtras = this.getIntent().getExtras();
        if(bundleExtras != null) // Extras received from Gallery
        {
            _pelconnerOptionDialog = new PelconnerOptionDialog(this);
            _pelconnerOptionDialog.setOnDismissListener(this);
            _pelconnerOptionDialog.setTitle(R.string.dialog_camera_title);
        	
            int receivedRequestCode = -1; 
            receivedRequestCode = bundleExtras.getInt(PelconnerActivity.ACTIVITY_REQUEST_CODE);
            
            if(receivedRequestCode != -1)
            {
            	if(receivedRequestCode == PelconnerActivity.ACTIVITY_REQUEST_CODE_CAMERA_REGULAR)
            	{
                    _pelconnerOptionDialog.add(Availables.SNAPSHOT_IMPORT);
                    _pelconnerOptionDialog.add(Availables.SNAPSHOT_SAVE);
                    _pelconnerOptionDialog.add(Availables.SNAPSHOT_CANCEL);
            	}
            	else if(receivedRequestCode == PelconnerActivity.ACTIVITY_REQUEST_CODE_CAMERA_EXTENDED)
            	{
                    _pelconnerOptionDialog.add(Availables.SNAPSHOT_IMPORT);
                    _pelconnerOptionDialog.add(Availables.SNAPSHOT_SAVE);
                    _pelconnerOptionDialog.add(Availables.SNAPSHOT_SHARE);
                    _pelconnerOptionDialog.add(Availables.SNAPSHOT_CANCEL);
            	}
            }
            
    		_pelconnerOptionDialog.show();
        }
	}

	/* (non-Javadoc)
	 * @see com.android.pelconner.PelconnerActivity#onDismiss(android.content.DialogInterface)
	 */
	@Override
	public void onDismiss(DialogInterface dialog) {
		
		String tag = ((PelconnerDialog)dialog).getTag();
		
		if(tag.equalsIgnoreCase(PelconnerDialog.DIALOG_TAG_OPTION))
		{
		
			PelconnerOption selectedOption = _pelconnerOptionDialog.getSelectedOption();
			Intent intentResult = null;
			if(selectedOption.getOption() == PelconnerOption.Availables.SNAPSHOT_IMPORT)
			{
				intentResult = new Intent(PelconnerSaveActivity.this, PelconnerCameraActivity.class);
				setResult(RESULT_CODE_IMPORT, intentResult);
				//startActivity(intentResult);
				finish();
			}
			else if(selectedOption.getOption() == PelconnerOption.Availables.SNAPSHOT_SAVE)
			{
				/*
				intentResult = new Intent(PelconnerSaveActivity.this, PelconnerCameraActivity.class);
				setResult(RESULT_CODE_SAVE, intentResult);
				//startActivity(intentResult);
				finish();
				*/
				
				
		        Bundle bundleExtras = this.getIntent().getExtras();
		        if(bundleExtras != null)
		        {
		            _byteSnapshot = bundleExtras.getByteArray(PelconnerActivity.ACTIVITY_RESPONSE_SNAPSHOT_BYTES);
		        }
				
				Bitmap bitmap = BitmapFactory.decodeByteArray(_byteSnapshot, 0, _byteSnapshot.length);
				
				PelconnerSaveDialog dialogSave = new PelconnerSaveDialog(this, bitmap);
				dialogSave.setOnDismissListener(this);
				dialogSave.show();
			}
			else if(selectedOption.getOption() == PelconnerOption.Availables.SNAPSHOT_SHARE)
			{
				intentResult = new Intent(PelconnerSaveActivity.this, PelconnerCameraActivity.class);
				setResult(RESULT_CODE_SHARE, intentResult);
				//startActivity(intentResult);
				finish();
			}
			else if(selectedOption.getOption() == PelconnerOption.Availables.SNAPSHOT_CANCEL)
			{
				intentResult = new Intent(PelconnerSaveActivity.this, PelconnerCameraActivity.class);
				setResult(RESULT_CANCELED, intentResult);
				//startActivity(intentResult);
				finish();
			}
		}
		else if(tag.equalsIgnoreCase(PelconnerDialog.DIALOG_TAG_SAVE))
		{
			PelconnerSaveDialog dialogSave = (PelconnerSaveDialog)dialog;
			ImageData imageData = dialogSave.getImageData();
			if(imageData.getHeight() > 0 && imageData.getWidth() > 0 && _byteSnapshot != null)
			{
				if(imageData.isSaveLocation())
				{
					LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
					if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
					{
						Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivity(intent);
					}
				}
				
				// Save the image
				saveIt(imageData, _byteSnapshot);
			}
			
			Intent intentResult = new Intent(PelconnerSaveActivity.this, PelconnerCameraActivity.class);
			
			// CameraActivity does not have the implementation for RESULT_OK --> it will be detected as RESULT_CANCEL, which is Ok
			// because we've already saved the image, and we just want to start the Camera once again for next snapshot
			setResult(RESULT_OK, intentResult); 
			//startActivity(intentResult);
			finish();
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
		/*boolean bResult = PelconnerActivity.removeReference(this);
		if(!bResult)
		{
			Log.e(TAG, "Reference not removed from the list!");
		}*/
		
        // Finishing with this activity
		super.finish();
	}

}
