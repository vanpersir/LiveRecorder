package com.ds.liverecorder.ui.adpter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ds.liverecorder.R
import com.ds.liverecorder.ui.model.Concert
import java.text.SimpleDateFormat
import java.util.Locale

class ConcertAdapter(private var concerts: List<Concert>) :
    RecyclerView.Adapter<ConcertAdapter.ConcertViewHolder>() {

    class ConcertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPoster: ImageView = itemView.findViewById(R.id.ivPoster)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvVenue: TextView = itemView.findViewById(R.id.tvVenue)
        val tvDaysLeft: TextView = itemView.findViewById(R.id.tvDaysLeft)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConcertViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.concert_item, parent, false)
        return ConcertViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ConcertViewHolder, position: Int) {
        val concert = concerts[position]
        holder.ivPoster.setImageResource(concert.posterResId)
        holder.tvTitle.text = concert.title
        holder.tvVenue.text = concert.venue

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        holder.tvDate.text = dateFormat.format(concert.date)

        val daysLeft = (concert.date.time - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)
        holder.tvDaysLeft.text = "$daysLeft 天后"
    }

    override fun getItemCount(): Int {
        return concerts.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateConcerts(newConcerts: List<Concert>) {
        concerts = newConcerts
        notifyDataSetChanged()
    }
}