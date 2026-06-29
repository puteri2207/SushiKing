package com.example.sushiking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CustomerReservationActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var chipGroup: ChipGroup
    private val allReservations = mutableListOf<Reservation>()
    private var currentFilter = "all"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_reservation)

        recyclerView = findViewById(R.id.recyclerView)
        tvEmpty = findViewById(R.id.tvEmpty)
        progressBar = findViewById(R.id.progressBar)
        chipGroup = findViewById(R.id.chipGroup)

        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val chip = group.findViewById<Chip>(checkedIds[0])
            currentFilter = when (chip.text.toString()) {
                "All" -> "all"
                "Pending" -> "pending"
                "Confirmed" -> "confirmed"
                "Completed" -> "completed"
                "No Show" -> "noshow"
                else -> "all"
            }
            filterAndDisplay()
        }

        loadReservations()
    }

    private fun loadReservations() {
        val uid = auth.currentUser?.uid ?: return
        progressBar.visibility = View.VISIBLE

        db.collection("reservations")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { docs ->
                progressBar.visibility = View.GONE
                allReservations.clear()
                docs.documents.forEach { doc ->
                    allReservations.add(Reservation(
                        reservationId = doc.getString("reservationId") ?: doc.id,
                        userId = doc.getString("userId") ?: "",
                        userName = doc.getString("userName") ?: "",
                        date = doc.getString("date") ?: "",
                        time = doc.getString("time") ?: "",
                        pax = doc.getLong("pax")?.toInt() ?: 1,
                        status = doc.getString("status") ?: "pending"
                    ))
                }
                allReservations.sortByDescending { it.date }
                filterAndDisplay()
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Failed to load reservations"
            }
    }

    private fun filterAndDisplay() {
        val filtered = if (currentFilter == "all") allReservations.toList()
        else allReservations.filter { it.status == currentFilter }

        if (filtered.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            tvEmpty.text = if (allReservations.isEmpty())
                "📅 No reservations yet!\nMake your first booking now."
            else "No $currentFilter reservations"
        } else {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerView.adapter = CustomerReservationAdapter(filtered)
        }
    }

    inner class CustomerReservationAdapter(private val list: List<Reservation>) :
        RecyclerView.Adapter<CustomerReservationAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvDate: TextView = view.findViewById(R.id.tvResDate)
            val tvTime: TextView = view.findViewById(R.id.tvResTime)
            val tvPax: TextView = view.findViewById(R.id.tvResPax)
            val tvStatus: TextView = view.findViewById(R.id.tvResStatus)
            val tvResId: TextView = view.findViewById(R.id.tvResId)
            val statusBar: View = view.findViewById(R.id.statusBar)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_customer_reservation, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val res = list[position]
            holder.tvDate.text = "📅 ${res.date}"
            holder.tvTime.text = "🕐 ${res.time}"
            holder.tvPax.text = "👥 ${res.pax} pax"
            holder.tvResId.text = "ID: ${res.reservationId.take(8).uppercase()}"

            when (res.status) {
                "pending" -> {
                    holder.tvStatus.text = "🕐 Pending"
                    holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#F97316"))
                    holder.statusBar.setBackgroundColor(android.graphics.Color.parseColor("#F97316"))
                }
                "confirmed" -> {
                    holder.tvStatus.text = "✅ Confirmed"
                    holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#22C55E"))
                    holder.statusBar.setBackgroundColor(android.graphics.Color.parseColor("#22C55E"))
                }
                "completed" -> {
                    holder.tvStatus.text = "🍣 Completed"
                    holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#3B82F6"))
                    holder.statusBar.setBackgroundColor(android.graphics.Color.parseColor("#3B82F6"))
                }
                "noshow" -> {
                    holder.tvStatus.text = "❌ No Show"
                    holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#EF4444"))
                    holder.statusBar.setBackgroundColor(android.graphics.Color.parseColor("#EF4444"))
                }
            }
        }

        override fun getItemCount() = list.size
    }
}
