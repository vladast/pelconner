package com.terrestrial.pelconner.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.terrestrial.pelconner.R;
import com.terrestrial.pelconner.helper.PelconnerOption;
import com.terrestrial.pelconner.helper.PelconnerOption.Availables;

public class PelconnerOptionAdapter extends ArrayAdapter<PelconnerOption> /*implements AdapterView.OnItemClickListener*/
{

	private Context _context;
	private ArrayList<PelconnerOption> _options;
	
	public PelconnerOptionAdapter(Context context, int textViewResourceId, ArrayList<PelconnerOption> options) {
		super(context, textViewResourceId, options);
		_context = context;
		_options = options;
	}

    public PelconnerOptionAdapter(Context context, int textViewResourceId)
    {
    	super(context, textViewResourceId);
    	_context = context;
    	_options = new ArrayList<PelconnerOption>();    	
    }
	
	public void add(Availables option) {
		_options.add(new PelconnerOption(option));
	}

	/*
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		
	}*/
	
	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getCount()
	 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return _options.size();
	}

	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getItem(int)
	 */
	@Override
	public PelconnerOption getItem(int position) {
		return _options.get(position);
	}

	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//return super.getView(position, convertView, parent);
		View view = convertView;
    	if(view == null)
    	{
            LayoutInflater layoutInflater = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.option_row, null);
    	}
    	
    	PelconnerOption option = _options.get(position);
        if (option != null) 
        {
        	ImageView imageViewOption = (ImageView)view.findViewById(R.id.imageViewOption);
        	TextView textViewOption = (TextView)view.findViewById(R.id.textViewOption);
        	
        	imageViewOption.setImageResource(option.getImageResource());
        	textViewOption.setText(_context.getString(option.getTextResource()));
        }
		return view;
	}

}
