package com.example.sushiking

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "Guest"
                val email = doc.getString("email") ?: ""
                view.findViewById<TextView>(R.id.tvProfileName).text = name
                view.findViewById<TextView>(R.id.tvProfileEmail).text = email
                view.findViewById<TextView>(R.id.tvAvatarInitial).text =
                    name.firstOrNull()?.uppercase() ?: "?"
            }

        view.findViewById<CardView>(R.id.btnEditProfile).setOnClickListener {
            showEditProfileDialog(uid)
        }

        view.findViewById<CardView>(R.id.btnMyReservations).setOnClickListener {
            startActivity(Intent(requireContext(), CustomerReservationActivity::class.java))
        }

        view.findViewById<CardView>(R.id.btnTransactionHistory).setOnClickListener {
            startActivity(Intent(requireContext(), TransactionHistoryActivity::class.java))
        }

        view.findViewById<CardView>(R.id.btnFaq).setOnClickListener {
            showFaqDialog()
        }

        view.findViewById<CardView>(R.id.btnStoreLocator).setOnClickListener {
            startActivity(Intent(requireContext(), StoreLocatorActivity::class.java))
        }

        // Website
        view.findViewById<CardView>(R.id.btnWebsite).setOnClickListener {
            openUrl("https://www.sushi-king.com")
        }

        // Instagram
        view.findViewById<CardView>(R.id.btnInstagram).setOnClickListener {
            openUrl("https://www.instagram.com/sushikingmalaysia")
        }

        // Facebook
        view.findViewById<CardView>(R.id.btnFacebook).setOnClickListener {
            openUrl("https://www.facebook.com/SushiKingMalaysia")
        }

        // TikTok
        view.findViewById<CardView>(R.id.btnTiktok).setOnClickListener {
            openUrl("https://www.tiktok.com/@sushikingmalaysia")
        }

        view.findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    auth.signOut()
                    startActivity(Intent(requireContext(), LoginChoice::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun showEditProfileDialog(uid: String) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.edit_profile, null)

        val etName = dialogView.findViewById<TextInputEditText>(R.id.etName)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.etEmail)
        val etCurrentPassword = dialogView.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNewPassword = dialogView.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnSave = dialogView.findViewById<MaterialButton>(R.id.btnSave)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnCancel)

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                etName.setText(doc.getString("name") ?: "")
                etEmail.setText(doc.getString("email") ?: "")
            }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()
            val newEmail = etEmail.text.toString().trim()
            val currentPass = etCurrentPassword.text.toString().trim()
            val newPass = etNewPassword.text.toString().trim()
            val confirmPass = etConfirmPassword.text.toString().trim()

            if (newName.isEmpty()) { showSnackbar("Name cannot be empty!"); return@setOnClickListener }

            db.collection("users").document(uid)
                .update(mapOf("name" to newName, "email" to newEmail))
                .addOnSuccessListener {
                    showSnackbar("Profile updated! ✅")
                    view?.findViewById<TextView>(R.id.tvProfileName)?.text = newName
                    view?.findViewById<TextView>(R.id.tvProfileEmail)?.text = newEmail
                    view?.findViewById<TextView>(R.id.tvAvatarInitial)?.text =
                        newName.firstOrNull()?.uppercase() ?: "?"
                    dialog.dismiss()
                }

            if (currentPass.isNotEmpty() && newPass.isNotEmpty()) {
                if (newPass != confirmPass) { showSnackbar("Passwords don't match!"); return@setOnClickListener }
                if (newPass.length < 6) { showSnackbar("Password min 6 characters!"); return@setOnClickListener }
                val user = auth.currentUser ?: return@setOnClickListener
                val credential = EmailAuthProvider.getCredential(user.email ?: "", currentPass)
                user.reauthenticate(credential)
                    .addOnSuccessListener {
                        user.updatePassword(newPass)
                            .addOnSuccessListener { showSnackbar("Password changed! ✅") }
                            .addOnFailureListener { showSnackbar("Failed to change password") }
                    }
                    .addOnFailureListener { showSnackbar("Current password is incorrect!") }
            }
        }
        dialog.show()
    }

    private fun showFaqDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("❓ FAQ")
            .setMessage(
                "Q: How do I earn stamps?\n" +
                        "A: Show your QR code to cashier when you dine.\n\n" +
                        "Q: How many stamps for a reward?\n" +
                        "A: Collect 8 stamps = FREE Bento! 🍱\n\n" +
                        "Q: Can I use QR at any outlet?\n" +
                        "A: Yes! Any Sushi King outlet in Malaysia.\n\n" +
                        "Q: How do I make a reservation?\n" +
                        "A: Tap the Reserve tab at the bottom.\n\n" +
                        "Q: Can I cancel my reservation?\n" +
                        "A: Please contact the nearest outlet directly."
            )
            .setPositiveButton("Got it!", null)
            .show()
    }

    private fun showSnackbar(msg: String) {
        Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG).show()
    }
}
