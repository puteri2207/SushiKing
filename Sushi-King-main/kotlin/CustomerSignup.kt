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

class CustomerSignup : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var tilName: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.customer_signup)

        tilName = findViewById(R.id.tilName)
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        progressBar = findViewById(R.id.progressBar)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<MaterialButton>(R.id.btnSignUp).setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirm = etConfirmPassword.text.toString().trim()
            if (!validate(name, email, password, confirm)) return@setOnClickListener
            signUp(name, email, password)
        }

        findViewById<TextView>(R.id.tvSignIn).setOnClickListener { finish() }
    }

    private fun validate(name: String, email: String, password: String, confirm: String): Boolean {
        var valid = true
        listOf(tilName, tilEmail, tilPassword, tilConfirmPassword).forEach { it.error = null }
        if (name.isEmpty()) { tilName.error = "Name is required"; valid = false }
        if (email.isEmpty()) { tilEmail.error = "Email is required"; valid = false }
        if (password.isEmpty()) { tilPassword.error = "Password is required"; valid = false }
        if (password.length < 6) { tilPassword.error = "Min 6 characters"; valid = false }
        if (password != confirm) { tilConfirmPassword.error = "Passwords don't match"; valid = false }
        return valid
    }

    private fun signUp(name: String, email: String, password: String) {
        setLoading(true)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser!!.uid
                    val user = User(
                        uid = uid, name = name, email = email,
                        role = "customer", smilePoints = 0, stamps = 0
                    )
                    db.collection("users").document(uid).set(user)
                        .addOnSuccessListener {
                            setLoading(false)
                            showWelcomeDialog(name)
                        }
                        .addOnFailureListener { e ->
                            setLoading(false)
                            showSnackbar("Error: ${e.message}")
                        }
                } else {
                    setLoading(false)
                    showSnackbar("Sign up failed: ${task.exception?.message}")
                }
            }
    }

    private fun showWelcomeDialog(name: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("🎉 Congrats for joining us!")
            .setMessage("Welcome to Sushi King, $name!\n\nYou can now earn Smile Points every time you dine with us.")
            .setPositiveButton("Visit Us Now!") { _, _ ->
                startActivity(Intent(this, CustomerMain::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
            .setCancelable(false)
            .show()
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        findViewById<MaterialButton>(R.id.btnSignUp).isEnabled = !loading
    }

    private fun showSnackbar(msg: String) {
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).show()
    }
}
