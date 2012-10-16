package com.marlin.webclient;

import java.net.*;
import java.util.*;

/**
 * This class implements a stores settings for web forms.
 * It is used by the WebRobot to fill form field with
 * predefined values.
 *
 * <b>FormHandler is not thread safe</b>. That means you can't 
 * use the same formHandler in different tasks with different addValue() calls,
 * because there is only one internal array to store those values.
 */
public class FormHandler {

  /**
   * initializes a new FormHandler without any settings
   */
  public FormHandler() {
    defaults=new Vector<FormField>();
    clearValues();
  }



  /**
   * add a new default value for this form handler
   */
  public void addDefault(String fieldname, String value) {
    FormField ff = new FormField();
    ff.setFieldname(fieldname);
    ff.setValue(value);
    defaults.add(ff);
  }

  /**
   * add a new value for this form handler
   */
  public void addValue(String fieldname, String value) {
    FormField ff = new FormField();
    ff.setFieldname(fieldname);
    ff.setValue(value);
    values.add(ff);
  }

  /**
   * if we have added values before, this function removed all 
   * values (e.g. to retrieve a new document with a different
   * set of values)
   */
  public void clearValues() {
    values = new Vector<FormField>();
  }
  
  public String getUrl() { 
    //return url.toString(); 
    return url; 
  }
  
  public void setUrl(String u) 
    throws MalformedURLException
  { 
    //this.url=new URL(u); 
    this.url = u;
  }


  /**
   * construct an encoded string of all attributes and their values
   * to process this form
   */
  public String getParamString() {
    StringBuffer sb = new StringBuffer();

    // first, use the defaults
    for (int i=0; i<defaults.size(); i++) {
      FormField ff = (FormField)defaults.elementAt(i);
      // default overidden by another value ?
      if (! hasValue(ff.getFieldname())) {
	sb.append(ff.toEncodedString());
	sb.append('&');
      }
    }

    // now add the values
    for (int i=0; i<values.size(); i++) {
      FormField ff = (FormField)values.elementAt(i);
      sb.append(ff.toEncodedString());
      sb.append('&');
    }

    // remove the last "&"
    sb.deleteCharAt(sb.length()-1);

    return sb.toString();
  }
 
  /**
   * look, if we have a value for this attribute or if
   * we should use the default
   */
  protected boolean hasValue(String attrib) {
    for (int i=0; i<values.size(); i++) {
      FormField ff = (FormField)values.elementAt(i);
      if (ff.getFieldname().equals(attrib)) {
	return true;
      }
    }
    return false;
  }

  public int getMethod() { 
    return method; 
  }  

  public void setMethod(int method) {
    this.method = method; 
  }
  
  /**
   * Get the value of defaults.
   * @return Value of defaults.
   */
  public Vector getDefaults() {
    return defaults;
  }
  
  /**
   * Set the value of defaults.
   * @param v  Value to assign to defaults.
   */
  public void setDefaults(Vector<FormField> v) {
    this.defaults = v;
  }


  /**
   * Get the value of values.
   * @return Value of values.
   */
  public Vector getValues() {
    return values;
  }

  
  /**
   * Set the value of values.
   * @param v  Value to assign to values.
   */
  public void setValues(Vector<FormField>  v) {
    this.values = v;
  }
  

  /**
   * GET or POST ?
   */
  private int method;


  /**
   *@link aggregation
   *     @associates <{FormField}>*/
  private Vector<FormField> defaults;

  // since not allways a full url is given, this solution is better.
  // a set URL with "/search" would throw a MalformatException
  private String url;
  //private URL url;

  /**
   *@link aggregation
   *     @associates <{FormField}>*/
  private Vector<FormField> values;
}
