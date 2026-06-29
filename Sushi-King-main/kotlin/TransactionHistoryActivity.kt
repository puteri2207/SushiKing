package com.example.sushiking

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TransactionHistoryActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_history)

        recyclerView = findViewById(R.id.recyclerView)
        tvEmpty = findViewById(R.id.tvEmpty)
        progressBar = findViewById(R.id.progressBar)

        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        loadTransactions()
    }

    private fun loadTransactions() {
        val uid = auth.currentUser?.uid ?: return
        progressBar.visibility = View.VISIBLE

        db.collection("reservations")
            .whereEqualTo("userId", uid)
            .whereEqualTo("depositPaid", true)
            .get()
            .addOnSuccessListener { docs ->
                progressBar.visibility = View.GONE
                val list = docs.documents.mapNotNull { doc ->
                    val ordersRaw = doc.get("orderItems") as? List<*> ?: emptyList<Any>()
                    val orders = ordersRaw.mapNotNull { item ->
                        val map = item as? Map<*, *> ?: return@mapNotNull null
                        OrderItem(
                            menuItemId = map["menuItemId"] as? String ?: "",
                            menuItemName = map["menuItemName"] as? String ?: "",
                            price = (map["price"] as? Double) ?: 0.0,
                            quantity = ((map["quantity"] as? Long) ?: 1).toInt()
                        )
                    }
                    Reservation(
                        reservationId = doc.getString("reservationId") ?: doc.id,
                        userId = doc.getString("userId") ?: "",
                        userName = doc.getString("userName") ?: "",
                        date = doc.getString("date") ?: "",
                        time = doc.getString("time") ?: "",
                        pax = doc.getLong("pax")?.toInt() ?: 1,
                        status = doc.getString("status") ?: "pending",
                        depositPaid = doc.getBoolean("depositPaid") ?: false,
                        depositAmount = doc.getDouble("depositAmount") ?: 20.0,
                        orderItems = orders,
                        orderTotal = doc.getDouble("orderTotal") ?: 0.0,
                        totalAmount = doc.getDouble("totalAmount") ?: 20.0,
                        paymentMethod = doc.getString("paymentMethod") ?: "Card"
                    )
                }.sortedByDescending { it.date }

                if (list.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    tvEmpty.text = "💳 No transactions yet!\nMake a reservation to get started."
                } else {
                    tvEmpty.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    recyclerView.adapter = TransactionAdapter(list)
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Failed to load transactions"
            }
    }

    fun showReceipt(reservation: Reservation) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_receipt)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.findViewById<TextView>(R.id.tvReceiptId).text = "#${reservation.reservationId.take(8).uppercase()}"
        dialog.findViewById<TextView>(R.id.tvReceiptName).text = reservation.userName
        dialog.findViewById<TextView>(R.id.tvReceiptDate).text = reservation.date
        dialog.findViewById<TextView>(R.id.tvReceiptTime).text = reservation.time
        dialog.findViewById<TextView>(R.id.tvReceiptPax).text = "${reservation.pax} pax"
        dialog.findViewById<TextView>(R.id.tvReceiptStatus).text = reservation.status.uppercase()
        dialog.findViewById<TextView>(R.id.tvReceiptPayment).text = reservation.paymentMethod

        val itemsContainer = dialog.findViewById<LinearLayout>(R.id.llOrderItems)
        itemsContainer.removeAllViews()

        if (reservation.orderItems.isEmpty()) {
            val tv = TextView(this).apply {
                text = "No pre-ordered items"
                textSize = 13f
                setTextColor(Color.parseColor("#6B7280"))
                setPadding(0, 4, 0, 4)
            }
            itemsContainer.addView(tv)
        } else {
            reservation.orderItems.forEach { item ->
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 4, 0, 4)
                }
                val tvName = TextView(this).apply {
                    text = "• ${item.menuItemName} x${item.quantity}"
                    textSize = 13f
                    setTextColor(Color.parseColor("#1F2937"))
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                val tvPrice = TextView(this).apply {
                    text = "RM%.2f".format(item.price * item.quantity)
                    textSize = 13f
                    setTextColor(Color.parseColor("#1F2937"))
                }
                row.addView(tvName)
                row.addView(tvPrice)
                itemsContainer.addView(row)
            }
        }

        dialog.findViewById<TextView>(R.id.tvReceiptDeposit).text = "RM%.2f".format(reservation.depositAmount)
        dialog.findViewById<TextView>(R.id.tvReceiptFoodTotal).text = "RM%.2f".format(reservation.orderTotal)
        dialog.findViewById<TextView>(R.id.tvReceiptTotal).text = "RM%.2f".format(reservation.totalAmount)

        dialog.findViewById<MaterialButton>(R.id.btnCloseReceipt).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    inner class TransactionAdapter(private val list: List<Reservation>) :
        RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvId: TextView = view.findViewById(R.id.tvTransId)
            val tvDate: TextView = view.findViewById(R.id.tvTransDate)
            val tvAmount: TextView = view.findViewById(R.id.tvTransAmount)
            val tvStatus: TextView = view.findViewById(R.id.tvTransStatus)
            val tvItems: TextView = view.findViewById(R.id.tvTransItems)
            val btnReceipt: MaterialButton = view.findViewById(R.id.btnViewReceipt)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_transaction, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val res = list[position]
            holder.tvId.text = "#${res.reservationId.take(8).uppercase()}"
            holder.tvDate.text = "📅 ${res.date} ${res.time}"
            holder.tvAmount.text = "RM%.2f".format(res.totalAmount)
            holder.tvItems.text = if (res.orderItems.isEmpty()) "No pre-ordered food"
            else "${res.orderItems.size} item(s) pre-ordered"

            when (res.status) {
                "completed" -> {
                    holder.tvStatus.text = "✅ Completed"
                    holder.tvStatus.setTextColor(Color.parseColor("#22C55E"))
                }
                "confirmed" -> {
                    holder.tvStatus.text = "🔵 Confirmed"
                    holder.tvStatus.setTextColor(Color.parseColor("#3B82F6"))
                }
                "pending" -> {
                    holder.tvStatus.text = "🟡 Pending"
                    holder.tvStatus.setTextColor(Color.parseColor("#F97316"))
                }
                else -> {
                    holder.tvStatus.text = res.status
                    holder.tvStatus.setTextColor(Color.parseColor("#6B7280"))
                }
            }

            holder.btnReceipt.setOnClickListener { showReceipt(res) }
        }

        override fun getItemCount() = list.size
    }
}
