package com.example.api_api



import android.widget.EditText
import android.util.Log
import cz.msebera.android.httpclient.Header
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import java.security.MessageDigest
import android.widget.TextView
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.RequestParams
import com.loopj.android.http.TextHttpResponseHandler
import com.google.gson.Gson
import network.MarvelResponse
import com.loopj.android.http.BinaryHttpResponseHandler
import android.graphics.BitmapFactory


class MainActivity : AppCompatActivity() {

    private lateinit var nameTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var characterImageView: ImageView
    private lateinit var refreshButton: Button
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private var currentIndex = 0
    private var characterList: List<MarvelResponse.Character>? = null
    private val client = AsyncHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)

        // Set click listener for the search button
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                // Perform API request with the query
                loadCharacterData(query)
            }
        }

        nameTextView = findViewById(R.id.nameTextView)
        descriptionTextView = findViewById(R.id.descriptionTextView)
        characterImageView = findViewById(R.id.characterImageView)
        refreshButton = findViewById(R.id.refreshButton)

        refreshButton.setOnClickListener {
            loadCharacterData() // This triggers the network request
        }

        // Load the first character data when the activity starts
        loadCharacterData()
    }

    private fun loadCharacterData(query: String = "") {
        val timestamp = System.currentTimeMillis().toString()
        val publicKey = "809a469d3098318b4c11192b1d2aec55" // Use your actual public key
        val privateKey = "cbe9d74069952764f33dbb2ad025e029f8844f6a" // Use your actual private key
        val hash = md5(timestamp + privateKey + publicKey)

        val params = RequestParams()
        params.put("ts", timestamp)
        params.put("apikey", publicKey)
        params.put("hash", hash)
        if (query.isNotEmpty()) {
            params.put("nameStartsWith", query) // Add the query parameter if not empty
        }

        client.get("https://gateway.marvel.com/v1/public/characters", params, object : TextHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out cz.msebera.android.httpclient.Header>?, responseString: String?) {
                responseString?.let { jsonString ->
                    val response = Gson().fromJson(jsonString, MarvelResponse::class.java)
                    characterList = response.data.results
                    characterList?.let {
                        if (it.isNotEmpty()) {
                            // Display the first character
                            currentIndex = 0
                            updateUI(it[currentIndex])
                            // Start a timer to automatically advance to the next character after 5 seconds
                            startTimer()
                        }
                    }
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out cz.msebera.android.httpclient.Header>?, responseString: String?, throwable: Throwable?) {
                // Handle failed response
            }
        })
    }

    private fun updateUI(character: MarvelResponse.Character) {
        val nameTextView: TextView = findViewById(R.id.nameTextView)
        val descriptionTextView: TextView = findViewById(R.id.descriptionTextView)
        val characterImageView: ImageView = findViewById(R.id.characterImageView)

        nameTextView.text = character.name
        descriptionTextView.text = character.description.ifEmpty { "No description available." }

        val imageUrl = "${character.thumbnail.path}/standard_large.${character.thumbnail.extension}"

        // Load image using Async Http Client
        val client = AsyncHttpClient()
        client.get(imageUrl, object : BinaryHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, binaryData: ByteArray?) {
                // Convert binary data to Bitmap
                val bitmap = BitmapFactory.decodeByteArray(binaryData, 0, binaryData?.size ?: 0)
                // Set the bitmap to the ImageView
                characterImageView.setImageBitmap(bitmap)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, binaryData: ByteArray?, error: Throwable?) {
                // Handle failure to load image
                Log.e("IMAGE_LOAD_ERROR", "Failed to load image: $statusCode")
            }
        })
    }

    private fun startTimer() {
        // Timer to automatically advance to the next character after 5 seconds
        val timer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Do nothing on tick
            }

            override fun onFinish() {
                // Advance to the next character
                characterList?.let {
                    if (currentIndex < it.size - 1) {
                        currentIndex++
                    } else {
                        currentIndex = 0 // Start from the beginning if reached the end
                    }
                    // Display the next character
                    updateUI(it[currentIndex])
                    // Restart the timer
                    startTimer()
                }
            }
        }
        timer.start()
    }

    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        return md.digest(input.toByteArray()).joinToString("") {
            "%02x".format(it)
        }
    }
}



