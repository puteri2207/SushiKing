package com.example.sushiking

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "customer",
    val smilePoints: Int = 0,
    val stamps: Int = 0
)

data class Staff(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val name: String = "",
    val email: String = "",
    val staffId: String = "",
    val phone: String = "",
    val role: String = "Cashier"
)

data class OrderItem(
    val menuItemId: String = "",
    val menuItemName: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1
)

data class Reservation(
    val reservationId: String = "",
    val userId: String = "",
    val userName: String = "",
    val date: String = "",
    val time: String = "",
    val pax: Int = 2,
    val status: String = "pending",
    val depositPaid: Boolean = false,
    val depositAmount: Double = 20.0,
    val orderItems: List<OrderItem> = emptyList(),
    val orderTotal: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paymentMethod: String = "",
    val notes: String = ""
)

data class MenuItem(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val description: String = "",
    val ingredients: String = "",
    val calories: Int = 0,
    val price: Double = 0.0,
    val imageUrl: String = ""
)
