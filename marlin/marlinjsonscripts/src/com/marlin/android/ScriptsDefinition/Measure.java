/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.marlin.android.ScriptsDefinition;

import java.io.Serializable;
import java.util.Date;
/**
 *
 * @author rquesada
 */
public class Measure  implements Serializable
{
    private MeasureCategory Category;
    private MeasureType Type;
    private String MeasureItem;
    private String Value;
    private Date StarTime;
    private Date EndTime;
    private Metric Metric;        

    /**
     * @return the Type
     */
    public MeasureType getType() {
        return Type;
    }

    /**
     * @param Type the Type to set
     */
    public void setType(MeasureType Type) {
        this.Type = Type;
    }

    /**
     * @return the MeasureItem
     */
    public String getMeasureItem() {
        return MeasureItem;
    }

    /**
     * @param MeasureItem the MeasureItem to set
     */
    public void setMeasureItem(String MeasureItem) {
        this.MeasureItem = MeasureItem;
    }

    /**
     * @return the Value
     */
    public String getValue() {
        return Value;
    }

    /**
     * @param Value the Value to set
     */
    public void setValue(String Value) {
        this.Value = Value;
    }

    /**
     * @return the Metric
     */
    public Metric getMetric() {
        return Metric;
    }

    /**
     * @param Metric the Metric to set
     */
    public void setMetric(Metric Metric) {
        this.Metric = Metric;
    }
}
