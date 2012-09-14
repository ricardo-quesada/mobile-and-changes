package com.marlin.android.sdk;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

import android.util.Log;

public class GetThread extends Thread {

	private CookieManager cookieManager = null;
	private HashMap<String, String> headers = null;
	private final int internalId;
	private URL elementUrl = null;
	private long connectionStart = 0;
	private long firstByte = 0;
	private long connectionEnd = 0;
	private long dnsStart = 0;
	private long dnsEnd = 0;

	private boolean ssl;
	private boolean availability;
	private String contentType;
	private long bytesCount;
	private int resultCode;
	private String resultDesc;

	/** The response result of the URL get. */
	private List<PageElement> pageElements;

	public GetThread(URL elementUrl, int id, CookieManager cm,
			HashMap<String, String> hdrs) {
		this.elementUrl = elementUrl;
		this.internalId = id;
		this.cookieManager = cm;
		this.headers = hdrs;
		pageElements = new ArrayList<PageElement>();
	}

	public List<PageElement> getResult() {
		return pageElements;
	}

	public URL getUrl() {
		return elementUrl;
	}

	/**
	 * Executes the GetMethod and prints some status information.
	 */
	@Override
	public void run() {

		Log.d("Marlin", getClass().getName() + internalId
				+ " - about to get something from " + elementUrl);

		HttpURLConnection con = null;
		InputStream ins = null;
		// start from 1 because redirect count =0 indicates the final url.
		try {

			for (int redirCount = 1; redirCount <= 10; redirCount++) {

				dnsStart = System.currentTimeMillis();
				try {
					InetAddress.getByName(elementUrl.getHost());
				} catch (UnknownHostException e1) {
					Log.w("Marlin", getClass().getName() + ": "
							+ e1.getMessage());
				}
				dnsEnd = System.currentTimeMillis();
				Log.d("Marlin", getClass().getName() + ": Dns lookup took="
						+ (dnsEnd - dnsStart));

				int respCode = 0;
				String encoding = null;
				for (int i = 0; i < 3; i++) {
					connectionStart = System.currentTimeMillis();
					connectionEnd = 0;
					con = (HttpURLConnection) elementUrl.openConnection();
					if (cookieManager != null) {
						cookieManager.setCookies(con);
					}
					for (String hdrName : headers.keySet()) {
						con.setRequestProperty(hdrName, headers.get(hdrName));
					}

					con.setInstanceFollowRedirects(false);
					if (cookieManager != null) {
						cookieManager.storeCookies(con);
					}
					respCode = con.getResponseCode();
					contentType = con.getContentType();
					encoding = con.getContentEncoding();
					Log.d("Marlin", getClass().getName() + ": try " + i
							+ " respcode=" + respCode);
					if (con.getResponseCode() != -1) {
						break;
					}
				}
				if (respCode == 200) {
					con.setDefaultUseCaches(false);
					con.setConnectTimeout(60000);
					if ("gzip".equalsIgnoreCase(encoding)) {
						ins = new GZIPInputStream(con.getInputStream());
					} else {
						ins = con.getInputStream();
					}
					int c;
					boolean firstbyte = true;
					bytesCount = 0;
					while ((c = ins.read()) != -1) {
						bytesCount++;
						if (firstbyte) {
							firstByte = System.currentTimeMillis();
							Log.d("Marlin", getClass().getName()
									+ ": firstbyte:");
							firstbyte = false;
						}
					}

					ins.close();
					con.disconnect();
					availability = true;
					connectionEnd = System.currentTimeMillis();

					Log.d("Marlin", getClass().getName() + ": fetched bytes:"
							+ bytesCount);
					break;
				} else if (respCode >= 300 && respCode < 400) {
					String redirLoc = con.getHeaderField("location");
					if (!redirLoc.startsWith("http")) {
						redirLoc = elementUrl.toString() + redirLoc;
					}
					URL redirUrl = new URL(redirLoc);
					Log.d("Marlin", getClass().getName() + ">>" + elementUrl);
					con.disconnect();
					connectionEnd = System.currentTimeMillis();
					pageElements.add(buildRedirectPageElement(redirCount,
							respCode));
					elementUrl = redirUrl;
				} else {
					availability = false;
					resultCode = -1;
					resultDesc = "Server returned response code: " + respCode;
					Log.e("Marlin", getClass().getName() + resultDesc);
					break;
				}
			}
		} catch (FileNotFoundException fnfe) {
			availability = false;
			resultCode = 404;
			resultDesc = fnfe.getClass().getName() + ": " + fnfe.getMessage();
			Log.e("Marlin", getClass().getName(), fnfe);
		} catch (Exception e) {
			availability = false;
			resultCode = -1;
			resultDesc = e.getClass().getName() + ": " + e.getMessage();
			Log.e("Marlin", getClass().getName(), e);
		} finally {
			if (ins != null) {
				try {
					ins.close();
				} catch (IOException e) {
					Log.e("Marlin", getClass().getName(), e);
				}
			}
			if (con != null) {
				con.disconnect();
			}
		}

		pageElements.add(buildFinalPageElement());
	}

	private PageElement buildFinalPageElement() {
		PageElement pageElement = new PageElement();
		pageElement.setElementUrl(elementUrl.toString());
		pageElement.setElementId(elementUrl.getPath());
		pageElement.setRedirectCount(0);
		pageElement.setAvailability(availability);
		pageElement.setResultCode(resultCode);
		pageElement.setResultDesc(resultDesc);
		Connection connection = new Connection();
		connection.setDnsTime(Long.toString(dnsEnd - dnsStart));
		connection.setBytesDownloaded(Long.toString(bytesCount));
		if (firstByte > 0) {
			connection.setFirstByte(Long.toString(firstByte - connectionStart));
		}
		if (connectionEnd > 0) {
			connection.setConnectionTime(Long.toString(connectionEnd
					- connectionStart));
		}
		connection.setContentType(contentType);
		connection.setStartTimeStamp(Long.toString(connectionStart));
		pageElement.setConnection(connection);
		// milliseconds and Kilo bytes cancel each other
		double throughput = 0;
		if (connectionEnd > 0) {
			throughput = ((0.0 + bytesCount) / (connectionEnd - connectionStart));
		}
		DecimalFormat df = new DecimalFormat("#0.0");
		pageElement.setThroughput(df.format(throughput) + "Kbps");
		return pageElement;
	}

	private PageElement buildRedirectPageElement(int redirCount, int respCode) {
		PageElement pageElement = new PageElement();
		pageElement.setElementUrl(elementUrl.toString());
		pageElement.setElementId(elementUrl.getPath());
		pageElement.setRedirectCount(redirCount);
		pageElement.setAvailability(true);
		pageElement.setResultCode(respCode);
		pageElement.setResultDesc("Redirect");
		Connection con = new Connection();
		con.setSsl(ssl);
		if (connectionEnd > 0) {
			con.setConnectionTime(Long
					.toString(connectionEnd - connectionStart));
		}
		if (dnsEnd > dnsStart) {
			con.setDnsTime(Long.toString(dnsEnd - dnsStart));
		}
		con.setStartTimeStamp(Long.toString(connectionStart));
		pageElement.setConnection(con);
		return pageElement;
	}
}
