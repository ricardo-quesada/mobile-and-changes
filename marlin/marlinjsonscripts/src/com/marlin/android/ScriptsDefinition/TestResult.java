/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.marlin.android.ScriptsDefinition;

import java.util.Date;

/**
 *
 * @author rquesada
 */
public class TestResult 
{
    private int Id;
    /*
     * Name of the test
     */
    private String Name;
    
    private String _DeviceId;
    
    /*
     * List of all the URLs that must be test
     */ 
    private URLTestResult[] URLResults;
    
    private Date _StartTest;
    private Date _EndTest;
    private Date _PostTime;
    private TestResultState _Estate;
    private int _RunNumber;
    private Layer[] _Layers;

    /**
     * @return the Id
     */
    public int getId() {
        return Id;
    }

    /**
     * @param Id the Id to set
     */
    public void setId(int Id) {
        this.Id = Id;
    }

    /**
     * @return the Name
     */
    public String getName() {
        return Name;
    }

    /**
     * @param Name the Name to set
     */
    public void setName(String Name) {
        this.Name = Name;
    }

    /**
     * @return the _DeviceId
     */
    public String getDeviceId() {
        return _DeviceId;
    }

    /**
     * @param DeviceId the _DeviceId to set
     */
    public void setDeviceId(String DeviceId) {
        this._DeviceId = DeviceId;
    }

    /**
     * @return the URLResults
     */
    public URLTestResult[] getURLResults() {
        return URLResults;
    }

    /**
     * @param URLResults the URLResults to set
     */
    public void setURLResults(URLTestResult[] URLResults) {
        this.URLResults = URLResults;
    }

    /**
     * @return the _StartTest
     */
    public Date getStartTest() {
        return _StartTest;
    }

    /**
     * @param StartTest the _StartTest to set
     */
    public void setStartTest(Date StartTest) {
        this._StartTest = StartTest;
    }

    /**
     * @return the _EndTest
     */
    public Date getEndTest() {
        return _EndTest;
    }

    /**
     * @param EndTest the _EndTest to set
     */
    public void setEndTest(Date EndTest) {
        this._EndTest = EndTest;
    }

    /**
     * @return the _PostTime
     */
    public Date getPostTime() {
        return _PostTime;
    }

    /**
     * @param PostTime the _PostTime to set
     */
    public void setPostTime(Date PostTime) {
        this._PostTime = PostTime;
    }

    /**
     * @return the _Estate
     */
    public TestResultState getEstate() {
        return _Estate;
    }

    /**
     * @param Estate the _Estate to set
     */
    public void setEstate(TestResultState Estate) {
        this._Estate = Estate;
    }

    /**
     * @return the _RunNumber
     */
    public int getRunNumber() {
        return _RunNumber;
    }

    /**
     * @param RunNumber the _RunNumber to set
     */
    public void setRunNumber(int RunNumber) {
        this._RunNumber = RunNumber;
    }

    /**
     * @return the _Layers
     */
    public Layer[] getLayers() {
        return _Layers;
    }

    /**
     * @param Layers the _Layers to set
     */
    public void setLayers(Layer[] Layers) {
        this._Layers = Layers;
    }
}
