package com.example.sushiking

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StaffSignUp : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var etFirstName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etStaffId: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_signup)

        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etStaffId = findViewById(R.id.etStaffId)
        etPhone = findViewById(R.id.etPhone)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        progressBar = findViewById(R.id.progressBar)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<MaterialButton>(R.id.btnRegister).setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val staffId = etStaffId.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (!validate(firstName, lastName, staffId, phone, email, password, confirmPassword)) return@setOnClickListener
            registerStaff(firstName, lastName, staffId, phone, email, password)
        }

        findViewById<TextView>(R.id.tvSignIn).setOnClickListener { finish() }
    }

    private fun validate(
        firstName: String, lastName: String, staffId: String,
        phone: String, email: String, password: String, confirmPassword: String
    ): Boolean {
        if (firstName.isEmpty()) { showSnackbar("First name is required"); return false }
        if (lastName.isEmpty()) { showSnackbar("Last name is required"); return false }
        if (staffId.isEmpty()) { showSnackbar("Staff ID is required"); return false }
        if (phone.isEmpty()) { showSnackbar("Phone number is required"); return false }
        if (email.isEmpty()) { showSnackbar("Email is required"); return false }
        if (password.isEmpty()) { showSnackbar("Password is required"); return false }
        if (password.length < 6) { showSnackbar("Password must be at least 6 characters"); return false }
        if (password != confirmPassword) { showSnackbar("Passwords don't match"); return false }
        return true
    }

    private fun registerStaff(
        firstName: String, lastName: String, staffId: String,
        phone: String, email: String, password: String
    ) {
        setLoading(true)

        // Check if staffId already exists
        db.collection("staff")
            .whereEqualTo("staffId", staffId)
            .get()
            .addOnSuccessListener { docs ->
                if (!docs.isEmpty) {
                    setLoading(false)
                    showSnackbar("Staff ID already registered!")
                    return@addOnSuccessListener
                }

                // Create Firebase Auth account
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = auth.currentUser!!.uid
                            val staff = Staff(
                                uid = uid,
                                firstName = firstName,
                                lastName = lastName,
                                name = "$firstName $lastName",
                                email = email,
                                staffId = staffId,
                                phone = phone,
                                role = "Cashier"
                            )
                            db.collection("staff").document(uid).set(staff)
                                .addOnSuccessListener {
                                    setLoading(false)
                                    showSuccessDialog(firstName)
                                }
                                .addOnFailureListener { e ->
                                    setLoading(false)
                                    showSnackbar("Error: ${e.message}")
                                }
                        } else {
                            setLoading(false)
                            showSnackbar("Registration failed: ${task.exception?.message}")
                        }
                    }
            }
            .addOnFailureListener {
                setLoading(false)
                showSnackbar("Error checking Staff ID")
            }
    }

    private fun showSuccessDialog(firstName: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("🎉 Registration Successful!")
            .setMessage("Welcome, $firstName!\n\nYour staff account has been created. You can now login with your credentials.")
            .setPositiveButton("Login Now") { _, _ ->
                auth.signOut()
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        findViewById<MaterialButton>(R.id.btnRegister).isEnabled = !loading
    }

    private fun showSnackbar(msg: String) {
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).show()
    }
}
