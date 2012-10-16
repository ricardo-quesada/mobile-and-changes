package com.marlin.android.app.service;

import java.io.File;
import java.io.InputStream;
import java.net.URLEncoder;
import java.sql.Time;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.marlin.android.ScriptsDefinition.ActionType;
import com.marlin.android.ScriptsDefinition.Device;
import com.marlin.android.ScriptsDefinition.IJsonObject;
import com.marlin.android.ScriptsDefinition.Layer;
import com.marlin.android.ScriptsDefinition.Measure;
import com.marlin.android.ScriptsDefinition.MeasureCategory;
import com.marlin.android.ScriptsDefinition.MeasureType;
import com.marlin.android.ScriptsDefinition.Metric;
import com.marlin.android.ScriptsDefinition.Parameter;
import com.marlin.android.ScriptsDefinition.SendableDevice;
import com.marlin.android.ScriptsDefinition.Step;
import com.marlin.android.ScriptsDefinition.Step2;
import com.marlin.android.ScriptsDefinition.Test;
import com.marlin.android.ScriptsDefinition.Test2;
import com.marlin.android.ScriptsDefinition.TestModifier;
import com.marlin.android.ScriptsDefinition.TestResult;
import com.marlin.android.ScriptsDefinition.TestResultState;
import com.marlin.android.ScriptsDefinition.TestURL;
import com.marlin.android.ScriptsDefinition.TimeFormater;
import com.marlin.android.ScriptsDefinition.URLTestResult;
import com.marlin.android.ScriptsDefinition.URLTypes;
import com.marlin.android.WebServiceInteraction.RestHelper;
import com.marlin.android.app.Constants;
import com.marlin.android.app.HiddenWebViewActivity;
import com.marlin.android.app.ServiceListener;
import com.marlin.android.sdk.Battery;
import com.marlin.android.sdk.DeviceDetails;
import com.marlin.android.sdk.Event;
import com.marlin.android.sdk.Location;
import com.marlin.android.sdk.Memory;
import com.marlin.android.sdk.OperatingSystem;
import com.marlin.android.sdk.Platform;
import com.marlin.android.sdk.ScriptResults;
import com.marlin.android.sdk.Stats;
import com.marlin.android.sdk.WebViewRunner;



public class AppService extends WakefulIntentService implements WebViewRunner {

	private ServiceListener listener = null;
	private static Thread liveThread = null;
	private static Platform pf = null;
	private static long lastRunAt = 0;
	private static long currentRunAt = 0;
	private static HashMap<String, String> webViewResult = null;
	private static Location lastKnownLocation = null;

	public class APIBinder extends Binder {
		public AppService getService() {
			return AppService.this;
		}
	}

	private final IBinder mBinder = new APIBinder();

	@Override
	public IBinder onBind(Intent intent) {
		Log.w("Marlin", getClass().getName() + "onBind:" + intent);
		return mBinder;
	}

	public AppService() {
		super("AppService");
		Log.w("Marlin", getClass().getName() + "onCreate AppService:");
	}

	public void setServiceListener(ServiceListener lsnr) {
		listener = lsnr;
	}

	public void initialzePlatform(boolean full) {
		if (pf == null) {
			Log.d("Marlin", AppService.class.getName()
					+ ": pl init ini service.");
			pf = new Platform(this);
		}
		pf.initialize(full);
	}

	public void stopPlatform() {
		if (pf != null) {
			// if too soon in the first run, the battery status does not show
			// wait for the battery info to be populated for 30 sec
			// check every 2 sec
			for (int i = 0; i < 15; i++) {
				if (pf.getBattery() != null
						&& pf.getBattery().getType() != null) {
					break;
				}
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
			}
			Log.d("Marlin", AppService.class.getName()
					+ ": pl stop in service.");
			pf.stop();
			pf = null;
		}
	}

	public boolean isRunning() {
		return liveThread != null && liveThread.isAlive();
	}

