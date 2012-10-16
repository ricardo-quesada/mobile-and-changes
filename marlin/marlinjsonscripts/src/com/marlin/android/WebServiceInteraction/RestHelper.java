package com.marlin.android.WebServiceInteraction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;


public class RestHelper {

	private static final int TIME_OUT = 300000;
	private static final String CONTENT_TYPE = "application/json";

	private static RestHelper _instance;

	private RestHelper()
	{

	}

	public String PUT(String pUrl, String pPutData) throws Exception
	{
		try
		{

			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is established.
			int timeoutConnection = TIME_OUT;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT) 
			int timeoutSocket = TIME_OUT;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

			HttpPut putRequest = new HttpPut(pUrl);

			StringEntity input = new StringEntity(pPutData);
			input.setContentType(CONTENT_TYPE);

			putRequest.setEntity(input);
			HttpResponse response = httpClient.execute(putRequest);

			HttpEntity entity = response.getEntity();

			InputStream instream = entity.getContent();
			String result= convertStreamToString(instream);
			instream.close();
			return result;
		}
		catch(Exception ex)
		{
			Exception Ex = new Exception(
					"There was an error excecuting an HTTP PUT Request to: " + pUrl, 
					ex);

			throw Ex;
		}
	}

	public String GET(String pUrl) throws Exception
	{
		try
		{
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is established.
			int timeoutConnection = TIME_OUT;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT) 
			int timeoutSocket = TIME_OUT;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			HttpClient httpclient = new DefaultHttpClient(httpParameters);
			HttpGet httpget = new HttpGet(pUrl);

			HttpResponse response;

			response = httpclient.execute(httpget);

			HttpEntity entity = response.getEntity();

			InputStream instream = entity.getContent();
			String result= convertStreamToString(instream);
			instream.close();
			return result; 
		}
		catch(Exception ex)
		{
			Exception Ex = new Exception(
					"There was an error excecuting an HTTP GET Request to: " + pUrl, 
					ex);

			throw Ex;
		}

	}


	//Ricardo agrega
	public String postData(String pUrl, List<NameValuePair> nameValuePairs) throws Exception
	{
		try {
			// Create a new HttpClient and Post Header
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is established.
			int timeoutConnection = TIME_OUT;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT) 
			int timeoutSocket = TIME_OUT;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			HttpClient httpclient = new DefaultHttpClient(httpParameters);
			HttpPost httppost = new HttpPost(pUrl);

			// Add your data
			/*List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

			nameValuePairs.add(new BasicNameValuePair("id", "12345"));
			nameValuePairs.add(new BasicNameValuePair("stringdata", "Hi"));*/

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);

			HttpEntity entity = response.getEntity();

			InputStream instream = entity.getContent();
			String result= convertStreamToString(instream);
			instream.close();
			return result;
		}

		catch(Exception ex)
		{
			Exception Ex = new Exception(
					"There was an error excecuting an HTTP POST Request to: " + pUrl, 
					ex);

			throw Ex;
		}
	}

	public String POST(String pUrl, String pPostData) throws Exception
	{
		try
		{
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is established.
			int timeoutConnection = TIME_OUT;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT) 
			int timeoutSocket = TIME_OUT;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

			HttpPost postRequest = new HttpPost(pUrl);
			

			StringEntity input = new StringEntity(pPostData);
			input.setContentType(CONTENT_TYPE);
			postRequest.setHeader("Accept", CONTENT_TYPE);
			postRequest.setHeader("Content-type", CONTENT_TYPE);
			

			postRequest.setEntity(input);
			HttpResponse response = httpClient.execute(postRequest);

			HttpEntity entity = response.getEntity();

			InputStream instream = entity.getContent();
			String result= convertStreamToString(instream);
			instream.close();
			return result;
		}
		catch(Exception ex)
		{
			Exception Ex = new Exception(
					"There was an error excecuting an HTTP PUT Request to: " + pUrl, 
					ex);

			throw Ex;
		}
	}

	
	public String GetWithCredentials(String pUrl, String pUserName, String pPassword) throws Exception{
		String scriptStr = null;
		try {
			HttpParams httpParameters = new BasicHttpParams();

			// Set the timeout in milliseconds until a connection is established.
			int timeoutConnection = TIME_OUT;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);

			// Set the default socket timeout (SO_TIMEOUT) 
			int timeoutSocket = TIME_OUT;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

			// Set connection credentials
			httpClient.getCredentialsProvider().setCredentials(	
					new AuthScope(null, -1),
					new UsernamePasswordCredentials(pUserName,pPassword));

			HttpGet httpget = new HttpGet(pUrl);
			HttpEntity httpEntity = httpClient.execute(httpget).getEntity();
			scriptStr = EntityUtils.toString(httpEntity, "UTF-8");
		} catch (Exception ex) {
			Exception Ex = new Exception(
					"There was an error excecuting an HTTP GET Request to: " + pUrl, 
					ex);

			throw Ex;
		}

		return scriptStr;
	}

	private static String convertStreamToString(InputStream pInputStream) throws IOException 
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(pInputStream));
		StringBuilder stringBuilder = new StringBuilder();

		String line = null;
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line + "\n");
		}
		return stringBuilder.toString();
	}

	public static RestHelper getInstance()
	{
		if (_instance == null)
			_instance = new RestHelper();
		return _instance; 
	}


}
