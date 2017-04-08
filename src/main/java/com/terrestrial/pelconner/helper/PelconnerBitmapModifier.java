package com.terrestrial.pelconner.helper;

import java.util.Random;

import com.terrestrial.pelconner.dialogs.PelconnerColorDialog;
import com.terrestrial.pelconner.helper.PelconnerOption.Availables;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.Toast;

public class PelconnerBitmapModifier
{
	public static final int MAX_LINE_WIDTH = 40;
	
	private Context _context;
	private Display _display;
	private Canvas _canvas;
	private Paint _paint;
	private Matrix _matrix;
	private ColorMatrix _colorMatrix;
	private Bitmap _bitmapModified;
	private Bitmap _bitmapInitial;
	//private Bitmap _bitmapInserted;
	
	private int _initialCanvasState;
	private int _currentCanvasState;
	
	private boolean _flipAngle = false;
	private float startX, startY, startAngle, endX, endY, prevX, prevY, distX, distY;
	private Path _path;
	
	private static int _color;
	private static int _paintWidth;
	private static boolean _antiAliasEnabled;
	private static boolean _fillEnabled;
	private static Cap _cap;
	private static Typeface _typeface;
	private static float _textSize;
	private static boolean _initialized = false; // only one initialization is permitted
	
	public void clear()
	{
		/*
		if(_bitmapModified != null)
			_bitmapModified.recycle();
		
		if(_bitmapInitial != null)
			_bitmapInitial.recycle();
		*/
	}
	
	public PelconnerBitmapModifier(Context context, Display display)
	{
		_context = context;
		_display = display;
		
		//_bitmapModified = Bitmap.createBitmap(_display.getWidth(), _display.getHeight(), Bitmap.Config.ARGB_8888);

		try
		{
			_bitmapModified = Bitmap.createBitmap(_display.getWidth(), _display.getHeight(), Bitmap.Config.ARGB_8888);
		}
		catch(OutOfMemoryError oome)
		{
			Log.e("OOM", "PelconnerBitmapModifier: default c-tor");
			//_bitmapModified = Bitmap.createBitmap(_display.getWidth(), _display.getHeight(), Bitmap.Config.RGB_565);
			_bitmapModified = Bitmap.createBitmap(_display.getWidth(), _display.getHeight(), Config.ARGB_4444);
			//Toast.makeText(_context, "Processing...", Toast.LENGTH_SHORT);
		}
		
		_canvas = new Canvas(_bitmapModified);
		_canvas.drawColor(Color.TRANSPARENT);
		
		_path = new Path();
		if(!_initialized)
		{
			_typeface = null;
			_textSize = 10F;
			_color = Color.WHITE;
			_paintWidth = 10;
			_antiAliasEnabled = true;
			_fillEnabled = false;
			_cap = Cap.ROUND;
		}
		initiatePaint();
		_matrix = new Matrix();
		_colorMatrix = new ColorMatrix();
		//_canvas.drawBitmap(_bitmapModified, _matrix, _paint);
		//_canvas.drawColor(Color.TRANSPARENT);
		_initialCanvasState = _canvas.save();
		_currentCanvasState = _initialCanvasState;
		try
		{
			_bitmapInitial = _bitmapModified.copy(_bitmapModified.getConfig(), true); //(_bitmapModified, 0, 0, _bitmapModified.getWidth(), _bitmapModified.getHeight());
		}
		catch(OutOfMemoryError ooem)
		{
			Log.e("OOM", "PelconnerBitmapModifier: default c-tor 2");
			_bitmapInitial = _bitmapModified;//.copy(Config.ALPHA_8, true);
		}
			
		startX = startY = endX = endY = -1000;
		distX = distY = 0;
	}
	
