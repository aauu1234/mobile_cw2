package org.tensorflow.lite.examples.classification

import android.Manifest
import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobScheduler
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.plant_info_dialog.view.*
import kotlin.random.Random

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
const val GEOFENCE_RADIUS = 50
const val GEOFENCE_ID = "REMINDER_GEOFENCE_ID"
const val GEOFENCE_EXPIRATION = 10 * 24 * 60 * 60 * 1000 // 10 days
const val GEOFENCE_DWELL_DELAY =  10 * 1000 // 10 secs // 2 minutes
const val GEOFENCE_LOCATION_REQUEST_CODE = 12345
const val CAMERA_ZOOM_LEVEL = 13f
const val LOCATION_REQUEST_CODE = 123
private val TAG: String = Map::class.java.simpleName

/**
 * A simple [Fragment] subclass.
 * Use the [Setting.newInstance] factory method to
 * create an instance of this fragment.
 */
class Map : Fragment() {
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var markerOptions: MarkerOptions
    private lateinit var locationManager:LocationManager
    private lateinit var geofenceHelper: GeofenceHelper
    private val REQUEST_LOCATION_PERMISSION = 1
    private  val locationcode=2000
    private  val locationcode1=2001
    private val kongTongArea=LatLng(22.312065,114.222278)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

      //Initialize view
        var rootView:View=inflater.inflate(R.layout.fragment_map,container,false)


        // Initialize map fragment
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        geofenceHelper= GeofenceHelper(requireContext())
        // Initialize map fragment

        //set noti permission start
        val notificationManager= requireActivity().getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M&&!notificationManager.isNotificationPolicyAccessGranted){
            val intent=Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
        }
       //set noti permission end

       //  map old one start

        if(!::mapFragment.isInitialized){
            mapFragment=SupportMapFragment.newInstance();
            mapFragment!!.getMapAsync{ map ->
            // When map is loaded
            val hongkong =LatLng(22.344297,114.148768)
            markerOptions=MarkerOptions().position(hongkong).title("Marker in HongKong").snippet("HongKong Toxic Plant Map")
            map.addMarker(markerOptions)
                val cameraUpdate=CameraUpdateFactory.newLatLngZoom(hongkong,12F)
                map.animateCamera(cameraUpdate)

            enableMyLocation(map)
            kongTongGeofence(kongTongArea,map,"TongKong")
            handleMapGeofenceList(map)
            onMapLongClick(map)

            // Draw Polygon
            map.addPolygon(drawHouse())
            map.addPolygon(drawPingShak())
            map.addPolygon(drawKongTong())
            //draw ploygon end

            map.setOnMapClickListener { latLng -> // When clicked on map
                // Initialize marker options
                val markerOptions = MarkerOptions()
                // Set position of marker
                markerOptions.position(latLng)
                // Set title of marker
                markerOptions.title(latLng.latitude.toString() + " : " + latLng.longitude)
                // Remove all marker
                // Animating to zoom the marker
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
                // Add marker on map
                map.addMarker(markerOptions)


            }
        }
        }

        getChildFragmentManager().beginTransaction().replace(R.id.google_map,mapFragment).commit()


        return rootView
    }
//check the phone permission
    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
//enable the gpa location
    private fun enableMyLocation(map:GoogleMap) {
        if (isPermissionGranted()) {
            val locationRequest = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            val locationSettingsRequest = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)
                .build()
            val locationSettingsResponse =
                LocationServices.getSettingsClient(requireContext()).checkLocationSettings(locationSettingsRequest)
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            map.isMyLocationEnabled = true


        }
        else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }
//for debug function
    private fun getCurrentLocation(map: GoogleMap):LatLng{
        var mylatitude=0.0
        var mylongtitude=0.0
        if(map.isMyLocationEnabled){
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            }
            fusedLocationClient.lastLocation.addOnCompleteListener {
                task->
                val location:Location?=task.result
                if(location==null){
                    Toast.makeText(requireContext(),"Failed get current location",Toast.LENGTH_SHORT)
                    mylatitude=0.0
                    mylongtitude=0.0
                }else{
                    Toast.makeText(requireContext(),"Success get current location", Toast.LENGTH_SHORT)
                    mylatitude=location.latitude
                    mylongtitude=location.longitude
                }
            }
        }
        var myloca=LatLng(mylatitude,mylongtitude);
        return myloca
    }
