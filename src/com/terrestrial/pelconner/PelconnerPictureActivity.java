package com.terrestrial.pelconner;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.ArrayList;

import com.terrestrial.pelconner.R;
import com.terrestrial.pelconner.dialogs.PelconnerAboutDialog;
import com.terrestrial.pelconner.dialogs.PelconnerColorDialog;
import com.terrestrial.pelconner.dialogs.PelconnerDialog;
import com.terrestrial.pelconner.dialogs.PelconnerOptionDialog;
import com.terrestrial.pelconner.dialogs.PelconnerSaveDialog;
import com.terrestrial.pelconner.dialogs.PelconnerTextDialog;
import com.terrestrial.pelconner.helper.ImageData;
import com.terrestrial.pelconner.helper.ImageInfo;
import com.terrestrial.pelconner.helper.PelconnerBitmapModifier;
import com.terrestrial.pelconner.helper.PelconnerOption;
import com.terrestrial.pelconner.helper.PelconnerOption.Availables;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.LayoutInflater.Factory;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.BadTokenException;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import android.widget.ZoomButton;

public class PelconnerPictureActivity extends PelconnerActivity implements OnDismissListener, OnTouchListener, SensorEventListener, OnSeekBarChangeListener, OnClickListener {
	
	//protected String TAG = "PelconnerPictureActivity";
	private final int ID_DIALOG_PROCESSING = 110;
	
	/**
	 * Designates whether the picture is "dirty"
	 */
	private boolean _dirty;
	private boolean _dirtyImport; // Used to detect if the imported image is moved or not.
	private Bitmap _bitmapFinal;
	private Bitmap _bitmapDisplayed;
	private Bitmap _bitmapModified;
	private Bitmap _bitmapTemp;
//	private Bitmap _bitmapBackground;
	private PelconnerBitmapModifier _pelconnerBitmapModifier;
	private ImageView _imageViewPicturePreview;
	private RelativeLayout _relativeLayoutContainer;
	private PelconnerOption.Availables _selectedAction;
	
	// Touch events
	private float _startX = 0;
	private float _startY = 0;
	private float _startX1 = 0;
	private float _startY1 = 0;	
	private float _downX = 0;
	private float _downY = 0;
	private float _upX = 0;
	private float _upY = 0;
	private float _startDistance = 0;
	private PointF _pinchCentralPoint;
	
	// Color
	private int _color = Color.WHITE; // Initial color
	
	// Accelerometer variables
	private float _lastX, _lastY, _lastZ;
	private float _currentX, _currentY, _currentZ;
	private long _lastTime = -1;
	private long _currentTime = -1;
	private final double _tresholdMin = 1.5E-8;//7E-8;
	private final double _tresholdMax = 1.5E-7;
	private Sensor _accelerometer = null;
	SensorManager _sensorManager = null;
	
	// Orientation variables
	private float _startAzimuth, _currentAzimuth;
	private Sensor _orientation = null;
	
	// Seek Bars - Effects
	private SeekBar _seekBarSaturation = null;
	private SeekBar _seekBarBrightness = null;
	private SeekBar _seekBarContrast = null;
	private SeekBar _seekBarTransparency = null;
	private SeekBar _seekBarBlackAndWhite = null;
	private SeekBar _seekBarNoise = null;
	
	private ZoomButton _zoomButton = null;
	
	// Seek Bars - menu options
	private SeekBar _seekBarLineWidth = null; 
	private SeekBar _seekBarAngle = null;
	private SeekBar _seekBarZoom = null;
	
	ProgressDialog _progressDialog = null;
	Thread threadProcessing = null;
	Runnable _runnableToggleDialog = null;
	boolean _blockSeekBar = false;
	
	private Handler _handlerProcessing = null;
	
	private enum ActionType
	{
		NONE,
		IMPORT_BMP,
		IMPORT_URI,
		EDIT, // Edit without the imports
		TOOL,
		//DRAW, // An option chosen from Tools menu for drawing objects
		//SHAKE, // Shake Draw - contains a list of drown lines
		EFFECT, // An option chosen from Effects menu
		FLATTEN, // If the modifications are submitted in the final Bitmap image.
		COLOR // Color has been changed.
	};
	
	private class Action
	{
		private ActionType _type;
		private Availables _selectedOption;
		private ArrayList<Object> _params; // Actual object depends of the ActionType
		
		// IDEA:
		// Ukoliko se importuje slike, _object bi trebalo da bude importovana slika u obliku Bitmap-a ili URI-a.
		// Za slucaj ostalih, _object bi trebalo da bude tip akcije, a _params bi trebalo da budu odgovarajuci parametri
		// SHAKE: _params bi trebalo da sadrzi zavrsne tacke svake linije koja je nacrtana. Na taj nacin bi _params
		// bio u sledecem obliku: downX1, downY1, downX2, downY2,....
		
		// Ukoliko je FLATTEN poslednja operacija, onda se trenutno izabrana opcija iz Edit/Effects-a primenjuje na _bimapFinal
		
		public Action()
		{
			_type = ActionType.NONE;
			_params = new ArrayList<Object>();
			_selectedOption = Availables.NONE;
		}
		
		/*public void setType(ActionType type)
		{
			_type = type;
		}*/
		
		public ActionType getType()
		{
			return _type;
		}
		
		public void setOption(Availables option)
		{
			_selectedOption = option;
		}
		
		public Availables getOption()
		{
			return _selectedOption;
		}
		
		public void addParam(Object param)
		{
			_params.add(param);
			//Log.d("ACTION", "Added param: " + param.toString());
		}
		
		public Object getParam(int index)
		{
			Object param = null;
			if(_params.size() > 0)
			{
				if(index < _params.size())
				{
					param = _params.get(index);
				}
				else
				{
					param = _params.get(_params.size() - 1); // Getting the last object
				}
			}
			else
			{
				param = new Object();
			}
			return param;
		}
	}
	
	private Action _currentAction = null;
	private ArrayList<Action> _listOfActions = null;
	
