package com.marlin.android.sdk;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.webkit.WebView;

import com.google.gson.Gson;

public class Platform {

	public static final String DATA_POST_URL = "http://www.marlinmobile.com:8080/marlinserver/AndroidStatsReceiver";
	// public static final String DATA_POST_URL =
	// "http://192.168.2.100:8080/marlinserver/AndroidStatsReceiver";
	// public static final String DATA_POST_URL =
	// "http://192.168.1.4:8080/marlinserver/AndroidStatsReceiver";
	public static final String HEADER_MIRROR_URL = "http://www.marlinmobile.com:8080/marlinserver/HeaderMirror";

	private Stats stats;
	private Context context;
	private LocationManager locManager;
	private LocationListener locListener;
	private TelephonyManager telManager;
	private ConnectivityManager connManager;
	private android.location.Location andLocation;
	private Battery battery;
	private int signalStrength;

	private BroadcastReceiver battReceiver;
	private PhoneStateListener listener;

	private HashMap<String, ScriptResults> scriptResults;

	private CookieManager cookieManager = null;
	private HashMap<String, String> headers = null;
	private boolean step1Initialized = false;
	private boolean step2Initialized = false;

	public Platform(Context ctx) {
		context = ctx;
		cookieManager = new CookieManager();
		headers = new HashMap<String, String>();
	}

	public boolean isReady() {
		if (battery == null || battery.getType() == null) {
			return false;
		}
		return true;
	}

	public void initialize(boolean full) {
		if (!step1Initialized) {
			initializeStep1();
		}
		if (full && !step2Initialized) {
			initializeStep2();
		}
	}

	private void initializeStep1() {
		step1Initialized = true;
		Log.i("Marlin", "step1 initializing");
		battery = new Battery();
		new BatteryMonitorWorkerThread().start();

		telManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		new SignalStrengthWorkerThread().start();

		connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		Log.i("Marlin", "step1 initialized");
	}

	private void initializeStep2() {
		step2Initialized = true;
		Log.i("Marlin", "step2 initializing");
		scriptResults = new HashMap<String, ScriptResults>();

		locListener = new LocationListener() {

			@Override
			public void onLocationChanged(android.location.Location loc) {
				Log.d("Marlin", "onLocationChanged" + loc);
				if (andLocation == null
						|| loc.getAccuracy() < andLocation.getAccuracy()) {
					andLocation = loc;
				}
			}

			@Override
			public void onProviderDisabled(String arg0) {
				Log.i("Marlin", "provider disabled - " + arg0);
				// TODO Auto-generated method stub

			}

			@Override
			public void onProviderEnabled(String arg0) {
				Log.i("Marlin", "provider enabled - " + arg0);
				// TODO Auto-generated method stub

			}

			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
				Log.i("Marlin", "status changed - " + arg0 + "," + arg1 + ","
						+ arg2);
				// TODO Auto-generated method stub

			}
		};

		new LocationManagerWorkerThread().start();