//request the nessary phone permission
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Toast.makeText(requireContext(),"onRequestPermissionsResult Action",Toast.LENGTH_LONG).show()
        if(requestCode==locationcode){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                if((ActivityCompat.checkSelfPermission(requireContext(),ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED) &&
                    (ActivityCompat.checkSelfPermission(requireContext(),android.Manifest.permission.ACCESS_COARSE_LOCATION) !=PackageManager.PERMISSION_GRANTED)){

                    return
                }
                map.isMyLocationEnabled=true
            }
        }
        if(requestCode==locationcode1){
            if(grantResults.isNotEmpty()&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.checkSelfPermission(requireContext(), ACCESS_BACKGROUND_LOCATION)!=PackageManager.PERMISSION_GRANTED&&(ActivityCompat.checkSelfPermission(requireContext(),android.Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)){
                    return
                }
                Toast.makeText(requireContext(),"You can add Geofence",Toast.LENGTH_LONG).show()
            }
        }
    }
//for long click map function
    private fun onMapLongClick(map: GoogleMap){
        map.setOnMapLongClickListener { latlng ->
            if (Build.VERSION.SDK_INT >= 29) {
                handleMapLongClick(latlng,map)

            }
        }
    }
//create the Geofence function
    private fun kongTongGeofence(p0:LatLng,map: GoogleMap,ID: String){
        handleMapGeofence(p0,map,ID)
    }

    private fun handleMapGeofence(p0:LatLng,map:GoogleMap,ID: String){
        //   map.clear()
        addMarker(p0,map)
        addCircle(p0,map)
        addToxicGeofence(p0,ID)
    }

    @SuppressLint("SuspiciousIndentation")
    private fun handleMapGeofenceList(map:GoogleMap){
        //   map.clear()

        val toxicPlantLocList= getToxicPlantLocData()
        for(item in toxicPlantLocList){
         val location=LatLng(item.locaLatitude,item.locaLongitude)
            addDetailMarker(location,map,item.plantEnName,item.locaEnName,item.PlantImage)
            addCircle(location,map)
            addToxicGeofence(location,item.locaEnName)

        }

    }

    private fun handleMapLongClick(p0:LatLng,map:GoogleMap){
     //   map.clear()
        addMarker(p0,map)
        addTestCircle(p0,map)
        addGeofence(p0)
    }

    private fun addMarker(latLng: LatLng,map:GoogleMap){
        markerOptions=MarkerOptions().position(latLng)
        map.addMarker(markerOptions)
    }

    private fun addDetailMarker(latLng: LatLng,map:GoogleMap,plantName:String,locationName:String,plantImage:Bitmap){

        markerOptions=MarkerOptions().position(latLng).title(plantName).snippet(locationName)

//        map.setInfoWindowAdapter(object:GoogleMap.InfoWindowAdapter{
//            override fun getInfoWindow(p0: Marker): View? {
//                TODO("Not yet implement)
//            }
//
//            override fun getInfoContents(p0: Marker): View? {
//                TODO("Not yet implemented")
//            }
//        })
        map.setInfoWindowAdapter(CustomInfoWindowAdapter())
        map.setOnInfoWindowClickListener {
            marker->onMarkerClick(marker)
        }
     //   map.setInfoWindowAdapter(CustomInfoWindowForGoogleMap(requireContext()))
        map.addMarker(markerOptions)?.tag = plantImage


    }

    private fun onMarkerClick(marker:Marker){

       // Toast.makeText(requireContext(),"sad",Toast.LENGTH_SHORT).show()
        val bundle=Bundle()
        bundle.putString("plantName",marker.title)
        var dialog=PlantInfoDialog()
        dialog.arguments=bundle
        dialog.show(parentFragmentManager,"plantInfoDialog")
    }

    private fun addCircle(latLng: LatLng,map:GoogleMap){

        val circleOptions=CircleOptions()
        circleOptions.center(latLng)
        circleOptions.radius(200.00)
        circleOptions.strokeColor(Color.argb(255,255,0,0))
        circleOptions.fillColor(Color.argb(64,Random.nextInt(256),Random.nextInt(256),Random.nextInt(256)))
        circleOptions.strokeWidth(4F)
        map.addCircle(circleOptions)
    }

    private fun addTestCircle(latLng: LatLng,map:GoogleMap){

        val circleOptions=CircleOptions()
        circleOptions.center(latLng)
        circleOptions.radius(101.00)
        circleOptions.strokeColor(Color.argb(255,255,0,0))
        circleOptions.fillColor(Color.argb(64,200,10,10))
        circleOptions.strokeWidth(4F)
        map.addCircle(circleOptions)
    }
//add new geofence function
    private fun addGeofence(p0: LatLng){
        val randomId = Random.nextInt(9999).toString()
        val geofence =geofenceHelper.getGeofence(randomId,p0,101.00, Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL or Geofence.GEOFENCE_TRANSITION_EXIT)
            val geofenceRequest=geofence?.let{geofenceHelper.getGeofencingRequest(it)}
            val pendingIntent=geofenceHelper.pendingIntent
        if(ActivityCompat.checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            return
        }
        geofencingClient.addGeofences(geofenceRequest!!,pendingIntent).run {
            addOnSuccessListener {
                Log.d("Sucess","Geofence Added")
            }
            addOnFailureListener {
                Log.d("Failure","Geofence Not Added")
            }
        }
    }
