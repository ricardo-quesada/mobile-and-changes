package com.marlin.android.app.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.Date;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;


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
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.google.gson.Gson;
import com.marlin.android.app.Constants;
import com.marlin.android.app.ServiceListener;
import com.marlin.android.sdk.Battery;
import com.marlin.android.sdk.DeviceDetails;
import com.marlin.android.sdk.Memory;
import com.marlin.android.sdk.Platform;
import com.marlin.android.sdk.ScriptResults;
import com.marlin.android.sdk.Stats;

import com.marlin.android.WebServiceInteraction.RestHelper;
import com.marlin.android.ScriptsDefinition.*;


public class AppService extends WakefulIntentService {

	private ServiceListener listener = null;
	private static Thread liveThread = null;
	private static Platform pf = null;
	private static long lastRunAt = 0;
	private static long currentRunAt = 0;

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
				doProcess(false);
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
				List<Script> scripts = getScriptsToRun(runAll); //Aqui tendria los test q corren.
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
									.getDescription(), se.getUrl());
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

	private List<Script> getScriptsToRun(boolean runAll) { //este solo chequea si tienen q correr x la hora
		List<Script> allScripts = getScripts(); //aqui tendria un test
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
						String[] runHours = runAt.split(","); //este es el testtimes
						Calendar cal = Calendar.getInstance(TimeZone
								.getTimeZone("UTC"));
						for (String runHour : runHours) {
							Log.v("Marlin", getClass().getName() + ": scriptId"
									+ s.getScriptId() + " runHour=" + runHour);
							cal.set(Calendar.HOUR_OF_DAY, Integer
									.parseInt(runHour));
							long nextRun = cal.getTimeInMillis();
							Log.v("Marlin", getClass().getName() + ": nextRun="
									+ nextRun + " lastRunAt=" + lastRunAt
									+ " currentRunAt=" + currentRunAt);
							if (nextRun > lastRunAt //Chequea si tiene q correr el test
									&& nextRun < (currentRunAt + Constants.PERIOD)) {
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
			HttpGet httpget = new HttpGet(Constants.SCRIPTS_URL);
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
		editor.commit();
	}


	///*********************************************************************///
	// Creating my own getScripts with json result and with RestHelper
	// seria mejor recibir una lista de test, agarrarlos uno x uno y convertirlos en Test y meterlos a lista


	
	
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
							pf.processUrl(test.getId()+"", test.getId()+"", test.getName(), url.getURL());
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
	
	private List<Test> getScripts2(){
		RestHelper instance = RestHelper.getInstance();
		List<Test> testList = new ArrayList<Test>();
		String jsonResult = "";
		try {
			jsonResult = instance.GET(Constants.MARLIN_SCRIPTS_URL);
		} catch (Exception ex) {
			// TODO Auto-generated catch block
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
					JSONObject stepObj = stepsArray.getJSONObject(uIndex);
					Step step = new Step();
					step.setExtendedSpec(stepObj.getString("ExtendedSpec"));
					step.setMethod(stepObj.getString("Method"));
					step.setNumber(stepObj.getInt("Number"));
					step.setObject(stepObj.getString("Object"));
					step.setWaitTime(stepObj.getInt("waitTime"));
					step.setType(ActionType.valueOf(stepObj.getString("Type")));

					steps.add(step);
				}
				url.setSteps((Step[]) steps.toArray());

				urls.add(url);
			}
			test.setURLs(urls);


		} catch (JSONException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			throw e;

		}
		return test;
	}

	

	private void postDataToServer(Stats statsResult) {
		// TODO Auto-generated method stub
		TestResult testResult = CreateTestResult(statsResult);
		String jsonString = IJsonObject.ToJsonObject(testResult);	
		try {
			RestHelper instance = RestHelper.getInstance();
			instance.PUT(Constants.MARLIN_SCRIPTS_URL, jsonString);
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		
	}

	private TestResult CreateTestResult(Stats statsResult) {

		TestResult testResult = new TestResult();
		testResult.setId(2); //TODO: este esta en el test, ver donde almacenar
		testResult.setName(""); //TODO: este esta en el test, ver donde almacenar
		testResult.setDeviceId(statsResult.getDeviceId());
		testResult.setStartTest(new Date());//TODO: ver de donde saco la hora q inicio
		testResult.setEndTest(new Date());
		testResult.setState(TestResultState.COMPLETED);
		testResult.setRunNumber(1);
		
		
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
		localizationBody.put("latitude", deviceDetails.getLocation().getLatitude());
		localizationBody.put("longitude", deviceDetails.getLocation().getLongitude());
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
		layers.add(memoryLayer);


		//network
		for(int networkIndex = 0; networkIndex<deviceDetails.getNetwork().length; networkIndex++){
			Layer networkLayer = new Layer();
			networkLayer.setLayerName("network"+networkIndex);
			Map<String,String> networkBody = new HashMap<String,String>();
			networkBody.put("type", deviceDetails.getNetwork()[networkIndex].getType());
			networkBody.put("phone_technology", deviceDetails.getNetwork()[networkIndex].getPhoneTechnology());
			networkBody.put("data_technology", deviceDetails.getNetwork()[networkIndex].getDataTechnology());
			networkBody.put("carrier", deviceDetails.getNetwork()[networkIndex].getCarrier());
			networkBody.put("signal_strength", deviceDetails.getNetwork()[networkIndex].getSignalStrength());
			layers.add(networkLayer);
		}
		testResult.setLayers(layers);//aqui meto todos los statsResult.getDeviceDetails();
		
		//Setting urls results
		for(ScriptResults test: statsResult.getScriptResults()){
			test.getScriptId(); //este seria el id q tengo q ponerle a cada url cuando le hago processURL
			test.getEvents(); //estos events es lo q me devuelve PlatformUrlProcessor.java.processUrl -> debo analizar muy bn q trae ese mae
//TODO: x aqui voy
		}
		
		
		return testResult;
	}


	//test string
    //"[{\"Id\":1, \"Name\":\"Name of the test\",\"Version\":1.0,\"URLs\":[{\"URL\":\"http://www.testhost.com/site\",\"Type\":\"PAGE\",\"Steps\":[{\"Number\":1,\"Type\":\"NAVIGATE\",\"waitTime\":1000,\"Object\":\"TextArea\",\"Method\":\"onclick\",\"ExtendedSpec\":\"ExtendedSpec\"},{\"Number\":1,\"Type\":\"CLICK\",\"waitTime\":2000,\"Object\":\"Button\",\"Method\":\"onChange\",\"ExtendedSpec\":\"ExtendedSpec\"}]}],\"Modifiers\":[\"RUN_ONE_TIME\",\"RUN_ONE_TIME\"],\"LayerSpecs\":[{\"LayerName\":\"Pop\"},{\"LayerName\":\"Pop\"}],\"VerificationNumber\":[10,12,14,16,18],\"Measures\":[{\"Type\":\"CARRIER\",\"MeasureItem\":\"AT\u0026T\",\"Value\":\"\"},{\"Type\":\"CARRIER\",\"MeasureItem\":\"Sprint\",\"Value\":\"thevalue\"}],\"StartDate\":\"Sep 14, 2012 7:34:28 AM\",\"EndDate\":\"Sep 14, 2012 7:34:28 AM\",\"TestTimes\":[\"06:00:10 PM\",\"06:00:20 PM\"],\"TimeZone\":\"UTC\"}]";

}
