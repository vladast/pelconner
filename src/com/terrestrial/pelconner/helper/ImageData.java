package com.terrestrial.pelconner.helper;

public class ImageData {
	
	public enum Format{
		JPG,
		BMP,
		PNG
	};

	private String _name;
	private String _description;
	private int _height;
	private int _width;
	private boolean _saveLocation;
	//private float _latitude;
	//private float _longitude;
	private Format _format;
	
	public ImageData()
	{
		_height = _width = 0;
		_saveLocation = true;
		_format = Format.JPG;
	}
	
	/**
	 * @return the _name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * @param _name the _name to set
	 */
	public void setName(String name) {
		_name = name;
	}
	
	public String getDescription()
	{
		return _description;
	}
	
	public void setDescription(String description)
	{
		_description = description;
	}
	
	/**
	 * @return the _height
	 */
	public int getHeight() {
		return _height;
	}
	/**
	 * @param _height the _height to set
	 */
	public void setHeight(int height) {
		_height = height;
	}
	/**
	 * @return the _width
	 */
	public int getWidth() {
		return _width;
	}
	/**
	 * @param _width the _width to set
	 */
	public void setWidth(int width) {
		_width = width;
	}
	/**
	 * @return the _saveLocation
	 */
	public boolean isSaveLocation() {
		return _saveLocation;
	}
	/**
	 * @param _saveLocation the _saveLocation to set
	 */
	public void setSaveLocation(boolean saveLocation) {
		_saveLocation = saveLocation;
	}
	/**
	 * @return the _format
	 */
	public Format getFormat() {
		return _format;
	}
	/**
	 * @param _format the _format to set
	 */
	public void setFormat(Format format) {
		_format = format;
	}
}
