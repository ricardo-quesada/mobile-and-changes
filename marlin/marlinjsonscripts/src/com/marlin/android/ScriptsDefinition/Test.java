/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.marlin.android.ScriptsDefinition;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.io.Serializable;

/**
 *
 * @author rquesada
 */
public class Test  implements Serializable
{
    private int Id;
    /*
     * Name of the test
     */
    private String Name;
    /*
     * Version number for this test with this name
     */
    private float Version;
    /*
     * List of all the URLs that must be test
     */ 
    private List<TestURL> URLs;
    /*
     * List of modifiers marks for this test
     */
    private List<TestModifier> Modifiers;
    /*
     * Special layers used for specific processors
     */
    private List<Layer> LayerSpecs;
    /*
     * Validation number used as a checksum for this test script
     */
    private byte[] VerificationNumber;
    /*
     * Special measures to be considered on this test
     */
    private List<Measure> Measures;
    /*
     * Date where the test must starts
     */
    private Date StartDate;
    /*
     * EndDate where the test must be completed
     */
    private Date EndDate;
    /*
     * Hours where the test must run
     */
    private String[] TestTimes;
    /*
     * TimeZones for the TestTimes
     */
    private String TimeZone;
    
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
     * @return the Version
     */
    public float getVersion() {
        return Version;
    }

    /**
     * @param Version the Version to set
     */
    public void setVersion(float Version) {
        this.Version = Version;
    }

    /**
     * @return the URLs
     */
    public List<TestURL> getURLs() {
        return URLs;
    }

    /**
     * @param URLs the URLs to set
     */
    public void setURLs(List<TestURL> URLs) {
        this.URLs = URLs;
    }

    /**
     * @return the Modifiers
     */
    public List<TestModifier> getModifiers() {
        return Modifiers;
    }

    /**
     * @param Modifiers the Modifiers to set
     */
    public void setModifiers(List<TestModifier> Modifiers) {
        this.Modifiers = Modifiers;
    }

    /**
     * @return the LayerSpecs
     */
    public List<Layer> getLayerSpecs() {
        return LayerSpecs;
    }

    /**
     * @param LayerSpecs the LayerSpecs to set
     */
    public void setLayerSpecs(List<Layer> LayerSpecs) {
        this.LayerSpecs = LayerSpecs;
    }

    /**
     * @return the VerificationNumber
     */
    public byte[] getVerificationNumber() {
        return VerificationNumber;
    }

    /**
     * @param VerificationNumber the VerificationNumber to set
     */
    public void setVerificationNumber(byte[] VerificationNumber) {
        this.VerificationNumber = VerificationNumber;
    }

    /**
     * @return the Measures
     */
    public List<Measure> getMeasures() {
        return Measures;
    }

    /**
     * @param Measures the Measures to set
     */
    public void setMeasures(List<Measure> Measures) {
        this.Measures = Measures;
    }

    /**
     * @return the StartDate
     */
    public Date getStartDate() {
        return StartDate;
    }

    /**
     * @param StartDate the StartDate to set
     */
    public void setStartDate(Date StartDate) {
        this.StartDate = StartDate;
    }

    /**
     * @return the EndDate
     */
    public Date getEndDate() {
        return EndDate;
    }

    /**
     * @param EndDate the EndDate to set
     */
    public void setEndDate(Date EndDate) {
        this.EndDate = EndDate;
    }

    /**
     * @return the TestTimes
     */
    public String[] getTestTimes() {
        return TestTimes;
    }

    /**
     * @param TestTimes the TestTimes to set
     */
    public void setTestTimes(String[] TestTimes) {
        this.TestTimes = TestTimes;
    }

    /**
     * @return the TimeZone
     */
    public String getTimeZone() {
        return TimeZone;
    }

    /**
     * @param TimeZone the TimeZone to set
     */
    public void setTimeZone(String TimeZone) {
        this.TimeZone = TimeZone;
    }

    /**
     * @return the _Id
     */
    public int getId() {
        return Id;
    }

    /**
     * @param Id the _Id to set
     */
    public void setId(int Id) {
        this.Id = Id;
    }
    
    
    
}
