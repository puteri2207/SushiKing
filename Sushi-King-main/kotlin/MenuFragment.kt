package com.example.sushiking

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

class MenuFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var etSearch: TextInputEditText
    private lateinit var chipGroupCategory: ChipGroup
    private lateinit var chipGroupSushiType: ChipGroup
    private lateinit var scrollSushiTypes: HorizontalScrollView
    private val allItems = mutableListOf<MenuItem>()

    // Sushi types
    private val sushiTypes = listOf("Sashimi", "Nigiri", "Gunkan", "Maki", "Volcano", "Temaki", "Special Sushi")

    private var selectedCategory = "All" // All, Sushi, Beverages, Desserts, Tsumami
    private var selectedSushiType = "All Sushi" // All Sushi, Sashimi, Nigiri, etc.

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_menu, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        progressBar = view.findViewById(R.id.progressBar)
        etSearch = view.findViewById(R.id.etSearch)
        chipGroupCategory = view.findViewById(R.id.chipGroupCategory)
        chipGroupSushiType = view.findViewById(R.id.chipGroupSushiType)
        scrollSushiTypes = view.findViewById(R.id.scrollSushiTypes)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Search
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterAndDisplay(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Level 1 - Category chips
        chipGroupCategory.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val chip = group.findViewById<Chip>(checkedIds[0])
            selectedCategory = when (chip.id) {
                R.id.chipAll -> "All"
                R.id.chipSushi -> "Sushi"
                R.id.chipBeverages -> "Beverages"
                R.id.chipDesserts -> "Desserts"
                else -> "All"
            }

            // Show/hide sushi type chips
            if (selectedCategory == "Sushi") {
                scrollSushiTypes.visibility = View.VISIBLE
            } else {
                scrollSushiTypes.visibility = View.GONE
                selectedSushiType = "All Sushi"
                // Reset sushi type chip to "All Sushi"
                chipGroupSushiType.check(R.id.chipSushiAll)
            }

            filterAndDisplay(etSearch.text.toString())
        }

        // Level 2 - Sushi type chips
        chipGroupSushiType.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val chip = group.findViewById<Chip>(checkedIds[0])
            selectedSushiType = chip.text.toString()
            filterAndDisplay(etSearch.text.toString())
        }

        loadMenu()
    }

    private fun loadMenu() {
        progressBar.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE
        recyclerView.visibility = View.GONE

        db.collection("menu").get()
            .addOnSuccessListener { docs ->
                progressBar.visibility = View.GONE
                allItems.clear()
                docs.documents.forEach { doc ->
                    allItems.add(MenuItem(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        type = doc.getString("type") ?: "",
                        description = doc.getString("description") ?: "",
                        ingredients = doc.getString("ingredients") ?: "",
                        calories = doc.getLong("calories")?.toInt() ?: 0,
                        price = doc.getDouble("price") ?: 0.0,
                        imageUrl = doc.getString("imageUrl") ?: ""
                    ))
                }
                allItems.sortBy { it.name }
                filterAndDisplay("")
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Failed to load menu."
            }
    }

    private fun filterAndDisplay(query: String) {
        var filtered = allItems.toList()

        // Filter by category
        when (selectedCategory) {
            "Sushi" -> {
                // Filter by sushi types
                if (selectedSushiType == "All Sushi") {
                    filtered = filtered.filter { it.type in sushiTypes }
                } else {
                    filtered = filtered.filter { it.type == selectedSushiType }
                }
            }
            "Beverages" -> filtered = filtered.filter { it.type == "Beverages" }
            "Desserts" -> filtered = filtered.filter { it.type == "Desserts" }
            "Tsumami" -> filtered = filtered.filter { it.type == "Tsumami" }
            "All" -> { /* show everything */ }
        }

        // Filter by search
        if (query.isNotEmpty()) {
            filtered = filtered.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.type.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }

        // Sort A-Z
        filtered = filtered.sortedBy { it.name }

        if (filtered.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            tvEmpty.text = if (allItems.isEmpty())
                "🍣 Menu is being prepared!\nCheck back soon."
            else "No items found"
        } else {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerView.adapter = MenuListAdapter(filtered)
        }
    }

    private fun showDetailDialog(item: MenuItem) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_menu_detail)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val ivImage = dialog.findViewById<ImageView>(R.id.ivDetailImage)
        val tvName = dialog.findViewById<TextView>(R.id.tvDetailName)
        val tvDesc = dialog.findViewById<TextView>(R.id.tvDetailDesc)
        val tvCalories = dialog.findViewById<TextView>(R.id.tvDetailCalories)
        val tvIngredients = dialog.findViewById<TextView>(R.id.tvDetailIngredients)
        val tvPrice = dialog.findViewById<TextView>(R.id.tvDetailPrice)
        val tvType = dialog.findViewById<TextView>(R.id.tvDetailType)
        val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)

        tvName.text = item.name
        tvDesc.text = item.description
        tvCalories.text = "${item.calories} kcal"
        tvIngredients.text = item.ingredients
        tvPrice.text = "RM %.2f".format(item.price)
        tvType.text = item.type

        val color = when (item.type) {
            "Sashimi" -> "#E31E24"
            "Nigiri" -> "#C9A96E"
            "Gunkan" -> "#065F46"
            "Maki" -> "#1E3A5F"
            "Volcano" -> "#7C2D12"
            "Temaki" -> "#4A1942"
            "Special Sushi" -> "#B45309"
            "Beverages" -> "#0369A1"
            "Desserts" -> "#BE185D"
            "Tsumami" -> "#065F46"
            else -> "#6B7280"
        }
        tvType.setBackgroundColor(Color.parseColor(color))

        if (item.imageUrl.isNotEmpty()) {
            Glide.with(requireContext())
                .load(item.imageUrl)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(ivImage)
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    inner class MenuListAdapter(private val list: List<MenuItem>) :
        RecyclerView.Adapter<MenuListAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivImage: ImageView = view.findViewById(R.id.ivItemImage)
            val tvName: TextView = view.findViewById(R.id.tvItemName)
            val tvPrice: TextView = view.findViewById(R.id.tvItemPrice)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_menu_grid, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvName.text = item.name
            holder.tvPrice.text = "RM %.2f".format(item.price)

            if (item.imageUrl.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(item.imageUrl)
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.ivImage)
            } else {
                holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            holder.itemView.setOnClickListener { showDetailDialog(item) }
        }

        override fun getItemCount() = list.size
    }
}
