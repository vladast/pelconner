package com.terrestrial.pelconner.dialogs;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.terrestrial.pelconner.R;
import com.terrestrial.pelconner.PelconnerCameraActivity;

public class PelconnerSnapshotDialog extends PelconnerDialog implements OnClickListener {

	/*
    <string name="dialog_camera_title">Camera Snapshot</string>
    <string name="dialog_camera_button_import">Import</string>
    <string name="dialog_camera_button_save">Save...</string>
    <string name="dialog_camera_button_share">Share</string>
    <string name="dialog_camera_button_cancel">Cancel</string>
	*/
	
	// Class fields
	ImageView _imageViewThumbnail;

	Button _buttonImport;
	Button _buttonSave;
	Button _buttonShare; // Exist only in EXTENDED version.
	Button _buttonCancel;
	PelconnerCameraActivity.Type _cameraType;
	Option _selectedOption;
	
	public static enum Option
	{
		NONE,
		IMPORT,
		SAVE,
		SHARE,
		CANCEL
	};

	public PelconnerSnapshotDialog(Context context, PelconnerCameraActivity.Type cameraType, Bitmap bitmap)
	{
		super(context);
		
		
		TAG = PelconnerDialog.DIALOG_TAG_SNAPSHOT;
		
		_context = context;
		_cameraType = cameraType;
		_selectedOption = Option.NONE;
		
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		if(_cameraType == PelconnerCameraActivity.Type.EXTENDED)
		{
			setContentView(R.layout.camera_snapshot_extended);
			
			_buttonShare = (Button)findViewById(R.id.buttonShare);
			_buttonShare.setOnClickListener(this);
		}
		else
		{
			setContentView(R.layout.camera_snapshot);
		}
		
		_imageViewThumbnail = (ImageView)findViewById(R.id.imageViewThumbnail);
		_buttonImport = (Button)findViewById(R.id.buttonImport);
		_buttonSave = (Button)findViewById(R.id.buttonSave);
		_buttonCancel = (Button)findViewById(R.id.buttonCancel);
		
		_buttonImport.setOnClickListener(this);
		_buttonSave.setOnClickListener(this);
		_buttonCancel.setOnClickListener(this);
		
		_imageViewThumbnail.setImageBitmap(bitmap);
		
	}

	/* (non-Javadoc)
	 * @see com.android.pelconner.dialogs.PelconnerDialog#onClick(android.view.View)
	 */
	@Override
	public void onClick(View sender) 
	{
		// TODO: Set the selected option and dismiss the dialog
		if(sender == _buttonImport)
		{
			_selectedOption = Option.IMPORT;
		}
		else if(sender == _buttonSave)
		{
			_selectedOption = Option.SAVE;
		}
		else if(sender == _buttonShare)
		{
			_selectedOption = Option.SHARE;
		}
		else if(sender == _buttonCancel)
		{
			_selectedOption = Option.CANCEL;
		}
		else
		{
			_selectedOption = Option.NONE;
		}
		dismiss();
	}
	
	public Option getSelectedOption()
	{
		return _selectedOption;
	}
}
