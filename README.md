# DataWedge Intent Configuration Sample
Simple demo that shows how to configure DataWedge thru intents.
It gets all the configuration parameters using DW APIs and allows the user to modify them at runtime.

Zebra Techdocs Documentation: https://techdocs.zebra.com/datawedge/11-3/guide/api/

## Profile & Plugins structure

![image](https://user-images.githubusercontent.com/101400857/209328799-86b77e52-f677-40b6-9ccd-7947999c17c7.png)

![image](https://user-images.githubusercontent.com/101400857/209328822-fd2039bd-721e-4eaf-94a8-b426c88e8846.png)

## A few Code snippets

### Get and parse DataWedge configuration
#### Get config
```kotlin
fun getProfileConfiguration(profileName : String){
    val profileBundle = Bundle()
    profileBundle.putString("PROFILE_NAME", profileName)
    val pluginName: ArrayList<String> = ArrayList()
    pluginName.add("BARCODE")
    pluginName.add("RFID")
    //...and more

    val bConfig = Bundle()
    bConfig.putStringArrayList("PLUGIN_NAME", pluginName)
    profileBundle.putBundle("PLUGIN_CONFIG", bConfig)
    
    // Send intent
    val dwIntent = Intent()
        dwIntent.action = "com.symbol.datawedge.api.ACTION"
        dwIntent.putExtra("com.symbol.datawedge.api.GET_CONFIG", profileBundle)
        context.sendBroadcast(dwIntent)
}
```
#### Parse Config result
```kotlin
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
```

### Build Bundles

#### Profile

```kotlin
var profileName : String = name
var profileEnabled : Boolean = true
var configMode : String = ""
var pluginConfig : DWPlugin = DWPlugin()
private var _associatedApps : MutableList<String> = mutableListOf()
private var updateApps : Boolean = false

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
         val pluginBundle = pluginConfig.buildPluginBundle(); // Build plugin
         profileBundle.putBundle("PLUGIN_CONFIG", pluginBundle)
     }
    return profileBundle
}
```
#### Plugin

```kotlin
var pluginName : String = ""
var resetConfig : Boolean = false
private var _paramList : HashMap<String, DWParamValue> = HashMap()

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
```
