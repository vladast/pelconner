package com.terrestrial.pelconner.helper;

import com.terrestrial.pelconner.R;

public class PelconnerOption {
	
	public static enum Availables
	{
		NONE,
		BITMAP,
		EDIT_NEW,
		MAIN_IMPORT_GALLERY,
		MAIN_IMPORT_CAMERA,
		EDIT_IMPORT_GALLERY,
		EDIT_IMPORT_CAMERA,
		EDIT_MOVE,
		EDIT_ROTATE,
		EDIT_ANGLE,
		EDIT_RESIZE,
		EDIT_STRETCH,
		EDIT_ZOOM,
		EDIT_ZOOM_MANUAL,
		EDIT_PINCH_ZOOM,
		EDIT_CROP,
		TOOLS_TEXT,
		TOOLS_FILL,
		TOOLS_COLOR_TEST, // For testing purposes! Remove it afterwards!
		TOOLS_RECTANGLE_SELECT,
		TOOLS_CIRCLE_SELECT,
		TOOLS_DRAW,
		TOOLS_RECTANGLE,
		TOOLS_ELLIPSE,
		TOOLS_SHAKE_DRAW,
		TOOLS_LINE,
		TOOLS_LINE_WIDTH,
		EFFECTS_TRANSPARENCY,
		EFFECTS_NOISE,
		EFFECTS_GRAYSCALE,
		EFFECTS_BLACK_AND_WHITE,
		EFFECTS_SATURATION,
		EFFECTS_CONTRAST,
		EFFECTS_BRIGHTNESS,
		EFFECTS_NEGATIVE,
		MORE_ABOUT,
		MORE_HELP,
		MORE_QUIT,
		SNAPSHOT_IMPORT,
		SNAPSHOT_SAVE,
		SNAPSHOT_SHARE,
		SNAPSHOT_CANCEL
	}
	
	private int _resourceImageId;
	private int _resourceTextId;
	private Availables _option;
	
