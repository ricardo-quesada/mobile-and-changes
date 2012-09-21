/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.marlin.android.ScriptsDefinition;

import java.util.Date;
import java.util.List;

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
    
    private String DeviceId;
    
    /*
     * List of all the URLs that must be test
     */ 
    private List<URLTestResult> URLResults;
    
    private Date StartTest;
    private Date EndTest;
    private Date PostTime;
    private TestResultState State;
    private int RunNumber;
    private List<Layer> Layers;

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
        return DeviceId;
    }

    /**
     * @param DeviceId the _DeviceId to set
     */
    public void setDeviceId(String DeviceId) {
        this.DeviceId = DeviceId;
    }

    /**
     * @return the URLResults
     */
    public List<URLTestResult> getURLResults() {
        return URLResults;
    }

    /**
     * @param URLResults the URLResults to set
     */
    public void setURLResults(List<URLTestResult> URLResults) {
        this.URLResults = URLResults;
    }

    /**
     * @return the _StartTest
     */
    public Date getStartTest() {
        return StartTest;
    }

    /**
     * @param StartTest the _StartTest to set
     */
    public void setStartTest(Date StartTest) {
        this.StartTest = StartTest;
    }

    /**
     * @return the _EndTest
     */
    public Date getEndTest() {
        return EndTest;
    }

    /**
     * @param EndTest the _EndTest to set
     */
    public void setEndTest(Date EndTest) {
        this.EndTest = EndTest;
    }

    /**
     * @return the _PostTime
     */
    public Date getPostTime() {
        return PostTime;
    }

    /**
     * @param PostTime the PostTime to set
     */
    public void setPostTime(Date PostTime) {
        this.PostTime = PostTime;
    }

    /**
     * @return the State
     */
    public TestResultState getEstate() {
        return State;
    }

    /**
     * @param State the state to set
     */
    public void setState(TestResultState State) {
        this.State = State;
    }

    /**
     * @return the RunNumber
     */
    public int getRunNumber() {
        return RunNumber;
    }

    /**
     * @param RunNumber the RunNumber to set
     */
    public void setRunNumber(int RunNumber) {
        this.RunNumber = RunNumber;
    }

    /**
     * @return the _Layers
     */
    public List<Layer> getLayers() {
        return Layers;
    }

    /**
     * @param Layers the _Layers to set
     */
    public void setLayers(List<Layer> Layers) {
        this.Layers = Layers;
    }
}
