/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.marlin.android.ScriptsDefinition;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.google.gson.Gson;
/**
 *
 * @author rquesada
 */
public class MarlinScripts
{

    
	
	/**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        Test test = new Test();
        test.setEndDate(new Date());
        test.setStartDate(test.getEndDate());
        List<Layer> layers = new ArrayList<Layer>();
        test.setLayerSpecs(layers);
        Layer layer = new Layer();
        layer.setLayerName("Pop");
        Map<String,String> dic = new HashMap<String,String>();
        dic.put("testkey", "testvalue");
        dic.put("thekey", "thevalue");
        layer.setLayerBody(dic);
        test.getLayerSpecs().add(layer);
        test.getLayerSpecs().add(layer);
        
        test.setMeasures(new ArrayList<Measure>());
        
        Measure measure1 = new Measure();
        measure1.setType(MeasureType.CARRIER);
        measure1.setMeasureItem("AT&T");
        measure1.setValue("");
        measure1.setCategory(MeasureCategory.PERFORMANCE);
        measure1.setStarTime(new Date());
        measure1.setEndTime(new Date());
        measure1.setMetric(Metric.MILISECOND);
        
        test.getMeasures().add(measure1);
        
        measure1 = new Measure();
        measure1.setType(MeasureType.CARRIER);
        measure1.setMeasureItem("Sprint");
        measure1.setValue("thevalue");
        measure1.setCategory(MeasureCategory.PERFORMANCE);
        measure1.setStarTime(new Date());
        measure1.setEndTime(new Date());
        measure1.setMetric(Metric.MILISECOND);
        test.getMeasures().add(measure1);
        
        test.setModifiers(new ArrayList<TestModifier>());
        test.getModifiers().add(TestModifier.RUN_ONE_TIME);
        test.getModifiers().add(TestModifier.RUN_AVERAGE);
        
        test.setName("Name of the test");
        test.setTimeZone("UTC");
        test.setVersion((float) 1.0);
        test.setVerificationNumber(new byte[5]);
        test.getVerificationNumber()[0] = 10;
        test.getVerificationNumber()[1] = 12;
        test.getVerificationNumber()[2] = 14;
        test.getVerificationNumber()[3] = 16;
        test.getVerificationNumber()[4] = 18;
        
        
        test.setURLs(new ArrayList<TestURL>());
        test.getURLs().add(new TestURL());
        test.getURLs().get(0).setType(URLTypes.PAGE);
        test.getURLs().get(0).setURL("http://www.nacion.com/");
        
        
        test.getURLs().get(0).setSteps(new ArrayList<Step>());

        Step step = new Step();
        step.setExtendedSpec("ExtendedSpec");
        step.setMethod("onclick");
        step.setNumber(1);
        step.setObject("TextArea");
        step.setType(ActionType.NAVIGATE);
        step.setWaitTime(1000);        
        test.getURLs().get(0).getSteps().add(step);
        
        
        step = new Step();
        step.setExtendedSpec("ExtendedSpec");
        step.setMethod("onChange");
        step.setNumber(1);
        step.setObject("Button");
        step.setType(ActionType.CLICK);
        step.setWaitTime(2000);        
        test.getURLs().get(0).getSteps().add(step);
        
        test.setTestTimes(new String[2]);
        test.getTestTimes()[0] = TimeFormater.TimeToString(new Time(10000));
        test.getTestTimes()[1] = TimeFormater.TimeToString(new Time(20000));
        
        test.setId(2);

        JSONObject obj = new JSONObject(test);
        System.out.println(obj.toString());
        
        
        Gson gson = new Gson();
		String data = gson.toJson(test);
		//Log.i("Marlin", data);
		System.out.println(data);
		
		//System.out.println(IJsonObject.ToGsonString(data));
		
	    
		
		//Test myobj = gson.fromJson(data, Test.class);
		//Test eed = gson.fromJson(obj.toString(), test.getClass());
		//System.out.println(myobj.getId());
		//System.out.println(eed.getId());
        
        // Result Test
        
        TestResult result = new TestResult();
        result.setDeviceId("DeviceId");
        result.setState(TestResultState.COMPLETED);
        result.setLayers(new ArrayList<Layer>());
        
        Layer layRes = new Layer();
        layRes.setLayerName("Pop");

        result.getLayers().add(layRes);
        result.getLayers().add(layRes);
        
        result.setName("TestName");
        result.setStartTest(new Date());
        result.setEndTest(result.getStartTest());
        
        result.setRunNumber(1);
        
        result.setURLResults(new ArrayList<URLTestResult>());
        URLTestResult testResult= new URLTestResult();
        testResult.setType(URLTypes.PAGE);
        testResult.setURL("http://www.testhost.com/site");
        testResult.setResults(new ArrayList<Measure>());
        Measure measure = new Measure();
        measure.setMeasureItem("Image_001");
        measure.setType(MeasureType.LOAD);
        measure.setMetric(Metric.MILISECOND);
        measure.setValue("2500");
        
        Measure measure2 = new Measure();
        measure2.setMeasureItem("Button_002");
        measure2.setType(MeasureType.LOAD);
        measure2.setMetric(Metric.MILISECOND);
        measure2.setValue("1250");
        
        testResult.getResults().add(measure);
        testResult.getResults().add(measure2);

        result.getURLResults().add(testResult);
        
        

        
        String objResult = gson.toJson(result);
        System.out.println(objResult);
    }
}
