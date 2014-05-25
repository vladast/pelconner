package com.terrestrial.pelconner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;

import com.terrestrial.pelconner.R;
import com.terrestrial.pelconner.dialogs.PelconnerAlertDialog;
import com.terrestrial.pelconner.dialogs.PelconnerDialog;
import com.terrestrial.pelconner.helper.ImageData;
import com.terrestrial.pelconner.helper.ImageSaverThread;
import com.terrestrial.pelconner.helper.Locator;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.LayoutInflater.Factory;

public class PelconnerActivity extends Activity implements OnDismissListener {
	
	protected String TAG = "PelconnerActivity";
	
	// Phone capabilities definitions
	protected final String PHONE_CAPABILITIES = "PhoneCapabilities";
	protected final String PHONE_CAPABILITIES_GPS = "GPS"; // boolean
	protected final String PHONE_CAPABILITIES_ACCELERATOR = "Accelerator"; // boolean
	//protected final String PHONE_CAPABILITIES_COMPASS = "Compass"; // boolean
	protected final String PHONE_CAPABILITIES_NETWORK = "Network"; // boolean
	protected final String PHONE_CAPABILITIES_CAMERA = "Camera"; // boolean
	protected final String PHONE_CAPABILITIES_SDCARD = "SdCard"; // boolean
	protected final String PHONE_CAPABILITIES_SCREEN_WIDTH = "ScreenWidth"; // integer
	protected final String PHONE_CAPABILITIES_SCREEN_HEIGHT = "ScreenHeight"; // integer
	
	/**
	 * Used while starting activities for result
	 */
	protected static final String ACTIVITY_REQUEST_CODE = "requestCode";
	protected static final String ACTIVITY_REQUEST_CODE_ORIENTATION = "requestCodeOrientation";
	//protected static final String ACTIVITY_REQUEST_CODE_DATA = "requestCodeData";
	protected static final String ACTIVITY_RESPONSE_SELECTED_IMAGE = "responseSelectedImage";
	protected static final String ACTIVITY_RESPONSE_SNAPSHOT_BYTES = "responseSnapshotBytes";
	protected static final String ACTIVITY_HELP_TYPE = "helpType";
	protected static final int ACTIVITY_REQUEST_CODE_GALLERY_REGULAR = 1;
	protected static final int ACTIVITY_REQUEST_CODE_GALLERY_EXTENDED = 2;
	protected static final int ACTIVITY_REQUEST_CODE_CAMERA_REGULAR = 3;
	protected static final int ACTIVITY_REQUEST_CODE_CAMERA_EXTENDED = 4;
	protected static final int ACTIVITY_REQUEST_CODE_COLOR = 5;
	
	/**
	 * Class tags
	 * @author vladimir.stankovic
	 *
	 */
	protected static final String ACTIVITY_TAG_MAIN = "PelconnerMainActivity";
	protected static final String ACTIVITY_TAG_PICTURE = "PelconnerPictureActivity";
	protected static final String ACTIVITY_TAG_CAMERA = "PelconnerCamereActivity";
	protected static final String ACTIVITY_TAG_GALLERY = "PelconnerGalleryActivity";
	protected static final String ACTIVITY_TAG_COLOR = "PelconnerColorActivity";
	protected static final String ACTIVITY_TAG_HELP = "PelconnerHelpActivity";
	
	public static enum Type {
		MAIN,
		PICTURE,
		CAMERA,
		GALLERY,
		COLOR,
		SAVE,
		SHARE,
		HELP_MAIN,
		HELP_PICTURE
	};
	
	/**
	 * Reference counting list
	 */
	public static ArrayList<PelconnerActivity> _listOfActivities;
	
	/**
	 * Adding referenced object to the list of references
	 * @param reference
	 * Referenced object to add
	 */
	public static void addReference(PelconnerActivity reference)
	{
		// TODO: Add a logic to check if two same objects of the same type are stored.
		// If so, remove the old one.
		
		if(_listOfActivities == null)
		{
			_listOfActivities = new ArrayList<PelconnerActivity>();
		}
		
		boolean isAlreadyIn = false;
		for(int i = 0; i < _listOfActivities.size(); ++i)
		{
			if(reference.TAG.equalsIgnoreCase(_listOfActivities.get(i).TAG))
			{
				isAlreadyIn = true;
				_listOfActivities.set(i, reference);
				break;
			}
		}
		
		if(!isAlreadyIn)
		{
			Log.d("PelconnerActivity", "Added activity with TAG '" + reference.TAG + "'");
			//_listOfActivities.add(reference);
		}
	}
	
