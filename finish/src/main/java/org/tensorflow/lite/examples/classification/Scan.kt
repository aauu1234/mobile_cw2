package org.tensorflow.lite.examples.classification

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.*
import android.media.Image
import android.media.ImageReader
import android.media.ThumbnailUtils
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_scan.view.*
import kotlinx.android.synthetic.main.map_info_window.view.*
import org.tensorflow.lite.DataType
import org.tensorflow.lite.examples.classification.ml.FlowerModel
import org.tensorflow.lite.examples.classification.util.YuvToRgbConverter
import org.tensorflow.lite.examples.classification.viewmodel.Recognition
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

// Constants
private const val MAX_RESULT_DISPLAY = 3 // Maximum number of results displayed
private const val TAG = "TFL Classify" // Name for logging
private const val REQUEST_CODE_PERMISSIONS = 999 // Return code after asking for permission
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA) // permission needed

// Listener for the result of the ImageAnalyzer
//typealias RecognitionListener = (recognition: List<Recognition>) -> Unit

/**
 * A simple [Fragment] subclass.
 * Use the [Scan.newInstance] factory method to
 * create an instance of this fragment.
 */
class Scan : Fragment(){

    // TODO: Rename and change types of parameters
//    private var param1: String? = null
//    private var param2: String? = null
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//
//
//        }
//    }
    var camera: Button? = null
    var gallery:android.widget.Button? = null
    var imageView: ImageView? = null
    var result: TextView? = null
    var imageSize: Int = 1024
    var resultLabel:String=""
    var plantArrayList= arrayListOf<PlantModels>()
    private val abc by lazy {
        println("this is lazy function block") // Display the result of analysis
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view: View = inflater!!.inflate(R.layout.fragment_scan, container, false)
        camera = view.button3
        gallery = view.button2
        result =view.result
        imageView =view.imageView2

      //  var plantArrayList= arrayListOf<PlantModels>()
        //   var plantStatus=""
// 初始化，只需要调用一次
        AssetsDatabaseManager.initManager(requireContext());
// 获取管理对象，因为数据库需要通过管理对象才能够获取
        val mg = AssetsDatabaseManager.getManager();
        // 通过管理对象获取数据库l
        val db1: SQLiteDatabase = mg.getDatabase("PlantStore.db");
// 对数据库进行操作
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

        if(view?.EPOS2?.visibility==View.VISIBLE){
            view?.EAT2?.visibility=View.INVISIBLE
            view?.NPOS2?.visibility=View.INVISIBLE
            view?.POS2?.visibility=View.INVISIBLE
            view?.EPOS2?.visibility=View.INVISIBLE
        }

    //    disableAllButton()

        view.button3.setOnClickListener(View.OnClickListener {
            if (ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, 3)
            } else {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 100)
            }
        })
        view.button2.setOnClickListener(View.OnClickListener {
            val cameraIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(cameraIntent, 1)
        })

        view.button4.setOnClickListener(View.OnClickListener{
            val i= Intent(this.context, MainActivity::class.java)
            startActivity(i)
        })

        return view
    }



    override fun onStart() {


        super.onStart()

//        val i= Intent(this.context, MainActivity::class.java)
//        startActivity(i)


    }



    fun classifyImage(image: Bitmap) {
        try {
            //flowerModel: FlowerModel
            val model: FlowerModel = FlowerModel.newInstance(requireActivity().applicationContext)

            // Creates inputs for reference.
            val inputFeature0 =
                TensorBuffer.createFixedSize(intArrayOf(1, 1024, 1024, 3), DataType.FLOAT32)
            val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
            byteBuffer.order(ByteOrder.nativeOrder())
            val intValues = IntArray(imageSize * imageSize)
            image.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)
           // image.
            var pixel = 0
            //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
            for (i in 0 until imageSize) {
                for (j in 0 until imageSize) {
                    val `val` = intValues[pixel++] // RGB
                    byteBuffer.putFloat((`val` shr 16 and 0xFF) * (1f / 1))
                    byteBuffer.putFloat((`val` shr 8 and 0xFF) * (1f / 1))
                    byteBuffer.putFloat((`val` and 0xFF) * (1f / 1))
                }
            }
            inputFeature0.loadBuffer(byteBuffer)
            val items = mutableListOf<Recognition>()
            // Runs model inference and gets result.
            // TODO 2: Convert Image to Bitmap then to TensorImage
            val tfImage = TensorImage.fromBitmap(image)
            val output = model.process(tfImage)
                .probabilityAsCategoryList.apply {
                    sortByDescending { it.score } // Sort with highest confidence first
                }.take(MAX_RESULT_DISPLAY) // take the top results

            for (item in output) {
                items.add(Recognition(item.label, item.score))
                //  Log.d(TAG, output.label)

                //      if(output.score>0.60){

                //    }

            }


            result?.setText(items[0].label+" : "+items[0].confidence*100+"%")
            // Releases model resources if no longer used.
            resultLabel=items[0].label
            Log.d(TAG, "resultLabel : $resultLabel")
            // Check plant status and name
            val status = checkPlantStatus(plantArrayList)
            if (status!="") {
                Log.d(TAG, "status.isNotEmptystatus.isNotEmptystatus.isNotEmptystatus.isNotEmptystatus.isNotEmptystatus.isNotEmpty")
                alertButton(status)
            } else {
                Log.d(TAG, "status.isEmpty Empty")
                disableAllButton()
            }

            val plantName = checkPlantName(plantArrayList)
            if (plantName.isNotEmpty()) {
                buttonAction(plantName)
            } else {
                disableAllButton()
            }
            
            model.close()
        } catch (e: IOException) {
            // TODO Handle the exception
        }
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

                return item.status
                //break
            }
        }
        return ""
    }
    private fun checkPlantName(plantArrayList:ArrayList<PlantModels>):String{
        Log.d(TAG, "Enter checkPlantName function")
        for(item in plantArrayList){
            Log.d(TAG,"Loop "+item.plantname+" : "+item.status)

            var regex="(.*?${item.plantname}.*?)".toRegex()
            Log.d(TAG, "Loop regex : $regex")

            Log.d(TAG, "Loop result compare database : ${regex.matches(input = resultLabel)} ")

            if(regex.matches(input = resultLabel) ){
                Log.d(TAG,"check Plant Name")
                //  plantStatus=item.status

                return item.plantname
                //break
            }
        }
        return ""
    }

    private fun disableAllButton(){
        Log.d(TAG,"Enter disableAllButton function")
        if(view?.EAT2?.visibility ==View.VISIBLE)
            view?.EAT2?.visibility=View.INVISIBLE
        if(view?.NPOS2?.visibility==View.VISIBLE)
            view?.NPOS2?.visibility=View.INVISIBLE
        if(view?.POS2?.visibility==View.VISIBLE)
            view?.POS2?.visibility=View.INVISIBLE
        if(view?.EPOS2?.visibility==View.VISIBLE)
            view?.EPOS2?.visibility=View.INVISIBLE
    }


    private fun buttonAction(plantName:String){
        Log.d(TAG,"Enter buttonAction function")
        view?.EAT2?.setOnClickListener(View.OnClickListener {
            //bundle used to transfer data to fragment
            val bundle=Bundle()
            bundle.putString("plantName",plantName)
            var dialog= PlantInfoDialog()
            dialog.arguments=bundle
            dialog.show(parentFragmentManager,"plantInfoDialog")

        })
        view?.NPOS2?.setOnClickListener(View.OnClickListener {
            val bundle=Bundle()
            bundle.putString("plantName",plantName)
            var dialog=PlantInfoDialog()
            dialog.arguments=bundle
            dialog.show(parentFragmentManager,"plantInfoDialog")
        })
        view?.POS2?.setOnClickListener(View.OnClickListener {
            val bundle=Bundle()
            bundle.putString("plantName",plantName)
            var dialog=PlantInfoDialog()
            dialog.arguments=bundle
            dialog.show(parentFragmentManager,"plantInfoDialog")
        })
        view?.EPOS2?.setOnClickListener(View.OnClickListener {
            val bundle=Bundle()
            bundle.putString("plantName",plantName)
            var dialog=PlantInfoDialog()
            dialog.arguments=bundle
            dialog.show(parentFragmentManager,"plantInfoDialog")
        })
    }
    private fun alertButton(status:String){
        Log.d(TAG,"Enter alertButton function")
        var status=status
        Log.d(TAG, "status$status")
        if(status=="EAT"){
            if(view?.EAT2?.visibility==View.INVISIBLE)
                view?.EAT2?.visibility=View.VISIBLE
            if(view?.NPOS2?.visibility==View.VISIBLE)
                view?.NPOS2?.visibility=View.INVISIBLE
            if(view?.POS2?.visibility==View.VISIBLE)
                view?.POS2?.visibility=View.INVISIBLE
            if(view?.EPOS2?.visibility==View.VISIBLE)
                view?.EPOS2?.visibility=View.INVISIBLE
        }else if(status=="NPOS"){
            Log.d(TAG, "NPOS")
            if(view?.EAT2?.visibility==View.VISIBLE)
                view?.EAT2?.visibility=View.INVISIBLE
            if(view?.NPOS2?.visibility==View.INVISIBLE){
                Log.d(TAG, "view?.NPOS2?.visibility==View.INVISIBLE")
                view?.NPOS2?.visibility=View.VISIBLE
            }

            if(view?.POS2?.visibility==View.VISIBLE)
                view?.POS2?.visibility=View.INVISIBLE
            if(view?.EPOS2?.visibility==View.VISIBLE)
                view?.EPOS2?.visibility=View.INVISIBLE

        }else if(status=="POS"){
            if(view?.EAT2?.visibility==View.VISIBLE)
                view?.EAT2?.visibility=View.INVISIBLE
            if(view?.NPOS2?.visibility==View.VISIBLE)
                view?.NPOS2?.visibility=View.INVISIBLE
            if(view?.POS2?.visibility==View.INVISIBLE)
                view?.POS2?.visibility=View.VISIBLE
            if(view?.EPOS2?.visibility==View.VISIBLE)
                view?.EPOS2?.visibility=View.INVISIBLE

        }else if(status=="EPOS"){
            if(view?.EAT2?.visibility==View.VISIBLE)
                view?.EAT2?.visibility=View.INVISIBLE
            if(view?.NPOS2?.visibility==View.VISIBLE)
                view?.NPOS2?.visibility=View.INVISIBLE
            if(view?.POS2?.visibility==View.VISIBLE)
                view?.POS2?.visibility=View.INVISIBLE
            if(view?.EPOS2?.visibility==View.INVISIBLE)
                view?.EPOS2?.visibility=View.VISIBLE


        }

    }

    private lateinit var bitmapBuffer: Bitmap
    private lateinit var rotationMatrix: Matrix

    @SuppressLint("UnsafeExperimentalUsageError")
     fun toBitmap(imageProxy: Image): Bitmap? {
        val yuvToRgbConverter = YuvToRgbConverter(requireContext())

        val image = imageProxy ?: return null

        // Initialise Buffer
        if (!::bitmapBuffer.isInitialized) {
            // The image rotation and RGB image buffer are initialized only once
            Log.d(TAG, "Initalise toBitmap()")
            rotationMatrix = Matrix()

            rotationMatrix.postRotate(0F)
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



     @SuppressLint("SuspiciousIndentation")
     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 3) {

                   var image = data!!.extras!!["data"] as Bitmap?
                val dimension = Math.min(image!!.width, image.height)
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension)
                  imageView?.setImageBitmap(image)
                 image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false)


                //alan test start
                if (image != null) {
                    classifyImage(image)
                }
                //alan test end
            } else {
                val dat = data!!.data
                var image: Bitmap? = null
                try {
                    image = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, dat)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                imageView?.setImageBitmap(image)
                val dimension = Math.min(image!!.width, image.height)
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension)
              //  image = Bitmap.createScaledBitmap(image!!, imageSize, imageSize, false)
                image = Bitmap.createScaledBitmap(image!!, imageSize, imageSize, false)
                classifyImage(image)

                // Check plant status and name
//                val status = checkPlantStatus(plantArrayList)
//                if (status!="") {
//                    Log.d(TAG, "status.isNotEmptystatus.isNotEmptystatus.isNotEmptystatus.isNotEmptystatus.isNotEmptystatus.isNotEmpty")
//                    alertButton(status)
//                } else {
//                    Log.d(TAG, "status.isEmpty Empty")
//                    disableAllButton()
//                }
//
//                val plantName = checkPlantName(plantArrayList)
//                if (plantName.isNotEmpty()) {
//                    buttonAction(plantName)
//                } else {
//                    disableAllButton()
//                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Scan.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Scan().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    //alan test start

    //alan test end
}