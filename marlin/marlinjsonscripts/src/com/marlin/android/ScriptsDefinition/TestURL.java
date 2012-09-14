/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.marlin.android.ScriptsDefinition;

import java.io.Serializable;

public class TestURL  implements Serializable
{
    private String URL;
    private URLTypes Type;
    /*
     * Steps to be executed on this script
     */
    private Step[] Steps;
    

    /**
     * @return the URL
     */
    public String getURL() {
        return URL;
    }

    /**
     * @param URL the URL to set
     */
    public void setURL(String URL) {
        this.URL = URL;
    }

    /**
     * @return the Type
     */
    public URLTypes getType() {
        return Type;
    }

    /**
     * @param Type the Type to set
     */
    public void setType(URLTypes Type) {
        this.Type = Type;
    }

    /**
     * @return the Steps
     */
    public Step[] getSteps() {
        return Steps;
    }

    /**
     * @param Steps the Steps to set
     */
    public void setSteps(Step[] Steps) {
        this.Steps = Steps;
    }

    
}
