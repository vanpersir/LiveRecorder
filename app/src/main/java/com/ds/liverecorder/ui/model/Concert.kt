package com.ds.liverecorder.ui.model

import com.ds.liverecorder.R
import java.io.Serializable
import java.util.Date

data class Concert(
    val id: Long = 0,
    val title: String = "",
    val venue: String = "",
    val date: Date = Date(),
    val notes: String = "",
    val posterResId: Int = R.drawable.ic_launcher_background,
    val posterPath: String? = null,
    val ticketPrice: String = "0.00",
    val ticketPriceCurrency: String = "CNY",
    val actualPaid: String = "0.00",
    val actualPaidCurrency: String = "CNY",
    val otherFees: String = "0.00",
    val otherFeesCurrency: String = "CNY",
    val performers: List<Performer> = listOf(), // 修改为Performer类型列表
    val guests: List<Performer> = listOf(), // 修改为Performer类型列表
    val status: String = "",
    val category: String = "",
    val rating: Int = 0
) : Serializable
