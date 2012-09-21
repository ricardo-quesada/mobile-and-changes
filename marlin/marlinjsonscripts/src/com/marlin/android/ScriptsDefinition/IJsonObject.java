package com.marlin.android.ScriptsDefinition;

import org.json.JSONObject;

import com.google.gson.Gson;

public class IJsonObject {
	
	public static String ToJsonObject (Object object){
		
		 JSONObject obj = new JSONObject(object);
		 return obj.toString();
	}
	
	public static String ToGsonString (Object object){
		Gson gson = new Gson();
		return gson.toJson(object);
	}

}
