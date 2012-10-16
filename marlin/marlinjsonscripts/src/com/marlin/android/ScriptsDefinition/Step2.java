package com.marlin.android.ScriptsDefinition;

import java.io.Serializable;
import java.util.List;

public class Step2 implements Serializable{
    private int Number;
    private ActionType Type;
    private List<Parameter> Parameters;
    
    
	public int getNumber() {
		return Number;
	}
	
	public void setNumber(int number) {
		Number = number;
	}

	public ActionType getType() {
		return Type;
	}

	public void setType(ActionType type) {
		Type = type;
	}

	public List<Parameter> getParameters() {
		return Parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		Parameters = parameters;
	}
	
	
	
	
    

}
