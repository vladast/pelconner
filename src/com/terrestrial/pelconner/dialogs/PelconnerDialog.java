package com.terrestrial.pelconner.dialogs;

import com.terrestrial.pelconner.R;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

public class PelconnerDialog extends Dialog implements OnClickListener{

	public static final String DIALOG_TAG_ABOUT = "PelconnerAboutDialog";
	public static final String DIALOG_TAG_ALERT = "PelconnerAlertDialog";
	public static final String DIALOG_TAG_NO_SDCARD = "PelconnerNosdcardDialog";
	public static final String DIALOG_TAG_SAVE = "PelconnerSaveDialog";
	public static final String DIALOG_TAG_SNAPSHOT = "PelconnerSnapshotDialog";	
	public static final String DIALOG_TAG_OPTION = "PelconnerOptionDialog";
	public static final String DIALOG_TAG_TEXT = "PelconnerTextDialog";
	public static final String DIALOG_TAG_COLOR = "PelconnerColorDialog";
	
	protected String TAG = "PelconnerDialog";
	
	protected Context _context;

	static public enum Answer{
		YES, 
		NO,
		CANCEL,
		OK
	};
	
	public String getTag()
	{
		return TAG;
	}
	
	public PelconnerDialog(Context context) {
		super(context);
		_context = context;
	}
	
	/**
	 * PelconnerDialog c-tor
	 * @param context
	 * Context reference
	 * @param iconResId
	 * Resource ID of the dialog icon
	 * @param titleResId
	 * Resource ID of the title text
	 * @param messageResId
	 * Resrouce ID of the message text
	 */
	public PelconnerDialog(Context context, int iconResId, int titleResId, int messageResId)
	{
		super(context);
		_context = context;
	}
	
	private void setDialogText(int resId)
	{
		//_context.getResources().getDrawable(id)
		
	}
	
	private void setDialogIcon(int resId)
	{
		
	}

	@Override
	public void onClick(android.view.View v) {
		// TODO Auto-generated method stub
		
	}

}
