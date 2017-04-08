package com.terrestrial.pelconner;

import com.terrestrial.pelconner.R;
import com.terrestrial.pelconner.dialogs.PelconnerAboutDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * 
 * @author vladimir.stankovic
 *
 */
public class PelconnerMainActivity extends PelconnerActivity implements OnClickListener 
{
	//private String TAG = "PelconnerMainActivity";
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//super.onBackPressed();
	}

	// Buttons
	private Button buttonNewPicture;
	private Button buttonCamera;
	private Button buttonGallery;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		TAG = "PelconnerMainActivity";
		
        // Removing title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.main);
        
        // Instantiate button objects
        buttonNewPicture = (Button)findViewById(R.id.buttonNewPicture);
        buttonCamera = (Button)findViewById(R.id.buttonCamera);
        buttonGallery = (Button)findViewById(R.id.buttonGallery);
        
        // Set OnClick listeners
        buttonNewPicture.setOnClickListener(this);
        buttonCamera.setOnClickListener(this);
        buttonGallery.setOnClickListener(this);
        
        PelconnerActivity.addReference(this);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.main, menu);
		changeMenuResource();
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
		case R.id.menu_item_new_picture:
			startPelconnerActivity(PelconnerActivity.Type.PICTURE);
			return true;
		case R.id.menu_item_camera:
			startPelconnerActivity(PelconnerActivity.Type.CAMERA);
			return true;
		case R.id.menu_item_gallery:
			startPelconnerActivity(PelconnerActivity.Type.GALLERY);
		case R.id.menu_item_save:
			/*_dialogSave = new PelconnerSaveDialog(this, _bitmapSnapshot); 
			_dialogSave.setOnDismissListener(this);
			_dialogSave.show();*/
			return true;
		case R.id.menu_item_share:
			//shareIt(_byteBitmap);
			return true;
		case R.id.menu_item_about:
			PelconnerAboutDialog aboutDialog = new PelconnerAboutDialog(this);
			aboutDialog.show();
			return true;
		case R.id.menu_item_help:
			startPelconnerActivity(PelconnerActivity.Type.HELP_MAIN);
			return true;
		case R.id.menu_item_quit:
			this.shutDown(true);
			return true;
		default:
			return super.onOptionsItemSelected(item);	
		}		
	}

	@Override
	public void onClick(View sender) {
		int senderId = sender.getId();
		switch(senderId)
		{
		case R.id.buttonNewPicture:
			//Toast.makeText(this, "New Picture", Toast.LENGTH_SHORT).show();
			startPelconnerActivity(PelconnerActivity.Type.PICTURE);
			break;
		case R.id.buttonCamera:
			//Toast.makeText(this, "Camera", Toast.LENGTH_SHORT).show();
			startPelconnerActivity(PelconnerActivity.Type.CAMERA);
			break;
		case R.id.buttonGallery:
			//Toast.makeText(this, "Gallery", Toast.LENGTH_SHORT).show();
			startPelconnerActivity(PelconnerActivity.Type.GALLERY);
			break;
		default:
			// Invalid case
		}
	}

	/* (non-Javadoc)
	 * @see com.android.pelconner.PelconnerActivity#onDismiss(android.content.DialogInterface)
	 */
	@Override
	public void onDismiss(DialogInterface dialog) {
		/*PelconnerSaveDialog dialogSave = (PelconnerSaveDialog)dialog;
		ImageData imageData = dialogSave.getImageData();
		//PelconnerSnapshotDialog.Option selectedOption = dialogSave.getSelectedOption(); 
		_dialogSave = null; // Signaling that we've finished with this dialog
		if(imageData.getHeight() > 0 && imageData.getWidth() > 0)
		{
			// Save the image
			saveIt(imageData, _byteSnapshot);
		}*/
		super.onDismiss(dialog);
	}
	
	void startPelconnerActivity(PelconnerActivity.Type activityType)
	{
		Intent intent = null;
		Context context = PelconnerMainActivity.this;
		boolean resultExpected = false;
		
		switch(activityType){
		case CAMERA:
			resultExpected = true;
			intent = new Intent(context, PelconnerCameraActivity.class);
			break;
		case GALLERY:
			resultExpected = true;
			intent = new Intent(context, PelconnerGalleryActivity.class);
			break;
		case PICTURE:
			intent = new Intent(context, PelconnerPictureActivity.class);
			break;
		case HELP_MAIN:
		//case HELP_PICTURE:
			resultExpected = true; // Not actually, but we need to differ types of Help activity
			intent = new Intent(context, PelconnerHelpActivity.class);
			break;
		default:
		}
		
		if(intent != null)
		{
			if(resultExpected)
			{
				if(activityType == PelconnerActivity.Type.GALLERY)
				{
					// Starting Gallery without support for share
					intent.putExtra(PelconnerActivity.ACTIVITY_REQUEST_CODE, PelconnerActivity.ACTIVITY_REQUEST_CODE_GALLERY_EXTENDED);
					startActivityForResult(intent, PelconnerActivity.ACTIVITY_REQUEST_CODE_GALLERY_EXTENDED);
				}
				else if(activityType == PelconnerActivity.Type.CAMERA)
				{
					// Starting Camera
					intent.putExtra(PelconnerActivity.ACTIVITY_REQUEST_CODE, PelconnerActivity.ACTIVITY_REQUEST_CODE_CAMERA_EXTENDED);
					startActivityForResult(intent, PelconnerActivity.ACTIVITY_REQUEST_CODE_CAMERA_EXTENDED);
				}
				else if(activityType == PelconnerActivity.Type.HELP_MAIN/* || activityType == PelconnerActivity.Type.HELP_PICTURE*/)
				{					
					if(activityType == PelconnerActivity.Type.HELP_MAIN)
					{
						intent.putExtra(PelconnerActivity.ACTIVITY_HELP_TYPE, PelconnerHelpActivity.Type.MAIN);
					}
					/*else if(activityType == PelconnerActivity.Type.HELP_PICTURE)
					{
						intent.putExtra(PelconnerActivity.ACTIVITY_HELP_TYPE, PelconnerHelpActivity.Type.PICTURE);
					}*/
					
					startActivity(intent);
				}
			}
			else
			{
				startActivity(intent);
			}
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//super.onActivityResult(requestCode, resultCode, data);
		switch(resultCode)
		{
		case PelconnerActivity.ACTIVITY_REQUEST_CODE_GALLERY_EXTENDED:
			//Toast.makeText(this, "ACTIVITY_REQUEST_CODE_GALLERY_EXTENDED", Toast.LENGTH_SHORT).show();
			break;
		case PelconnerActivity.ACTIVITY_REQUEST_CODE_CAMERA_EXTENDED:
			//Toast.makeText(this, "ACTIVITY_REQUEST_CODE_CAMERA_EXTENDED", Toast.LENGTH_SHORT).show();
			break;
		case RESULT_OK:
			//Toast.makeText(this, "RESULT_OK", Toast.LENGTH_SHORT).show();
			break;
		case RESULT_CANCELED:
		default:
			break;
		}
	}
}