	/**
	 * Removing the referenced object from the list of references
	 * @param reference
	 * Referenced object to remove
	 * @return
	 * <i>true</i> if the removal was successful 
	 */
	public static boolean removeReference(PelconnerActivity reference)
	{
		boolean bResult = false;
		if(_listOfActivities != null)
		{
			Log.d("PelconnerActivity", "Removing acitivity with TAG '" + reference.TAG + "'");
			bResult = _listOfActivities.remove(reference);
			if(bResult)
				Log.d("PelconnerActivity", "Removed successfully");
		}
		return bResult;
	}
	
	public static Intent getReference(String tag)
	{
		for(PelconnerActivity pelconnerActivity : _listOfActivities)
		{
			//Log.d("getReference", pelconnerActivity.TAG);
			if(tag.equalsIgnoreCase(pelconnerActivity.TAG))
			{
				//Log.d("getReference", "Found activity with tag: " + pelconnerActivity.TAG);
				return pelconnerActivity.getIntent();
			}
		}
		
		return new Intent();
	}
	
	/**
	 * Shuts down Pelconner
	 */
	public /*static*/ void shutDown(boolean askBeforeQuiting)
	{
		if(askBeforeQuiting)
		{
			// TODO: Open up the "Are you sure?" dialog
			PelconnerAlertDialog dialogShutDown = new PelconnerAlertDialog(this, R.drawable.ic_dialog_alert_shutdown, 
					R.string.dialog_title_shutdown, R.string.dialog_text_shutdown);
			
			dialogShutDown.setOnDismissListener(this);
			
			dialogShutDown.show();
			
			// TODO: If "dirty" flag is set, open up "Do you want to save changes?" dialog
		}
		else
		{
			if(_listOfActivities != null && _listOfActivities.size() > 0)
			{
				for(int i = 0; i < _listOfActivities.size(); ++i)
				{
					_listOfActivities.get(i).finish();
				}
			}
			
			System.exit(0);
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) 
	{
		if(((PelconnerAlertDialog)dialog).getAnswer() == PelconnerDialog.Answer.YES)
		{
			if(_listOfActivities != null && _listOfActivities.size() > 0)
			{
				for(int i = 0; i < _listOfActivities.size(); ++i)
				{
					_listOfActivities.get(i).finish();
				}
			}
			
			System.exit(0);
		}
	}
	
	Uri convertContentToFileUri(Uri uriContent)
	{
		String stringExternalMedia = Media.EXTERNAL_CONTENT_URI.toString();
		String stringContent = uriContent.toString();
		stringContent = stringContent.replace(stringExternalMedia + "/", "");
		
		String[] whereValues = {
				stringContent
		};
		
		String[] columns = {
				Media.DATA
		};
		Cursor cursor = this.managedQuery(Media.EXTERNAL_CONTENT_URI, columns, Media._ID + "= ?", whereValues, null);
		
		//int cursorIndexId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
		int cursorIndexFileColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		
		if(cursor.moveToFirst())
		{
			return Uri.parse(cursor.getString(cursorIndexFileColumn));
		}
		
		return Uri.EMPTY;
	}
	
	/**
	 * Share image with other applications on the phone.
	 * @param data
	 * Image byte representation
	 */
	public void shareIt(byte[] data)
	{
		ContentValues contentValues = new ContentValues(8);
		String imageName = getString(R.string.app_name) + '_' + (new Date()).toGMTString().replace(' ', '-').replace(':', '_');
		contentValues.put(Media.DISPLAY_NAME, imageName); 
		contentValues.put(Media.TITLE, getString(R.string.app_name));
		contentValues.put(Media.DESCRIPTION, getString(R.string.media_description));
		Display display = getWindowManager().getDefaultDisplay();
		contentValues.put(Media.ORIENTATION, display.getOrientation());
		contentValues.put(Media.DATE_TAKEN, (new Date()).getTime());
		//String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + 
		//	"Pelconner" + "/" + String.valueOf(System.currentTimeMillis()) + ".jpg";
		
		//contentValues.put(Media.DATA, filePath);
		Locator locator = new Locator(this);
		Location location = locator.getCurrentLocation();
		if(location != null)
		{
			contentValues.put(Media.LATITUDE, location.getLatitude());
			contentValues.put(Media.LONGITUDE, location.getLongitude());
		}
		contentValues.put(Media.MIME_TYPE, "image/jpeg");
		
		// THIS CAUSES INVALID STATE EXCEPTION ON INSERT!
		//contentValues.put(Media.DATA, _byteSnapshot);
		
		Uri imageFileUri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, contentValues);
		
		Uri imageFileUriChanged = convertContentToFileUri(imageFileUri);
		
		String imagePath = "file://" + imageFileUriChanged.getPath();
		
		try
		{
			OutputStream imageFileOS = getContentResolver().openOutputStream(imageFileUri);
			
			imageFileOS.write(data);
			
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageFileOS);			
			
			imageFileOS.flush();
			imageFileOS.close();
		}
		catch(FileNotFoundException e)
		{
			// TODO: Implement exception handling
			//Log.e("ERR", e.getMessage());
		}
		catch(IOException e)
		{
			// TODO: Implement exception handling
			//Log.e("ERR", e.getMessage());
		}
		
