package com.spozebra.dwconfigsample.config

import android.os.Bundle

class DWPlugin {
    var pluginName : String = ""
    var resetConfig : Boolean = false
    private var _paramList : HashMap<String, DWParamValue> = HashMap()

    fun addParam(name : String, value : String?){
        if(value != null)
            _paramList[name] = DWParamValue(value, false)
    }
    fun updateParam(name : String, value : String?){
        if(value != null)
            _paramList[name] = DWParamValue(value, true)
    }

    fun getParam(name : String): String? {
        return _paramList[name]?.value
    }

    fun getAllParams(): List<String?> {
        return _paramList.keys.toList();
    }

    fun getParamasUpdatdCount(): Int {
        return _paramList.values.count { x -> x.changed }
    }

    internal fun buildPluginBundle() : Bundle {
        // Create plugin
        val pluginBundle = Bundle()
        pluginBundle.putString("PLUGIN_NAME", pluginName)
        pluginBundle.putString("RESET_CONFIG", if(resetConfig) "true" else "false")

        // Add properties
        val paramsBundle = Bundle()
        for(param in _paramList.filter { x -> x.value.changed }){
            paramsBundle.putString(param.key, param.value.value)
            param.value.changed = false
        }
        pluginBundle.putBundle("PARAM_LIST", paramsBundle)

        return pluginBundle
    }

}