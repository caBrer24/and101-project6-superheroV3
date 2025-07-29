package com.example.chooseapi

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers
import org.json.JSONException
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var superheroAdapter: SuperheroAdapter
    private val superheroList = mutableListOf<Superhero>()

    private val apiKey = BuildConfig.API_KEY
    private val client = AsyncHttpClient()

    private var heroesToFetch = 20
    private var heroesDataFetchedCounter = 0
    private val fetchedSuperheroesMap = mutableMapOf<String, PartialSuperheroData>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.heroes_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        superheroAdapter = SuperheroAdapter(superheroList) // Adapter will use List<Superhero>
        recyclerView.adapter = superheroAdapter

        recyclerView.addItemDecoration(DividerItemDecoration(this@MainActivity, LinearLayoutManager.VERTICAL))
        fetchTwentyRandomHeroes()
    }

    private fun generateRandomHeroIds(count: Int, maxId: Int = 731): Set<Int> {
        if (count > maxId) throw IllegalArgumentException("Cannot generate more unique IDs than available.")
        val randomIds = mutableSetOf<Int>()
        while (randomIds.size < count) {
            randomIds.add(Random.nextInt(1, maxId + 1))
        }
        Log.d("RandomHeroes", "Generated IDs: $randomIds")
        return randomIds
    }

    private fun fetchTwentyRandomHeroes() {
        superheroList.clear() // Clear previous list
        fetchedSuperheroesMap.clear()
        heroesDataFetchedCounter = 0
        superheroAdapter.notifyDataSetChanged() // Show empty list initially

        val randomIds = generateRandomHeroIds(heroesToFetch)

        for (heroId in randomIds) {
            val idString = heroId.toString()
            fetchedSuperheroesMap[idString] = PartialSuperheroData(id = idString)
            fetchHeroBiography(idString)
            fetchHeroImageUrl(idString)
        }
    }

    // Temporary data holder for combining async results
    data class PartialSuperheroData(
        val id: String,
        var name: String? = null,
        var imageUrl: String? = null,
        var fullName: String? = null,
        var firstAppearance: String? = null,
        var bioFetchAttempted: Boolean = false,
        var imageFetchAttempted: Boolean = false
    )

    private fun fetchHeroBiography(heroId: String) {
        val bioUrl = "https://superheroapi.com/api/$apiKey/$heroId/biography"
        client.get(bioUrl, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                try {
                    val data = fetchedSuperheroesMap[heroId] ?: PartialSuperheroData(id = heroId)
                    val jsonObject = json.jsonObject
                    data.name = jsonObject.optString("name", "N/A")
                    data.fullName = jsonObject.optString("full-name", "N/A")


                    data.firstAppearance = jsonObject.optString("first-appearance", "N/A")
                    fetchedSuperheroesMap[heroId] = data
                    Log.d("FetchHeroes", "Bio success for $heroId: ${data.name}")
                } catch (e: JSONException) {
                    Log.e("FetchHeroes", "Bio JSON parse error for $heroId: ${e.message}")
                } finally {
                    val data = fetchedSuperheroesMap[heroId]
                    data?.bioFetchAttempted = true
                    checkIfHeroDataComplete(heroId)
                }
            }

            override fun onFailure(statusCode: Int, headers: Headers?, errorResponse: String, throwable: Throwable?) {
                Log.e("FetchHeroes", "Bio failure for $heroId: $errorResponse")
                val data = fetchedSuperheroesMap[heroId]
                data?.bioFetchAttempted = true
                checkIfHeroDataComplete(heroId)
            }
        })
    }

    private fun fetchHeroImageUrl(heroId: String) {
        val imageUrlEndpoint = "https://superheroapi.com/api/$apiKey/$heroId/image"
        client.get(imageUrlEndpoint, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                try {
                    val data = fetchedSuperheroesMap[heroId] ?: PartialSuperheroData(id = heroId)
                    data.imageUrl = json.jsonObject.getString("url")

                    if (data.name == null) {
                        data.name = json.jsonObject.optString("name", "Hero $heroId")
                    }
                    fetchedSuperheroesMap[heroId] = data
                    Log.d("FetchHeroes", "Image success for $heroId: ${data.imageUrl}")
                } catch (e: JSONException) {
                    Log.e("FetchHeroes", "Image JSON parse error for $heroId: ${e.message}")
                } finally {
                    val data = fetchedSuperheroesMap[heroId]
                    data?.imageFetchAttempted = true
                    checkIfHeroDataComplete(heroId)
                }
            }

            override fun onFailure(statusCode: Int, headers: Headers?, errorResponse: String, throwable: Throwable?) {
                Log.e("FetchHeroes", "Image failure for $heroId: $errorResponse")
                val data = fetchedSuperheroesMap[heroId]
                data?.imageFetchAttempted = true
                checkIfHeroDataComplete(heroId)
            }
        })
    }

    private fun checkIfHeroDataComplete(heroId: String) {
        val data = fetchedSuperheroesMap[heroId]
        if (data != null && data.bioFetchAttempted && data.imageFetchAttempted) {
            // Both calls for this hero have completed (success or failure)
            heroesDataFetchedCounter++
            Log.d("FetchHeroes", "Data fetch attempts complete for $heroId. Total attempts: $heroesDataFetchedCounter/$heroesToFetch")


            if (data.name != null) { // Only add if we have at least a name and hopefully other details
                superheroList.add(
                    Superhero(
                        id = data.id,
                        name = data.name ?: "Unknown",
                        imageUrl = data.imageUrl,
                        fullName = data.fullName,
                        firstAppearance = data.firstAppearance,

                    )
                )
            }


            if (heroesDataFetchedCounter == heroesToFetch) {
                // All heroes processed, update UI
                runOnUiThread {
                    superheroList.sortBy { it.id.toInt() }
                    superheroAdapter.notifyDataSetChanged()
                    Log.d("FetchHeroes", "All $heroesToFetch heroes processed. Updating RecyclerView with ${superheroList.size} items.")
                }
            }
        }
    }
}
