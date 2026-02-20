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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rv_diets)

        val layoutManager = FlexboxLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        val healthData = loadHealthDataFromJson()
        val displayList = mutableListOf<Any>()

        healthData.categories.forEach { category ->
            displayList.add(category.category_name)
            displayList.addAll(category.items)
        }

        val adapter = DietAdapter(displayList) { diet ->
            showDietDetailsDialog(diet)
        }
        recyclerView.adapter = adapter

        view.findViewById<Button>(R.id.btn_finish_setup).setOnClickListener {

            val selectedDiets = mutableSetOf<String>()

            healthData.categories.forEach { category ->
                category.items.filter { it.isSelected }.forEach {
                    selectedDiets.add(it.name)
                }
            }

            val prefs = requireContext()
                .getSharedPreferences("user_settings", Context.MODE_PRIVATE)

            prefs.edit().apply {
                putStringSet("user_diseases", selectedDiets)

                val uid = com.google.firebase.auth.FirebaseAuth
                    .getInstance().currentUser?.uid

                if (uid != null) {
                    putBoolean("profile_completed_$uid", true)
                }

                apply()
            }

            findNavController().navigate(
                R.id.action_dietSelectionFragment_to_homeFragment
            )
        }
    }

    private fun loadHealthDataFromJson(): HealthData {
        val jsonString = requireContext().assets.open("health_rules.json")
            .bufferedReader().use { it.readText() }
        return Gson().fromJson(jsonString, HealthData::class.java)
    }

    private fun showDietDetailsDialog(diet: DietItem) {
        val dialog = DietBottomSheetFragment.newInstance(diet)
        dialog.show(childFragmentManager, "DietBottomSheet")

        // Теперь recyclerView доступен здесь
        childFragmentManager.setFragmentResultListener("diet_result", this) { _, bundle ->
            val isSaved = bundle.getBoolean("isSaved")
            if (isSaved) {
                diet.isSelected = true
                recyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }
}