	public PelconnerBitmapModifier(Context context, Display display, Uri uriInitial)
	{
		this(context, display);
		
		try
		{
	        Uri uriImageFile = Uri.parse("file://" + uriInitial.getPath());
	        float width, height;
	        width = _display.getWidth();
	        height = _display.getHeight();
	        
			BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
			bmpFactoryOptions.inJustDecodeBounds = true;
			_bitmapInitial = null;
			Bitmap bitmapInitial = BitmapFactory.decodeStream(_context.getContentResolver().openInputStream(uriImageFile), 
					null, 
					bmpFactoryOptions);
			
			int heightRatio = (int)Math.ceil(bmpFactoryOptions.outHeight / width);
			int widthRatio = (int)Math.ceil(bmpFactoryOptions.outWidth / height);
			if(heightRatio > 1 && widthRatio > 1)
			{
				if(heightRatio > widthRatio)
				{
					bmpFactoryOptions.inSampleSize = heightRatio;
				}
				else
				{
					bmpFactoryOptions.inSampleSize = widthRatio;
				}
			}
			
			bmpFactoryOptions.inJustDecodeBounds = false;
			bitmapInitial = BitmapFactory.decodeStream(_context.getContentResolver().openInputStream(uriImageFile), 
					null, 
					bmpFactoryOptions);
			//_bitmapModified = Bitmap.createBitmap((int)width, (int)height, bitmapInitial.getConfig());
			
			//*vladast*//
			//_bitmapModified = Bitmap.createBitmap(bitmapInitial.getWidth(), bitmapInitial.getHeight(), bitmapInitial.getConfig());
			
			
			
			
			//_bitmapModified = Bitmap.createBitmap(_bitmapInitial.getWidth(), _bitmapInitial.getHeight(), _bitmapInitial.getConfig());
			//_canvas = new Canvas(_bitmapModified);
			_canvas.drawColor(Color.TRANSPARENT);
			
			//Matrix matrix = new Matrix();
			_matrix.reset();
			
			_paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
			
			_canvas.drawBitmap(bitmapInitial, _matrix, _paint);
			
			initiatePaint();
			
			bitmapInitial.recycle();
			
			/*
			_initialCanvasState = _canvas.save();
			_currentCanvasState = _initialCanvasState;
			
			if(!_initialized)
			{
				_typeface = null;
				_textSize = 10F;
				_color = Color.BLACK;
				_paintWidth = 10;
				_antiAliasEnabled = true;
				_fillEnabled = false;
				_cap = Cap.ROUND;
			}
			initiatePaint();
			
			_matrix = new Matrix();
			
	        float scale;
			
	        if(width < height) // TODO treba proveriti i samu sliku koja se importuje, a ne samo ekran.
	        {
	        	scale = width / bitmapInitial.getWidth();
	        }
	        else
	        {
	        	scale = height / bitmapInitial.getHeight();
	        }

	        _matrix.setScale(scale, scale);
	        
			_colorMatrix = new ColorMatrix();
			_canvas.drawBitmap(bitmapInitial, _matrix, _paint);
			
			_bitmapInitial = Bitmap.createBitmap(_bitmapModified, 0, 0, (int)width, (int)height);
			
			startX = startY = endX = endY = -1000;
			distX = distY = 0;
			*/
		}
		catch(Exception e)
		{
			//Log.e("ERROR", e.getMessage());
		}
		
		
		
		
		
		/*_context = context;
		_display = display;
        
		try
		{
	        Uri uriImageFile = Uri.parse("file://" + uriInitial.getPath());
	        float width, height;
	        width = _display.getWidth();
	        height = _display.getHeight();
	        
			BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
			bmpFactoryOptions.inJustDecodeBounds = true;
			Bitmap bitmapInitial = BitmapFactory.decodeStream(_context.getContentResolver().openInputStream(uriImageFile), 
					null, 
					bmpFactoryOptions);
			
			int heightRatio = (int)Math.ceil(bmpFactoryOptions.outHeight / width);
			int widthRatio = (int)Math.ceil(bmpFactoryOptions.outWidth / height);
			if(heightRatio > 1 && widthRatio > 1)
			{
				if(heightRatio > widthRatio)
				{
					bmpFactoryOptions.inSampleSize = heightRatio;
				}
				else
				{
					bmpFactoryOptions.inSampleSize = widthRatio;
				}
			}
			
			bmpFactoryOptions.inJustDecodeBounds = false;
			bitmapInitial = BitmapFactory.decodeStream(_context.getContentResolver().openInputStream(uriImageFile), 
					null, 
					bmpFactoryOptions);
			_bitmapModified = Bitmap.createBitmap((int)width, (int)height, bitmapInitial.getConfig());
			
			_canvas = new Canvas(_bitmapModified);
			//_canvas.drawColor(0xFFCCCCCC);
			_initialCanvasState = _canvas.save();
			_currentCanvasState = _initialCanvasState;
			
			if(!_initialized)
			{
				_typeface = null;
				_textSize = 10F;
				_color = Color.BLACK;
				_paintWidth = 10;
				_antiAliasEnabled = true;
				_fillEnabled = false;
				_cap = Cap.ROUND;
			}
			initiatePaint();
			
			_matrix = new Matrix();
			
	        float scale;
			
	        if(width < height) // TODO treba proveriti i samu sliku koja se importuje, a ne samo ekran.
	        {
	        	scale = width / bitmapInitial.getWidth();
	        }
	        else
	        {
	        	scale = height / bitmapInitial.getHeight();
	        }

	        _matrix.setScale(scale, scale);
	        
			_colorMatrix = new ColorMatrix();
			_canvas.drawBitmap(bitmapInitial, _matrix, _paint);
			
			_bitmapInitial = Bitmap.createBitmap(_bitmapModified, 0, 0, (int)width, (int)height);
			
			startX = startY = endX = endY = -1000;
			distX = distY = 0;
		}
		catch(Exception e)
		{
			Log.e("ERROR", e.getMessage());
		}*/
	}
	
	public PelconnerBitmapModifier(Context context, Display display, Bitmap bitmapInitial)
	{
		// This one is called when image is captured through the Camera
		this(context, display);
        
		try
		{
	        float width, height, scale;
	        width = _display.getWidth();
	        height = _display.getHeight();
	        
	        // Rotating the received image
	        /*Bitmap bitmapTemp = Bitmap.createBitmap(bitmapInitial.getHeight(), bitmapInitial.getWidth(), bitmapInitial.getConfig());
	        Canvas canvas = new Canvas(bitmapTemp);
	        Matrix matrix = new Matrix();*/
	        
	        /*matrix.setRotate(90);
	        canvas.drawBitmap(bitmapInitial, matrix, _paint);
	        
	        bitmapInitial = bitmapTemp.copy(bitmapInitial.getConfig(), true);
	        */	        
	        
	        
	        if(width < height) // TODO treba proveriti i samu sliku koja se importuje, a ne samo ekran.
	        {
	        	scale = width / bitmapInitial.getWidth();
	        }
	        else
	        {
	        	scale = height / bitmapInitial.getHeight();
	        }
	        
	        //*vladast*//
	        /*
	        try
	        {
	        	_bitmapModified = Bitmap.createBitmap((int)width, (int)height, bitmapInitial.getConfig());
	        }
	        catch(OutOfMemoryError ooem)
	        {
	        	_bitmapModified = Bitmap.createBitmap((int)width, (int)height, Config.ALPHA_8);
	        } 
	        */
	        
	        // _bitmapModified is already created in default c-tor
			_canvas = new Canvas(_bitmapModified);
			//_canvas.drawColor(0x00CCCCCC);
			_initialCanvasState = _canvas.save();
			_currentCanvasState = _initialCanvasState;
			
			if(!_initialized)
			{
				_path = new Path();
				_typeface = null;
				_textSize = 10F;
				_color = Color.BLACK;
				_paintWidth = 10;
				_antiAliasEnabled = true;
				_fillEnabled = false;
				_cap = Cap.ROUND;
			}
			initiatePaint();

			_matrix = new Matrix();
			_matrix.preRotate(45);
			_matrix.setScale(scale, scale);
			
			_colorMatrix = new ColorMatrix();
			
			_canvas.drawBitmap(bitmapInitial, _matrix, _paint);
			
			_bitmapInitial.recycle();
			
			try
			{
				_bitmapInitial = _bitmapModified.copy(_bitmapModified.getConfig(), true);
			}
			catch(OutOfMemoryError ooem)
			{
				Log.e("OOM", "PelconnerBitmapModifier: c-tor with Bitmap");
				_bitmapInitial = _bitmapModified.copy(Config.ALPHA_8, true);
			}
			
			bitmapInitial.recycle();
			
			startX = startY = endX = endY = -1000;
			distX = distY = 0;
		}
		catch(Exception e)
		{
			//Log.e("ERROR", e.getMessage());
		}
	}
	
