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

        // 1. Получаем название категории
        val categoryName = arguments?.getString("categoryName") ?: "Fruits & Vegetables"
        binding.tvTitle.text = categoryName

        // 2. Назад
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // 3. RecyclerView
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
            showDetails = false // СКРЫВАЕМ
        )
        binding.recyclerView.adapter = adapter
    }

    private fun getSubCategoriesForCategory(categoryName: String): List<FoodItem> {
        return when (categoryName) {

            "Fruits & Vegetables" -> listOf(
                FoodItem("Just Vegetables", "Fresh vegetables", imageRes = R.drawable.ic_fruits),
                FoodItem("Processed Vegetables", "Canned and frozen", imageRes = R.drawable.ic_fruits),
                FoodItem("Just Fruits", "Fresh fruits", imageRes = R.drawable.ic_fruits),
                FoodItem("Processed Fruits", "Canned and frozen fruits", imageRes = R.drawable.ic_fruits),
                FoodItem("Dried fruits", "Dehydrated fruits", imageRes = R.drawable.ic_fruits)
            )

            "Breads & Carbs" -> listOf(
                FoodItem("Bread", "Fresh bread", imageRes = R.drawable.ic_bread),
                FoodItem("Flatbreads", "Flat bread varieties", imageRes = R.drawable.ic_bread),
                FoodItem("Rolls, Bagels & Buns", "Bakery items", imageRes = R.drawable.ic_bread),
                FoodItem("Rice", "Grains", imageRes = R.drawable.ic_bread),
                FoodItem("Pasta & Noodles", "Italian classics", imageRes = R.drawable.ic_bread),
                FoodItem("Grains", "Whole grains", imageRes = R.drawable.ic_bread),
                FoodItem("Potato Products", "Potato based", imageRes = R.drawable.ic_bread)
            )

            "Breakfast" -> listOf(
                FoodItem("Cold Cereals", "Ready to eat cereals", imageRes = R.drawable.ic_breakfast),
                FoodItem("Hot Cereals", "Hot breakfast cereals", imageRes = R.drawable.ic_breakfast),
                FoodItem("Pancake & Waffle Mixes", "Breakfast mixes", imageRes = R.drawable.ic_breakfast),
                FoodItem("Breakfast Tarts", "Pastry breakfast items", imageRes = R.drawable.ic_breakfast),
                FoodItem("Prepared Breakfast", "Ready breakfast meals", imageRes = R.drawable.ic_breakfast),
                FoodItem("Pastries", "Breakfast pastries", imageRes = R.drawable.ic_breakfast),
                FoodItem("Plant Powder", "Plant based powders", imageRes = R.drawable.ic_breakfast)
            )

            "Dairy & Eggs" -> listOf(
                FoodItem("Milk & Milk Substitutes", "Dairy and alternatives", imageRes = R.drawable.ic_meat),
                FoodItem("Yogurt", "Yogurt products", imageRes = R.drawable.ic_meat),
                FoodItem("Cottage Cheese & Cheese Spreads", "Soft cheeses", imageRes = R.drawable.ic_meat),
                FoodItem("Cheese", "Cheese varieties", imageRes = R.drawable.ic_meat),
                FoodItem("Butter & Vegetable Spreads", "Spreads and butter", imageRes = R.drawable.ic_meat),
                FoodItem("Whipped Cream", "Cream products", imageRes = R.drawable.ic_meat),
                FoodItem("Sour Cream", "Sour cream", imageRes = R.drawable.ic_meat),
                FoodItem("Eggs & Egg Substitutes", "Eggs and alternatives", imageRes = R.drawable.ic_meat)
            )

            "Meat & Fish" -> listOf(
                FoodItem("Fresh or Frozen Fish & Seafood", "Fresh seafood", imageRes = R.drawable.ic_meat),
                FoodItem("Prepared Fish & Seafood", "Ready seafood", imageRes = R.drawable.ic_meat),
                FoodItem("Chicken & Poultry", "Poultry products", imageRes = R.drawable.ic_meat),
                FoodItem("Prepared Poultry Products", "Ready poultry", imageRes = R.drawable.ic_meat),
                FoodItem("Sausages & Hot Dogs", "Processed meats", imageRes = R.drawable.ic_meat),
                FoodItem("Lunch Meat (Deli Meats)", "Deli meats", imageRes = R.drawable.ic_meat),
                FoodItem("Beef & Veal", "Beef products", imageRes = R.drawable.ic_meat),
                FoodItem("Prepared Beef Products", "Ready beef", imageRes = R.drawable.ic_meat),
                FoodItem("Bacon, Ham & Pork", "Pork products", imageRes = R.drawable.ic_meat),
                FoodItem("Lamb", "Lamb products", imageRes = R.drawable.ic_meat)
            )

            "Other Proteins" -> listOf(
                FoodItem("Legumes", "Beans and legumes", imageRes = R.drawable.ic_meat),
                FoodItem("Chili", "Chili products", imageRes = R.drawable.ic_meat),
                FoodItem("Eggs & Egg Substitutes", "Egg alternatives", imageRes = R.drawable.ic_meat),
                FoodItem("Nuts and Seeds", "Nuts and seeds", imageRes = R.drawable.ic_meat),
                FoodItem("Nut & Seed Butters", "Nut butters", imageRes = R.drawable.ic_meat),
                FoodItem("Tofu & Seitan", "Plant proteins", imageRes = R.drawable.ic_meat),
                FoodItem("Vegetarian Products", "Vegetarian options", imageRes = R.drawable.ic_meat)
            )

            "Condiments & More" -> listOf(
                FoodItem("Ketchup", "Tomato ketchup", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem("Mustard", "Mustard varieties", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem("Mayonnaise", "Mayo products", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem("Sauces, Gravies & Marinades", "Sauces and marinades", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem("Olives", "Olive products", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem("Pickles, Relish & Sauerkraut", "Pickled items", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem("Salad Dressing", "Dressings", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem("Salad Toppings", "Salad additions", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem("Pasta Sauce", "Pasta sauces", imageRes = R.drawable.ic_launcher_foreground)
            )

            "Dips, Spreads & Jams" -> listOf(
                FoodItem("Nut & Seed Butters", "Nut butters", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem("Salsa", "Salsa varieties", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem("Hummus", "Hummus products", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem("Other Dips", "Various dips", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem("Jams, Syrup, Preserves", "Sweet spreads", imageRes = R.drawable.ic_launcher_foreground)
            )

            "Prepared Foods & Soups" -> listOf(
                FoodItem(title = "Prepared Appetizers & Snacks", subtitle = "Ready appetizers", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Prepared Breakfast", subtitle = "Ready breakfast", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Prepared Lunch - Burritos & Sandwiches", subtitle = "Lunch items", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Prepared Meals - Entrees", subtitle = "Main dishes", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Prepared Poultry Products", subtitle = "Ready poultry", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Prepared Beef Products", subtitle = "Ready beef", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Prepared Fish & Seafood", subtitle = "Ready seafood", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Prepared Pizza, Pasta & Noodles", subtitle = "Italian ready meals", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Prepared Sides & Salads", subtitle = "Side dishes", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Prepared Soup", subtitle = "Ready soups", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Condensed Soup, Soup Mixes, Broth", subtitle = "Soup products", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Pasta Sauce", subtitle = "Pasta sauces", imageRes = R.drawable.ic_launcher_foreground)
            )

            "Fast Food" -> listOf(
                FoodItem(title = "Sandwiches & Chicken", subtitle = "Sandwiches and chicken", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Salads", subtitle = "Fast food salads", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Breakfast", subtitle = "Breakfast items", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Sides", subtitle = "Side dishes", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Soup", subtitle = "Soups", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Pizza", subtitle = "Pizza items", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Pasta", subtitle = "Pasta dishes", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Kids Meals", subtitle = "Children meals", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Desserts", subtitle = "Sweet treats", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Drinks", subtitle = "Beverages", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Condiments & Dressing", subtitle = "Sauces and dressings", imageRes = R.drawable.ic_launcher_foreground)
            )

            "Restaurant" -> listOf(
                FoodItem(title = "Sandwiches & Main Dishes", subtitle = "Main courses", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Salads", subtitle = "Salad dishes", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Sides", subtitle = "Side dishes", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Drinks", subtitle = "Beverages", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Desserts", subtitle = "Sweet dishes", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Condiments", subtitle = "Condiments", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Breakfast", subtitle = "Breakfast menu", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Soups & Appetizers", subtitle = "Starters", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Pizza", subtitle = "Pizza dishes", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Pasta", subtitle = "Pasta dishes", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Kid's Meal", subtitle = "Children meals", imageRes = R.drawable.ic_launcher_foreground)
            )

            "Chains (by ABC)" -> listOf(
                FoodItem(title = "7-Eleven", subtitle = "Convenience Store", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "A&W", subtitle = "Burgers & Root Beer", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Applebee's", subtitle = "Grill & Bar", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Arby's", subtitle = "Sandwiches", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Au bon pain", subtitle = "Bakery & Cafe", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Auntie Anne's", subtitle = "Pretzels", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Baja Fresh", subtitle = "Mexican Grill", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Baskin-Robbins", subtitle = "Ice Cream", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Beef o Brady's", subtitle = "Family Sports Pub", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Ben & Jerry's", subtitle = "Ice Cream", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "BJ's Restaurants", subtitle = "Brewhouse", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Blimpie", subtitle = "Sub Sandwiches", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Bob Evans", subtitle = "Family Restaurant", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Bojangle's", subtitle = "Chicken & Biscuits", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Bonefish Grill", subtitle = "Seafood", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Boston Market", subtitle = "Rotisserie Kitchen", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Braum's", subtitle = "Ice Cream & Dairy", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Burger King", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Burgerville", subtitle = "Local Burgers", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "California Pizza Kitchen", subtitle = "Pizza", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Captain D's", subtitle = "Seafood", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Caribou Coffee", subtitle = "Coffee House", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Carrabba's Italian Grill", subtitle = "Italian", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Casey's General Store", subtitle = "Pizza & Snacks", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Charley's Grilled Subs", subtitle = "Steaks & Subs", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Carl's Jr", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Carvel", subtitle = "Ice Cream", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Chicken Express", subtitle = "Fried Chicken", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Checkers", subtitle = "Burgers & Fries", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Chester's", subtitle = "Fried Chicken", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Chick-fil-A", subtitle = "Chicken Sandwiches", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Chili's Grill & Bar", subtitle = "American Grill", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Chipotle", subtitle = "Mexican Grill", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Chuck E Cheese's", subtitle = "Family Fun & Pizza", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Church's Chicken", subtitle = "Fried Chicken", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "CiCi's Pizza", subtitle = "Pizza Buffet", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Cinnabon", subtitle = "Baked Goods", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Cold Stone Creamery", subtitle = "Ice Cream", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Corner Bakery Cafe", subtitle = "Bakery & Cafe", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Cracker Barrel", subtitle = "Old Country Store", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Culver's", subtitle = "ButterBurgers", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Dairy Queen", subtitle = "Ice Cream & Burgers", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Denny's", subtitle = "Diner", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Del Taco", subtitle = "Mexican", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Domino's Pizza", subtitle = "Pizza Delivery", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Donatos Pizza", subtitle = "Pizza", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Dunkin Donuts", subtitle = "Coffee & Donuts", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Einstein Bros. Bagels", subtitle = "Bagels", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "El Pollo Loco", subtitle = "Chicken", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Elevation Burger", subtitle = "Organic Burgers", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Fatburger", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Fazolis", subtitle = "Italian", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Famous Dave's", subtitle = "BBQ", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Firehouse Subs", subtitle = "Subs", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Five Guys", subtitle = "Burgers & Fries", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Freddy's", subtitle = "Steakburgers", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Friendly's", subtitle = "Ice Cream", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Frisch's Big Boy", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Fuddruckers", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Gattis Pizza", subtitle = "Pizza", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Godfather's Pizza", subtitle = "Pizza", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Golden Corrall", subtitle = "Buffet", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Green Burrito", subtitle = "Mexican", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Hardees", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Hooters", subtitle = "Wings", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Huddle House", subtitle = "Diner", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "ihop", subtitle = "Pancakes", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "In-N-Out", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Jack in the Box", subtitle = "Burgers & Tacos", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Jamba Juice", subtitle = "Smoothies", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Jason's Deli", subtitle = "Deli", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Jersey Mike's Subs", subtitle = "Subs", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Jet's Pizza", subtitle = "Pizza", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Jimmy John's", subtitle = "Subs", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Joe's Crab Shack", subtitle = "Seafood", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Johnny Carino's", subtitle = "Italian", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Johnny Rockets", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "KFC", subtitle = "Fried Chicken", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Krispy Kreme", subtitle = "Donuts", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Krystal", subtitle = "Sliders", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "L&L Hawaiian BBQ", subtitle = "Hawaiian", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Lee's Famous Chicken", subtitle = "Fried Chicken", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Little Caesars", subtitle = "Pizza", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Long John's Silver", subtitle = "Seafood", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "LongHorn Steakhouse", subtitle = "Steakhouse", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "McAlister's Deli", subtitle = "Deli", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "McDonald's", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Moe's Southwest Grill", subtitle = "Mexican", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Mrs. Fields", subtitle = "Cookies", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Noodles & Company", subtitle = "Noodles", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "O'Charley's", subtitle = "Grill & Bar", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Old Country Buffet", subtitle = "Buffet", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Olive Garden", subtitle = "Italian", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "On The Border", subtitle = "Mexican Grill", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Orange Julius", subtitle = "Smoothies", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Outback Steakhouse", subtitle = "Steakhouse", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "P.F. Chang's", subtitle = "Asian Dining", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Panda Express", subtitle = "Chinese Food", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Panera Bread", subtitle = "Bakery & Cafe", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Papa John's", subtitle = "Pizza", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Papa Murphy's", subtitle = "Pizza", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Pei Wei Asian Diner", subtitle = "Asian", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Penn Station", subtitle = "East Coast Subs", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Perkins", subtitle = "Bakery", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Pita Pit", subtitle = "Pita Sandwiches", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Pizza Hut", subtitle = "Pizza", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Pizza Inn", subtitle = "Pizza", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Pizza Ranch", subtitle = "Pizza Buffet", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Ponderosa Steakhouse", subtitle = "Steaks", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Popeyes", subtitle = "Louisiana Kitchen", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Potbelly Sandwich Works", subtitle = "Sandwiches", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Quiznos", subtitle = "Toasted Subs", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Raising Cane's", subtitle = "Chicken Fingers", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Rally's", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Red Lobster", subtitle = "Seafood", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Red Robin", subtitle = "Gourmet Burgers", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Romano's Macaroni Grill", subtitle = "Italian", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Round Table Pizza", subtitle = "Pizza", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Rubio's", subtitle = "Mexican", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Ruby Tuesday", subtitle = "American Grill", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Sbarro", subtitle = "Italian Eatery", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Schlotzsky's", subtitle = "Sandwiches", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Sheetz", subtitle = "Convenience Food", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Smashburger", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Sonic", subtitle = "Drive-In", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Soup Plantation", subtitle = "Salad Bar", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Starbucks", subtitle = "Coffee", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Steak n' shake", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Subway", subtitle = "Fresh Subs", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Taco Bell", subtitle = "Mexican Fast Food", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Taco Bueno", subtitle = "Mexican", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Taco Cabana", subtitle = "Mexican", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Taco John's", subtitle = "Mexican", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Texas Roadhouse", subtitle = "Steaks", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "TGI Fridays", subtitle = "Grill & Bar", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "The Cheesecake Factory", subtitle = "Restaurant", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "The Habit Burger Grill", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Tim Hortons", subtitle = "Coffee & Bake Shop", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Togo's", subtitle = "Sandwiches", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Tony Roma's", subtitle = "Ribs & Steaks", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Uno", subtitle = "Pizza & Grill", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Village Inn", subtitle = "Family Restaurant", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Wawa", subtitle = "Convenience Food", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Wendy's", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Whataburger", subtitle = "Burgers", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "White Castle", subtitle = "Sliders", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Wienerschnitzel", subtitle = "Hot Dogs", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Wing Stop", subtitle = "Chicken Wings", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Zaxby's", subtitle = "Chicken Fingers", imageRes = R.drawable.ic_launcher_foreground)
            )

            "Salty Snacks" -> listOf(
                FoodItem("Snack Bars", "Energy bars", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem("Nuts and Seeds", "Nuts and seeds", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem("Nut Mixes & Trail Mix", "Mixed nuts", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem("Popcorn", "Popcorn varieties", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem("Chips & Puffs", "Potato chips", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem("Crackers", "Snack crackers", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem("Pretzels", "Pretzel snacks", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem("Jerky", "Dried meat snacks", imageRes = R.drawable.ic_launcher_foreground)
            )

            "Sweet Snacks" -> listOf(
                FoodItem("Fruit Flavored Snacks", "Fruit snacks", imageRes = R.drawable.ic_meat),
                FoodItem("Candy & Chocolate", "Sweets and chocolate", imageRes = R.drawable.ic_meat),
                FoodItem("Cookies", "Cookie varieties", imageRes = R.drawable.ic_meat),
                FoodItem("Gum & Mints", "Chewing gum and mints", imageRes = R.drawable.ic_meat),
                FoodItem("Sorbets & Popsicles", "Frozen treats", imageRes = R.drawable.ic_meat),
                FoodItem("Ice cream & Frozen Yogurt", "Frozen desserts", imageRes = R.drawable.ic_meat),
                FoodItem("Pudding & Gelatin", "Pudding products", imageRes = R.drawable.ic_meat),
                FoodItem("Snack Cakes, Pies", "Sweet pastries", imageRes = R.drawable.ic_meat),
                FoodItem("Pastries", "Pastry items", imageRes = R.drawable.ic_meat),
                FoodItem("Snack Bars", "Sweet bars", imageRes = R.drawable.ic_meat)
            )

            "Beverages" -> listOf(
                FoodItem("Water, Unsweetened", "Plain water", imageRes = R.drawable.ic_meat),
                FoodItem("Water, Sweetened", "Flavored water", imageRes = R.drawable.ic_meat),
                FoodItem("Coffee Beans & Instant Coffee", "Coffee products", imageRes = R.drawable.ic_meat),
                FoodItem("Prepared Coffee Drinks", "Ready coffee", imageRes = R.drawable.ic_meat),
                FoodItem("Tea Bags", "Tea products", imageRes = R.drawable.ic_meat),
                FoodItem("Prepared Tea Drinks", "Ready tea", imageRes = R.drawable.ic_meat),
                FoodItem("Juice and Fruit Drinks", "Fruit beverages", imageRes = R.drawable.ic_meat),
                FoodItem("Drink Mix", "Drink mixes", imageRes = R.drawable.ic_meat),
                FoodItem("Chocolate Drinks & Mixes", "Chocolate beverages", imageRes = R.drawable.ic_meat),
                FoodItem("Breakfast Drinks & Smoothies", "Smoothies", imageRes = R.drawable.ic_meat),
                FoodItem("Carbonated Soft Drinks", "Soda drinks", imageRes = R.drawable.ic_meat),
                FoodItem("Energy and sports drinks", "Sports beverages", imageRes = R.drawable.ic_meat),
                FoodItem("Energy Drinks", "Energy beverages", imageRes = R.drawable.ic_meat),
                FoodItem("Wine", "Wine products", imageRes = R.drawable.ic_meat),
                FoodItem("Beer", "Beer products", imageRes = R.drawable.ic_meat),
                FoodItem("Spirits & Liquors", "Spirits", imageRes = R.drawable.ic_meat),
                FoodItem("Cocktails", "Mixed drinks", imageRes = R.drawable.ic_meat)
            )

            "Cooking & Baking" -> listOf(
                FoodItem("Bread Dough & Mix", "Bread mixes", imageRes = R.drawable.ic_meat),
                FoodItem("Cake & Cookie Dough Mix", "Baking mixes", imageRes = R.drawable.ic_meat),
                FoodItem("Flour & Meal", "Flour products", imageRes = R.drawable.ic_meat),
                FoodItem("Sweeteners", "Sugar and sweeteners", imageRes = R.drawable.ic_meat),
                FoodItem("Non-Caloric Sweeteners", "Sugar substitutes", imageRes = R.drawable.ic_meat),
                FoodItem("Baking Ingredients", "Baking supplies", imageRes = R.drawable.ic_meat),
                FoodItem("Oil & Shortening", "Cooking oils", imageRes = R.drawable.ic_meat),
                FoodItem("Cooking Wine & Vinegar", "Vinegar products", imageRes = R.drawable.ic_meat),
                FoodItem("Spices, Seasonings & Rubs", "Seasonings", imageRes = R.drawable.ic_meat),
                FoodItem("Processed or Pre-Salted Spices", "Salted spices", imageRes = R.drawable.ic_meat),
                FoodItem("Just Salt", "Salt products", imageRes = R.drawable.ic_meat),
                FoodItem("Stuffing", "Stuffing mixes", imageRes = R.drawable.ic_meat)
            )

            "Baby Food" -> listOf(
                FoodItem(title = "Baby Formula", subtitle = "Infant formula", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Baby Food - Stage 1 & 2", subtitle = "Early stage foods", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Baby Food - Stage 3", subtitle = "Advanced stage foods", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Baby Juice", subtitle = "Infant juices", imageRes = R.drawable.ic_launcher_foreground)
            )

            "Pet Food" -> listOf(
                FoodItem(title = "Dog Food", subtitle = "Dog food products", imageRes = R.drawable.ic_launcher_foreground),
                FoodItem(title = "Cat Food", subtitle = "Cat food products", imageRes = R.drawable.ic_launcher_foreground)
            )

            else -> listOf(
                FoodItem(title = "General Products", subtitle = "Various items", imageRes = R.drawable.ic_launcher_foreground)
            )
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

















