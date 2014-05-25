package com.terrestrial.pelconner.dialogs;

import com.terrestrial.pelconner.R;

import android.app.Application;
import android.content.Context;
import android.graphics.Paint.FontMetrics;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class PelconnerTextDialog extends PelconnerDialog implements OnClickListener, OnItemSelectedListener {

	public static enum Style
	{
		NORMAL,
		BOLD,
		ITALIC,
		BOLD_ITALIC
	}
	
	private EditText _editTextText;
	private Spinner _spinnerFonts;
	private Spinner _spinnerSizes;
	private Spinner _spinnerStyles;
	private Button _buttonOk;
	private Button _buttonCancel;
	private RelativeLayout _relativeLayoutText;
	
	// Fields
	private String _text;
	private String _fontName;
	private int _textSize;
	private Style _textStyle;

	public String getText()
	{
		return _text;
	}
	
	public String getFontName()
	{
		return _fontName;
	}

	public int getTextSize()
	{
		return _textSize;
	}
	
	public Style getTextStyle()
	{
		return _textStyle;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Dialog#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE); 	
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.text);
		
		TAG = PelconnerDialog.DIALOG_TAG_TEXT;
		
		_editTextText = (EditText)findViewById(R.id.editTextText);
		_spinnerFonts = (Spinner)findViewById(R.id.spinnerFonts);
		_spinnerSizes = (Spinner)findViewById(R.id.spinnerSizes);
		_spinnerStyles = (Spinner)findViewById(R.id.spinnerStyles);
		_buttonOk = (Button)findViewById(R.id.buttonTextOk);
		_buttonCancel = (Button)findViewById(R.id.buttonTextCancel);
		_relativeLayoutText = (RelativeLayout)findViewById(R.id.relativeLayoutText);
		
		
		android.view.WindowManager.LayoutParams params = getWindow().getAttributes();
		int one = _relativeLayoutText.getWidth();
		int two = getWindow().getAttributes().width;
		//int three = getWindow().getContainer().getAttributes().width;
		int four = getWindow().getWindowManager().getDefaultDisplay().getWidth();
		int width = params.width;//this.getWindow().getWindowManager().getDefaultDisplay().getWidth();//_linearLayoutText.getHeight() / 2;
		//int height = _linearLayoutText.getWidth() / 2;
		
		_buttonOk.setWidth(_buttonCancel.getWidth());
		//_buttonCancel.setWidth(50);
		

		ArrayAdapter<CharSequence> adapterFonts = ArrayAdapter.createFromResource(getContext(), R.array.dialog_text_fonts, android.R.layout.simple_spinner_item);
		adapterFonts.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		ArrayAdapter<CharSequence> adapterSizes = ArrayAdapter.createFromResource(getContext(), R.array.dialog_text_sizes, android.R.layout.simple_spinner_item);
		adapterFonts.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		ArrayAdapter<CharSequence> adapterStyles = ArrayAdapter.createFromResource(getContext(), R.array.dialog_text_styles, android.R.layout.simple_spinner_item);
		adapterStyles.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		_spinnerFonts.setAdapter(adapterFonts);
		_spinnerFonts.setOnItemSelectedListener(this);
		
		_spinnerSizes.setAdapter(adapterSizes);
		_spinnerSizes.setOnItemSelectedListener(this);
		
		_spinnerStyles.setAdapter(adapterStyles);
		_spinnerStyles.setOnItemSelectedListener(this);
		
		_buttonOk.setOnClickListener(this);
		_buttonCancel.setOnClickListener(this);
	}

	public PelconnerTextDialog(Context context) {
		super(context);

		_text = "";
		_fontName = "";
		_textSize = 5;
		_textStyle = Style.NORMAL;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long resId) {
		if(parent == _spinnerFonts)
		{
			_editTextText.setTypeface(getSelectedTypeface());
		}
		else if(parent == _spinnerStyles)
		{
			int style = 0;
			Style selection = getSelectedStyle();
			
			switch(selection){
			case NORMAL:
				style = 0;
				break;
			case BOLD:
				style = 1;
				break;
			case ITALIC:
				style = 2;
				break;
			case BOLD_ITALIC:
				style = 3;
				break;
			}
			
			_editTextText.setTypeface(getSelectedTypeface(), style);
		}
	}

	public Typeface getSelectedTypeface()
	{
		String fontName = _spinnerFonts.getSelectedItem().toString();
		Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "ChopinScript.ttf");;
		
		/*if(fontName.equalsIgnoreCase("Chopin Script"))
		{
			typeface = Typeface.createFromAsset(getContext().getAssets(), "ChopinScript.ttf");
		}
		else*/ if(fontName.equalsIgnoreCase("Old Standard TT"))
		{
			typeface = Typeface.createFromAsset(getContext().getAssets(), "OldStandard-Regular.ttf");
		}
		else if(fontName.equalsIgnoreCase("Precious"))
		{
			typeface = Typeface.createFromAsset(getContext().getAssets(), "Precious.ttf");
		}
		else if(fontName.equalsIgnoreCase("Royal"))
		{
			typeface = Typeface.createFromAsset(getContext().getAssets(), "Royal.ttf");
		}
		else if(fontName.equalsIgnoreCase("Wickhop Handwriting"))
		{
			typeface = Typeface.createFromAsset(getContext().getAssets(), "wickhop-handwriting.ttf");
		}
		return typeface;
	}
	
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see com.android.pelconner.dialogs.PelconnerDialog#onClick(android.view.View)
	 */
	@Override
	public void onClick(View sender) {
		if(sender == _buttonOk)
		{
			_text = _editTextText.getText().toString();
			_fontName = _spinnerFonts.getSelectedItem().toString();
			_textSize = Integer.parseInt(_spinnerSizes.getSelectedItem().toString());
			_textStyle = getSelectedStyle();
			dismiss();
		}
		else if(sender == _buttonCancel)
		{
			_text = "";
			dismiss();
		}
	}
	
	/* (non-Javadoc)
	 * @see android.app.Dialog#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		_text = "";
		dismiss();
	}

	private Style getSelectedStyle()
	{
		String selection = _spinnerStyles.getSelectedItem().toString();
		Style styleSelected = Style.NORMAL;
		/*if(selection.equalsIgnoreCase("normal"))
		{
			styleSelected = Style.NORMAL;
		}
		else*/ if(selection.equalsIgnoreCase("bold"))
		{
			styleSelected = Style.BOLD;
		}
		else if(selection.equalsIgnoreCase("italic"))
		{
			styleSelected = Style.ITALIC;
		}
		else if(selection.equalsIgnoreCase("bold-italic"))
		{
			styleSelected = Style.BOLD_ITALIC;
		}
		return styleSelected;
	}

}
