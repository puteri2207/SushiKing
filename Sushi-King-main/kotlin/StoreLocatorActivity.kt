package com.example.sushiking

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class StoreLocatorActivity : AppCompatActivity() {

    data class SushiKingOutlet(
        val name: String,
        val address: String,
        val hours: String,
        val phone: String,
        val lat: Double,
        val lng: Double
    )

    private val outlets = listOf(
        SushiKingOutlet("Sushi King Mid Valley", "Mid Valley Megamall, Lingkaran Syed Putra, Mid Valley City, KL", "10:00 AM - 10:00 PM", "03-2282 3378", 3.1179, 101.6773),
        SushiKingOutlet("Sushi King Sunway Pyramid", "Sunway Pyramid, 3 Jalan PJS 11/15, Bandar Sunway, Petaling Jaya", "10:00 AM - 10:00 PM", "03-5638 8228", 3.0731, 101.6070),
        SushiKingOutlet("Sushi King KLCC", "Suria KLCC, Jalan Ampang, Kuala Lumpur", "10:00 AM - 10:00 PM", "03-2382 0228", 3.1579, 101.7123),
        SushiKingOutlet("Sushi King IOI City Mall", "IOI City Mall, Lebuh IRC, IOI Resort City, Putrajaya", "10:00 AM - 10:00 PM", "03-8959 3228", 2.9701, 101.7223),
        SushiKingOutlet("Sushi King 1 Utama", "1 Utama Shopping Centre, 1 Lebuh Bandar Utama, Petaling Jaya", "10:00 AM - 10:00 PM", "03-7726 6228", 3.1483, 101.6150),
        SushiKingOutlet("Sushi King Pavilion KL", "Pavilion Kuala Lumpur, 168 Jalan Bukit Bintang, KL", "10:00 AM - 10:00 PM", "03-2141 3228", 3.1488, 101.7133),
        SushiKingOutlet("Sushi King The Curve", "The Curve, 6 Jalan PJU 7/3, Mutiara Damansara, PJ", "10:00 AM - 10:00 PM", "03-7728 5228", 3.1577, 101.5955),
        SushiKingOutlet("Sushi King Paradigm Mall", "Paradigm Mall, 1 Jalan SS7/26A, Kelana Jaya, PJ", "10:00 AM - 10:00 PM", "03-7887 3228", 3.1056, 101.5905),
        SushiKingOutlet("Sushi King Setia City Mall", "Setia City Mall, 7 Jalan Setia Dagang AH U13/AH, Shah Alam", "10:00 AM - 10:00 PM", "03-3358 3228", 3.1350, 101.5120),
        SushiKingOutlet("Sushi King Alamanda Putrajaya", "Alamanda Putrajaya, Presint 1, Putrajaya", "10:00 AM - 10:00 PM", "03-8889 3228", 2.9357, 101.6938)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_locator)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // Open all outlets in Google Maps
        findViewById<CardView>(R.id.btnFindNearMe).setOnClickListener {
            val uri = Uri.parse("geo:3.1390,101.6869?q=Sushi+King")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // Fallback to browser
                val browserUri = Uri.parse("https://www.google.com/maps/search/Sushi+King+Malaysia")
                startActivity(Intent(Intent.ACTION_VIEW, browserUri))
            }
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = OutletAdapter(outlets)
    }

    inner class OutletAdapter(private val list: List<SushiKingOutlet>) :
        RecyclerView.Adapter<OutletAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvOutletName)
            val tvAddress: TextView = view.findViewById(R.id.tvOutletAddress)
            val tvHours: TextView = view.findViewById(R.id.tvOutletHours)
            val tvPhone: TextView = view.findViewById(R.id.tvOutletPhone)
            val btnDirections: CardView = view.findViewById(R.id.btnDirections)
            val btnCall: CardView = view.findViewById(R.id.btnCall)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_outlet, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val outlet = list[position]
            holder.tvName.text = outlet.name
            holder.tvAddress.text = "📍 ${outlet.address}"
            holder.tvHours.text = "🕐 ${outlet.hours}"
            holder.tvPhone.text = "📞 ${outlet.phone}"

            holder.btnDirections.setOnClickListener {
                val uri = Uri.parse("geo:${outlet.lat},${outlet.lng}?q=${Uri.encode(outlet.name)}")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage("com.google.android.apps.maps")
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${outlet.lat},${outlet.lng}")
                    startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                }
            }

            holder.btnCall.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${outlet.phone.replace("-", "").replace(" ", "")}"))
                startActivity(intent)
            }
        }

        override fun getItemCount() = list.size
    }
}
