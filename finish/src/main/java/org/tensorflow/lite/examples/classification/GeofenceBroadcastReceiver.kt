package org.tensorflow.lite.examples.classification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.getSystemService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private lateinit var geofencelist: List<Geofence>
    private lateinit var audioManager: AudioManager
    private var dwellTimer: CountDownTimer? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("info", "enter GeofenceBroadcastReceiver class")
        if (context != null) {
            audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
        }
        val notificationHelper = NotificationHelper(context!!)
        val geofenceEvent = GeofencingEvent.fromIntent(intent!!)
        if (geofenceEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofenceEvent.errorCode)
            Log.d("error", errorMessage)
            Log.d("onReceive", "Geofence Event has get an Error")
        }
        val location = geofenceEvent.triggeringLocation
        val transitionTypes = geofenceEvent.geofenceTransition

        geofencelist = geofenceEvent.triggeringGeofences

        val sharedPreferences = context?.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val enableNotifications = sharedPreferences?.getBoolean("EnableNotifications", true) ?: true

        if (!enableNotifications) {
          //  dwellTimer?.cancel()
            stopDwellTimer()
            return
        }
        when (transitionTypes) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Log.d("getMapStatus", "enter GEOFENCE_TRANSITION_ENTER")
                Toast.makeText(context, "You Enter the Toxic Plants Area!!", Toast.LENGTH_LONG).show()
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                // Play ringing sound
                val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                val ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
                ringtone.play()
                // Stop the ringtone after 3 seconds
                Handler(Looper.getMainLooper()).postDelayed({
                    ringtone.stop()
                }, 5000)
                notificationHelper.sendHighPriorityNotification("You Enter the Toxic Plants Area!!", "", MainActivity2().javaClass)
                startDwellTimer(context)
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                Log.d("getMapStatus", "enter EOFENCE_TRANSITION_DWELL")
                Toast.makeText(context, "You Dwell the Toxic Plants Area!!", Toast.LENGTH_LONG).show()
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                // Play ringing sound
                audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK)
                notificationHelper.sendHighPriorityNotification("You are in the Toxic Plants Area!!", "", MainActivity2().javaClass)
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Log.d("getMapStatus", "enter GEOFENCE_TRANSITION_EXIT")
                Toast.makeText(context, "You Exited the Toxic Plants Area!!", Toast.LENGTH_LONG).show()
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                // Play ringing sound
                audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK)
                notificationHelper.sendHighPriorityNotification("You leave the Toxic Plants Area.", "", MainActivity2().javaClass)
                stopDwellTimer()
            }
        }
    }

    private fun startDwellTimer(context: Context) {
        dwellTimer = object : CountDownTimer(3, 5000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d("GeofenceReceiver", "Geofence dwell (10 seconds interval)")
                Toast.makeText(context, "You are still in the Toxic Plants Area!!", Toast.LENGTH_SHORT).show()
                // Perform your desired action every 10 seconds while inside the geofence
            }

            override fun onFinish() {
                // This should not be called as we set the timer to Long.MAX_VALUE
            }
        }.start()
    }

    fun stopDwellTimer() {
        Log.d("GeofenceReceiver", "Stop DwellTimer")
        dwellTimer?.cancel()

    }

    companion object {
        private var instance: GeofenceBroadcastReceiver? = null

        fun getInstance(): GeofenceBroadcastReceiver {
            if (instance == null) {
                instance = GeofenceBroadcastReceiver()
            }
            return instance!!
        }
    }
}
