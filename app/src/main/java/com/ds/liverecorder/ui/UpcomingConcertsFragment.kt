package com.ds.liverecorder.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ds.liverecorder.data.ConcertDatabase
import com.ds.liverecorder.data.entity.ConcertEntity
import com.ds.liverecorder.data.repository.ConcertRepository
import com.ds.liverecorder.data.viewmodel.ConcertViewModel
import com.ds.liverecorder.databinding.FragmentRecordBinding
import com.ds.liverecorder.ui.adpter.ConcertCardAdapter
import com.ds.liverecorder.ui.adpter.ConcertTimelineAdapter
import com.ds.liverecorder.ui.adpter.ConcertPosterAdapter
import com.ds.liverecorder.ui.utils.GridSpacingItemDecoration
import com.ds.liverecorder.ui.model.Concert
import com.ds.liverecorder.ui.model.Performer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class UpcomingConcertsFragment : Fragment() {
    private var _binding: FragmentRecordBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private var currentDisplayMode = "card"
    private lateinit var concertViewModel: ConcertViewModel
    private lateinit var cardAdapter: ConcertCardAdapter
    private lateinit var timelineAdapter: ConcertTimelineAdapter
    private lateinit var posterAdapter: ConcertPosterAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = binding.recyclerView
        
        // 初始化ViewModel
        val database = ConcertDatabase.getDatabase(requireContext())
        val concertDao = database.concertDao()
        val performerDao = database.performerDao()
        val repository = ConcertRepository(concertDao, performerDao)
        val factory = com.ds.liverecorder.data.viewmodel.ConcertViewModelFactory(repository)
        concertViewModel = ViewModelProvider(this, factory)[ConcertViewModel::class.java]
        
        // 初始化适配器
        cardAdapter = ConcertCardAdapter()
        timelineAdapter = ConcertTimelineAdapter()
        posterAdapter = ConcertPosterAdapter()
        
        // 先显示空列表
        updateAdapters(emptyList())
        setupRecyclerView(currentDisplayMode)
        
        // 观察演出数据变化，只显示状态为"待看"的演出
        viewLifecycleOwner.lifecycleScope.launch {
            concertViewModel.getAllConcertsFlow().collect { concertEntities ->
                // 筛选出状态为"待看"的演出
                val upcomingConcerts = concertEntities.filter { entity ->
                    entity.status == "待看"
                }
                
                // 将ConcertEntity列表转换为Concert列表
                val concerts = upcomingConcerts.map { entity ->
                    Concert(
                        id = entity.id,
                        title = entity.title,
                        venue = entity.venue,
                        date = entity.date,
                        notes = entity.notes,
                        posterResId = entity.posterResId,
                        posterPath = entity.posterPath,
                        ticketPrice = entity.ticketPrice,
                        ticketPriceCurrency = entity.ticketPriceCurrency,
                        actualPaid = entity.actualPaid,
                        actualPaidCurrency = entity.actualPaidCurrency,
                        otherFees = entity.otherFees,
                        otherFeesCurrency = entity.otherFeesCurrency,
                        performers = entity.performers.map { name -> 
                            Performer(name = name) 
                        },
                        guests = entity.guests.map { name -> 
                            Performer(name = name) 
                        },
                        status = entity.status,
                        category = entity.category,
                        rating = entity.rating
                    )
                }
                
                // 更新适配器
                updateAdapters(concerts)
                // 更新空状态视图
                updateEmptyView(concerts.isEmpty())
            }
        }
        
        // 只在Fragment创建时更新一次演出状态，避免持续监视
        updateExpiredConcertsOnce()
    }


    private fun updateEmptyView(isEmpty: Boolean) {
        if (isEmpty) {
            binding.emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    /**
     * 仅在Fragment创建时更新一次过期演出状态
     */
    private fun updateExpiredConcertsOnce() {
        concertViewModel.updateExpiredConcertsStatus()
    }

    private fun updateAdapters(concerts: List<Concert>) {
        cardAdapter.submitList(concerts)
        timelineAdapter.submitList(concerts)
        posterAdapter.submitList(concerts)
        
        if (recyclerView.adapter == null) {
            setupRecyclerView(currentDisplayMode)
        }
    }

    fun switchDisplayMode(mode: String) {
        currentDisplayMode = mode
        // 只有在Fragment附加到Activity时才更新UI
        if (isAdded && context != null) {
            setupRecyclerView(mode)
        }
    }

    private fun setupRecyclerView(displayMode: String) {
        // 确保Fragment已附加到上下文
        if (!isAdded || context == null) return
        
        when (displayMode) {
            "card" -> {
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = cardAdapter
                if (recyclerView.itemDecorationCount > 0) {
                    recyclerView.removeItemDecorationAt(0)
                }
            }
            "timeline" -> {
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = timelineAdapter
                if (recyclerView.itemDecorationCount > 0) {
                    recyclerView.removeItemDecorationAt(0)
                }
            }
            "poster" -> {
                val gridLayoutManager = GridLayoutManager(requireContext(), 2)
                recyclerView.layoutManager = gridLayoutManager
                recyclerView.adapter = posterAdapter
                
                if (recyclerView.itemDecorationCount > 0) {
                    recyclerView.removeItemDecorationAt(0)
                }
                
                recyclerView.addItemDecoration(
                    GridSpacingItemDecoration(
                        2,
                        50,
                        true
                    )
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}