	public void clearBitmaps()
	{
		//_bitmapInitial = Bitmap.createBitmap(_display.getWidth(), _display.getHeight(), Config.ARGB_8888);
		
		// No need to create bitmaps again, because we're goint to make them transparent
		/*
		try
		{
			_bitmapInitial = Bitmap.createBitmap(_display.getWidth(), _display.getHeight(), Bitmap.Config.ARGB_8888);
		}
		catch(OutOfMemoryError oome)
		{
			_bitmapInitial = Bitmap.createBitmap(_display.getWidth(), _display.getHeight(), Bitmap.Config.ALPHA_8);
		}
		*/
		
		/*
		Canvas canvas = new Canvas(_bitmapInitial);
		canvas.drawColor(Color.TRANSPARENT);
		try
		{
			_bitmapModified = _bitmapInitial.copy(_bitmapInitial.getConfig(), true);
		}
		catch(OutOfMemoryError ooem)
		{
			_bitmapInitial = _bitmapModified.copy(Config.ALPHA_8, true);
		}
		*/
		
		_bitmapInitial.eraseColor(Color.TRANSPARENT);
	}
	
	private void initiatePaint()
	{
		// Setting default values
		_paint = new Paint();
		/*_paint.setColor(Color.BLACK);
		_paint.setStrokeCap(Cap.ROUND);
		_paint.setStrokeWidth(20);*/
		
		//_color = Color.BLACK;
		_paint.setColor(_color);
		
		//_paintWidth = 10;
		_paint.setStrokeWidth(_paintWidth);
		
		//_antiAliasEnabled = true;
		_paint.setAntiAlias(_antiAliasEnabled);
		
		//_fillEnabled = false;
		_paint.setStyle(_fillEnabled ? Style.FILL : Style.STROKE);
		
		//_cap = Cap.ROUND;
		_paint.setStrokeCap(_cap);
		
		//_typeface = null;
		if(_typeface != null)
			_paint.setTypeface(_typeface);
		
		//_textSize = 10F;
		_paint.setTextSize(_textSize);
		
		
		/*
		private int _color;
		private int _paintWidth;
		private boolean _antiAliasEnabled;
		private boolean _fillEnabled;
		*/

		_initialized = true;
	}

	//private int _color;
	public int getColor()
	{
		return _color;
	}
	
	public void setColor(int color)
	{
		// TODO Add color number verification
		_color = color;
		_paint.setColor(_color);
	}
	
	//private int _paintWidth;
	public int getLineWidth()
	{
		return _paintWidth;
	}
	
	public void setLineWidth(int width)
	{
		_paintWidth = width;
		_paint.setStrokeWidth(_paintWidth);
	}
	
	//private boolean _antiAliasEnabled;
	public boolean isAntiAliasing()
	{
		return _antiAliasEnabled;
	}
	
	public void setAntiAliasing(boolean antiAliasing)
	{
		_antiAliasEnabled = antiAliasing;
		_paint.setAntiAlias(_antiAliasEnabled);
	}
	
	//private boolean _fillEnabled;
	public boolean isFillEnabled()
	{
		return _fillEnabled;
	}
	
	public void setFillEnabled(boolean fillEnabled)
	{
		_fillEnabled = fillEnabled;
		_paint.setStyle(_fillEnabled ? Style.FILL : Style.STROKE);
	}
	
	//private Cap _cap;	
	public Cap getLineEnding()
	{
		return _cap;
	}
	
	public void setLineEnding(Cap cap)
	{
		_cap = cap;
		_paint.setStrokeCap(_cap);
	}

	//private Typeface _typeface;
	private Typeface getTypeface()
	{
		return _typeface;
	}
	
	private void setTypeface(Typeface typeface)
	{
		_typeface = typeface;
		_paint.setTypeface(_typeface);
	}
	
	//private int _textSize
	private float getTextSize()
	{
		return _textSize;
	}
	
	private void setTextSize(float textSize)
	{
		_textSize = textSize;
		_paint.setTextSize(_textSize);
	}
	
	public Bitmap getBitmap()
	{
		return _bitmapModified;
	}
	
	

	public Bitmap addText(String text, Typeface selectedTypeface, float textSize,
			com.terrestrial.pelconner.dialogs.PelconnerTextDialog.Style textStyle) {

		switch(textStyle)
		{
		case ITALIC:
			_typeface = Typeface.create(selectedTypeface, Typeface.ITALIC);
			break;
		case BOLD:
			_typeface = Typeface.create(selectedTypeface, Typeface.BOLD);
			break;
		case BOLD_ITALIC:
			_typeface = Typeface.create(selectedTypeface, Typeface.BOLD_ITALIC);
			break;
		}
		
		//_paint.setTypeface(selectedTypeface);
		this.setTypeface(selectedTypeface);
		//_paint.setTextSize(textSize);
		this.setTextSize(textSize);
		int currentLineWidth = this.getLineWidth();
		boolean isFilling = this.isFillEnabled();
		this.setLineWidth(1);
		this.setFillEnabled(true);
		_canvas.drawText(text, 30, 30, _paint);
		this.setLineWidth(currentLineWidth);
		this.setFillEnabled(isFilling);
		return _bitmapModified.copy(_bitmapModified.getConfig(), true);
	}
	
	public Bitmap addText(String text, Typeface selectedTypeface, float textSize,
			com.terrestrial.pelconner.dialogs.PelconnerTextDialog.Style textStyle,
			float x, float y) 
	{

		switch(textStyle)
		{
		case ITALIC:
			_typeface = Typeface.create(selectedTypeface, Typeface.ITALIC);
			break;
		case BOLD:
			_typeface = Typeface.create(selectedTypeface, Typeface.BOLD);
			break;
		case BOLD_ITALIC:
			_typeface = Typeface.create(selectedTypeface, Typeface.BOLD_ITALIC);
			break;
		}
		
		//_paint.setTypeface(selectedTypeface);
		this.setTypeface(selectedTypeface);
		//_paint.setTextSize(textSize);
		this.setTextSize(textSize);
		Paint paint = new Paint(_paint);
		paint.setStrokeWidth(1);
		paint.setStyle(Style.FILL);
		//int currentLineWidth = this.getLineWidth();
		//boolean isFilling = this.isFillEnabled();
		//this.setLineWidth(1);
		//this.setFillEnabled(true);
		
		//*vladast*//
		/*
		try
		{
			_bitmapModified = _bitmapInitial.copy(_bitmapInitial.getConfig(), true); //Bitmap.createBitmap(_bitmapInitial.getWidth(), _bitmapInitial.getHeight(), _bitmapInitial.getConfig());
		}
		catch(OutOfMemoryError ooem)
		{
			_bitmapModified = _bitmapInitial.copy(Config.ALPHA_8, true);
		}
		*/
		
		_bitmapModified.eraseColor(Color.TRANSPARENT);
		_canvas = new Canvas(_bitmapModified);	

		_canvas.drawBitmap(_bitmapInitial, new Matrix(), paint);
		
		paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
		
		//_canvas.drawColor(Color.TRANSPARENT);
		_canvas.drawText(text, x, y, paint);
		//this.setLineWidth(currentLineWidth);
		//this.setFillEnabled(isFilling);
		return _bitmapModified.copy(_bitmapModified.getConfig(), true);
	}
	
