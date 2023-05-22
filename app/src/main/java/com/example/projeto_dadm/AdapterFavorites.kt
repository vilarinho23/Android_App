package com.example.projeto_dadm

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AdapterFavorites(private val context: Context, private val itemsList : ArrayList<Items>) : RecyclerView.Adapter<AdapterFavorites.ViewHolder>() {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("RestFav", Context.MODE_PRIVATE)
    private val gson: Gson = Gson()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vh = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_fav, parent, false)
        )
        return vh
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = itemsList[position]
        holder.restname.text = currentItem.restName
        holder.restaddress.text = currentItem.restAddress
        holder.latRest = currentItem.restLat
        holder.lonRest = currentItem.restLon

        var context = holder.mapsButton.context

        holder.mapsButton.setOnClickListener {
            mapsRedirect(currentItem.restLat, currentItem.restLon, context)
        }
    }

    private fun mapsRedirect(restLat: String, restLon: String, context: Context) {
        val uri: String = String.format(
            "http://maps.google.com/maps?q=loc:%s,%s",
            restLat,
            restLon
        )
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        context.startActivity(intent)
    }

    inner class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val restname: TextView = itemView.findViewById(R.id.RestName)
        val restaddress: TextView = itemView.findViewById(R.id.RestAddress)
        val mapsButton: Button = itemView.findViewById(R.id.maps_button)
        var latRest = ""
        var lonRest = ""
    }

    fun clearData() {
        itemsList.clear()
    }

}