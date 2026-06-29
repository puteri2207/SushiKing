package com.example.sushiking

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

class StaffMenuActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var etSearch: TextInputEditText
    private val allItems = mutableListOf<MenuItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_menu)

        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        etSearch = findViewById(R.id.etSearch)
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btnAddItem).setOnClickListener { showAddDialog() }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterItems(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

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
                filterItems(etSearch.text.toString())
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Failed to load menu"
            }
    }

    private fun filterItems(query: String) {
        val filtered = if (query.isEmpty()) allItems.toList()
        else allItems.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.type.contains(query, ignoreCase = true)
        }

        if (filtered.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerView.adapter = StaffMenuAdapter(filtered.toMutableList())
        }
    }

    private fun showAddDialog(existing: MenuItem? = null) {
        // Use Dialog directly — NO MaterialAlertDialogBuilder
        // This removes the double header problem!
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_menu_item)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.95).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val tvDialogTitle = dialog.findViewById<TextView>(R.id.tvDialogTitle)
        val etName = dialog.findViewById<TextInputEditText>(R.id.etName)
        val spinnerType = dialog.findViewById<Spinner>(R.id.spinnerType)
        val etDesc = dialog.findViewById<TextInputEditText>(R.id.etDesc)
        val etIngredients = dialog.findViewById<TextInputEditText>(R.id.etIngredients)
        val etCalories = dialog.findViewById<TextInputEditText>(R.id.etCalories)
        val etPrice = dialog.findViewById<TextInputEditText>(R.id.etPrice)
        val etImageUrl = dialog.findViewById<TextInputEditText>(R.id.etImageUrl)
        val ivPreview = dialog.findViewById<ImageView>(R.id.ivPreview)
        val btnCancel = dialog.findViewById<MaterialButton>(R.id.btnCancel)
        val btnSave = dialog.findViewById<MaterialButton>(R.id.btnSave)
        val btnDelete = dialog.findViewById<MaterialButton>(R.id.btnDelete)

        val types = listOf(
            "Beverages", "Desserts", "Gunkan", "Maki", "Nigiri",
            "Sashimi", "Special Sushi", "Temaki", "Tsumami", "Volcano"
        )
        spinnerType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)

        // Set title and fill fields if editing
        if (existing != null) {
            tvDialogTitle.text = "✏️ Edit Menu Item"
            etName.setText(existing.name)
            spinnerType.setSelection(types.indexOf(existing.type).coerceAtLeast(0))
            etDesc.setText(existing.description)
            etIngredients.setText(existing.ingredients)
            etCalories.setText(existing.calories.toString())
            etPrice.setText(existing.price.toString())
            etImageUrl.setText(existing.imageUrl)
            if (existing.imageUrl.isNotEmpty()) {
                ivPreview.visibility = View.VISIBLE
                Glide.with(this).load(existing.imageUrl).centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery).into(ivPreview)
            }
            btnDelete.visibility = View.VISIBLE
        } else {
            tvDialogTitle.text = "➕ Add Menu Item"
            btnDelete.visibility = View.GONE
        }

        // Live image preview
        etImageUrl.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val url = s.toString().trim()
                if (url.startsWith("http")) {
                    ivPreview.visibility = View.VISIBLE
                    Glide.with(this@StaffMenuActivity).load(url).centerCrop()
                        .placeholder(android.R.drawable.ic_menu_gallery).into(ivPreview)
                } else {
                    ivPreview.visibility = View.GONE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Cancel
        btnCancel.setOnClickListener { dialog.dismiss() }

        // Save
        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter item name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val item = hashMapOf(
                "name" to name,
                "type" to spinnerType.selectedItem.toString(),
                "description" to etDesc.text.toString().trim(),
                "ingredients" to etIngredients.text.toString().trim(),
                "calories" to (etCalories.text.toString().toIntOrNull() ?: 0),
                "price" to (etPrice.text.toString().toDoubleOrNull() ?: 0.0),
                "imageUrl" to etImageUrl.text.toString().trim()
            )
            if (existing == null) {
                db.collection("menu").add(item).addOnSuccessListener {
                    loadMenu()
                    dialog.dismiss()
                    Toast.makeText(this, "✅ Item added!", Toast.LENGTH_SHORT).show()
                }
            } else {
                db.collection("menu").document(existing.id).update(item as Map<String, Any>)
                    .addOnSuccessListener {
                        loadMenu()
                        dialog.dismiss()
                        Toast.makeText(this, "✅ Item updated!", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // Delete
        btnDelete.setOnClickListener {
            dialog.dismiss()
            MaterialAlertDialogBuilder(this)
                .setTitle("Delete Item")
                .setMessage("Delete '${existing?.name}' from menu?")
                .setPositiveButton("Delete") { _, _ ->
                    existing?.let { item ->
                        db.collection("menu").document(item.id).delete()
                            .addOnSuccessListener {
                                loadMenu()
                                Toast.makeText(this, "🗑 Item deleted", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        dialog.show()
    }

    inner class StaffMenuAdapter(private val list: MutableList<MenuItem>) :
        RecyclerView.Adapter<StaffMenuAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvItemName)
            val tvType: TextView = view.findViewById(R.id.tvItemType)
            val tvPrice: TextView = view.findViewById(R.id.tvItemPrice)
            val tvCalories: TextView = view.findViewById(R.id.tvItemCalories)
            val ivImage: ImageView = view.findViewById(R.id.ivMenuImage)
            val btnEdit: MaterialButton = view.findViewById(R.id.btnEdit)
            val btnDelete: MaterialButton = view.findViewById(R.id.btnDelete)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_menu_staff, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvName.text = item.name
            holder.tvType.text = item.type
            holder.tvPrice.text = "RM %.2f".format(item.price)
            holder.tvCalories.text = "🔥 ${item.calories} kcal"

            if (item.imageUrl.isNotEmpty()) {
                Glide.with(this@StaffMenuActivity)
                    .load(item.imageUrl)
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(holder.ivImage)
            } else {
                holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            holder.btnEdit.setOnClickListener { showAddDialog(item) }
            holder.btnDelete.setOnClickListener {
                MaterialAlertDialogBuilder(this@StaffMenuActivity)
                    .setTitle("Delete Item")
                    .setMessage("Delete '${item.name}' from menu?")
                    .setPositiveButton("Delete") { _, _ ->
                        db.collection("menu").document(item.id).delete()
                            .addOnSuccessListener { loadMenu() }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        override fun getItemCount() = list.size
    }
}
