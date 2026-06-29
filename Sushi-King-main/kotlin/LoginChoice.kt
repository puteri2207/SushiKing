package com.example.sushiking

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class LoginChoice : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_choice)

        findViewById<MaterialButton>(R.id.btnCustomer).setOnClickListener {
            startActivity(Intent(this, CustomerLogin::class.java))
        }

        findViewById<MaterialButton>(R.id.btnStaff).setOnClickListener {
            startActivity(Intent(this, StaffLogin::class.java))
        }
    }
}