	public Bitmap insertBitmap(Bitmap bitmap, boolean rotate)
	{
		try
		{
			_bitmapModified = _bitmapInitial.copy(_bitmapInitial.getConfig(), true);
		}
		catch(OutOfMemoryError ooem)
		{
			Log.e("OOM", "PelconnerBitmapModifier:insertBitmap");
			_bitmapModified = _bitmapInitial.copy(Config.ALPHA_8, true);
		}
		_canvas = new Canvas(_bitmapModified);
		_matrix.reset();
		
		
		// Scalling the imported image from Gallery to fit the Picture frame.
		float aspectRatioDisplay, aspectRatioBitmap, widthResult, heightResult, widthBitmap, heightBitmap, widthDisplay, heightDisplay;
		widthBitmap = rotate ? bitmap.getHeight() : bitmap.getWidth();
		heightBitmap = rotate ? bitmap.getWidth() : bitmap.getHeight();
		widthResult = _bitmapInitial.getWidth();
		heightResult = _bitmapInitial.getHeight();
		aspectRatioDisplay = widthResult / heightResult;
		aspectRatioBitmap = widthBitmap / heightBitmap;
		
		
		float scale;
		if(aspectRatioBitmap > aspectRatioDisplay)
		{
			heightResult = heightResult / aspectRatioBitmap;
			scale = widthResult / widthBitmap;
		}
		else
		{
			widthResult = heightResult * aspectRatioBitmap;
			scale = heightResult / heightBitmap;
		}
		//float scaleY = heightResult / heightBitmap;
		
		_matrix.setScale(scale, scale);
		_matrix.postRotate(rotate ? 90 : 0, widthResult / 2, widthResult / 2);
		
		// End of scalling
		
		
		
		
		//_canvas.drawColor(Color.TRANSPARENT);
		
		_paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
		_paint.setAlpha(255);
		_canvas.drawBitmap(bitmap, _matrix, _paint);
		initiatePaint();
		try
		{
			_bitmapInitial = _bitmapModified.copy(_bitmapModified.getConfig(), true);
		}
		catch(OutOfMemoryError ooem)
		{
			Log.e("OOM", "PelconnerBitmapModifier: insertBitmap 2");
			//_bitmapInitial = _bitmapModified.copy(Config.ALPHA_8, true);
			_bitmapInitial = _bitmapModified;//Bitmap.createBitmap(_bitmapModified);
		}
		
		
		_matrix.reset();
		return _bitmapModified;
	}
	
