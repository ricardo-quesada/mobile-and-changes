package com.marlin.webclient;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

/**
 * This class implements an HTML document
 * 
 * It uses JTidy to parse the given HTML code to an internal DOM representation.
 * 
 */
public class HtmlDocument {

	/** URL of this document */
	private URL url = null;

	/** Content text as an array of bytes (this is how we get it from HTTP !) */
	private byte[] content = null;

	/** the DOM representation of this HTML document */
	private Document domDoc = null;

	/** encoding */
	private String encoding;

	/** Base URL */
	private URL baseURL = null;

	/** All links */
	Vector<URL> links;

	/**
	 * initializes HTML document without content
	 */
	private HtmlDocument(URL url) {
		this.url = url;
	}

	/**
	 * Initializes an HTML document with the given content.
	 * 
	 * @param url
	 *            the URL of this document. Needed for link extraction.
	 * @param content
	 *            some HTML text as an array of bytes
	 */
	public HtmlDocument(URL url, byte[] content) {
		this(url);
		this.content = content;
		parse();
	}

	/**
	 * Initializes an HTML document with the given content.
	 * 
	 * @param url
	 *            the URL of this document. Needed for link extraction.
	 * @param content
	 *            some HTML text as an array of bytes
	 * @param newEncoding
	 *            Is the encoding of the content.
	 */
	public HtmlDocument(URL url, byte[] content, String newEncoding) {
		this(url);
		this.content = content;
		encoding = newEncoding;
		parse();
	}

	/**
	 * Initalizes an HTML document from a String. Convert string to bytes using
	 * default encoding
	 */
	public HtmlDocument(URL url, String contentStr) {
		this(url);
		this.content = new byte[contentStr.length() + 1];
		for (int i = 0; i < contentStr.length(); i++) {
			this.content[i] = (byte) contentStr.charAt(i);
		}
		parse();
	}

	/**
	 * Extracts all links to other documents from this HTML document.
	 * 
	 * @return a Vector of URLs containing the included links
	 */
	private void parse() {
		if (domDoc == null) {
			parseToDOM();
		}
		this.links = new Vector<URL>();
		if (domDoc != null) {
			extractLinks(domDoc.getDocumentElement(), links);
		}
	}

	public Vector<URL> getLinks() {
		return this.links;
	}

	/**
	 * Extracts all links to included images from this HTML document.
	 * 
	 * @return a Vector of URLs containing the included links
	 */
	public Vector getImageLinks() {
		if (domDoc == null) {
			parseToDOM();
		}
		Vector<URL> links = new Vector<URL>();
		extractImageLinks(domDoc.getDocumentElement(), links);

		return links;
	}

	/**
	 * Extracts all page elements included from this HTML document.
	 * 
	 * @return a Vector of URLs containing the included links
	 */
	public Vector<URL> getPageElementLinks() {
		if (domDoc == null) {
			parseToDOM();
		}
		Vector<URL> links = new Vector<URL>();
		if (domDoc != null) {
			extractPageElementLinks(domDoc.getDocumentElement(), links);
		}

		return links;
	}

	/**
	 * gets all Element nodes of a given type as a Vector
	 * 
	 * @param type
	 *            the type of elements to return. e.g. type="a" will return all
	 *            <A> tags. type must be lowercase
	 * @return a Vector containing all element nodes of the given type
	 */
	public Vector getElements(String type) {
		if (domDoc == null) {
			parseToDOM();
		}

		Vector<Element> links = new Vector<Element>();
		extractElements(domDoc.getDocumentElement(), type, links);

		return links;
	}

