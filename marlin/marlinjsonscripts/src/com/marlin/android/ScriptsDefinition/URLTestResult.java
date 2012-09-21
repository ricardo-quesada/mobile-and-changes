/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.marlin.android.ScriptsDefinition;

import java.io.Serializable;
import java.util.List;

public class URLTestResult  implements Serializable
{
    private String URL;
    private URLTypes Type;
    private List<Measure> Results;

    /**
     * 
     * @return the list of measures collected
     */
    public List<Measure> getResults() {
        return Results;
    }

    
    /**
     * 
     * @param _Results the list measures to set
     */
    public void setResults(List<Measure> Results) {
        this.Results = Results;
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
}
