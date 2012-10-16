package com.marlin.android.sdk;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import android.util.Log;

import com.marlin.webclient.HtmlDocument;

public class PlatformUrlProcessor {

	private URL eventUrl = null;
	private long eventStart = 0;
	private long connectionStart = 0;
	private long firstByte = 0;
	private long connectionEnd = 0;
	private long eventEnd = 0;
	private long dnsStart = 0;
	private long dnsEnd = 0;

	private boolean ssl;
	private boolean availability;
	private String contentType;
	private long bytesCount;
	private int resultCode;
	private String resultDescription;
	private CookieManager cookieManager;
	private HashMap<String, String> headers = null;

	public List<Event> processUrl(WebViewRunner wvRunner, CookieManager cm,
			HashMap<String, String> hdrs, String url, String traceRoute) {
		List<Event> events = new ArrayList<Event>();
		eventStart = System.currentTimeMillis();
		eventEnd = 0;
		resultDescription = "Success";

		cookieManager = cm;
		headers = hdrs;
		// wv.setPictureListener(new PictureListener() {
		//
		// @Override
		// public void onNewPicture(WebView arg0, Picture pic) {
		// Log.v("Marlin", getClass().getName() + ": onNewPicture:");
		// }
		// });
		StringWriter sw = new StringWriter();
		HttpURLConnection con = null;
		InputStream ins = null;
		try {
			eventUrl = new URL(url);
			// start from 1 because redirect count =0 indicates the final url.
			for (int redirCount = 1; redirCount <= 10; redirCount++) {
				Log.i("Marlin", getClass().getName() + ": redirCount :"
						+ redirCount);
				connectionStart = 0;
				firstByte = 0;
				connectionEnd = 0;

				if (eventUrl.getProtocol().startsWith("https")) {
					ssl = true;
				}

				dnsStart = System.currentTimeMillis();
				try {
					InetAddress.getByName(eventUrl.getHost());
				} catch (UnknownHostException e1) {
					Log.w("Marlin", getClass().getName() + ": "
							+ e1.getMessage());
				}
				dnsEnd = System.currentTimeMillis();
				Log.d("Marlin", getClass().getName() + ": Dns lookup took="
						+ (dnsEnd - dnsStart));

				// try 3 times to connect
				int respCode = 0;
				String encoding = null;
				for (int i = 0; i < 3; i++) {
					connectionStart = System.currentTimeMillis();
					con = (HttpURLConnection) eventUrl.openConnection();
					if (cookieManager != null) {
						try {
							cookieManager.setCookies(con);
						} catch (Exception e) {
							Log.w("Marlin", getClass().getName()
									+ ": Failed to setCookies", e);
						}
					}
					for (String hdrName : headers.keySet()) {
						con.setRequestProperty(hdrName, headers.get(hdrName));
					}

					con.setInstanceFollowRedirects(false);
					respCode = con.getResponseCode();
					if (cookieManager != null) {
						try {
							cookieManager.storeCookies(con);
						} catch (Exception e) {
							Log.w("Marlin", getClass().getName()
									+ ": Failed to storeCookies", e);
						}
					}
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
							firstbyte = false;
						}
						sw.write(c);
					}

					ins.close();
					con.disconnect();
					connectionEnd = System.currentTimeMillis();

					Log.d("Marlin", getClass().getName() + ": fetched bytes:"
							+ bytesCount);
					break;
				} else if (respCode >= 300 && respCode < 400) {
					String redirLoc = con.getHeaderField("location");
					Log.d("Marlin", getClass().getName() + "redirLoc: "
							+ eventUrl);
					if (!redirLoc.startsWith("http")) {
						if (redirLoc.startsWith("/")) {
							String rootUrl = eventUrl.toString();
							int baseUrlEnd = rootUrl.indexOf("/", rootUrl
									.indexOf("://") + 3);
							if (baseUrlEnd <= 0) {
								baseUrlEnd = rootUrl.length();
							}
							redirLoc = rootUrl.substring(0, baseUrlEnd)
									+ redirLoc;
						} else {
							redirLoc = eventUrl.toString() + redirLoc;
						}
					}
					URL redirUrl = new URL(redirLoc);
					Log.d("Marlin", getClass().getName() + "EventUrl: "
							+ eventUrl);
					con.disconnect();
					connectionEnd = System.currentTimeMillis();
					events.add(buildRedirectEvent(redirCount, respCode));
					eventUrl = redirUrl;
				} else {
					availability = false;
					resultCode = -1;
					resultDescription = "Server returned response code: "
							+ respCode;
					Log.e("Marlin", getClass().getName() + resultDescription);
					sw.write("Error: " + resultDescription);
					break;
				}
			}
		} catch (FileNotFoundException fnfe) {
			availability = false;
			resultCode = 404;
			resultDescription = fnfe.getClass().getName() + ": "
					+ fnfe.getMessage();
			Log.e("Marlin", getClass().getName(), fnfe);
			sw.write("Error: " + fnfe.getMessage());
		} catch (Exception e) {
			availability = false;
			resultCode = -1;
			resultDescription = e.getClass().getName() + ": " + e.getMessage();
			Log.e("Marlin", getClass().getName(), e);
			sw.write("Error: " + e.getMessage());
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
		contentType = "text/html";
		eventEnd = 0;
		if (wvRunner != null) {
			wvRunner.loadDataWithBaseURL(eventUrl.toString(), sw.toString(),
					contentType, "utf-8", "about:blank");
			// wait for the page to load for a max of 1 min
			// check every 2 sec
			for (int i = 0; i < 30; i++) {
				HashMap<String, String> wvRunnerResult = wvRunner
						.getWebViewResults();
				Log.d("Marlin", getClass().getName() + ": wvRunnerResult="
						+ wvRunnerResult);
				if (wvRunnerResult != null) {
					eventStart = Long.parseLong(wvRunnerResult
							.get(WebViewRunner.START_TIME));
					eventEnd = Long.parseLong(wvRunnerResult
							.get(WebViewRunner.END_TIME));
					availability = Boolean.parseBoolean(wvRunnerResult
							.get(WebViewRunner.AVAILABILITY));
					resultCode = Integer.parseInt(wvRunnerResult
							.get(WebViewRunner.RESULT_CODE));
					resultDescription = wvRunnerResult
							.get(WebViewRunner.RESULT_DESCRIPTION);
					break;
				}
				try {
					Thread.yield();
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
			}
		} else {
			eventEnd = System.currentTimeMillis();
			availability = true;
		}

		// Build Page element data
		List<PageElement> pes = null;
		try {
			HtmlDocument doc = new HtmlDocument(eventUrl, sw.toString()
					.getBytes());
			// HtmlDocument doc = new HtmlDocument(eventUrl,
			// htmlOut.getBytes());
			Set<URL> elements = new HashSet<URL>();
			for (URL u : doc.getPageElementLinks()) {
				elements.add(u);
			}
			Log.d("Marlin", getClass().getName() + ": Page Elements:"
					+ elements);
			// approx calc. for timeout for page elements.
			// if we loaded it onto a web view, we take twice the time it took
			// the page to load as timeout, we take 5 times it took for the html
			// to download as a rule of thumb. We might have to fine tune this
			// later.
			long timeOut = 5 * 60 * 1000; // default is 5 min
			if (eventEnd > 0) {
				timeOut = (eventEnd - eventStart);
				if (wvRunner != null) {
					timeOut = 2 * timeOut;
				} else {
					timeOut = 5 * timeOut;
				}
			}
			// allow twice the time of page load as a timeout
			PageElementsProcessor pep = new PageElementsProcessor(
					cookieManager, headers, timeOut);
			pes = pep.multiProcessRequest(elements);
		} catch (Exception e) {
			Log.e("Marlin", getClass().getName(), e);
		}

		Event event = buildFinalEvent(pes);
		event.setPageElements(pes);

		// Get TraceRoute Data
		if (traceRoute != null && !"none".equalsIgnoreCase(traceRoute)) {
			Set<String> hostNames = new HashSet<String>();
			hostNames.add(eventUrl.getHost());
			for (Iterator<PageElement> iter = pes.iterator(); iter.hasNext();) {
				try {
					PageElement pe = iter.next();
					URL eleUrl = new URL(pe.getElementUrl());
					if ("all".equalsIgnoreCase(traceRoute)
							|| traceRoute.contains(eleUrl.getHost())) {
						hostNames.add(eleUrl.getHost());
					}
				} catch (Exception e) {
					Log.e("Marlin", getClass().getName(), e);
				}
			}

			long trTimeOut = 10 * 60 * 1000; // default is 10 min
			TraceRouteProcessor trp = new TraceRouteProcessor(30, trTimeOut);
			try {
				event.setTraceRouteData(trp.multiProcessRequest(hostNames));
			} catch (Exception e) {
				Log.e("Marlin", getClass().getName(), e);
			}
		}

		events.add(event);

		return events;
	}

	private Event buildFinalEvent(List<PageElement> pes) {
		Event event = new Event();
		event.setRedirectCount(0);
		event.setUrl(eventUrl.toString());
		event.setAvailability(availability);
		event.setResultCode(resultCode);
		event.setResultDesc(resultDescription);
		// calculation for throughput
		// add the bytes of the current page and the page elements
		// then take the end time of the page element loaded last
		// and then compute thoughput as
		// total bytes downloaded / (eventStart-lastElementEnd)

		long totalBytes = bytesCount;
		long longestEndTime = connectionEnd;
		if (pes != null && pes.size() > 0) {
			for (PageElement pe : pes) {
				if (pe.getConnection() != null) {
					try {
						String bytes = pe.getConnection().getBytesDownloaded();
						if (bytes != null) {
							totalBytes += Long.parseLong(bytes);
						}
						long pageEndTime = Long.parseLong(pe.getConnection()
								.getStartTimeStamp())
								+ Long.parseLong(pe.getConnection()
										.getConnectionTime());
						if (pageEndTime > longestEndTime) {
							longestEndTime = pageEndTime;
						}
					} catch (Exception e) {
						// Not sure what to do
						Log.e("Marlin", getClass().getName(), e);
					}
				}
			}
		}

		// milliseconds and Kilo bytes cancel each other

		double throughput = 0;
		if (connectionEnd > 0) {
			throughput = ((0.0 + totalBytes) / (longestEndTime - eventStart));
		}
		DecimalFormat df = new DecimalFormat("#0.0");
		event.setThroughput(df.format(throughput) + "Kbps");

		Connection con = new Connection();
		con.setSsl(ssl);
		con.setLoadRate(df.format(throughput) + "Kbps");
		if (connectionEnd > 0) {
			con.setConnectionTime(Long
					.toString(connectionEnd - connectionStart));
		}
		con.setBytesDownloaded(Long.toString(bytesCount));
		if (firstByte > 0) {
			con.setFirstByte(Long.toString(firstByte - connectionStart));
		}
		con.setContentType(contentType);
		if (longestEndTime > eventStart) {
			con.setEndToEndTime(Long.toString(longestEndTime - eventStart));
		}
		if (dnsEnd > dnsStart) {
			con.setDnsTime(Long.toString(dnsEnd - dnsStart));
		}
		con.setStartTimeStamp(Long.toString(connectionStart));
		event.setConnection(con);
		return event;
	}

	private Event buildRedirectEvent(int redirCount, int respCode) {
		Event event = new Event();
		event.setRedirectCount(redirCount);
		event.setUrl(eventUrl.toString());
		event.setAvailability(true);
		event.setResultCode(respCode);
		event.setResultDesc("Redirect");
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
		event.setConnection(con);
		return event;
	}
}
