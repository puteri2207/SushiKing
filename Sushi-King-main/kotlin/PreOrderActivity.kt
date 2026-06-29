package com.example.sushiking

import android.content.Intent
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

class PreOrderActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var etSearch: TextInputEditText
    private lateinit var chipGroupCategory: ChipGroup
    private lateinit var chipGroupSushiType: ChipGroup
    private lateinit var scrollSushiTypes: HorizontalScrollView
    private lateinit var tvSelectedCount: TextView
    private lateinit var btnConfirmOrder: MaterialButton

    private val allItems = mutableListOf<MenuItem>()
    val orderItems = mutableListOf<OrderItem>()
    private val sushiTypes = listOf("Sashimi", "Nigiri", "Gunkan", "Maki", "Volcano", "Temaki", "Special Sushi")
    private var selectedCategory = "All"
    private var selectedSushiType = "All Sushi"

    companion object {
        const val RESULT_ORDER = "order_result"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preorder)

        recyclerView = findViewById(R.id.recyclerView)
        tvEmpty = findViewById(R.id.tvEmpty)
        progressBar = findViewById(R.id.progressBar)
        etSearch = findViewById(R.id.etSearch)
        chipGroupCategory = findViewById(R.id.chipGroupCategory)
        chipGroupSushiType = findViewById(R.id.chipGroupSushiType)
        scrollSushiTypes = findViewById(R.id.scrollSushiTypes)
        tvSelectedCount = findViewById(R.id.tvSelectedCount)
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder)

        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterAndDisplay(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        chipGroupCategory.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val chip = group.findViewById<Chip>(checkedIds[0])
            selectedCategory = when (chip.id) {
                R.id.chipAll -> "All"
                R.id.chipSushi -> "Sushi"
                R.id.chipBeverages -> "Beverages"
                R.id.chipDesserts -> "Desserts"
                R.id.chipTsumami -> "Tsumami"
                else -> "All"
            }
            scrollSushiTypes.visibility = if (selectedCategory == "Sushi") View.VISIBLE else View.GONE
            filterAndDisplay(etSearch.text.toString())
        }

        chipGroupSushiType.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val chip = group.findViewById<Chip>(checkedIds[0])
            selectedSushiType = chip.text.toString()
            filterAndDisplay(etSearch.text.toString())
        }

        btnConfirmOrder.setOnClickListener {
            val intent = Intent()
            val orderData = ArrayList<HashMap<String, Any>>()
            orderItems.forEach { item ->
                orderData.add(hashMapOf(
                    "menuItemId" to item.menuItemId,
                    "menuItemName" to item.menuItemName,
                    "price" to item.price,
                    "quantity" to item.quantity
                ))
            }
            intent.putExtra("orderItems", orderData)
            intent.putExtra("orderTotal", orderItems.sumOf { it.price * it.quantity })
            setResult(RESULT_OK, intent)
            finish()
        }

        loadMenu()
    }

    private fun loadMenu() {
        progressBar.visibility = View.VISIBLE
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
    }

    private fun filterAndDisplay(query: String) {
        var filtered = allItems.toList()
        when (selectedCategory) {
            "Sushi" -> filtered = if (selectedSushiType == "All Sushi")
                filtered.filter { it.type in sushiTypes }
            else filtered.filter { it.type == selectedSushiType }
            "Beverages" -> filtered = filtered.filter { it.type == "Beverages" }
            "Desserts" -> filtered = filtered.filter { it.type == "Desserts" }
            "Tsumami" -> filtered = filtered.filter { it.type == "Tsumami" }
        }
        if (query.isNotEmpty()) {
            filtered = filtered.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }
        filtered = filtered.sortedBy { it.name }

        if (filtered.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerView.adapter = PreOrderAdapter(filtered)
        }
    }

    fun updateOrderCount() {
        val totalItems = orderItems.sumOf { it.quantity }
        val totalPrice = orderItems.sumOf { it.price * it.quantity }
        if (totalItems > 0) {
            tvSelectedCount.text = "$totalItems item(s) selected — RM%.2f".format(totalPrice)
            tvSelectedCount.visibility = View.VISIBLE
            btnConfirmOrder.text = "Confirm Order ($totalItems) ✅"
        } else {
            tvSelectedCount.visibility = View.GONE
            btnConfirmOrder.text = "Confirm Order"
        }
    }

    fun showItemDetail(item: MenuItem) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_preorder_detail)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val ivImage = dialog.findViewById<ImageView>(R.id.ivDetailImage)
        val tvName = dialog.findViewById<TextView>(R.id.tvDetailName)
        val tvType = dialog.findViewById<TextView>(R.id.tvDetailType)
        val tvDesc = dialog.findViewById<TextView>(R.id.tvDetailDesc)
        val tvCalories = dialog.findViewById<TextView>(R.id.tvDetailCalories)
        val tvIngredients = dialog.findViewById<TextView>(R.id.tvDetailIngredients)
        val tvPrice = dialog.findViewById<TextView>(R.id.tvDetailPrice)
        val tvQty = dialog.findViewById<TextView>(R.id.tvQty)
        val btnMinus = dialog.findViewById<MaterialButton>(R.id.btnMinus)
        val btnPlus = dialog.findViewById<MaterialButton>(R.id.btnPlus)
        val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)
        val btnAddToOrder = dialog.findViewById<MaterialButton>(R.id.btnAddToOrder)

        tvName.text = item.name
        tvType.text = item.type
        tvDesc.text = item.description
        tvCalories.text = "🔥 ${item.calories} kcal"
        tvIngredients.text = item.ingredients
        tvPrice.text = "RM %.2f".format(item.price)

        val existing = orderItems.find { it.menuItemId == item.id }
        var qty = existing?.quantity ?: 0
        tvQty.text = qty.toString()

        if (item.imageUrl.isNotEmpty()) {
            Glide.with(this).load(item.imageUrl).centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery).into(ivImage)
        }

        fun updateBtn() {
            btnAddToOrder.text = if (qty > 0) "Update Order ($qty)" else "Add to Order"
        }
        updateBtn()

        btnMinus.setOnClickListener {
            if (qty > 0) { qty--; tvQty.text = qty.toString(); updateBtn() }
        }
        btnPlus.setOnClickListener {
            qty++; tvQty.text = qty.toString(); updateBtn()
        }

        btnClose.setOnClickListener { dialog.dismiss() }

        btnAddToOrder.setOnClickListener {
            val order = orderItems.find { it.menuItemId == item.id }
            if (qty == 0) {
                orderItems.removeIf { it.menuItemId == item.id }
            } else if (order != null) {
                val index = orderItems.indexOf(order)
                orderItems[index] = order.copy(quantity = qty)
            } else {
                orderItems.add(OrderItem(item.id, item.name, item.price, qty))
            }
            updateOrderCount()
            recyclerView.adapter?.notifyDataSetChanged()
            dialog.dismiss()
        }

        dialog.show()
    }

    inner class PreOrderAdapter(private val list: List<MenuItem>) :
        RecyclerView.Adapter<PreOrderAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivImage: ImageView = view.findViewById(R.id.ivItemImage)
            val tvName: TextView = view.findViewById(R.id.tvItemName)
            val tvPrice: TextView = view.findViewById(R.id.tvItemPrice)
            val tvQty: TextView = view.findViewById(R.id.tvQty)
            val btnMinus: MaterialButton = view.findViewById(R.id.btnMinus)
            val btnPlus: MaterialButton = view.findViewById(R.id.btnPlus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_preorder, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            val existing = orderItems.find { it.menuItemId == item.id }
            val qty = existing?.quantity ?: 0

            holder.tvName.text = item.name
            holder.tvPrice.text = "RM %.2f".format(item.price)
            holder.tvQty.text = qty.toString()

            if (item.imageUrl.isNotEmpty()) {
                Glide.with(this@PreOrderActivity).load(item.imageUrl).centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery).into(holder.ivImage)
            }

            // Click item to see detail
            holder.itemView.setOnClickListener { showItemDetail(item) }

            holder.btnMinus.setOnClickListener {
                val order = orderItems.find { it.menuItemId == item.id }
                if (order != null) {
                    if (order.quantity > 1) {
                        val index = orderItems.indexOf(order)
                        orderItems[index] = order.copy(quantity = order.quantity - 1)
                    } else orderItems.remove(order)
                }
                notifyItemChanged(position)
                updateOrderCount()
            }

            holder.btnPlus.setOnClickListener {
                val order = orderItems.find { it.menuItemId == item.id }
                if (order != null) {
                    val index = orderItems.indexOf(order)
                    orderItems[index] = order.copy(quantity = order.quantity + 1)
                } else orderItems.add(OrderItem(item.id, item.name, item.price, 1))
                notifyItemChanged(position)
                updateOrderCount()
            }
        }

        override fun getItemCount() = list.size
    }
}
