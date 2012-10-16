package com.marlin.android.ScriptsDefinition;

import org.json.JSONObject;

import com.google.gson.Gson;

public class IJsonObject {
	
	public JSONObject ToJsonObject (Object object){
		
		 JSONObject obj = new JSONObject(object);
		 return obj;
	}
	
	public String ToGsonString (Object object){
		Gson gson = new Gson();
		return gson.toJson(object);
	}

}
