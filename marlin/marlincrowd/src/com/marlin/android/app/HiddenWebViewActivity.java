package com.marlin.android.app;

import java.util.HashMap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.marlin.android.app.service.AppService;
import com.marlin.android.sdk.WebViewRunner;

public class HiddenWebViewActivity extends Activity {

	private WebView wv;
	private long eventStart = 0;
	private long eventEnd = 0;
	private boolean availability;
	private int resultCode;
	private String resultDescription;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("Marlin", AppService.class.getName()
				+ ": resume HiddenWebViewActivity.");
		this.setVisible(false);
		wv = new WebView(this);
		eventStart = System.currentTimeMillis();
		eventEnd = 0;
		wv.setWebViewClient(new PlatformWebViewClient());
		wv.getSettings().setJavaScriptEnabled(true);
		wv.loadDataWithBaseURL(getIntent().getStringExtra(
				WebViewRunner.BASE_URL), getIntent().getStringExtra(
				WebViewRunner.DATA), getIntent().getStringExtra(
				WebViewRunner.MIME_TYPE), getIntent().getStringExtra(
				WebViewRunner.ENCODING), getIntent().getStringExtra(
				WebViewRunner.FAIL_URL));
		HashMap<String, String> resultMap = new HashMap<String, String>();
		resultMap.put(WebViewRunner.START_TIME, Long.toString(eventStart));
		resultMap.put(WebViewRunner.END_TIME, Long.toString(eventEnd));
		resultMap.put(WebViewRunner.AVAILABILITY, Boolean
				.toString(availability));
		resultMap.put(WebViewRunner.RESULT_CODE, Integer.toString(resultCode));
		resultMap.put(WebViewRunner.RESULT_DESCRIPTION, resultDescription);
		MarlinApplication ma = (MarlinApplication) getApplication();
		ma.getAppService().setWebViewResult(resultMap);
		finish();
	}

	private class PlatformWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.d("Marlin", getClass().getName() + ": NOT loading url:" + url);
			// view.loadUrl(url);
			return true;
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			Log.d("Marlin", getClass().getName() + ": on page started:" + url);
			super.onPageStarted(view, url, favicon);
			eventEnd = 0;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			Log.d("Marlin", getClass().getName() + ": on page finished:" + url);
			eventEnd = System.currentTimeMillis();
			availability = true;
			super.onPageFinished(view, url);
			view
					.loadUrl("javascript:window.HTMLOUT.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			Log.d("Marlin", getClass().getName() + ": on received error:"
					+ failingUrl);
			eventEnd = System.currentTimeMillis();
			availability = false;
			resultCode = errorCode;
			resultDescription = description;
			super.onReceivedError(view, errorCode, description, failingUrl);
		}

		@Override
		public void onLoadResource(WebView view, String url) {
			Log.d("Marlin", getClass().getName() + ": on load resource:" + url);
			super.onLoadResource(view, url);
		}

	}
}