package com.ds.liverecorder.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ds.liverecorder.R
import java.util.Date

@Entity(tableName = "concerts")
data class ConcertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String = "",

    @ColumnInfo(name = "venue")
    val venue: String = "",

    @ColumnInfo(name = "date")
    val date: Date = Date(),

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "poster_res_id")
    val posterResId: Int = R.drawable.ic_launcher_background,
    
    @ColumnInfo(name = "poster_path")
    val posterPath: String? = null,

    @ColumnInfo(name = "ticket_price")
    val ticketPrice: String = "0.00",
    
    @ColumnInfo(name = "ticket_price_currency")
    val ticketPriceCurrency: String = "CNY",

    @ColumnInfo(name = "actual_paid")
    val actualPaid: String = "0.00",
    
    @ColumnInfo(name = "actual_paid_currency")
    val actualPaidCurrency: String = "CNY",

    @ColumnInfo(name = "other_fees")
    val otherFees: String = "0.00",
    
    @ColumnInfo(name = "other_fees_currency")
    val otherFeesCurrency: String = "CNY",

    @ColumnInfo(name = "performers")
    val performers: List<String> = listOf(), // 保留字符串列表用于存储表演者名称
    
    @ColumnInfo(name = "guests")
    val guests: List<String> = listOf(), // 保留字符串列表用于存储嘉宾名称
    
    @ColumnInfo(name = "status")
    val status: String = "", // 演出状态
    
    @ColumnInfo(name = "category")
    val category: String = "", // 演出类型
    
    @ColumnInfo(name = "rating")
    val rating: Int = 0 // 演出评价（1-5星）
)