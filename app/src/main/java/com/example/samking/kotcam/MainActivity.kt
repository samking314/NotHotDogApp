package com.example.samking.kotcam

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import android.R.attr.data
import com.google.android.gms.tasks.OnSuccessListener
import android.support.annotation.NonNull
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener









class MainActivity : AppCompatActivity() {

    //Firebase variables
    //var storage: FirebaseStorage? = null
    //private val storageRef: StorageReference? = null


    private var storage = FirebaseStorage.getInstance()
    var storageRef = storage.getReferenceFromUrl("gs://not-hotdog-kotlin.appspot.com")
    // Create a reference to "mountains.jpg"
    var hotdogRef = storageRef.child("maybeHotDog.jpg")
    // Create a reference to 'images/mountains.jpg'
    var hotdogImagesRef = storageRef.child("images/maybeHotDog.jpg")
    // While the file names are the same, the references point to different files
    //hotdogRef.getName().equals(hotdogImagesRef.getName())   // true
    //hotdogRef.getPath().equals(hotdogImagesRef.getPath())   // false

    //private var mDatabase: DatabaseReference? = null
    var database = FirebaseDatabase.getInstance()
    val mDatabase = database.getReference("hotdog")

    val CAMERA_REQUEST_CODE = 0

    var answer: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //initialize firebase variables
        //storage = FirebaseStorage.getInstance()


        //called if camera button is clicked
        cameraButton.setOnClickListener {
            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if (callCameraIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(callCameraIntent, CAMERA_REQUEST_CODE)
            }

        }

        // Read from the database
        mDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot.getValue(String::class.java)
                Log.i("Value is: ", value!!)
                answer = value
                if (answer.equals("hot dog"))
                    toastMessage("You got a hot dog!!!")
                else if (!answer.equals("Is it a dog???"))
                    toastMessage("NO HOT DOG FOR YOU")
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.i("Failed to read value.", error.toException().toString())
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){

            CAMERA_REQUEST_CODE -> {
                if(resultCode == Activity.RESULT_OK && data != null){
                    //put bitmap into local var
                    var bitmap = data.extras.get("data") as Bitmap

                    //put bitmap into the imageview
                    photoImageView.setImageBitmap(bitmap)

                    //upload our image to firebase
                    uploadImage(bitmap)

                    mDatabase.setValue("Is it a dog???");

                }
            }

            else -> {
                Toast.makeText(this, "Unrecognized request code", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun toastMessage(str: String) {
        Toast.makeText(this,str,Toast.LENGTH_LONG).show()
    }


    private fun uploadImage(bitmap: Bitmap) {

        //get the byte array data
        var byteArr= getByteArr(bitmap)


        //upload to firebase
        val uploadTask = hotdogImagesRef.putBytes(byteArr)

        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
            Log.i("NOOOOOOOOOOOOOOOO", "Upload FAILED")
        }.addOnSuccessListener { //taskSnapshot ->
            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
            //val downloadUrl = taskSnapshot.downloadUrl
            Log.i("Success!!!", "UPLOAD COMPLETE")
        }


    }


    private fun getByteArr(bitmap: Bitmap): ByteArray {

        //put bitmap data into local variable
        var out = bitmap

        //create an ouput stream for our bitmap data
        val outputStream: ByteArrayOutputStream = ByteArrayOutputStream()
        //compress bitmap to JPEG
        out.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

        //now put compressed JPEG in byte arr to send to firebase
        var byteArrData = outputStream.toByteArray()

        //convert byte arr to base 64 image
        //var b64Image = Base64.encodeToString(byteArrData, Base64.DEFAULT)

        return byteArrData
    }


}

