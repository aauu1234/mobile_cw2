package org.tensorflow.lite.examples.classification

import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng
import android.content.SharedPreferences

class GeofenceHelper(context: Context?):ContextWrapper(context) {
    private final val TAG:String="GEOFENCEHELPER"
    private val sharedPreferences: SharedPreferences = getSharedPreferences("GeofencePreferences", Context.MODE_PRIVATE)
    fun getGeofencingRequest(geofence: Geofence): GeofencingRequest{
        Log.d("GeofenceHelper","getGeofencingRequest")
        return GeofencingRequest.Builder().apply {
            addGeofence(geofence)
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or
                    GeofencingRequest.INITIAL_TRIGGER_DWELL or
                    GeofencingRequest.INITIAL_TRIGGER_EXIT)
            Log.d("GeofenceHelper","setInitialTrigger")
        }.build()
    }

    fun getGeofence(ID: String, latLng: LatLng, radius: Double, transitiontype: Int): Geofence? {
        Log.d("GeofenceHelper", "getGeofence")

        // Check if the Geofence ID already exists
        val existingGeofenceIds = sharedPreferences.getStringSet("GeofenceIds", mutableSetOf())
        if (existingGeofenceIds?.contains(ID) == true) {
            Log.d("GeofenceHelper", "Geofence with ID $ID already exists")
            return null
        }

        // Add the Geofence ID to SharedPreferences
        existingGeofenceIds?.add(ID)
        sharedPreferences.edit().putStringSet("GeofenceIds", existingGeofenceIds).apply()

        return Geofence.Builder()
            .setCircularRegion(latLng.latitude, latLng.longitude, radius.toFloat())
            .setRequestId(ID)
            .setLoiteringDelay(10000)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(transitiontype)
            .build()
    }

    val pendingIntent:PendingIntent by lazy{
        Log.d("GeofenceHelper","Pending Intent to GeofenceBroadcastReceiver")
        val intent= Intent(applicationContext,GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(applicationContext,0,intent,PendingIntent.FLAG_UPDATE_CURRENT)

    }

    fun getErrorString(e:Exception):String{
        if(e is ApiException){
            val apiException=e
            when(apiException.statusCode){
                GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE->return "Geofence Not Availble"
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES->return "Too many Geofence to update"
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS->return "Too many pending geofence"
            }

        }
        return e.localizedMessage
    }

    fun removeGeofence(){

    }

}