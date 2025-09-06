package com.ds.liverecorder.domain.model

import java.util.Date

/**
 * 演出领域模型
 */
data class Concert(
    val id: Long = 0,
    val title: String,
    val venue: String,
    val date: Date,
    val notes: String,
    val posterResId: Int,
    val posterPath: String,
    val ticketPrice: String,
    val ticketPriceCurrency: String,
    val actualPaid: String,
    val actualPaidCurrency: String,
    val otherFees: String,
    val otherFeesCurrency: String,
    val performers: List<String>,
    val guests: List<String>,
    val status: String,
    val category: String,
    val rating: Int
)