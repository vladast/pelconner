package com.terrestrial.pelconner.dialogs;

import com.terrestrial.pelconner.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PelconnerColorDialog extends PelconnerDialog implements OnClickListener, OnTouchListener, OnSeekBarChangeListener {

	private ImageView _imageViewColorCircle;
	private ImageView _imageViewColorPreview;
	private SeekBar _seekBarVolume;
	private SeekBar _seekBarAlpha;
	private Button _buttonOk;
	private Button _buttonCancel;
	private TextView _textViewColor;
	
	// Fields
	private int _oldColor;
	private int _color;
	private float[] _arrayHSV;
	
	float _x, _y;
	
	/*
	 * V = max (r,g,b)
	 * S = (max (r,g,b)) - min (r,g,b)/max (r,g,b)
	 * H = depends on which of r,g,b is the maximum
	 */
	
	
	public PelconnerColorDialog(Context context, int color) {
		super(context);
		_oldColor = color;
		_color = color;
		_arrayHSV = new float[3];
		Color.colorToHSV(_color, _arrayHSV);
		
	}

	/* (non-Javadoc)
	 * @see android.app.Dialog#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE); 	
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.color);
		
		TAG = PelconnerDialog.DIALOG_TAG_COLOR;
		
		initializeLayout();
	}	
	
	void initializeLayout()
	{
		_imageViewColorCircle = (ImageView)findViewById(R.id.imageViewColorCircle);
		_imageViewColorPreview = (ImageView)findViewById(R.id.imageViewColorPreview);
		_textViewColor = (TextView)findViewById(R.id.textViewColor);
		_seekBarVolume = (SeekBar)findViewById(R.id.seekBarVolume);
		_seekBarAlpha = (SeekBar)findViewById(R.id.seekBarAlpha);
		_buttonOk = (Button)findViewById(R.id.buttonColorOk);
		_buttonCancel = (Button)findViewById(R.id.buttonColorCancel);
		
		_imageViewColorCircle.setOnTouchListener(this);
		_seekBarVolume.setOnSeekBarChangeListener(this);
		_seekBarAlpha.setOnSeekBarChangeListener(this);
		_buttonOk.setOnClickListener(this);
		_buttonCancel.setOnClickListener(this);
		
		changePreview();
		
		_seekBarVolume.setProgress((int)(_arrayHSV[2] * 100F));
		_seekBarAlpha.setProgress(Color.alpha(_color));
	}
	
	/* (non-Javadoc)
	 * @see com.android.pelconner.dialogs.PelconnerDialog#onClick(android.view.View)
	 */
	@Override
	public void onClick(View sender) {
		if(sender == _buttonOk)
		{
			dismiss();
		}
		else if(sender == _buttonCancel)
		{
			_color = _oldColor;
			dismiss();
		}
	}

	@Override
	public boolean onTouch(View sender, MotionEvent event) {
		// #1: Nadji centralu tacku ImageView-a.
		// #2: Nadji poziciju tacke koja je dotaknuta, relativno u odnosu na centralnu tacku.
		// #3: Nadji stepen i udaljenost te tacke od centra. Diametar kruga je polovina ImageView-a.
		boolean result = false;
		if(sender == _imageViewColorCircle)
		{
			boolean received = false;
			int action = event.getAction();
			switch (action)
			{
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
			case MotionEvent.ACTION_UP:
				/*int widthCircle = _imageViewColorCircle.getLayoutParams().width;
				int heightCircle = _imageViewColorCircle.getLayoutParams().height;*/
				

				
				
				//_x = event.getX() > widthCircle ? widthCircle : event.getX();
				//_y = event.getY() > heightCircle ? heightCircle : event.getY();
				_x = event.getX();
				_y = event.getY();
				
				received = true;
				break;
			}
			
			if(received)
			{
				updatePickerPosition();
				updateColor();
				changePreview();
				result = true;
			}
		}
		return result;
	}

	public int getColor()
	{
		return _color;
	}
	
	private void updateColor() {
		int widthCircle = _imageViewColorCircle.getLayoutParams().width;
		int heightCircle = _imageViewColorCircle.getLayoutParams().height;
		
		float dX, dY, degree, distance;
		dX = _x - (widthCircle / 2);
		dY = (heightCircle / 2) - _y;		

		float a, b, c, d;
		a = _x / _y;
		b = (float) Math.sqrt(Math.pow(widthCircle / 2, 2) + Math.pow(heightCircle, 2));
		c = (float) Math.sqrt(Math.pow(dX / 2, 2) + Math.pow(dY, 2));	
		
		if(c > b)
		{
			float signX, signY;
			signX = dX / Math.abs(dX);
			signY = dY / Math.abs(dY);

			d = (float)Math.abs(a * Math.sqrt(Math.pow(b, 2) / (Math.pow(a, 2) + 1)));
			dY = (float) (signY * d);
			dX = (float) (signX * Math.abs(a * d));

		}
		
		degree = (float)Math.toDegrees((float)Math.atan(Math.abs(dX / dY)));
		distance = ((float) Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2))) / (widthCircle / 2);
		
		//Log.d("DEG_DIST", "degree = " + degree);
		
		
		if(dX > 0 && dY < 0)
		{
			degree = 180 - degree;
		}
		else if(dX < 0 && dY < 0)
		{
			degree = 180 + degree;
		}
		else if(dX < 0 && dY > 0)
		{
			degree = 360 - degree;
		}
		
		/*
		if((dX < 0) || (dX < 0 && dY < 0))
		{
			degree += 270;
		}
		else if(dY < 0)
		{
			degree = 0 - degree;
		}
		
		if(degree < 0)
		{
			degree += 360;
		}
		*/
		
		//Log.d("DEG_DIST", "Degree/Distance = " + degree + "/" + distance);
		
		_arrayHSV[0] = degree;
		_arrayHSV[1] = distance;
		
		_color = Color.HSVToColor(Color.alpha(_color), _arrayHSV);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if(seekBar == _seekBarVolume)
		{
			_arrayHSV[2] = (float)progress / 100F;
			int colorTemp = Color.HSVToColor(_arrayHSV);
			_color = Color.argb(Color.alpha(_color), Color.red(colorTemp), Color.green(colorTemp), Color.blue(colorTemp));
			changePreview();
		}
		else if(seekBar == _seekBarAlpha)
		{
			_color = Color.argb(progress, Color.red(_color), Color.green(_color), Color.blue(_color));
			changePreview();
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// Not used		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// Not used
	}

	private void changePreview()
	{
		// Update the image preview int the top-right corner of the screen
		// and the text representation of the color.
		int viewWidth = _imageViewColorPreview.getLayoutParams().width;
		int viewHeight = _imageViewColorPreview.getLayoutParams().height;
		
		/*Rect rect = _imageViewColorCircle.getDrawable().getBounds();
		
		int tempWidth = _imageViewColorCircle.getLayoutParams().width;
		int tempHeight = _imageViewColorCircle.getLayoutParams().height;
		
		rect = _imageViewColorCircle.getBackground().copyBounds();*/
		
		
		Bitmap bitmap = null;
		try
		{
			bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Config.ARGB_8888);
		}
		catch(OutOfMemoryError oome)
		{
			bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Config.ARGB_4444);
		}
		finally
		{
			if(bitmap == null)
			{
				bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Config.ALPHA_8);
			}
		}
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		paint.setARGB(Color.alpha(_color), Color.red(_color), Color.green(_color), Color.blue(_color));
		Color.RGBToHSV(Color.red(_color), Color.green(_color), Color.blue(_color), _arrayHSV);
		paint.setAntiAlias(true);
		paint.setStyle(Style.FILL);
		
		canvas.drawARGB(0, 0, 0, 0); // draw transparent background
		canvas.drawCircle(viewWidth / 2, viewHeight / 2, (viewHeight / 2) - 1, paint);
		
		_imageViewColorPreview.setImageBitmap(bitmap);
	}
	
	private void updatePickerPosition()
	{
		
		Bitmap bitmapCircle = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_color_circle);
		bitmapCircle = bitmapCircle.copy(bitmapCircle.getConfig(), true);
		//Bitmap bitmapPicker = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_color_circle_pick);
		
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStrokeWidth(2);
		paint.setStyle(Style.STROKE);
		paint.setColor(Color.argb(156, 75, 75, 75));
		
		
		Canvas canvas = new Canvas(bitmapCircle);
		//canvas.drawBitmap(bitmapPicker, _x - (bitmapPicker.getWidth() / 2), _y - (bitmapPicker.getHeight() / 2), paint);//(bitmapPicker, matrix, paint);
		canvas.drawCircle(_x, _y, 8, paint);
	}
}
