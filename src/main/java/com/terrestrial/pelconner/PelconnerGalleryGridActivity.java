package com.terrestrial.pelconner;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class PelconnerGalleryGridActivity extends PelconnerActivity implements OnItemClickListener {

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
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		/*if(_selectedImage.getImageFileUri() != Uri.EMPTY)
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
		}		*/
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#finish()
	 */
	@Override
	public void finish() {// Remove the reference from the list
		boolean bResult = PelconnerActivity.removeReference(PelconnerGalleryGridActivity.this);
		
		if(!bResult)
		{
			//Log.e(TAG, "Reference not removed from the list!");
		}
		
        // Finishing with this activity
		super.finish();
	}
	
}
