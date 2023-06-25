package org.tensorflow.lite.examples.classification

import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.plant_drug_effect_dialog.view.*
import kotlinx.android.synthetic.main.plant_info_dialog.view.*
import kotlinx.android.synthetic.main.plant_info_dialog.view.curing
import kotlinx.android.synthetic.main.plant_info_dialog.view.drugEffect
import kotlinx.android.synthetic.main.plant_info_dialog.view.info

class PlantInfoDialog : DialogFragment() {
    var plantName:String?=""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
      //  return super.onCreateView(inflater, container, savedInstanceState)
        var rootView:View=inflater.inflate(R.layout.plant_info_dialog,container,false)
        plantName=arguments?.getString("plantName")

        rootView.infoTitle.text=plantName
//call sqlite database table to get plant related records
        AssetsDatabaseManager.initManager(activity);

        val mg = AssetsDatabaseManager.getManager();

        val db1: SQLiteDatabase = mg.getDatabase("PlantStore.db");

        val cursor=db1.query("Plant",null,"plantname = '${plantName}'",null,null,null,null)

        if(cursor.moveToFirst()){
            do{

                val info=cursor.getString(cursor.getColumnIndex("info"))
                val plantImage=cursor.getBlob(cursor.getColumnIndex("image"))

                val options: BitmapFactory.Options = BitmapFactory.Options()
                options.inTempStorage = ByteArray(1024 * 32)

                val bm: Bitmap =
                    BitmapFactory.decodeByteArray(plantImage, 0, plantImage.size , options)
                rootView.InfoPlantImage.setImageBitmap(bm)

                rootView.InfoPlantImage.
                rootView.detail.text=info

            }while(cursor.moveToNext())
        }else{

            val cursor=db1.query("Plant",null,"enName = '${plantName}'",null,null,null,null)

            if(cursor.moveToFirst()){
                do{


                    plantName=cursor.getString(cursor.getColumnIndex("plantname"))
                    arguments?.putString("plantName",plantName)
                    val info=cursor.getString(cursor.getColumnIndex("info"))
                    val plantImage=cursor.getBlob(cursor.getColumnIndex("image"))

                    val options: BitmapFactory.Options = BitmapFactory.Options()
                    options.inTempStorage = ByteArray(1024 * 32)

                    val bm: Bitmap =
                        BitmapFactory.decodeByteArray(plantImage, 0, plantImage.size , options)
                    rootView.InfoPlantImage.setImageBitmap(bm)

                    rootView.InfoPlantImage.
                    rootView.detail.text=info



                }while(cursor.moveToNext())
            }

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