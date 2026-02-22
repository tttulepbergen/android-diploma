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
            if (findNavController().currentDestination?.id == R.id.subCategoriesFragment) {
                findNavController().popBackStack()
            } else {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        setupRecyclerView(categoryName)
    }
    private fun setupRecyclerView(categoryName: String) {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val subCategories = getSubCategoriesForCategory(categoryName)

        val adapter = FoodAdapter(
            items = subCategories,
            onItemClick = { selectedSubCategory ->
                if (findNavController().currentDestination?.id == R.id.subCategoriesFragment) {
                    val bundle = bundleOf("subCategoryName" to selectedSubCategory.title)
                    findNavController().navigate(
                        R.id.action_subCategoriesFragment_to_productListFragment,
                        bundle
                    )
                }
            },
            onFavoriteClick = { },
            showDetails = false
        )
        binding.recyclerView.adapter = adapter
    }

    private fun getSubCategoriesForCategory(categoryName: String): List<FoodItem> {
        return when (categoryName) {

            "Fruits & Vegetables" -> listOf(
                FoodItem("Processed Vegetables", "Canned and frozen", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/044/771/696/small/a-basket-brimming-with-vegetables-free-png.png", ingredients = ""),
                FoodItem("Just Fruits", "Fresh fruits", imageUrl = "https://static.vecteezy.com/system/resources/previews/044/570/896/non_2x/wicker-basket-filled-with-assorted-fresh-fruits-on-a-transparent-background-png.png", ingredients = ""),
                FoodItem("Processed Fruits", "Canned and frozen fruits", imageUrl = "https://static.vecteezy.com/system/resources/previews/050/431/850/non_2x/fruits-strawberry-orange-grapefruit-apples-blueberries-pineapple-png.png", ingredients = ""),
                FoodItem("Dried fruits", "Dehydrated fruits", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/044/813/356/small_2x/bowl-of-dried-fruits-top-view-isolated-on-transparent-background-png.png", ingredients = "")
            )

            "Breads & Carbs" -> listOf(
                FoodItem("Bread", "Fresh bread", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/044/013/366/small/bread-slice-on-white-transparent-background-format-png.png", ingredients = ""),
                FoodItem("Flatbreads", "Flat bread varieties", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/046/822/369/small/a-neatly-stacked-pile-of-freshly-baked-pita-bread-isolated-on-a-transparent-background-free-png.png", ingredients = ""),
                FoodItem("Rolls, Bagels & Buns", "Bakery items", imageUrl = "https://static.vecteezy.com/system/resources/previews/059/656/570/non_2x/fresh-bagel-breakfast-bread-on-transparent-background-free-png.png", ingredients = ""),
                FoodItem("Rice", "Grains", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/045/913/366/small/uncooked-white-rice-in-wooden-bowl-cut-out-stock-png.png", ingredients = ""),
                FoodItem("Pasta & Noodles", "Italian classics", imageUrl = "https://static.vecteezy.com/system/resources/previews/050/760/364/non_2x/a-close-up-view-of-uncooked-spaghetti-noodles-png.png", ingredients = ""),
                FoodItem("Grains", "Whole grains", imageUrl = "https://freepngimg.com/download/grocery/53777-8-grain-png-download-free.png", ingredients = ""),
                FoodItem("Potato Products", "Potato based", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/048/050/936/small/potatoes-with-leaves-on-transparent-background-background-png.png", ingredients = "")
            )

            "Breakfast" -> listOf(
                FoodItem("Cold Cereals", "Ready to eat cereals", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/071/027/277/small/close-up-of-bowl-of-cereal-with-milk-on-a-free-png.png", ingredients = ""),
                FoodItem("Hot Cereals", "Hot breakfast cereals", imageUrl = "https://static.vecteezy.com/system/resources/previews/048/556/607/non_2x/a-bowl-of-oatmeal-with-oatmeal-and-oatmeal-on-a-transparent-background-png.png", ingredients = ""),
                FoodItem("Pancake & Waffle Mixes", "Breakfast mixes", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/024/508/745/small/pancakes-isolated-on-background-with-generative-ai-png.png", ingredients = ""),
                FoodItem("Breakfast Tarts", "Pastry breakfast items", imageUrl = "https://static.vecteezy.com/system/resources/previews/057/095/046/non_2x/delicious-mini-fruit-tarts-png.png", ingredients = ""),
                FoodItem("Prepared Breakfast", "Ready breakfast meals", imageUrl = "https://static.vecteezy.com/system/resources/previews/026/991/846/non_2x/english-breakfast-with-eggs-bacon-and-beans-png.png", ingredients = ""),
                FoodItem("Pastries", "Breakfast pastries", imageUrl = "https://static.vecteezy.com/system/resources/previews/070/116/793/non_2x/delicious-assortment-of-pastries-and-baked-goods-composition-on-transparent-background-png.png", ingredients = ""),
                FoodItem("Plant Powder", "Plant based powders", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/052/649/494/small/green-powder-made-from-fresh-tea-leaves-on-display-png.png", ingredients = "")
            )

            "Dairy & Eggs" -> listOf(
                FoodItem("Milk & Milk Substitutes", "Dairy and alternatives", imageUrl = "https://freepngimg.com/download/milk/4-2-milk-png-clipart.png", ingredients = ""),
                FoodItem("Yogurt", "Yogurt products", imageUrl = "https://static.vecteezy.com/system/resources/previews/073/624/011/non_2x/blueberry-yogurt-with-mint-leaves-in-white-bowl-isolated-on-transparent-background-png.png", ingredients = ""),
                FoodItem("Cottage Cheese & Cheese Spreads", "Soft cheeses", imageUrl = "https://static.vecteezy.com/system/resources/previews/051/689/227/non_2x/cottage-cheese-front-view-full-length-isolate-on-transparency-background-png.png", ingredients = ""),
                FoodItem("Cheese", "Cheese varieties", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/046/437/254/small/3d-cheese-emoji-png.png", ingredients = ""),
                FoodItem("Butter & Vegetable Spreads", "Spreads and butter", imageUrl = "https://static.vecteezy.com/system/resources/previews/037/492/917/non_2x/ai-generated-slices-of-butter-free-png.png", ingredients = ""),
                FoodItem("Whipped Cream", "Cream products", imageUrl = "https://static.vecteezy.com/system/resources/previews/043/665/178/non_2x/realistic-white-whipped-cream-free-png.png", ingredients = ""),
                FoodItem("Sour Cream", "Sour cream", imageUrl = "https://static.vecteezy.com/system/resources/previews/047/826/581/non_2x/sour-cream-against-transparent-background-free-png.png", ingredients = ""),
                FoodItem("Eggs & Egg Substitutes", "Eggs and alternatives", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/047/490/414/small/a-pile-of-brown-eggs-isolated-on-a-transparent-background-free-png.png", ingredients = "")
            )

            "Meat & Fish" -> listOf(
                FoodItem("Fresh or Frozen Fish & Seafood", "Fresh seafood", imageUrl = "https://static.vecteezy.com/system/resources/previews/056/565/911/non_2x/shrimp-seafood-three-cooked-prawns-on-transparent-background-png.png", ingredients = ""),
                FoodItem("Prepared Fish & Seafood", "Ready seafood", imageUrl = "https://static.vecteezy.com/system/resources/previews/060/818/215/non_2x/a-plate-of-fresh-seafood-including-lobster-shrimp-and-lemon-png.png", ingredients = ""),
                FoodItem("Chicken & Poultry", "Poultry products", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/069/652/089/small/sliced-grilled-chicken-breast-closeup-culinary-food-cut-out-transparent-png.png", ingredients = ""),
                FoodItem("Prepared Poultry Products", "Ready poultry", imageUrl = "https://static.vecteezy.com/system/resources/previews/065/588/610/non_2x/sliced-chicken-breast-garnished-with-fresh-rosemary-on-a-white-plate-for-a-delicious-meal-sliced-chicken-breast-with-rosemary-garnish-on-white-platter-transparent-background-free-png.png", ingredients = ""),
                FoodItem("Sausages & Hot Dogs", "Processed meats", imageUrl = "https://static.vecteezy.com/system/resources/previews/046/829/062/non_2x/hot-dog-with-ketchup-and-mustard-isolated-on-transparent-background-png.png", ingredients = ""),
                FoodItem("Lunch Meat (Deli Meats)", "Deli meats", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/045/839/647/small/sliced-ham-on-transparent-background-png.png", ingredients = ""),
                FoodItem("Beef & Veal", "Beef products", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/049/799/009/small/steak-meat-beef-isolated-transparent-background-png.png", ingredients = ""),
                FoodItem("Prepared Beef Products", "Ready beef", imageUrl = "https://static.vecteezy.com/system/resources/previews/054/040/634/non_2x/deliciously-cooked-roast-beef-with-herbs-on-a-plate-for-a-perfect-dinner-experience-png.png", ingredients = ""),
                FoodItem("Bacon, Ham & Pork", "Pork products", imageUrl = "https://static.vecteezy.com/system/resources/previews/025/222/207/non_2x/bacon-slices-isolated-on-transparent-background-png.png", ingredients = ""),
                FoodItem("Lamb", "Lamb products", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/043/101/753/small/raw-lamb-steak-and-meat-on-transparency-background-isolated-gourmet-meal-png.png", ingredients = "")
            )

            "Other Proteins" -> listOf(
                FoodItem("Legumes", "Beans and legumes", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/047/814/072/small/flat-lay-the-assortment-of-peas-lentils-and-legumes-isolated-on-a-transparent-background-png.png", ingredients = ""),
                FoodItem("Chili", "Chili products", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/049/161/687/small/red-chili-pepper-with-water-drops-on-a-transparent-background-png.png", ingredients = ""),
                FoodItem("Eggs & Egg Substitutes", "Egg alternatives", imageUrl = "https://static.vecteezy.com/system/resources/previews/019/908/317/non_2x/fresh-chicken-eggs-in-woven-bamboo-basket-isolated-with-clipping-path-in-file-format-close-up-with-full-focus-png.png", ingredients = ""),
                FoodItem("Nuts and Seeds", "Nuts and seeds", imageUrl = "https://static.vecteezy.com/system/resources/previews/059/652/408/non_2x/assortment-of-nuts-and-seeds-arranged-in-a-circular-pattern-on-a-png.png", ingredients = ""),
                FoodItem("Nut & Seed Butters", "Nut butters", imageUrl = "https://static.vecteezy.com/system/resources/previews/054/197/365/non_2x/delicious-peanut-butter-jar-for-cooking-and-snacking-on-transparent-background-png.png", ingredients = ""),
                FoodItem("Tofu & Seitan", "Plant proteins", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/025/222/302/small/tofu-cubes-isolated-on-transparent-background-png.png", ingredients = ""),
                FoodItem("Vegetarian Products", "Vegetarian options", imageUrl = "https://static.vecteezy.com/system/resources/previews/025/222/144/non_2x/tofu-cubes-isolated-on-transparent-background-png.png", ingredients = "")
            )

            "Condiments & More" -> listOf(
                FoodItem("Ketchup", "Tomato ketchup", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/049/158/919/small/fresh-tomatoes-and-ketchup-in-a-bowl-png.png", ingredients = ""),
                FoodItem("Mustard", "Mustard varieties", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/044/813/390/small/bowl-with-mustard-isolated-on-transparent-background-png.png", ingredients = ""),
                FoodItem("Mayonnaise", "Mayo products", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/055/208/870/small/white-ceramic-bowl-with-mayonnaise-png.png", ingredients = ""),
                FoodItem("Sauces, Gravies & Marinades", "Sauces and marinades", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/049/678/934/small/creamy-brown-gravy-served-in-a-ceramic-gravy-boat-transparent-png.png", ingredients = ""),
                FoodItem("Olives", "Olive products", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/036/698/507/small/ai-generated-olives-on-transparent-background-ai-generated-png.png", ingredients = ""),
                FoodItem("Pickles, Relish & Sauerkraut", "Pickled items", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/045/905/548/small/stack-of-sliced-pickles-cut-out-stock-png.png", ingredients = ""),
                FoodItem("Salad Dressing", "Dressings", imageUrl = "https://www.pngkey.com/png/full/315-3158107_grilled-prawn-and-asparagus-caesar-salad-salad-dressing.png", ingredients = ""),
                FoodItem("Salad Toppings", "Salad additions", imageUrl = "https://static.vecteezy.com/system/resources/previews/046/842/225/non_2x/green-bowl-overflowing-with-fresh-healthy-salad-ingredients-png.png", ingredients = ""),
                FoodItem("Pasta Sauce", "Pasta sauces", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/044/813/338/small/bowl-of-hot-chili-sauce-isolated-on-transparent-background-png.png", ingredients = "")
            )

            "Dips, Spreads & Jams" -> listOf(
                FoodItem("Nut & Seed Butters", "Nut butters", imageUrl = "https://static.vecteezy.com/system/resources/previews/054/197/365/non_2x/delicious-peanut-butter-jar-for-cooking-and-snacking-on-transparent-background-png.png", ingredients = ""),
                FoodItem("Salsa", "Salsa varieties", imageUrl = "https://static.vecteezy.com/system/resources/previews/059/903/488/non_2x/delicious-homemade-tomato-salsa-in-a-white-bowl-ready-to-serve-free-png.png", ingredients = ""),
                FoodItem("Hummus", "Hummus products", imageUrl = "https://static.vecteezy.com/system/resources/previews/055/669/281/non_2x/high-resolution-bowl-of-hummus-with-pita-bread-and-chickpeas-garnished-with-herbs-png.png", ingredients = ""),
                FoodItem("Other Dips", "Various dips", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/048/778/049/small/bowl-with-white-mayonnaise-dip-isolated-on-transparent-background-free-png.png", ingredients = ""),
                FoodItem("Jams, Syrup, Preserves", "Sweet spreads", imageUrl = "https://static.vecteezy.com/system/resources/previews/047/655/748/non_2x/tasty-strawberry-jam-jar-isolated-on-a-transparent-background-free-png.png", ingredients = "")
            )

            "Prepared Foods & Soups" -> listOf(
                FoodItem(title = "Prepared Appetizers & Snacks", subtitle = "Ready appetizers", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/065/283/572/small/variety-of-salty-snacks-and-popcorn-in-bowl-png.png", ingredients = ""),
                FoodItem(title = "Prepared Breakfast", subtitle = "Ready breakfast", imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR7vtzzJKlWhwWyKcTkCrpss1vGswylE6eHAQ&s", ingredients = ""),
                FoodItem(title = "Prepared Lunch - Burritos & Sandwiches", subtitle = "Lunch items", imageUrl = "https://static.vecteezy.com/system/resources/previews/047/706/301/non_2x/chicken-wrap-sandwich-with-transparent-background-png.png", ingredients = ""),
                FoodItem(title = "Prepared Meals - Entrees", subtitle = "Main dishes", imageUrl = "https://static.vecteezy.com/system/resources/previews/052/012/856/non_2x/grilled-steak-and-fries-on-a-plate-isolated-on-a-transparent-background-free-png.png", ingredients = ""),
                FoodItem(title = "Prepared Poultry Products", subtitle = "Ready poultry", imageUrl = "https://static.vecteezy.com/system/resources/previews/059/607/324/non_2x/grilled-sliced-chicken-breast-close-up-poultry-meat-cut-out-transparent-png.png", ingredients = ""),
                FoodItem(title = "Prepared Beef Products", subtitle = "Ready beef", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/047/598/224/small/beef-steak-served-in-plate-on-white-background-grilled-steak-medium-rare-png.png", ingredients = ""),
                FoodItem(title = "Prepared Fish & Seafood", subtitle = "Ready seafood", imageUrl = "https://static.vecteezy.com/system/resources/previews/060/596/298/non_2x/assorted-sushi-rolls-japanese-cuisine-delicacy-cut-out-transparent-png.png", ingredients = ""),
                FoodItem(title = "Prepared Pizza, Pasta & Noodles", subtitle = "Italian ready meals", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/045/383/391/small_2x/a-cheesy-delicious-pizza-with-tasty-pepperoni-on-a-transparent-background-png.png", ingredients = ""),
                FoodItem(title = "Prepared Sides & Salads", subtitle = "Side dishes", imageUrl = "https://static.vecteezy.com/system/resources/previews/054/057/192/non_2x/mixed-greens-and-vegetables-in-a-salad-bowl-png.png", ingredients = ""),
                FoodItem(title = "Prepared Soup", subtitle = "Ready soups", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/049/667/757/small/delicious-creamy-potato-and-carrot-soup-with-parsley-garnish-transparent-background-png.png", ingredients = ""),
                FoodItem(title = "Condensed Soup, Soup Mixes, Broth", subtitle = "Soup products", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/070/913/285/small/white-bowl-with-vegetable-soup-isolated-on-transparent-background-png.png", ingredients = ""),
                FoodItem(title = "Pasta Sauce", subtitle = "Pasta sauces", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/044/813/338/small/bowl-of-hot-chili-sauce-isolated-on-transparent-background-png.png", ingredients = "")
            )

            "Fast Food" -> listOf(
                FoodItem(title = "Sandwiches & Chicken", subtitle = "Sandwiches and chicken", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/041/277/335/small/ai-generated-sandwich-with-ham-cheese-tomatoes-and-lettuce-isolated-on-transparent-background-png.png", ingredients = ""),
                FoodItem(title = "Salads", subtitle = "Fast food salads", imageUrl = "https://static.vecteezy.com/system/resources/previews/054/057/192/non_2x/mixed-greens-and-vegetables-in-a-salad-bowl-png.png", ingredients = ""),
                FoodItem(title = "Breakfast", subtitle = "Breakfast items", imageUrl = "https://static.vecteezy.com/system/resources/previews/059/063/808/non_2x/delicious-and-hearty-full-english-breakfast-plate-with-fried-eggs-sausages-toast-beans-and-tomatoes-isolated-on-transparent-background-png.png", ingredients = ""),
                FoodItem(title = "Sides", subtitle = "Side dishes", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/036/290/866/small/ai-generated-french-fries-with-dipping-sauce-on-a-transparent-background-ai-png.png", ingredients = ""),
                FoodItem(title = "Soup", subtitle = "Soups", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/049/667/757/small/delicious-creamy-potato-and-carrot-soup-with-parsley-garnish-transparent-background-png.png", ingredients = ""),
                FoodItem(title = "Pizza", subtitle = "Pizza items", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/045/383/391/small_2x/a-cheesy-delicious-pizza-with-tasty-pepperoni-on-a-transparent-background-png.png", ingredients = ""),
                FoodItem(title = "Pasta", subtitle = "Pasta dishes", imageUrl = "https://static.vecteezy.com/system/resources/previews/054/720/991/non_2x/delicious-spaghetti-with-tomato-sauce-on-transparent-background-png.png", ingredients = ""),
                FoodItem(title = "Kids Meals", subtitle = "Children meals", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/041/641/646/small/3d-fast-food-icon-set-design-for-fast-food-delivery-minimal-design-concept-free-png.png", ingredients = ""),
                FoodItem(title = "Desserts", subtitle = "Sweet treats", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/050/511/637/small/chocolate-cake-slice-with-cream-and-nuts-ultra-realistic-design-on-transparent-background-for-bakery-gourmet-and-tasty-dessert-banners-and-posts-png.png", ingredients = ""),
                FoodItem(title = "Drinks", subtitle = "Beverages", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/057/672/767/small_2x/orange-juice-transparent-isolated-background-freshly-squeezed-refreshing-summer-drink-in-a-tall-glass-with-garnish-healthy-nutritious-natural-superfood-png.png", ingredients = ""),
                FoodItem(title = "Condiments & Dressing", subtitle = "Sauces and dressings", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/044/813/338/small/bowl-of-hot-chili-sauce-isolated-on-transparent-background-png.png", ingredients = "")
            )

            "Restaurant" -> listOf(
                FoodItem(title = "Sandwiches & Main Dishes", subtitle = "Main courses", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/041/277/335/small/ai-generated-sandwich-with-ham-cheese-tomatoes-and-lettuce-isolated-on-transparent-background-png.png", ingredients = ""),
                FoodItem(title = "Salads", subtitle = "Salad dishes", imageUrl = "https://static.vecteezy.com/system/resources/previews/054/057/192/non_2x/mixed-greens-and-vegetables-in-a-salad-bowl-png.png", ingredients = ""),
                FoodItem(title = "Sides", subtitle = "Side dishes", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/036/290/866/small/ai-generated-french-fries-with-dipping-sauce-on-a-transparent-background-ai-png.png", ingredients = ""),
                FoodItem(title = "Drinks", subtitle = "Beverages", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/057/672/767/small_2x/orange-juice-transparent-isolated-background-freshly-squeezed-refreshing-summer-drink-in-a-tall-glass-with-garnish-healthy-nutritious-natural-superfood-png.png", ingredients = ""),
                FoodItem(title = "Desserts", subtitle = "Sweet dishes", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/050/511/637/small/chocolate-cake-slice-with-cream-and-nuts-ultra-realistic-design-on-transparent-background-for-bakery-gourmet-and-tasty-dessert-banners-and-posts-png.png", ingredients = ""),
                FoodItem(title = "Condiments", subtitle = "Condiments", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/044/813/338/small/bowl-of-hot-chili-sauce-isolated-on-transparent-background-png.png", ingredients = ""),
                FoodItem(title = "Breakfast", subtitle = "Breakfast menu", imageUrl = "https://static.vecteezy.com/system/resources/previews/059/063/808/non_2x/delicious-and-hearty-full-english-breakfast-plate-with-fried-eggs-sausages-toast-beans-and-tomatoes-isolated-on-transparent-background-png.png", ingredients = ""),
                FoodItem(title = "Soups & Appetizers", subtitle = "Starters", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/049/667/757/small/delicious-creamy-potato-and-carrot-soup-with-parsley-garnish-transparent-background-png.png", ingredients = ""),
                FoodItem(title = "Pizza", subtitle = "Pizza dishes", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/045/383/391/small_2x/a-cheesy-delicious-pizza-with-tasty-pepperoni-on-a-transparent-background-png.png", ingredients = ""),
                FoodItem(title = "Pasta", subtitle = "Pasta dishes", imageUrl = "https://static.vecteezy.com/system/resources/previews/054/720/991/non_2x/delicious-spaghetti-with-tomato-sauce-on-transparent-background-png.png", ingredients = ""),
                FoodItem(title = "Kid's Meal", subtitle = "Children meals", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/041/641/646/small/3d-fast-food-icon-set-design-for-fast-food-delivery-minimal-design-concept-free-png.png", ingredients = "")
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
                FoodItem("Snack Bars", "Energy bars", imageUrl = "https://static.vecteezy.com/system/resources/previews/058/987/895/non_2x/granola-bar-with-chocolate-chips-healthy-snack-cut-out-transparent-png.png", ingredients = ""),
                FoodItem("Nuts and Seeds", "Nuts and seeds", imageUrl = "https://static.vecteezy.com/system/resources/previews/050/765/159/non_2x/trail-mix-mix-of-nuts-seeds-and-dried-fruits-served-in-small-wooden-bowl-png.png", ingredients = ""),
                FoodItem("Nut Mixes & Trail Mix", "Mixed nuts", imageUrl = "https://static.vecteezy.com/system/resources/previews/059/652/408/non_2x/assortment-of-nuts-and-seeds-arranged-in-a-circular-pattern-on-a-png.png", ingredients = ""),
                FoodItem("Popcorn", "Popcorn varieties", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/054/341/901/small/a-striped-red-and-white-container-filled-with-delicious-popcorn-png.png", ingredients = ""),
                FoodItem("Chips & Puffs", "Potato chips", imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSN_L706It58ZaRboqT1PsBbjFEVB5d8Eb4Xw&s", ingredients = ""),
                FoodItem("Crackers", "Snack crackers", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/059/309/751/small/appetizing-crispy-crackers-savory-flavor-food-photography-cut-out-transparent-png.png", ingredients = ""),
                FoodItem("Pretzels", "Pretzel snacks", imageUrl = "https://static.vecteezy.com/system/resources/previews/027/291/796/non_2x/pretzels-in-a-bowl-free-png.png", ingredients = ""),
                FoodItem("Jerky", "Dried meat snacks", imageUrl = "https://static.vecteezy.com/system/resources/previews/055/930/331/non_2x/delectable-stacked-beef-jerky-strips-on-transparent-background-free-png.png", ingredients = "")
            )

            "Sweet Snacks" -> listOf(
                FoodItem("Fruit Flavored Snacks", "Fruit snacks", imageUrl = "https://static.vecteezy.com/system/resources/previews/055/299/791/non_2x/sweet-fruit-jelly-candies-on-transparent-background-png.png", ingredients = ""),
                FoodItem("Candy & Chocolate", "Sweets and chocolate", imageUrl = "https://static.vecteezy.com/system/resources/previews/050/474/851/non_2x/a-colorful-heap-of-chocolates-and-candies-png.png", ingredients = ""),
                FoodItem("Cookies", "Cookie varieties", imageUrl = "https://static.vecteezy.com/system/resources/previews/044/308/311/non_2x/chocolate-chip-cookies-with-transparent-background-png.png", ingredients = ""),
                FoodItem("Gum & Mints", "Chewing gum and mints", imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ5l-rJrsCe0EyUPMS6r2LLst-yDYItxx1iZg&s", ingredients = ""),
                FoodItem("Sorbets & Popsicles", "Frozen treats", imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRyzVzclHAO6aY27OMuoERr-iQ3WCiq9zVeHg&s", ingredients = ""),
                FoodItem("Ice cream & Frozen Yogurt", "Frozen desserts", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/053/382/229/small/strawberry-sorbet-with-mint-garnish-png.png", ingredients = ""),
                FoodItem("Pudding & Gelatin", "Pudding products", imageUrl = "https://static.vecteezy.com/system/resources/previews/046/613/433/non_2x/pudding-isolated-on-transparent-background-free-png.png", ingredients = ""),
                FoodItem("Snack Cakes, Pies", "Sweet pastries", imageUrl = "https://static.vecteezy.com/system/resources/previews/046/804/487/non_2x/a-slice-of-chocolate-cake-free-png.png", ingredients = ""),
                FoodItem("Pastries", "Pastry items", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/044/763/490/small/croissant-french-pastry-croissant-top-view-puff-pastry-dessert-isolated-png.png", ingredients = ""),
                FoodItem("Snack Bars", "Sweet bars", imageUrl = "https://static.vecteezy.com/system/resources/previews/058/987/895/non_2x/granola-bar-with-chocolate-chips-healthy-snack-cut-out-transparent-png.png", ingredients = "")
            )

            "Beverages" -> listOf(
                FoodItem("Water, Unsweetened", "Plain water", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/048/051/079/small_2x/a-plastic-bottle-of-water-on-a-transparent-background-png.png", ingredients = ""),
                FoodItem("Water, Sweetened", "Flavored water", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/035/574/992/small_2x/ai-generated-red-soda-can-on-transparent-background-image-png.png", ingredients = ""),
                FoodItem("Coffee Beans & Instant Coffee", "Coffee products", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/041/290/274/small/ai-generated-coffee-beans-in-the-sack-isolated-on-transparent-background-png.png", ingredients = ""),
                FoodItem("Prepared Coffee Drinks", "Ready coffee", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/048/123/595/small/a-cup-of-cappuccino-coffee-drink-no-background-perfect-for-print-on-demand-t-shirt-design-merchandise-image-png.png", ingredients = ""),
                FoodItem("Tea Bags", "Tea products", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/044/245/664/small/single-tea-bag-isolated-on-transparent-background-png.png", ingredients = ""),
                FoodItem("Prepared Tea Drinks", "Ready tea", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/044/813/811/small/cup-of-tea-isolated-on-transparent-background-png.png", ingredients = ""),
                FoodItem("Juice and Fruit Drinks", "Fruit beverages", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/041/289/653/small/ai-generated-fresh-apple-juice-isolated-on-transparent-background-png.png", ingredients = ""),
                FoodItem("Drink Mix", "Drink mixes", imageUrl = "https://static.vecteezy.com/system/resources/previews/046/353/122/non_2x/refreshing-tropical-smoothie-featuring-a-mix-of-vibrant-fruits-for-a-nutritious-and-vitamin-rich-drink-free-png.png", ingredients = ""),
                FoodItem("Chocolate Drinks & Mixes", "Chocolate beverages", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/049/160/008/small/a-chocolate-milkshake-is-topped-with-swirls-of-whipped-cream-chocolate-drizzle-and-shavings-served-in-a-clear-glass-with-a-black-straw-png.png", ingredients = ""),
                FoodItem("Breakfast Drinks & Smoothies", "Smoothies", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/023/742/327/small/latte-coffee-isolated-illustration-ai-generative-free-png.png", ingredients = ""),
                FoodItem("Carbonated Soft Drinks", "Soda drinks", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/035/574/992/small_2x/ai-generated-red-soda-can-on-transparent-background-image-png.png", ingredients = ""),
                FoodItem("Energy and sports drinks", "Sports beverages", imageUrl = "https://static.vecteezy.com/system/resources/previews/065/986/826/non_2x/red-energy-drink-can-design-with-a-bold-lightning-bolt-logo-for-a-z-power-brand-png.png", ingredients = ""),
                FoodItem("Energy Drinks", "Energy beverages", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/060/423/854/small/cold-energy-drink-can-with-condensation-isolated-on-transparent-background-perfect-for-boosting-energy-and-focus-free-png.png", ingredients = ""),
                FoodItem("Wine", "Wine products", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/021/217/339/small/red-wine-bottle-png.png", ingredients = ""),
                FoodItem("Beer", "Beer products", imageUrl = "https://static.vecteezy.com/system/resources/previews/027/098/289/non_2x/glass-of-beer-isolated-png.png", ingredients = ""),
                FoodItem("Spirits & Liquors", "Spirits", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/059/634/105/small/refreshing-raspberry-lemonade-cocktail-in-elegant-glass-with-garnish-png.png", ingredients = ""),
                FoodItem("Cocktails", "Mixed drinks", imageUrl = "https://static.vecteezy.com/system/resources/previews/055/532/695/non_2x/refreshing-summer-cocktail-with-colorful-fruits-and-mint-served-on-a-bright-transparent-background-summer-cocktail-isolated-transparent-background-free-png.png", ingredients = "")
            )

            "Cooking & Baking" -> listOf(
                FoodItem("Bread Dough & Mix", "Bread mixes", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/032/325/247/small/raw-dough-isolated-on-transparent-background-file-cut-out-ai-generated-png.png", ingredients = ""),
                FoodItem("Cake & Cookie Dough Mix", "Baking mixes", imageUrl = "https://static.vecteezy.com/system/resources/previews/065/690/824/non_2x/chocolate-chip-cookie-dough-bowl-on-transparent-background-png.png", ingredients = ""),
                FoodItem("Flour & Meal", "Flour products", imageUrl = "https://static.vecteezy.com/system/resources/previews/051/674/509/non_2x/wooden-bowl-of-flour-with-wheat-branch-on-transparent-background-free-png.png", ingredients = ""),
                FoodItem("Sweeteners", "Sugar and sweeteners", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/045/726/706/small_2x/sugar-cubes-on-a-transparent-background-ai-generated-free-png.png", ingredients = ""),
                FoodItem("Non-Caloric Sweeteners", "Sugar substitutes", imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRL5B7pUnOfEvCqGhq_hmFE-vfp8QfE_hBnmg&s", ingredients = ""),
                FoodItem("Baking Ingredients", "Baking supplies", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/058/026/171/small/baking-ingredients-arranged-on-a-clean-surface-png.png", ingredients = ""),
                FoodItem("Oil & Shortening", "Cooking oils", imageUrl = "https://static.vecteezy.com/system/resources/previews/035/966/262/non_2x/two-bottle-of-vegetable-oils-in-set-isolated-in-file-format-top-view-and-flat-lay-png.png", ingredients = ""),
                FoodItem("Cooking Wine & Vinegar", "Vinegar products", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/021/217/339/small/red-wine-bottle-png.png", ingredients = ""),
                FoodItem("Spices, Seasonings & Rubs", "Seasonings", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/036/512/737/small/ai-generated-spices-and-herbs-in-wooden-bowl-isolated-on-transparent-background-png.png", ingredients = ""),
                FoodItem("Processed or Pre-Salted Spices", "Salted spices", imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRnG0Y9J3HbbQZaBPKhFRy4oSWAYGOvKy8UdQ&s", ingredients = ""),
                FoodItem("Just Salt", "Salt products", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/056/609/269/small/a-bowl-filled-with-a-generous-amount-of-white-crystalline-salt-free-png.png", ingredients = ""),
                FoodItem("Stuffing", "Stuffing mixes", imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRYOR5ARwM9xUO72G6xFdUC_YW7w0NvqKbk0w&s", ingredients = "")
            )

            "Baby Food" -> listOf(
                FoodItem(title = "Baby Formula", subtitle = "Infant formula", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/070/970/646/small/transparent-baby-bottle-filled-with-milk-png.png", ingredients = ""),
                FoodItem(title = "Baby Food - Stage 1 & 2", subtitle = "Early stage foods", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/066/666/693/small_2x/delicious-homemade-baby-food-bowl-with-spoon-isolated-on-transparent-background-png.png", ingredients = ""),
                FoodItem(title = "Baby Food - Stage 3", subtitle = "Advanced stage foods", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/055/236/626/small/a-small-glass-jar-with-a-yellow-lid-and-spoon-filled-with-creamy-yellow-substance-on-a-clear-background-png.png", ingredients = ""),
                FoodItem(title = "Baby Juice", subtitle = "Infant juices", imageUrl = "https://static.vecteezy.com/system/resources/previews/039/094/304/non_2x/juice-box-clipart-illustration-on-transparent-background-free-png.png", ingredients = "")
            )

            "Pet Food" -> listOf(
                FoodItem(title = "Dog Food", subtitle = "Dog food products", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/046/542/111/small/pet-food-in-bowl-png.png", ingredients = ""),
                FoodItem(title = "Cat Food", subtitle = "Cat food products", imageUrl = "https://static.vecteezy.com/system/resources/thumbnails/050/591/207/small/adorable-fluffy-kitten-sitting-beside-a-bowl-of-cat-food-on-transparent-background-free-png.png", ingredients = "")
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

















