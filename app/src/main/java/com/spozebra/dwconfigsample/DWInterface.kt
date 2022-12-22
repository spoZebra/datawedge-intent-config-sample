package com.spozebra.dwconfigsample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.spozebra.dwconfigsample.config.DWPlugin
import com.spozebra.dwconfigsample.config.DWProfile


class DWInterface(private val context: Context) {
    private val DEFAULT_PROFILE_NAME = "DWConfigSampleProfile"
    private val ACTION_DATAWEDGE = "com.symbol.datawedge.api.ACTION"

    private val EXTRA_GET_VERSION =  "com.symbol.datawedge.api.GET_VERSION_INFO"
    private val EXTRA_GET_CONFIG = "com.symbol.datawedge.api.GET_CONFIG"
    private val EXTRA_GET_PROFILES =  "com.symbol.datawedge.api.GET_PROFILES_LIST"

    private val EXTRA_SET_CONFIG = "com.symbol.datawedge.api.SET_CONFIG"


    fun setupAppDefaultProfile() {
        val profile = DWProfile(DEFAULT_PROFILE_NAME)
        profile.addApp(context.packageName)
        profile.configMode = "CREATE_IF_NOT_EXIST"

        var profileBundle = profile.buildProfile()
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileBundle)
    }

    fun setupAppDefaultIntents(){
        val profile = DWProfile(DEFAULT_PROFILE_NAME)
        profile.configMode = "UPDATE"
        profile.pluginConfig = DWPlugin()
        profile.pluginConfig.pluginName = "INTENT"
        profile.pluginConfig.resetConfig = true
        profile.pluginConfig.updateParam("scanner_selection", "auto")
        profile.pluginConfig.updateParam("scanner_input_enabled", "true")
        profile.pluginConfig.updateParam("intent_output_enabled", "true")
        profile.pluginConfig.updateParam("intent_action", "${context.packageName}.ACTION")
        profile.pluginConfig.updateParam("intent_delivery", "2")

        var profileBundle = profile.buildProfile()
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileBundle)
    }
    fun updateProfile(profile : DWProfile, plugin : DWPlugin){
        profile.configMode = "UPDATE"
        profile.pluginConfig = plugin

        var profileBundle = profile.buildProfile()
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileBundle)
    }

     fun getVersionInfo(){
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_GET_VERSION, "")
    }

    fun getProfiles(){
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_GET_PROFILES, "")
    }

     fun getProfileConfiguration(profileName : String){
        val profileBundle = Bundle()
        profileBundle.putString("PROFILE_NAME", profileName)
        val pluginName: ArrayList<String> = ArrayList()
        pluginName.add("BARCODE")
        pluginName.add("INTENT")

        val bConfig = Bundle()
        bConfig.putStringArrayList("PLUGIN_NAME", pluginName)
        profileBundle.putBundle("PLUGIN_CONFIG", bConfig)

        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_GET_CONFIG, profileBundle)
    }

    private fun sendDataWedgeIntentWithExtra(action: String, extraKey: String, extras: Bundle) {
        val dwIntent = Intent()
        dwIntent.action = action
        dwIntent.putExtra(extraKey, extras)
        context.sendBroadcast(dwIntent)
    }
    private fun sendDataWedgeIntentWithExtra(action: String, extraKey: String, extraValue: String) {
        val dwIntent = Intent()
        dwIntent.action = action
        dwIntent.putExtra(extraKey, extraValue)
        context.sendBroadcast(dwIntent)
    }
}