	public Bitmap modify(Availables action, float downX, float downY, float upX, float upY, MotionEvent motionEvent)
	{
		//Log.d("MODIFY", "Called!");
		
		if(motionEvent != null)
		{
			if(motionEvent.getAction() == MotionEvent.ACTION_DOWN)
			{
				/*float a, b;
				a = downY - (_display.getHeight() / 2);
				b = downX - (_display.getWidth() / 2);
				prevX = b;
				startAngle = (float) Math.toDegrees((float)Math.atan(a / b));*/
				

				/*
				if(action == Availables.TOOLS_FILL)
				{
					_canvas = new Canvas(_bitmapModified);
					_canvas.drawColor(_color);	
				}
				*/
			}
			else if(motionEvent.getAction() == MotionEvent.ACTION_MOVE)
			{
				if(startX == -1000 && startY == -1000)
				{
					startX = downX;
					startY = downY;
					endX = endY = -1000;	
	
					float a, b;
					a = downY - (_display.getHeight() / 2);
					b = downX - (_display.getWidth() / 2);
					prevX = b;
					startAngle = (float) Math.toDegrees((float)Math.atan(a / b));
					_flipAngle = false;
					_path = new Path();
					_path.moveTo(startX, startY);
				}
				else
				{
					switch(action)
					{
					case NONE:
						_canvas.drawLine(downX, downY, upX, upY, _paint);
						break;
					case EDIT_NEW:
						break;
					case MAIN_IMPORT_CAMERA:
					case MAIN_IMPORT_GALLERY:
					case EDIT_IMPORT_CAMERA:
					case EDIT_IMPORT_GALLERY:
						
						
						
						//*vladast*//
						//_bitmapModified = Bitmap.createBitmap(_bitmapInitial.getWidth(), _bitmapInitial.getHeight(), _bitmapInitial.getConfig());
						_canvas = new Canvas(_bitmapModified);	

						_canvas.drawColor(Color.TRANSPARENT);
						
						Matrix matrixImport = new Matrix();
						matrixImport.preTranslate(upX - startX, upY - startY);
						Paint paintGallery = new Paint();
						paintGallery.setAlpha(255);
						_canvas.drawBitmap(_bitmapInitial, matrixImport, paintGallery/*_paint*/);		
						
						/*
						
						//_bitmapModified = _bitmapInitial.copy(_bitmapInitial.getConfig(), true); //Bitmap.createBitmap(_bitmapInitial.getWidth(), _bitmapInitial.getHeight(), _bitmapInitial.getConfig());
						_canvas = new Canvas(_bitmapModified);	
						_canvas.drawColor(Color.TRANSPARENT);

						Matrix matrixImport = new Matrix();
						matrixImport.preTranslate(upX - startX, upY - startY);
						_canvas.drawBitmap(_bitmapModified, matrixImport, _paint);
						//_canvas.drawBitmap(_bitmapModified, left, top, paint)
						*/
						break;
					case EDIT_MOVE:
						
						//_bitmapModified = Bitmap.createBitmap(_bitmapInitial.getWidth(), _bitmapInitial.getHeight(), _bitmapInitial.getConfig());
						_bitmapModified.eraseColor(Color.TRANSPARENT);	
						_canvas = new Canvas(_bitmapModified);	

						_canvas.drawColor(Color.TRANSPARENT);
							
						Matrix matrix = new Matrix();
						matrix.preTranslate(upX - startX, upY - startY);
						Paint paintMove = new Paint();
						paintMove.setAlpha(255);
						_canvas.drawBitmap(_bitmapInitial, matrix, paintMove);
						break;
					case EDIT_ROTATE:
						float cx, cy, degree;
						cx = _bitmapModified.getWidth() / 2;
						cy = _bitmapModified.getHeight() / 2;
						
						float a, b;
						a = downY - cy;
						b = downX - cx;
						//Log.d("TAN", "a/b = " + a + "/" + b);
						int offset = 0;
						if(_flipAngle)
						{
							offset = -180;
						}
						degree = (float) Math.toDegrees((float)Math.atan(a / b)) - startAngle + offset;
						
						if((prevX > 0 && b < 0) || (prevX < 0 && b > 0))
						{
							degree = degree - 180F;
							_flipAngle = !_flipAngle;
						}
						
						//Log.d("DEG", "degree = " + degree + "; a/b = " + a + "/" + b);
						prevX = b;
						
						
						
						_matrix.reset();
						_matrix.setTranslate(-cx, -cy);
						//_matrix.setTranslate(cx, cy);     
						_matrix.setRotate(degree, cx, cy);
						//_matrix.postTranslate(cx, cy);
						//tempBitmap = Bitmap.createBitmap(_bitmapInitial, 0, 0, _display.getWidth(), _display.getHeight(), _matrix, false);
						
						//vladast
						/*
						tempBitmap = _bitmapInitial.copy(_bitmapInitial.getConfig(), true);
						
						_bitmapModified = _bitmapInitial.copy(_bitmapInitial.getConfig(), true);
						_canvas = new Canvas(_bitmapModified);
						
						_canvas.drawBitmap(_bitmapInitial, 0, 0, _paint);
						_canvas.drawBitmap(tempBitmap, _matrix, null);
						*/
						
						
						_bitmapModified = Bitmap.createBitmap(_bitmapInitial.getWidth(), _bitmapInitial.getHeight(), _bitmapInitial.getConfig());
						_canvas = new Canvas(_bitmapModified);	
			
						//_canvas.drawBitmap(_bitmapInitial, 0, 0, _paint);
						//_canvas.drawBitmap(_bitmapInitial, /*distX + (upX - startX)*/ upX - startX, /*distY + (upY - startY)*/ upY - startY, _paint);
						
						_canvas.drawColor(Color.TRANSPARENT);
						
						_canvas.drawBitmap(_bitmapInitial, _matrix, _paint);
						
						break;
					case EDIT_RESIZE:
						break;
					case EDIT_STRETCH:
						break;
/*					case EDIT_ZOOM:
						//break;
					case EDIT_ZOOM_MANUAL:
						// downX is from 0-200 range where 100 represents "no-zoom"
						// 0-99 --> zoom-out
						// 101-199 --> zoom-in
						// Max/Min Zoom-in is 4x
						_bitmapModified = Bitmap.createBitmap(_bitmapInitial.getWidth(), _bitmapInitial.getHeight(), _bitmapInitial.getConfig());
						_canvas = new Canvas(_bitmapModified);	

						_canvas.drawColor(Color.TRANSPARENT);
						
						float maxZoomValue = 4.0F;
						float midValue = 100F;
						Matrix matrixZoom = new Matrix();
						//matrixZoom.preTranslate(upX - startX, upY - startY);
						
						matrixZoom.setScale(0.5F, 0.5F);
						_canvas.drawBitmap(_bitmapInitial, matrixZoom, _paint);
						break;
*/
					case EDIT_PINCH_ZOOM:
						/**
						 * downX --> scale
						 * downY --> -1000
						 * upX --> x-coordinate of the center point
						 * upY --> y-coordinate of the center point
						 */
						
						
						if(downY == -1000)
						{
							_bitmapModified = Bitmap.createBitmap(_bitmapInitial.getWidth(), _bitmapInitial.getHeight(), _bitmapInitial.getConfig());
							_canvas = new Canvas(_bitmapModified);	
			
							_canvas.drawColor(Color.TRANSPARENT);
							
							float minZoomValue = 0.25F;
							float maxZoomValue = 4.0F;
							float midValue = 100F;
							float scale;
							
							Matrix matrixZoom = new Matrix();
							//matrixZoom.preTranslate(upX - startX, upY - startY);
							/*
							Log.d("ZOOM", "downX = " + downX);
							if(downX < 100) // Zoom-Out
							{
								scale = ((1F - minZoomValue) / midValue) * downX + minZoomValue;
							}
							else // Zoom-In
							{
								scale = ((maxZoomValue - 1F) / midValue) * (downX - midValue) + 1;
							}
							
							
							float translateX, translateY;
							translateX = (1 - scale) * (_display.getWidth() / 2F);
							translateY = (1 - scale) * (_display.getHeight() / 2F);
							matrixZoom.preTranslate(-translateX, -translateY);
							matrixZoom.setScale(scale, scale);
							matrixZoom.postTranslate(translateX, translateY);
							*/
							matrixZoom.postScale(downX, downX, upX, upY);
							
							boolean oldAntiAlias = _paint.isAntiAlias();
							_paint.setAntiAlias(true);
							Paint paintZoom = new Paint();
							paintZoom.setAlpha(255);
							_canvas.drawBitmap(_bitmapInitial, matrixZoom, paintZoom);
							_paint.setAntiAlias(oldAntiAlias);
						}						
						break;
					case EDIT_CROP:
						//Toast.makeText(_context, "Crop!", Toast.LENGTH_SHORT);
						break;
					case TOOLS_COLOR_TEST:
						break;
					case TOOLS_RECTANGLE_SELECT:
						break;
					case TOOLS_CIRCLE_SELECT:
						// TEST
						int color = Color.argb(255, 200, 0, 0); // Red...
						PelconnerColorDialog dialogColor = new PelconnerColorDialog(_context, color);
						dialogColor.show();
						break;
					case TOOLS_DRAW:
						if(downX == upX && downY == upY)
						{
							Paint paintDot = new Paint();
							paintDot.setColor(_color);
							paintDot.setStyle(Style.FILL);
							paintDot.setAntiAlias(true);
							_canvas.drawCircle(upX, upY, _paintWidth / 2, paintDot);
						}
						else
						{
							//_canvas.drawLine(downX, downY, upX, upY, _paint);
							
							/*_bitmapModified = _bitmapInitial.copy(_bitmapInitial.getConfig(), true);
							_canvas = new Canvas(_bitmapModified);
							_canvas.drawColor(Color.TRANSPARENT);
							Paint paint = new Paint(_paint);
							_canvas.drawBitmap(_bitmapInitial, _matrix, _paint);
							_path.lineTo(upX, upY);
							_canvas.drawPath(_path, _paint);*/
							
							/*_bitmapModified = _bitmapInitial.copy(_bitmapInitial.getConfig(), true);
							_canvas = new Canvas(_bitmapModified);
							_path.lineTo(upX, upY);
							_canvas.drawPath(_path, _paint);*/
							
							//paintDisplay.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
							
							/*
							try
							{
								_bitmapModified = _bitmapInitial.copy(_bitmapInitial.getConfig(), true);
							}
							catch(OutOfMemoryError ooem)
							{
								_bitmapModified = _bitmapModified.copy(Config.ALPHA_8, true);
							}
							*/
							
							_bitmapModified = Bitmap.createScaledBitmap(_bitmapInitial, 
									_display.getWidth(), _display.getHeight(), false);
							//_bitmapInitial.recycle(); 
							
							_canvas = new Canvas(_bitmapModified);
							_path.lineTo(upX, upY);
							Paint paint = new Paint(_paint);
							paint.setStyle(Style.STROKE);
							paint.setStrokeJoin(Join.ROUND);
							//paint.setStrokeMiter(1);
							_canvas.drawPath(_path, paint);
						}
						break;
					case TOOLS_RECTANGLE:
						/*
						try
						{
							_bitmapModified = _bitmapInitial.copy(_bitmapInitial.getConfig(), true);
						}
						catch(OutOfMemoryError ooem)
						{
							_bitmapModified = _bitmapModified.copy(Config.ALPHA_8, true);
						}
						*/
						
						_bitmapModified = Bitmap.createScaledBitmap(_bitmapInitial, 
								_display.getWidth(), _display.getHeight(), false);
						//_bitmapInitial.recycle(); 
						
						_canvas = new Canvas(_bitmapModified);
						_canvas.drawRect(startX, startY, upX, upY, _paint);
						break;
					case TOOLS_ELLIPSE:
						//_canvas.drawColor(0xFFCCCCCC);
						try
						{
							/*if(_bitmapModified != null)
							{
								_bitmapModified.recycle();
								_bitmapModified = null;
							}*/
							_bitmapModified = _bitmapInitial.copy(_bitmapInitial.getConfig(), true);
						}
						catch(OutOfMemoryError ooem)
						{
							Log.e("OOM", "PelconnerBitmapModifier: modify TOOLS_ELLIPSE");
							_bitmapModified = _bitmapModified.copy(Config.ALPHA_8, true);
						}
						_canvas = new Canvas(_bitmapModified);
						//_paint.setStyle(Style.STROKE);
						_canvas.drawBitmap(_bitmapInitial, 0, 0, _paint);
						float diamX = Math.abs((startX - upX) / 2F);
						float diamY = Math.abs((startY - upY) / 2F);
						float diam = 0;
						if(Math.abs(diamX) < Math.abs(diamY))
						{
							diam = diamY;
						}
						else
						{
							diam = diamX;
						}
						float posX, posY;
						posX = (startX + upX) / 2;
						posY = (startY + upY) / 2;
						
						_canvas.drawCircle(posX, posY, Math.abs(diam), _paint);
						break;
					case TOOLS_LINE:
						//_canvas.restoreToCount(_initialCanvasState);
						//_canvas.drawColor(0xFFCCCCCC);
						try
						{
							/*if(_bitmapModified != null)
							{
								_bitmapModified.recycle();
								_bitmapModified = null;
							}*/
							_bitmapModified = _bitmapInitial.copy(_bitmapInitial.getConfig(), true);
						}
						catch(OutOfMemoryError ooem)
						{
							Log.e("OOM", "PelconnnerBitmapModified: modify TOOLS_LINE");
							_bitmapModified = _bitmapModified.copy(Config.ALPHA_8, true);
						}
						_canvas = new Canvas(_bitmapModified);
						
						_canvas.drawLine(startX, startY, upX, upY, _paint);
						
						// old
						/*
						_canvas.drawBitmap(_bitmapInitial, 0, 0, _paint);
						_canvas.drawLine(startX, startY, upX, upY, _paint);
						*/
						break;
					default:
					}				
				}
			}
			else if(motionEvent.getAction() == MotionEvent.ACTION_UP)
			{
				endX = upX;
				endY = upY;
				distX = startX - endX;
				distY = startY - endY;
				startX = startY = -1000;
				//_currentCanvasState = _canvas.save(Canvas.ALL_SAVE_FLAG);
				
				try
				{
					/*
					if(_bitmapInitial != null)
					{
						_bitmapInitial.recycle();
						_bitmapInitial = null;
					}
					*/
					
					_bitmapInitial = _bitmapModified.copy(_bitmapModified.getConfig(), true);
				}
				catch(OutOfMemoryError oome)
				{
					Log.e("OOM", "PelconnerBitmapModifier: modify ACTION_UP");
					_bitmapInitial = _bitmapModified.copy(Config.ALPHA_8, true);
				}
			}
		}
		else
		{
			float cx, cy;
			cx = _display.getWidth() / 2;
			cy = _display.getHeight() / 2;
			
			switch(action)
			{
			case TOOLS_SHAKE_DRAW:
				if(downX == upX && downY == upY)
				{
					Paint paintDot = new Paint();
					paintDot.setColor(_color);
					paintDot.setStyle(Style.FILL);
					paintDot.setAntiAlias(true);
					_canvas.drawCircle(upX, upY, _paintWidth / 2, paintDot);
				}
				else if(downX == -1000 && downY == -1000)
				{
					_path.moveTo(upX, upY);
				}
				else
				{
					_bitmapModified = _bitmapInitial.copy(_bitmapInitial.getConfig(), true);
					_canvas = new Canvas(_bitmapModified);
					_path.lineTo(upX, upY);
					Paint paint = new Paint(_paint);
					paint.setStyle(Style.STROKE);
					paint.setStrokeJoin(Join.ROUND);
					//paint.setStrokeMiter(1);
					_canvas.drawPath(_path, paint);
				}
				break;
			case EDIT_ROTATE:
				float degree = downX;
				
				_matrix.reset();
				_matrix.setTranslate(-cx, -cy);
				//_matrix.setTranslate(cx, cy);     
				_matrix.setRotate(degree, cx, cy);
				//_matrix.postTranslate(cx, cy);
				//tempBitmap = Bitmap.createBitmap(_bitmapInitial, 0, 0, _display.getWidth(), _display.getHeight(), _matrix, false);
				
				Bitmap tempBitmap = null;
				try
				{
					tempBitmap = _bitmapInitial.copy(_bitmapInitial.getConfig(), true);
				}
				catch(OutOfMemoryError ooem)
				{
					Log.e("OOM", "PelconnerBitmapModifier: modify EDIT_ROTATE");
					tempBitmap = _bitmapInitial.copy(Config.ALPHA_8, true);
				}
				_canvas.drawBitmap(_bitmapInitial, 0, 0, _paint);
				_canvas.drawBitmap(tempBitmap, _matrix, null);
				break;
			case EDIT_ZOOM:
			case EDIT_ZOOM_MANUAL:
				// downX is from 0-200 range where 100 represents "no-zoom"
				// 0-99 --> zoom-out
				// 101-199 --> zoom-in
				// Max/Min Zoom-in is 4x
				
				if(downY == 0 && upY == 0)
				{
					/*
					if(_bitmapModified != null)
					{
						_bitmapModified.recycle();
						_bitmapModified = null;
					}
					*/
					_bitmapModified = Bitmap.createBitmap(_bitmapInitial.getWidth(), _bitmapInitial.getHeight(), _bitmapInitial.getConfig());
					_canvas = new Canvas(_bitmapModified);	
	
					_canvas.drawColor(Color.TRANSPARENT);
					
					float minZoomValue = 0.25F;
					float maxZoomValue = 4.0F;
					float midValue = 100F;
					float scale;
					
					Matrix matrixZoom = new Matrix();
					//matrixZoom.preTranslate(upX - startX, upY - startY);
					//Log.d("ZOOM", "downX = " + downX);
					if(downX < 100) // Zoom-Out
					{
						scale = ((1F - minZoomValue) / midValue) * downX + minZoomValue;
					}
					else // Zoom-In
					{
						scale = ((maxZoomValue - 1F) / midValue) * (downX - midValue) + 1;
					}
					
					float translateX, translateY;
					translateX = /*_display.getWidth() / 2F;*/(1 - scale) * (_display.getWidth() / 2F);
					translateY = /*_display.getHeight() / 2F;*/(1 - scale) * (_display.getHeight() / 2F);
					matrixZoom.preTranslate(-translateX, -translateY);
					matrixZoom.setScale(scale, scale);
					matrixZoom.postTranslate(translateX, translateY);
					boolean oldAntiAlias = _paint.isAntiAlias();
					_paint.setAntiAlias(true);
					_canvas.drawBitmap(_bitmapInitial, matrixZoom, _paint);
					_paint.setAntiAlias(oldAntiAlias);
				}
				break;				
			case EDIT_ANGLE:
				
				float cx2, cy2, degree2;
				cx2 = _bitmapModified.getWidth() / 2;
				cy2 = _bitmapModified.getHeight() / 2;
				degree2 = downX;
				
				_matrix.reset();
				_matrix.setTranslate(-cx2, -cy2);  
				_matrix.setRotate(degree2, cx2, cy2);
				
				_bitmapModified = Bitmap.createBitmap(_bitmapInitial.getWidth(), _bitmapInitial.getHeight(), _bitmapInitial.getConfig());
				
				_canvas = new Canvas(_bitmapModified);	
				_canvas.drawColor(Color.TRANSPARENT);
				_canvas.drawBitmap(_bitmapInitial, _matrix, _paint);
				
				break;				
			case EFFECTS_BLACK_AND_WHITE:
				_matrix.reset();
				break;
			case EFFECTS_GRAYSCALE:
				_colorMatrix = new ColorMatrix();
				_colorMatrix.setSaturation(0);
				_paint.reset();
				_matrix.reset();
				initiatePaint();
				_paint.setColorFilter(new ColorMatrixColorFilter(_colorMatrix));
				_canvas.drawBitmap(_bitmapInitial, _matrix, _paint);
				break;
			case EFFECTS_NOISE:
				Config config = _bitmapInitial.getConfig();
				_bitmapInitial = addNoise(_bitmapInitial, downX).copy(config, true);
				_paint.reset();
				_matrix.reset();
				initiatePaint();
				_canvas.drawBitmap(_bitmapInitial, _matrix, _paint);
				break;
			case EFFECTS_TRANSPARENCY:
				float transparency = (downX / 100F);// * 255;//(100F - downX) / 100F;//logValue(0, 100, downX);
				_matrix.reset();
				_colorMatrix = new ColorMatrix();
				_colorMatrix.set(new float[]{
						1, 0, 0, transparency, 0,
						0, 1, 0, transparency, 0,
						0, 0, 1, transparency, 0,
						0, 0, 0, transparency, 0
				});
				//_paint.reset();
				//_paint.setColorFilter(new ColorMatrixColorFilter(_colorMatrix));
				//_paint.setARGB(100, 255, 255, 255);
				//_paint.setAlpha(0);
				//_paint.setARGB(a, r, g, b)
				
				
				/*
				_bitmapInitial = _bitmapInitial.copy(Config.ARGB_8888, true);
				////_canvas.drawColor(Color.TRANSPARENT);
				_canvas.drawBitmap(_bitmapInitial, _matrix, _paint);
				_paint.reset();
				initiatePaint();
				*/
				
				_paint.reset();
				_matrix.reset();
				initiatePaint();
				//_paint.setColorFilter(new ColorMatrixColorFilter(_colorMatrix));
				
				//_paint.setAlpha((int) Math.floor(transparency));
				//_paint.setARGB(30, 200, 0, 0);

				_paint.setColorFilter(new ColorMatrixColorFilter(_colorMatrix));
				_canvas.drawColor(Color.TRANSPARENT);
				_canvas.drawBitmap(_bitmapInitial, _matrix, _paint);			
				
				break;
			case EFFECTS_SATURATION:
				
				float saturation = logValue(0, 100, downX);
				_matrix.reset();
				_colorMatrix = new ColorMatrix();
				_colorMatrix.setSaturation(saturation);
				_paint.reset();
				initiatePaint();
				_paint.setColorFilter(new ColorMatrixColorFilter(_colorMatrix));
				_canvas.drawBitmap(_bitmapInitial, _matrix, _paint);
				break;
				
			case EFFECTS_BRIGHTNESS:
				float brightness = (downX - 50) * 2;//logValue(-100, 100, downX);
				_colorMatrix = new ColorMatrix();
				_colorMatrix.set(new float[]{
						1, 0, 0, 0, brightness,
						0, 1, 0, 0, brightness,
						0, 0, 1, 0, brightness,
						0, 0, 0, 1, 0
				});
				_paint.reset();
				_matrix.reset();
				initiatePaint();
				_paint.setColorFilter(new ColorMatrixColorFilter(_colorMatrix));
				_canvas.drawBitmap(_bitmapInitial, _matrix, _paint);			
				break;
			case EFFECTS_CONTRAST:
				float contrast = (downX / 100) + 1;//logValue(0, 100, downX);
				_colorMatrix = new ColorMatrix();
				_colorMatrix.set(new float[]{
						contrast, 0, 0, 0, 0,
						0, contrast, 0, 0, 0,
						0, 0, contrast, 0, 0,
						0, 0, 0, 1, 0
				});
				_paint.reset();
				_matrix.reset();
				initiatePaint();
				_paint.setColorFilter(new ColorMatrixColorFilter(_colorMatrix));
				_canvas.drawBitmap(_bitmapInitial, _matrix, _paint);
				break;
			case EFFECTS_NEGATIVE:
				_colorMatrix = new ColorMatrix();
				_colorMatrix.set(new float[]{
						-1, 0, 0, 0, 255,
						0, -1, 0, 0, 255,
						0, 0, -1, 0, 255,
						0, 0, 0, 1, 0
				});
				_paint.reset();
				_matrix.reset();
				initiatePaint();
				_paint.setColorFilter(new ColorMatrixColorFilter(_colorMatrix));
				_canvas.drawBitmap(_bitmapInitial, _matrix, _paint);				
				break;
			default:
				// Not implemented
			}
		}
		
		//_bitmapInitial.recycle();
		//_bitmapModified.recycle();
		
		return _bitmapModified;
	}
	
