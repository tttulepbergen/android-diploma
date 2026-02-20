package com.example.scanfit.mainNavigation.browse.categories

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanfit.adapters.FoodAdapter
import com.example.scanfit.R
import com.example.scanfit.databinding.FragmentSubCategoriesBinding
import com.example.scanfit.data.FoodItem

class SubCategoriesFragment : Fragment(R.layout.fragment_sub_categories) {

    private var _binding: FragmentSubCategoriesBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSubCategoriesBinding.bind(view)

        val categoryName = arguments?.getString("categoryName") ?: "Fruits & Vegetables"
        binding.tvTitle.text = categoryName

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        setupRecyclerView(categoryName)
    }

    private fun setupRecyclerView(categoryName: String) {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val subCategories = getSubCategoriesForCategory(categoryName)

        val adapter = FoodAdapter(
            items = subCategories,
            onItemClick = { selectedSubCategory ->
                val bundle = bundleOf("subCategoryName" to selectedSubCategory.title)
                findNavController().navigate(
                    R.id.action_subCategoriesFragment_to_productListFragment,
                    bundle
                )
            },
            onFavoriteClick = { },
            showDetails = false
        )
        binding.recyclerView.adapter = adapter
    }

    private fun getSubCategoriesForCategory(categoryName: String): List<FoodItem> {
        return when (categoryName) {

            "Fruits & Vegetables" -> listOf(
                FoodItem("Just Vegetables", "Fresh vegetables", imageRes = R.drawable.ic_fruits, ingredients = ""),
                FoodItem("Processed Vegetables", "Canned and frozen", imageRes = R.drawable.ic_fruits, ingredients = ""),
                FoodItem("Just Fruits", "Fresh fruits", imageRes = R.drawable.ic_fruits, ingredients = ""),
                FoodItem("Processed Fruits", "Canned and frozen fruits", imageRes = R.drawable.ic_fruits, ingredients = ""),
                FoodItem("Dried fruits", "Dehydrated fruits", imageRes = R.drawable.ic_fruits, ingredients = "")
            )

            "Breads & Carbs" -> listOf(
                FoodItem("Bread", "Fresh bread", imageRes = R.drawable.ic_bread, ingredients = ""),
                FoodItem("Flatbreads", "Flat bread varieties", imageRes = R.drawable.ic_bread, ingredients = ""),
                FoodItem("Rolls, Bagels & Buns", "Bakery items", imageRes = R.drawable.ic_bread, ingredients = ""),
                FoodItem("Rice", "Grains", imageRes = R.drawable.ic_bread, ingredients = ""),
                FoodItem("Pasta & Noodles", "Italian classics", imageRes = R.drawable.ic_bread, ingredients = ""),
                FoodItem("Grains", "Whole grains", imageRes = R.drawable.ic_bread, ingredients = ""),
                FoodItem("Potato Products", "Potato based", imageRes = R.drawable.ic_bread, ingredients = "")
            )

            "Breakfast" -> listOf(
                FoodItem("Cold Cereals", "Ready to eat cereals", imageRes = R.drawable.ic_breakfast, ingredients = ""),
                FoodItem("Hot Cereals", "Hot breakfast cereals", imageRes = R.drawable.ic_breakfast, ingredients = ""),
                FoodItem("Pancake & Waffle Mixes", "Breakfast mixes", imageRes = R.drawable.ic_breakfast, ingredients = ""),
                FoodItem("Breakfast Tarts", "Pastry breakfast items", imageRes = R.drawable.ic_breakfast, ingredients = ""),
                FoodItem("Prepared Breakfast", "Ready breakfast meals", imageRes = R.drawable.ic_breakfast, ingredients = ""),
                FoodItem("Pastries", "Breakfast pastries", imageRes = R.drawable.ic_breakfast, ingredients = ""),
                FoodItem("Plant Powder", "Plant based powders", imageRes = R.drawable.ic_breakfast, ingredients = "")
            )

            "Dairy & Eggs" -> listOf(
                FoodItem("Milk & Milk Substitutes", "Dairy and alternatives", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Yogurt", "Yogurt products", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Cottage Cheese & Cheese Spreads", "Soft cheeses", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Cheese", "Cheese varieties", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Butter & Vegetable Spreads", "Spreads and butter", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Whipped Cream", "Cream products", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Sour Cream", "Sour cream", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Eggs & Egg Substitutes", "Eggs and alternatives", imageRes = R.drawable.ic_meat, ingredients = "")
            )

            "Meat & Fish" -> listOf(
                FoodItem("Fresh or Frozen Fish & Seafood", "Fresh seafood", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Prepared Fish & Seafood", "Ready seafood", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Chicken & Poultry", "Poultry products", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Prepared Poultry Products", "Ready poultry", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Sausages & Hot Dogs", "Processed meats", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Lunch Meat (Deli Meats)", "Deli meats", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Beef & Veal", "Beef products", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Prepared Beef Products", "Ready beef", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Bacon, Ham & Pork", "Pork products", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Lamb", "Lamb products", imageRes = R.drawable.ic_meat, ingredients = "")
            )

            "Other Proteins" -> listOf(
                FoodItem("Legumes", "Beans and legumes", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Chili", "Chili products", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Eggs & Egg Substitutes", "Egg alternatives", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Nuts and Seeds", "Nuts and seeds", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Nut & Seed Butters", "Nut butters", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Tofu & Seitan", "Plant proteins", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Vegetarian Products", "Vegetarian options", imageRes = R.drawable.ic_meat, ingredients = "")
            )

            "Condiments & More" -> listOf(
                FoodItem("Ketchup", "Tomato ketchup", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem("Mustard", "Mustard varieties", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem("Mayonnaise", "Mayo products", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem("Sauces, Gravies & Marinades", "Sauces and marinades", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem("Olives", "Olive products", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem("Pickles, Relish & Sauerkraut", "Pickled items", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem("Salad Dressing", "Dressings", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem("Salad Toppings", "Salad additions", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem("Pasta Sauce", "Pasta sauces", imageRes = R.drawable.ic_launcher_foreground, ingredients = "")
            )

            "Dips, Spreads & Jams" -> listOf(
                FoodItem("Nut & Seed Butters", "Nut butters", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem("Salsa", "Salsa varieties", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem("Hummus", "Hummus products", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem("Other Dips", "Various dips", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem("Jams, Syrup, Preserves", "Sweet spreads", imageRes = R.drawable.ic_launcher_foreground, ingredients = "")
            )

            "Prepared Foods & Soups" -> listOf(
                FoodItem(title = "Prepared Appetizers & Snacks", subtitle = "Ready appetizers", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Prepared Breakfast", subtitle = "Ready breakfast", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Prepared Lunch - Burritos & Sandwiches", subtitle = "Lunch items", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Prepared Meals - Entrees", subtitle = "Main dishes", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Prepared Poultry Products", subtitle = "Ready poultry", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Prepared Beef Products", subtitle = "Ready beef", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Prepared Fish & Seafood", subtitle = "Ready seafood", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Prepared Pizza, Pasta & Noodles", subtitle = "Italian ready meals", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Prepared Sides & Salads", subtitle = "Side dishes", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Prepared Soup", subtitle = "Ready soups", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Condensed Soup, Soup Mixes, Broth", subtitle = "Soup products", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Pasta Sauce", subtitle = "Pasta sauces", imageRes = R.drawable.ic_launcher_foreground, ingredients = "")
            )

            "Fast Food" -> listOf(
                FoodItem(title = "Sandwiches & Chicken", subtitle = "Sandwiches and chicken", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Salads", subtitle = "Fast food salads", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Breakfast", subtitle = "Breakfast items", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Sides", subtitle = "Side dishes", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Soup", subtitle = "Soups", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Pizza", subtitle = "Pizza items", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Pasta", subtitle = "Pasta dishes", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Kids Meals", subtitle = "Children meals", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Desserts", subtitle = "Sweet treats", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Drinks", subtitle = "Beverages", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Condiments & Dressing", subtitle = "Sauces and dressings", imageRes = R.drawable.ic_launcher_foreground, ingredients = "")
            )

            "Restaurant" -> listOf(
                FoodItem(title = "Sandwiches & Main Dishes", subtitle = "Main courses", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Salads", subtitle = "Salad dishes", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Sides", subtitle = "Side dishes", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Drinks", subtitle = "Beverages", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Desserts", subtitle = "Sweet dishes", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Condiments", subtitle = "Condiments", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Breakfast", subtitle = "Breakfast menu", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Soups & Appetizers", subtitle = "Starters", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Pizza", subtitle = "Pizza dishes", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Pasta", subtitle = "Pasta dishes", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Kid's Meal", subtitle = "Children meals", imageRes = R.drawable.ic_launcher_foreground, ingredients = "")
            )

            "Chains (by ABC)" -> listOf(
                FoodItem(title = "7-Eleven", subtitle = "Convenience Store", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "A&W", subtitle = "Burgers & Root Beer", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Applebee's", subtitle = "Grill & Bar", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Arby's", subtitle = "Sandwiches", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Au bon pain", subtitle = "Bakery & Cafe", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Auntie Anne's", subtitle = "Pretzels", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Baja Fresh", subtitle = "Mexican Grill", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Baskin-Robbins", subtitle = "Ice Cream", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Beef o Brady's", subtitle = "Family Sports Pub", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Ben & Jerry's", subtitle = "Ice Cream", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "BJ's Restaurants", subtitle = "Brewhouse", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Blimpie", subtitle = "Sub Sandwiches", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Bob Evans", subtitle = "Family Restaurant", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Bojangle's", subtitle = "Chicken & Biscuits", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Bonefish Grill", subtitle = "Seafood", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Boston Market", subtitle = "Rotisserie Kitchen", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Braum's", subtitle = "Ice Cream & Dairy", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Burger King", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Burgerville", subtitle = "Local Burgers", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "California Pizza Kitchen", subtitle = "Pizza", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Captain D's", subtitle = "Seafood", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Caribou Coffee", subtitle = "Coffee House", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Carrabba's Italian Grill", subtitle = "Italian", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Casey's General Store", subtitle = "Pizza & Snacks", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Charley's Grilled Subs", subtitle = "Steaks & Subs", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Carl's Jr", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Carvel", subtitle = "Ice Cream", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Chicken Express", subtitle = "Fried Chicken", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Checkers", subtitle = "Burgers & Fries", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Chester's", subtitle = "Fried Chicken", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Chick-fil-A", subtitle = "Chicken Sandwiches", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Chili's Grill & Bar", subtitle = "American Grill", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Chipotle", subtitle = "Mexican Grill", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Chuck E Cheese's", subtitle = "Family Fun & Pizza", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Church's Chicken", subtitle = "Fried Chicken", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "CiCi's Pizza", subtitle = "Pizza Buffet", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Cinnabon", subtitle = "Baked Goods", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Cold Stone Creamery", subtitle = "Ice Cream", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Corner Bakery Cafe", subtitle = "Bakery & Cafe", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Cracker Barrel", subtitle = "Old Country Store", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Culver's", subtitle = "ButterBurgers", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Dairy Queen", subtitle = "Ice Cream & Burgers", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Denny's", subtitle = "Diner", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Del Taco", subtitle = "Mexican", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Domino's Pizza", subtitle = "Pizza Delivery", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Donatos Pizza", subtitle = "Pizza", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Dunkin Donuts", subtitle = "Coffee & Donuts", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Einstein Bros. Bagels", subtitle = "Bagels", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "El Pollo Loco", subtitle = "Chicken", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Elevation Burger", subtitle = "Organic Burgers", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Fatburger", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Fazolis", subtitle = "Italian", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Famous Dave's", subtitle = "BBQ", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Firehouse Subs", subtitle = "Subs", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Five Guys", subtitle = "Burgers & Fries", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Freddy's", subtitle = "Steakburgers", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Friendly's", subtitle = "Ice Cream", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Frisch's Big Boy", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Fuddruckers", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Gattis Pizza", subtitle = "Pizza", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Godfather's Pizza", subtitle = "Pizza", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Golden Corrall", subtitle = "Buffet", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Green Burrito", subtitle = "Mexican", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Hardees", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Hooters", subtitle = "Wings", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Huddle House", subtitle = "Diner", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "ihop", subtitle = "Pancakes", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "In-N-Out", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Jack in the Box", subtitle = "Burgers & Tacos", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Jamba Juice", subtitle = "Smoothies", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Jason's Deli", subtitle = "Deli", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Jersey Mike's Subs", subtitle = "Subs", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Jet's Pizza", subtitle = "Pizza", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Jimmy John's", subtitle = "Subs", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Joe's Crab Shack", subtitle = "Seafood", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Johnny Carino's", subtitle = "Italian", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Johnny Rockets", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "KFC", subtitle = "Fried Chicken", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Krispy Kreme", subtitle = "Donuts", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Krystal", subtitle = "Sliders", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "L&L Hawaiian BBQ", subtitle = "Hawaiian", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Lee's Famous Chicken", subtitle = "Fried Chicken", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Little Caesars", subtitle = "Pizza", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Long John's Silver", subtitle = "Seafood", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "LongHorn Steakhouse", subtitle = "Steakhouse", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "McAlister's Deli", subtitle = "Deli", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "McDonald's", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Moe's Southwest Grill", subtitle = "Mexican", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Mrs. Fields", subtitle = "Cookies", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Noodles & Company", subtitle = "Noodles", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "O'Charley's", subtitle = "Grill & Bar", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Old Country Buffet", subtitle = "Buffet", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Olive Garden", subtitle = "Italian", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "On The Border", subtitle = "Mexican Grill", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Orange Julius", subtitle = "Smoothies", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Outback Steakhouse", subtitle = "Steakhouse", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "P.F. Chang's", subtitle = "Asian Dining", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Panda Express", subtitle = "Chinese Food", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Panera Bread", subtitle = "Bakery & Cafe", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Papa John's", subtitle = "Pizza", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Papa Murphy's", subtitle = "Pizza", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Pei Wei Asian Diner", subtitle = "Asian", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Penn Station", subtitle = "East Coast Subs", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Perkins", subtitle = "Bakery", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Pita Pit", subtitle = "Pita Sandwiches", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Pizza Hut", subtitle = "Pizza", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Pizza Inn", subtitle = "Pizza", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Pizza Ranch", subtitle = "Pizza Buffet", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Ponderosa Steakhouse", subtitle = "Steaks", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Popeyes", subtitle = "Louisiana Kitchen", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Potbelly Sandwich Works", subtitle = "Sandwiches", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Quiznos", subtitle = "Toasted Subs", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Raising Cane's", subtitle = "Chicken Fingers", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Rally's", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Red Lobster", subtitle = "Seafood", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Red Robin", subtitle = "Gourmet Burgers", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Romano's Macaroni Grill", subtitle = "Italian", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Round Table Pizza", subtitle = "Pizza", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Rubio's", subtitle = "Mexican", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Ruby Tuesday", subtitle = "American Grill", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Sbarro", subtitle = "Italian Eatery", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Schlotzsky's", subtitle = "Sandwiches", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Sheetz", subtitle = "Convenience Food", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Smashburger", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Sonic", subtitle = "Drive-In", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Soup Plantation", subtitle = "Salad Bar", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Starbucks", subtitle = "Coffee", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Steak n' shake", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Subway", subtitle = "Fresh Subs", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Taco Bell", subtitle = "Mexican Fast Food", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Taco Bueno", subtitle = "Mexican", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Taco Cabana", subtitle = "Mexican", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Taco John's", subtitle = "Mexican", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Texas Roadhouse", subtitle = "Steaks", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "TGI Fridays", subtitle = "Grill & Bar", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "The Cheesecake Factory", subtitle = "Restaurant", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "The Habit Burger Grill", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Tim Hortons", subtitle = "Coffee & Bake Shop", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Togo's", subtitle = "Sandwiches", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Tony Roma's", subtitle = "Ribs & Steaks", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Uno", subtitle = "Pizza & Grill", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Village Inn", subtitle = "Family Restaurant", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Wawa", subtitle = "Convenience Food", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Wendy's", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Whataburger", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "White Castle", subtitle = "Sliders", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Wienerschnitzel", subtitle = "Hot Dogs", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Wing Stop", subtitle = "Chicken Wings", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Zaxby's", subtitle = "Chicken Fingers", imageRes = R.drawable.ic_launcher_foreground, ingredients = "")
            )

            "Salty Snacks" -> listOf(
                FoodItem("Snack Bars", "Energy bars", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem("Nuts and Seeds", "Nuts and seeds", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem("Nut Mixes & Trail Mix", "Mixed nuts", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem("Popcorn", "Popcorn varieties", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem("Chips & Puffs", "Potato chips", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem("Crackers", "Snack crackers", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem("Pretzels", "Pretzel snacks", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem("Jerky", "Dried meat snacks", imageRes = R.drawable.ic_launcher_foreground, ingredients = "")
            )

            "Sweet Snacks" -> listOf(
                FoodItem("Fruit Flavored Snacks", "Fruit snacks", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Candy & Chocolate", "Sweets and chocolate", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Cookies", "Cookie varieties", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Gum & Mints", "Chewing gum and mints", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Sorbets & Popsicles", "Frozen treats", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Ice cream & Frozen Yogurt", "Frozen desserts", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Pudding & Gelatin", "Pudding products", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Snack Cakes, Pies", "Sweet pastries", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Pastries", "Pastry items", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Snack Bars", "Sweet bars", imageRes = R.drawable.ic_meat, ingredients = "")
            )

            "Beverages" -> listOf(
                FoodItem("Water, Unsweetened", "Plain water", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Water, Sweetened", "Flavored water", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Coffee Beans & Instant Coffee", "Coffee products", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Prepared Coffee Drinks", "Ready coffee", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Tea Bags", "Tea products", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Prepared Tea Drinks", "Ready tea", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Juice and Fruit Drinks", "Fruit beverages", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Drink Mix", "Drink mixes", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Chocolate Drinks & Mixes", "Chocolate beverages", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Breakfast Drinks & Smoothies", "Smoothies", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Carbonated Soft Drinks", "Soda drinks", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Energy and sports drinks", "Sports beverages", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Energy Drinks", "Energy beverages", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Wine", "Wine products", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Beer", "Beer products", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Spirits & Liquors", "Spirits", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Cocktails", "Mixed drinks", imageRes = R.drawable.ic_meat, ingredients = "")
            )

            "Cooking & Baking" -> listOf(
                FoodItem("Bread Dough & Mix", "Bread mixes", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Cake & Cookie Dough Mix", "Baking mixes", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Flour & Meal", "Flour products", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Sweeteners", "Sugar and sweeteners", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Non-Caloric Sweeteners", "Sugar substitutes", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Baking Ingredients", "Baking supplies", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Oil & Shortening", "Cooking oils", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Cooking Wine & Vinegar", "Vinegar products", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Spices, Seasonings & Rubs", "Seasonings", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Processed or Pre-Salted Spices", "Salted spices", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Just Salt", "Salt products", imageRes = R.drawable.ic_meat, ingredients = ""),
                FoodItem("Stuffing", "Stuffing mixes", imageRes = R.drawable.ic_meat, ingredients = "")
            )

            "Baby Food" -> listOf(
                FoodItem(title = "Baby Formula", subtitle = "Infant formula", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Baby Food - Stage 1 & 2", subtitle = "Early stage foods", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Baby Food - Stage 3", subtitle = "Advanced stage foods", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Baby Juice", subtitle = "Infant juices", imageRes = R.drawable.ic_launcher_foreground, ingredients = "")
            )

            "Pet Food" -> listOf(
                FoodItem(title = "Dog Food", subtitle = "Dog food products", imageRes = R.drawable.ic_launcher_foreground, ingredients = ""),
                FoodItem(title = "Cat Food", subtitle = "Cat food products", imageRes = R.drawable.ic_launcher_foreground, ingredients = "")
            )

            else -> listOf(
                FoodItem(title = "General Products", subtitle = "Various items", imageRes = R.drawable.ic_launcher_foreground, ingredients = "")
            )
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

















