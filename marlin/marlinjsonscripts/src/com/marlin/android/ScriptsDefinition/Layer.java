/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.marlin.android.ScriptsDefinition;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

/**
 *
 * @author rquesada
 */
public class Layer  implements Serializable
{
    private String LayerName;
    private Map<String, String> LayerBody;

    /**
     * @return the LayerName
     */
    public String getLayerName() {
        return LayerName;
    }

    /**
     * @param LayerName the LayerName to set
     */
    public void setLayerName(String LayerName) {
        this.LayerName = LayerName;
    }

    /**
     * @return the LayerBody
     */
    public Map<String, String> getLayerBody() {
        return LayerBody;
    }

    /**
     * @param dic the LayerBody to set
     */
    public void setLayerBody(Map<String, String> dic) {
        this.LayerBody = dic;
    }
}
