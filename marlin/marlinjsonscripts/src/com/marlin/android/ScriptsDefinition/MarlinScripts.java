/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.marlin.android.ScriptsDefinition;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.List;

import org.json.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.util.Log;

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
        List<Layer> x = new ArrayList<Layer>();
        test.setLayerSpecs(x);
        Layer lay = new Layer();
        lay.setLayerName("Pop");
        //lay.setLayerBody("<Proto><definition render=\"1.0\"></Proto>");
        test.getLayerSpecs().add(lay);
        test.getLayerSpecs().add(lay);
        
        test.setMeasures(new ArrayList<Measure>());
        
        Measure metric = new Measure();
        metric.setType(MeasureType.CARRIER);
        metric.setMeasureItem("AT&T");
        metric.setValue("");
        test.getMeasures().add(metric);
        
        metric = new Measure();
        metric.setType(MeasureType.CARRIER);
        metric.setMeasureItem("Sprint");
        metric.setValue("thevalue");
        test.getMeasures().add(metric);
        
        test.setModifiers(new ArrayList<TestModifier>());
        test.getModifiers().add(TestModifier.RUN_ONE_TIME);
        test.getModifiers().add(TestModifier.RUN_ONE_TIME);
        
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
        test.getURLs().get(0).setURL("http://www.testhost.com/site");
        
        
        test.getURLs().get(0).setSteps(new Step[2]);

        Step step = new Step();
        step.setExtendedSpec("ExtendedSpec");
        step.setMethod("onclick");
        step.setNumber(1);
        step.setObject("TextArea");
        step.setType(ActionType.NAVIGATE);
        step.setWaitTime(1000);        
        test.getURLs().get(0).getSteps()[0] = step;
        
        
        step = new Step();
        step.setExtendedSpec("ExtendedSpec");
        step.setMethod("onChange");
        step.setNumber(1);
        step.setObject("Button");
        step.setType(ActionType.CLICK);
        step.setWaitTime(2000);        
        test.getURLs().get(0).getSteps()[1] = step;
        
        test.setTestTimes(new String[2]);
        test.getTestTimes()[0] = TimeFormater.TimeToString(new Time(10000));
        test.getTestTimes()[1] = TimeFormater.TimeToString(new Time(20000));
        
        test.setId(2);

        JSONObject obj = new JSONObject(test);
        System.out.println(obj.toString());
        
        
        Gson gson = new Gson();
		String data = gson.toJson(test);
		Log.i("Marlin", data);
		System.out.println(data);
		
	    
		
		Test myobj = gson.fromJson(data, Test.class);
		Test eed = gson.fromJson(obj.toString(), test.getClass());
		System.out.println(myobj.getId());
		System.out.println(eed.getId());
        
        // Result Test
        
        TestResult result = new TestResult();
        result.setDeviceId("DeviceId");
        result.setEstate(TestResultState.COMPLETED);
        result.setLayers(new Layer[2]);
        
        Layer layRes = new Layer();
        layRes.setLayerName("Pop");

        result.getLayers()[0] = layRes;
        result.getLayers()[1]= layRes;
        
        result.setName("TestName");
        result.setStartTest(new Date());
        result.setEndTest(result.getStartTest());
        
        result.setRunNumber(1);
        
        result.setURLResults(new URLTestResult[1]);
        URLTestResult testResult= new URLTestResult();
        testResult.setType(URLTypes.PAGE);
        testResult.setURL("http://www.testhost.com/site");
        testResult.setResults(new Measure[2]);
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
        
        testResult.getResults()[0] = measure;
        testResult.getResults()[1] = measure2;

        result.getURLResults()[0] = testResult;
        
        

        
        JSONObject objResult = new JSONObject(result);
        System.out.println(objResult.toString());
    }
}
