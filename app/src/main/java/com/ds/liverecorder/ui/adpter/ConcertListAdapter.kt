package com.ds.liverecorder.ui.adpter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ds.liverecorder.databinding.ItemConcertBinding
import com.ds.liverecorder.ui.model.Concert
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class ConcertListAdapter : ListAdapter<Concert, ConcertListAdapter.ConcertViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConcertViewHolder {
        val binding = ItemConcertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ConcertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConcertViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ConcertViewHolder(private val binding: ItemConcertBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(concert: Concert) {
            binding.tvArtist.text = concert.title
            
            // 格式化日期
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            binding.tvDate.text = dateFormat.format(concert.date)
            
            binding.tvVenue.text = concert.venue
            
            // 计算剩余天数
            val oneDayInMillis = 24 * 60 * 60 * 1000L
            val daysDiff = ((concert.date.time - System.currentTimeMillis()) / oneDayInMillis).toInt()
            val daysText = when {
                daysDiff > 0 -> "${abs(daysDiff)}天后"
                daysDiff < 0 -> "${abs(daysDiff)}天前"
                else -> "今天"
            }
            
            // 显示剩余天数（添加到日期文本中）
            binding.tvDate.text = "${binding.tvDate.text} ($daysText)"
            
            // 根据是否已过期设置不同颜色
            if (daysDiff < 0) {
                binding.tvArtist.setTextColor(binding.root.context.getColor(android.R.color.darker_gray))
                binding.tvDate.setTextColor(binding.root.context.getColor(android.R.color.darker_gray))
                binding.tvVenue.setTextColor(binding.root.context.getColor(android.R.color.darker_gray))
            } else {
                binding.tvArtist.setTextColor(binding.root.context.getColor(android.R.color.black))
                binding.tvDate.setTextColor(binding.root.context.getColor(android.R.color.black))
                binding.tvVenue.setTextColor(binding.root.context.getColor(android.R.color.black))
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Concert>() {
            override fun areItemsTheSame(oldItem: Concert, newItem: Concert): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Concert, newItem: Concert): Boolean {
                return oldItem.id == newItem.id && 
                       oldItem.title == newItem.title && 
                       oldItem.venue == newItem.venue && 
                       oldItem.date == newItem.date &&
                       oldItem.posterResId == newItem.posterResId
            }
        }
    }
}