	/**
	 * Extract page element links from the given DOM subtree and put it into the
	 * given vector.
	 * 
	 * @param element
	 *            the top level DOM element of the DOM tree to parse
	 * @param links
	 *            the vector that will store the links
	 */
	protected void extractPageElementLinks(Element element, Vector<URL> links) {

		// this should not happen !
		if (element == null) {
			Log.e("Marlin", "got a null element");
			return;
		}

		String name = element.getNodeName().toLowerCase();

		if (name.equals("frame")) {

			// FRAME SRC=
			addLink(element.getAttribute("src"), links);

			// handle internal frame (iframes) as well
		} else if (name.equals("iframe")) {

			// IFRAME SRC=
			addLink(element.getAttribute("src"), links);

		} else if (name.equals("link")) {

			// LINK HREF=
			addLink(element.getAttribute("href"), links);

		} else if (name.equals("image")) {

			// IMAGEG SRC= (incorrect, but seems to work in some browsers :(
			addLink(element.getAttribute("src"), links);

		} else if (name.equals("img")) {

			// IMG SRC=
			addLink(element.getAttribute("src"), links);

		} else if (name.equals("meta")) {

			// META HTTP-EQUIV=REFRESH
			String equiv = element.getAttribute("http-equiv");
			if ((equiv != null) && (equiv.equalsIgnoreCase("refresh"))) {
				String refreshcontent = element.getAttribute("content");
				if (refreshcontent == null) {
					refreshcontent = "";
				}

				StringTokenizer st = new StringTokenizer(refreshcontent, ";");
				while (st.hasMoreTokens()) {
					String token = st.nextToken().trim();
					AttribValuePair av = new AttribValuePair(token);
					if (av.getAttrib().equals("url")) {
						addLink(av.getValue(), links);
					}
				}
			}

		} else if (name.equals("body")) {
			// BODY BACKGROUND=
			String background = element.getAttribute("background");
			if (!(background == null) || (background.equals(""))) {
				addLink(background, links);
			}

		} else {
			// Log.d("Marlin", "Ignore tag name: " + name);
		}

		// recursive travel through all childs
		NodeList childs = element.getChildNodes();

		for (int i = 0; i < childs.getLength(); i++) {
			if (childs.item(i) instanceof Element) {
				extractPageElementLinks((Element) childs.item(i), links);
			}
		}

	}

	/**
	 * Extract links from the given DOM subtree and put it into the given
	 * vector.
	 * 
	 * @param element
	 *            the top level DOM element of the DOM tree to parse
	 * @param links
	 *            the vector that will store the links
	 */
	protected void extractLinks(Element element, Vector<URL> links) {

		// this should not happen !
		if (element == null) {
			Log.e("Marlin", "got a null element");
			return;
		}

		String name = element.getNodeName().toLowerCase();

		if (name.equals("a")) {

			// A HREF=
			addLink(element.getAttribute("href"), links);

		} else if (name.equals("base")) {

			// BASE HREF=
			try {
				this.baseURL = new URL(element.getAttribute("href"));
				//Log.i("Marlin", "baseUR=" + baseURL);
			} catch (MalformedURLException e) {
			}

		} else if (name.equals("frame")) {

			// FRAME SRC=
			addLink(element.getAttribute("src"), links);

			// handle internal frame (iframes) as well
		} else if (name.equals("iframe")) {

			// IFRAME SRC=
			addLink(element.getAttribute("src"), links);

		} else if (name.equals("image")) {

			// IMAGEG SRC= (incorrect, but seems to work in some browsers :(
			addLink(element.getAttribute("src"), links);

		} else if (name.equals("img")) {

			// IMG SRC=
			addLink(element.getAttribute("src"), links);

		} else if (name.equals("area")) {

			// AREA HREF=
			addLink(element.getAttribute("href"), links);

		} else if (name.equals("meta")) {

			// META HTTP-EQUIV=REFRESH
			String equiv = element.getAttribute("http-equiv");
			if ((equiv != null) && (equiv.equalsIgnoreCase("refresh"))) {
				String refreshcontent = element.getAttribute("content");
				if (refreshcontent == null) {
					refreshcontent = "";
				}

				StringTokenizer st = new StringTokenizer(refreshcontent, ";");
				while (st.hasMoreTokens()) {
					String token = st.nextToken().trim();
					AttribValuePair av = new AttribValuePair(token);
					if (av.getAttrib().equals("url")) {
						addLink(av.getValue(), links);
					}
				}
			}

		} else if (name.equals("body")) {
			// BODY BACKGROUND=
			String background = element.getAttribute("background");
			if (!(background == null) || (background.equals(""))) {
				addLink(background, links);
			}

		} else {
			//Log.i("Marlin", "Ignore tag name: " + name);
		}

		// recursive travel through all childs
		NodeList childs = element.getChildNodes();

		for (int i = 0; i < childs.getLength(); i++) {
			if (childs.item(i) instanceof Element) {
				extractLinks((Element) childs.item(i), links);
			}
		}

	}

