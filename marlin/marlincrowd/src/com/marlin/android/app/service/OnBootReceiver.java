package com.marlin.android.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.marlin.android.app.Constants;

public class OnBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences settings = context.getSharedPreferences(
				Constants.PREFS_NAME, 0);
		boolean state = settings.getBoolean(Constants.OPTIN, false);
		Log.d("Marlin", getClass().getName() + ": On Boot state=" + state);
		AppService.toggleService(context, state);
	}
}
