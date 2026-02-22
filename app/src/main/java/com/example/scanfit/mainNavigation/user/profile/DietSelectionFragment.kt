package com.example.scanfit.mainNavigation.user.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.scanfit.adapters.DietAdapter
import com.example.scanfit.R
import com.example.scanfit.utils.ScanActivity
import com.example.scanfit.data.DietItem
import com.example.scanfit.data.HealthData
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.gson.Gson
import kotlin.collections.filter
import kotlin.collections.mutableSetOf
import androidx.navigation.fragment.findNavController

class DietSelectionFragment : Fragment(R.layout.fragment_diet_selection) {

    private lateinit var recyclerView: RecyclerView
    private var healthData: HealthData? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rv_diets)
        recyclerView.layoutManager = FlexboxLayoutManager(context)

        healthData = loadHealthDataFromJson()

        updateRecyclerView()

        view.findViewById<Button>(R.id.btn_finish_setup).setOnClickListener {
            saveAllCategories()
        }
    }

    private fun updateRecyclerView() {
        val displayList = mutableListOf<Any>()
        healthData?.categories?.forEach { category ->
            displayList.add(category.category_name)
            displayList.addAll(category.items)
        }

        if (recyclerView.adapter == null) {
            recyclerView.adapter = DietAdapter(displayList) { diet ->
                showDietDetailsDialog(diet)
            }
        } else {
            (recyclerView.adapter as DietAdapter).updateData(displayList)
        }
    }

    private fun showDietDetailsDialog(diet: DietItem) {
        val dialog = DietBottomSheetFragment.newInstance(diet)
        dialog.show(childFragmentManager, "DietBottomSheet")

        childFragmentManager.setFragmentResultListener("diet_result", this) { _, bundle ->
            if (bundle.getBoolean("isSaved")) {
                healthData?.categories?.forEach { category ->
                    category.items.find { it.id == diet.id }?.isSelected = true
                }
                updateRecyclerView()
            }
        }
    }

    private fun saveAllCategories() {
        val diseases = mutableSetOf<String>()
        val allergens = mutableSetOf<String>()
        val diets = mutableSetOf<String>()

        healthData?.categories?.forEach { category ->
            val selected = category.items.filter { it.isSelected }.map { it.name }
            when (category.category_name) {
                "Diseases" -> diseases.addAll(selected)
                "Allergens" -> allergens.addAll(selected)
                "Diets" -> diets.addAll(selected)
            }
        }

        val prefs = requireContext().getSharedPreferences("user_settings", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putStringSet("user_diseases", diseases)
            putStringSet("user_allergens", allergens)
            putStringSet("user_diets", diets)

            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) putBoolean("profile_completed_$uid", true)

            apply()
        }
        findNavController().navigate(R.id.action_dietSelectionFragment_to_homeFragment)
    }

    private fun loadHealthDataFromJson(): HealthData {
        val jsonString = requireContext().assets.open("health_rules.json").bufferedReader().use { it.readText() }
        return Gson().fromJson(jsonString, HealthData::class.java)
    }
}