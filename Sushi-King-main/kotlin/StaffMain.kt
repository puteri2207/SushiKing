package com.example.sushiking

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StaffMain : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.staff_main)

        val uid = auth.currentUser?.uid ?: return

        db.collection("staff").document(uid).get()
            .addOnSuccessListener { doc ->
                val firstName = doc.getString("firstName") ?: ""
                val lastName = doc.getString("lastName") ?: ""
                val name = doc.getString("name") ?: "$firstName $lastName"
                val staffId = doc.getString("staffId") ?: "N/A"
                val role = doc.getString("role") ?: "Staff"
                val phone = doc.getString("phone") ?: "N/A"
                val email = doc.getString("email") ?: "N/A"

                findViewById<TextView>(R.id.tvStaffName).text = "Hello, ${firstName.ifEmpty { name }}! 👔"
                findViewById<TextView>(R.id.tvStaffId).text = "Staff ID: $staffId"
                findViewById<TextView>(R.id.tvStaffRole).text = role

                findViewById<CardView>(R.id.btnStaffProfile).setOnClickListener {
                    showStaffProfile(firstName, lastName, staffId, phone, email, role)
                }
            }
            .addOnFailureListener {
                findViewById<TextView>(R.id.tvStaffName).text = "Hello, Staff! 👔"
                findViewById<CardView>(R.id.btnStaffProfile).setOnClickListener {
                    Snackbar.make(findViewById(android.R.id.content), "Could not load profile", Snackbar.LENGTH_SHORT).show()
                }
            }

        loadTodayReservations()

        findViewById<CardView>(R.id.btnManageReservations).setOnClickListener {
            startActivity(Intent(this, StaffReservationActivity::class.java))
        }

        findViewById<CardView>(R.id.btnViewMenu).setOnClickListener {
            startActivity(Intent(this, StaffMenuActivity::class.java))
        }

        findViewById<CardView>(R.id.btnAnnouncements).setOnClickListener {
            showAnnouncements()
        }

        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    auth.signOut()
                    startActivity(Intent(this, LoginChoice::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun loadTodayReservations() {
        val container = findViewById<LinearLayout>(R.id.reservationsList)
        container.removeAllViews()

        db.collection("reservations")
            .whereEqualTo("status", "pending")
            .limit(10)
            .get()
            .addOnSuccessListener { docs ->
                container.removeAllViews()
                if (docs.isEmpty) {
                    val tv = TextView(this).apply {
                        text = "🎉 No pending reservations"
                        setTextColor(Color.parseColor("#9CA3AF"))
                        textSize = 14f
                        gravity = Gravity.CENTER
                        setPadding(16, 16, 16, 16)
                    }
                    container.addView(tv)
                    return@addOnSuccessListener
                }

                docs.documents.forEach { doc ->
                    val name = doc.getString("userName") ?: "Guest"
                    val date = doc.getString("date") ?: ""
                    val time = doc.getString("time") ?: ""
                    val pax = doc.getLong("pax")?.toInt() ?: 1
                    val resId = doc.getString("reservationId") ?: doc.id

                    val row = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        setPadding(0, 12, 0, 12)
                    }

                    val info = TextView(this).apply {
                        text = "🍽  $name  •  $date  $time  •  $pax pax"
                        setTextColor(Color.WHITE)
                        textSize = 13f
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    }

                    val confirmBtn = MaterialButton(this).apply {
                        text = "✓ Confirm"
                        textSize = 12f
                        setBackgroundColor(Color.parseColor("#22C55E"))
                        setTextColor(Color.WHITE)
                        setOnClickListener { confirmReservation(resId) }
                    }

                    row.addView(info)
                    row.addView(confirmBtn)
                    container.addView(row)
                }
            }
    }

    private fun confirmReservation(reservationId: String) {
        db.collection("reservations").document(reservationId)
            .update("status", "confirmed")
            .addOnSuccessListener {
                MaterialAlertDialogBuilder(this)
                    .setTitle("✅ Confirmed!")
                    .setMessage("Reservation confirmed successfully.")
                    .setPositiveButton("OK") { _, _ -> loadTodayReservations() }
                    .show()
            }
    }

    private fun showStaffProfile(
        firstName: String, lastName: String, staffId: String,
        phone: String, email: String, role: String
    ) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_staff_profile, null)

        val fullName = "$firstName $lastName".trim()
        dialogView.findViewById<TextView>(R.id.tvAvatar).text =
            firstName.firstOrNull()?.uppercase() ?: "S"
        dialogView.findViewById<TextView>(R.id.tvName).text = fullName.ifEmpty { "Staff" }
        dialogView.findViewById<TextView>(R.id.tvRole).text = role
        dialogView.findViewById<TextView>(R.id.tvFirstName).text = firstName.ifEmpty { "N/A" }
        dialogView.findViewById<TextView>(R.id.tvLastName).text = lastName.ifEmpty { "N/A" }
        dialogView.findViewById<TextView>(R.id.tvStaffIdDialog).text = staffId
        dialogView.findViewById<TextView>(R.id.tvPhone).text = phone
        dialogView.findViewById<TextView>(R.id.tvEmail).text = email

        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun showAnnouncements() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_announcement, null)
        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton("Got it!", null)
            .show()
    }
}
