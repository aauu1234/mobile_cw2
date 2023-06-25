package org.tensorflow.lite.examples.classification

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_home.view.*
import org.tensorflow.lite.examples.classification.plantList.ChildItem
import org.tensorflow.lite.examples.classification.plantList.ParentAdapter
import org.tensorflow.lite.examples.classification.plantList.ParentItem
import java.util.*

private lateinit var recyclerView: RecyclerView
private lateinit var searchView: SearchView
private val parentList = ArrayList<ParentItem>()
const val RESULT_SPEECH = 1
private var btnSpeak: ImageButton? = null
private var voiceText:String=""
lateinit var fragmentManagers: FragmentManager

interface DatabaseUpdateObserver {
    fun onDatabaseUpdated()
}

class Home : Fragment(), DatabaseUpdateObserver {


 private val memoryCache = LruCache<ByteArray, Bitmap>(50 * 1024 * 1024)

    override fun onDatabaseUpdated() {
        // Refresh the list here
        val parentAdapter2 = ParentAdapter(parentList, fragmentManagers)
        if (parentList.isEmpty()) {
            addDataToList()
        }
        recyclerView.adapter = parentAdapter2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentManagers = requireActivity().supportFragmentManager

    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var rootView: View = inflater.inflate(R.layout.fragment_home, container, false)
        val fabRefresh: FloatingActionButton = rootView.findViewById(R.id.fab_refresh)
        var parentAdapter2: ParentAdapter? =null
        searchView = rootView.searchView
        fabRefresh.setOnClickListener {
            Log.d("INFO", "1")
               parentAdapter2= ParentAdapter(parentList,fragmentManagers)
         //   searchView = rootView.searchView
            Log.d("INFO", "2")
            recyclerView = rootView.parentRecyclerView
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            if (parentList.isEmpty()) {
                Log.d("INFO", "3")
                addDataToList()
            }else{
                parentList.clear()
                Log.d("INFO", "4")
                addDataToList()
            }
            val adapter = ParentAdapter(parentList,fragmentManagers)
            adapter.notifyDataSetChanged()
            recyclerView.adapter = adapter

        }




          parentAdapter2= ParentAdapter(parentList,fragmentManagers)
        searchView = rootView.searchView

        recyclerView = rootView.parentRecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        if (parentList.isEmpty()) {
            addDataToList()
        }
        val adapter = ParentAdapter(parentList,fragmentManagers)

        recyclerView.adapter = adapter





        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                parentAdapter2?.let { filterList(newText, it) }

                return true
            }

        })


        btnSpeak =rootView.btnSpeak

        btnSpeak?.let { button ->
            button.setOnClickListener {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                try {
                    startActivityForResult(intent, RESULT_SPEECH)
                    voiceText=""
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        requireContext(),
                        "Your device doesn't support Speech to Text",
                        Toast.LENGTH_SHORT
                    ).show()
                    e.printStackTrace()
                }
            }
        }


        return rootView
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RESULT_SPEECH -> if (resultCode == Activity.RESULT_OK && data != null) {
                val text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
              //  tvText?.text = text!![0]
                voiceText=text!![0]
                val lowercaseText = text[0].lowercase(Locale.ROOT)
                searchView.setQuery(lowercaseText, false) // Set the lowercase text to searchView

            }
        }
    }

    fun filterList(query: String?,parentAdapter2:ParentAdapter) {

        if (query != null) {
            val filteredParentList = java.util.ArrayList<ParentItem>()

            for (i in parentList) {
                var filteredChildList = java.util.ArrayList<ChildItem>()
                var childItemExist=false
                Log.d(ContentValues.TAG, i.title)
                for(k in i.mList){
                    Log.d(ContentValues.TAG, k.title)
                    if(query!=null || query!="") {
                        if (k.title.lowercase(Locale.ROOT).contains(query)) {

                            filteredChildList.add(k)
                            childItemExist = true

                        }
                    }
                }
                if(childItemExist){
                    filteredParentList.add(ParentItem(i.title,i.logo,filteredChildList))
                    Log.d(ContentValues.TAG, filteredChildList.toString())
                    Log.d(ContentValues.TAG, filteredParentList.toString())
                    //  filteredChildList.clear()
                }
                //after one filtered parent and child item packed,then clear the array for new packing
                //       filteredChildList.clear()

            }

            if (filteredParentList.isEmpty()) {
                Toast.makeText(requireContext(), "No Data found", Toast.LENGTH_SHORT).show()
            } else {
              //  Toast.makeText(requireContext(), "Goto data", Toast.LENGTH_SHORT).show()
                Log.d(ContentValues.TAG, filteredParentList.toString())
                parentAdapter2.setFilteredList(filteredParentList)
                //  adapter2.setFilteredList(filteredList)
                recyclerView.adapter = parentAdapter2
            }
        }
    }

    private fun addDataToList() {
         val childItems1 = ArrayList<ChildItem>()
         val childItems2 = ArrayList<ChildItem>()
        val childItems3 = ArrayList<ChildItem>()
        val childItems4 = ArrayList<ChildItem>()
        val PlantList=getToxicPlantLocData()

        for(item in PlantList){
            if(item.status=="EAT"){
                childItems1.add(ChildItem(item.enName, item.PlantImage))
            }else if(item.status=="NPOS"){
                childItems2.add(ChildItem(item.enName, item.PlantImage))
            }else if(item.status=="POS"){
                childItems3.add(ChildItem(item.enName, item.PlantImage))
            }else if(item.status=="EPOS"){
                childItems4.add(ChildItem(item.enName, item.PlantImage))
            }else{
                Log.d(ContentValues.TAG, "Plant data error")
                Log.d(ContentValues.TAG, "Plant data error")
            }



        }

        parentList.add(ParentItem("Eatable Plants", R.drawable.eatable, childItems1))
        parentList.add(ParentItem("Neutral And Non-Eatable Plants", R.drawable.non_eatable, childItems2))
        parentList.add(ParentItem("Toxic Plants", R.drawable.toxic, childItems3))
        parentList.add(ParentItem("Extreme Toxic Plants", R.drawable.deadly_icon, childItems4))

    }

    @SuppressLint("Range")
    private fun getToxicPlantLocData():List<PlantModels>{
        AssetsDatabaseManager.closeAllDatabase()
        AssetsDatabaseManager.initManager(activity);
        val mg = AssetsDatabaseManager.getManager();
        val db1: SQLiteDatabase = mg.getDatabase("PlantStore.db");
        val toxicPlantLocList= arrayListOf<PlantModels>();
        Log.d("INFO", "start call list")
        val cursor=db1.query("Plant",null,null,null,null,null,null)
        if(cursor.moveToFirst()){
            do{
                var item:PlantModels
                val plantInfo : String?= cursor.getString(cursor.getColumnIndex("info"))
                val plantDrugEffecet : String?= cursor.getString(cursor.getColumnIndex("drugEffect"))
                val plantCuring : String?= cursor.getString(cursor.getColumnIndex("curing"))
                val plantStatus : String?= cursor.getString(cursor.getColumnIndex("status"))
                val plantEnName: String? = cursor.getString(cursor.getColumnIndex("enName"))
                val plantCnName: String? = cursor.getString(cursor.getColumnIndex("plantname"))
                val plantImage=cursor.getBlob(cursor.getColumnIndex("image"))
                if (plantCnName != null) {
                    Log.d("INFO", "list data record:$plantCnName")
                }
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeByteArray(plantImage, 0, plantImage.size, options)
                options.inSampleSize = calculateInSampleSize(options, 48, 48)
                options.inJustDecodeBounds = false
                val bm: Bitmap = BitmapFactory.decodeByteArray(plantImage, 0, plantImage.size, options)

                // Add to memory cache
                memoryCache.put(plantImage, bm)
                item= PlantModels(plantCnName,plantEnName,plantStatus,plantInfo,plantDrugEffecet,plantCuring,bm)
                toxicPlantLocList.add(item)

            }while(cursor.moveToNext())
        }
        cursor.close()
        for(item in toxicPlantLocList){

        }
        return toxicPlantLocList
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        val totalPixels = width * height
        val totalReqPixelsCap = reqWidth * reqHeight * 2
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }

        return inSampleSize
    }



}

