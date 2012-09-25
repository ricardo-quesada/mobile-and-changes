/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.marlin.android.ScriptsDefinition;

import java.io.Serializable;
import java.util.List;

public class TestURL  implements Serializable
{
	/*
	 * 
	 */
    private int UrlId;
	/*
	 * 
	 */
    private String URL;
    /*
     * 
     */
    private URLTypes Type;
    /*
     * Steps to be executed on this script
     */
    private List<Step> Steps;
    
    /**
     * 
     * @return
     */
    public int getUrlId() {
		return UrlId;
	}

    /**
     * 
     * @param urlId
     */
	public void setUrlId(int urlId) {
		UrlId = urlId;
	}

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
    public List<Step> getSteps() {
        return Steps;
    }

    /**
     * @param Steps the Steps to set
     */
    public void setSteps(List<Step> Steps) {
        this.Steps = Steps;
    }

	

    
}
