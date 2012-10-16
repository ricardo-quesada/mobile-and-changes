package com.marlin.android.ScriptsDefinition;

import java.io.Serializable;
import java.util.List;

public class Test2  implements Serializable
{
	/*
	 * Unique test identifier  
	 */
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
     * The url that must be test
     */ 
    private String Url;
    /*
     * The list of steps to run over the url
     */
    private List<Step2> Steps;
    
	public int getId() {
		return Id;
	}
	
	public void setId(int id) {
		Id = id;
	}
	
	public String getName() {
		return Name;
	}
	
	public void setName(String name) {
		Name = name;
	}
	
	public float getVersion() {
		return Version;
	}
	
	public void setVersion(float version) {
		Version = version;
	}
	
	public String getUrl() {
		return Url;
	}
	
	public void setUrl(String url) {
		Url = url;
	}
	
	public List<Step2> getSteps() {
		return Steps;
	}
	
	public void setSteps(List<Step2> steps) {
		Steps = steps;
	}

}
