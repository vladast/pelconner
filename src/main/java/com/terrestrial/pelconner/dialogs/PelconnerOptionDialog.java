package com.terrestrial.pelconner.dialogs;

import com.terrestrial.pelconner.R;
import com.terrestrial.pelconner.adapter.PelconnerOptionAdapter;
import com.terrestrial.pelconner.helper.PelconnerOption;
import com.terrestrial.pelconner.helper.PelconnerOption.Availables;

import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class PelconnerOptionDialog extends PelconnerDialog implements OnItemClickListener {

	private Context _context;
	private PelconnerOptionAdapter _pelconnerOptionAdapter;
	private TextView _textViewTitle;
	private PelconnerOption _selectedOption;
	
	public PelconnerOptionDialog(Context context) {
		super(context);
		_context = context;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.options); //
		TAG = PelconnerDialog.DIALOG_TAG_OPTION;
		
		_pelconnerOptionAdapter = new PelconnerOptionAdapter(context, R.layout.option_row);
				
		_textViewTitle = (TextView)findViewById(R.id.textViewOptionsTitle);
		if(_textViewTitle != null)
		{
			_textViewTitle.setText(_context.getString(R.string.about)); // TODO: Change with correct title!
		}
		
		ListView listViewOptions = (ListView)findViewById(R.id.listViewOptions);
		if(listViewOptions != null)
		{
			listViewOptions.setAdapter(_pelconnerOptionAdapter);
			listViewOptions.setOnItemClickListener(this);
		}
	}

	/**
	 * Adds option to the list
	 * @param option
	 */
	public void add(Availables option)
	{
		_pelconnerOptionAdapter.add(option);
	}
	
	public void setTitle(int stringResId)
	{
		if(_textViewTitle != null)
		{
			_textViewTitle.setText(_context.getString(stringResId));
		}		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long resourceId) {
		_selectedOption = _pelconnerOptionAdapter.getItem(position);
		dismiss();
	}
	
	public PelconnerOption getSelectedOption()
	{
		if(_selectedOption != null)
		{
			return _selectedOption;
		}
		return new PelconnerOption(PelconnerOption.Availables.NONE);
	}

}