	private static Bitmap addNoise(Bitmap bitmap, float level)
	{
		// level should go from 0 to 255
		
		/*Bitmap bitmapWithNoise = bitmap.copy(bitmap.getConfig(), true);
		Random random = new Random();
		double noiseValue = 0;
		int colorValue, colorRedValue, colorGreenValue, colorBlueValue;
		
		for(int i = 0; i < bitmapWithNoise.getWidth(); ++i)
		{
			for(int j = 0; j < bitmapWithNoise.getHeight(); ++j)
			{
				noiseValue = random.nextGaussian() * level;
				
				colorValue = bitmapWithNoise.getPixel(i, j);
				colorRedValue = Color.red(colorValue) + (int)noiseValue;
				colorBlueValue = Color.blue(colorValue) + (int)noiseValue;
				colorGreenValue = Color.green(colorValue) + (int)noiseValue;
				
				colorRedValue = colorRedValue > 255 ? 255 : colorRedValue;
				colorBlueValue = colorBlueValue > 255 ? 255 : colorBlueValue;
				colorGreenValue = colorGreenValue > 255 ? 255 : colorGreenValue;
				
				colorRedValue = colorRedValue < 0 ? 0 : colorRedValue;
				colorBlueValue = colorBlueValue < 0 ? 0 : colorBlueValue;
				colorGreenValue = colorGreenValue < 0 ? 0 : colorGreenValue;
				
				bitmapWithNoise.setPixel(i, j, Color.rgb(colorRedValue, colorGreenValue, colorBlueValue));
			}
		}
		
		return bitmapWithNoise;*/
		
		
		// Scatter into four threads, each of which is processing one part of the image
		Bitmap bmpFirst, bmpSecond, bmpThird, bmpFourth, bmpResult;
		int halfWidth, halfHeight;
		halfWidth = bitmap.getWidth() / 2;
		halfHeight = bitmap.getHeight() / 2;
		Config config = bitmap.getConfig();
		
		bmpFirst = Bitmap.createBitmap(bitmap, 0, 0, halfWidth, halfHeight);
		bmpSecond = Bitmap.createBitmap(bitmap, halfWidth, 0, halfWidth, halfHeight);
		bmpThird = Bitmap.createBitmap(bitmap, 0, halfHeight, halfWidth, halfHeight);
		bmpFourth = Bitmap.createBitmap(bitmap, halfWidth, halfHeight, halfWidth, halfHeight);
		bmpResult = Bitmap.createBitmap(halfWidth * 2, halfHeight * 2, config); // empty bitmap 
		
		ImageProcessorThread threadFirst = new ImageProcessorThread(bmpFirst, Availables.EFFECTS_NOISE, level);
		ImageProcessorThread threadSecond = new ImageProcessorThread(bmpSecond, Availables.EFFECTS_NOISE, level);
		ImageProcessorThread threadThird = new ImageProcessorThread(bmpThird, Availables.EFFECTS_NOISE, level);
		ImageProcessorThread threadFourth = new ImageProcessorThread(bmpFourth, Availables.EFFECTS_NOISE, level);
		
		threadFirst.run();
		threadSecond.run();
		threadThird.run();
		threadFourth.run();
		
		boolean _running = true;
		while(_running)
		{
			_running = threadFirst.isAlive() || threadSecond.isAlive() || threadThird.isAlive() || threadFourth.isAlive();
			try {
				Thread.sleep(50); // sleep for half of a second
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
		}
		
		bmpFirst = threadFirst.getBitmap();
		bmpSecond = threadSecond.getBitmap();
		bmpThird = threadThird.getBitmap();
		bmpFourth = threadFourth.getBitmap();
		
		Canvas canvas = new Canvas(bmpResult);
		Paint paint = new Paint();
		paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_OVER));
		canvas.drawBitmap(bmpFirst, 0, 0, paint);
		canvas.drawBitmap(bmpSecond, halfWidth, 0, paint);
		canvas.drawBitmap(bmpThird, 0, halfHeight, paint);
		canvas.drawBitmap(bmpFourth, halfWidth, halfHeight, paint);
		
