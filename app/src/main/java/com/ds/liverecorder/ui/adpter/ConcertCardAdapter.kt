package com.ds.liverecorder.ui.adpter

import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ds.liverecorder.databinding.ItemConcertCardBinding
import com.ds.liverecorder.ui.ConcertDetailActivity
import com.ds.liverecorder.ui.model.Concert
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class ConcertCardAdapter :
    ListAdapter<Concert, ConcertCardAdapter.ConcertViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConcertViewHolder {
        val binding = ItemConcertCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ConcertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConcertViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ConcertViewHolder(private val binding: ItemConcertCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(concert: Concert) {
            binding.concertTitle.text = concert.title
            
            // 格式化日期
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            binding.concertDate.text = dateFormat.format(concert.date)
            
            binding.concertLocation.text = concert.venue
            
            // 计算剩余天数
            val daysDiff = ((concert.date.time - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
            binding.daysLeft.text = when {
                daysDiff > 0 -> "${abs(daysDiff)}天后"
                daysDiff < 0 -> "${abs(daysDiff)}天前"
                else -> "今天"
            }
            
            // 设置海报图片，优先使用自定义图片路径，否则使用默认资源
            if (concert.posterPath != null) {
                val bitmap = BitmapFactory.decodeFile(concert.posterPath)
                binding.concertPoster.setImageBitmap(bitmap)
            } else {
                binding.concertPoster.setImageResource(concert.posterResId)
            }
            
            // 添加点击事件跳转到详情页面
            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, ConcertDetailActivity::class.java)
                intent.putExtra(ConcertDetailActivity.EXTRA_CONCERT, concert)
                context.startActivity(intent)
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
                       oldItem.posterResId == newItem.posterResId &&
                       oldItem.posterPath == newItem.posterPath
            }
        }
    }
}