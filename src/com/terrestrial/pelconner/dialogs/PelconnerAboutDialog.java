package com.terrestrial.pelconner.dialogs;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.terrestrial.pelconner.R;

public class PelconnerAboutDialog extends PelconnerDialog implements OnClickListener {

	private Button _buttonClose;
	private TextView _textViewVersion;
	
	public PelconnerAboutDialog(Context context) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.about);
		
		TAG = PelconnerDialog.DIALOG_TAG_ABOUT;
		
		_context = context;
		
		_buttonClose = (Button)findViewById(R.id.buttonClose);
		_buttonClose.setOnClickListener(this);
		
		//@+id/textViewVersion
		_textViewVersion = (TextView)findViewById(R.id.textViewVersion);
		try
		{
			_textViewVersion.setText(getVersion());
		}
		catch(NameNotFoundException e)
		{
			// TODO Implement error handling...
		}
	}

	/* (non-Javadoc)
	 * @see com.android.pelconner.dialogs.PelconnerDialog#onClick(android.view.View)
	 */
	@Override
	public void onClick(View sender) {
		if(sender == _buttonClose)
		{
			dismiss();
		}
	}
	
	private String getVersion() throws NameNotFoundException
	{
		String stringVersionName = null;
		PackageInfo packageInfo = _context.getPackageManager().getPackageInfo(_context.getPackageName(), 0);
		stringVersionName = _context.getString(R.string.about_version_text) + " " + packageInfo.versionName;
		
		return stringVersionName;
	}
}
