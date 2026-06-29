package com.example.sushiking

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivitySplash : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            checkUserRoleAndRoute(currentUser.uid)
            return
        }

        findViewById<MaterialButton>(R.id.btnGetStarted).setOnClickListener {
            startActivity(Intent(this, LoginChoice::class.java))
            finish()
        }
    }

    private fun checkUserRoleAndRoute(uid: String) {
        db.collection("staff").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    startActivity(Intent(this, StaffMain::class.java))
                } else {
                    startActivity(Intent(this, CustomerMain::class.java))
                }
                finish()
            }
            .addOnFailureListener {
                startActivity(Intent(this, CustomerMain::class.java))
                finish()
            }
    }
}
