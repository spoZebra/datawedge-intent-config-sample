package com.spozebra.dwconfigsample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.spozebra.dwconfigsample.config.DWPlugin
import com.spozebra.dwconfigsample.config.DWProfile


class MainActivity : AppCompatActivity() {
    var profileList = listOf<DWProfile>()
    var pluginList = listOf<DWPlugin>()

    private lateinit var editTextLog : EditText
    private lateinit var progressBar : ProgressBar

    private lateinit var spinnerProfile : Spinner
    private lateinit var spinnerPlugin : Spinner
    private lateinit var spinnerParameter : Spinner
    private lateinit var editTextParamValue : EditText
    private lateinit var applyButton : Button

    private val dataWedgeReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            // NEW Barcode scanned
            if (action == "com.spozebra.dwconfigsample.ACTION") {
                val decodedData: String? = intent.getStringExtra("com.symbol.datawedge.data_string")
                this@MainActivity.newBarcodeScanned(decodedData)
            }
            // DW Api result
            else if (action.equals("com.symbol.datawedge.api.RESULT_ACTION")) {
                // Get version
                if (intent.hasExtra("com.symbol.datawedge.api.RESULT_GET_VERSION_INFO")) {
                    val res = intent.getBundleExtra("com.symbol.datawedge.api.RESULT_GET_VERSION_INFO")
                    val dwVersion = res!!.getString("DATAWEDGE")
                    val decoderVersion = res.getString("DECODER_LIBRARY")
                    val fullVersion = "DataWedge:$dwVersion\nDecoderLib:$decoderVersion"
                    this@MainActivity.dwVersionReceived(fullVersion)
                }
                // Get profiles list
                else if (intent.hasExtra("com.symbol.datawedge.api.RESULT_GET_PROFILES_LIST")) {
                    var profilesList = intent.getStringArrayExtra ("com.symbol.datawedge.api.RESULT_GET_PROFILES_LIST")
                    this@MainActivity.dwProfilesReceived(profilesList!!.map { x -> DWProfile(x) }.toList())
                }
                // Get configuration
                else if (intent.hasExtra("com.symbol.datawedge.api.RESULT_GET_CONFIG")) {
                    val pluginList = ArrayList<DWPlugin>()

                    val resultGetConfig = intent.getBundleExtra("com.symbol.datawedge.api.RESULT_GET_CONFIG")
                    val resultConfigKeys = resultGetConfig!!.keySet()

                    for (resultKey in resultConfigKeys) {
                        if (resultKey.equals("PLUGIN_CONFIG")) {
                            val bundleArrayList = resultGetConfig.getParcelableArrayList<Bundle>("PLUGIN_CONFIG")
                            var plugin = DWPlugin()

                            for (configBundle in bundleArrayList!!) {
                                if(configBundle.getString("PLUGIN_NAME") != null) {
                                    plugin.pluginName = configBundle.getString("PLUGIN_NAME")!!
                                }
                                for (configBundleParam in configBundle.keySet()) {
                                    if (configBundleParam.equals("PARAM_LIST")) {
                                        val paramList = configBundle.getBundle("PARAM_LIST")
                                        if (paramList != null) {
                                            for (paramKey in paramList.keySet()) {
                                                plugin.addParam(paramKey, paramList.getString(paramKey))
                                            }
                                        }
                                    }
                                }
                                pluginList.add(plugin)
                                plugin = DWPlugin()
                            }
                        }
                    }

                    this@MainActivity.dwConfigReceived(pluginList)
                }
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextLog = findViewById(R.id.editTextTextLog)
        progressBar = findViewById(R.id.progressBar)

        spinnerProfile = findViewById(R.id.spinnerProfile)
        spinnerPlugin = findViewById(R.id.spinnerPlugin)
        spinnerParameter = findViewById(R.id.spinnerParameter)
        editTextParamValue = findViewById(R.id.editTextParamValue)
        applyButton = findViewById(R.id.applyButton)

        dwInterface = DWInterface(applicationContext)
        // App default profile
        dwInterface.setupAppDefaultProfile()
        dwInterface.setupAppDefaultIntents() // Configure intent output
        // Get DW Info
        dwInterface.getVersionInfo()
        dwInterface.getProfiles()

        // Register DW receiver
        registerReceivers()

        initUI()

    }
    override fun onPause() {
        super.onPause()
        unregisterReceiver(dataWedgeReceiver)
    }

