/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.marlin.android.ScriptsDefinition;

import java.io.Serializable;
/**
 *
 * @author rquesada
 */
public class Step  implements Serializable{
    private int Number;
    private ActionType Type;
    private int waitTime;
    private String Object;
    private String Method;
    private String ExtendedSpec;

    /**
     * @return the Number
     */
    public int getNumber() {
        return Number;
    }

    /**
     * @param Number the Number to set
     */
    public void setNumber(int Number) {
        this.Number = Number;
    }

    /**
     * @return the Type
     */
    public ActionType getType() {
        return Type;
    }

    /**
     * @param Type the Type to set
     */
    public void setType(ActionType Type) {
        this.Type = Type;
    }

    /**
     * @return the waitTime
     */
    public int getWaitTime() {
        return waitTime;
    }

    /**
     * @param waitTime the waitTime to set
     */
    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    /**
     * @return the Object
     */
    public String getObject() {
        return Object;
    }

    /**
     * @param Object the Object to set
     */
    public void setObject(String Object) {
        this.Object = Object;
    }

    /**
     * @return the Method
     */
    public String getMethod() {
        return Method;
    }

    /**
     * @param Method the Method to set
     */
    public void setMethod(String Method) {
        this.Method = Method;
    }

    /**
     * @return the ExtendedSpec
     */
    public String getExtendedSpec() {
        return ExtendedSpec;
    }

    /**
     * @param ExtendedSpec the ExtendedSpec to set
     */
    public void setExtendedSpec(String ExtendedSpec) {
        this.ExtendedSpec = ExtendedSpec;
    }
}
