package org.tensorflow.lite.examples.classification

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import androidx.fragment.app.Fragment


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Setting.newInstance] factory method to
 * create an instance of this fragment.
 */
class Setting : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var notificationHelper: NotificationHelper? = null
    private lateinit var notificationSwitch: Switch
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_setting, container, false)

        sharedPreferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        notificationSwitch = view.findViewById(R.id.notification_switch)
        notificationSwitch.isChecked = sharedPreferences.getBoolean("EnableNotifications", true)


        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("EnableNotifications", isChecked).apply()

            if (!isChecked) {
                // Stop the dwell timer in GeofenceBroadcastReceiver
                GeofenceBroadcastReceiver.getInstance().stopDwellTimer()
            }
        }


        return view
    }


    fun sendNotification(view: View?) {
        notificationHelper?.sendHighPriorityNotification(
            "this is title", "this is some awesome notificaiton. wow i learnt it the easy way.",
           org.tensorflow.lite.examples.classification.MainActivity::class.java
        )
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Setting().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}