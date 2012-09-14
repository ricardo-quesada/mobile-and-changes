package com.marlin.webclient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class WebClientTest {

	public static void main(String[] args) throws Exception {

		//String url = "http://www.google.com/mobile/";
//		String url = "http://m.rediff.com/";
//		String url = "http://mobile.marlinmobile.com";
//		String url = "http://www.shopadidas.com/home/index.jsp?cm_mmc=Adidas_USeCom-_-USBrnd-_-Redirect-_-Mobile";
		String url = "http://www.hp.com/supportforum";
		StringWriter sw = new StringWriter();
		HttpURLConnection con = null;
		InputStream ins = null;
		String encoding = null;
		try {
			URL myurl = new URL(url);
			// try 3 times to connect
			int respCode = 0;
			for (int i = 0; i < 3; i++) {
				con = (HttpURLConnection) myurl.openConnection();
				CookieHandler.setDefault(new CookieManager());

				//con.setInstanceFollowRedirects(false);
//				con.setRequestProperty ( "User-agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7A341 Safari/528.16");
				con.setRequestProperty("accept-encoding","gzip");
				con.setRequestProperty("accept-language","en-US");
				con.setRequestProperty("x-wap-profile","http://uaprof.vtext.com/adr62k/adr62k.xml");
				con.setRequestProperty("user-agent","Mozilla/5.0 (Linux; U; Android 2.1; en-us; ADR6200 Build/ERD79) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17");
				con.setRequestProperty("accept","application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5,application/youtube-client");
				con.setRequestProperty("accept-charset","utf-8, iso-8859-1, utf-16, *;q=0.7");
				respCode = con.getResponseCode();
				encoding = con.getContentEncoding();
				if (con.getResponseCode() != -1) {
					break;
				}
			}
			if (respCode == 200) {
				con.setDefaultUseCaches(false);
				con.setConnectTimeout(60000);
				if ("gzip".equals(encoding)) {
					ins = new GZIPInputStream(con.getInputStream());
				} else {
					ins = con.getInputStream();
				}
				int c;
				while ((c = ins.read()) != -1) {
					sw.write(c);
				}

				ins.close();
				con.disconnect();
			} else {
				System.err.println("Response Code:" + respCode);
			}
		} catch (FileNotFoundException fnfe) {
			sw.write("Error: " + fnfe.getMessage());
		} catch (Exception e) {
			sw.write("Error: " + e.getMessage());
		} finally {
			if (ins != null) {
				try {
					ins.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (con != null) {
				con.disconnect();
			}
		}
		System.err.println(sw.toString());
		HtmlDocument doc = new HtmlDocument(new URL(url), sw.toString()
				.getBytes());
		Set<URL> elements = new HashSet<URL>();
		for(URL u : doc.getPageElementLinks()) {
			elements.add(u);
		}
		System.err.println(elements);
	}
}