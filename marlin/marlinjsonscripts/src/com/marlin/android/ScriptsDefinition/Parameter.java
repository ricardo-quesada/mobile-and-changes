package com.marlin.android.ScriptsDefinition;

import java.io.Serializable;

public class Parameter implements Serializable{

    private String Name;
    private String Value;
    
    
	public String getName() {
		return Name;
	}
	
	public void setName(String name) {
		Name = name;
	}
	
	public String getValue() {
		return Value;
	}
	
	public void setValue(String value) {
		Value = value;
	}


}
