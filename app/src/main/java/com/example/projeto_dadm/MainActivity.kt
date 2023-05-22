package com.example.projeto_dadm

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Rect
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class MainActivity : AppCompatActivity(){
    private var mRequestQueue: RequestQueue? = null
    private var jsonObjectRequest: JsonObjectRequest? = null
    var restName = ArrayList<String>()
    var restAddress = ArrayList<String>()
    var restLat = ArrayList<String>()
    var restLon = ArrayList<String>()
    var userLat = ""
    var userLon = ""
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placeAutoCompleteTextView: AutoCompleteTextView
    private lateinit var requestQueue: RequestQueue
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var ivMenuTrigger: ImageView
    private lateinit var newRecyclerview: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationEnabled()

        requestQueue = Volley.newRequestQueue(this)
        placeAutoCompleteTextView = findViewById(R.id.SearchInput)
        adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line)
        placeAutoCompleteTextView.setAdapter(adapter)
        val nominatimAutoComplete = NominatimAutoComplete(this, placeAutoCompleteTextView)
        nominatimAutoComplete.setupAutoComplete()


        ivMenuTrigger = findViewById(R.id.ivFavButton)

        ivMenuTrigger.setOnClickListener {
            switchToNewFragment()
        }


        placeAutoCompleteTextView.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString()
                if (input.isNullOrEmpty()){
                    restName.clear()
                    restAddress.clear()
                    restLat.clear()
                    restLon.clear()
                    var url = "https://api.tomtom.com/search/2/nearbySearch/.json?key=HQI3PHJYKXKmuFDgGGpH5yVWVizV21th&lat=" + userLat + "&lon=" + userLon + "&categorySet=7315"
                    getData(url)
                }
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        placeAutoCompleteTextView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                //esconder teclado e dar unfocus
                val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(
                    currentFocus!!.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
                val place = placeAutoCompleteTextView.text.toString()
                if (place.isNotEmpty()) {
                    getPlaceCoordinates(place)
                } else {
                    Toast.makeText(this, "Please enter a place name", Toast.LENGTH_SHORT).show()
                }

            }
            true
        }

    }

    // Favoritos
    fun switchToNewFragment() {
        val fragment = Favorites()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }


    // Localização
    private fun getPlaceCoordinates(place: String) {
        val apiKey = "HQI3PHJYKXKmuFDgGGpH5yVWVizV21th"
        val encodedPlace = place.replace(" ", "%20")
        val url = "https://api.tomtom.com/search/2/geocode/$encodedPlace.json?key=$apiKey"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val results = response.getJSONArray("results")
                if (results.length() > 0) {
                    val result = results.getJSONObject(0)
                    val position = result.getJSONObject("position")
                    val latitude = position.getDouble("lat")
                    val longitude = position.getDouble("lon")
                    restName.clear()
                    restAddress.clear()
                    restLat.clear()
                    restLon.clear()
                    var url = "https://api.tomtom.com/search/2/nearbySearch/.json?key=HQI3PHJYKXKmuFDgGGpH5yVWVizV21th&lat=" + latitude + "&lon=" + longitude + "&categorySet=7315"
                    getData(url)
                } else {
                    Toast.makeText(this, "No coordinates found for $place", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error occurred: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(request)
    }

    @SuppressLint("ServiceCast")
    private fun locationEnabled() {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Yes") { _, _ ->
                        startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),23)

                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.cancel()
                    }
                val alert = builder.create()
                alert.show()
        }else{
            requestLocationPermission()
        }

    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {
            // Permission already granted
            getCurrentLocation()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 23) {
            // Check if the location settings have been enabled
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                // Location settings have been enabled, so restart the activity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                locationEnabled()
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Localização atual indisponível", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    // Use the last known location
                    userLat = location.latitude.toString()
                    userLon = location.longitude.toString()
                    var url = "https://api.tomtom.com/search/2/nearbySearch/.json?key=HQI3PHJYKXKmuFDgGGpH5yVWVizV21th&lat=" + userLat + "&lon=" + userLon + "&categorySet=7315"
                    getData(url)
                }else {
                    val locationRequest = LocationRequest.create().apply {
                        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                        interval = 10000
                        fastestInterval = 5000
                    }

                    fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            val newLocation = locationResult.lastLocation
                            // Use the new location
                            userLat = newLocation.latitude.toString()
                            userLon = newLocation.longitude.toString()
                            var url = "https://api.tomtom.com/search/2/nearbySearch/.json?key=HQI3PHJYKXKmuFDgGGpH5yVWVizV21th&lat=" + userLat + "&lon=" + userLon + "&categorySet=7315"
                            getData(url)
                        }
                    }, Looper.getMainLooper())
                }

            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Sem localização", Toast.LENGTH_SHORT).show()
            }
    }


    //função unfocus edittext´s
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    // recycleview e getdata para o recycleview
    private fun getData(url: String) {
        mRequestQueue = Volley.newRequestQueue(this)
        jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->

                var jsonArray = response.getJSONArray("results")

                (0 until jsonArray.length()).forEach {
                    val restaurant = jsonArray.getJSONObject(it)

                    restName.add(restaurant.getJSONObject("poi").get("name").toString())
                    restAddress.add(restaurant.getJSONObject("address").get("freeformAddress").toString())
                    restLat.add(restaurant.getJSONObject("position").get("lat").toString())
                    restLon.add(restaurant.getJSONObject("position").get("lon").toString())
                }
                recycleViewFun()
            },
            { error ->
                Log.i(ContentValues.TAG, "Error :" + error.toString())
            }
        )
        mRequestQueue!!.add(jsonObjectRequest)
    }

    private fun recycleViewFun() {
        newRecyclerview = findViewById<RecyclerView>(R.id.recyclerView)
        newRecyclerview.layoutManager = LinearLayoutManager(this)
        val data = arrayListOf<Items>()

        restName.forEach{ name ->
            var index = restName.indexOf(name)
            val checkFav = retrieveJsonData("rest_Favs")
            var items: Items
            if (checkFav.any { it.restName == name  && it.restAddress == restAddress[index] }){
                items = Items(name, restAddress[index], restLat[index], restLon[index], R.drawable.heart_filled)
            }else{
                items = Items(name, restAddress[index], restLat[index], restLon[index], R.drawable.heart_svgrepo_com)
            }
            data.add(items)
        }
        var adapter = MyAdapter(this, data)
        newRecyclerview.adapter = adapter

    }
    fun getRecyclerView(): RecyclerView? {
        return newRecyclerview
    }
    private fun retrieveJsonData(key: String): ArrayList<Items> {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("RestFav", Context.MODE_PRIVATE)
        val gson: Gson = Gson()
        val json = sharedPreferences.getString(key, null)
        if (json != null) {
            val itemType = object : TypeToken<ArrayList<Items>>() {}.type
            return gson.fromJson(json, itemType)
        }
        return ArrayList()
    }
}