	public PelconnerOption(Availables option)
	{
		_option = option;
		switch(_option){
		case EDIT_NEW:
			_resourceImageId = R.drawable.ic_option_edit_new_picture;
			_resourceTextId = R.string.new_picture;	
			break;
		case EDIT_IMPORT_GALLERY:
			_resourceImageId = R.drawable.ic_option_edit_gallery;
			_resourceTextId = R.string.import_picture_gallery;
			break;
		case EDIT_IMPORT_CAMERA:
			_resourceImageId = R.drawable.ic_option_edit_camera;
			_resourceTextId = R.string.import_picture_camera;
			break;
		case EDIT_MOVE:
			_resourceImageId = R.drawable.ic_option_edit_move;
			_resourceTextId = R.string.edit_move;
			break;
		case EDIT_ROTATE:
			_resourceImageId = R.drawable.ic_option_edit_rotate;
			_resourceTextId = R.string.edit_rotate;
			break;
		case EDIT_RESIZE:
			_resourceImageId = R.drawable.ic_launcher;
			_resourceTextId = R.string.edit_resize;
			break;
		case EDIT_STRETCH:
			_resourceImageId = R.drawable.ic_launcher;
			_resourceTextId = R.string.edit_stretch;
			break;
		case EDIT_ZOOM:
			_resourceImageId = R.drawable.ic_button_zoom;
			_resourceTextId = R.string.edit_zoom;
			break;
		case EDIT_PINCH_ZOOM:
			_resourceImageId = R.drawable.ic_option_tools_pinch_zoom;
			_resourceTextId = R.string.edit_pinch_zoom;
			break;
		case EDIT_CROP:
			_resourceImageId = R.drawable.ic_option_tools_crop;
			_resourceTextId = R.string.edit_crop;
			break;
		case TOOLS_FILL:
			_resourceImageId = R.drawable.ic_option_tools_fill;
			_resourceTextId = R.string.tools_fill;
			break;
		case TOOLS_COLOR_TEST:
			_resourceImageId = R.drawable.ic_launcher;
			_resourceTextId = R.string.tools_color_test;
			break;
		case TOOLS_RECTANGLE_SELECT:
			_resourceImageId = R.drawable.ic_launcher;
			_resourceTextId = R.string.tools_rectangle_select;
			break;
		case TOOLS_CIRCLE_SELECT:
			_resourceImageId = R.drawable.ic_launcher;
			_resourceTextId = R.string.tools_circle_select;
			break;
		case TOOLS_DRAW:
			_resourceImageId = R.drawable.ic_option_tools_draw;
			_resourceTextId = R.string.tools_draw;
			break;
		case TOOLS_RECTANGLE:
			_resourceImageId = R.drawable.ic_option_tools_rectangle;
			_resourceTextId = R.string.tools_rectangle;
			break;
		case TOOLS_ELLIPSE:
			//_resourceImageId = R.drawable.ic_option_edit_ellipse;
			//_resourceTextId = R.string.tools_ellipse;
			_resourceImageId = R.drawable.ic_option_tools_circle;
			_resourceTextId = R.string.tools_circle;
			break;
		case TOOLS_SHAKE_DRAW:
			_resourceImageId = R.drawable.ic_option_tools_shake;
			_resourceTextId = R.string.tools_shake_draw;
			break;			
		case TOOLS_LINE:
			_resourceImageId = R.drawable.ic_option_tools_line;
			_resourceTextId = R.string.tools_line;
			break;
		case TOOLS_TEXT:
			_resourceImageId = R.drawable.ic_option_tools_text;
			_resourceTextId = R.string.edit_text;
			break;
		case MORE_ABOUT:
			_resourceImageId = R.drawable.ic_menu_info;//R.drawable.ic_option_more_info;
			_resourceTextId = R.string.info;
			break;
		case MORE_HELP:
			_resourceImageId = R.drawable.ic_menu_help;//R.drawable.ic_option_more_help;
			_resourceTextId = R.string.help;
			break;
		case MORE_QUIT:
			_resourceImageId = R.drawable.ic_menu_quit;//R.drawable.ic_option_more_quit;
			_resourceTextId = R.string.quit;
			break;
		case EFFECTS_TRANSPARENCY:
			_resourceImageId = R.drawable.ic_button_transparency;
			_resourceTextId = R.string.effects_transparency;
			break;
		case EFFECTS_NOISE:
			_resourceImageId = R.drawable.ic_launcher;
			_resourceTextId = R.string.effects_noise;
			break;
		case EFFECTS_GRAYSCALE:
			_resourceImageId = R.drawable.ic_button_grayscale;
			_resourceTextId = R.string.effects_grayscale;
			break;
		case EFFECTS_BLACK_AND_WHITE:
			_resourceImageId = R.drawable.ic_launcher;
			_resourceTextId = R.string.effects_black_and_white;
			break;
		case EFFECTS_SATURATION:
			_resourceImageId = R.drawable.ic_button_saturation;
			_resourceTextId = R.string.effects_saturation;
			break;
		case EFFECTS_BRIGHTNESS:
			_resourceImageId = R.drawable.ic_button_brightness;
			_resourceTextId = R.string.effects_brightness;
			break;
		case EFFECTS_CONTRAST:
			_resourceImageId = R.drawable.ic_button_contrast;
			_resourceTextId = R.string.effects_contrast;
			break;
		case EFFECTS_NEGATIVE:
			_resourceImageId = R.drawable.ic_button_negative;
			_resourceTextId = R.string.effects_negative;			
			break;
		case SNAPSHOT_IMPORT:
			_resourceImageId = R.drawable.ic_menu_import;
			_resourceTextId = R.string.dialog_camera_button_import;
			break;
		case SNAPSHOT_CANCEL:
			_resourceImageId = R.drawable.ic_button_decline;
			_resourceTextId = R.string.dialog_camera_button_cancel;
			break;
		case SNAPSHOT_SAVE:
			_resourceImageId = R.drawable.ic_menu_save;
			_resourceTextId = R.string.dialog_camera_button_save;
			break;
		case SNAPSHOT_SHARE:
			_resourceImageId = R.drawable.ic_menu_share;
			_resourceTextId = R.string.dialog_camera_button_share;
			break;
		default:
			// Option that hasn't been implemented or uninitialized option
			_resourceImageId = R.drawable.ic_launcher;
			_resourceTextId = R.string.app_name;
		}
	}
	
	public Availables getOption()
	{
		return _option;
	}
	
	public int getImageResource()
	{
		return _resourceImageId;
	}
	
	public int getTextResource()
	{
		return _resourceTextId;
	}
}
