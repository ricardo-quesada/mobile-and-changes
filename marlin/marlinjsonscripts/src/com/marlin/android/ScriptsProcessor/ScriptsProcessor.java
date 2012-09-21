package com.marlin.android.ScriptsProcessor;

import java.sql.Time;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.gson.Gson;
import com.marlin.android.ScriptsDefinition.ActionType;
import com.marlin.android.ScriptsDefinition.Layer;
import com.marlin.android.ScriptsDefinition.Measure;
import com.marlin.android.ScriptsDefinition.MeasureType;
import com.marlin.android.ScriptsDefinition.Metric;
import com.marlin.android.ScriptsDefinition.Step;
import com.marlin.android.ScriptsDefinition.Test;
import com.marlin.android.ScriptsDefinition.TestModifier;
import com.marlin.android.ScriptsDefinition.TestURL;
import com.marlin.android.ScriptsDefinition.TimeFormater;
import com.marlin.android.ScriptsDefinition.URLTypes;
import com.marlin.android.WebServiceInteraction.RestHelper;
//import com.marlin.android.app.service.AppService;
import com.marlin.android.sdk.Platform;
import com.marlin.android.sdk.ScriptResults;
import com.marlin.android.sdk.Stats;

import com.marlin.android.ScriptsProcessor.Constants;

public class ScriptsProcessor {

	private static Platform pf = null;

	private static Thread liveThread = null;
	private static long lastRunAt = 0;
	private static long currentRunAt = 0;

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
			test.setId(scriptObj.getInt("Id"));
			test.setName(scriptObj.getString("Name"));
			test.setVersion((float)scriptObj.getDouble("Version"));
			DateFormat df = DateFormat.getDateInstance();
			test.setStartDate(df.parse(scriptObj.getString("StartDate")));
			test.setEndDate(df.parse(scriptObj.getString("EndDate")));
			test.setTimeZone(scriptObj.getString("TimeZone"));

			List<TestModifier> modifiers = new ArrayList<TestModifier>();
			JSONArray modifiersArray = scriptObj.getJSONArray("Modifiers");
			for(int mod=0; mod<modifiersArray.length(); mod++){
				String enumDesc = modifiersArray.getString(mod);
				modifiers.add(TestModifier.valueOf(enumDesc));
			}
			test.setModifiers(modifiers);


			List<Layer> layers = new ArrayList<Layer>();
			JSONArray layersArray = scriptObj.getJSONArray("LayerSpecs");
			for(int laIndex=0; laIndex<layersArray.length(); laIndex++){
				JSONObject layerObj = layersArray.getJSONObject(laIndex);
				Layer lay = new Layer();
				lay.setLayerName(layerObj.getString("LayerName"));
				JSONObject layBody = layerObj.getJSONObject("LayerBody");
				//lay.setLayerBody(layBody); //ver q hacer con dictionary
				layers.add(lay);
			}
			test.setLayerSpecs(layers);


			String str = scriptObj.getString("VerificationNumber");
			Gson gson = new Gson();
			byte[] parsed = gson.fromJson(str, byte[].class);
			test.setVerificationNumber(parsed);

			JSONArray testTimes = scriptObj.getJSONArray("TestTimes");
			String[] times = gson.fromJson(testTimes.toString(), String[].class);
			test.setTestTimes(times);

			List<Measure> measures = new ArrayList<Measure>();
			JSONArray measuresArray = scriptObj.getJSONArray("Measures");
			for(int mIndex=0; mIndex<layersArray.length(); mIndex++){
				JSONObject measureObj = layersArray.getJSONObject(mIndex);
				Measure mes = new Measure();
				mes.setMeasureItem(measureObj.getString("MeasureItem"));
				mes.setValue(measureObj.getString("Value"));
				mes.setType(MeasureType.valueOf(measureObj.getString("Type")));
				mes.setMetric(Metric.valueOf(measureObj.getString("Metric")));

				measures.add(mes);
			}
			test.setMeasures(measures);


			List<TestURL> urls = new ArrayList<TestURL>();
			JSONArray urlsArray = scriptObj.getJSONArray("URLs");
			for(int uIndex=0; uIndex<urlsArray.length(); uIndex++){
				JSONObject urlObj = urlsArray.getJSONObject(uIndex);
				TestURL url = new TestURL();
				url.setURL(urlObj.getString("URL"));
				url.setType(URLTypes.valueOf(urlObj.getString("Type")));

				//Steps
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

	public void doProcess2(boolean runAll) {

		//boolean startable = acquirePlatform();
		//if (startable) {
			
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
					pf.dump();
				}

				Log.d("Marlin", getClass().getName() + ": received:"
						+ tests.size());
			} catch (Exception e) {
				Log.d("Marlin", getClass().getName(), e);
			} finally {
				stopPlatform();
			}
	}



	public void initialzePlatform(boolean full) {
		if (pf == null) {
			//pf = new Platform(this); //aqui se le pasa el service
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
			pf.stop();
			pf = null;
		}
	}
}
