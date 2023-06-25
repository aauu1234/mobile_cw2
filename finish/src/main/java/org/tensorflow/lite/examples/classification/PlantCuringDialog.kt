package org.tensorflow.lite.examples.classification

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.plant_curing_dialog.view.*
import kotlinx.android.synthetic.main.plant_info_dialog.view.*
import kotlinx.android.synthetic.main.plant_info_dialog.view.curing
import kotlinx.android.synthetic.main.plant_info_dialog.view.drugEffect
import kotlinx.android.synthetic.main.plant_info_dialog.view.info

class PlantCuringDialog : DialogFragment(){
    var plantName:String?=""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var rootView:View=inflater.inflate(R.layout.plant_curing_dialog,container,false)
        plantName=arguments?.getString("plantName")
        rootView.CuringTitle.text=plantName
//call sqlite database table to get plant related records
        AssetsDatabaseManager.initManager(activity);

        val mg = AssetsDatabaseManager.getManager();

        val db1: SQLiteDatabase = mg.getDatabase("PlantStore.db");

        val cursor=db1.query("Plant",null,"plantname = '${plantName}'",null,null,null,null)

        if(cursor.moveToFirst()){
            do{

                val curing=cursor.getString(cursor.getColumnIndex("curing"))


                rootView.CuringDetail.text=curing

            }while(cursor.moveToNext())
        }
        cursor.close()

        rootView.drugEffect.setOnClickListener {

            var dialog=PlantDrugEffectDialog()
            dialog.arguments=arguments
            dialog.show(parentFragmentManager,"plantDrugEffectDialog")
            dismiss()

        }
        rootView.curing.setOnClickListener {
            var dialog=PlantCuringDialog()
            dialog.arguments=arguments
            dialog.show(parentFragmentManager,"plantCuringDialog")
            dismiss()
        }
        rootView.info.setOnClickListener {
            var dialog=PlantInfoDialog()
            dialog.arguments=arguments
            dialog.show(parentFragmentManager,"plantInfoDialog")
            dismiss()
        }

        return rootView
    }
}