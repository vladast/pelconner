package com.terrestrial.pelconner.dialogs;

import com.terrestrial.pelconner.R;

import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class PelconnerAlertDialog extends PelconnerDialog {
	
	private Button buttonYes;
	private Button buttonNo;
	private TextView textViewTitle;
	private TextView textViewMessage;
	//private ImageView imageViewIcon;
	
	private Answer _answer;
	
	private PelconnerAlertDialog(Context context) {
		super(context);
		_context = context;
		TAG = PelconnerDialog.DIALOG_TAG_ALERT;
		
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		
		setContentView(R.layout.alert);
		
		buttonYes = (Button)findViewById(R.id.buttonYes);
		buttonNo = (Button)findViewById(R.id.buttonNo);
		textViewMessage = (TextView)findViewById(R.id.textViewMessage);
		textViewTitle = (TextView)findViewById(R.id.textViewTitle);
		//imageViewIcon = (ImageView)findViewById(R.id.imageViewIcon);
	}

	public PelconnerAlertDialog(Context context, int iconResId, int titleResId, int messageResId)
	{
		super(context);
		_context = context;
		TAG = PelconnerDialog.DIALOG_TAG_ALERT;
		
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		
		setContentView(R.layout.alert);
		
		buttonYes = (Button)findViewById(R.id.buttonYes);
		buttonNo = (Button)findViewById(R.id.buttonNo);
		textViewMessage = (TextView)findViewById(R.id.textViewMessage);
		textViewTitle = (TextView)findViewById(R.id.textViewTitle);
		//imageViewIcon = (ImageView)findViewById(R.id.imageViewIcon);
		
		setIcon(iconResId);
		setTitle(titleResId);
		setMessage(messageResId);
		
		buttonYes.setOnClickListener(this);
		buttonNo.setOnClickListener(this);
	}
	
	/**
	 * Set dialog icon from drawable resource
	 * @param iconResId
	 * Resource ID of the icon
	 */
	public void setIcon(int iconResId)
	{
		//imageViewIcon.setImageResource(iconResId);
	}
	
	/**
	 * Set dialog title from resource string
	 * @param titleResId
	 * Resource ID of the title string
	 */
	public void setTitle(int titleResId)
	{
		textViewTitle.setText(titleResId);
	}
	
	/**
	 * Dialog answer accessor
	 * @return
	 * Chosen answer
	 */
	public Answer getAnswer()
	{
		return _answer;
	}
	
	/**
	 * Set dialog message from resource string
	 * @param messageResId
	 * Resource ID o the message string
	 */
	public void setMessage(int messageResId)
	{
		textViewMessage.setText(messageResId);
	}
	
	/* (non-Javadoc)
	 * @see com.android.pelconner.dialogs.PelconnerDialog#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		//super.onClick(v);
		int buttonId = v.getId();
		if(buttonId == R.id.buttonYes)
		{
			_answer = PelconnerDialog.Answer.YES;
			dismiss();
		}
		else
		{
			_answer = PelconnerDialog.Answer.NO;
			dismiss();
		}
	}
}