//add default toxic plant geofence function
    private fun addToxicGeofence(p0: LatLng,ID:String){

        val geofence =geofenceHelper.getGeofence(ID,p0,200.00, Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL or Geofence.GEOFENCE_TRANSITION_EXIT)
        if (geofence != null) {
        val geofenceRequest=geofence?.let{geofenceHelper.getGeofencingRequest(it)}
        val pendingIntent=geofenceHelper.pendingIntent
        if(ActivityCompat.checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            return
        }
        geofencingClient.addGeofences(geofenceRequest!!,pendingIntent).run {
            addOnSuccessListener {
                Log.d("Sucess","Geofence Added")
            }
            addOnFailureListener {
                Log.d("Failure","Geofence Not Added")
            }
        }
        } else {
            Log.d("Failure","Geofence Already Exist")
        }
    }
//handle the toxic plant data from sqlite
    private fun getToxicPlantLocData():List<ToxicPlantLocModels>{
        AssetsDatabaseManager.initManager(activity);
        val mg = AssetsDatabaseManager.getManager();
        val db1: SQLiteDatabase = mg.getDatabase("PlantStore.db");
        val toxicPlantLocList= arrayListOf<ToxicPlantLocModels>();
        val cursor=db1.query("Toxic_plant_loca , Plant",null,"plant_seq=id",null,null,null,null)
        if(cursor.moveToFirst()){
            do{
                var item:ToxicPlantLocModels
                val plantLocaSeq = cursor.getInt(cursor.getColumnIndex("plant_loca_seq"))
                val plantSeq = cursor.getInt(cursor.getColumnIndex("plant_seq"))
                val locaLatitude = cursor.getDouble(cursor.getColumnIndex("loca_latitude"))
                val locaLongitude = cursor.getDouble(cursor.getColumnIndex("loca_longitude"))
                val locaEnName: String? = cursor.getString(cursor.getColumnIndex("loca_enname"))
                val locaCnName: String? = cursor.getString(cursor.getColumnIndex("loca_cnname"))
                val plantEnName: String? = cursor.getString(cursor.getColumnIndex("plant_enname"))
                val plantCnName: String? = cursor.getString(cursor.getColumnIndex("plant_cnname"))
                val status:String?=cursor.getString(cursor.getColumnIndex("status"))
                val plantImage=cursor.getBlob(cursor.getColumnIndex("image"))

                val options: BitmapFactory.Options = BitmapFactory.Options()
                options.inTempStorage = ByteArray(1024 * 32)

                val bm: Bitmap =
                    BitmapFactory.decodeByteArray(plantImage, 0, plantImage.size , options)

                item= ToxicPlantLocModels(plantLocaSeq,plantSeq,locaLatitude,locaLongitude,locaEnName,locaCnName,plantEnName,plantCnName,bm,status)
                toxicPlantLocList.add(item)

            }while(cursor.moveToNext())
        }
        cursor.close()
        for(item in toxicPlantLocList){
            Log.d(TAG,"Loop "+item.plantEnName+" : "+item.locaEnName)

        }
        return toxicPlantLocList
    }

    class CustomInfoWindowForGoogleMap(context: Context) : GoogleMap.InfoWindowAdapter {

        var mContext = context
        var mWindow = (context as Activity).layoutInflater.inflate(R.layout.map_info_window, null)

        private fun rendowWindowText(marker: Marker, view: View){

            val tvTitle = view.findViewById<TextView>(R.id.textView2)
            val tvSnippet = view.findViewById<TextView>(R.id.textView3)

            tvTitle.text = marker.title
            tvSnippet.text = marker.snippet

        }

        override fun getInfoContents(marker: Marker): View {
            rendowWindowText(marker, mWindow)
            return mWindow
        }

        override fun getInfoWindow(marker: Marker): View? {
            rendowWindowText(marker, mWindow)
            return mWindow
        }
    }

    /** Demonstrates customizing the info window and/or its contents.  */
    internal inner class CustomInfoWindowAdapter : GoogleMap.InfoWindowAdapter {

        // These are both view groups containing an ImageView with id "badge" and two
        // TextViews with id "title" and "snippet".
        private val window: View = layoutInflater.inflate(R.layout.custom_info_window, null)
        private val contents: View = layoutInflater.inflate(R.layout.custom_info_contents, null)

        override fun getInfoWindow(marker: Marker): View? {

            render(marker, window)
            return window
        }

        override fun getInfoContents(marker: Marker): View? {

            render(marker, contents)
            return contents
        }

        private fun render(marker: Marker, view: View) {
            var plantImage: Bitmap
            val badge =R.drawable.ic_alarm
            if(marker.tag==null){
                view.findViewById<ImageView>(R.id.badge).setImageResource(badge)
            }else {
                 plantImage = marker.tag as Bitmap
                view.findViewById<ImageView>(R.id.badge).setImageBitmap(Bitmap.createScaledBitmap(plantImage,100,100,false))
            }


            // Set the title and snippet for the custom info window
            val title: String? = marker.title
            val titleUi = view.findViewById<TextView>(R.id.title)

            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                titleUi.text = SpannableString(title).apply {
                    setSpan(ForegroundColorSpan(Color.RED), 0, length, 0)
                }
            } else {
                titleUi.text = ""
            }

            val snippet: String? = marker.snippet
            val snippetUi = view.findViewById<TextView>(R.id.snippet)
            if (snippet != null) {
                snippetUi.text = SpannableString(snippet).apply {
                    setSpan(ForegroundColorSpan(Color.MAGENTA), 0, 10, 0)
                    setSpan(ForegroundColorSpan(Color.BLUE), 12, snippet.length, 0)
                }
            } else {
                snippetUi.text = ""
            }
        }
    }


    private  fun createGeofence(){

        val geofenceList = mutableListOf<Geofence>()
        geofenceList.add(Geofence.Builder()
            // Set the request ID of the geofence. This is a string to identify this
            // geofence.
            .setRequestId("1")

            // Set the circular region of this geofence.
            .setCircularRegion(
                22.334826,
                114.215518,
                100.0F
            )

            // Set the expiration duration of the geofence. This geofence gets automatically
            // removed after this period of time.
            .setExpirationDuration(999999999)

            // Set the transition types of interest. Alerts are only generated for these
            // transition. We track entry and exit transitions in this sample.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)

            // Create the geofence.
            .build())
    }




    companion object {
        fun removeGeofences(context: Context, triggeringGeofenceList: MutableList<Geofence>) {
            val geofenceIdList = mutableListOf<String>()
            for (entry in triggeringGeofenceList) {
                geofenceIdList.add(entry.requestId)
            }
            LocationServices.getGeofencingClient(context).removeGeofences(geofenceIdList)
        }

        fun showNotification(context: Context?, message: String) {
            val CHANNEL_ID = "REMINDER_NOTIFICATION_CHANNEL"
            var notificationId = 1589
            notificationId += Random(notificationId).nextInt(1, 30)

            val notificationBuilder = NotificationCompat.Builder(context!!.applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(message)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(message)
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.app_name)
                }
                notificationManager.createNotificationChannel(channel)
            }
            notificationManager.notify(notificationId, notificationBuilder.build())
        }
    }



