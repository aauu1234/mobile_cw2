
package org.tensorflow.lite.examples.classification

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import org.tensorflow.lite.examples.classification.databinding.ActivityMainBinding
import org.tensorflow.lite.examples.classification.ml.FlowerModel
import org.tensorflow.lite.examples.classification.ui.RecognitionAdapter
import org.tensorflow.lite.examples.classification.util.YuvToRgbConverter
import org.tensorflow.lite.examples.classification.viewmodel.Recognition
import org.tensorflow.lite.examples.classification.viewmodel.RecognitionListViewModel
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.model.Model
import java.util.concurrent.Executors
import org.tensorflow.lite.gpu.CompatibilityList

// Cants
private const val MAX_RESULT_DISPLAY = 3 // Maximum number of results displayed
private const val TAG = "TFL Classify" // Name for logging
private const val REQUEST_CODE_PERMISSIONS = 999 // Return code after asking for permission
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA) // permission needed


// Listener for the result of the ImageAnalyzer
//alan test
typealias RecognitionListener = (recognition: List<Recognition>) -> Unit

/**
 * Main entry point into TensorFlow Lite Classifier
 */
class MainActivity : AppCompatActivity() {

    // CameraX variables
    //private  var freezePreview: Preview?=null// Preview use case, fast, responsive view of the camera
    private lateinit var preview: Preview // Preview use case, fast, responsive view of the camera
    private lateinit var imageAnalyzer: ImageAnalysis // Analysis use case, for running ML code
    private lateinit var camera: Camera
    private val cameraExecutor = Executors.newSingleThreadExecutor()
     var pauseAnalysis :Boolean=false



    // Views attachment
    private val resultRecyclerView by lazy {
        findViewById<RecyclerView>(R.id.recognitionResults) // Display the result of analysis
    }


    private val viewFinder by lazy {
        findViewById<PreviewView>(R.id.viewFinder) // Display the preview image from Camera
    }

    // Contains the recognition result. Since  it is a viewModel, it will survive screen rotations
    private val recogViewModel: RecognitionListViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    var resultLabel:String=""
    var resultConidence:Float = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

       var plantArrayList= arrayListOf<PlantModels>()

        AssetsDatabaseManager.initManager(this);

        val mg = AssetsDatabaseManager.getManager();

        val db1: SQLiteDatabase = mg.getDatabase("PlantStore.db");

        val cursor=db1.query("Plant",null,null,null,null,null,null)

        if(cursor.moveToFirst()){
            do{
                var item:PlantModels
                val name=cursor.getString(cursor.getColumnIndex("plantname"))
                val status=cursor.getString(cursor.getColumnIndex("status"))
                item=PlantModels(name,status)
                Log.d(TAG,"DATABASE Rercprd $name")
                Log.d(TAG,"DATABASE Rercprd $status")
            plantArrayList.add(item)
            }while(cursor.moveToNext())
        }
        cursor.close()

        for(item in plantArrayList){
            Log.d(TAG,"Loop "+item.plantname+" : "+item.status)

        }

