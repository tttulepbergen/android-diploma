package com.example.scanfit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.example.scanfit.data.DietItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.slider.Slider

class DietBottomSheetFragment : BottomSheetDialogFragment() {

    private var dietItem: DietItem? = null
    private var isSubOptionMode = false
    // Список для хранения ID выбранных чекбоксов
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

        dietItem = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(ARG_DIET, DietItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable(ARG_DIET) as? DietItem
        }

        val saveBtn = view.findViewById<MaterialButton>(R.id.saveButton)

        updateUI(view)

        saveBtn.setOnClickListener {
            if (dietItem?.ui_type == "selection_modal" && !isSubOptionMode) {
                // Если мы в режиме выбора чекбоксов и нажали "Next"
                if (selectedOptions.isNotEmpty()) {
                    isSubOptionMode = true
                    updateUI(view)
                } else {
                    // Можно добавить Toast "Please select at least one option"
                }
            } else {
                // Если мы уже на ползунке или это обычная диета — сохраняем всё
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
                    // ЭКРАН 2: Много ползунков
                    subOptionsContainer.visibility = View.VISIBLE // Используем этот же контейнер для слайдеров
                    sliderContainer.visibility = View.GONE        // Скрываем одиночный слайдер из XML
                    saveBtn.text = "Save selection"
                    description.text = "Set avoidance level for each selected item"

                    setupMultipleSliders(subOptionsContainer) // Генерируем слайдеры программно
                } else {
                    // ЭКРАН 1: Множественный выбор чекбоксов
                    sliderContainer.visibility = View.GONE
                    subOptionsContainer.visibility = View.VISIBLE
                    saveBtn.text = "Next" // Меняем текст кнопки
                    description.text = "Select all categories you want to avoid"
                    setupCheckboxes(subOptionsContainer)
                }
            }
        }
    }

    private fun setupMultipleSliders(container: LinearLayout) {
        container.removeAllViews()

        // Фильтруем только те подпункты, которые пользователь выбрал галочками
        val selectedSubOptions = dietItem?.sub_options?.filter { selectedOptions.contains(it.id) }

        selectedSubOptions?.forEach { subOption ->
            // Создаем заголовок для каждого слайдера (например, "Mammalian meats")
            val subTitle = TextView(requireContext()).apply {
                text = subOption.name
                textSize = 16f
                setPadding(0, 16, 0, 8)
                setTextColor(resources.getColor(R.color.black, null)) // Замените на ваш цвет
            }

            // Инфлейтим (раздуваем) макет одного слайдера.
            // Вам нужно создать отдельный файл layout (например, item_diet_slider.xml),
            // скопировав туда структуру вашего sliderContainer из основного диалога.
            val sliderView = layoutInflater.inflate(R.layout.item_diet_slider, container, false)

            val slider = sliderView.findViewById<Slider>(R.id.dietSlider)
            val labelEnd = sliderView.findViewById<TextView>(R.id.labelEnd)
            val labelMiddle = sliderView.findViewById<TextView>(R.id.labelMiddle)
            val subDescription = sliderView.findViewById<TextView>(R.id.dietDescription)

            // Настройка конкретного слайдера на основе данных subOption
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

            // Логика текста описания для каждого слайдера
            slider.addOnChangeListener { _, value, _ ->
                // Здесь можно сохранять результат для каждого ID отдельно в Map
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
                // Устанавливаем состояние, если пользователь уже что-то выбрал
                isChecked = selectedOptions.contains(option.id)

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 8, 0, 8) }

                // ТЕПЕРЬ ОБРАБАТЫВАЕМ ТОЛЬКО ГАЛОЧКУ, НЕ ПЕРЕХОДИМ СРАЗУ
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

        // Читаем количество уровней из объекта (по умолчанию 2)
        val maxLevels = dietItem?.max_levels ?: 2

        // Настраиваем количество точек на слайдере
        slider.valueFrom = 0f
        slider.valueTo = (maxLevels - 1).toFloat()
        slider.stepSize = 1f

        // Настраиваем текстовые подписи под слайдером
        if (maxLevels == 3) {
            labelMiddle.text = "Intolerance" // В центре
            labelEnd.visibility = View.VISIBLE
            labelEnd.text = "Severe"        // Справа
        } else {
            labelMiddle.text = ""           // Очищаем центр
            labelEnd.visibility = View.VISIBLE
            labelEnd.text = "Intolerance"   // Справа
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

        // Устанавливаем начальный текст
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