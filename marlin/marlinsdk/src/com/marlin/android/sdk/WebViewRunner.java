package com.marlin.android.sdk;

import java.util.HashMap;

public interface WebViewRunner {

	public static final String BASE_URL = "baseUrl";
	public static final String DATA = "data";
	public static final String MIME_TYPE = "mimeType";
	public static final String ENCODING = "encoding";
	public static final String FAIL_URL = "failUrl";
	
	public static final String START_TIME = "startTime";
	public static final String END_TIME = "endTime";
	public static final String AVAILABILITY = "availability";
	public static final String RESULT_CODE = "resultCode";
	public static final String RESULT_DESCRIPTION = "resultDescription";

	public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String failUrl);
	
	public HashMap<String, String> getWebViewResults();
}
