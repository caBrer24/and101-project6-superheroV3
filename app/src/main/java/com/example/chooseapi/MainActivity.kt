package com.example.chooseapi

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.BuildConfig
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers
import org.json.JSONException
import kotlin.random.Random



class MainActivity : AppCompatActivity() {
    private var randomHero: Int = Random.nextInt(1, 731)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        val nextButton = findViewById<Button>(R.id.nextButton)
        val biography = findViewById<TextView>(R.id.biography)
        val shName = findViewById<TextView>(R.id.shName)
        fetchHeroImage()
        fetchHeroBioAndName(biography, shName)
        setupButton(nextButton, biography, shName)
    }

    private fun setupButton(button: Button, biography: TextView, shName: TextView) {
        button.setOnClickListener {

            randomHero = Random.nextInt(1, 731)
            fetchHeroImage()
            fetchHeroBioAndName(biography, shName)

        }
    }

    private fun fetchHeroImage() {
        val client = AsyncHttpClient()
        val apiKey = getString(R.string.api_key)
        client["https://superheroapi.com/api/$apiKey/${randomHero}/image", object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                val imageUrl = json.jsonObject.getString("url")
                val imageView = findViewById<ImageView>(R.id.shImage)
                Glide.with(this@MainActivity).load(imageUrl).into(imageView)
                Log.d("HeroImage", "success: $json")
                Log.d("HeroImageURL", "superhero image URL is: $imageUrl")
            }
            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                errorResponse: String,
                throwable: Throwable?
            ) {
                Log.d("HeroImage Error", errorResponse)
            }
        }]
    }

    private fun fetchHeroBioAndName(biography: TextView, name: TextView) {

        val client = AsyncHttpClient()

        client["https://superheroapi.com/api/53bf9e00f34eb90e6444a6e57ac64058/${randomHero}/biography", object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                Log.d("HeroBio", "Raw JSON response: ${json.jsonObject.toString(2)}")

                try {
                    val jsonObject = json.jsonObject // Get the main JSONObject
                    val heroName = jsonObject.optString("name", "Unknown Hero")
                    name.text = heroName

                    // Retrieve each attribute individually
                    val fullName = jsonObject.optString("full-name", "N/A")
                    val alterEgos = jsonObject.optString("alter-egos", "N/A")
                    val aliasesArray = jsonObject.optJSONArray("aliases")
                    val placeOfBirth = jsonObject.optString("place-of-birth", "N/A")
                    val firstAppearance = jsonObject.optString("first-appearance", "N/A")
                    val publisher = jsonObject.optString("publisher", "N/A")
                    val alignment = jsonObject.optString("alignment", "N/A")


                    val aliasesString = if (aliasesArray != null && aliasesArray.length() > 0) {
                        val aliasesList = mutableListOf<String>()
                        for (i in 0 until aliasesArray.length()) {
                            aliasesList.add(aliasesArray.optString(i, ""))
                        }
                        aliasesList.joinToString(", ") // Join into a comma-separated string
                    } else {
                        "N/A"
                    }

                    // Construct the string
                    val biographyDetails = """
                    Full Name: $fullName
                    Alter Egos: $alterEgos
                    Aliases: $aliasesString
                    Place of Birth: $placeOfBirth
                    First Appearance: $firstAppearance
                    Publisher: $publisher
                    Alignment: $alignment
                """.trimIndent()

                    // Update TextView
                    biography.text = biographyDetails

                    Log.d("HeroBio", "Successfully parsed biography for hero $randomHero")

                } catch (e: JSONException) {
                    Log.e("HeroBio Error", "Error parsing JSON: ${e.message}")
                    biography.text = "Error loading biography."
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                errorResponse: String,
                throwable: Throwable?
            ) {
                Log.e("HeroBio Error", "Failed to fetch biography for hero $randomHero: $errorResponse")
                biography.text = "Failed to load biography."
            }
        }]
    }

}