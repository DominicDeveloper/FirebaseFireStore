package com.asadbek.firebasestorage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.asadbek.firebasestorage.databinding.ActivityMainBinding
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var firebaseStorage: FirebaseStorage
    lateinit var firebaseFirestore: FirebaseFirestore
    lateinit var reference: StorageReference
    var imageUrl:String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseStorage = FirebaseStorage.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        reference = firebaseStorage.getReference("reklama")

        binding.btnUpload.setOnClickListener {
            getImageContent.launch("image/*")
            binding.btnSend.visibility = View.VISIBLE
        }
        binding.btnGetImage.setOnClickListener {
            getImage()
        }

        binding.btnSend.setOnClickListener {
            val rasmlar = Rasmlar(imageUrl)
            firebaseFirestore.collection("reklama")
                .add(rasmlar)
                .addOnSuccessListener {
                    Toast.makeText(this, "Rasm serverga joylandi!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Rasm serverga joylanmadi!", Toast.LENGTH_SHORT).show()
                }
            binding.btnSend.visibility = View.INVISIBLE
        }




    }

    private var getImageContent = registerForActivityResult(ActivityResultContracts.GetContent()){ uri ->
        val title = System.currentTimeMillis()
        try {
            val uploadTask = reference.child(title.toString()).putFile(uri!!)
            Toast.makeText(this, "Surat yuklanmoqda!", Toast.LENGTH_SHORT).show()

            uploadTask.addOnSuccessListener {
                val downloadUrl = it.metadata?.reference?.downloadUrl
                downloadUrl?.addOnSuccessListener { imageUri ->
                    binding.imageView.setImageURI(uri)
                    imageUrl = imageUri.toString()
                    Toast.makeText(this, "Tayyor!", Toast.LENGTH_SHORT).show()

                }
            }.removeOnFailureListener {
                Toast.makeText(this, "Server xatosi", Toast.LENGTH_SHORT).show()
            }

        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun getImage(){
        FirebaseFirestore.getInstance().collection("reklama")
            .get()
            .addOnSuccessListener { rasme ->
                for (i in rasme){
                    val rasmlar = rasme.toObjects(Rasmlar::class.java)
                    Glide.with(this).load(rasmlar.get(0).image).into(binding.imageView)
                    Log.d(TAG, "getImage: ${rasmlar.get(0).image}")
                }

            }
            .addOnFailureListener {
                Toast.makeText(this, "Serverda xatolik!", Toast.LENGTH_SHORT).show()
            }
    }
}