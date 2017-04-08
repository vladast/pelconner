package com.terrestrial.pelconner.dialogs;

import java.util.Date;

import com.terrestrial.pelconner.R;
import com.terrestrial.pelconner.helper.ImageData;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

public class PelconnerSaveDialog extends PelconnerDialog implements OnClickListener {

	// Class fields
	//private ImageView _imageViewPreview;
	private EditText _editTextName;
	private EditText _editTextDescription;
	//private EditText _editTextHeight;
	//private EditText _editTextWidth;
	//private CheckBox _checkBoxLocation;
	private Spinner _spinnerFormat;
	private Button _buttonSave;
	private Button _buttonCancel;
	
	private ImageData _imageData;
	private Context _context;
	private Bitmap _bitmap;
	
	public PelconnerSaveDialog(Context context, Bitmap bitmapPreview)
	{
		super(context);
		_context = context;
		_bitmap = bitmapPreview;
		
		_imageData = new ImageData();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Dialog#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.save);
		
		TAG = PelconnerDialog.DIALOG_TAG_SAVE;
		
		//_imageViewPreview = (ImageView)findViewById(R.id.imageViewPreview);
		_editTextName = (EditText)findViewById(R.id.editTextName);
		_editTextDescription = (EditText)findViewById(R.id.editTextDescription);
		//_editTextHeight = (EditText)findViewById(R.id.editTextHeight);
		//_editTextWidth = (EditText)findViewById(R.id.editTextWidth);
		//_checkBoxLocation = (CheckBox)findViewById(R.id.checkBoxLocation);
		_spinnerFormat = (Spinner)findViewById(R.id.spinnerFormat);
		_buttonSave = (Button)findViewById(R.id.buttonSave);
		_buttonCancel = (Button)findViewById(R.id.buttonCancel);
		
		//_imageViewPreview.setImageBitmap(_bitmap);
		_editTextName.setText(getRandomName());
		_editTextDescription.setText(_context.getString(R.string.dialog_save_description));
		//_editTextHeight.setText(String.valueOf(_bitmap.getHeight()));
		//_editTextWidth.setText(String.valueOf(_bitmap.getWidth()));
		//_checkBoxLocation.setChecked(true);
		// Set Spinner's adapter
		ArrayAdapter<CharSequence> adapterFormat = ArrayAdapter.createFromResource(_context, R.array.save_picture_formats, android.R.layout.simple_spinner_item);
		adapterFormat.setDropDownViewResource(android.R.layout.simple_spinner_item/*simple_spinner_dropdown_item*/);
		_spinnerFormat.setAdapter(adapterFormat);
		_spinnerFormat.setSelection(0); // JPG selected
		// Set listeners
		_buttonSave.setOnClickListener(this);
		_buttonCancel.setOnClickListener(this);
	}
	
	/* (non-Javadoc)
	 * @see com.android.pelconner.dialogs.PelconnerDialog#onClick(android.view.View)
	 */
	@Override
	public void onClick(View sender) {
		if(sender == _buttonSave)
		{
			_imageData.setName(_editTextName.getText().toString());
			_imageData.setDescription(_editTextDescription.getText().toString());
			_imageData.setHeight(_bitmap.getHeight());
			_imageData.setWidth(_bitmap.getWidth());
			//_imageData.setHeight(Integer.valueOf(_editTextHeight.getText().toString()));
			//_imageData.setWidth(Integer.valueOf(_editTextWidth.getText().toString()));
			_imageData.setSaveLocation(false/*_checkBoxLocation.isChecked()*/);
			ImageData.Format format = null;
			
			int selectedItem = _spinnerFormat.getSelectedItemPosition();
			switch(selectedItem)
			{
			case 0: // JPG
				format = ImageData.Format.JPG;
				break;
			case 1: // PNG
				format = ImageData.Format.PNG;
				break;
			}
			_imageData.setFormat(format);
			
			//_imageData.setFormat(ImageData.Format.JPG);
			
			dismiss();
		}
		else if(sender == _buttonCancel)
		{
			_imageData = new ImageData(); // Dismissing all data
			dismiss();
		}
	}

	public ImageData getImageData()
	{
		return _imageData;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Dialog#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}
	
	private String getRandomName()
	{
		// TODO: Generate a random name...
		Date date = new Date();
		String name = "Pelconner";//"Pelconner-" + String.valueOf(date.getYear()) + "_" + String.valueOf(date.getMonth()) + "_" + String.valueOf(date.getDate() + "_" +
		//		String.valueOf(date.getHours() + "_" + String.valueOf(date.getMinutes() + "_" + String.valueOf(date.getMinutes())))); 
		return name;
	}
}