    override fun onRestart() {
        super.onRestart()
        registerReceivers()
    }

    private fun initUI(){

        applyButton.setOnClickListener { _ -> applyProfileConfig() }

        spinnerProfile.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                progressBar.visibility = VISIBLE;
                dwInterface.getProfileConfiguration(spinnerProfile.selectedItem.toString())
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }

        spinnerPlugin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                val selectedPlugin: String = spinnerPlugin.selectedItem.toString()
                val paramAdapter = ArrayAdapter(applicationContext,android.R.layout.simple_spinner_item,
                    pluginList.first { x -> x.pluginName == selectedPlugin }.getAllParams().sortedBy { x -> x })
                spinnerParameter.adapter = paramAdapter
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }

        spinnerParameter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                val selectedPlugin: String = spinnerPlugin.selectedItem.toString()
                val selectedParam: String = spinnerParameter.selectedItem.toString()
                val paramValue = pluginList.first { x -> x.pluginName == selectedPlugin }.getParam(selectedParam)
                editTextParamValue.setText(paramValue)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    private fun updateParamValue(){
        editTextParamValue.clearFocus()
        val selectedParam = spinnerParameter.selectedItem.toString()
        pluginList.first { x -> x.pluginName == spinnerPlugin.selectedItem.toString() }.updateParam(selectedParam, editTextParamValue.text.toString())
    }

    private fun applyProfileConfig(){
        updateParamValue()
        val selectedProfile = spinnerProfile.selectedItem.toString()
        val selectedPlugin = spinnerPlugin.selectedItem.toString()
        editTextLog.append("Applying ${pluginList.sumOf { x -> x.getParamasUpdatdCount() }} modifications\n")
        dwInterface.updateProfile(selectedProfile, pluginList.first { x -> x.pluginName == selectedPlugin })
        editTextLog.append("New config applied\n")
    }

    // Create filter for the broadcast intent
    private fun registerReceivers() {
        val filter = IntentFilter()
        filter.addAction("com.symbol.datawedge.api.NOTIFICATION_ACTION") // for notification result
        filter.addAction("com.symbol.datawedge.api.RESULT_ACTION") // for error code result
        filter.addCategory(Intent.CATEGORY_DEFAULT) // needed to get version info

        // register to received broadcasts via DataWedge scanning
        filter.addAction("$packageName.ACTION")
        filter.addAction("$packageName.service.ACTION")
        registerReceiver(dataWedgeReceiver, filter)
    }

    fun newBarcodeScanned(barcode: String?) {
        runOnUiThread {
            val lines = barcode!!.split('\t').count()
            editTextLog.append("--- NEW BARCODE SCANNED ---\n")
            editTextLog.append(barcode + "\n")
            editTextLog.append("--- END ---\n\n")
        }
    }

    fun dwVersionReceived(version : String?){
        runOnUiThread {
            editTextLog.append("$version\n")
        }
    }
    fun dwProfilesReceived(profiles: List<DWProfile>){
        runOnUiThread {
            profileList = profiles
            val profileAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, profileList.map { x -> x.profileName })
            spinnerProfile.adapter = profileAdapter
        }
    }
    fun dwConfigReceived(plugins : List<DWPlugin>){
        runOnUiThread {
            pluginList = plugins
            val pluginAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, pluginList.map { x -> x.pluginName })
            spinnerPlugin.adapter = pluginAdapter

            progressBar.visibility = GONE;
        }
    }

    companion object {
        private lateinit var dwInterface : DWInterface
    }
}