	public static void toggleService(Context context, boolean state) {
		//TODO: aqui ya acepto el optin, entonces ya aqui puedo mandar la info del cel

		subscribeNewDevice(context);
		/////

		AlarmManager mgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, OnAlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

		mgr.cancel(pi);
		Log.d("Marlin", AppService.class.getName() + ": cancel all alarms.");
		if (state) {
			mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock
					.elapsedRealtime() + 1000, Constants.PERIOD, pi);
			Log.d("Marlin", AppService.class.getName() + ": new alarm set");
		}
	}

	private static void subscribeNewDevice(Context context) {
		Device device = getDeviceToSuscribe(context);
		SendableDevice sendableDevice = new SendableDevice();
		sendableDevice.setDevice(device);

		// Just in case
		/*List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

		nameValuePairs.add(new BasicNameValuePair("id", "12345"));
		nameValuePairs.add(new BasicNameValuePair("stringdata", "Hi"));
		//instance.postData(Constants.MARLIN_SCRIPTS_URL, nameValuePairs); just in case
		 */

		IJsonObject ijsonObject = new IJsonObject();

		String jsonDevice = ijsonObject.ToGsonString(sendableDevice);

		try {
			RestHelper instance = RestHelper.getInstance();
			instance.POST(Constants.MARLIN_SCRIPTS_URL, jsonDevice);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static Device getDeviceToSuscribe(Context context) {
		DeviceDetails deviceDetails = pf.getDeviceDetails(true);
		Device device = new Device();
		device.setBattery_type(deviceDetails.getBattery().getType());
		device.setBandwidth(getLinkSpeed(context));
		device.setBattery_capacity("lookup table");
		device.setCategory("");
		device.setDevice_image("lookup table");
		device.setDevice_manufacturer(pf.getManufacturer());
		device.setDevice_name(pf.getDeviceName());
		device.setDisplay_color_depth("lookup table");
		device.setDisplay_resolution(getScreenSize(context));
		device.setDisplay_type("");
		device.setL2_cache("lookup table");
		device.setMemory_internal(deviceDetails.getMemory().getTotal());
		device.setMemory_ram("lo tengo en el activity");
		device.setMemory_removable(deviceDetails.getMemory().getTotal());
		device.setProcessor_core("lookup table");
		device.setProcessor_manufacturer("lookup table");
		device.setProcessor_name(fetch_cpu_info());
		device.setProcessor_speed("lookup table");
		device.setStandby_time("no se como");
		device.setTalk_time("esta en el activity");

		return device;
	}

	@Override
	protected void doWakefulWork(Intent intent) {
		SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME,
				0);
		lastRunAt = settings.getLong(Constants.LAST_RUN_AT, 0);
		Log.d("Marlin", getClass().getName() + ": LastRunAt=" + lastRunAt);
		if (settings.getBoolean(Constants.OPTIN, false)) {
			Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			if (now.getTimeInMillis() > lastRunAt + Constants.PERIOD) {
				Log.d("Marlin", getClass().getName()
						+ ": Woke up and doing work");
				currentRunAt = now.getTimeInMillis();
				// load last known location
				lastKnownLocation = new Location();
				lastKnownLocation.setAddress(settings.getString(
						Constants.LAST_ADDRESS, ""));
				lastKnownLocation.setCity(settings.getString(
						Constants.LAST_CITY, ""));
				lastKnownLocation.setState(settings.getString(
						Constants.LAST_STATE, ""));
				lastKnownLocation.setZip(settings.getString(Constants.LAST_ZIP,
						""));
				lastKnownLocation.setCountry(settings.getString(
						Constants.LAST_COUNTRY, ""));
				lastKnownLocation.setLatitude(settings.getString(
						Constants.LAST_LATITUDE, ""));
				lastKnownLocation.setLongitude(settings.getString(
						Constants.LAST_LONGITUDE, ""));
				Log.d("Marlin", getClass().getName() + ": loading location=" + lastKnownLocation);
				doProcess3(false);//TODO: Aqui comienza mi cambio
				SharedPreferences.Editor editor = settings.edit();
				editor.putLong(Constants.LAST_RUN_AT, currentRunAt);
				Log.d("Marlin", getClass().getName() + ": saved lastRunAt="
						+ now.getTimeInMillis());
				editor.commit();
			} else {
				Log.d("Marlin", getClass().getName()
						+ ": Woke up early - sleep again.");
			}
		} else {
			Log.d("Marlin", getClass().getName()
					+ ": Woke up and found disabled");
		}
	}

	public DeviceDetails getDeviceDetails(boolean full) {
		DeviceDetails dd = null;
		if (pf != null) {
			dd = pf.getDeviceDetails(full);
		}
		return dd;
	}

	public Memory getMemory() {
		Memory mem = null;
		if (pf != null) {
			mem = pf.getMemoryDetails();
		}
		return mem;
	}

	public Battery getBattery() {
		Battery bat = null;
		if (pf != null) {
			bat = pf.getBattery();
		}
		return bat;
	}

	public void doProcess(boolean runAll) {
		boolean startable = acquirePlatform();
		if (startable) {
			if (listener != null) {
				listener.platformBusy();
			}
			Log.d("Marlin", getClass().getName() + ": platform started");

			try {
				initialzePlatform(true);
				List<Script> scripts = getScriptsToRun(runAll);
				if (scripts != null && scripts.size() > 0) {
					// wait for the platform to be ready for 30 sec
					// check every 2 sec
					for (int i = 0; i < 15; i++) {
						Log.d("Marlin", getClass().getName()
								+ ": waiting for platform to be ready");
						if (pf.isReady()) {
							Log.d("Marlin", getClass().getName()
									+ ": platform is ready");
							break;
						}
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
						}
					}
					Log.d("Marlin", getClass().getName() + ": continuing..");
					for (Script s : scripts) {
						for (ScriptEvent se : s.getScriptEvents()) {
							Log.d("Marlin", getClass().getName()
									+ ": ScriptEvent.." + se);
							pf.processUrl(s.getScriptId(), se.getEventId(), se
									.getDescription(), se.getUrl(), se
									.getTraceRoute());
						}
					}
					pf.dump();
					addHistory();
				}

				Log.d("Marlin", getClass().getName() + ": received:"
						+ scripts.size());
			} catch (Exception e) {
				Log.d("Marlin", getClass().getName(), e);
			} finally {
				stopPlatform();
			}

			releasePlatform();
			if (listener != null) {
				listener.platformAvailable();
			}
		} else {
			Log.d("Marlin", getClass().getName()
					+ ": another thread is running the platform");
		}
	}

	private boolean acquirePlatform() {
		if (liveThread != null && liveThread.isAlive()) {
			Log.d("Marlin", getClass().getName() + ": acquire :running thread="
					+ liveThread.getId());
			// someone else is running now. quit
			return false;
		}
		liveThread = Thread.currentThread();
		Log.d("Marlin", getClass().getName() + ": saved running thread="
				+ liveThread.getId());
		return true;
	}

	private void releasePlatform() {
		Log.d("Marlin", getClass().getName() + ": release platform called");
		if (Thread.currentThread().getId() == liveThread.getId()) {
			liveThread = null;
		} else {
			Log.d("Marlin", getClass().getName() + ": current thread "
					+ Thread.currentThread().getId()
					+ " trying to release but lock is held by thread "
					+ liveThread.getId());
		}
	}

	private List<Script> getScriptsToRun(boolean runAll) {
		List<Script> allScripts = getScripts();
		if (runAll) {
			return allScripts;
		}

		List<Script> scriptsToRun = new ArrayList<Script>();
		for (Script s : allScripts) {
			boolean selected = false;
			String runAt = s.getRunAt();
			Log.v("Marlin", getClass().getName() + ": scriptId"
					+ s.getScriptId() + " runat=" + runAt);
			if (runAt != null && runAt.trim().length() > 0) {
				if ("-1".equals(runAt)) {
					selected = false;
				} else {
					try {
						String[] runHours = runAt.split(",");
						Calendar cal = Calendar.getInstance(TimeZone
								.getTimeZone("UTC"));
						for (String runHour : runHours) {
							Log.v("Marlin", getClass().getName() + ": scriptId"
									+ s.getScriptId() + " runHour=" + runHour);
							cal.set(Calendar.HOUR_OF_DAY, Integer
									.parseInt(runHour));
							cal.set(Calendar.MINUTE, 0);
							cal.set(Calendar.SECOND, 0);
							cal.set(Calendar.MILLISECOND, 0);
							long nextRun = cal.getTimeInMillis();
							Log.v("Marlin", getClass().getName() + ": nextRun="
									+ nextRun + " lastRunAt=" + lastRunAt
									+ " currentRunAt=" + currentRunAt);
							if (nextRun > lastRunAt
									&& nextRun < currentRunAt
									&& nextRun + Constants.PERIOD > currentRunAt) {
								// we have to run this time
								selected = true;
								break;
							}
						}
					} catch (Exception e) {
						Log.e("Marlin", getClass().getName(), e);
						// err on the safer side - run it.
						selected = true;
					}
				}
			} else {
				// run always
				selected = true;
			}

			Log.v("Marlin", getClass().getName() + ": selected=" + selected);
			if (selected) {
				scriptsToRun.add(s);
			}
		}

		return scriptsToRun;
	}

	private List<Script> getScripts() {
		List<Script> scriptList = new ArrayList<Script>();

		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 60000);
		HttpConnectionParams.setSoTimeout(httpParams, 60000);
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
		httpClient.getCredentialsProvider().setCredentials(
				new AuthScope(null, -1),
				new UsernamePasswordCredentials(Constants.SCRIPTS_USERNAME,
						Constants.SCRIPTS_PASSWORD));
		String scriptStr = null;
		try {
			StringBuilder scriptsUrl = new StringBuilder(Constants.SCRIPTS_URL);
			scriptsUrl.append("?deviceId=").append(
					URLEncoder.encode(pf.getEncryptedDeviceId()));
			Context ctx = getApplicationContext();
			scriptsUrl.append("&versionCode="
					+ ctx.getPackageManager().getPackageInfo(
							ctx.getPackageName(), 0).versionCode);
			if (lastKnownLocation != null) {
				scriptsUrl.append("&locAddr="
						+ URLEncoder.encode(lastKnownLocation.getAddress()));
				scriptsUrl.append("&locCity="
						+ URLEncoder.encode(lastKnownLocation.getCity()));
				scriptsUrl.append("&locState="
						+ URLEncoder.encode(lastKnownLocation.getState()));
				scriptsUrl.append("&locZip="
						+ URLEncoder.encode(lastKnownLocation.getZip()));
				scriptsUrl.append("&locCountry="
						+ URLEncoder.encode(lastKnownLocation.getCountry()));
				scriptsUrl.append("&locLat="
						+ URLEncoder.encode(lastKnownLocation.getLatitude()));
				scriptsUrl.append("&locLon="
						+ URLEncoder.encode(lastKnownLocation.getLongitude()));
			}
			scriptsUrl.append("&model="
					+ URLEncoder.encode(OperatingSystem.getInstance()
							.getModel()));

			Log.d("Marlin", getClass().getName() + ": Scripts Url="
					+ scriptsUrl.toString());
			HttpGet httpget = new HttpGet(scriptsUrl.toString());
			HttpEntity he = httpClient.execute(httpget).getEntity();
			scriptStr = EntityUtils.toString(he, "UTF-8");
		} catch (Exception e) {
			Log.e("Marlin", getClass().getName()
					+ ": error while fetching scripts ", e);
		}

		if (scriptStr != null && scriptStr.trim().length() > 0) {
			try {
				JSONArray scriptArray = new JSONArray(scriptStr);
				for (int i = 0; i < scriptArray.length(); i++) {
					JSONObject scriptObj = scriptArray.getJSONObject(i);
					Script sc = new Script();
					sc.setScriptId(scriptObj.getString("scriptId"));
					sc.setName(scriptObj.getString("name"));
					if (scriptObj.has("runAt")) {
						sc.setRunAt(scriptObj.getString("runAt"));
					}
					JSONArray eventArray = scriptObj
							.getJSONArray("scriptEvents");
					List<ScriptEvent> eventList = new ArrayList<ScriptEvent>();
					for (int j = 0; j < eventArray.length(); j++) {
						JSONObject eventObj = eventArray.getJSONObject(j);
						ScriptEvent se = new ScriptEvent();
						se.setEventId(eventObj.getString("eventId"));
						se.setDescription(eventObj.getString("description"));
						se.setUrl(eventObj.getString("url"));
						se.setTraceRoute(eventObj.getString("traceRoute"));
						eventList.add(se);
					}
					sc.setScriptEvents(eventList);
					scriptList.add(sc);
				}
			} catch (Exception e) {
				Log.e("Marlin", getClass().getName()
						+ ": error while parsing scripts ", e);
			}
		}

		return scriptList;
	}

	private void addHistory() {
		// save timestamp to history
		SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME,
				0);
		String runHistoryStr = settings.getString(Constants.RUN_HISTORY, "");
		Log.d("Marlin", getClass().getName() + ": read history="
				+ runHistoryStr);
		String[] runHistory = runHistoryStr.split(",");
		ArrayBlockingQueue<String> runHistoryQueue = new ArrayBlockingQueue<String>(
				Constants.RUN_HISTORY_MAXSIZE);
		runHistoryQueue.addAll(Arrays.asList(runHistory));
		while (runHistoryQueue.size() >= Constants.RUN_HISTORY_MAXSIZE) {
			try {
				runHistoryQueue.take();
			} catch (InterruptedException e) {
				Log.d("Marlin", getClass().getName()
						+ ": interrupted take from history", e);
			}
		}
		runHistoryQueue.add(Long.toString(Calendar.getInstance()
				.getTimeInMillis()));

		StringBuilder sb = new StringBuilder();
		for (String runTime : runHistoryQueue) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append(runTime);
		}
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(Constants.RUN_HISTORY, sb.toString());
		Log.d("Marlin", getClass().getName() + ": saved history="
				+ sb.toString());

		lastKnownLocation = pf.getLastKnownLocation();
		Log.d("Marlin", getClass().getName() + ": saved location=" + lastKnownLocation);
		if (lastKnownLocation != null) {
			editor.putString(Constants.LAST_ADDRESS, lastKnownLocation
					.getAddress());
			editor.putString(Constants.LAST_CITY, lastKnownLocation.getCity());
			editor
			.putString(Constants.LAST_STATE, lastKnownLocation
					.getState());
			editor.putString(Constants.LAST_ZIP, lastKnownLocation.getZip());
			editor.putString(Constants.LAST_COUNTRY, lastKnownLocation
					.getCountry());
			editor.putString(Constants.LAST_LATITUDE, lastKnownLocation
					.getLatitude());
			editor.putString(Constants.LAST_LONGITUDE, lastKnownLocation
					.getLongitude());
		}
		editor.commit();
	}

	public void setWebViewResult(HashMap<String, String> wvResult) {
		webViewResult = wvResult;
	}

	@Override
	public HashMap<String, String> getWebViewResults() {
		return webViewResult;
	}

	@Override
	public void loadDataWithBaseURL(String baseUrl, String data,
			String mimeType, String encoding, String failUrl) {
		webViewResult = null;
		Log.w("Marlin", getClass().getName() + ": loadWebView called");
		Intent intent = new Intent(this, HiddenWebViewActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(BASE_URL, baseUrl);
		intent.putExtra(DATA, data);
		intent.putExtra(MIME_TYPE, mimeType);
		intent.putExtra(ENCODING, encoding);
		intent.putExtra(FAIL_URL, failUrl);
		startActivity(intent);
	}



	///*********************************************************************///
	// Creating my own getScripts with json result and with RestHelper
	// seria mejor recibir una lista de test, agarrarlos uno x uno y convertirlos en Test y meterlos a lista



	/**
	 * This will make all the process, take any test and process any url on it
	 * @param runAll to determine if all the tests should run or only the once in time
	 */
	public void doProcess2(boolean runAll) {
		boolean startable = acquirePlatform();
		if (startable) {
			if (listener != null) {
				listener.platformBusy();
			}
			Log.d("Marlin", getClass().getName() + ": platform started");

			try {
				initialzePlatform(true);
				List<Test> tests = getScriptsToRun2(runAll); //Aqui tendria los test q corren.
				if (tests != null && tests.size() > 0) {
					// wait for the platform to be ready for 30 sec
					// check every 2 sec
					for (int i = 0; i < 15; i++) {
						Log.d("Marlin", getClass().getName()
								+ ": waiting for platform to be ready");
						if (pf.isReady()) {
							Log.d("Marlin", getClass().getName()
									+ ": platform is ready");
							break;
						}
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
						}
					}
					Log.d("Marlin", getClass().getName() + ": continuing..");
					for (Test test : tests) {
						for (TestURL url : test.getURLs()) {
							Log.d("Marlin", getClass().getName()
									+ ": ScriptEvent.." + url);
							pf.processUrl(test.getId()+"", url.getUrlId()+"", test.getName(), url.getURL(), url.getURL());//TODO: el ultimo getURL va a ser el traceroute. Ese debe venir en el test.
						}
					}
					Stats statsResult = pf.dump2(null);
					postDataToServer(statsResult);
					addHistory();
				}

				Log.d("Marlin", getClass().getName() + ": received:"
						+ tests.size());
			} catch (Exception e) {
				Log.d("Marlin", getClass().getName(), e);
			} finally {
				stopPlatform();
			}

			releasePlatform();
			if (listener != null) {
				listener.platformAvailable();
			}
		} else {
			Log.d("Marlin", getClass().getName()
					+ ": another thread is running the platform");
		}
	}



	/**
	 * This will make all the process, take any test and process any url on it
	 * @param runAll to determine if all the tests should run or only the once in time
	 */
	public void doProcess3(boolean runAll) {
		boolean startable = acquirePlatform();
		if (startable) {
			if (listener != null) {
				listener.platformBusy();
			}
			Log.d("Marlin", getClass().getName() + ": platform started");


			try {
				initialzePlatform(true);
				List<Test2> tests = getScripts3(); //Aqui tendria los test q corren.
				if (tests != null && tests.size() > 0) {
					// wait for the platform to be ready for 30 sec
					// check every 2 sec
					for (int i = 0; i < 15; i++) {
						Log.d("Marlin", getClass().getName()
								+ ": waiting for platform to be ready");
						if (pf.isReady()) {
							Log.d("Marlin", getClass().getName()
									+ ": platform is ready");
							break;
						}
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
						}
					}
					Log.d("Marlin", getClass().getName() + ": continuing..");
					for (Test2 test : tests) {		
						//process the base url
						pf.processUrl(test.getId()+"","0", test.getName(), test.getUrl(), null);//TODO: el ultimo getURL va a ser el traceroute. Ese debe venir en el test.
						//process the url change given in the steps, others steps or parameters are being ignored
						for (Step2 step : test.getSteps()) {
							for(Parameter param:  step.getParameters()){
								if(param.getName().equals("url")){//TODO: ver q va a mandar jose
									pf.processUrl(test.getId()+"", step.getNumber()+"", test.getName(), param.getValue(), null);
								}
							}
						}
					}
					Stats statsResult = pf.dump2(null);
					postDataToServer(statsResult);
					addHistory();
				}

				Log.d("Marlin", getClass().getName() + ": received:"
						+ tests.size());
			} catch (Exception e) {
				Log.d("Marlin", getClass().getName(), e);
			} finally {
				stopPlatform();
			}

			releasePlatform();
			if (listener != null) {
				listener.platformAvailable();
			}
		} else {
			Log.d("Marlin", getClass().getName()
					+ ": another thread is running the platform");
		}
	}

	/**
	 * Returns the scripts that should be run
	 * @param runAll to determine if all the tests should run or only the once in time
	 * @return The list of tests to run
	 */
	private List<Test> getScriptsToRun2(boolean runAll) { //este solo chequea si tienen q correr x la hora
		List<Test> allTests = getScripts2(); //aqui tendria los tests
		if (runAll) {
			return allTests;
		}

		List<Test> testToRun = new ArrayList<Test>();
		for (Test test : allTests) {
			if (test.getTestTimes() == null || test.getTestTimes().length <= 0) {
				return allTests;
			}
			else{
				try {
					Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
					for (String runTime : test.getTestTimes()) {
						//TODO: aqui tengo q revisar la verificacion de la hora, para q decida si corre o no
						Time inTime = TimeFormater.StringToTime(runTime);
						cal.set(Calendar.HOUR_OF_DAY, inTime.getHours());
						long nextRun = cal.getTimeInMillis();

						if (nextRun > lastRunAt //Chequea si tiene q correr el test
								&& nextRun < (currentRunAt + Constants.PERIOD)) {
							// we have to run this time
							testToRun.add(test);
						}
					}
				} catch (Exception e) {
					Log.e("Marlin", getClass().getName(), e);
					// err on the safer side - run it.

				}
			}
		}

		return testToRun;
	}



	/**
	 * Returns all the tests from the web service
	 * @return the list of tests
	 */
	private List<Test> getScripts2(){
		RestHelper instance = RestHelper.getInstance();
		List<Test> testList = new ArrayList<Test>();
		String jsonResult = "";
		try {
			jsonResult = instance.GET(Constants.MARLIN_SCRIPTS_URL);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (jsonResult != null && jsonResult.trim().length() > 0) {
			try {
				JSONArray testArray = new JSONArray(jsonResult);
				for (int i = 0; i < testArray.length(); i++) {
					JSONObject scriptObj = testArray.getJSONObject(i);
					Test test = CreateTest(scriptObj);
					testList.add(test);
				}
			} catch (Exception e) {
				Log.e("Marlin", getClass().getName()
						+ ": error while parsing scripts ", e);
			}
		}
		return testList;
	}


	/**
	 * Returns all the tests from the web service
	 * @return the list of tests
	 */
	private List<Test2> getScripts3(){
		RestHelper instance = RestHelper.getInstance();
		List<Test2> testList = new ArrayList<Test2>();
		String jsonResult = "";
		try {
			jsonResult = instance.GET(Constants.MARLIN_SCRIPTS_URL);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (jsonResult != null && jsonResult.trim().length() > 0) {
			try {
				JSONArray testArray = new JSONArray(jsonResult);
				for (int i = 0; i < testArray.length(); i++) {
					JSONObject scriptObj = testArray.getJSONObject(i);
					Test2 test = CreateTest2(scriptObj);
					testList.add(test);
				}
			} catch (Exception e) {
				Log.e("Marlin", getClass().getName()
						+ ": error while parsing scripts ", e);
			}
		}
		return testList;
	}

	/**
	 * Creates a test from a json piece of the downloaded tests
	 * @param scriptObj the json piece that corresponds to a test
	 * @return the Test object
	 * @throws Exception any json transformation exception
	 */
	public Test CreateTest(JSONObject scriptObj) throws Exception{
		Test test = new Test();
		try {
			//partes del tests simples
			test.setId(scriptObj.getInt("Id"));
			test.setName(scriptObj.getString("Name"));
			test.setVersion((float)scriptObj.getDouble("Version"));
			DateFormat df = DateFormat.getDateInstance();
			test.setStartDate(df.parse(scriptObj.getString("StartDate")));
			test.setEndDate(df.parse(scriptObj.getString("EndDate")));
			test.setTimeZone(scriptObj.getString("TimeZone"));

			//los modifiers
			List<TestModifier> modifiers = new ArrayList<TestModifier>();
			JSONArray modifiersArray = scriptObj.getJSONArray("Modifiers");
			for(int mod=0; mod<modifiersArray.length(); mod++){
				String enumDesc = modifiersArray.getString(mod);
				modifiers.add(TestModifier.valueOf(enumDesc));
			}
			test.setModifiers(modifiers);


			//los layers
			List<Layer> layers = new ArrayList<Layer>();
			JSONArray layersArray = scriptObj.getJSONArray("LayerSpecs");
			for(int laIndex=0; laIndex<layersArray.length(); laIndex++){
				JSONObject layerObj = layersArray.getJSONObject(laIndex);
				Layer lay = new Layer();
				lay.setLayerName(layerObj.getString("LayerName"));
				JSONObject layBody = layerObj.getJSONObject("LayerBody");
				Map<String,String> layerBody = new HashMap<String,String> ();
				for(int lbIndex=0; lbIndex<layBody.length(); lbIndex++){
					//TODO: no puedo recorrer el arreglo, ocupo saber todos los keys
				}
				lay.setLayerBody(layerBody); 
				layers.add(lay);
			}
			test.setLayerSpecs(layers);


			//verification number
			String str = scriptObj.getString("VerificationNumber");
			Gson gson = new Gson();
			byte[] verificationNumber = gson.fromJson(str, byte[].class);
			test.setVerificationNumber(verificationNumber);

			//test times
			JSONArray testTimes = scriptObj.getJSONArray("TestTimes");
			String[] times = gson.fromJson(testTimes.toString(), String[].class);
			test.setTestTimes(times);


			//los measures
			List<Measure> measures = new ArrayList<Measure>();
			JSONArray measuresArray = scriptObj.getJSONArray("Measures");
			for(int mIndex=0; mIndex<measuresArray.length(); mIndex++){
				JSONObject measureObj = measuresArray.getJSONObject(mIndex);
				Measure mes = new Measure();
				mes.setMeasureItem(measureObj.getString("MeasureItem"));
				mes.setValue(measureObj.getString("Value"));
				mes.setType(MeasureType.valueOf(measureObj.getString("Type")));
				mes.setMetric(Metric.valueOf(measureObj.getString("Metric")));
				mes.setCategory(MeasureCategory.valueOf(measureObj.getString("Category")));
				mes.setStarTime(new Date(measureObj.getString("StarTime")));
				mes.setEndTime(new Date(measureObj.getString("EndTime")));

				measures.add(mes);
			}
			test.setMeasures(measures);


			//los urls
			List<TestURL> urls = new ArrayList<TestURL>();
			JSONArray urlsArray = scriptObj.getJSONArray("URLs");
			for(int uIndex=0; uIndex<urlsArray.length(); uIndex++){
				JSONObject urlObj = urlsArray.getJSONObject(uIndex);
				TestURL url = new TestURL();
				url.setURL(urlObj.getString("URL"));
				url.setType(URLTypes.valueOf(urlObj.getString("Type")));

				//los Steps
				List<Step> steps = new ArrayList<Step>();
				JSONArray stepsArray = urlObj.getJSONArray("Steps");
				for(int sIndex=0; sIndex<stepsArray.length(); sIndex++){
					JSONObject stepObj = stepsArray.getJSONObject(sIndex);
					Step step = new Step();
					step.setExtendedSpec(stepObj.getString("ExtendedSpec"));
					step.setMethod(stepObj.getString("Method"));
					step.setNumber(stepObj.getInt("Number"));
					step.setObject(stepObj.getString("Object"));
					step.setWaitTime(stepObj.getInt("WaitTime"));
					step.setType(ActionType.valueOf(stepObj.getString("Type")));

					steps.add(step);
				}
				url.setSteps(steps);

				urls.add(url);
			}
			test.setURLs(urls);


		} catch (JSONException e) {
			e.printStackTrace();
			throw e;

		}
		return test;
	}

	/**
	 * Creates a test from a json piece of the downloaded tests
	 * @param scriptObj the json piece that corresponds to a test
	 * @return the Test object
	 * @throws Exception any json transformation exception
	 */
	public Test2 CreateTest2(JSONObject scriptObj) throws Exception{
		Test2 test = new Test2();
		try {
			//partes del tests simples
			test.setId(scriptObj.getInt("id"));
			test.setName(scriptObj.getString("name"));
			test.setVersion((float)scriptObj.getDouble("version"));
			test.setUrl(scriptObj.getString("URL"));

			//los Steps
			List<Step2> steps = new ArrayList<Step2>();
			JSONArray stepsArray = scriptObj.getJSONArray("steps");
			for(int sIndex=0; sIndex<stepsArray.length(); sIndex++){
				JSONObject stepObj = stepsArray.getJSONObject(sIndex);
				Step2 step = new Step2();
				step.setNumber(stepObj.getInt("number"));
				step.setType(ActionType.valueOf(stepObj.getString("type")));//TODO: coordinar el case de estos maes.

				List<Parameter> parameters = new ArrayList<Parameter>();
				JSONArray parametersArray = stepObj.getJSONArray("parameters");//TODO: esta mal escrito
				for(int pIndex=0; pIndex<parametersArray.length(); pIndex++){
					JSONObject parameterObj = parametersArray.getJSONObject(pIndex);
					Parameter parameter = new Parameter();
					parameter.setName(parameterObj.getString("name"));
					parameter.setValue(parameterObj.getString("value"));
					parameters.add(parameter);
				}
				step.setParameters(parameters);

				steps.add(step);
			}
			test.setSteps(steps);

		} catch (JSONException e) {
			e.printStackTrace();
			throw e;
		}
		return test;
	}


	/**
	 * Posts the result data to the webapp server
	 * @param statsResult the test results
	 */
	private void postDataToServer(Stats statsResult) {
		List<TestResult> testResult = CreateTestResult(statsResult);
		IJsonObject ijsonObject = new IJsonObject();

		String forTest = ijsonObject.ToGsonString(statsResult);

		System.out.println(forTest);
		String jsonString = ijsonObject.ToGsonString(testResult);	
		try {
			RestHelper instance = RestHelper.getInstance();
			instance.POST(Constants.MARLIN_SCRIPTS_URL, jsonString);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * Creates a TestResult object with the information stored in statsResult
	 * @param statsResult All the result test information
	 * @return the TestResult with the information to be sent
	 */
	private List<TestResult> CreateTestResult(Stats statsResult) { //esto se crea x cada 
		//aqui tengo la info del cel y el resultado de cada test dentro de scriptResults

		List<Layer> layers = CreateLayers(statsResult); //esta info es general para todos

		List<TestResult> resultTests = new ArrayList<TestResult>();
		ScriptResults[] scriptsResults = statsResult.getScriptResults(); // todos los tests

		// tengo q hacer este brete de abajo para cada test y meterlo
		for(int i = 0; i< scriptsResults.length; i++){
			ScriptResults theResult = scriptsResults[i]; //este es 1 test del mae

			TestResult testResult = new TestResult(); // hago uno mio


			testResult.setId(Integer.parseInt(theResult.getScriptId())); //TODO: este esta en el test, ver donde almacenar
			testResult.setName(""); //TODO: este esta en el test, ver donde almacenar
			testResult.setDeviceId(statsResult.getDeviceId());
			testResult.setStartTest(new Date());//TODO: ver de donde saco la hora q inicio
			testResult.setEndTest(new Date());
			testResult.setState(TestResultState.COMPLETED); //TODO: Lo tengo en resultdesc
			testResult.setRunNumber(1);

			//Setting layers
			testResult.setLayers(layers);//creados arriba, el mismo para todos

			//Setting urls results
			List<URLTestResult> urlResults = new ArrayList<URLTestResult>();

			for(Event anyUrl : theResult.getEvents()){
				URLTestResult urlResult = new URLTestResult();
				urlResult.setType(URLTypes.PAGE);
				urlResult.setURL(anyUrl.getUrl()); 
				// los resultados finales
				List<Measure> results = CreateResultsList(anyUrl);
				urlResult.setResults(results);
				urlResults.add(urlResult);
			}
			testResult.setURLResults(urlResults);
			resultTests.add(testResult);
		}



		// esta es la parte de crear 1 test result

		return resultTests;
	}

	private List<Layer> CreateLayers(Stats statsResult){
		//Setting layers
		DeviceDetails deviceDetails = statsResult.getDeviceDetails();
		List<Layer> layers = new ArrayList<Layer>();

		//OS section
		Layer osLayer = new Layer();
		osLayer.setLayerName("OS");
		Map<String,String> osBody = new HashMap<String,String>();
		osBody.put("os_name", deviceDetails.getOperatingSystem().getName());
		osBody.put("os_version", deviceDetails.getOperatingSystem().getVersion());
		osLayer.setLayerBody(osBody);
		layers.add(osLayer);

		//Battery section
		Layer batteryLayer = new Layer();
		batteryLayer.setLayerName("Battery");
		Map<String,String> batteryBody = new HashMap<String,String>();
		batteryBody.put("battery_type", deviceDetails.getBattery().getType());
		batteryBody.put("battery_level", deviceDetails.getBattery().getLevel());
		batteryBody.put("battery_status", deviceDetails.getBattery().getStatus());
		batteryBody.put("battery_health", deviceDetails.getBattery().getHealth());
		batteryBody.put("battery_plugged", deviceDetails.getBattery().getPlugged());
		batteryBody.put("device_model", "");//TODO: este no lo tengo
		batteryLayer.setLayerBody(batteryBody);
		layers.add(batteryLayer);


		//localization section
		Layer localizationLayer = new Layer();
		localizationLayer.setLayerName("localization");
		Map<String,String> localizationBody = new HashMap<String,String>();
		localizationBody.put("latitude", deviceDetails.getLocation() != null ? deviceDetails.getLocation().getLatitude() : "0");
		localizationBody.put("longitude", deviceDetails.getLocation() != null ? deviceDetails.getLocation().getLongitude() : "0");
		localizationLayer.setLayerBody(localizationBody);
		layers.add(localizationLayer);


		//timezone
		//body.put("device_timezone", ""); //TODO: sacar este

		//memory
		Layer memoryLayer = new Layer();
		memoryLayer.setLayerName("memory");
		Map<String,String> memoryBody = new HashMap<String,String>();
		memoryBody.put("memory_total", deviceDetails.getMemory().getTotal());
		memoryBody.put("memory_free", deviceDetails.getMemory().getFree());
		memoryLayer.setLayerBody(memoryBody);
		layers.add(memoryLayer);


		//network
		for(int networkIndex = 0; networkIndex<deviceDetails.getNetwork().length; networkIndex++){
			Layer networkLayer = new Layer();
			networkLayer.setLayerName("network_"+networkIndex);
			Map<String,String> networkBody = new HashMap<String,String>();
			networkBody.put("type", deviceDetails.getNetwork()[networkIndex].getType());
			networkBody.put("phone_technology", deviceDetails.getNetwork()[networkIndex].getPhoneTechnology());
			networkBody.put("data_technology", deviceDetails.getNetwork()[networkIndex].getDataTechnology());
			networkBody.put("carrier", deviceDetails.getNetwork()[networkIndex].getCarrier());
			networkBody.put("signal_strength", deviceDetails.getNetwork()[networkIndex].getSignalStrength());
			networkLayer.setLayerBody(networkBody);
			layers.add(networkLayer);
		}

		return layers;
	}


	/**
	 * @param test 
	 * @return A list of measures
	 */
	private List<Measure> CreateResultsList(Event event) {

		List<Measure> results = new ArrayList<Measure>();

		//battery consumption measure
		Measure batteryMeasure = new Measure();
		batteryMeasure.setMeasureItem("battery_consumption");
		batteryMeasure.setCategory(MeasureCategory.PERFORMANCE);
		batteryMeasure.setType(MeasureType.LOAD);
		batteryMeasure.setMetric(Metric.PERCENTAGE);
		batteryMeasure.setValue(event.getPowerConsumption());
		results.add(batteryMeasure);

		//available measure
		Measure availableMeasure = new Measure();
		availableMeasure.setMeasureItem("available");
		availableMeasure.setCategory(MeasureCategory.DEVICE);
		availableMeasure.setType(MeasureType.LOAD);
		availableMeasure.setMetric(Metric.BOOL);
		availableMeasure.setValue((Boolean.valueOf(event.getAvailability())).toString());
		results.add(availableMeasure);

		//throughput measure
		Measure throughputMeasure = new Measure();
		throughputMeasure.setMeasureItem("throughput");
		throughputMeasure.setCategory(MeasureCategory.DEVICE);
		throughputMeasure.setType(MeasureType.LOAD);
		throughputMeasure.setMetric(Metric.PERCENTAGE);
		throughputMeasure.setValue(event.getThroughput());
		results.add(throughputMeasure);

		//redirect count measure
		Measure redirectMeasure = new Measure();
		redirectMeasure.setMeasureItem("redirect_count");
		redirectMeasure.setCategory(MeasureCategory.PERFORMANCE);
		redirectMeasure.setType(MeasureType.LOAD);
		//redirectMeasure.setMetric(Metric.BPS);
		redirectMeasure.setValue(Integer.toString(event.getRedirectCount()));
		results.add(redirectMeasure);

		//result code measure
		Measure resultMeasure = new Measure();
		resultMeasure.setMeasureItem("result_code");
		resultMeasure.setCategory(MeasureCategory.PERFORMANCE);
		resultMeasure.setType(MeasureType.LOAD);
		//redirectMeasure.setMetric(Metric.BPS);
		resultMeasure.setValue(Integer.toString(event.getResultCode()));
		results.add(resultMeasure);

		//signal strength measure
		Measure signalMeasure = new Measure();
		signalMeasure.setMeasureItem("signal_strength");
		signalMeasure.setCategory(MeasureCategory.DEVICE);
		signalMeasure.setType(MeasureType.CARRIER);
		redirectMeasure.setMetric(Metric.PERCENTAGE);
		signalMeasure.setValue(event.getSignalStrength());
		results.add(signalMeasure);

		//bytes download measure
		Measure bytesMeasure = new Measure();
		bytesMeasure.setMeasureItem("bytes_download");
		bytesMeasure.setCategory(MeasureCategory.PERFORMANCE);
		bytesMeasure.setType(MeasureType.LOAD);
		//redirectMeasure.setMetric(Metric.BYTES);
		bytesMeasure.setValue(event.getConnection().getBytesDownloaded());
		results.add(bytesMeasure);

		//content type measure
		Measure contentMeasure = new Measure();
		contentMeasure.setMeasureItem("content_type");
		contentMeasure.setCategory(MeasureCategory.PERFORMANCE);
		contentMeasure.setType(MeasureType.LOAD);
		//redirectMeasure.setMetric(Metric.BYTES);
		contentMeasure.setValue(event.getConnection().getContentType());
		results.add(contentMeasure);


		//List<PageElement> x= event.getPageElements();
		//for(PageElement y : x){
		// TODO: x aqui voy: en event tengo mucha info de los elementos de la pagina, 
		//ver como meter aqui
		//	}

		Measure elementsMeasure = new Measure();
		elementsMeasure.setMeasureItem("content_type");
		elementsMeasure.setCategory(MeasureCategory.PERFORMANCE);
		elementsMeasure.setType(MeasureType.LOAD);
		//redirectMeasure.setMetric(Metric.BYTES);
		elementsMeasure.setValue("");//TODO: tomar decision aqui si agregar nada mas la lista de elementos (x) o hacer un measure con cada uno y ver como es la mejor forma de manejar estos measures

		results.add(elementsMeasure);

		return results;
	}



	// TODO: cpu information
	//Some processor info
	public static String run(String[] cmd, String workdirectory) throws Exception {
		String result = "";

		try {
			ProcessBuilder builder = new ProcessBuilder(cmd);
			// set working directory
			if (workdirectory != null)
				builder.directory(new File(workdirectory));
			builder.redirectErrorStream(true);
			Process process = builder.start();
			InputStream in = process.getInputStream();
			byte[] re = new byte[1024];
			while (in.read(re) != -1) {
				System.out.println(new String(re));
				result = result + new String(re);
			}
			in.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}



	// cpu info
	public static  String fetch_cpu_info() {
		String result = null;

		try {
			String[] args = { "/system/bin/cat", "/proc/cpuinfo" };
			result = run(args, "/system/bin/");
			Log.i("result", "result=" + result);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}


	//Screen size measures
	public static String getScreenSize(Context context){
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		int width = display.getWidth();  // deprecated
		int height = display.getHeight();  // deprecated
		return width+"x"+height;
	}

	public static String getLinkSpeed(Context context){
		//TODO: aqui agregue para sacar el bandwidth pero solo con wifi
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int linkSpeed = 0;
		if (wifiInfo != null) {
			linkSpeed = wifiInfo.getLinkSpeed(); //measured using WifiInfo.LINK_SPEED_UNITS
			Log.d("marlin", linkSpeed+"");
		}
		return linkSpeed + "";
	}




	//test string
	//"[{\"Id\":1, \"Name\":\"Name of the test\",\"Version\":1.0,\"URLs\":[{\"URL\":\"http://www.testhost.com/site\",\"Type\":\"PAGE\",\"Steps\":[{\"Number\":1,\"Type\":\"NAVIGATE\",\"waitTime\":1000,\"Object\":\"TextArea\",\"Method\":\"onclick\",\"ExtendedSpec\":\"ExtendedSpec\"},{\"Number\":1,\"Type\":\"CLICK\",\"waitTime\":2000,\"Object\":\"Button\",\"Method\":\"onChange\",\"ExtendedSpec\":\"ExtendedSpec\"}]}],\"Modifiers\":[\"RUN_ONE_TIME\",\"RUN_ONE_TIME\"],\"LayerSpecs\":[{\"LayerName\":\"Pop\"},{\"LayerName\":\"Pop\"}],\"VerificationNumber\":[10,12,14,16,18],\"Measures\":[{\"Type\":\"CARRIER\",\"MeasureItem\":\"AT\u0026T\",\"Value\":\"\"},{\"Type\":\"CARRIER\",\"MeasureItem\":\"Sprint\",\"Value\":\"thevalue\"}],\"StartDate\":\"Sep 14, 2012 7:34:28 AM\",\"EndDate\":\"Sep 14, 2012 7:34:28 AM\",\"TestTimes\":[\"06:00:10 PM\",\"06:00:20 PM\"],\"TimeZone\":\"UTC\"}]";

	/*{

 Ê Ê"id": 2,
 Ê Ê"name": "Google Response",
 Ê Ê"version": "1.0",
 Ê Ê"URL": "www.google.com",
 Ê Ê"steps": [
 Ê Ê Ê Ê{
 Ê Ê Ê Ê Ê Ê"number": 1,
 Ê Ê Ê Ê Ê Ê"type": "navigate",
 Ê Ê Ê Ê Ê Ê"paramethers": [
 Ê Ê Ê Ê Ê Ê Ê Ê{
 Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê"name": "waittime",
 Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê"value": "1000"
 Ê Ê Ê Ê Ê Ê Ê Ê},
 Ê Ê Ê Ê Ê Ê Ê Ê{
 Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê"name": "object",
 Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê"value": "textarea"
 Ê Ê Ê Ê Ê Ê Ê Ê}
 Ê Ê Ê Ê Ê Ê]
 Ê Ê Ê Ê}
 Ê Ê]

}
	 * */
}