	/**
	 * Extract links to includes images from the given DOM subtree and put them
	 * into the given vector.
	 * 
	 * @param element
	 *            the top level DOM element of the DOM tree to parse
	 * @param links
	 *            the vector that will store the links
	 */
	protected void extractImageLinks(Element element, Vector<URL> links) {

		// this should not happen !
		if (element == null) {
			Log.e("Marlin", "got a null element");
			return;
		}

		String name = element.getNodeName();

		if (name.equals("img")) {
			// IMG SRC=
			addLink(element.getAttribute("src"), links);
		}

		if (name.equals("image")) {
			// IMAGE SRC=
			addLink(element.getAttribute("src"), links);
		}

		// recursive travel through all childs
		NodeList childs = element.getChildNodes();

		for (int i = 0; i < childs.getLength(); i++) {
			if (childs.item(i) instanceof Element) {
				extractImageLinks((Element) childs.item(i), links);
			}
		}

	}

	/**
	 * Extract elements from the given DOM subtree and put it into the given
	 * vector.
	 * 
	 * @param element
	 *            the top level DOM element of the DOM tree to parse
	 * @param type
	 *            HTML tag to extract (e.g. "a", "form", "head" ...)
	 * @param elementList
	 *            the vector that will store the elements
	 */
	protected void extractElements(Element element, String type,
			Vector<Element> elementList) {

		// this should not happen !
		if (element == null) {
			Log.e("Marlin", "got a null element");
			return;
		}

		String name = element.getNodeName();

		if (name.equals(type)) {
			elementList.add(element);
		}

		// recursive travel through all childs
		NodeList childs = element.getChildNodes();

		for (int i = 0; i < childs.getLength(); i++) {
			if (childs.item(i) instanceof Element) {
				extractElements((Element) childs.item(i), type, elementList);
			}
		}

	}

	/**
	 * parses the document to a DOM tree using Tidy
	 */
	private void parseToDOM() {
		ByteArrayInputStream is = new ByteArrayInputStream(content);

		// set tidy parameters
		Tidy tidy = new Tidy();
		tidy.setUpperCaseTags(false);
		tidy.setUpperCaseAttrs(false);

		domDoc = tidy.parseDOM(is, null);
	}

	/**
	 * adds a links to the given vector. ignores (but logs) possible errors
	 */
	private void addLink(String newURL, Vector<URL> links) {

		// remove part after # from the URL
		// thanks to Johannes Christen for bug fix.
		if ((newURL == null) || (newURL.equals("")))
			return;
		int pos = newURL.indexOf("#");
		if (pos >= 0) {
			newURL = newURL.substring(0, pos);
		}

		if (encoding != null) {
			try {
				newURL = new String(newURL.getBytes(), encoding);
			} catch (UnsupportedEncodingException e) {
			}
		} else {
			try {
				newURL = new String(newURL.getBytes(), "ISO-8859-1");
			} catch (UnsupportedEncodingException e) {
			}
		}

		try {
			URL u = null;
			if (this.baseURL != null) {
				u = new URL(this.baseURL, newURL);
			} else {
				u = new URL(url, newURL);
			}
			links.add(u);
		} catch (Exception e) {
			Log.d("Marlin", "error during link extraction: " + e.getMessage()
					+ " " + newURL);
		}
	}

	public URL getBaseURL() {
		return baseURL;
	}

}
