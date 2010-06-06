package com.android.launcher;

import java.util.Calendar;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

import java.io.FileReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParserException;
import android.graphics.Color;
import android.widget.Toast;
import java.io.FileWriter;
import org.xmlpull.v1.XmlSerializer;
import android.util.Xml;
import java.util.ArrayList;
import java.io.File;

public class MyLauncherSettings extends PreferenceActivity implements OnPreferenceChangeListener {

    private static final String ALMOSTNEXUS_PREFERENCES = "launcher.preferences.almostnexus";
    private boolean shouldRestart=false;
    private static final String FROYOMSG="Changing this setting will make the Launcher restart itself";
    private static final String NORMALMSG="Changing this setting will make the Launcher restart itself";   
    private String mMsg;
    private Context mContext;
    
    private static final String XML_FILENAME = "adw_settings.xml";
    
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		//TODO: ADW should i read stored values after addPreferencesFromResource?
		mMsg=(Build.VERSION.SDK_INT>=8)?FROYOMSG:NORMALMSG;
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(ALMOSTNEXUS_PREFERENCES);
        addPreferencesFromResource(R.xml.launcher_settings);
        dlgSeekBarPreference desktopScreens= (dlgSeekBarPreference) findPreference("desktopScreens");
        desktopScreens.setMin(2);
        desktopScreens.setOnPreferenceChangeListener(this);
        dlgSeekBarPreference defaultScreen= (dlgSeekBarPreference) findPreference("defaultScreen");
        defaultScreen.setMin(1);
        defaultScreen.setMax(AlmostNexusSettingsHelper.getDesktopScreens(this)-1);
        defaultScreen.setOnPreferenceChangeListener(this);
        Preference drawerNew = (Preference) findPreference("drawerNew");
        drawerNew.setOnPreferenceChangeListener(this);
        dlgSeekBarPreference columnsPortrait= (dlgSeekBarPreference) findPreference("drawerColumnsPortrait");
        columnsPortrait.setMin(1);
        dlgSeekBarPreference rowsPortrait= (dlgSeekBarPreference) findPreference("drawerRowsPortrait");
        rowsPortrait.setMin(1);
        dlgSeekBarPreference columnsLandscape= (dlgSeekBarPreference) findPreference("drawerColumnsLandscape");
        columnsLandscape.setMin(1);
        dlgSeekBarPreference rowsLandscape= (dlgSeekBarPreference) findPreference("drawerRowsLandscape");
        rowsLandscape.setMin(1);
        dlgSeekBarPreference zoomSpeed= (dlgSeekBarPreference) findPreference("zoomSpeed");
        zoomSpeed.setMin(300);
        dlgSeekBarPreference uiScaleAB= (dlgSeekBarPreference) findPreference("uiScaleAB");
        uiScaleAB.setMin(1);
        Preference donateLink = (Preference) findPreference("donatePref");
        donateLink.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				String url = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=9S8WKFETUYRHG";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);				
				return true;
			}
		});
		
        Preference uiHideLabels = (Preference) findPreference("uiHideLabels");
        uiHideLabels.setOnPreferenceChangeListener(this);
        Preference mDateColorPref = findPreference("highlights_color");
        
        mContext=this;
        
        Preference importFromXML = findPreference("xml_import");
        importFromXML.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
                AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                alertDialog.setTitle(getResources().getString(R.string.title_dialog_xml));
                alertDialog.setMessage(getResources().getString(R.string.message_dialog_import));
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(android.R.string.ok), 
                    new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        importXML();
                    }
                });
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(android.R.string.cancel), 
                    new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    
                    }
                });
                alertDialog.show();
                return true;
            }
        });        
        
        Preference exportToXML = findPreference("xml_export");
        exportToXML.setOnPreferenceClickListener(new OnPreferenceClickListener() {        
			public boolean onPreferenceClick(Preference preference) {
                AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                alertDialog.setTitle(getResources().getString(R.string.title_dialog_xml));
                alertDialog.setMessage(getResources().getString(R.string.message_dialog_export));
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(android.R.string.ok), 
                    new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        exportXML();
                    }
                });
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(android.R.string.cancel), 
                    new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    
                    }
                });
                alertDialog.show();
                return true;
            }
        });        

    }
    
	@Override
	protected void onPause(){
		if(shouldRestart){
			if(Build.VERSION.SDK_INT<=7){
				Intent intent = new Intent(getApplicationContext(), Launcher.class);
	            PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(),0, intent, 0);
	
	            // We want the alarm to go off 30 seconds from now.
	            Calendar calendar = Calendar.getInstance();
	            calendar.setTimeInMillis(System.currentTimeMillis());
	            calendar.add(Calendar.SECOND, 1);
	
	            // Schedule the alarm!
	            AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
	            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
	   			ActivityManager acm = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		        acm.restartPackage("com.android.launcher");
			}else{
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		}
		super.onPause();
	}
	
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.getKey().equals("desktopScreens")) {
			dlgSeekBarPreference pref = (dlgSeekBarPreference) findPreference("defaultScreen");
			pref.setMax((Integer) newValue+1);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(mMsg)
			       .setCancelable(false)
			       .setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
							shouldRestart=true;
			           }
			       });
			AlertDialog alert = builder.create();
			alert.show();
		}else if (preference.getKey().equals("defaultScreen")){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(mMsg)
			       .setCancelable(false)
			       .setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
							shouldRestart=true;
			           }
			       });
			AlertDialog alert = builder.create();
			alert.show();		
		}else if(preference.getKey().equals("drawerNew")){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(mMsg)
			       .setCancelable(false)
			       .setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
							shouldRestart=true;
			           }
			       });
			AlertDialog alert = builder.create();
			alert.show();
		}else if(preference.getKey().equals("uiHideLabels")){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(mMsg)
			       .setCancelable(false)
			       .setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
							shouldRestart=true;
			           }
			       });
			AlertDialog alert = builder.create();
			alert.show();
		}else if(preference.getKey().equals("highlights_color")){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(mMsg)
			       .setCancelable(false)
			       .setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
							shouldRestart=true;
			           }
			       });
			AlertDialog alert = builder.create();
			alert.show();
		}
        return true;  
	}    

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        try {
            if (preference.getKey().equals("highlights_color")) {
            	ColorPickerDialog cp = new ColorPickerDialog(this,mHighlightsColorListener,readHighlightsColor());
            	cp.show();
            }
        }
        catch (NullPointerException e) {
        }
        return false;
	}
	
    private int readHighlightsColor() {
    	return AlmostNexusSettingsHelper.getHighlightsColor(this);
    }

    ColorPickerDialog.OnColorChangedListener mHighlightsColorListener =
    	new ColorPickerDialog.OnColorChangedListener() {
    	public void colorChanged(int color) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setMessage(mMsg)
			       .setCancelable(false)
			       .setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
							shouldRestart=true;
			           }
			       });
			AlertDialog alert = builder.create();
			alert.show();
    		getPreferenceManager().getSharedPreferences().edit().putInt("highlights_color", color).commit();
    	}
    };
    
    // Wysie: Lazy way
    private void importXML() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Toast.makeText(mContext, R.string.xml_sdcard_unmounted, Toast.LENGTH_SHORT).show();
            return;
        }
        
        FileReader reader = null;
        boolean success = false;
                
        try {
            reader = new FileReader(new File(Environment.getExternalStorageDirectory() + "/" + XML_FILENAME));
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(reader);
            int eventType = parser.getEventType();
            String prefType = null;
            String prefValue = null;
            SharedPreferences sp = mContext.getSharedPreferences(ALMOSTNEXUS_PREFERENCES, mContext.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
						prefType = parser.getName().trim();
						if (!prefType.equalsIgnoreCase("adw_settings")) {
						    prefValue = parser.nextText();
						    
						    if (prefType.equals("homeBinding")) {
						        editor.putString(prefType, prefValue);
						    }
						    else {
    						    try {
	    					        editor.putInt(prefType, Integer.parseInt(prefValue));
	    					    }
	    					    catch (NumberFormatException e) {
	    					        if (prefValue.equalsIgnoreCase("true") || prefValue.equalsIgnoreCase("false")) {
	    					            editor.putBoolean(prefType, Boolean.parseBoolean(prefValue));
	    					        }
	    					    }
						    }
						}						    
						break;
                }
                eventType = parser.next();
            }
            editor.commit();
            success = true;
        }
        catch (FileNotFoundException e) {
            Toast.makeText(mContext, R.string.xml_file_not_found, Toast.LENGTH_SHORT).show();
        }
        catch (IOException e) {
            Toast.makeText(mContext, R.string.xml_io_exception, Toast.LENGTH_SHORT).show();
        }
        catch (XmlPullParserException e) {
            Toast.makeText(mContext, R.string.xml_parse_error, Toast.LENGTH_SHORT).show();
        }
        finally {
            if (reader != null) {
        		try {
	        	    reader.close();
	        	} catch (IOException e) {
	        	}
	        }
        }
        
        if (success) {
            Toast.makeText(mContext, R.string.xml_import_success, Toast.LENGTH_SHORT).show();
            shouldRestart = true;
        }
    }
    
    private void exportXML() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Toast.makeText(mContext, R.string.xml_sdcard_unmounted, Toast.LENGTH_SHORT).show();
            return;
        }        
        
        FileWriter writer = null;
        File outFile = new File(Environment.getExternalStorageDirectory() + "/" + "tempADWSettings.xml");
        boolean success = false;
        
        try {            
            outFile.createNewFile();
            writer = new FileWriter(outFile);
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(writer);
            
            // Start XML
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", "adw_settings");            
            
            // Screen Prefs
            serializer.startTag("", "wallpaperHack");
            serializer.text("" + AlmostNexusSettingsHelper.getWallpaperHack(this));
            serializer.endTag("", "wallpaperHack");            
            serializer.startTag("", "desktopRotation");
            serializer.text("" + AlmostNexusSettingsHelper.getDesktopRotation(this));
            serializer.endTag("", "desktopRotation");            
            serializer.startTag("", "hideStatusbar");
            serializer.text("" + AlmostNexusSettingsHelper.getHideStatusbar(this));
            serializer.endTag("", "hideStatusbar");            
            serializer.startTag("", "desktopScreens");
            serializer.text("" + AlmostNexusSettingsHelper.getDesktopScreens(this));
            serializer.endTag("", "desktopScreens");            
            serializer.startTag("", "defaultScreen");
            serializer.text("" + AlmostNexusSettingsHelper.getDefaultScreen(this));
            serializer.endTag("", "defaultScreen");            
            serializer.startTag("", "desktopSpeed");
            serializer.text("" + AlmostNexusSettingsHelper.getDesktopSpeed(this));
            serializer.endTag("", "desktopSpeed");            
            serializer.startTag("", "desktopBounce");
            serializer.text("" + AlmostNexusSettingsHelper.getDesktopBounce(this));
            serializer.endTag("", "desktopBounce");
            
            // Drawer Settings
            serializer.startTag("", "zoomSpeed");
            serializer.text("" + AlmostNexusSettingsHelper.getZoomSpeed(this));
            serializer.endTag("", "zoomSpeed");            
            serializer.startTag("", "drawerNew");
            serializer.text("" + AlmostNexusSettingsHelper.getDrawerNew(this));
            serializer.endTag("", "drawerNew");            
            serializer.startTag("", "drawerAnimated");
            serializer.text("" + AlmostNexusSettingsHelper.getDrawerAnimated(this));
            serializer.endTag("", "drawerAnimated");            
            serializer.startTag("", "drawerAlpha");
            serializer.text("" + AlmostNexusSettingsHelper.getDrawerAlpha(this));
            serializer.endTag("", "drawerAlpha");            
            serializer.startTag("", "drawerColumnsPortrait");
            serializer.text("" + AlmostNexusSettingsHelper.getColumnsPortrait(this));
            serializer.endTag("", "drawerColumnsPortrait");            
            serializer.startTag("", "drawerRowsPortrait");
            serializer.text("" + AlmostNexusSettingsHelper.getRowsPortrait(this));
            serializer.endTag("", "drawerRowsPortrait");            
            serializer.startTag("", "drawerColumnsLandscape");
            serializer.text("" + AlmostNexusSettingsHelper.getColumnsLandscape(this));
            serializer.endTag("", "drawerColumnsLandscape");            
            serializer.startTag("", "drawerRowsLandscape");
            serializer.text("" + AlmostNexusSettingsHelper.getRowsLandscape(this));
            serializer.endTag("", "drawerRowsLandscape");
            
            // Preview Settings
            serializer.startTag("", "previewsNew");
            serializer.text("" + AlmostNexusSettingsHelper.getNewPreviews(this));
            serializer.endTag("", "previewsNew");            
            serializer.startTag("", "previewsFullScreen");
            serializer.text("" + AlmostNexusSettingsHelper.getFullScreenPreviews(this));
            serializer.endTag("", "previewsFullScreen");
            
            // UI Settings
            serializer.startTag("", "homeBinding");
            serializer.text("" + AlmostNexusSettingsHelper.getHomeBinding(this));
            serializer.endTag("", "homeBinding");            
            serializer.startTag("", "uiDots");
            serializer.text("" + AlmostNexusSettingsHelper.getUIDots(this));
            serializer.endTag("", "uiDots");            
            serializer.startTag("", "uiDockbar");
            serializer.text("" + AlmostNexusSettingsHelper.getUIDockbar(this));
            serializer.endTag("", "uiDockbar");            
            serializer.startTag("", "uiCloseDockbar");
            serializer.text("" + AlmostNexusSettingsHelper.getUICloseDockbar(this));
            serializer.endTag("", "uiCloseDockbar");            
            serializer.startTag("", "uiLAB");
            serializer.text("" + AlmostNexusSettingsHelper.getUILAB(this));
            serializer.endTag("", "uiLAB");            
            serializer.startTag("", "uiRAB");
            serializer.text("" + AlmostNexusSettingsHelper.getUIRAB(this));
            serializer.endTag("", "uiRAB");            
            serializer.startTag("", "uiTint");
            serializer.text("" + AlmostNexusSettingsHelper.getUITint(this));
            serializer.endTag("", "uiTint");            
            serializer.startTag("", "uiScaleAB");
            serializer.text("" + AlmostNexusSettingsHelper.getuiScaleAB(this));
            serializer.endTag("", "uiScaleAB");            
            serializer.startTag("", "uiAppsBg");
            serializer.text("" + AlmostNexusSettingsHelper.getUIAppsBg(this));
            serializer.endTag("", "uiAppsBg");            
            serializer.startTag("", "uiABBg");
            serializer.text("" + AlmostNexusSettingsHelper.getUIABBg(this));
            serializer.endTag("", "uiABBg");            
            serializer.startTag("", "uiHideLabels");
            serializer.text("" + AlmostNexusSettingsHelper.getUIHideLabels(this));
            serializer.endTag("", "uiHideLabels");            
            serializer.startTag("", "highlights_color");
            serializer.text("" + AlmostNexusSettingsHelper.getHighlightsColor(this));
            serializer.endTag("", "highlights_color");
            
            // End XML
            serializer.endTag("", "adw_settings");
            serializer.endDocument();
            serializer.flush();
            success = true;
        }
        catch (Exception e) {
            Toast.makeText(mContext, R.string.xml_write_error, Toast.LENGTH_SHORT).show();
        }
        finally {
            if (writer != null) {
        		try {
	        	    writer.close();
	        	} catch (IOException e) {
	        	}
	        }
        }
        
        if (success) {
            File xmlFile = new File(Environment.getExternalStorageDirectory() + "/" + XML_FILENAME);
            outFile.renameTo(xmlFile);
            Toast.makeText(mContext, R.string.xml_export_success, Toast.LENGTH_SHORT).show();
        }
        
        if (outFile.exists())
            outFile.delete();
    }
}