//for the debug function
    private fun cancelJob() {
        val scheduler = requireActivity().getSystemService(AppCompatActivity.JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.cancel(321)
        Log.d(TAG, "Job cancelled")
    }
//draw the area
    private fun drawHouse():PolygonOptions {
        //draw ploygon start
        val polygonOptions = PolygonOptions()
        polygonOptions.add(LatLng(22.334826,114.215518))
        polygonOptions.add(LatLng(22.334882,114.215812))
        polygonOptions.add(LatLng(22.334511,114.215849))
        polygonOptions.add(LatLng(22.334425,114.215584))

        polygonOptions.strokeWidth(5f)
        polygonOptions.strokeColor(Color.rgb(150,16,11))
        polygonOptions.fillColor(Color.argb(100,150,22,15))

        return  polygonOptions
    }
    //located PingShak location
    private fun drawPingShak():PolygonOptions {
        //draw ploygon start
        val polygonOptions = PolygonOptions()
        polygonOptions.add(LatLng(22.332888,114.212469))
        polygonOptions.add(LatLng(22.333015,114.212168))
        polygonOptions.add(LatLng(22.332836,114.212066))
        polygonOptions.add(LatLng(22.332757,114.212237))

        polygonOptions.strokeWidth(5f)
        polygonOptions.strokeColor(Color.rgb(0,2,200))
        polygonOptions.fillColor(Color.argb(100,0,2,200))

        return  polygonOptions
    }
//located kong tong location
    private fun drawKongTong():PolygonOptions {
        //draw ploygon start
        val polygonOptions = PolygonOptions()
        polygonOptions.add(LatLng(22.312309,114.222795))
        polygonOptions.add(LatLng(22.312063,114.222613))
        polygonOptions.add(LatLng(22.312176,114.222345))
        polygonOptions.add(LatLng(22.312373,114.222425))

        polygonOptions.strokeWidth(5f)
        polygonOptions.strokeColor(Color.rgb(0,163,11))
        polygonOptions.fillColor(Color.argb(100,0,224,15))

        return  polygonOptions
    }





}


