package com.example.sushiking

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StaffLogin : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.staff_login)

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        findViewById<MaterialButton>(R.id.btnSignIn).setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showSnackbar("Please fill in all fields")
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser!!.uid
                        db.collection("staff").document(uid).get()
                            .addOnSuccessListener { doc ->
                                progressBar.visibility = View.GONE
                                if (doc.exists()) {
                                    startActivity(Intent(this, StaffMain::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    })
                                } else {
                                    auth.signOut()
                                    showSnackbar("Not a staff account!")
                                }
                            }
                            .addOnFailureListener {
                                progressBar.visibility = View.GONE
                                showSnackbar("Error: ${it.message}")
                            }
                    } else {
                        progressBar.visibility = View.GONE
                        showSnackbar("Login failed: ${task.exception?.message}")
                    }
                }
        }

        findViewById<android.widget.TextView>(R.id.tvRegister).setOnClickListener {
            startActivity(Intent(this, StaffSignUp::class.java))
        }
    }

    private fun showSnackbar(msg: String) {
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).show()
    }
}
