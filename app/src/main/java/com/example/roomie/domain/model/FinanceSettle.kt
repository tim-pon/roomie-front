package com.example.roomie.domain.model

data class FinanceSettle(
    val id: Int,
    val giverId: Int,
    val giverName: String,
    val receiverId: Int,
    val receiverName: String,
    val amount: Double
)