		Intent intent = new Intent(Intent.ACTION_SEND); 
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setType("image/*"); 
		intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + imagePath)); 
		
		startActivity(Intent.createChooser(intent, getString(R.string.share_image)));		
	}
	
	void shareIt(Uri imageUri)
	{
		/*ContentValues contentValues = new ContentValues(8);
		String imageName = getString(R.string.app_name) + '_' + (new Date()).toGMTString().replace(' ', '-').replace(':', '_');
		contentValues.put(Media.DISPLAY_NAME, imageName); 
		contentValues.put(Media.TITLE, getString(R.string.app_name));
		contentValues.put(Media.DESCRIPTION, getString(R.string.media_description));
		Display display = getWindowManager().getDefaultDisplay();
		contentValues.put(Media.ORIENTATION, display.getOrientation());
		contentValues.put(Media.DATE_TAKEN, (new Date()).getTime());
		Locator locator = new Locator(this);
		Location location = locator.getCurrentLocation();
		if(location != null)
		{
			contentValues.put(Media.LATITUDE, location.getLatitude());
			contentValues.put(Media.LONGITUDE, location.getLongitude());
		}
		contentValues.put(Media.MIME_TYPE, "image/jpeg");
		
		getContentResolver().update(imageUri, contentValues, null, null);*/
		
		Intent intent = new Intent(Intent.ACTION_SEND);
		
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + imageUri.toString())); 
		intent.setType("image/*");
		
		startActivity(Intent.createChooser(intent, getString(R.string.share_image)));	
	}
	
	/**
	 * Save the image on the phone
	 * @param imageData
	 * Instance of ImageData class containing specific data about the image
	 * @param data
	 * Image byte representation
	 */
	void saveIt(ImageData imageData, byte[] data)
	{
		// TODO Resize the image if requested!
		Display display = getWindowManager().getDefaultDisplay();
		ImageSaverThread imageSaverThread = new ImageSaverThread(this, imageData, data, display.getOrientation());
		imageSaverThread.start();
		imageSaverThread.isAlive();
	}
	
	protected void changeMenuResource()
	{
		if(getLayoutInflater().getFactory() == null)
		{
			getLayoutInflater().setFactory(new Factory(){
				@Override
				public View onCreateView(String name, Context context, AttributeSet attrs) {
					Log.d("Menu", name);
					/*if(name.equalsIgnoreCase("com.android.internal.view.menu.IconMenuView"))
					{
						try
						{
							LayoutInflater li = getLayoutInflater();
							final View view = li.createView(name, null, attrs);
							
							new Handler().post(new Runnable(){
								public void run(){
									view.setBackgroundColor(Color.BLACK);
								}
							});
							return view;
						}
						catch(InflateException e) {}
						catch(ClassNotFoundException e) {}						
					}
					else*/ if(name.equalsIgnoreCase("com.android.internal.view.menu.IconMenuItemView"))
					{
						try
						{
							LayoutInflater li = getLayoutInflater();
							final View view = li.createView(name, null, attrs);
							
							new Handler().post(new Runnable(){
								public void run(){
									view.setBackgroundColor(Color.BLACK);
									view.setBackgroundResource(R.drawable.button_menu_option);
								}
							});
							return view;
						}
						catch(InflateException e) {}
						catch(ClassNotFoundException e) {}
					}
					
					return null;
				}
				
			});
		}
	}
}