	private boolean hasAccelerometer()
	{
		_sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> availableSensors = _sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if(availableSensors.size() > 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private void changeBitmapModified(Bitmap newBitmapModified)
	{
		if(_bitmapModified != null)
		{
			//_bitmapModified.recycle();
			_bitmapModified = null;
		}
		_bitmapModified = newBitmapModified;
	}
	
	private void actionSelected(Availables selectedOption) {
		if(selectedOption == Availables.EDIT_NEW)
		{
			_selectedAction = Availables.EDIT_NEW;
			// TODO: treba proveriti da li je slika "dirty" i ukoliko jeste, prikazati dijalog sa odgovarajucim pitanjem.
			// Dijalog treba da ima tri dugmeta: Yes, No, Cancel.
			// Yes: Otvara se Save dijalog
			// No: Pokrece se nova slika
			// Cancel: Nista se ne preduzima i ostaje se na trenutnoj slici
			initPicturePreview();
			// TODO: Proveri sta treba da se uradi sa listom akcija, itd.
			
			/*
	        new Handler().postDelayed(new Runnable() { 
	            public void run() {
	                openOptionsMenu(); 
	            } 
	        }, 1500);
	        */
	        
	        // We are starting a new "session" --> remove all current actions
	        _listOfActions.clear();
		}
		else if(selectedOption == Availables.EDIT_IMPORT_GALLERY)
		{
			startPelconnerActivity(Type.GALLERY);
		}
		else if(selectedOption == Availables.EDIT_IMPORT_CAMERA)
		{
			startPelconnerActivity(Type.CAMERA);
		}
		else if(selectedOption == Availables.EDIT_ZOOM)
		{
			/*
			_zoomButton = new ZoomButton(this);
			
			_zoomButton.setOnClickListener(this);
			
			//android.widget.RelativeLayout.LayoutParams params = new LayoutParams();
			_relativeLayoutContainer.addView(_zoomButton);
			*/
			
			// TODO Zoom seekbar is displayed with set of zoom values - default position should be the center of the seekbar.
			_seekBarZoom = new SeekBar(this);
			_seekBarZoom.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			//_seekBarZoom.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar));
			//_seekBarZoom.setIndeterminateDrawable(getResources().getDrawable(R.drawable.seekbar));
			_seekBarZoom.setBackgroundDrawable(getResources().getDrawable(R.drawable.seekbar_normal_background));
			_seekBarZoom.setProgressDrawable(getResources().getDrawable(R.color.transparent));
			_seekBarZoom.setThumb(getResources().getDrawable(R.drawable.seekbar_normal_thumb));
			_seekBarZoom.setPadding(8, 0, 8, 0);
			// Left half is for negative zoom, and the right half is for positive zoom 
			_seekBarZoom.setMax(200);
			_seekBarZoom.setProgress(100); // In the middle of the seek bar - no zoom.
			_seekBarZoom.setOnSeekBarChangeListener(this);
			_relativeLayoutContainer.addView(_seekBarZoom);
		}
		
		/*
		else if(selectedOption == Availables.EDIT_MOVE)
		{
			// TODO Perform operation
		}
		else if(selectedOption == Availables.EDIT_ROTATE)
		{
			// TODO Perform operation
		}
		else if(selectedOption == Availables.EDIT_RESIZE)
		{
			// TODO Perform operation
		}
		else if(selectedOption == Availables.EDIT_STRETCH)
		{
			// TODO Perform operation
		}
		else if(selectedOption == Availables.EDIT_PINCH_ZOOM)
		{
			// TODO Perform operation
		}
		else if(selectedOption == Availables.EDIT_CROP)
		{
			// TODO Perform operation
		}
		else if(selectedOption == Availables.TOOLS_FILL)
		{
			// TODO Perform operation
		}
		else if(selectedOption == Availables.TOOLS_RECTANGLE_SELECT)
		{
			// TODO Perform operation
		}
		else if(selectedOption == Availables.TOOLS_CIRCLE_SELECT)
		{
			// TODO Perform operation
		}
		else if(selectedOption == Availables.TOOLS_DRAW)
		{
			// TODO Perform operation
		}
		else if(selectedOption == Availables.TOOLS_RECTANGLE)
		{
			// TODO Perform operation
		}
		else if(selectedOption == Availables.TOOLS_ELLIPSE)
		{
			// TODO Perform operation
		}
		else if(selectedOption == Availables.TOOLS_SHAKE_DRAW)
		{
			// TODO Perform operation
		}
		else if(selectedOption == Availables.TOOLS_LINE)
		{
			// TODO Perform operation
		}*/
		else if(selectedOption == Availables.MORE_ABOUT)
		{
			PelconnerAboutDialog aboutDialog = new PelconnerAboutDialog(this);
			aboutDialog.show();
		}
		else if(selectedOption == Availables.MORE_HELP)
		{
			startPelconnerActivity(PelconnerActivity.Type.HELP_PICTURE);
		}
		else if(selectedOption == Availables.MORE_QUIT)
		{
			this.shutDown(true);
		}
		else if(selectedOption == Availables.TOOLS_SHAKE_DRAW)
		{
			_selectedAction = selectedOption;
			
			_sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
			List<Sensor> availableSensors = _sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
			if(availableSensors.size() > 0)
			{
				_accelerometer = availableSensors.get(0);
				
				_sensorManager.registerListener(this, _accelerometer, SensorManager.SENSOR_DELAY_GAME);
				
				_lastX = -1000;
				_lastY = -1000;
			}
			else
			{
				// TODO: Accelerometer does not exist on the phone!
				_accelerometer = null;
			}
		}
		else if(selectedOption == Availables.EDIT_RESIZE)
		{
			_selectedAction = selectedOption;
			
			_sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
			List<Sensor> availableSensors = _sensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
			if(availableSensors.size() > 0)
			{
				_orientation = availableSensors.get(0);
				
				_sensorManager.registerListener(this, _orientation, SensorManager.SENSOR_DELAY_FASTEST);
				
				_startAzimuth = _currentAzimuth = -1000;
			}
			else
			{
				// TODO: Accelerometer does not exist on the phone!
				_orientation = null;
			}			
		}
		else if(selectedOption == Availables.TOOLS_TEXT)
		{
			_selectedAction = selectedOption;
			
			PelconnerTextDialog dialogText = new PelconnerTextDialog(this);
			dialogText.setOnDismissListener(this);
			dialogText.show();
		}
		else if(selectedOption == Availables.TOOLS_COLOR_TEST)
		{
			_selectedAction = selectedOption;
			
			int color = Color.argb(200, 200, 0, 0); // Transparent red....
			
			PelconnerColorDialog dialogColor = new PelconnerColorDialog(this, color);
			dialogColor.setOnDismissListener(this);
			dialogColor.show();
		}
		else if(selectedOption == Availables.EFFECTS_BLACK_AND_WHITE)
		{
			_selectedAction = selectedOption;
			_seekBarBlackAndWhite = new SeekBar(this);
			_seekBarBlackAndWhite.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			//_seekBarBlackAndWhite.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar));
			//_seekBarBlackAndWhite.setIndeterminateDrawable(getResources().getDrawable(R.drawable.seekbar));
			_seekBarBlackAndWhite.setBackgroundDrawable(getResources().getDrawable(R.drawable.seekbar_normal_background));
			_seekBarBlackAndWhite.setProgressDrawable(getResources().getDrawable(R.color.transparent));
			_seekBarBlackAndWhite.setThumb(getResources().getDrawable(R.drawable.seekbar_normal_thumb));
			_seekBarBlackAndWhite.setPadding(8, 0, 8, 0);
			_seekBarBlackAndWhite.setOnSeekBarChangeListener(this);
			_relativeLayoutContainer.addView(_seekBarBlackAndWhite);
		}
		else if(selectedOption == Availables.EFFECTS_BRIGHTNESS)
		{
			_selectedAction = selectedOption;
			_seekBarBrightness = new SeekBar(this);
			_seekBarBrightness.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			//_seekBarBrightness.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar));
			//_seekBarBrightness.setIndeterminateDrawable(getResources().getDrawable(R.drawable.seekbar));
			_seekBarBrightness.setBackgroundDrawable(getResources().getDrawable(R.drawable.seekbar_normal_background));
			_seekBarBrightness.setProgressDrawable(getResources().getDrawable(R.color.transparent));
			_seekBarBrightness.setThumb(getResources().getDrawable(R.drawable.seekbar_normal_thumb));
			_seekBarBrightness.setPadding(8, 0, 8, 0);
			_seekBarBrightness.setProgress(50);
			_seekBarBrightness.setOnSeekBarChangeListener(this);
			_relativeLayoutContainer.addView(_seekBarBrightness);			
		}
		else if(selectedOption == Availables.EFFECTS_CONTRAST)
		{
			_selectedAction = selectedOption;
			_seekBarContrast = new SeekBar(this);
			_seekBarContrast.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			//_seekBarContrast.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar));
			//_seekBarContrast.setIndeterminateDrawable(getResources().getDrawable(R.drawable.seekbar));
			_seekBarContrast.setBackgroundDrawable(getResources().getDrawable(R.drawable.seekbar_normal_background));
			_seekBarContrast.setProgressDrawable(getResources().getDrawable(R.color.transparent));
			_seekBarContrast.setThumb(getResources().getDrawable(R.drawable.seekbar_normal_thumb));
			_seekBarContrast.setPadding(8, 0, 8, 0);
			_seekBarContrast.setProgress(50);
			_seekBarContrast.setOnSeekBarChangeListener(this);
			_relativeLayoutContainer.addView(_seekBarContrast);	
		}
		else if(selectedOption == Availables.EFFECTS_GRAYSCALE)
		{
			_selectedAction = selectedOption;
			changeBitmapModified(_pelconnerBitmapModifier.modify(_selectedAction, 0, 0, 0, 0, null));
			updateDisplay(false);
			
			_currentAction = new Action();
			_currentAction.setOption(_selectedAction);
			// For Grayscale, no params are needed
			addAction();
			
		}
		else if(selectedOption == Availables.EFFECTS_NEGATIVE)
		{
			_selectedAction = selectedOption;
			changeBitmapModified(_pelconnerBitmapModifier.modify(_selectedAction, 0, 0, 0, 0, null));
			updateDisplay(false);
			
			_currentAction = new Action();
			_currentAction.setOption(_selectedAction);
			// For Negative, no params are needed
			addAction();
		}
		else if(selectedOption == Availables.EFFECTS_NOISE)
		{
			_selectedAction = selectedOption;
			_seekBarNoise = new SeekBar(this);
			_seekBarNoise.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			_seekBarNoise.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar));
			_seekBarNoise.setIndeterminateDrawable(getResources().getDrawable(R.drawable.seekbar));
			_seekBarNoise.setPadding(8, 5, 8, 0);
			_seekBarNoise.setOnSeekBarChangeListener(this);
			_relativeLayoutContainer.addView(_seekBarNoise);			
		}
		else if(selectedOption == Availables.EFFECTS_SATURATION)
		{
			_selectedAction = selectedOption;
			_seekBarSaturation = new SeekBar(this);
			_seekBarSaturation.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			//_seekBarSaturation.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar));
			//_seekBarSaturation.setIndeterminateDrawable(getResources().getDrawable(R.drawable.seekbar));
			_seekBarSaturation.setBackgroundDrawable(getResources().getDrawable(R.drawable.seekbar_normal_background));
			_seekBarSaturation.setProgressDrawable(getResources().getDrawable(R.color.transparent));
			_seekBarSaturation.setThumb(getResources().getDrawable(R.drawable.seekbar_normal_thumb));
			_seekBarSaturation.setPadding(8, 0, 8, 0);
			_seekBarSaturation.setOnSeekBarChangeListener(this);
			_relativeLayoutContainer.addView(_seekBarSaturation);
		}
		else if(selectedOption == Availables.EFFECTS_TRANSPARENCY)
		{
			_selectedAction = selectedOption;
			_seekBarTransparency = new SeekBar(this);
			_seekBarTransparency.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			//_seekBarTransparency.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar));
			//_seekBarTransparency.setIndeterminateDrawable(getResources().getDrawable(R.drawable.seekbar));
			_seekBarTransparency.setBackgroundDrawable(getResources().getDrawable(R.drawable.seekbar_normal_background));
			_seekBarTransparency.setProgressDrawable(getResources().getDrawable(R.color.transparent));
			_seekBarTransparency.setThumb(getResources().getDrawable(R.drawable.seekbar_normal_thumb));
			_seekBarTransparency.setPadding(8, 0, 8, 0);
			_seekBarTransparency.setProgress(100);
			_seekBarTransparency.setOnSeekBarChangeListener(this);
			_relativeLayoutContainer.addView(_seekBarTransparency);			
		}
		else
		{
			// Tools/Edit/Effects options that modify the displayed image
			_selectedAction = selectedOption;
		}
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
	
	void finishAndBack()
	{
		// TODO: start Main intent and finish with this activity
		startActivity(new Intent(PelconnerPictureActivity.this, PelconnerMainActivity.class));
		
		/*
		Intent intentMain = PelconnerActivity.getReference(PelconnerActivity.ACTIVITY_TAG_MAIN);
		startActivity(intentMain);
		*/
		
		// Remove the reference from the list
		boolean bResult = PelconnerActivity.removeReference(PelconnerPictureActivity.this);
		
		if(!bResult)
		{
			//Log.e(TAG, "Reference not removed from the list!");
		}
		
        // Finishing with this activity
        PelconnerPictureActivity.this.finish();		
	}
	
	private void initPicturePreview(Bitmap bitmapImage)
	{
		/*
		_selectedAction = Availables.NONE;
		
		_imageViewPicturePreview = (ImageView)findViewById(R.id.imageViewPicturePreview);
		
        _pelconnerBitmapModifier = new PelconnerBitmapModifier(this, getWindowManager().getDefaultDisplay(), bitmapImage);
        _bitmapModified = _pelconnerBitmapModifier.getBitmap();
        _bitmapFinal = _bitmapModified.copy(_bitmapModified.getConfig(), true); 
		
        //_bitmapDisplayed = BitmapFactory.decodeResource(getResources(), R.drawable.ic_picture_background);
        //_bitmapDisplayed = Bitmap.createBitmap(_bitmapFinal, 0, 0, 
        //		getWindowManager().getDefaultDisplay().getWidth(), getWindowManager().getDefaultDisplay().getHeight());
        
        initializeDefaultDisplay();
        
        Canvas canvasBackground = new Canvas(_bitmapDisplayed);
        Paint paintBackground = new Paint();
        paintBackground.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
        canvasBackground.drawBitmap(_bitmapFinal, 0, 0, paintBackground);
		
        _imageViewPicturePreview.setImageBitmap(_bitmapDisplayed);
		//_imageViewPicturePreview.setImageBitmap(_bitmapModified);
		_imageViewPicturePreview.setOnTouchListener(this);
		*/
		
		initPicturePreview();
		
		changeBitmapModified(_pelconnerBitmapModifier.insertBitmap(bitmapImage, true));
		
		/*_imageViewPicturePreview.setImageBitmap(_bitmapBackground);
		_imageViewPicturePreview.invalidate();*/
		
		//_bitmapModified = _pelconnerBitmapModifier.modify(_selectedAction, _downX, _downY, _upX, _upY, motionEvent);
		/*Bitmap bitmapHolder = Bitmap.createBitmap(getWindowManager().getDefaultDisplay().getWidth(), getWindowManager().getDefaultDisplay().getHeight(), Config.ARGB_8888); // Bitmap.createBitmap(_bitmapBackground);
		bitmapHolder = bitmapHolder.copy(bitmapHolder.getConfig(), true);
		Canvas canvasDisplay = new Canvas(bitmapHolder);
		Paint paintDisplay = new Paint();
		paintDisplay.setAlpha(255);
		
		paintDisplay.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
		canvasDisplay.drawBitmap(_bitmapModified, 0, 0, paintDisplay);*/
		
		_imageViewPicturePreview.setImageBitmap(/*bitmapHolder*/_bitmapModified);
		_imageViewPicturePreview.invalidate();
	}
	
	private void initPicturePreview(Uri uriImage) 
	{
		/*
		_selectedAction = Availables.NONE;
		
        _imageViewPicturePreview = (ImageView)findViewById(R.id.imageViewPicturePreview);
        
        _pelconnerBitmapModifier = new PelconnerBitmapModifier(this, getWindowManager().getDefaultDisplay(), uriImage);
        _bitmapModified = _pelconnerBitmapModifier.getBitmap();
        _bitmapFinal = _bitmapModified.copy(_bitmapModified.getConfig(), true); 
        
        //_bitmapDisplayed = BitmapFactory.decodeResource(getResources(), R.drawable.ic_picture_background);
        //_bitmapDisplayed = Bitmap.createBitmap(_bitmapFinal, 0, 0, 
        //		getWindowManager().getDefaultDisplay().getWidth(), getWindowManager().getDefaultDisplay().getHeight());
		
        initializeDefaultDisplay();
        
        Canvas canvasBackground = new Canvas(_bitmapDisplayed);
        Paint paintBackground = new Paint();
        paintBackground.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
        canvasBackground.drawBitmap(_bitmapFinal, 0, 0, paintBackground);
        
        _imageViewPicturePreview.setImageBitmap(_bitmapDisplayed);
		//_imageViewPicturePreview.setImageBitmap(_bitmapModified);
		_imageViewPicturePreview.setOnTouchListener(this);		
		*/
		
		initPicturePreview();
        
		try
		{
			//Log.d("SHARE", "Imported location: " + uriImage.getPath());
			///external/images/media/113
			//MediaStore.Images.Media.q
			
			Bitmap bitmapInitial = null;
			
			boolean phisicalLocation = uriImage.getPath().toString().endsWith(".jpg") || uriImage.getPath().toString().endsWith(".png");
			
			Uri uriImageFile = Uri.EMPTY;
			
			if(!phisicalLocation)
			{
				//Log.d("PHY", "It's in MediaStore");
				//uriImageFile = Uri.parse("content://" + uriImage.getPath());
				//bitmapInitial = MediaStore.Images.Media.getBitmap(getContentResolver(), uriImage);
				
				/*
				List<String> pathSegments = uriImage.getPathSegments();
				String stringID = pathSegments.get(pathSegments.size() - 1); // Fetching last path segment containing image ID
				*/
				
				
				Cursor cursor = managedQuery(uriImage, null, null, null, null);
				cursor.moveToFirst();
				//Log.d("PHY", "Got cursor!");
				int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				//Log.d("PHY", "data column index is " + dataColumn);
				String bitmapPath = cursor.getString(dataColumn);
				//Log.d("PHY", "bitmapPath is: " + bitmapPath);
				bitmapInitial = getBitmap(bitmapPath);
				
				
				//Log.d("PHY", "width/height = " + bitmapInitial.getWidth() + "/" + bitmapInitial.getHeight());
			}
			else
			{
				//Log.d("PHY", "It's on SD");
		        uriImageFile = Uri.parse("file://" + uriImage.getPath());
		        float width, height;
		        width = getWindowManager().getDefaultDisplay().getWidth();
		        height = getWindowManager().getDefaultDisplay().getHeight();
		        
				BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
				bmpFactoryOptions.inJustDecodeBounds = true;
				bitmapInitial = BitmapFactory.decodeStream(getContentResolver().openInputStream(uriImageFile), 
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
				
				bitmapInitial = BitmapFactory.decodeStream(getContentResolver().openInputStream(uriImageFile), 
						null, 
						bmpFactoryOptions);
			}
			
			changeBitmapModified(_pelconnerBitmapModifier.insertBitmap(bitmapInitial, false));
    		
    		/*
			_imageViewPicturePreview.setImageBitmap(_bitmapBackground);
			_imageViewPicturePreview.invalidate();
			
			//_bitmapModified = _pelconnerBitmapModifier.modify(_selectedAction, _downX, _downY, _upX, _upY, motionEvent);
			Bitmap bitmapHolder = null;//Bitmap.createBitmap(_bitmapBackground);
			try
			{
				//bitmapHolder = bitmapHolder.copy(bitmapHolder.getConfig(), true);
				bitmapHolder = _bitmapBackground.copy(_bitmapBackground.getConfig(), true);
			}
			catch(OutOfMemoryError ooem)
			{
				bitmapHolder = Bitmap.createBitmap(_bitmapBackground);
			}
			Canvas canvasDisplay = new Canvas(bitmapHolder);
			Paint paintDisplay = new Paint();
			paintDisplay.setAlpha(255);

			
			paintDisplay.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
			canvasDisplay.drawBitmap(_bitmapModified, 0, 0, paintDisplay);
			*/
			
			_imageViewPicturePreview.setImageBitmap(/*bitmapHolder*/_bitmapModified);
			_imageViewPicturePreview.invalidate();
		}
		catch(Exception e)
		{
			// TODO error handling...
		}
	}
	
	private Bitmap getBitmap(String bitmapPath)
	{
		//Log.d("PHY", "getBitmap entered...");
		BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
		bitmapFactoryOptions.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath, bitmapFactoryOptions);
		
		int heightRatio = (int)Math.ceil(bitmapFactoryOptions.outHeight / (float)(getWindowManager().getDefaultDisplay().getHeight()));
		int widthRatio = (int)Math.ceil(bitmapFactoryOptions.outWidth / (float)(getWindowManager().getDefaultDisplay().getWidth()));
		
		if(heightRatio > 1 && widthRatio > 1)
		{
			if(heightRatio > widthRatio)
			{
				bitmapFactoryOptions.inSampleSize = heightRatio;
			}
			else
			{
				bitmapFactoryOptions.inSampleSize = widthRatio;
			}
		}
		
		bitmapFactoryOptions.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeFile(bitmapPath, bitmapFactoryOptions);
		
		return bitmap;
	}
	
	private void initPicturePreview()
	{
        _imageViewPicturePreview = (ImageView)findViewById(R.id.imageViewPicturePreview);
        
		if(!(_listOfActions != null && _listOfActions.size() > 0) || _selectedAction == Availables.EDIT_NEW)
		{
			if(_pelconnerBitmapModifier != null)
				_pelconnerBitmapModifier.clear();
			_pelconnerBitmapModifier = new PelconnerBitmapModifier(this, getWindowManager().getDefaultDisplay());
		}
		
		changeBitmapModified(_pelconnerBitmapModifier.getBitmap());
		
        try
        {
        	_bitmapFinal = _bitmapModified.copy(_bitmapModified.getConfig(), true);
        }
        catch(OutOfMemoryError ooem)
        {
        	Log.e("OOM", "initPicturePreview");
        	_bitmapFinal = _bitmapModified;
        }
		
		if(!(_listOfActions != null && _listOfActions.size() > 0) || _selectedAction == Availables.EDIT_NEW)
		{
			_selectedAction = Availables.NONE;
			initializeDefaultDisplay();
		}

        _imageViewPicturePreview.setImageBitmap(_bitmapDisplayed);
		//_imageViewPicturePreview.setImageBitmap(_bitmapModified);
		_imageViewPicturePreview.setOnTouchListener(this);
	}

	private void initializeDefaultDisplay()
	{
        float displayWidth, displayHeight, logoWidth, logoHeight, logoInitialWidth, logoInitialHeight, scale;
        displayWidth = getWindowManager().getDefaultDisplay().getWidth();
        displayHeight = getWindowManager().getDefaultDisplay().getHeight();
        logoWidth = 0.6F * displayWidth;
        logoHeight = logoWidth;
        
        /*
		BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
    	//bitmapFactoryOptions.inSampleSize = 8;
    	bitmapFactoryOptions.inJustDecodeBounds = true;
    	Bitmap bitmap = BitmapFactory.decodeFile(_selectedImage.getImageFileUri().getPath(), bitmapFactoryOptions);
    	
    	int heightRatio = (int)Math.ceil((float)bitmapFactoryOptions.outHeight / (float)displayHeight);
    	int widthRatio = (int)Math.ceil((float)bitmapFactoryOptions.outWidth / (float)displayWidth);
    	
    	if(heightRatio > 1 && widthRatio > 1)
    	{
    		if(heightRatio > widthRatio)
    		{
    			bitmapFactoryOptions.inSampleSize = heightRatio;
    		}
    		else
    		{
    			bitmapFactoryOptions.inSampleSize = widthRatio;
    		}
    	}
    	
    	bitmapFactoryOptions.inJustDecodeBounds = false;
    	
    	float bitmapHeight, bitmapWidth, aspectRatio, aspectRatioDisplay;    	
    	
    	bitmap = BitmapFactory.decodeFile(_selectedImage.getImageFileUri().getPath(), bitmapFactoryOptions);
    	
    	bitmapHeight = bitmap.getHeight();
    	bitmapWidth = bitmap.getWidth();
        */
        
        int heightRatio, widthRatio;
        
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inJustDecodeBounds = false;
        //bitmapOptions.outHeight = displayHeight;
        //bitmapOptions.inSampleSize = 12;
        bitmapOptions.outWidth = (int) displayWidth;
        bitmapOptions.outHeight = (int) displayHeight;
        
        try
        {
        	
        	_bitmapDisplayed = Bitmap.createBitmap((int)displayWidth, (int)displayHeight, Config.ARGB_8888);
        }
        catch(OutOfMemoryError oome)
        {
        	Log.e("OOM", "initializeDefaultDisplay");
        	_bitmapDisplayed = Bitmap.createBitmap((int)displayWidth, (int)displayHeight, Config.ALPHA_8);
        }
        
        /*
        Bitmap bitmapDisplayed = null;
        try
        {
        	bitmapDisplayed = BitmapFactory.decodeResource(getResources(), R.drawable.ic_picture_background, bitmapOptions);
        }
        catch(OutOfMemoryError ooem)
        {
        	bitmapOptions.inSampleSize = 8;
        	bitmapDisplayed = BitmapFactory.decodeResource(getResources(), R.drawable.ic_picture_background, bitmapOptions);
        }
        	
        bitmapOptions.outHeight = (int)logoHeight;
        bitmapOptions.outWidth = (int)logoWidth;
        
        Bitmap tempBitmap = null;
        try
        {
        	tempBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_picture_background_front);
        }
        catch(OutOfMemoryError ooem)
        {
        	tempBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_picture_background_front, bitmapOptions);
        }
        
        logoInitialWidth = tempBitmap.getWidth();
        logoInitialHeight = tempBitmap.getHeight();
        scale = logoWidth / logoInitialWidth;
        
        //Canvas canvasLogo = new Canvas(tempBitmap);
        //canvasLogo.scale(scale, scale);
        int margin = (int)Math.floor((displayWidth - logoWidth) / 2); 
        
        Matrix matrixLogo = new Matrix();
        matrixLogo.preScale(scale, scale);
        matrixLogo.postTranslate(margin, margin);
        
        float displayedWidth = _bitmapDisplayed.getWidth();
        float displayedHeight = _bitmapDisplayed.getHeight();
        //_bitmapDisplayed = _bitmapDisplayed.copy(bitmapDisplayed.getConfig(), true);
        Canvas canvasBackground = new Canvas(_bitmapDisplayed);
        Paint paintBackground = new Paint();
        paintBackground.setAlpha(255);
        //canvasBackground.drawColor(Color.CYAN);
        //paintBackground.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
        
        canvasBackground.drawBitmap(bitmapDisplayed, 0, 0, paintBackground);
        if(displayHeight > bitmapDisplayed.getHeight() && displayWidth > bitmapDisplayed.getWidth())
        {
        	canvasBackground.drawBitmap(bitmapDisplayed, 0, bitmapDisplayed.getHeight(), paintBackground);
        	canvasBackground.drawBitmap(bitmapDisplayed, bitmapDisplayed.getWidth(), 0, paintBackground); 
        	canvasBackground.drawBitmap(bitmapDisplayed, bitmapDisplayed.getWidth(), bitmapDisplayed.getHeight(), paintBackground);
        }
        else if(displayHeight > bitmapDisplayed.getHeight())
        {
        	canvasBackground.drawBitmap(bitmapDisplayed.copy(bitmapDisplayed.getConfig(), false), 0, bitmapDisplayed.getHeight(), paintBackground);
        }
        else if(displayWidth > bitmapDisplayed.getWidth())
        {
        	canvasBackground.drawBitmap(bitmapDisplayed.copy(bitmapDisplayed.getConfig(), false), bitmapDisplayed.getWidth(), 0, paintBackground);
        }
        
        
        canvasBackground.drawBitmap(tempBitmap.copy(tempBitmap.getConfig(), false), matrixLogo, paintBackground);
        */
        
        
        /*
        tempBitmap.recycle();
        bitmapDisplayed.recycle();
        */
		//canvasBackground.drawBitmap(tempBitmap, margin, margin, paintBackground);
        /*
        try
        {
        	_bitmapBackground = _bitmapDisplayed.copy(_bitmapDisplayed.getConfig(), false);
        }
        catch(OutOfMemoryError oome)
        {
        	_bitmapBackground = _bitmapDisplayed.copy(Config.ALPHA_8, false);
        }
        */
	}
	
	public boolean isDirty()
	{
		//return _dirty;
		return _listOfActions.size() > 0 ? true : false;
	}
	
	private void checkDirty()
	{
		if(isDirty())
		{
			// TODO open "you have unsaved changes" dijalog
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
		case PelconnerActivity.ACTIVITY_REQUEST_CODE_GALLERY_REGULAR:
			//Toast.makeText(this, "ACTIVITY_REQUEST_CODE_GALLERY_EXTENDED", Toast.LENGTH_SHORT).show();
			break;
		case PelconnerActivity.ACTIVITY_REQUEST_CODE_CAMERA_REGULAR:
			//Toast.makeText(this, "ACTIVITY_REQUEST_CODE_CAMERA_EXTENDED", Toast.LENGTH_SHORT).show();
			break;
		case PelconnerActivity.ACTIVITY_REQUEST_CODE_COLOR:
			//Toast.makeText(this, "ACTIVITY_REQUEST_CODE_COLOR", Toast.LENGTH_SHORT).show();
			break;
		case RESULT_OK:
			//Toast.makeText(this, "RESULT_OK", Toast.LENGTH_SHORT).show();
			
			Bundle bundleResult = data.getExtras();
	        if(bundleResult != null) // Extras received from Gallery
	        {
	        	/*
	            String stringReceivedImageUri = bundleResult.getString(PelconnerActivity.ACTIVITY_RESPONSE_SELECTED_IMAGE);
	            Log.d(TAG, "Extras: " + stringReceivedImageUri);
	            Uri uriSelectedImage = Uri.parse(stringReceivedImageUri);
	            
	            // TODO: This is only for testing purposes!
	            ImageView imageViewOnlyForTest = (ImageView)findViewById(R.id.imageViewOnlyForTest);
	            imageViewOnlyForTest.setImageURI(uriSelectedImage);
	            */
	            
	            
	            Bundle bundleSelectedImage = bundleResult.getBundle(PelconnerActivity.ACTIVITY_RESPONSE_SELECTED_IMAGE);
	            
	            if(bundleSelectedImage != null)
	            {
	            	String stringReceivedExtras = bundleSelectedImage.getString(ImageInfo.URI);
		            //Log.d(TAG, "Extras: " + stringReceivedExtras); 
		            Uri uriSelectedImage = Uri.parse(stringReceivedExtras);
		            //Log.d(TAG, "Extras2: " + uriSelectedImage.toString()); 
		            
		            initPicturePreview(uriSelectedImage);
		            
		            _selectedAction = Availables.EDIT_IMPORT_GALLERY;		            
		            done(_selectedAction);
		            
		            
		            /*
		    		try
		    		{
		    	        Uri uriImageFile = Uri.parse("file://" + uriSelectedImage.getPath());
		    	        float width, height;
		    	        width = getWindowManager().getDefaultDisplay().getWidth();
		    	        height = getWindowManager().getDefaultDisplay().getHeight();
		    	        
		    			BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
		    			bmpFactoryOptions.inJustDecodeBounds = true;
		    			Bitmap bitmapInitial = BitmapFactory.decodeStream(getContentResolver().openInputStream(uriImageFile), 
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
		    			bitmapInitial = BitmapFactory.decodeStream(getContentResolver().openInputStream(uriImageFile), 
		    					null, 
		    					bmpFactoryOptions);
		    			
	            		_bitmapModified = _pelconnerBitmapModifier.insertBitmap(bitmapInitial);
	            		
	            		
						_imageViewPicturePreview.setImageBitmap(_bitmapBackground);
						_imageViewPicturePreview.invalidate();
						
						//_bitmapModified = _pelconnerBitmapModifier.modify(_selectedAction, _downX, _downY, _upX, _upY, motionEvent);
						Bitmap bitmapHolder = Bitmap.createBitmap(_bitmapBackground);
						bitmapHolder = bitmapHolder.copy(bitmapHolder.getConfig(), true);
						Canvas canvasDisplay = new Canvas(bitmapHolder);
						Paint paintDisplay = new Paint();
						//paintDisplay.setAlpha(0);
						
						paintDisplay.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
						canvasDisplay.drawBitmap(_bitmapModified, 0, 0, paintDisplay);

						_imageViewPicturePreview.setImageBitmap(bitmapHolder);
						_imageViewPicturePreview.invalidate();
		    		}
		    		catch(Exception e)
		    		{
		    			// TODO error handling...
		    		}
		    		*/
	            }
	            else
	            {
	            	byte[] bytesReceivedExtras = bundleResult.getByteArray(PelconnerActivity.ACTIVITY_RESPONSE_SNAPSHOT_BYTES);
	            	Bitmap bitmapReceived = BitmapFactory.decodeByteArray(bytesReceivedExtras, 0, bytesReceivedExtras.length);
	     
	            	if(bitmapReceived != null)
	            	{		            	
	            		initPicturePreview(bitmapReceived);
	            		_selectedAction = Availables.EDIT_IMPORT_CAMERA;
	            		done(_selectedAction);
	            		
	            		/*
	            		initPicturePreview();
	            		
	            		_bitmapModified = _pelconnerBitmapModifier.insertBitmap(bitmapReceived);
	            		
	            		
						_imageViewPicturePreview.setImageBitmap(_bitmapBackground);
						_imageViewPicturePreview.invalidate();
						
						//_bitmapModified = _pelconnerBitmapModifier.modify(_selectedAction, _downX, _downY, _upX, _upY, motionEvent);
						Bitmap bitmapHolder = Bitmap.createBitmap(_bitmapBackground);
						bitmapHolder = bitmapHolder.copy(bitmapHolder.getConfig(), true);
						Canvas canvasDisplay = new Canvas(bitmapHolder);
						Paint paintDisplay = new Paint();
						//paintDisplay.setAlpha(0);
						
						paintDisplay.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
						canvasDisplay.drawBitmap(_bitmapModified, 0, 0, paintDisplay);

						_imageViewPicturePreview.setImageBitmap(bitmapHolder);
						_imageViewPicturePreview.invalidate();
						*/
	            	}
	            }
	        }
			break;
		case RESULT_CANCELED:
		default:
			break;
		}
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() 
	{
		//super.onBackPressed();
		if(_dirty)
		{
			// TODO: Ask to save picture
			//Toast.makeText(this, "Dirty!", Toast.LENGTH_SHORT);
		}
		else
		{
			// TODO: Finish with this activity and get back to Main activity
			//Toast.makeText(this, "Not Dirty.", Toast.LENGTH_SHORT);
			
			finishAndBack();
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		//Log.d("onCreate", "Called!");
		super.onCreate(savedInstanceState);
		
		TAG = "PelconnerPictureActivity";
		
		// Removing title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.picture);

        initiate();

        _listOfActions = new ArrayList<Action>();
        PelconnerActivity.addReference(this);
        
        /*
        new Handler().postDelayed(new Runnable() { 
            public void run() { 
                openOptionsMenu(); 
            } 
        }, 1000);
        */
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onLowMemory()
	 */
	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
		System.gc();
	}

	private void initiate()
	{
        _dirty = false;
        _selectedAction = Availables.NONE;
        
        /**
         * Check if the activity was started from Gallery.
         * If so, there should be some Extras bundled with Intent.
         */
        Bundle bundleExtras = this.getIntent().getExtras();
        if(bundleExtras != null) // Extras received from Gallery
        {
        	Bundle bundleExtrasString = bundleExtras.getBundle(PelconnerActivity.ACTIVITY_RESPONSE_SELECTED_IMAGE);
            
            if(bundleExtrasString != null /*stringReceivedExtras != null*/)
            {
            	/*
            	String stringReceivedExtras = bundleExtrasString.getString(ImageInfo.URI);
	            Log.d(TAG, "Extras: " + stringReceivedExtras); 
	            Uri uriSelectedImage = Uri.parse(stringReceivedExtras);
	            
	            initPicturePreview(uriSelectedImage);
	            */
	            
	            ////////////////////////////////////
	            
            	
	           
	            String stringReceivedExtras = bundleExtrasString.getString(ImageInfo.URI);
	            //Log.d(TAG, "Extras: " + stringReceivedExtras); 
	            
	            /**
	             * If received URI is in a form of "content://media/external/images/media/112",
	             * it should be transformed into
	             */
	            
	            
	            Uri uriSelectedImage = Uri.parse(stringReceivedExtras);  
		        
	            initPicturePreview(uriSelectedImage);
	            done(/*Availables.NONE*/ Availables.MAIN_IMPORT_GALLERY); // Image is just created.
	            
	            /*
	            initPicturePreview();
	            
	    		try
	    		{
	    	        Uri uriImageFile = Uri.parse("file://" + uriSelectedImage.getPath());
	    	        float width, height;
	    	        width = getWindowManager().getDefaultDisplay().getWidth();
	    	        height = getWindowManager().getDefaultDisplay().getHeight();
	    	        
	    			BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
	    			bmpFactoryOptions.inJustDecodeBounds = true;
	    			Bitmap bitmapInitial = BitmapFactory.decodeStream(getContentResolver().openInputStream(uriImageFile), 
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
	    			bitmapInitial = BitmapFactory.decodeStream(getContentResolver().openInputStream(uriImageFile), 
	    					null, 
	    					bmpFactoryOptions);
	    			
            		_bitmapModified = _pelconnerBitmapModifier.insertBitmap(bitmapInitial);
            		
            		
					_imageViewPicturePreview.setImageBitmap(_bitmapBackground);
					_imageViewPicturePreview.invalidate();
					
					//_bitmapModified = _pelconnerBitmapModifier.modify(_selectedAction, _downX, _downY, _upX, _upY, motionEvent);
					Bitmap bitmapHolder = Bitmap.createBitmap(_bitmapBackground);
					bitmapHolder = bitmapHolder.copy(bitmapHolder.getConfig(), true);
					Canvas canvasDisplay = new Canvas(bitmapHolder);
					Paint paintDisplay = new Paint();
					//paintDisplay.setAlpha(0);
					
					paintDisplay.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
					canvasDisplay.drawBitmap(_bitmapModified, 0, 0, paintDisplay);

					_imageViewPicturePreview.setImageBitmap(bitmapHolder);
					_imageViewPicturePreview.invalidate();
	    		}
	    		catch(Exception e)
	    		{
	    			// TODO error handling...
	    		}*/
	            
            }
            else
            {
            	byte[] bytesReceivedExtras = bundleExtras.getByteArray(PelconnerActivity.ACTIVITY_RESPONSE_SNAPSHOT_BYTES);
            	Bitmap bitmapReceived = BitmapFactory.decodeByteArray(bytesReceivedExtras, 0, bytesReceivedExtras.length);
            	
	            // TODO: This is only for testing purposes!
            	if(bitmapReceived != null)
            	{
            		//initPicturePreview(bitmapReceived);
            		/*
            		initPicturePreview();
            		_pelconnerBitmapModifier.insertBitmap(bitmapReceived);
            		*/
            		/////////////////////
            		
            		initPicturePreview(bitmapReceived);
            		done(/*Availables.NONE*/ Availables.MAIN_IMPORT_CAMERA); // Image is just created.

            		/*
            		initPicturePreview();
            		
            		_bitmapModified = _pelconnerBitmapModifier.insertBitmap(bitmapReceived);
            		
            		
					_imageViewPicturePreview.setImageBitmap(_bitmapBackground);
					_imageViewPicturePreview.invalidate();
					
					//_bitmapModified = _pelconnerBitmapModifier.modify(_selectedAction, _downX, _downY, _upX, _upY, motionEvent);
					Bitmap bitmapHolder = Bitmap.createBitmap(_bitmapBackground);
					bitmapHolder = bitmapHolder.copy(bitmapHolder.getConfig(), true);
					Canvas canvasDisplay = new Canvas(bitmapHolder);
					Paint paintDisplay = new Paint();
					//paintDisplay.setAlpha(0);
					
					paintDisplay.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
					canvasDisplay.drawBitmap(_bitmapModified, 0, 0, paintDisplay);

					_imageViewPicturePreview.setImageBitmap(bitmapHolder);
					_imageViewPicturePreview.invalidate();
					*/
					
            	}
            }
        }
        else
        {
        	initPicturePreview();
        }
        

        _relativeLayoutContainer = (RelativeLayout)findViewById(R.id.relativeLayoutContainer);
        
        
        /*_handlerProcessing = new Handler(){

    		@Override
    		public void handleMessage(Message msg) {
    			
    			String messageValue = (String)msg.obj;
    			
    			if(messageValue.equalsIgnoreCase("show"))
    			{
    				showDialog(ID_DIALOG_PROCESSING);
    				//_progressDialog.show();
    			}
    			else if(messageValue.equalsIgnoreCase("dismiss"))
    			{
    				dismissDialog(ID_DIALOG_PROCESSING);
    				//_progressDialog.dismiss();
    			}
    		}
    		
    	};*/
	}
	
	private void removeViews()
	{
		if(_seekBarSaturation != null)
		{
			_relativeLayoutContainer.removeView(_seekBarSaturation);
			_seekBarSaturation = null;
		}	
		if(_seekBarBrightness != null)
		{
			_relativeLayoutContainer.removeView(_seekBarBrightness);
			_seekBarBrightness = null;
		}
		if(_seekBarContrast != null)
		{
			_relativeLayoutContainer.removeView(_seekBarContrast);
			_seekBarContrast = null;
		}
		if(_seekBarTransparency != null)
		{
			_relativeLayoutContainer.removeView(_seekBarTransparency);
			_seekBarTransparency = null;
		}
		if(_seekBarNoise != null)
		{
			_relativeLayoutContainer.removeView(_seekBarNoise);
			_seekBarNoise = null;
		}
		if(_seekBarLineWidth != null)
		{
			_relativeLayoutContainer.removeView(_seekBarLineWidth);
			_seekBarLineWidth = null;			
		}
		if(_seekBarAngle != null)
		{
			_relativeLayoutContainer.removeView(_seekBarAngle);
			_seekBarAngle = null;			
		}
		if(_seekBarZoom != null)
		{
			_relativeLayoutContainer.removeView(_seekBarZoom);
			_seekBarZoom = null;			
		}
		if(_zoomButton != null)
		{
			_relativeLayoutContainer.removeView(_seekBarZoom);
			_seekBarZoom = null;			
		}
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		removeViews();
		
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.picture, menu);
		return true;
	}
	*/	

	/* (non-Javadoc)
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		removeViews();
		
		MenuInflater menuInflater = getMenuInflater();
		
		if(menu.size() > 0)
			menu.clear();
		
		switch(_selectedAction)
		{
		case EDIT_CROP:
		case EDIT_MOVE:
		case EFFECTS_BLACK_AND_WHITE:
		case EFFECTS_BRIGHTNESS:
		case EFFECTS_CONTRAST:
		case EFFECTS_GRAYSCALE:
		case EFFECTS_NEGATIVE:
		case EFFECTS_NOISE:
		case EFFECTS_SATURATION:
		case EFFECTS_TRANSPARENCY:
			menuInflater.inflate(R.menu.picture_crop_move, menu);
			break;
		case TOOLS_RECTANGLE:
		case TOOLS_ELLIPSE:
			if(_pelconnerBitmapModifier != null && _pelconnerBitmapModifier.isFillEnabled())
			{
				menuInflater.inflate(R.menu.picture_shape_outlined, menu);
			}
			else 
			{
				menuInflater.inflate(R.menu.picture_shape_filled, menu);
			}
			break;
		case EDIT_ROTATE:
		case EDIT_ANGLE:
			menuInflater.inflate(R.menu.picture_rotate, menu);
			break;
		case EDIT_ZOOM:
		case EDIT_ZOOM_MANUAL:
		case EDIT_PINCH_ZOOM:
			menuInflater.inflate(R.menu.picture_zoom, menu);
			break;
		case TOOLS_TEXT:
			menuInflater.inflate(R.menu.picture_text, menu);
			break;
		case TOOLS_FILL:
			menuInflater.inflate(R.menu.picture_text_fill, menu);
			break;
		case TOOLS_DRAW:
		case TOOLS_LINE:
		case TOOLS_LINE_WIDTH:
		case TOOLS_SHAKE_DRAW:
			menuInflater.inflate(R.menu.picture_line, menu);
			break;
		case NONE:
		case MAIN_IMPORT_CAMERA:
		case MAIN_IMPORT_GALLERY:
		case EDIT_IMPORT_CAMERA:
		case EDIT_IMPORT_GALLERY:
			if(!_dirtyImport)
			{
				menuInflater.inflate(R.menu.picture, menu);
			}
			else
			{
				menuInflater.inflate(R.menu.picture_crop_move, menu);
			}
			break;
		default:
			return false;
		}
		
		changeMenuResource();
		
		return true;
	}	
	
	/* (non-Javadoc)
	 * @see com.android.pelconner.PelconnerActivity#onDismiss(android.content.DialogInterface)
	 */
	@Override
	public void onDismiss(DialogInterface dialog) {
		
		String tag = ((PelconnerDialog)dialog).getTag();
		
		if(tag.equalsIgnoreCase(PelconnerDialog.DIALOG_TAG_OPTION))
		{
			PelconnerOption selectedOption = ((PelconnerOptionDialog)dialog).getSelectedOption();
			actionSelected(selectedOption.getOption());
		}
		else if(tag.equalsIgnoreCase(PelconnerDialog.DIALOG_TAG_SAVE))
		{
			PelconnerSaveDialog dialogSave = (PelconnerSaveDialog)dialog;
			ImageData imageData = dialogSave.getImageData();
			//PelconnerSnapshotDialog.Option selectedOption = dialogSave.getSelectedOption(); 
			
			if(imageData.getHeight() > 0 && imageData.getWidth() > 0)
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
				ByteArrayOutputStream bos = new ByteArrayOutputStream();   
				//imageData.setFormat(Format.PNG);
				if(imageData.getFormat() == ImageData.Format.JPG)
				{
					Bitmap bitmapWhiteBackground = _bitmapFinal.copy(_bitmapFinal.getConfig(), true);
					Canvas canvasWhite = new Canvas(bitmapWhiteBackground);
					canvasWhite.drawColor(Color.WHITE);
					Paint paintWhiteBackground = new Paint();
					paintWhiteBackground.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
					canvasWhite.drawBitmap(_bitmapFinal, new Matrix(), paintWhiteBackground);
					
					bitmapWhiteBackground.compress(Bitmap.CompressFormat.JPEG, 100, bos);
					
					bitmapWhiteBackground.recycle();
				}
				else
				{
					_bitmapFinal.compress(Bitmap.CompressFormat.PNG, 100, bos);	
				}
				saveIt(imageData, bos.toByteArray());
			}			
		}
		else if(tag.equalsIgnoreCase(PelconnerDialog.DIALOG_TAG_TEXT))
		{
			PelconnerTextDialog dialogText = (PelconnerTextDialog)dialog;
			
			
			if(true/*!dialogText.getText().equalsIgnoreCase("")*/)
			{
				// TODO: read the selected values and perform the action...
				changeBitmapModified(_pelconnerBitmapModifier.addText(dialogText.getText(), 
						dialogText.getSelectedTypeface(), dialogText.getTextSize(), dialogText.getTextStyle()));
				updateDisplay(false);
				
				_currentAction = new Action();
				_currentAction.setOption(_selectedAction);
				_currentAction.addParam(dialogText.getText());
				_currentAction.addParam(dialogText.getSelectedTypeface());
				_currentAction.addParam((Integer)dialogText.getTextSize());
				_currentAction.addParam(dialogText.getTextStyle());
				
				addAction();
			}
		}
		else if(tag.equalsIgnoreCase(PelconnerDialog.DIALOG_TAG_COLOR))
		{
			PelconnerColorDialog dialogColor = (PelconnerColorDialog)dialog;
			_pelconnerBitmapModifier.setColor(dialogColor.getColor());
		}
		else
		{
			super.onDismiss(dialog);
		}
	}

	private void addAction() {
		// TODO Add check for the amount of added actions.
		// If some limit is reached, the image should be flatten and the action list narrowed to 1.
		if(_currentAction != null)
			_listOfActions.add(_currentAction);
		
		//Log.d("ACTION", "Added action: " + _currentAction.getOption().toString());
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		PelconnerOptionDialog pelconnerOptionDialog = null;
		switch(itemId)
		{
		/*
		case R.id.menu_item_help:
			startPelconnerActivity(PelconnerActivity.Type.HELP_PICTURE);
			break;
		*/
		/*
		case R.id.menu_subitem_import_camera:
			Toast.makeText(this, "Import sub-menu item clicked!", Toast.LENGTH_SHORT).show();
			// TODO: Open camera to obtain image
			startPelconnerActivity(Type.CAMERA);
			return true;
		case R.id.menu_subitem_import_gallery:
			// TODO: Open gallery to obtain image
			startPelconnerActivity(Type.GALLERY);
			return true;
		case R.id.menu_subitem_resize:
			// TODO: Open dialog to enter new canvas dimensions
			PelconnerOptionDialog pelconnerOptionDialog = new PelconnerOptionDialog(this);
			pelconnerOptionDialog.setOnDismissListener(this);
			pelconnerOptionDialog.add(PelconnerOption.Availables.EDIT_CROP);
			pelconnerOptionDialog.add(PelconnerOption.Availables.EDIT_IMPORT_GALLERY);
			pelconnerOptionDialog.add(PelconnerOption.Availables.EDIT_PINCH_ZOOM);
			pelconnerOptionDialog.add(PelconnerOption.Availables.TOOLS_CIRCLE_SELECT);
			pelconnerOptionDialog.add(PelconnerOption.Availables.TOOLS_ELLIPSE);
			pelconnerOptionDialog.add(PelconnerOption.Availables.TOOLS_FILL);
			pelconnerOptionDialog.add(PelconnerOption.Availables.EDIT_IMPORT_CAMERA);
			pelconnerOptionDialog.show();
			return true;
		case R.id.menu_subitem_rotate:
			// TODO: Open dialog to enter a degree for canvas rotation
			return true;
			*/
		case R.id.menu_item_edit:
			pelconnerOptionDialog = new PelconnerOptionDialog(this);
			pelconnerOptionDialog.setOnDismissListener(this);
			pelconnerOptionDialog.setTitle(R.string.edit);
			pelconnerOptionDialog.add(Availables.EDIT_NEW);
			pelconnerOptionDialog.add(Availables.EDIT_IMPORT_CAMERA);
			pelconnerOptionDialog.add(Availables.EDIT_IMPORT_GALLERY);
			pelconnerOptionDialog.add(Availables.EDIT_MOVE);
			pelconnerOptionDialog.add(Availables.EDIT_ROTATE);
			//pelconnerOptionDialog.add(Availables.EDIT_RESIZE);
			//pelconnerOptionDialog.add(Availables.EDIT_CROP);
			//pelconnerOptionDialog.add(Availables.EDIT_STRETCH);
			pelconnerOptionDialog.add(Availables.EDIT_ZOOM);
			pelconnerOptionDialog.add(Availables.EDIT_PINCH_ZOOM);
			pelconnerOptionDialog.show();
			return true;	
		case R.id.menu_item_tools:
			pelconnerOptionDialog = new PelconnerOptionDialog(this);
			pelconnerOptionDialog.setOnDismissListener(this);
			pelconnerOptionDialog.setTitle(R.string.tools);
			pelconnerOptionDialog.add(Availables.TOOLS_DRAW);
			pelconnerOptionDialog.add(Availables.TOOLS_LINE);
			pelconnerOptionDialog.add(Availables.TOOLS_TEXT);
			if(hasAccelerometer())// Acceloremeter detection
			{
				pelconnerOptionDialog.add(Availables.TOOLS_SHAKE_DRAW);
			}
			pelconnerOptionDialog.add(Availables.TOOLS_RECTANGLE);
			pelconnerOptionDialog.add(Availables.TOOLS_ELLIPSE);
			pelconnerOptionDialog.add(Availables.TOOLS_FILL);
			//pelconnerOptionDialog.add(Availables.TOOLS_RECTANGLE_SELECT);
			//pelconnerOptionDialog.add(Availables.TOOLS_CIRCLE_SELECT);
			pelconnerOptionDialog.show();
			return true;
		case R.id.menu_item_effects:
			pelconnerOptionDialog = new PelconnerOptionDialog(this);
			pelconnerOptionDialog.setOnDismissListener(this);
			pelconnerOptionDialog.setTitle(R.string.effects);
			//pelconnerOptionDialog.add(Availables.EFFECTS_BLACK_AND_WHITE);
			pelconnerOptionDialog.add(Availables.EFFECTS_GRAYSCALE);
			//pelconnerOptionDialog.add(Availables.EFFECTS_NOISE);
			pelconnerOptionDialog.add(Availables.EFFECTS_SATURATION);
			pelconnerOptionDialog.add(Availables.EFFECTS_TRANSPARENCY);
			pelconnerOptionDialog.add(Availables.EFFECTS_CONTRAST);
			pelconnerOptionDialog.add(Availables.EFFECTS_BRIGHTNESS);
			pelconnerOptionDialog.add(Availables.EFFECTS_NEGATIVE);
			pelconnerOptionDialog.show();
			return true;
		case R.id.menu_item_more:
			pelconnerOptionDialog = new PelconnerOptionDialog(this);
			pelconnerOptionDialog.setOnDismissListener(this);
			pelconnerOptionDialog.setTitle(R.string.more);
			pelconnerOptionDialog.add(Availables.MORE_ABOUT);
			pelconnerOptionDialog.add(Availables.MORE_HELP);
			pelconnerOptionDialog.add(Availables.MORE_QUIT);
			pelconnerOptionDialog.show();
			return true;
		case R.id.menu_item_save:
			//_bitmapFinal = _bitmapModified.copy(_bitmapModified.getConfig(), false);
			
			if(_bitmapFinal.isRecycled())
			{
				_bitmapFinal = Bitmap.createBitmap(_bitmapModified.getWidth(), _bitmapModified.getHeight(), _bitmapModified.getConfig());
			}
			
			Canvas canvas = new Canvas(_bitmapFinal);
			Matrix matrix = new Matrix();
			Paint paint = new Paint();
			canvas.drawBitmap(_bitmapModified, matrix, paint);
			
			PelconnerSaveDialog saveDialog = new PelconnerSaveDialog(this, _bitmapFinal);
			saveDialog.setOnDismissListener(this);
			saveDialog.show();
			return true;
		case R.id.menu_item_share:
			
			_bitmapFinal = _bitmapModified.copy(_bitmapModified.getConfig(), true);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();   
			_bitmapFinal.compress(CompressFormat.PNG, 100, bos);			
			byte[] imageBytes = bos.toByteArray();
			
			shareIt(imageBytes);
			return true;
			
			/*
			Bitmap bitmapFinalWithWhiteBackground = _bitmapModified.copy(_bitmapModified.getConfig(), false);
			Canvas canvasFinalWhite = new Canvas(bitmapFinalWithWhiteBackground);
			canvasFinalWhite.drawColor(Color.WHITE);
			
			Paint paintWhitePaint = new Paint();
			paintWhitePaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
			canvasFinalWhite.drawBitmap(bitmapFinalWithWhiteBackground, new Matrix(), paintWhitePaint);
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();   
			bitmapFinalWithWhiteBackground.compress(CompressFormat.PNG, 90, bos);			
			byte[] imageBytes = bos.toByteArray();
			
			shareIt(imageBytes);
			return true;
			*/
		case R.id.menu_picture_item_cancel:
			if(_pelconnerBitmapModifier != null)
				_pelconnerBitmapModifier.clear();
			_pelconnerBitmapModifier = new PelconnerBitmapModifier(this, this.getWindowManager().getDefaultDisplay(), _bitmapFinal);

			changeBitmapModified(_pelconnerBitmapModifier.getBitmap());
			
			//_imageViewPicturePreview.setImageBitmap(_bitmapFinal);
			updateDisplay(true);
			//_imageViewPicturePreview.setOnTouchListener(this);
			
			
			break;
		case R.id.menu_picture_item_done:
			_dirtyImport = false;
			_pinchCentralPoint = null;
			done(Availables.NONE);
			break;
		case R.id.menu_picture_item_color:
			PelconnerColorDialog dialogColor = new PelconnerColorDialog(this, _pelconnerBitmapModifier.getColor());
			dialogColor.setOnDismissListener(this);
			dialogColor.show();
			break;
		case R.id.menu_picture_item_text:
			PelconnerTextDialog dialogText = new PelconnerTextDialog(this);
			dialogText.setOnDismissListener(this);
			dialogText.show();
			break;	
		case R.id.menu_picture_item_filled:
			_pelconnerBitmapModifier.setFillEnabled(true);
			//Toast.makeText(this, "Fill style selected.", Toast.LENGTH_SHORT);
			break;
		case R.id.menu_picture_item_outlined:
			_pelconnerBitmapModifier.setFillEnabled(false);
			//Toast.makeText(this, "Outline style selected.", Toast.LENGTH_SHORT);
			break;
		case R.id.menu_picture_item_angle:
			_seekBarAngle = new SeekBar(this);
			_seekBarAngle.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			_seekBarAngle.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar));
			_seekBarAngle.setIndeterminateDrawable(getResources().getDrawable(R.drawable.seekbar));
			_seekBarAngle.setPadding(8, 5, 8, 0);
			_seekBarAngle.setProgress(0); // Angle is by default 0 --> when changed, the image should be rotated by that much
			_seekBarAngle.setMax(360); // Full circle
			_seekBarAngle.setOnSeekBarChangeListener(this);
			_relativeLayoutContainer.addView(_seekBarAngle);		
			break;
		case R.id.menu_picture_item_width:
			_seekBarLineWidth = new SeekBar(this);
			_seekBarLineWidth.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			_seekBarLineWidth.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar));
			_seekBarLineWidth.setIndeterminateDrawable(getResources().getDrawable(R.drawable.seekbar));
			_seekBarLineWidth.setPadding(8, 5, 8, 0);
			_seekBarLineWidth.setMax(PelconnerBitmapModifier.MAX_LINE_WIDTH);
			_seekBarLineWidth.setProgress(_pelconnerBitmapModifier.getLineWidth());
			_seekBarLineWidth.setOnSeekBarChangeListener(this);
			_relativeLayoutContainer.addView(_seekBarLineWidth);
			break;
		case R.id.menu_picture_item_zoom:
			// TODO Zoom seekbar is displayed with set of zoom values - default position should be the center of the seekbar.
			_seekBarZoom = new SeekBar(this);
			_seekBarZoom.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			_seekBarZoom.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar));
			_seekBarZoom.setIndeterminateDrawable(getResources().getDrawable(R.drawable.seekbar));
			_seekBarZoom.setPadding(8, 5, 8, 0);
			// Left half is for negative zoom, and the right half is for positive zoom 
			_seekBarZoom.setMax(200);
			_seekBarZoom.setProgress(100); // In the middle of the seek bar - no zoom.
			_seekBarZoom.setOnSeekBarChangeListener(this);
			_relativeLayoutContainer.addView(_seekBarZoom);		
			break;
		default:
			return super.onOptionsItemSelected(item);	
		}		
		return true;
	}

	private void done(Availables afterAction) {
		// TODO Auto-generated method stub
		// TODO _bitmapModified is merged into the _bitmapFinal
		// Create empty image
		
		// TODO Check if we really need to call createBitmap, having in mind that drawColor(Color.transparent) is called afterwards!
		/*
		try
		{
			_bitmapFinal = Bitmap.createBitmap(_bitmapFinal.getWidth(), _bitmapFinal.getHeight(), _bitmapFinal.getConfig());
		}
		catch(OutOfMemoryError ooem)
		{
			//_bitmapFinal = Bitmap.createBitmap(_bitmapFinal.getWidth(), _bitmapFinal.getHeight(), _bitmapFinal.getConfig());
			//Toast.makeText(this, "Processing...", Toast.LENGTH_SHORT);
		}
		*/
		
		if(_bitmapFinal.isRecycled())
		{
			_bitmapFinal = Bitmap.createBitmap(_bitmapModified.getWidth(), _bitmapModified.getHeight(), _bitmapModified.getConfig());
			//_bitmapFinal = Bitmap.createScaledBitmap(_bitmapModified, getWindowManager().getDefaultDisplay().getWidth(), 
			//		getWindowManager().getDefaultDisplay().getHeight(), false);
		}
		_bitmapFinal.eraseColor(Color.TRANSPARENT);
		
		Canvas canvas2 = new Canvas(_bitmapFinal);
		Matrix matrix2 = new Matrix();
		Paint paint2 = new Paint();
		paint2.setAlpha(255);
		//paint2.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_OVER));
		//canvas2.drawColor(Color.TRANSPARENT);
		
		
		// Dodao sam ovo:
		//_bitmapModified = _pelconnerBitmapModifier.getBitmap();
		
		canvas2.drawBitmap(_bitmapModified, matrix2, paint2);
		//_imageViewPicturePreview.setImageBitmap(_bitmapFinal);
		updateDisplay(true);
		
		if(_pelconnerBitmapModifier != null)
			_pelconnerBitmapModifier.clear();
		_pelconnerBitmapModifier = new PelconnerBitmapModifier(this, this.getWindowManager().getDefaultDisplay(), _bitmapFinal);
		
		changeBitmapModified(_pelconnerBitmapModifier.getBitmap());
		
		if(_selectedAction == Availables.TOOLS_SHAKE_DRAW)
		{
			_sensorManager.unregisterListener(this, _accelerometer);
		}
		
		_selectedAction = afterAction;//Availables.NONE;
		
		
		
		//_imageViewPicturePreview.setOnTouchListener(this);
		
		//openOptionsMenu();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() 
	{
		super.onPause();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() 
	{
		super.onResume();
	}

	private float getDistanceBetweenTouches(MotionEvent motionEvent)
	{
		float distance, dx, dy;
		dx = motionEvent.getX(0) - motionEvent.getX(1);
		dy = motionEvent.getY(0) - motionEvent.getY(1);
		distance = (float) Math.sqrt(((float)Math.pow(dx, 2) + (float)Math.pow(dy, 2)));
		return distance;
	}
	
	private PointF getMiddlePointBetweenTouches(MotionEvent motionEvent)
	{
		float x = motionEvent.getX(0) + motionEvent.getX(1);
		float y = motionEvent.getX(0) + motionEvent.getY(1);
		return new PointF(x / 2, y / 2);
	}
	
	@Override
	public boolean onTouch(View arg0, MotionEvent motionEvent) 
	{
		//Log.d("MOTION", "Event: " + motionEvent.getAction());
		
		//_paint = new Paint();
		//paint.setColor(Color.TRANSPARENT);
		//_paint.setStrokeCap(Cap.ROUND);
		//_paint.setStrokeWidth(20);
		//_paint.setAlpha(0);
		//_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		//_paint.setAntiAlias(true);
		
		if(_selectedAction == Availables.NONE)
			return false;
		
		int action = motionEvent.getAction();
		switch(action)
		{
		case MotionEvent.ACTION_DOWN:
			removeViews(); // Removing seek bars
			_downX = motionEvent.getX();
			_downY = motionEvent.getY();
			_startX = _downX;
			_startY = _downY;
			
			if(_selectedAction == Availables.TOOLS_FILL)
			{
				Canvas canvas = new Canvas(_bitmapModified);
				canvas.drawColor(_pelconnerBitmapModifier.getColor());
			}
			
			break;
		case MotionEvent.ACTION_POINTER_2_DOWN:
			_startDistance = getDistanceBetweenTouches(motionEvent);
			if(_startDistance > 10F)
			{
				//_pinchCentralPoint = getMiddlePointBetweenTouches(motionEvent);
				_pinchCentralPoint = new PointF(getWindowManager().getDefaultDisplay().getWidth() / 2, getWindowManager().getDefaultDisplay().getHeight() / 2);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			_upX = motionEvent.getX();
			_upY = motionEvent.getY();
			//_canvas.drawLine(_downX, _downY, _upX, _upY, _paint);
			//if(_selectedAction == Availables.EDIT_MOVE || _selectedAction == Availables.TOOLS_DRAW)
			//{
			
			// If action is Pencil Draw, pass every sub-line
			
				
				
				if(_selectedAction == Availables.TOOLS_DRAW)
				{
					//changeBitmapModified(_pelconnerBitmapModifier.modify(_selectedAction, _downX, _downY, _upX, _upY, motionEvent));
					_bitmapModified = _pelconnerBitmapModifier.modify(_selectedAction, _downX, _downY, _upX, _upY, motionEvent);
					
					updateDisplay(false);
					
					// We are collecting every sub-line
					_currentAction = new Action();
					_currentAction.setOption(_selectedAction);
					
					// Adding start/end points
					_currentAction.addParam((Float)_downX);
					_currentAction.addParam((Float)_downY);
					_currentAction.addParam((Float)_upX);
					_currentAction.addParam((Float)_upY);
					
					// The final action is added to the list at the end, when ACTION_UP event is received
					// At this moment, we are only adding params to the list.
					//addAction();
				}
				else if(_selectedAction == Availables.TOOLS_TEXT)
				{
					/*
					_imageViewPicturePreview.setImageBitmap(_bitmapBackground);
					_imageViewPicturePreview.invalidate();
					*/
					
					String text = (String)_currentAction.getParam(0);
					Typeface typeface = (Typeface)_currentAction.getParam(1);
					float textSize = (Integer)_currentAction.getParam(2);
					PelconnerTextDialog.Style textStyle = (PelconnerTextDialog.Style)_currentAction.getParam(3);
					
					//changeBitmapModified(_pelconnerBitmapModifier.addText(text, typeface, textSize, textStyle, _upX, _upY));
					_bitmapModified = _pelconnerBitmapModifier.addText(text, typeface, textSize, textStyle, _upX, _upY);
					
					/*
					Bitmap bitmapHolder = Bitmap.createBitmap(_bitmapBackground);
					bitmapHolder = bitmapHolder.copy(bitmapHolder.getConfig(), true);
					Canvas canvasDisplay = new Canvas(bitmapHolder);
					Paint paintDisplay = new Paint();
					paintDisplay.setAlpha(255);
					paintDisplay.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
					canvasDisplay.drawBitmap(_bitmapModified, 0, 0, paintDisplay);
					*/
					
					_imageViewPicturePreview.setImageBitmap(/*bitmapHolder*/_bitmapModified);
					_imageViewPicturePreview.invalidate();					
				}
				else if(_selectedAction == Availables.EDIT_IMPORT_CAMERA || _selectedAction == Availables.EDIT_IMPORT_GALLERY ||
						_selectedAction == Availables.MAIN_IMPORT_CAMERA || _selectedAction == Availables.MAIN_IMPORT_GALLERY)
				{
					_dirtyImport = true;
					
					/*
					_imageViewPicturePreview.setImageBitmap(_bitmapBackground);
					_imageViewPicturePreview.invalidate();
					*/
					
					//changeBitmapModified(_pelconnerBitmapModifier.modify(_selectedAction, _downX, _downY, _upX, _upY, motionEvent));
					_bitmapModified = _pelconnerBitmapModifier.modify(_selectedAction, _downX, _downY, _upX, _upY, motionEvent);
					
					/*Bitmap bitmapHolder = Bitmap.createBitmap(_bitmapBackground);
					bitmapHolder = bitmapHolder.copy(bitmapHolder.getConfig(), true);*/
					/*
					Bitmap bitmapHolder = _bitmapBackground.copy(_bitmapBackground.getConfig(), true);
					Canvas canvasDisplay = new Canvas(bitmapHolder);
					Paint paintDisplay = new Paint();
					paintDisplay.setAlpha(255);
					

					paintDisplay.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
					canvasDisplay.drawBitmap(_bitmapModified, 0, 0, paintDisplay);
					*/

					_imageViewPicturePreview.setImageBitmap(/*bitmapHolder*/_bitmapModified);
					_imageViewPicturePreview.invalidate();
				}
				else if(_selectedAction == Availables.EDIT_PINCH_ZOOM)
				{
					/**
					 * Calculate scale
					 */
					if(_pinchCentralPoint != null)
					{
						float currentDistance = getDistanceBetweenTouches(motionEvent);
						float scale = 1;
						if(currentDistance > 10F)
						{
							/*
							_imageViewPicturePreview.setImageBitmap(_bitmapBackground);
							_imageViewPicturePreview.invalidate();
							*/
							
							scale = currentDistance / _startDistance;
							
							//changeBitmapModified(_pelconnerBitmapModifier.modify(_selectedAction, scale, -1000, _pinchCentralPoint.x, _pinchCentralPoint.y, motionEvent));
							_bitmapModified = _pelconnerBitmapModifier.modify(_selectedAction, scale, -1000, _pinchCentralPoint.x, _pinchCentralPoint.y, motionEvent);
							
							/*
							Bitmap bitmapHolder = Bitmap.createBitmap(_bitmapBackground);
							bitmapHolder = bitmapHolder.copy(bitmapHolder.getConfig(), true);
							Canvas canvasDisplay = new Canvas(bitmapHolder);
							Paint paintDisplay = new Paint();
							paintDisplay.setAlpha(255);
							
	
							paintDisplay.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
							canvasDisplay.drawBitmap(_bitmapModified, 0, 0, paintDisplay);
							*/
	
							_imageViewPicturePreview.setImageBitmap(/*bitmapHolder*/_bitmapModified);
							_imageViewPicturePreview.invalidate();					
						}
					}
				}
				else if(_selectedAction == Availables.EDIT_ROTATE || _selectedAction == Availables.EDIT_MOVE)
				{
					/*
					_imageViewPicturePreview.setImageBitmap(_bitmapBackground);
					_imageViewPicturePreview.invalidate();
					*/
					
					//changeBitmapModified(_pelconnerBitmapModifier.modify(_selectedAction, _downX, _downY, _upX, _upY, motionEvent));
					_bitmapModified = _pelconnerBitmapModifier.modify(_selectedAction, _downX, _downY, _upX, _upY, motionEvent);
					
					/*
					Bitmap bitmapHolder = Bitmap.createBitmap(_bitmapBackground);
					bitmapHolder = bitmapHolder.copy(bitmapHolder.getConfig(), true);
					Canvas canvasDisplay = new Canvas(bitmapHolder);
					Paint paintDisplay = new Paint();
					paintDisplay.setAlpha(255);
					

					paintDisplay.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
					canvasDisplay.drawBitmap(_bitmapModified, 0, 0, paintDisplay);
					*/

					_imageViewPicturePreview.setImageBitmap(/*bitmapHolder*/_bitmapModified);
					_imageViewPicturePreview.invalidate();
					
					/*
					_pelconnerBitmapModifier = new PelconnerBitmapModifier(this, this.getWindowManager().getDefaultDisplay(), _bitmapFinal);
					Bitmap tempBitmap = _bitmapBackground.copy(_bitmapBackground.getConfig(), true);
					Bitmap tempBitmapModified = _pelconnerBitmapModifier.modify(_selectedAction, _startX, _startY, _upX, _upY, motionEvent);
					
					
					Canvas canvasDisplay = new Canvas(tempBitmap);
					Paint paintDisplay = new Paint();
					//paintDisplay.setAlpha(0);
					
					paintDisplay.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
					canvasDisplay.drawColor(Color.RED);
					//canvasDisplay.drawBitmap(tempBitmapModified, 0, 0, paintDisplay);
						
					_imageViewPicturePreview.setImageBitmap(tempBitmap);
					_imageViewPicturePreview.invalidate();
					*/
					
					/*
					_imageViewPicturePreview.setImageBitmap(_bitmapBackground);
					_imageViewPicturePreview.invalidate();
					
					Bitmap bitmapTempDisplay = _bitmapBackground.copy(_bitmapBackground.getConfig(), true);
					
					Canvas canvasDisplay = new Canvas(bitmapTempDisplay);
					Paint paintDisplay = new Paint();
					Bitmap bitmapTemp = _pelconnerBitmapModifier.modify(_selectedAction, _downX, _downY, _upX, _upY, motionEvent);
					canvasDisplay.drawColor(Color.TRANSPARENT);
					canvasDisplay.drawBitmap(_bitmapBackground, 0, 0, paintDisplay);
					paintDisplay.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
					canvasDisplay.drawBitmap(bitmapTemp, 0, 0, paintDisplay);
					
					_imageViewPicturePreview.setImageBitmap(bitmapTempDisplay);
					_imageViewPicturePreview.invalidate();*/						
				}
				else
				{
					
					//Bitmap bitmapTempDisplay;
					//bitmapTempDisplay = null;
					/*
					try
					{
						bitmapTempDisplay = _bitmapDisplayed.copy(_bitmapDisplayed.getConfig(), true);
					}
					catch(OutOfMemoryError oome)
					{
						bitmapTempDisplay = _bitmapDisplayed.copy(Config.ALPHA_8, true);
					}
					*/
					
					// new logic
					//bitmapTempDisplay = Bitmap.createScaledBitmap(_bitmapDisplayed, getWindowManager().getDefaultDisplay().getWidth(), getWindowManager().getDefaultDisplay().getHeight(), false);
					
					if(_bitmapTemp != null)
					{
						_bitmapTemp.recycle();
						_bitmapTemp = null;
					}
					_bitmapTemp = Bitmap.createBitmap(getWindowManager().getDefaultDisplay().getWidth(), getWindowManager().getDefaultDisplay().getHeight(), _bitmapDisplayed.getConfig());//_bitmapDisplayed.copy(_bitmapDisplayed.getConfig(), true);
					/*bitmapTempDisplay*///_bitmapTemp = Bitmap.createScaledBitmap(_bitmapDisplayed, _bitmapDisplayed.getWidth(), _bitmapDisplayed.getHeight(), false);//_bitmapDisplayed.copy(_bitmapDisplayed.getConfig(), true);
					
					//_bitmapDisplayed.recycle(); 
					
					Canvas canvasDisplay = new Canvas(/*bitmapTempDisplay*/_bitmapTemp);
					Paint paintDisplay = new Paint();
					paintDisplay.setAlpha(255);
					paintDisplay.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER/*DST_OVER*/));
					canvasDisplay.drawBitmap(_pelconnerBitmapModifier.modify(_selectedAction, _downX, _downY, _upX, _upY, motionEvent), 
							0, 0, paintDisplay);
					_imageViewPicturePreview.setImageBitmap(/*bitmapTempDisplay*/_bitmapTemp);
					_imageViewPicturePreview.invalidate();	
					
					/*
					_bitmapTemp.recycle();
					_bitmapTemp = null;
					*/
				}
				
				_downX = _upX;
				_downY = _upY;
			//}
			break;
		case MotionEvent.ACTION_UP:
			_upX = motionEvent.getX();
			_upY = motionEvent.getY();
			
			if(_selectedAction == Availables.EDIT_MOVE || _selectedAction == Availables.EDIT_ROTATE)
			{
				/*
				_imageViewPicturePreview.setImageBitmap(_bitmapBackground);
				_imageViewPicturePreview.invalidate();
				*/

				changeBitmapModified(_pelconnerBitmapModifier.modify(_selectedAction, _startX, _startY, _upX, _upY, motionEvent));
				
				/*
				Bitmap bitmapHolder = _bitmapBackground.copy(_bitmapBackground.getConfig(), true);
				bitmapHolder = bitmapHolder.copy(bitmapHolder.getConfig(), true);
				Canvas canvasDisplay = new Canvas(bitmapHolder);
				Paint paintDisplay = new Paint();
				paintDisplay.setAlpha(255);
				

				paintDisplay.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
				canvasDisplay.drawBitmap(_bitmapModified, 0, 0, paintDisplay);

				bitmapHolder = bitmapHolder.copy(bitmapHolder.getConfig(), true);
				*/
				
				_imageViewPicturePreview.setImageBitmap(/*bitmapHolder*/_bitmapModified);
				_imageViewPicturePreview.invalidate();
			}
			else if(_selectedAction == Availables.EDIT_PINCH_ZOOM)
			{
				_pinchCentralPoint = null;
			}
			else if(_selectedAction == Availables.TOOLS_TEXT)
			{
				// Do nothing
			}
			else if(_selectedAction == Availables.EDIT_IMPORT_CAMERA || _selectedAction == Availables.EDIT_IMPORT_GALLERY ||
					_selectedAction == Availables.MAIN_IMPORT_CAMERA || _selectedAction == Availables.MAIN_IMPORT_GALLERY)
			{
				done(/*_selectedAction*/ Availables.EDIT_MOVE);
			}
			else
			{
				_bitmapModified = _pelconnerBitmapModifier.modify(_selectedAction, _downX, _downY, _upX, _upY, motionEvent);
				/*Canvas canvasDisplay2 = new Canvas(_bitmapDisplayed);
				Paint paintDisplay2 = new Paint();
				//paintDisplay.setAlpha(0);
				paintDisplay2.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_OVER));
				canvasDisplay2.drawBitmap(_bitmapModified, 0, 0, paintDisplay2);
				_imageViewPicturePreview.setImageBitmap(_bitmapDisplayed);
				_imageViewPicturePreview.invalidate();*/
				updateDisplay(false);
			}

			
			
			// Line, Rectangle, Circle

			if(_selectedAction == Availables.TOOLS_LINE || _selectedAction == Availables.TOOLS_RECTANGLE || 
					_selectedAction == Availables.TOOLS_ELLIPSE || _selectedAction == Availables.EDIT_MOVE || 
					_selectedAction == Availables.EDIT_CROP)
			{
				_currentAction = new Action();
				_currentAction.setOption(_selectedAction);
				// Passing the starting and ending point of the selected action
				_currentAction.addParam((Float)_startX);
				_currentAction.addParam((Float)_startY);
				_currentAction.addParam((Float)_upX);
				_currentAction.addParam((Float)_upY);
				
				addAction();
			}
			else if(_selectedAction == Availables.TOOLS_DRAW)
			{
				// Current action has already been initialized for TOOLS_DRAW event in the ACTION_MOVE section
				// Thus, we are only adding ending path to the list of params
				
				// Last known action needs to be Draw
				if(_currentAction.getOption() == Availables.TOOLS_DRAW)
				{
					_currentAction.addParam((Float)_downX);
					_currentAction.addParam((Float)_downY);
					_currentAction.addParam((Float)_upX);
					_currentAction.addParam((Float)_upY);
					
					addAction();
				}
			}
			else if(_selectedAction == Availables.TOOLS_FILL)
			{
				_currentAction = new Action();
				_currentAction.setOption(_selectedAction);			
				// For Fill, we don't need any other params.
				// That's for now, without segmentation implemented
				addAction();
			}
			
			break;
		case MotionEvent.ACTION_CANCEL:
			break;
		default:
			break;
				
		}
		return true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		
        new Handler().postDelayed(new Runnable() { 
            public void run() { 
            	try
            	{
            		openOptionsMenu();
            	}
            	catch(BadTokenException e)
            	{
            		try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
            		openOptionsMenu();
            	}
            } 
        }, 1500);
	}

	/**
	 * 
	 */
	private void updateDisplay(boolean finalBitmap) {
		//_bitmapDisplayed = _bitmapBackground.copy(_bitmapFinal.getConfig(), true);
		_bitmapDisplayed.eraseColor(Color.TRANSPARENT);
		Canvas canvasDisplay = new Canvas(_bitmapDisplayed);
		Paint paintDisplay = new Paint();
		paintDisplay.setAlpha(255);
		
		//canvasDisplay.drawColor(Color.TRANSPARENT);
		if(finalBitmap)
		{
			//canvasDisplay.drawColor(Color.TRANSPARENT);
			//canvasDisplay.drawBitmap(_bitmapBackground, 0, 0, paintDisplay);
			//paintDisplay.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
			if(_bitmapFinal != null && _bitmapFinal.isRecycled())
				_bitmapFinal = Bitmap.createBitmap(getWindowManager().getDefaultDisplay().getWidth(), getWindowManager().getDefaultDisplay().getHeight(), Config.ARGB_8888);
			canvasDisplay.drawBitmap(_bitmapFinal, 0, 0, paintDisplay);
		}
		else
		{
			//canvasDisplay.drawBitmap(_bitmapBackground, 0, 0, paintDisplay);
			//paintDisplay.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
			if(_bitmapModified != null && _bitmapModified.isRecycled())
				_bitmapModified = Bitmap.createBitmap(getWindowManager().getDefaultDisplay().getWidth(), getWindowManager().getDefaultDisplay().getHeight(), Config.ARGB_8888);
			canvasDisplay.drawBitmap(_bitmapModified, 0, 0, paintDisplay);
		}
		_imageViewPicturePreview.setImageBitmap(_bitmapDisplayed);
		_imageViewPicturePreview.invalidate();
	}
	
	public void setDirty(boolean dirty)
	{
		_dirty = dirty;
	}

	void startPelconnerActivity(PelconnerActivity.Type activityType)
	{
		Intent intent = null;
		Context context = PelconnerPictureActivity.this;
		boolean resultExpected = false;
		
		switch(activityType){
		case GALLERY:
			resultExpected = true;
			intent = new Intent(context, PelconnerGalleryActivity.class);
			// Starting Gallery without support for share
			intent.putExtra(PelconnerActivity.ACTIVITY_REQUEST_CODE, PelconnerActivity.ACTIVITY_REQUEST_CODE_GALLERY_REGULAR);
			startActivityForResult(intent, PelconnerActivity.ACTIVITY_REQUEST_CODE_GALLERY_REGULAR);
			return;
		case CAMERA:
			resultExpected = true;
			intent = new Intent(context, PelconnerCameraActivity.class);
			// Starting Camera
			intent.putExtra(PelconnerActivity.ACTIVITY_REQUEST_CODE, PelconnerActivity.ACTIVITY_REQUEST_CODE_CAMERA_REGULAR);
			startActivityForResult(intent, PelconnerActivity.ACTIVITY_REQUEST_CODE_CAMERA_REGULAR);
			return;
		case COLOR:
			resultExpected = true;
			intent = new Intent(context, PelconnerColorActivity.class);
			// Starting Color Chooser
			intent.putExtra(PelconnerActivity.ACTIVITY_REQUEST_CODE, PelconnerActivity.ACTIVITY_REQUEST_CODE_COLOR);
			startActivityForResult(intent, PelconnerActivity.ACTIVITY_REQUEST_CODE_COLOR);
			return;
		case HELP_PICTURE:
			intent = new Intent(context, PelconnerHelpActivity.class);
			intent.putExtra(PelconnerActivity.ACTIVITY_HELP_TYPE, PelconnerHelpActivity.Type.PICTURE);
			startActivity(intent);
			return;
		default:
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Do nothing for now
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if(event.values.length < 3)
		{
			return;
		}
		else if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		{
			
			_currentX = event.values[SensorManager.DATA_X];
			_currentY = event.values[SensorManager.DATA_Y];
			_currentZ = event.values[SensorManager.DATA_Z];
			
			
			// TODO: Check the treshold
			long timeDifference = 0;
			_currentTime = System.currentTimeMillis();
			timeDifference = _currentTime - _lastTime;
			double currentForceValue = Math.abs(_currentX + _currentY + _currentZ - _lastX - _lastY - _lastZ) / timeDifference * 10000;
			//Log.d("Force", "Current force: " + currentForceValue);
			if(currentForceValue > _tresholdMin)
			{
				_selectedAction = Availables.TOOLS_SHAKE_DRAW; 
				//Log.d("ACC", "lastX/currentX = " + _lastX + "/" + _currentX + "; lastY/currentY = " + _lastY + "/" + _currentY);
				float factorX, factorY, width, height;
				width = getWindowManager().getDefaultDisplay().getWidth();
				height = getWindowManager().getDefaultDisplay().getHeight();
				factorX = width / 40F;
				factorY = height / 40F;
				float downX, downY, upX, upY;
				downX = (_lastX == -1000) ? _lastX :(-1) *  _lastX * factorX + width / 2;
				downY = (_lastY == -1000) ? _lastY : _lastY * factorY + height / 2;
				upX = (-1) * _currentX * factorX + width / 2;
				upY = _currentY * factorY + height / 2;
				
				changeBitmapModified(_pelconnerBitmapModifier.modify(_selectedAction, downX, downY, upX, upY, null));
				updateDisplay(false);
			}
			
			_lastX = _currentX;
			_lastY = _currentY;
			_lastZ = _currentZ;
		}
		else if(event.sensor.getType() == Sensor.TYPE_ORIENTATION)
		{
	           _currentAzimuth = event.values[0];//Math.round(event.values[0]);
	           //Log.d("AZZ", "0/1/2 = " + event.values[0] + "/" + event.values[1] + "/" + event.values[2]);
	           
	           if(_startAzimuth != -1000)
	           {
	        	   _selectedAction = Availables.EDIT_ROTATE; 
	        	   changeBitmapModified(_pelconnerBitmapModifier.modify(_selectedAction, _currentAzimuth/* - _startAzimuth*/, 0, 0, 0, null));
	        	   updateDisplay(false);    	   
	           }
	           else
	           {
	        	   _startAzimuth = _currentAzimuth;
	           }
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
		
		if(!_blockSeekBar)
		{
		
			ProgressDialog progressDialog = null;//new ProgressDialog(PelconnerPictureActivity.this);;
			
			
			if(_seekBarLineWidth != null)
			{
				//_selectedAction = Availables.TOOLS_LINE_WIDTH;
				_pelconnerBitmapModifier.setLineWidth(progress);
				return;
			}
			else if(_seekBarAngle != null)
			{
				_selectedAction = Availables.EDIT_ANGLE;
			}
			else if(_seekBarZoom != null)
			{
				_selectedAction = Availables.EDIT_ZOOM_MANUAL;
			}
			else if(_seekBarSaturation != null)
			{
				_selectedAction = Availables.EFFECTS_SATURATION;
			}
			else if(_seekBarBrightness != null)
			{
				_selectedAction = Availables.EFFECTS_BRIGHTNESS;
			}
			else if(_seekBarContrast != null)
			{
				_selectedAction = Availables.EFFECTS_CONTRAST;
			}
			else if(_seekBarBlackAndWhite != null)
			{
				_selectedAction = Availables.EFFECTS_BLACK_AND_WHITE;
			}
			else if(_seekBarTransparency != null)
			{
				_selectedAction = Availables.EFFECTS_TRANSPARENCY;
			}
			else if(_seekBarNoise != null)
			{
				_selectedAction = Availables.EFFECTS_NOISE;
				_progressDialog = new ProgressDialog(PelconnerPictureActivity.this);
				_progressDialog.setMessage("Processing...");
				_progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				_progressDialog.setCancelable(false);
				
				//showDialog(ID_DIALOG_PROCESSING);
			}
			
			if(_progressDialog != null)
			{
				//progressDialog.show();
				//showDialog(ID_DIALOG_PROCESSING);
				
				/*
				
				if(threadProcessing == null)
				{
					threadProcessing = new Thread(new Runnable(){
		
						@Override
						public void run() {
							Message message = _handlerProcessing.obtainMessage();//(1, "show");
							message.obj = "show";
							_handlerProcessing.sendMessage(message);
							
							//showDialog(ID_DIALOG_PROCESSING);
							//_progressDialog.show();
							
							_bitmapModified = _pelconnerBitmapModifier.modify(_selectedAction, (float)progress, 0, 0, 0, null);
							
							//dismissDialog(ID_DIALOG_PROCESSING);
							//progressDialog.dismiss();
							//_progressDialog.dismiss();
							
							message = _handlerProcessing.obtainMessage();//(1, "dismiss");
							message.obj = "dismiss";
							_handlerProcessing.sendMessage(message);
						}
						
					});			
					
					synchronized (_bitmapModified) {
						threadProcessing.start();
					}
					
					boolean running = true;
					State threadState = threadProcessing.getState();
					while(threadState != State.TERMINATED)
					{
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						threadState = threadProcessing.getState();
					}
					
					threadProcessing = null;
					
					_imageViewPicturePreview.setImageBitmap(_bitmapModified);
					_imageViewPicturePreview.invalidate();	
				}
				*/
				
				AsyncTask<String, Float, Void> asyncTask = new AsyncTask<String, Float, Void>() {
	
					@Override
					protected Void doInBackground(final String... params) {
						_blockSeekBar = false;
						changeBitmapModified(_pelconnerBitmapModifier.modify(_selectedAction, (float)progress, 0, 0, 0, null));
						return null;
					}
	
					/* (non-Javadoc)
					 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
					 */
					@Override
					protected void onPostExecute(Void result) {
						
						dismissDialog(ID_DIALOG_PROCESSING);
						//super.onPostExecute(result);
					}
	
					/* (non-Javadoc)
					 * @see android.os.AsyncTask#onPreExecute()
					 */
					@Override
					protected void onPreExecute() {
						_blockSeekBar = true;
						showDialog(ID_DIALOG_PROCESSING);
						//super.onPreExecute();
					}
					
				};
				
				asyncTask.execute(String.valueOf(progress));
			}
			else
			{
				if(_selectedAction == Availables.EFFECTS_TRANSPARENCY)
				{
					/*
					_imageViewPicturePreview.setImageBitmap(_bitmapBackground);
					_imageViewPicturePreview.invalidate();
					*/
					
					//Log.d("ANGLE", "Angle = " + progress);
					
					Bitmap currentBitmap = _bitmapFinal.copy(_bitmapFinal.getConfig(), false);
					//_bitmapModified = Bitmap.createBitmap(_bitmapModified.getWidth(), _bitmapModified.getHeight(), Config.ARGB_8888);
					
					//_bitmapModified.recycle();
					try
					{
						changeBitmapModified(Bitmap.createBitmap(_bitmapModified.getWidth(), _bitmapModified.getHeight(), Config.ARGB_8888));
					}
					catch(OutOfMemoryError oome)
					{
						Log.e("OOM", "onProgressChanged");
						changeBitmapModified(Bitmap.createBitmap(_bitmapModified.getWidth(), _bitmapModified.getHeight(), Config.ARGB_4444));
					}
						
					Canvas canvas = new Canvas(_bitmapModified);
					canvas.drawColor(Color.TRANSPARENT);
					Paint paint = new Paint();
					
					int actualProgress = (int)Math.floor(((float)progress / 100F) * 255);
					
					paint.setARGB(actualProgress, 0, 0, 0);
					canvas.drawBitmap(currentBitmap.copy(currentBitmap.getConfig(), false), new Matrix(), paint);
					

					/*
					Bitmap bitmapHolder = Bitmap.createBitmap(_bitmapBackground);
					bitmapHolder = bitmapHolder.copy(bitmapHolder.getConfig(), true);
					Canvas canvasDisplay = new Canvas(bitmapHolder);
					Paint paintDisplay = new Paint();

					paintDisplay.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
					canvasDisplay.drawBitmap(_bitmapModified, 0, 0, paintDisplay);
					*/					
					
					_imageViewPicturePreview.setImageBitmap(/*bitmapHolder*/_bitmapModified);
					_imageViewPicturePreview.invalidate();						
					
					currentBitmap.recycle();
				}
				else if(true/*_selectedAction == Availables.EDIT_ANGLE*/)
				{
					/*
					_imageViewPicturePreview.setImageBitmap(_bitmapBackground);
					_imageViewPicturePreview.invalidate();
					*/
					
					//Log.d("ANGLE", "Angle = " + progress);
					
					
					changeBitmapModified(_pelconnerBitmapModifier.modify(_selectedAction, (float)progress, 0, 0, 0, null));
					
					/*
					Bitmap bitmapHolder = Bitmap.createBitmap(_bitmapBackground);
					bitmapHolder = bitmapHolder.copy(bitmapHolder.getConfig(), true);
					Canvas canvasDisplay = new Canvas(bitmapHolder);
					Paint paintDisplay = new Paint();

					paintDisplay.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
					canvasDisplay.drawBitmap(_bitmapModified, 0, 0, paintDisplay);
					*/

					_imageViewPicturePreview.setImageBitmap(/*bitmapHolder*/_bitmapModified);
					_imageViewPicturePreview.invalidate();					
				}
				else
				{
					changeBitmapModified(_pelconnerBitmapModifier.modify(_selectedAction, (float)progress, 0, 0, 0, null));
					updateDisplay(false);					
				}
			}
		}
		
		/*
		_bitmapModified = _pelconnerBitmapModifier.modify(_selectedAction, (float)progress, 0, 0, 0, null);
		
		if(progressDialog != null)
		{
			//progressDialog.dismiss();
			dismissDialog(ID_DIALOG_PROCESSING);
		}
		
		_imageViewPicturePreview.setImageBitmap(_bitmapModified);
		_imageViewPicturePreview.invalidate();
		*/
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.picture);
        initiate();
        //updateDisplay(false);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		_currentAction = new Action();
		_currentAction.setOption(_selectedAction);
		_currentAction.addParam((Integer)seekBar.getProgress());
		addAction();
	}

	
	@Override
	protected Dialog onCreateDialog(int id) {
		if(id == ID_DIALOG_PROCESSING)
		{
			ProgressDialog dialogProcessing = new ProgressDialog(this);
			dialogProcessing.setMessage("Processing...");
			//dialogProcessing.setIndeterminate(true);
			dialogProcessing.setCancelable(false);
			return dialogProcessing;
		}
		return super.onCreateDialog(id);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
