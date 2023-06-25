package org.tensorflow.lite.examples.classification

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.fragment_newplantform.view.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val PERMISSION_REQUEST_CODE = 100
private const val IMAGE_PICK_CODE = 101

class NewPlantForm : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var imagePreview: ImageView
    private lateinit var statusSpinner: Spinner
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_newplantform, container, false)

        val uploadImageButton = view.findViewById<Button>(R.id.upload_image_button)
        val submitButton = view.findViewById<Button>(R.id.submit_button)
        imagePreview = view.findViewById(R.id.image_preview)
        // Initialize Spinner and set up ArrayAdapter
        statusSpinner = view.findViewById(R.id.status_input)
        setupStatusSpinner()
        // Set image preview visibility initially to GONE
        imagePreview.visibility = View.GONE

        uploadImageButton.setOnClickListener {
            if (checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
            } else {
                pickImageFromGallery()
            }
        }

        submitButton.setOnClickListener {
            // Handle form submission
            Log.d("INFO","Enter Submit Button")
            val plantNameEditText = view.findViewById<EditText>(R.id.plant_name_input)
            val statusSpinner = view.findViewById<Spinner>(R.id.status_input)
            val infoEditText = view.findViewById<EditText>(R.id.info_input)
            val drugEffectEditText = view.findViewById<EditText>(R.id.drug_effect_input)
            val curingEditText = view.findViewById<EditText>(R.id.curing_input)
            val enNameEditText = view.findViewById<EditText>(R.id.en_name_input)

            val plantName = plantNameEditText.text.toString()
            val status = statusSpinner.selectedItem.toString()
            val info = infoEditText.text.toString()
            val drugEffect = drugEffectEditText.text.toString()
            val curing = curingEditText.text.toString()
            val enName = enNameEditText.text.toString()

            val plantImage = imagePreview.drawable?.let { drawable ->
                (drawable as? BitmapDrawable)?.bitmap
            }

            Log.d("INFO", "plantName:$plantName")
            if (plantName.isNotBlank() && plantImage != null) {
                Log.d("INFO","start Call database insert command")
                val newRowId = AssetsDatabaseManager.getManager().insertPlant(
                    plantName, plantImage, status, info, drugEffect, curing, enName)
                if (newRowId != -1L) {
                    // Show success message and perform any necessary actions
                    Toast.makeText(requireContext(), "Success: New plant item created", Toast.LENGTH_SHORT).show()
                    AssetsDatabaseManager.forceInitManager(requireContext())
                } else {
                    // Show error message
                    Toast.makeText(requireContext(), "Error: New plant item created Failed", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Show error message for missing plant name or image
            }
        }

        return view
    }



    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }



    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            data?.data?.let { uri ->
                imagePreview.setImageURI(uri)
                // Set image preview visibility to VISIBLE when an image is selected
                imagePreview.visibility = View.VISIBLE
            }
        }
    }

    private fun setupStatusSpinner() {
        // Create an ArrayAdapter using the pre-defined string array and a default spinner layout
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.status_options,
            android.R.layout.simple_spinner_item
        )

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        statusSpinner.adapter = adapter
    }



    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NewPlantForm().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}