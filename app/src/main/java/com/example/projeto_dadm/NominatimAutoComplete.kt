package com.example.projeto_dadm

import android.R
import android.content.Context
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.URLEncoder

class NominatimAutoComplete(private val context: Context, private val autoCompleteTextView: AutoCompleteTextView) {
    private val baseUrl = "https://nominatim.openstreetmap.org/search"
    private val format = "json"

    fun setupAutoComplete() {
        autoCompleteTextView.threshold = 1

        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedSuggestion = autoCompleteTextView.adapter.getItem(position) as String
            autoCompleteTextView.setText(selectedSuggestion)
        }

        autoCompleteTextView.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString()
                getAutoCompleteSuggestions(input)
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun getAutoCompleteSuggestions(input: String) {
        val encodedInput = URLEncoder.encode(input, "UTF-8")
        val url = "$baseUrl?format=$format&q=$encodedInput"

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val suggestions = mutableListOf<String>()
                    val jsonArray = JSONArray(response)

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val displayName = jsonObject.getString("display_name")
                        suggestions.add(displayName)
                    }

                    val adapter =
                        ArrayAdapter(context, R.layout.simple_dropdown_item_1line, suggestions)
                    autoCompleteTextView.setAdapter(adapter)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    println("Response JSON: $response") // Print the response JSON for debugging purposes
                }
            },
            { error ->
                error.printStackTrace()
            }
        )

        Volley.newRequestQueue(context).add(request)
    }
}