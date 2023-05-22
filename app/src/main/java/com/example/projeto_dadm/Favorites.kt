package com.example.projeto_dadm

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Favorites : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private val gson: Gson = Gson()
    private lateinit var adapter2 : AdapterFavorites

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)
        view.isClickable = true
        view.isFocusableInTouchMode = true
        view.setOnTouchListener { _, _ -> true }

        val HomeButton = view.findViewById<ImageView>(R.id.ivHomeButton)
        HomeButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        val ClearButton = view.findViewById<ImageView>(R.id.ivClearButton)
        ClearButton.setOnClickListener {
            sharedPreferences = context.getSharedPreferences("RestFav", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.remove("rest_Favs")
            editor.apply()

            adapter2.clearData()
            adapter2.notifyDataSetChanged()

            val mainActivity = activity as? MainActivity
            if (mainActivity != null) {
                val recyclerView2 = mainActivity.getRecyclerView()
                val adapterMain = recyclerView2?.adapter as MyAdapter
                println(recyclerView2)
                adapterMain.updateFavsInItems()
            }


            Toast.makeText(requireContext(), "Favorites Have Been Cleared", Toast.LENGTH_SHORT).show()

        }
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerViewFavs)
        recycleViewFun()
    }
    private fun recycleViewFun() {
        sharedPreferences = context.getSharedPreferences("RestFav", Context.MODE_PRIVATE)
        recyclerView.layoutManager = LinearLayoutManager(context)
        var data = arrayListOf<Items>()
        val json = sharedPreferences.getString("rest_Favs", null)
        if (json != null) {
            val itemType = object : TypeToken<ArrayList<Items>>() {}.type
            data = gson.fromJson(json, itemType)
        }else{
            Toast.makeText(context, "You Don´t Have Favorites", Toast.LENGTH_SHORT).show()
        }

        adapter2 = AdapterFavorites(requireContext(), data)
        recyclerView.adapter = adapter2

    }


    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Favorites().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

    }

}