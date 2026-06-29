package com.example.sushiking

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder

class ShowQrActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.show_qr)

        val uid = auth.currentUser?.uid ?: return
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val ivQrCode = findViewById<ImageView>(R.id.ivQrCode)
        val tvName = findViewById<TextView>(R.id.tvName)
        val tvMemberId = findViewById<TextView>(R.id.tvMemberId)

        progressBar.visibility = View.VISIBLE
        ivQrCode.visibility = View.GONE

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "Member"
                tvName.text = name
                tvMemberId.text = "Member ID: ${uid.take(8).uppercase()}"
                generateQr(uid, ivQrCode, progressBar)
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                generateQr(uid, ivQrCode, progressBar)
            }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun generateQr(data: String, ivQrCode: ImageView, progressBar: ProgressBar) {
        try {
            val hints = mapOf(EncodeHintType.MARGIN to 1)
            val matrix = MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, 600, 600, hints)
            val bitmap: Bitmap = BarcodeEncoder().createBitmap(matrix)
            progressBar.visibility = View.GONE
            ivQrCode.visibility = View.VISIBLE
            ivQrCode.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            progressBar.visibility = View.GONE
        }
    }
}
