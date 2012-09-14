package com.marlin.android.app;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.marlin.android.app.service.AppService;
import com.marlin.android.sdk.DeviceDetails;

public class MarlinApplication extends Application implements ServiceListener {

	private AppService mAppService = null;
	private MyPhoneActivity phoneActivity = null;
	private DeviceDetails deviceDetails = null;

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.i("Marlin", getClass().getName() + ": onServiceConnected");
			mAppService = ((AppService.APIBinder) service).getService();
			new InitPlatformTask().execute();
		}

		public void onServiceDisconnected(ComponentName className) {
			Log.i("Marlin", getClass().getName() + ": onServiceDisconnected");
			mAppService = null;
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		bindService(new Intent(MarlinApplication.this, AppService.class),
				mConnection, Context.BIND_AUTO_CREATE);
	}

	public void setPhoneActivity(MyPhoneActivity phoneActivity) {
		this.phoneActivity = phoneActivity;
	}

	@Override
	public void platformAvailable() {
		Log.d("Marlin", getClass().getName() + ": platformAvailable");
	}

	@Override
	public void platformBusy() {
		Log.d("Marlin", getClass().getName() + ": platformBusy");
	}

	public void refreshPhoneData() {
		if (phoneActivity != null && mAppService != null) {
			DeviceDetails dd = mAppService.getDeviceDetails(false);
			Log.w("Marlin", getClass().getName() + ": refreshPhoneData dd="
					+ dd);
			if (dd != null && dd.getOperatingSystem() != null) {
				deviceDetails = dd;
			}
			phoneActivity.populateValues(deviceDetails);
		}
	}

	private class InitPlatformTask extends AsyncTask<Void, Integer, Void> {
		private final Handler mHandler = new Handler();

		protected Void doInBackground(Void... unused) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					Log.i("Marlin", getClass().getName()
							+ ": initialzePlatform");
					mAppService.initialzePlatform(false);
				}
			});
			return null;
		}

		protected void onProgressUpdate(Integer... progress) {
		}

		protected void onPostExecute(Void unused) {
			Log.i("Marlin", getClass().getName() + ": onPostExecute");
			refreshPhoneData();
		}
	}
}
