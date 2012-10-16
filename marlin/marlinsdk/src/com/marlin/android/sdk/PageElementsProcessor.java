package com.marlin.android.sdk;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class PageElementsProcessor {

	private long timeOut;
	private CookieManager cookieManager;
	private HashMap<String, String> headers = null;

	private PageElementsProcessor() {
		super();
	}

	public PageElementsProcessor(CookieManager cm,
			HashMap<String, String> hdrs, long timeOut) {
		super();
		this.timeOut = timeOut;
		this.cookieManager = cm;
		this.headers = hdrs;
	}

	/**
	 * @param urlSet
	 *            , a set of link URLs (Can not be null)
	 * @return List<Connection>
	 * @throws InterruptedException
	 */
	protected List<PageElement> multiProcessRequest(Set<URL> urlSet)
			throws InterruptedException {

		if (urlSet.isEmpty()) {
			return null;
		}

		List<URL> urlList = new ArrayList<URL>(urlSet);

		// create a thread for each URI
		GetThread[] threads = new GetThread[urlSet.size()];

		for (int i = 0; i < threads.length; i++) {
			threads[i] = new GetThread(urlList.get(i), i + 1, cookieManager,
					headers);
		}

		// start the threads
		for (int j = 0; j < threads.length; j++) {
			threads[j].start();
		}

		// join the threads
		for (int j = 0; j < threads.length; j++) {
			if (timeOut > 0) {
				threads[j].join(timeOut);
			}
		}

		/* Prepare the return. */
		List<PageElement> resp = new ArrayList<PageElement>(threads.length);

		for (int i = 0; i < threads.length; ++i) {
			List<PageElement> eleData = threads[i].getResult();
			if (eleData != null && eleData.size() > 0) {
				resp.addAll(eleData);
			} else {
				// May be we timed out before the thread finished.
				// create an empty element for tracking
				PageElement pe = new PageElement();
				pe.setElementUrl(threads[i].getUrl().toString());
				pe.setElementId(threads[i].getUrl().getPath());
				pe.setResultCode(-2);
				pe.setResultDesc("Did not complete within the timeout period.");
				resp.add(pe);
			}
		}

		return resp;
	}
}