		return bmpResult;
	}
	
	float logValue(int minValue, int maxValue, float progress)
	{
		float min, max, minLogValue, maxLogValue, scale, retValue;
		retValue = 0;
		if(minValue >= 0)
		{
			minLogValue = (float) Math.log1p((double) minValue);
			maxLogValue = (float) Math.log1p((double) maxValue);
			scale = (maxLogValue - minLogValue) / 100F;
			retValue = (float) Math.exp((double)(minLogValue + scale * (progress)));
		}
		else
		{
			boolean negative = progress < 50;
			
			minLogValue = (float) Math.log1p((double) 0);
			maxLogValue = (float) Math.log1p((double) maxValue);
			scale = (maxLogValue - minLogValue) / 50F;
			retValue = (float) Math.exp((double)(minLogValue + scale * (Math.abs(progress - 50))));
			retValue = retValue * (negative ? -1 : 1);
		}
		
		return retValue;
	}
	
	/*
	function logslider(value) {   
		// value will be between 0 and 100   
		var min = 0;   
		var max = 100;    
		// The result should be between 100 an 10000000   
		var minv = Math.log(100);   
		var maxv = Math.log(10000000);    
		// calculate adjustment factor   
		var scale = (maxv-minv) / (max-min);    
		return Math.exp(minv + scale*(value-min)); 
		}
	*/
}