////package com.example.api_api
////
////import android.os.Bundle
////import android.widget.Button
////import android.widget.ImageView
////import android.widget.TextView
////import androidx.appcompat.app.AppCompatActivity
////import com.bumptech.glide.Glide
////import com.example.api_api.network.MarvelResponse
////import com.example.api_api.network.RetrofitInstance
////import java.security.MessageDigest
////
////class MainActivity : AppCompatActivity() {
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        setContentView(R.layout.activity_main)
////
////        val refreshButton: Button = findViewById(R.id.refreshButton)
////        refreshButton.setOnClickListener {
////            loadCharacterData()
////        }
////    }
////
////    private fun loadCharacterData() {
////        val timestamp = System.currentTimeMillis().toString()
////        val publicKey = "your_public_key"
////        val privateKey = "your_private_key"
////        val hash = md5("$timestamp$privateKey$publicKey")
////
////        RetrofitInstance.api.getCharacters(timestamp, publicKey, hash).enqueue(object : retrofit2.Callback<MarvelResponse> {
////            override fun onResponse(call: retrofit2.Call<MarvelResponse>, response: retrofit2.Response<MarvelResponse>) {
////                val characterData = response.body()?.data?.results?.firstOrNull()
////                if (characterData != null) {
////                    runOnUiThread {
////                        updateUI(characterData)
////                    }
////                }
////            }
////
////            override fun onFailure(call: retrofit2.Call<MarvelResponse>, t: Throwable) {
////                // Handle API call failure
////            }
////        })
////    }
////
////    private fun updateUI(character: MarvelResponse.Character) {
////        val nameTextView: TextView = findViewById(R.id.nameTextView)
////        val descriptionTextView: TextView = findViewById(R.id.descriptionTextView)
////        val characterImageView: ImageView = findViewById(R.id.characterImageView)
////
////        nameTextView.text = character.name
////        descriptionTextView.text = character.description.ifEmpty { "Description not available." }
////
////        val imageUrl = "${character.thumbnail.path}.${character.thumbnail.extension}"
////        Glide.with(this).load(imageUrl).into(characterImageView)
////    }
////
////    private fun md5(input: String): String {
////        return MessageDigest.getInstance("MD5").digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
////    }
////}
//
//
//
//
//
//
//
//
//package com.example.api_api
//
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.widget.Button
//import java.security.MessageDigest
//import android.widget.TextView
//import android.widget.ImageView
//import android.graphics.BitmapFactory
//import android.util.Log
//import com.loopj.android.http.AsyncHttpClient
//import com.loopj.android.http.BinaryHttpResponseHandler
//import cz.msebera.android.httpclient.Header
//import com.bumptech.glide.Glide
//import com.loopj.android.http.AsyncHttpClient
//import com.loopj.android.http.RequestParams
//import com.loopj.android.http.TextHttpResponseHandler
//import cz.msebera.android.httpclient.Header
//import com.google.gson.JsonObject
//import com.google.gson.Gson
//
//
//
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import network.RetrofitInstance
//import network.MarvelApiService
//import network.MarvelResponse
//import network.MarvelResponse.Character // Import other needed classes similarly
//
//
//class MainActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        val refreshButton: Button = findViewById(R.id.refreshButton)
//        refreshButton.setOnClickListener {
//            loadCharacterData() // This triggers the network request
//        }
//    }
//
//
//    private fun loadCharacterData() {
//        val timestamp = System.currentTimeMillis().toString()
//        val publicKey = "809a469d3098318b4c11192b1d2aec55" // Use your actual public key
//        val privateKey = "cbe9d74069952764f33dbb2ad025e029f8844f6a" // Use your actual private key
//        val hash = md5(timestamp + privateKey + publicKey)
//
//        val client = AsyncHttpClient()
//        val params = RequestParams()
//        params.put("ts", timestamp)
//        params.put("apikey", publicKey)
//        params.put("hash", hash)
//
//        client.get("https://gateway.marvel.com/v1/public/characters", params, object : TextHttpResponseHandler() {
//            override fun onSuccess(statusCode: Int, headers: Array<Header>?, responseString: String?) {
//                // Handle successful response
//                responseString?.let { jsonString ->
//                    val response = Gson().fromJson(jsonString, MarvelResponse::class.java)
//                    val characterData = response.data.results.firstOrNull()
//                    characterData?.let { character ->
//                        runOnUiThread {
//                            updateUI(character)
//                        }
//                    }
//                }
//            }
//
//            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
//                // Handle failed response
//            }
//        })
//    }
//
//    private fun updateUI(character: MarvelResponse.Character) {
//        val nameTextView: TextView = findViewById(R.id.nameTextView)
//        val descriptionTextView: TextView = findViewById(R.id.descriptionTextView)
//        val characterImageView: ImageView = findViewById(R.id.characterImageView)
//
//        nameTextView.text = character.name
//        descriptionTextView.text = character.description.ifEmpty { "No description available." }
//
//        val imageUrl = "${character.thumbnail.path}/standard_large.${character.thumbnail.extension}"
//
//        // Load image using Async Http Client
//        val client = AsyncHttpClient()
//        client.get(imageUrl, object : BinaryHttpResponseHandler() {
//            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, binaryData: ByteArray?) {
//                // Convert binary data to Bitmap
//                val bitmap = BitmapFactory.decodeByteArray(binaryData, 0, binaryData?.size ?: 0)
//                // Set the bitmap to the ImageView
//                characterImageView.setImageBitmap(bitmap)
//            }
//
//            override fun onFailure(statusCode: Int, headers: Array<out Header>?, binaryData: ByteArray?, error: Throwable?) {
//                // Handle failure to load image
//                Log.e("IMAGE_LOAD_ERROR", "Failed to load image: $statusCode")
//            }
//        })
//    }
//
//    private fun md5(input: String): String {
//        val md = MessageDigest.getInstance("MD5")
//        return md.digest(input.toByteArray()).joinToString("") {
//            "%02x".format(it)
//        }
//    }
//}
//
//
//
//
