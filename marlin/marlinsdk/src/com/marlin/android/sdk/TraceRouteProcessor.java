package com.marlin.android.sdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.util.Log;

public class TraceRouteProcessor {

	private long timeOut;
	private int noHops;

	public TraceRouteProcessor(int noHops, long timeOut) {
		super();
		this.noHops = noHops;
		this.timeOut = timeOut;
	}

	/**
	 * @param hostNames
	 *            , a set of link URLs (Can not be null)
	 * @return List<Connection>
	 * @throws InterruptedException
	 */
	protected List<TraceRouteData> multiProcessRequest(Set<String> hostNames)
			throws InterruptedException {

		if (hostNames.isEmpty()) {
			return null;
		}

		List<String> hostList = new ArrayList<String>(hostNames);

		// create a thread for each URI
		TraceRouteThread[] threads = new TraceRouteThread[hostNames.size()];

		for (int i = 0; i < threads.length; i++) {
			threads[i] = new TraceRouteThread(hostList.get(i), noHops);
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
		List<TraceRouteData> resp = new ArrayList<TraceRouteData>(
				threads.length);

		for (int i = 0; i < threads.length; ++i) {
			TraceRouteData trData = threads[i].getResult();
			if (trData != null) {
				resp.add(trData);
				Log.d("Marlin", getClass().getName() + ": TraceRouteData for "
						+ trData.getHostName() + ": " + trData);
			}
		}

		return resp;
	}
}
