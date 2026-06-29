package com.example.sushiking

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ReservationFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var tvDate: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvPax: TextView
    private lateinit var tvOrderSummary: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var tvOutlet: TextView
    private var pax = 2
    private val orderItems = mutableListOf<OrderItem>()
    private val PRICE_PER_HEAD = 5.0
    private var selectedOutlet = ""

    private val outlets = listOf(
        "Sushi King Mid Valley" to "Mid Valley Megamall, KL",
        "Sushi King Sunway Pyramid" to "Sunway Pyramid, PJ",
        "Sushi King KLCC" to "Suria KLCC, KL",
        "Sushi King IOI City Mall" to "IOI City Mall, Putrajaya",
        "Sushi King 1 Utama" to "1 Utama, PJ",
        "Sushi King Pavilion KL" to "Pavilion KL",
        "Sushi King The Curve" to "The Curve, PJ",
        "Sushi King Paradigm Mall" to "Paradigm Mall, PJ",
        "Sushi King Setia City Mall" to "Setia City Mall, Shah Alam",
        "Sushi King MyTown" to "MyTown Shopping Centre, KL"
    )

    private val preOrderLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            @Suppress("UNCHECKED_CAST")
            val ordersRaw = data?.getSerializableExtra("orderItems") as? ArrayList<HashMap<String, Any>>
            orderItems.clear()
            ordersRaw?.forEach { map ->
                orderItems.add(OrderItem(
                    menuItemId = map["menuItemId"] as? String ?: "",
                    menuItemName = map["menuItemName"] as? String ?: "",
                    price = (map["price"] as? Double) ?: 0.0,
                    quantity = ((map["quantity"] as? Int) ?: 1)
                ))
            }
            updateOrderSummary()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_reservation, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvDate = view.findViewById(R.id.tvDate)
        tvTime = view.findViewById(R.id.tvTime)
        tvPax = view.findViewById(R.id.tvPax)
        tvOrderSummary = view.findViewById(R.id.tvOrderSummary)
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount)
        tvOutlet = view.findViewById(R.id.tvOutlet)

        tvDate.setOnClickListener { showDatePicker() }
        tvTime.setOnClickListener { showTimePicker() }

        view.findViewById<MaterialButton>(R.id.btnSelectOutlet).setOnClickListener { showOutletDialog() }
        view.findViewById<MaterialButton>(R.id.btnDecrease).setOnClickListener {
            if (pax > 1) { pax--; tvPax.text = pax.toString(); updateOrderSummary() }
        }
        view.findViewById<MaterialButton>(R.id.btnIncrease).setOnClickListener {
            if (pax < 20) { pax++; tvPax.text = pax.toString(); updateOrderSummary() }
        }
        view.findViewById<MaterialButton>(R.id.btnAddFood).setOnClickListener {
            preOrderLauncher.launch(Intent(requireContext(), PreOrderActivity::class.java))
        }
        view.findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener {
            tvDate.text = "Tap to select date"
            tvTime.text = "Tap to select time"
            tvOutlet.text = "Tap to select outlet"
            selectedOutlet = ""; pax = 2; tvPax.text = "2"
            orderItems.clear(); updateOrderSummary()
        }
        view.findViewById<MaterialButton>(R.id.btnDone).setOnClickListener {
            if (selectedOutlet.isEmpty()) { Snackbar.make(view, "Please select an outlet", Snackbar.LENGTH_SHORT).show(); return@setOnClickListener }
            if (tvDate.text == "Tap to select date" || tvTime.text == "Tap to select time") { Snackbar.make(view, "Please select date and time", Snackbar.LENGTH_SHORT).show(); return@setOnClickListener }
            showOrderConfirmation(tvDate.text.toString(), tvTime.text.toString(), pax)
        }
        updateOrderSummary()
    }

    private fun showOutletDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_select_outlet)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val container = dialog.findViewById<LinearLayout>(R.id.llOutlets)
        outlets.forEachIndexed { index, outlet ->
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_select_outlet, container, false)
            itemView.findViewById<TextView>(R.id.tvOutletName).text = outlet.first
            itemView.findViewById<TextView>(R.id.tvOutletAddress).text = "📍 ${outlet.second}"
            itemView.setOnClickListener {
                selectedOutlet = outlet.first
                tvOutlet.text = "📍 ${outlet.first}"
                dialog.dismiss()
            }
            container.addView(itemView)
            // Add divider except last
            if (index < outlets.size - 1) {
                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                    setBackgroundColor(Color.parseColor("#F3F4F6"))
                }
                container.addView(divider)
            }
        }

        dialog.findViewById<MaterialButton>(R.id.btnCancelOutlet).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        val minCal = Calendar.getInstance()
        minCal.add(Calendar.DAY_OF_MONTH, 1)
        val picker = DatePickerDialog(requireContext(),
            { _, y, m, d -> tvDate.text = "%02d/%02d/%d".format(d, m + 1, y) },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        picker.datePicker.minDate = minCal.timeInMillis
        picker.show()
    }

    private fun showTimePicker() {
        val cal = Calendar.getInstance()
        TimePickerDialog(requireContext(), { _, h, min ->
            val amPm = if (h < 12) "AM" else "PM"
            val hour = if (h > 12) h - 12 else if (h == 0) 12 else h
            tvTime.text = "%d:%02d %s".format(hour, min, amPm)
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
    }

    private fun updateOrderSummary() {
        val depositAmount = pax * PRICE_PER_HEAD
        val foodTotal = orderItems.sumOf { it.price * it.quantity }
        val total = depositAmount + foodTotal
        tvOrderSummary.text = if (orderItems.isEmpty()) "No food pre-ordered"
        else orderItems.joinToString("\n") { "• ${it.menuItemName} x${it.quantity} = RM%.2f".format(it.price * it.quantity) }
        tvTotalAmount.text = "Deposit: RM%.2f ($pax pax × RM%.2f)\nFood: RM%.2f\nTotal: RM%.2f".format(depositAmount, PRICE_PER_HEAD, foodTotal, total)
    }

    private fun showOrderConfirmation(date: String, time: String, pax: Int) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_order_confirm)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val depositAmount = pax * PRICE_PER_HEAD
        val foodTotal = orderItems.sumOf { it.price * it.quantity }
        val total = depositAmount + foodTotal

        dialog.findViewById<TextView>(R.id.tvConfirmOutlet).text = selectedOutlet
        dialog.findViewById<TextView>(R.id.tvConfirmDate).text = date
        dialog.findViewById<TextView>(R.id.tvConfirmTime).text = time
        dialog.findViewById<TextView>(R.id.tvConfirmPax).text = "$pax pax"
        dialog.findViewById<TextView>(R.id.tvConfirmDeposit).text = "RM%.2f".format(depositAmount)
        dialog.findViewById<TextView>(R.id.tvConfirmFood).text = "RM%.2f".format(foodTotal)
        dialog.findViewById<TextView>(R.id.tvConfirmTotal).text = "RM%.2f".format(total)

        val llFoodItems = dialog.findViewById<LinearLayout>(R.id.llFoodItems)
        llFoodItems.removeAllViews()
        if (orderItems.isEmpty()) {
            val tv = TextView(requireContext()).apply {
                text = "No food pre-ordered"
                textSize = 13f
                setTextColor(Color.parseColor("#9CA3AF"))
            }
            llFoodItems.addView(tv)
        } else {
            orderItems.forEach { item ->
                val tv = TextView(requireContext()).apply {
                    text = "• ${item.menuItemName} x${item.quantity}  —  RM%.2f".format(item.price * item.quantity)
                    textSize = 13f
                    setTextColor(Color.parseColor("#1F2937"))
                    setPadding(0, 4, 0, 4)
                }
                llFoodItems.addView(tv)
            }
        }

        dialog.findViewById<MaterialButton>(R.id.btnBack).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<MaterialButton>(R.id.btnProceedPay).setOnClickListener {
            dialog.dismiss()
            showPaymentDialog(date, time, pax)
        }
        dialog.show()
    }

    private fun showPaymentDialog(date: String, time: String, pax: Int) {
        val depositAmount = pax * PRICE_PER_HEAD
        val foodTotal = orderItems.sumOf { it.price * it.quantity }
        val total = depositAmount + foodTotal

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_payment, null)
        dialogView.findViewById<TextView>(R.id.tvPaymentDate).text = date
        dialogView.findViewById<TextView>(R.id.tvPaymentTime).text = time
        dialogView.findViewById<TextView>(R.id.tvPaymentPax).text = "$pax pax"
        dialogView.findViewById<TextView>(R.id.tvPaymentDeposit).text = "RM%.2f ($pax × RM5)".format(depositAmount)
        dialogView.findViewById<TextView>(R.id.tvPaymentFood).text = "RM%.2f".format(foodTotal)
        dialogView.findViewById<TextView>(R.id.tvPaymentTotal).text = "RM%.2f".format(total)

        val etCardNumber = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etCardNumber)
        etCardNumber.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true
                val digits = s.toString().replace(" ", "")
                val formatted = StringBuilder()
                for (i in digits.indices) {
                    if (i > 0 && i % 4 == 0) formatted.append(" ")
                    formatted.append(digits[i])
                }
                etCardNumber.setText(formatted)
                etCardNumber.setSelection(formatted.length)
                isFormatting = false
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val etExpiry = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etExpiry)
        etExpiry.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true
                val digits = s.toString().replace("/", "")
                val formatted = if (digits.length >= 3) "${digits.substring(0, 2)}/${digits.substring(2)}" else digits
                etExpiry.setText(formatted)
                etExpiry.setSelection(formatted.length)
                isFormatting = false
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<MaterialButton>(R.id.btnPayNow).setOnClickListener {
            val cardNumber = etCardNumber.text.toString().replace(" ", "")
            val expiry = etExpiry.text.toString().trim()
            val cvv = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etCvv).text.toString().trim()
            val cardName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etCardName).text.toString().trim()
            if (cardNumber.length < 16) { Snackbar.make(requireView(), "Enter valid 16-digit card number", Snackbar.LENGTH_SHORT).show(); return@setOnClickListener }
            if (expiry.length < 5) { Snackbar.make(requireView(), "Enter valid expiry MM/YY", Snackbar.LENGTH_SHORT).show(); return@setOnClickListener }
            if (cvv.length < 3) { Snackbar.make(requireView(), "Enter valid 3-digit CVV", Snackbar.LENGTH_SHORT).show(); return@setOnClickListener }
            if (cardName.isEmpty()) { Snackbar.make(requireView(), "Enter cardholder name", Snackbar.LENGTH_SHORT).show(); return@setOnClickListener }
            dialog.dismiss()
            saveReservation(date, time, pax, depositAmount, foodTotal, total)
        }
        dialogView.findViewById<MaterialButton>(R.id.btnCancelPayment).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun saveReservation(date: String, time: String, pax: Int, depositAmount: Double, foodTotal: Double, totalAmount: Double) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val userName = doc.getString("name") ?: "Guest"
                val reservationId = UUID.randomUUID().toString()
                val reservation = hashMapOf(
                    "reservationId" to reservationId,
                    "userId" to uid,
                    "userName" to userName,
                    "outlet" to selectedOutlet,
                    "date" to date,
                    "time" to time,
                    "pax" to pax,
                    "status" to "pending",
                    "depositPaid" to true,
                    "depositAmount" to depositAmount,
                    "pricePerHead" to PRICE_PER_HEAD,
                    "orderItems" to orderItems.map { mapOf("menuItemId" to it.menuItemId, "menuItemName" to it.menuItemName, "price" to it.price, "quantity" to it.quantity) },
                    "orderTotal" to foodTotal,
                    "totalAmount" to totalAmount,
                    "paymentMethod" to "Card"
                )
                db.collection("reservations").document(reservationId).set(reservation)
                    .addOnSuccessListener {
                        showSuccessDialog(date, time, pax, reservationId)
                        orderItems.clear()
                        tvDate.text = "Tap to select date"; tvTime.text = "Tap to select time"
                        tvOutlet.text = "Tap to select outlet"; selectedOutlet = ""
                        this.pax = 2; tvPax.text = "2"; updateOrderSummary()
                    }
                    .addOnFailureListener { Snackbar.make(requireView(), "Failed to save!", Snackbar.LENGTH_LONG).show() }
            }
    }

    private fun showSuccessDialog(date: String, time: String, pax: Int, reservationId: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_reservation_confirm, null)
        dialogView.findViewById<View>(R.id.layoutConfirm).visibility = View.GONE
        dialogView.findViewById<View>(R.id.layoutSuccess).visibility = View.VISIBLE
        dialogView.findViewById<TextView>(R.id.tvSuccessId).text = reservationId.take(8).uppercase()
        dialogView.findViewById<TextView>(R.id.tvSuccessDate).text = date
        dialogView.findViewById<TextView>(R.id.tvSuccessTime).text = time
        dialogView.findViewById<TextView>(R.id.tvSuccessPax).text = "$pax pax"
        val dialog = MaterialAlertDialogBuilder(requireContext()).setView(dialogView).setCancelable(false).create()
        dialogView.findViewById<MaterialButton>(R.id.btnSuccessOk).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
