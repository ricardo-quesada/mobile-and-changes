/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.marlin.android.ScriptsDefinition;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.List;

/**
 *
 * @author rquesada
 */
public class Layer  implements Serializable
{
    private String LayerName;
    private Dictionary<String, String> LayerBody;

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
    public Dictionary<String, String> getLayerBody() {
        return LayerBody;
    }

    /**
     * @param LayerBody the LayerBody to set
     */
    public void setLayerBody(Dictionary<String, String> LayerBody) {
        this.LayerBody = LayerBody;
    }
}