		try {
			getHeaders();
			Log.d("Marlin", "Get Headers returned:" + headers);
		} catch (MalformedURLException e) {
			Log.d("Marlin", "Get Headers Failed", e);
		}
		Log.i("Marlin", "step2 initialized");
	}

	public Battery getBattery() {
		return battery;
	}

	public CookieManager getCookieManager() {
		return cookieManager;
	}

	public void clearResults() {
		scriptResults.clear();
	}

	public void pause() {

	}

	public void resume() {

	}

	public void stop() {
		if (locManager != null)
			locManager.removeUpdates(locListener);
		if (telManager != null)
			telManager.listen(listener, PhoneStateListener.LISTEN_NONE);
		if (battReceiver != null) {
			try {
				context.unregisterReceiver(battReceiver);
			} catch (Exception e) {
				Log.d("Marlin", "unregisterReceiver failed", e);
			}
		}
	}

	public void dump() {
		dump(null);
	}

	public void dump(String scriptId) {
		stats = new Stats(encrypt(telManager.getDeviceId()));
		stats.setDeviceDetails(getDeviceDetails(true));
		if (scriptId != null && scriptId.trim().length() > 0) {
			stats.setScriptResults(new ScriptResults[] { scriptResults
					.get(scriptId) });
		} else {
			stats.setScriptResults((ScriptResults[]) scriptResults.values()
					.toArray(new ScriptResults[scriptResults.size()]));
		}

		Gson gson = new Gson();
		String data = gson.toJson(stats);
		Log.d("Marlin", "Stats:" + data);
		postData(data);
	}

	private void getHeaders() throws MalformedURLException {
		WebView webView = new WebView(context);
		URL headerMirrorUrl = new URL(HEADER_MIRROR_URL + "?deviceId="
				+ encrypt(telManager.getDeviceId()));

		webView.loadUrl(headerMirrorUrl.toString());

		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 60000);
		HttpConnectionParams.setSoTimeout(httpParams, 60000);
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
		String respStr = null;
		try {
			HttpGet httpget = new HttpGet(headerMirrorUrl.toString()
					+ "&op=get");
			HttpEntity he = httpClient.execute(httpget).getEntity();
			respStr = EntityUtils.toString(he, "UTF-8");
		} catch (Exception e) {
			Log.e("Marlin", getClass().getName()
					+ ": error while fetching headers ", e);
		}

		if (respStr != null && respStr.trim().length() > 0) {
			try {
				JSONObject obj = new JSONObject(respStr);
				JSONArray headerNames = obj.names();
				headers.clear();
				for (int i = 0; i < headerNames.length(); i++) {
					String name = headerNames.getString(i);
					if ("cookie".equalsIgnoreCase(name)
							|| "host".equalsIgnoreCase(name)) {
						continue;
					}
					headers.put(name, obj.getString(name));
				}

				// Hard code for debugging - need to remove
				headers
						.put("x-wap-profile",
								"http://www.htcmms.com.tw/Android/Common/Liberty/A6366-1.0.xml");

			} catch (Exception e) {
				Log.e("Marlin", getClass().getName()
						+ ": error while fetching headers ", e);
			}
		}
	}

	public DeviceDetails getDeviceDetails(boolean full) {
		DeviceDetails dd = new DeviceDetails();
		dd.setOperatingSystem(OperatingSystem.getInstance());
		dd.setMemory(getMemoryDetails());
		if (full && step2Initialized) {
			// try best to get a location fix.
			// we wait for 1 min by checking every 10 sec
			for (int i = 0; i < 12; i++) {
				Location loc = getLocation();
				if (loc.getLatitude() == null
						|| loc.getLatitude().trim().length() == 0
						|| loc.getLongitude() == null
						|| loc.getLongitude().trim().length() == 0) {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
					}
				} else {
					dd.setLocation(getLocation());
					break;
				}
			}
		}
		// try best to get a signal strength.
		// we wait for 20sec by checking every 2 sec
		for (int i = 0; i < 10; i++) {
			if (signalStrength == 0) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
			} else {
				break;
			}
		}
		// try best to get battery info.
		// we wait for 5sec by checking every 1 sec
		for (int i = 0; i < 5; i++) {
			if (battery == null || battery.getType() == null) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			} else {
				break;
			}
		}
		dd.setBattery(battery);
		dd.setNetwork(getNetworks());
		return dd;
	}

	private Network[] getNetworks() {
		NetworkInfo[] mobileNet = connManager.getAllNetworkInfo();
		Network[] netList = new Network[mobileNet.length];
		for (int i = 0; i < mobileNet.length; i++) {
			Network net = new Network();
			net.setType(mobileNet[i].getTypeName());
			net.setAvailable(mobileNet[i].isAvailable());
			net.setDataTechnology(mobileNet[i].getSubtypeName());
			net.setRoaming(mobileNet[i].isRoaming());
			if (ConnectivityManager.TYPE_MOBILE == mobileNet[i].getType()) {
				net.setSignalStrength(Integer.toString(signalStrength));
				net.setCarrier(telManager.getNetworkOperatorName());
				net.setPhoneNumber(encrypt(telManager.getLine1Number()));
				switch (telManager.getPhoneType()) {
				case TelephonyManager.PHONE_TYPE_CDMA:
					net.setPhoneTechnology("CDMA");
					break;
				case TelephonyManager.PHONE_TYPE_GSM:
					net.setPhoneTechnology("GSM");
					break;
				default:
					break;
				}

				switch (telManager.getNetworkType()) {
				case TelephonyManager.NETWORK_TYPE_1xRTT:
					net.setDataTechnology("1xRTT");
					break;
				case TelephonyManager.NETWORK_TYPE_CDMA:
					net.setDataTechnology("CDMA");
					break;
				case TelephonyManager.NETWORK_TYPE_EDGE:
					net.setDataTechnology("EDGE");
					break;
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
					net.setDataTechnology("EVDO_0");
					break;
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
					net.setDataTechnology("EVDO_A");
					break;
				case TelephonyManager.NETWORK_TYPE_GPRS:
					net.setDataTechnology("GPRS");
					break;
				case TelephonyManager.NETWORK_TYPE_HSDPA:
					net.setDataTechnology("HSDPA");
					break;
				case TelephonyManager.NETWORK_TYPE_HSPA:
					net.setDataTechnology("HSPA");
					break;
				case TelephonyManager.NETWORK_TYPE_HSUPA:
					net.setDataTechnology("HSUPA");
					break;
				case TelephonyManager.NETWORK_TYPE_UMTS:
					net.setDataTechnology("UMTS");
					break;
				default:
					break;
				}
			}
			netList[i] = net;
		}

		return netList;
	}

	public Memory getMemoryDetails() {
		Memory memory = new Memory();
		memory.setTotal(Long.toString(Debug.getNativeHeapSize()));
		memory.setFree(Long.toString(Debug.getNativeHeapFreeSize()));
		return memory;
	}

	private Location getLocation() {
		Location location = new Location();
		if (andLocation != null) {
			location.setLatitude(Double.toString(andLocation.getLatitude()));
			location.setLongitude(Double.toString(andLocation.getLongitude()));
			Geocoder gc = new Geocoder(context);
			try {
				List<Address> addresses = gc.getFromLocation(andLocation
						.getLatitude(), andLocation.getLongitude(), 1);
				// List<Address> addresses = gc.getFromLocation(42.2741295,
				// -71.395234, 1);
				if (addresses != null && addresses.size() > 0) {
					Address addr = addresses.get(0);
					location.setAddress(addr.getAddressLine(0));
					location.setCity(addr.getLocality());
					location.setState(addr.getAdminArea());
					location.setZip(addr.getPostalCode());
					location.setCountry(addr.getCountryName());
				}
			} catch (IOException e) {
				Log.e("Marlin", "Exception while getting address:", e);
			}

		}
		return location;
	}

	public void processUrl(String scriptId, String eventId,
			String eventDescription, String url) {
		processUrl(null, scriptId, eventId, eventDescription, url);
	}

	public void processUrl(WebView wv, String scriptId, String eventId,
			String eventDescription, String url) {
		ScriptResults sr = scriptResults.get(scriptId);
		if (sr == null) {
			sr = new ScriptResults();
			sr.setScriptId(scriptId);
			scriptResults.put(scriptId, sr);
		}
		PlatformUrlProcessor pup = new PlatformUrlProcessor();
		int battLevel1 = 0;
		int battLevel2 = 0;
		try {
			battLevel1 = Integer.parseInt(battery.getLevel());
		} catch (NumberFormatException e) {
			// nothing to do
		}
		if (wv == null) {
			wv = new WebView(context);
		}
		List<Event> events = pup.processUrl(wv, cookieManager, headers, url);
		try {
			battLevel2 = Integer.parseInt(battery.getLevel());
		} catch (NumberFormatException e) {
			// nothing to do
		}
		for (Event event : events) {
			event.setEventId(eventId);
			event.setDescription(eventDescription);
			event.setSignalStrength(Integer.toString(signalStrength));
			// set consumption only if we have an initial reading
			if (battLevel1 > 0 && battLevel2 < battLevel1) {
				event.setPowerConsumption(Integer.toString(battLevel2
						- battLevel1));
			}
		}
		List<Event> eventList = new ArrayList<Event>();
		if (sr.getEvents() != null) {
			eventList.addAll(Arrays.asList(sr.getEvents()));
		}
		eventList.addAll(events);
		sr.setEvents((Event[]) eventList.toArray(new Event[eventList.size()]));
		
		//Ricardo agregando para prueba:
		

	}

	private void postData(String data) {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getCredentialsProvider().setCredentials(
				new AuthScope(null, -1),
				new UsernamePasswordCredentials("marlin", "m0b1l3"));
		HttpPost httppost = new HttpPost(DATA_POST_URL);

		try {
			httppost.setEntity(new StringEntity(data));
			httpclient.execute(httppost);
		} catch (Exception e) {
			Log.e("Marlin", "Exception while posting data:", e);
		}
	}

	private class BatteryMonitorWorkerThread extends Thread {
		public BatteryMonitorWorkerThread() {
			super("BatteryMonitorWorkerThread");
		}

		public void run() {
			Looper.prepare();
			battReceiver = new BroadcastReceiver() {
				public void onReceive(Context context, Intent intent) {
					Log.d("Marlin", "BroadcastReceiver onReceive");
					String technology = intent
							.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
					int rawlevel = intent.getIntExtra(
							BatteryManager.EXTRA_LEVEL, -1);
					int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE,
							-1);
					int status = intent.getIntExtra(
							BatteryManager.EXTRA_STATUS, -1);
					int health = intent.getIntExtra(
							BatteryManager.EXTRA_HEALTH, -1);
					int plugged = intent.getIntExtra(
							BatteryManager.EXTRA_PLUGGED, -1);
					int level = -1; // percentage, or -1 for unknown
					if (rawlevel >= 0 && scale > 0) {
						level = (rawlevel * 100) / scale;
					}
					battery.setType(technology);
					battery.setStatus(Integer.toString(status));
					battery.setLevel(Integer.toString(level));
					battery.setHealth(Integer.toString(health));
					battery.setPlugged(Integer.toString(plugged));
				}
			};
			IntentFilter battFilter = new IntentFilter(
					Intent.ACTION_BATTERY_CHANGED);
			context.registerReceiver(battReceiver, battFilter);
			Looper.loop();
		}
	}

	private class LocationManagerWorkerThread extends Thread {
		public LocationManagerWorkerThread() {
			super("LocationManagerWorkerThread");
		}

		public void run() {
			Looper.prepare();
			locManager = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);
			if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				Log.i("Marlin", "enabled gps provider - listen");
				locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
						30000, 100, locListener);
			}
			if (locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				Log.i("Marlin", "enabled network provider - listen");
				locManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 30000, 100,
						locListener);
			}
			Looper.loop();
		}
	}

	private class SignalStrengthWorkerThread extends Thread {
		public SignalStrengthWorkerThread() {
			super("SignalStrengthWorkerThread");
		}

		public void run() {
			Looper.prepare();
			boolean newClassAvailable = false;
			try {
				Class.forName("android.telephony.SignalStrength");
				newClassAvailable = true;
			} catch (Exception ex) {
				Log.d("Marlin", "Using old signalStrength");
			}

			// Create a new PhoneStateListener
			if (newClassAvailable) {
				listener = new PhoneStateListener() {
					public void onSignalStrengthsChanged(SignalStrength ss) {
						Log.d("Marlin", "signalStrengthChanged via new class");
						if (ss.isGsm()) {
							signalStrength = ss.getGsmSignalStrength();
						} else {
							signalStrength = ss.getCdmaDbm();
						}
					}
				};
				telManager.listen(listener,
						PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
			} else {
				listener = new PhoneStateListener() {
					public void onSignalStrengthChanged(int strength) {
						Log.d("Marlin", "signalStrengthChanged via old class");
						signalStrength = strength;
					}
				};
				telManager.listen(listener,
						PhoneStateListener.LISTEN_SIGNAL_STRENGTH);
			}
			Looper.loop();
		}
	}

	private String encrypt(String str) {
		String encrypted = str;
		try {
			Base64 b64 = Base64.getInstance();
			encrypted = new String(b64.encode(str.getBytes()));
		} catch (Exception e) {
			// Do nothing - let it go as is for now
		}
		return encrypted;
	}
}
