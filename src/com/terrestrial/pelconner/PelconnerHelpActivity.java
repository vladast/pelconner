package com.terrestrial.pelconner;

import com.terrestrial.pelconner.R;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class PelconnerHelpActivity extends PelconnerActivity {

	static enum Type
	{
		MAIN,
		PICTURE
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		TAG = PelconnerActivity.ACTIVITY_TAG_MAIN;
		
        // Removing title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		int layoutResID = 0;
		
		Bundle bundleExtras = this.getIntent().getExtras();
        if(bundleExtras != null) // Extras received from Gallery
        {
        	Type type = (Type)bundleExtras.getSerializable(PelconnerActivity.ACTIVITY_HELP_TYPE);
            if(type == Type.MAIN)
            {
            	//Log.d("HELP", "Main");
            	layoutResID = R.layout.help_main;
            }
            else if (type == Type.PICTURE)
            {
            	//Log.d("HELP", "Picture");
            	layoutResID = R.layout.help_picture;
            }
        }
		
        if(layoutResID != 0)
        	setContentView(layoutResID);
        
		PelconnerActivity.addReference(this);
	}

}
