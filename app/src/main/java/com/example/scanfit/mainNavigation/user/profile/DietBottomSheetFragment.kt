package com.example.scanfit.mainNavigation.user.profile

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.example.scanfit.R
import com.example.scanfit.data.DietItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.slider.Slider

class DietBottomSheetFragment : BottomSheetDialogFragment() {

    private var dietItem: DietItem? = null
    private var isSubOptionMode = false
    private val selectedOptions = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_diet_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dietItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(ARG_DIET, DietItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable(ARG_DIET) as? DietItem
        }

        val saveBtn = view.findViewById<MaterialButton>(R.id.saveButton)

        updateUI(view)

        saveBtn.setOnClickListener {
            if (dietItem?.ui_type == "selection_modal" && !isSubOptionMode) {
                if (selectedOptions.isNotEmpty()) {
                    isSubOptionMode = true
                    updateUI(view)
                } else {

                }
            } else {
                val result = Bundle().apply {
                    putBoolean("isSaved", true)
                    putStringArrayList("selected_ids", ArrayList(selectedOptions))
                }
                parentFragmentManager.setFragmentResult("diet_result", result)
                dismiss()
            }
        }
    }

    private fun updateUI(view: View) {
        val title = view.findViewById<TextView>(R.id.dietTitle)
        val description = view.findViewById<TextView>(R.id.dietDescription)
        val subOptionsContainer = view.findViewById<LinearLayout>(R.id.subOptionsContainer)
        val sliderContainer = view.findViewById<LinearLayout>(R.id.sliderContainer)
        val saveBtn = view.findViewById<MaterialButton>(R.id.saveButton)

        title.text = dietItem?.name

        when (dietItem?.ui_type) {
            "severity_slider" -> {
                subOptionsContainer.visibility = View.GONE
                sliderContainer.visibility = View.VISIBLE
                saveBtn.text = "Save selection"
                setupSeveritySlider(view)
            }
            "selection_modal" -> {
                if (isSubOptionMode) {
                    subOptionsContainer.visibility = View.VISIBLE
                    sliderContainer.visibility = View.GONE
                    saveBtn.text = "Save selection"
                    description.text = "Set avoidance level for each selected item"

                    setupMultipleSliders(subOptionsContainer)
                } else {
                    sliderContainer.visibility = View.GONE
                    subOptionsContainer.visibility = View.VISIBLE
                    saveBtn.text = "Next"
                    description.text = "Select all categories you want to avoid"
                    setupCheckboxes(subOptionsContainer)
                }
            }
        }
    }

    private fun setupMultipleSliders(container: LinearLayout) {
        container.removeAllViews()

        val selectedSubOptions = dietItem?.sub_options?.filter { selectedOptions.contains(it.id) }

        selectedSubOptions?.forEach { subOption ->
            val subTitle = TextView(requireContext()).apply {
                text = subOption.name
                textSize = 16f
                setPadding(0, 16, 0, 8)
                setTextColor(resources.getColor(R.color.black, null)) // Замените на ваш цвет
            }

            val sliderView = layoutInflater.inflate(R.layout.item_diet_slider, container, false)

            val slider = sliderView.findViewById<Slider>(R.id.dietSlider)
            val labelEnd = sliderView.findViewById<TextView>(R.id.labelEnd)
            val labelMiddle = sliderView.findViewById<TextView>(R.id.labelMiddle)
            val subDescription = sliderView.findViewById<TextView>(R.id.dietDescription)

            val maxLevels = subOption.max_levels ?: 2
            slider.valueFrom = 0f
            slider.valueTo = (maxLevels - 1).toFloat()
            slider.stepSize = 1f

            if (maxLevels == 3) {
                labelMiddle.text = "Intolerance"
                labelEnd.text = "Severe"
            } else {
                labelMiddle.text = ""
                labelEnd.text = "Intolerance"
            }

            slider.addOnChangeListener { _, value, _ ->
                subDescription.text = if (value == 0f) "Preference" else "Intolerance"
            }

            container.addView(subTitle)
            container.addView(sliderView)
        }
    }

    private fun setupCheckboxes(container: LinearLayout) {
        container.removeAllViews()
        dietItem?.sub_options?.forEach { option ->
            val checkBox = MaterialCheckBox(requireContext()).apply {
                text = option.name
                isChecked = selectedOptions.contains(option.id)

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 8, 0, 8) }

                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedOptions.add(option.id)
                    } else {
                        selectedOptions.remove(option.id)
                    }
                }
            }
            container.addView(checkBox)
        }
    }


    private fun setupSeveritySlider(view: View) {
        val slider = view.findViewById<Slider>(R.id.dietSlider)
        val description = view.findViewById<TextView>(R.id.dietDescription)
        val labelMiddle = view.findViewById<TextView>(R.id.labelMiddle)
        val labelEnd = view.findViewById<TextView>(R.id.labelEnd)

        val maxLevels = dietItem?.max_levels ?: 2

        slider.valueFrom = 0f
        slider.valueTo = (maxLevels - 1).toFloat()
        slider.stepSize = 1f

        if (maxLevels == 3) {
            labelMiddle.text = "Intolerance"
            labelEnd.visibility = View.VISIBLE
            labelEnd.text = "Severe"
        } else {
            labelMiddle.text = ""
            labelEnd.visibility = View.VISIBLE
            labelEnd.text = "Intolerance"
        }

        val states = if (maxLevels == 3) {
            listOf(
                "I loosely follow this diet.",
                "I strictly avoid these ingredients.",
                "I have a severe allergy or medical necessity."
            )
        } else {
            listOf(
                "I prefer to avoid these.",
                "I have an intolerance to these."
            )
        }

        slider.clearOnChangeListeners()
        slider.addOnChangeListener { _, value, _ ->
            val index = value.toInt()
            if (index < states.size) {
                description.text = states[index]
            }
        }

        description.text = states[slider.value.toInt()]
    }

    companion object {
        private const val ARG_DIET = "arg_diet"
        fun newInstance(diet: DietItem): DietBottomSheetFragment {
            val fragment = DietBottomSheetFragment()
            val args = Bundle()
            args.putSerializable(ARG_DIET, diet)
            fragment.arguments = args
            return fragment
        }
    }
}