package com.example.sushiking

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        supportFragmentManager.commit {
            replace(R.id.menuFragmentContainer, MenuFragment())
        }
    }
}