        disableAllButton()


        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
            Log.d("TAG", "messageAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        Log.d(TAG,"result get")
        Log.d(TAG,resultLabel)
        Log.d(TAG,resultConidence.toString())

        binding.bottomNavigationView.menu.getItem(1).setChecked(true)
        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId){


                R.id.home->replaceFragment(Home())
                R.id.newPlant->replaceFragment(NewPlantForm())
                R.id.scan->replaceFragment(Scan())
                R.id.map->replaceFragment(Map())
                R.id.setting->replaceFragment(Setting())

                else->{


                }

            }
            true
        }

        // Initialising the resultRecyclerView and its linked viewAdaptor
        val viewAdapter = RecognitionAdapter(this)
        resultRecyclerView.adapter = viewAdapter

        // Disable recycler view animation to reduce flickering, otherwise items can move, fade in
        // and out as the list change
        resultRecyclerView.itemAnimator = null


        // Attach an observer on the LiveData field of recognitionList
        // This will notify the recycler view to update every time when a new list is set on the
        // LiveData field of recognitionList.
        recogViewModel.recognitionList.observe(this,
            Observer {
                viewAdapter.submitList(it)
                Log.d(TAG,"result get")
                Log.d(TAG,resultLabel)
                Log.d(TAG,resultConidence.toString())
                var plantItemSelected:String=""
                var plantStatus:String=""
                plantStatus=checkPlantStatus(plantArrayList)
                plantItemSelected=checkPlantName(plantArrayList)
                Log.d(TAG, "OutSide PauseAnalysis :$pauseAnalysis")

                if(pauseAnalysis){
                    Log.d(TAG, "FUCKKKKKKKKKK: $plantStatus")
               alertButton(plantStatus)
                    buttonAction(plantItemSelected)
                }else{

                    disableAllButton()
                }

            }
        )



    }

    override fun onStart() {
        super.onStart()

        Log.d(TAG,"result get")
        Log.d(TAG,resultLabel)
        Log.d(TAG,resultConidence.toString())
        pauseAnalysis=false

    }

    override fun onResume() {
        super.onResume()

    }

    private fun StopCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
          //  Toast.makeText(this, "unbindAll", Toast.LENGTH_LONG).show();
        }, ContextCompat.getMainExecutor(this))
       // Toast.makeText(getApplicationContext(), " called", Toast.LENGTH_LONG).show();
    }

    override fun onPause() {
        super.onPause()

      //  Toast.makeText(getApplicationContext(), "onPause called", Toast.LENGTH_LONG).show();
}

    /**
     * Check all permissions are granted - use for Camera permission in this example.
     */
    private fun allPermissionsGranted(): Boolean = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * This gets called after the Camera permission pop up is shown.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                // Exit the app if permission is not granted
                // Best practice is to explain and offer a chance to re-request but this is out of
                // scope in this sample. More details:
                // https://developer.android.com/training/permissions/usage-notes
                Toast.makeText(
                    this,
                    getString(R.string.permission_deny_text),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    /**
     * Start the Camera which involves:
     *
     * 1. Initialising the preview use case
     * 2. Initialising the image analyser use case
     * 3. Attach both to the lifecycle of this activity
     * 4. Pipe the output of the preview object to the PreviewView on the screen
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            preview = Preview.Builder()
                .build()
            imageAnalyzer = ImageAnalysis.Builder()
                // This sets the ideal size for the image to be analyse, CameraX will choose the
                // the most suitable resolution which may not be exactly the same or hold the same
                // aspect ratio
                .setTargetResolution(Size(224, 224))
                // How the Image Analyser should pipe in input, 1. every frame but drop no frame, or
                // 2. go to the latest frame and may drop some frame. The default is 2.
                // STRATEGY_KEEP_ONLY_LATEST. The following line is optional, kept here for clarity
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)

                .build()
                .also { analysisUseCase: ImageAnalysis ->
                    analysisUseCase.setAnalyzer(cameraExecutor, ImageAnalyzer(this,pauseAnalysis) { items ->
                        // updating the list of recognised objects
                        //**************  item label and % OUTPUT place *********************

                        if(items[0].confidence>0.4){
                            recogViewModel.updateData(items)
                            //    Log.d(TAG,list[0].status)
                            resultLabel=items[0].label
                            resultConidence=items[0].confidence

                        }


                    }


                    )
                }

            // Select camera, back is the default. If it is not available, choose front camera
            val cameraSelector =
                if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA))
                    CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera - try to bind everything at once and CameraX will find
                // the best combination.
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )

                // Attach the preview to preview view, aka View Finder
                preview.setSurfaceProvider(viewFinder.surfaceProvider)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

private fun checkPlantStatus(plantArrayList:ArrayList<PlantModels>):String{

    for(item in plantArrayList){
        Log.d(TAG,"Loop "+item.plantname+" : "+item.status)

        var regex="(.*?${item.plantname}.*?)".toRegex()
        Log.d(TAG, "Loop regex : $regex")

        Log.d(TAG, "Loop result compare database : ${regex.matches(input = resultLabel)} ")

        if(regex.matches(input = resultLabel) ){
            Log.d(TAG,"Check Plant Status")
          //  plantStatus=item.status
            pauseAnalysis=true
            Log.d(TAG, "PauseAnalysis :$pauseAnalysis")
            return item.status
            //break
        }else{
            pauseAnalysis=false
            disableAllButton()
        }
    }
    return ""
}
    private fun checkPlantName(plantArrayList:ArrayList<PlantModels>):String{

        for(item in plantArrayList){
            Log.d(TAG,"Loop "+item.plantname+" : "+item.status)

            var regex="(.*?${item.plantname}.*?)".toRegex()
            Log.d(TAG, "Loop regex : $regex")

            Log.d(TAG, "Loop result compare database : ${regex.matches(input = resultLabel)} ")

            if(regex.matches(input = resultLabel) ){
                Log.d(TAG,"check Plant Name")
                //  plantStatus=item.status
                pauseAnalysis=true
                Log.d(TAG, "PauseAnalysis :$pauseAnalysis")
                return item.plantname
                //break
            }else{
                pauseAnalysis=false
                disableAllButton()
            }
        }
        return ""
    }
    // disable all alert button function
private fun disableAllButton(){
    if(binding.EAT.visibility==View.VISIBLE)
        binding.EAT.visibility=View.INVISIBLE
    if(binding.NPOS.visibility==View.VISIBLE)
        binding.NPOS.visibility=View.INVISIBLE
    if(binding.POS.visibility==View.VISIBLE)
        binding.POS.visibility=View.INVISIBLE
    if(binding.EPOS.visibility==View.VISIBLE)
        binding.EPOS.visibility=View.INVISIBLE
}
// debug camera function
    private fun testStopCameraButton(){
        binding.button.setOnClickListener(View.OnClickListener{
            val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
            cameraProviderFuture.addListener(Runnable {
                // Used to bind the lifecycle of cameras to the lifecycle owner
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
                Toast.makeText(this, "unbindAll", Toast.LENGTH_LONG).show();

            }, ContextCompat.getMainExecutor(this))
        })
    }
//when the alert button click function
    private fun buttonAction(plantName:String){
        binding.EAT.setOnClickListener(View.OnClickListener {
            //bundle used to transfer data to fragment
            val bundle=Bundle()
            bundle.putString("plantName",plantName)
            var dialog=PlantInfoDialog()
            dialog.arguments=bundle
            dialog.show(supportFragmentManager,"plantInfoDialog")

        })
        binding.NPOS.setOnClickListener(View.OnClickListener {
            val bundle=Bundle()
            bundle.putString("plantName",plantName)
            var dialog=PlantInfoDialog()
            dialog.arguments=bundle
            dialog.show(supportFragmentManager,"plantInfoDialog")
        })
        binding.POS.setOnClickListener(View.OnClickListener {
            val bundle=Bundle()
            bundle.putString("plantName",plantName)
            var dialog=PlantInfoDialog()
            dialog.arguments=bundle
            dialog.show(supportFragmentManager,"plantInfoDialog")
        })
        binding.EPOS.setOnClickListener(View.OnClickListener {
            val bundle=Bundle()
            bundle.putString("plantName",plantName)
            var dialog=PlantInfoDialog()
            dialog.arguments=bundle
            dialog.show(supportFragmentManager,"plantInfoDialog")
        })
    }
    //alert button visibility status
    private fun alertButton(status:String){
        var status=status

        if(status=="EAT"){
            if(binding.EAT.visibility==View.INVISIBLE)
                binding.EAT.visibility=View.VISIBLE
            if(binding.NPOS.visibility==View.VISIBLE)
                binding.NPOS.visibility=View.INVISIBLE
            if(binding.POS.visibility==View.VISIBLE)
                binding.POS.visibility=View.INVISIBLE
            if(binding.EPOS.visibility==View.VISIBLE)
                binding.EPOS.visibility=View.INVISIBLE
        }else if(status=="NPOS"){
            if(binding.EAT.visibility==View.VISIBLE)
                binding.EAT.visibility=View.INVISIBLE
            if(binding.NPOS.visibility==View.INVISIBLE)
                binding.NPOS.visibility=View.VISIBLE
            if(binding.POS.visibility==View.VISIBLE)
                binding.POS.visibility=View.INVISIBLE
            if(binding.EPOS.visibility==View.VISIBLE)
                binding.EPOS.visibility=View.INVISIBLE

        }else if(status=="POS"){
            if(binding.EAT.visibility==View.VISIBLE)
                binding.EAT.visibility=View.INVISIBLE
            if(binding.NPOS.visibility==View.VISIBLE)
                binding.NPOS.visibility=View.INVISIBLE
            if(binding.POS.visibility==View.INVISIBLE)
                binding.POS.visibility=View.VISIBLE
            if(binding.EPOS.visibility==View.VISIBLE)
                binding.EPOS.visibility=View.INVISIBLE

        }else if(status=="EPOS"){
            if(binding.EAT.visibility==View.VISIBLE)
                binding.EAT.visibility=View.INVISIBLE
            if(binding.NPOS.visibility==View.VISIBLE)
                binding.NPOS.visibility=View.INVISIBLE
            if(binding.POS.visibility==View.VISIBLE)
                binding.POS.visibility=View.INVISIBLE
            if(binding.EPOS.visibility==View.INVISIBLE)
                binding.EPOS.visibility=View.VISIBLE


        }
//        if(binding.button4.visibility==View.INVISIBLE)
//        {
//            Log.d(TAG,"KPOPOPOOPOPPOPPOOPP")
//            Log.d(TAG,"true")
//            binding.button4.visibility=View.VISIBLE
//        }
    }


    private class ImageAnalyzer(ctx: Context,pauseAnalysis:Boolean,private val listener: RecognitionListener) :
        ImageAnalysis.Analyzer {


        // Initializing the flowerModel by lazy so that it runs in the same thread when the process
        // method is called.
        private val flowerModel: FlowerModel by lazy{


            val compatList = CompatibilityList()

            val options = if(compatList.isDelegateSupportedOnThisDevice) {
                Log.d(TAG, "This device is GPU Compatible ")
                Model.Options.Builder().setDevice(Model.Device.GPU).build()
            } else {
                Log.d(TAG, "This device is GPU Incompatible ")
                Model.Options.Builder().setNumThreads(4).build()
            }

            // Initialize the Flower Model
            FlowerModel.newInstance(ctx, options)
        }
            var pauseAnalysis=pauseAnalysis

        override fun analyze(imageProxy: ImageProxy) {

            val items = mutableListOf<Recognition>()

            Log.d(TAG,pauseAnalysis.toString())
            if(pauseAnalysis==false){

                val tfImage = TensorImage.fromBitmap(toBitmap(imageProxy))


                val outputs = flowerModel.process(tfImage)
                    .probabilityAsCategoryList.apply {
                        sortByDescending { it.score } // Sort with highest confidence first
                    }.take(MAX_RESULT_DISPLAY) // take the top results

                for (output in outputs) {
                    items.add(Recognition(output.label, output.score))


                }

                // Return the result
                listener(items.toList())

                // Close the image,this tells CameraX to feed the next image to the analyzer
                imageProxy.close()
            }else{
              Log.d(TAG,"asddddddddddddddaskdanjnjdanjfdanjdnjnjdanjjndfjnadfnjadfjndafnjdafnjdjna")
            }


        }


        /**
         * Convert Image Proxy to Bitmap
         */
        private val yuvToRgbConverter = YuvToRgbConverter(ctx)
        private lateinit var bitmapBuffer: Bitmap
        private lateinit var rotationMatrix: Matrix

        @SuppressLint("UnsafeExperimentalUsageError")
        private fun toBitmap(imageProxy: ImageProxy): Bitmap? {

            val image = imageProxy.image ?: return null

            // Initialise Buffer
            if (!::bitmapBuffer.isInitialized) {
                // The image rotation and RGB image buffer are initialized only once
                Log.d(TAG, "Initalise toBitmap()")
                rotationMatrix = Matrix()
                rotationMatrix.postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                bitmapBuffer = Bitmap.createBitmap(
                    imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888
                )
            }

            // Pass image to an image analyser
            yuvToRgbConverter.yuvToRgb(image, bitmapBuffer)

            // Create the Bitmap in the correct orientation
            return Bitmap.createBitmap(
                bitmapBuffer,
                0,
                0,
                bitmapBuffer.width,
                bitmapBuffer.height,
                rotationMatrix,
                false
            )


        }

    }

    private  fun replaceFragment(fragment: Fragment)
    {
        StopCamera()
        binding.EAT.visibility=View.INVISIBLE
        binding.POS.visibility=View.INVISIBLE
        binding.NPOS.visibility=View.INVISIBLE
        binding.EPOS.visibility=View.INVISIBLE
        val fragmentManager=supportFragmentManager
        val fragmentTransition=fragmentManager.beginTransaction()
        fragmentTransition.replace(R.id.frame_layout,fragment)
        fragmentTransition.commit()
    }



}

