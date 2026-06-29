package com.example.sushiking

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = auth.currentUser?.uid ?: run {
            startActivity(Intent(requireContext(), CustomerLogin::class.java))
            requireActivity().finish()
            return
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "Guest"
                view.findViewById<TextView>(R.id.tvUserName).text = "$name! 👋"
                view.findViewById<TextView>(R.id.tvAvatar).text =
                    name.firstOrNull()?.uppercase() ?: "?"
            }

        view.findViewById<MaterialButton>(R.id.btnShowQr).setOnClickListener {
            startActivity(Intent(requireContext(), ShowQrActivity::class.java))
        }

        view.findViewById<CardView>(R.id.cardMenu).setOnClickListener {
            startActivity(Intent(requireContext(), MenuActivity::class.java))
        }

        view.findViewById<CardView>(R.id.cardPromos).setOnClickListener {
            startActivity(Intent(requireContext(), PromoActivity::class.java))
        }

        view.findViewById<CardView>(R.id.cardReservation).setOnClickListener {
            findNavController().navigate(R.id.nav_reservations)
        }
    }
}
