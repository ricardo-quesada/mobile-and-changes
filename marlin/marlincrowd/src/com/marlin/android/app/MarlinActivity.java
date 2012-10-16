package com.marlin.android.app;

import com.marlin.android.app.service.AppService;


import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug.MemoryInfo;
import android.provider.CallLog;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;


public class MarlinActivity extends TabActivity {

	public static final String PREFS_NAME = "MarlinPlatformSettings";

	private ImageView splash;
	private boolean optin;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Restore preferences
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		optin = settings.getBoolean(Constants.OPTIN, false);

		splash = (ImageView) findViewById(R.id.splash);

		splash.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (splash != null && optin) {
					// Hopefully we have received a battery update by now.
					((MarlinApplication) getApplication()).refreshPhoneData();
					splash.setVisibility(View.GONE);
				}
				if (!optin) {
					showOptinDailog();
				}
			}
		}, 5000);
		getCallsTimes();
		String x = getMemoryDetails();
		createTabs();

		
		if(optin) {
			
			AppService.toggleService(MarlinActivity.this, true);
		}




	}

	private void createTabs() {
		Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;

		intent = new Intent().setClass(this, MyPhoneActivity.class);

		spec = tabHost.newTabSpec("myphone").setIndicator("My Phone",
				res.getDrawable(R.drawable.myphone)).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, RecentTestsActivity.class);
		spec = tabHost.newTabSpec("recenttests").setIndicator("Recent Tests",
				res.getDrawable(R.drawable.recenttests)).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, ShareActivity.class);
		spec = tabHost.newTabSpec("share").setIndicator("Share",
				res.getDrawable(R.drawable.share)).setContent(intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(0);
	}

	private void showOptinDailog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.optinHeader);

		String msgTxt = getResources().getString(R.string.optinText);
		SpannableStringBuilder ssBuilder = new SpannableStringBuilder(msgTxt);
		// ScaleXSpan span = new ScaleXSpan(1.0f);
		// ssBuilder.setSpan(span, 0, msgTxt.length(),
		// Spanned.SPAN_INCLUSIVE_INCLUSIVE);
		Linkify.addLinks(ssBuilder, Linkify.WEB_URLS);
		TextView msgTxtView = new TextView(this);
		msgTxtView.setPadding(10, 10, 10, 10);
		msgTxtView.setText(ssBuilder);
		msgTxtView.setMovementMethod(LinkMovementMethod.getInstance());

		builder.setView(msgTxtView);

		builder.setCancelable(false);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
				optin = true;
				saveSettings();
				AppService.toggleService(MarlinActivity.this, true);
				if (splash.isShown()) {
					splash.setVisibility(View.GONE);
				}
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
				optin = false;
				saveSettings();
				AppService.toggleService(MarlinActivity.this, false);
				MarlinActivity.this.finish();
			}
		});

		builder.show();
	}

	private void saveSettings() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(Constants.OPTIN, optin);
		Log.d("Marlin", getClass().getName() + ": saved optin=" + optin);
		editor.commit();
	}


	//TODO: Ricardo agregando
	public void getCallsTimes(){
		Uri allCalls = Uri.parse("content://call_log/calls");
		Cursor c = managedQuery(allCalls, null, null, null, null);
		for(String colName : c.getColumnNames())
			Log.v("marlin", "Column Name: " + colName);

		if (c.moveToFirst())
		{
			do{
				String id = c.getString(c.getColumnIndex(CallLog.Calls._ID));
				String num = c.getString(c.getColumnIndex(CallLog.Calls.NUMBER));
				int type = Integer.parseInt(c.getString(c.getColumnIndex(CallLog.Calls.TYPE)));

				String duration = c.getString(c.getColumnIndex(CallLog.Calls.DURATION));
				System.out.println("call time duration is"+duration);

				switch (type)
				{
				case 1: Log.v("marlin", id + ", " +num + ": INCOMING") ; break;
				case 2: Log.v("marlin", id + ", " +num + ": OUTGOING") ; break;
				case 3: Log.v("marlin", id + ", " +num + ": MISSED") ; break;
				}
			} while (c.moveToNext());
		}
	}
	
	public String getMemoryDetails(){
		android.app.ActivityManager.MemoryInfo mi = new android.app.ActivityManager.MemoryInfo();
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(mi);
		long availableMegs = mi.availMem / 1048576L;
		return availableMegs + "";	
		}

}