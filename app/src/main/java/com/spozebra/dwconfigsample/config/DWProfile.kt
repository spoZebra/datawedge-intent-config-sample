package com.spozebra.dwconfigsample.config

import android.os.Bundle

class DWProfile(name : String) {
    var profileName : String = name
    var profileEnabled : Boolean = true
    var configMode : String = ""
    var pluginConfig : DWPlugin = DWPlugin()
    private var _associatedApps : MutableList<String> = mutableListOf()
    private var updateApps : Boolean = false

    fun addApp(packageName : String){
        _associatedApps.add(packageName)
        updateApps = true
    }

     fun buildProfile() : Bundle {
        val profileBundle = Bundle()
        profileBundle.putString("PROFILE_NAME", profileName)
        profileBundle.putString("PROFILE_ENABLED", if(profileEnabled) "true" else "false")
        profileBundle.putString("CONFIG_MODE", configMode)

         if(updateApps) {
             val appBundle = Bundle()
             for (app in _associatedApps)
                 appBundle.putString("PACKAGE_NAME", app)
             appBundle.putStringArray("ACTIVITY_LIST", arrayOf("*"))
             profileBundle.putParcelableArray("APP_LIST", arrayOf(appBundle))
             updateApps = false
         }

         if(pluginConfig.pluginName != "") {
             val pluginBundle = pluginConfig.buildPluginBundle();
             profileBundle.putBundle("PLUGIN_CONFIG", pluginBundle)
         }
        return profileBundle
    }
}