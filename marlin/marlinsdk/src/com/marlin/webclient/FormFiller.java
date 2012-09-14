package com.marlin.webclient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class fills out form fields by predefined values
 */
public class FormFiller {

	/**
	 * Form Handlers
	 * 
	 * @link aggregation
	 * @associates <{FormHandler}>
	 */
	private Vector formHandlers = null;

	/**
	 * Basic initialization
	 */
	public FormFiller() {
	}

	/**
	 * Initializes the form filler with a given list of form handlers
	 * 
	 * @param formHandlers
	 *            a Vector containing FormHandler objects
	 */
	public FormFiller(Vector formHandler) {
		this();
		this.formHandlers = formHandler;
	}

	/**
	 * Sets the list of form handlers
	 * 
	 * @param formHandlers
	 *            a Vector containing FormHandler objects
	 */
	public void setFormHandlers(Vector formHandlers) {
		this.formHandlers = formHandlers;
	}

	/**
	 * Gets the list of form handlers
	 * 
	 * @return a Vector containing FormHandler objects
	 */
	public Vector getFormHandlers() {
		return this.formHandlers;
	}

	/**
	 * Tries to fill the given form with values
	 * 
	 * @param baseURL
	 *            the URL of the form itself. needed for relative adressing
	 * @param form
	 *            a element node containing a DOM description of a form (e.g.
	 *            from a DOM parser or HTML Tidy)
	 * 
	 * @return a form filled with values or null, if no form handler was found
	 */
	public ExtendedURL fillForm(URL baseURL, Element form) {
		ExtendedURL eurl = new ExtendedURL();
		String formURL = form.getAttribute("action");
		String type = form.getAttribute("method");
		FormHandler handler;
		URL absoluteFormURL = null;

		try {
			absoluteFormURL = new URL(baseURL, formURL);
		} catch (MalformedURLException e) {
			Log.i("Marlin", "MalformedURLException in fillForm(): "
					+ e.getMessage());
		}

		if (!form.getNodeName().equals("form")) {
			Log.e("Marlin", "not a form !");
			return null;
		}

		handler = getFormHandler(absoluteFormURL.toString());
		if (handler == null) {
			Log.d("Marlin", "found no form handler for URL " + formURL);
			return null;
		}

		if (type.equalsIgnoreCase("get")) {
			eurl.setRequestMethod(HttpConstants.GET);
		} else if (type.equalsIgnoreCase("post")) {
			eurl.setRequestMethod(HttpConstants.POST);
		} else if (type.equals("")) {
			// workaround for sites that have no "action" attribute
			// in their forms, like Google :-(
			eurl.setRequestMethod(HttpConstants.GET);
		} else {
			Log.d("Marlin", "method " + type + " unknown");
			return null;
		}

		try {
			eurl.setURL(absoluteFormURL);
		} catch (Exception e) {
			Log.d("Marlin", "error calculating URL: " + e.getMessage());
		}

		// clear the old data in this form handler
		handler.clearValues();

		// okay, now fill the form fields ...
		collectInputFields(form, handler);
		eurl.setParams(handler.getParamString());

		return eurl;
	}

	/**
	 * Get
	 */
	private void collectInputFields(Element element, FormHandler fh) {
		// this should not happen !
		if (element == null) {
			Log.e("Marlin", "got a null element");
			return;
		}

		if (element.getNodeName().equals("input")) {

			String type = element.getAttribute("type").toLowerCase();
			String name = element.getAttribute("name");
			String value = element.getAttribute("value");

			// ignore reset tags
			if (!type.equals("reset")) {

				// must have a name
				if ((name != null) && (!name.equals(""))) {

					// must have a value
					if ((value != null) && (!value.equals(""))) {

						// add this value to the form handler
						fh.addValue(name, value);

					}

				}

			}

		}

		// recursive travel through all childs
		NodeList childs = element.getChildNodes();

		for (int i = 0; i < childs.getLength(); i++) {
			if (childs.item(i) instanceof Element) {
				collectInputFields((Element) childs.item(i), fh);
			}
		}

	}

	/**
	 * Gets a form handler for a given URL
	 * 
	 * @param u
	 *            an URL
	 * @return a FormHandler object, if there is a registered FormHandler for
	 *         this URL, null otherwise
	 */
	protected FormHandler getFormHandler(String url) {
		if (url == null) {
			return null;
		}

		if (formHandlers == null) {
			return null;
		}

		for (int i = 0; i < formHandlers.size(); i++) {
			FormHandler fh = (FormHandler) formHandlers.elementAt(i);
			if (fh.getUrl().toString().equals(url)) {
				return fh;
			}
		}

		return null;
	}

}
