/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.marlin.android.ScriptsDefinition;

import java.io.Serializable;

public class URLTestResult  implements Serializable
{
    private String URL;
    private URLTypes Type;
    private Measure[] _Results;

    /**
     * 
     * @return the list of measures collected
     */
    public Measure[] getResults() {
        return _Results;
    }

    
    /**
     * 
     * @param _Results the list measures to set
     */
    public void setResults(Measure[] _Results) {
        this._Results = _Results;
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
