package com.example.projeto_dadm

import android.content.ClipData
import android.content.ClipData.Item
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
import java.util.*
import kotlin.collections.ArrayList


class MyAdapter(private val context: Context, private val itemsList : ArrayList<Items>) : RecyclerView.Adapter<MyAdapter.ViewHolder>(){

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("RestFav", Context.MODE_PRIVATE)
    private val gson: Gson = Gson()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vh = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false))
        return vh
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = itemsList[position]
        var context = holder.mapsButton.context
        holder.restname.text = currentItem.restName
        holder.restaddress.text = currentItem.restAddress
        holder.latRest = currentItem.restLat
        holder.lonRest = currentItem.restLon
        holder.ivFavs.setImageResource(currentItem.ivImage)


        holder.mapsButton.setOnClickListener {
            mapsRedirect(currentItem.restLat, currentItem.restLon, context)
        }
        holder.ivFavs.setOnClickListener {
            holder.bind(currentItem)
            holder.ivFavs.setImageResource(R.drawable.heart_filled)
        }
    }

    private fun mapsRedirect(restLat: String, restLon: String, context: Context){
        val uri: String = String.format(
            "http://maps.google.com/maps?q=loc:%s,%s",
            restLat,
            restLon
        )
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        context.startActivity(intent)
    }

    inner class ViewHolder(ItemView : View) : RecyclerView.ViewHolder(ItemView){
        val restname : TextView = itemView.findViewById(R.id.RestName)
        val restaddress : TextView = itemView.findViewById(R.id.RestAddress)
        val mapsButton : Button = itemView.findViewById(R.id.maps_button)
        val ivFavs : ImageView = itemView.findViewById(R.id.ivFavs)
        var latRest = ""
        var lonRest = ""

        fun bind(item: Items) {
            var dataStored = retrieveJsonData("rest_Favs")

            val hasItem = dataStored.any { it.restName == item.restName && it.restAddress == item.restAddress }

            if(!hasItem){
                dataStored.add(item)
                val json = gson.toJson(dataStored)
                storeJsonData("rest_Favs", json)
                println("Favoritos Com Alterações: " + retrieveJsonData("rest_Favs"))
            }else{
                println("Favoritos Sem Alterações: " + retrieveJsonData("rest_Favs"))
            }

        }
        private fun storeJsonData(key: String, json: String) {
            val editor = sharedPreferences.edit()
            editor.putString(key, json)
            editor.apply()
        }

        private fun retrieveJsonData(key: String): ArrayList<Items> {
            val json = sharedPreferences.getString(key, null)
            if (json != null) {
                val itemType = object : TypeToken<ArrayList<Items>>() {}.type
                return gson.fromJson(json, itemType)
            }
            return ArrayList()
        }

    }

    fun updateFavsInItems() {
        for (i in itemsList.indices) {
            val item = itemsList[i]
            item.ivImage = R.drawable.heart_svgrepo_com
        }
        notifyDataSetChanged()
    }

}

