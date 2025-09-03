package com.ds.liverecorder.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ds.liverecorder.R
import com.ds.liverecorder.data.viewmodel.ConcertViewModel
import com.ds.liverecorder.ui.model.Concert
import com.ds.liverecorder.ui.model.Performer
import com.ds.liverecorder.ui.adpter.ConcertAdapter
import kotlinx.coroutines.launch

class ConcertListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ConcertAdapter
    private lateinit var concertViewModel: ConcertViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_concert_list)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        adapter = ConcertAdapter(emptyList())
        recyclerView.adapter = adapter
        
        initViewModel()
    }
    
    private fun initViewModel() {
        // 初始化ViewModel
        val database = com.ds.liverecorder.data.ConcertDatabase.getDatabase(this)
        val concertDao = database.concertDao()
        val performerDao = database.performerDao()
        val repository = com.ds.liverecorder.data.repository.ConcertRepository(concertDao, performerDao)
        val factory = com.ds.liverecorder.data.viewmodel.ConcertViewModelFactory(repository)
        concertViewModel = ViewModelProvider(this, factory)[ConcertViewModel::class.java]
        
        lifecycleScope.launch {
            concertViewModel.getAllConcertsFlow().collect { concerts ->
                // 更新适配器数据
                adapter.updateConcerts(concerts.map { concert ->
                    Concert(
                        id = concert.id,
                        title = concert.title,
                        venue = concert.venue,
                        date = concert.date,
                        notes = concert.notes,
                        posterResId = concert.posterResId,
                        posterPath = concert.posterPath,
                        ticketPrice = concert.ticketPrice,
                        ticketPriceCurrency = concert.ticketPriceCurrency,
                        actualPaid = concert.actualPaid,
                        actualPaidCurrency = concert.actualPaidCurrency,
                        otherFees = concert.otherFees,
                        otherFeesCurrency = concert.otherFeesCurrency,
                        performers = concert.performers.map { name -> Performer(name = name) }, // 修复：将字符串列表转换为Performer对象列表
                        guests = concert.guests.map { name -> Performer(name = name) }, // 添加对guests的处理
                        status = concert.status,
                        category = concert.category,
                        rating = concert.rating
                    )
                })
            }
        }
    }
}