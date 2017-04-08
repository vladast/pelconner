package com.terrestrial.pelconner.dialogs;

import com.terrestrial.pelconner.R;
import com.terrestrial.pelconner.PelconnerActivity;

import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class PelconnerNosdcardDialog extends PelconnerDialog 
{
	Button buttonQuit;

	public PelconnerNosdcardDialog(Context context) {
		super(context);
		_context = context;
		TAG = PelconnerDialog.DIALOG_TAG_NO_SDCARD;
		
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		
		setContentView(R.layout.nosdcard);
		
		buttonQuit = (Button)findViewById(R.id.buttonNosdcardQuit);
		buttonQuit.setOnClickListener(this);
	}

	/* (non-Javadoc)
	 * @see com.android.pelconner.dialogs.PelconnerDialog#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) 
	{
		//super.onClick(v);
		dismiss();
	}

}
