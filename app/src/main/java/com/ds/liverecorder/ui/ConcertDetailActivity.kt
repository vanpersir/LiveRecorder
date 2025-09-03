package com.ds.liverecorder.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ds.liverecorder.R
import com.ds.liverecorder.data.ConcertDatabase
import com.ds.liverecorder.data.entity.ConcertEntity
import com.ds.liverecorder.data.repository.ConcertRepository
import com.ds.liverecorder.data.viewmodel.ConcertViewModel
import com.ds.liverecorder.ui.model.Concert
import com.ds.liverecorder.ui.model.Performer
import com.google.android.flexbox.FlexboxLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ConcertDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CONCERT = "extra_concert"
        const val REQUEST_EDIT_CONCERT = 1001
    }

    private lateinit var ivPoster: ImageView
    private lateinit var tvConcertTitle: TextView // 用于显示演出标题
    private lateinit var ratingBar: RatingBar
    private lateinit var tvTicketPriceInfo: TextView
    private lateinit var tvActualPaidInfo: TextView
    private lateinit var tvOtherFeesInfo: TextView
    private lateinit var tvVenue: TextView  // 用于显示地点
    private lateinit var tvDate: TextView   // 用于显示日期
    private lateinit var llPerformersContainer: FlexboxLayout
    private lateinit var llGuestsContainer: FlexboxLayout // 添加嘉宾容器引用
    private lateinit var concertViewModel: ConcertViewModel
    private lateinit var concert: Concert
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_concert_detail)

        // 确保ActionBar可见并启用home按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "演出详情"

        @Suppress("DEPRECATION")
        val concertFromIntent = intent.extras?.getSerializable(EXTRA_CONCERT) as? Concert ?: Concert()
        
        // 初始化ViewModel
        val concertDao = ConcertDatabase.getDatabase(this).concertDao()
        val performerDao = ConcertDatabase.getDatabase(this).performerDao()
        val repository = ConcertRepository(concertDao, performerDao)
        val factory = com.ds.liverecorder.data.viewmodel.ConcertViewModelFactory(repository)
        concertViewModel = ViewModelProvider(this, factory)[ConcertViewModel::class.java]

        initViews()
        
        // 从数据库获取最新数据
        lifecycleScope.launch {
            concertViewModel.getAllConcertsFlow().collect { concertEntities ->
                val concertEntity = concertEntities.find { it.id == concertFromIntent.id }
                if (concertEntity != null) {
                    concert = Concert(
                        id = concertEntity.id,
                        title = concertEntity.title,
                        venue = concertEntity.venue,
                        date = concertEntity.date,
                        notes = concertEntity.notes,
                        posterResId = concertEntity.posterResId,
                        posterPath = concertEntity.posterPath,
                        ticketPrice = concertEntity.ticketPrice,
                        ticketPriceCurrency = concertEntity.ticketPriceCurrency,
                        actualPaid = concertEntity.actualPaid,
                        actualPaidCurrency = concertEntity.actualPaidCurrency,
                        otherFees = concertEntity.otherFees,
                        otherFeesCurrency = concertEntity.otherFeesCurrency,
                        performers = concertEntity.performers.map { name -> 
                            Performer(name = name) 
                        },
                        guests = concertEntity.guests.map { name -> 
                            Performer(name = name) 
                        },
                        status = concertEntity.status,
                        category = concertEntity.category,
                        rating = concertEntity.rating
                    )
                    populateViews(concert)
                } else {
                    // 如果在数据库中找不到，则使用intent传递的数据
                    concert = concertFromIntent
                    populateViews(concert)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.concert_options_menu, menu)
        return true
    }

    private fun showEditDialog() {
        val editDialog = AddConcertDialogFragment()
        val bundle = Bundle()
        // 转换Performer列表为字符串列表
        val concertForEdit = Concert(
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
            performers = concert.performers, // 保持为Performer对象列表
            guests = concert.guests, // 保持为Performer对象列表
            status = concert.status,
            category = concert.category,
            rating = concert.rating
        )
        bundle.putSerializable("concert", concertForEdit)
        editDialog.arguments = bundle
        editDialog.show(supportFragmentManager, "AddConcertDialog")
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            R.id.action_edit -> {
                showEditDialog()
                true
            }
            R.id.action_delete -> {
                // 显示确认对话框
                AlertDialog.Builder(this)
                    .setTitle("删除演出")
                    .setMessage("确定要删除这场演出吗？此操作无法撤销。")
                    .setPositiveButton("确定") { _, _ ->
                        // 删除演出
                        concertViewModel.deleteConcertById(concert.id)
                        Toast.makeText(this, "演出已删除", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .setNegativeButton("取消", null)
                    .show()
                true
            }
            R.id.action_quick_rate -> {
                // 快速评价功能
                showQuickRatingDialog()
                true
            }
            R.id.action_change_status -> {
                // 修改状态功能
                showChangeStatusDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initViews() {
        // 初始化海报ImageView
        ivPoster = findViewById<ImageView>(R.id.ivPoster)
    
        // 初始化票价相关视图
        tvTicketPriceInfo = findViewById<TextView>(R.id.tvTicketPriceInfo)
        tvActualPaidInfo = findViewById<TextView>(R.id.tvActualPaidInfo)
        tvOtherFeesInfo = findViewById<TextView>(R.id.tvOtherFeesInfo)
        
        // 初始化评分视图
        ratingBar = findViewById<RatingBar>(R.id.ratingBar)
    
        // 初始化地点和日期TextView
        tvVenue = findViewById<TextView>(R.id.tvLocation)
        tvDate = findViewById<TextView>(R.id.tvDate)
    
        // 初始化演出者容器
        llPerformersContainer = findViewById<FlexboxLayout>(R.id.llPerformersContainer)

        // 初始化嘉宾容器
        llGuestsContainer = findViewById<FlexboxLayout>(R.id.llGuestsContainer)

        // 新增：初始化演出标题TextView
        tvConcertTitle = findViewById<TextView>(R.id.tvConcertTitle)
    }

    @SuppressLint("SetTextI18n")
    private fun populateViews(concert: Concert) {
        // 动态设置标题为concert.title
        title = concert.title.ifEmpty { "演出详情" }

        // 设置海报
        if (concert.posterPath != null) {
            val bitmap = BitmapFactory.decodeFile(concert.posterPath)
            ivPoster.setImageBitmap(bitmap)
        } else {
            ivPoster.setImageResource(concert.posterResId)
        }
        
        // 设置评分
        ratingBar.rating = concert.rating.toFloat()
        
        // 设置日期和地点信息（按照规范，日期和地点应该各自独立显示为一行）
        tvDate.text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(concert.date)
        tvVenue.text = concert.venue
        
        // 设置票价、实付和其他费用信息
        tvTicketPriceInfo.text = "${getString(R.string.ticket_price)}: ${concert.ticketPrice} ${concert.ticketPriceCurrency}"
        tvActualPaidInfo.text = "${getString(R.string.actual_paid)}: ${concert.actualPaid} ${concert.actualPaidCurrency}"
        tvOtherFeesInfo.text = "${getString(R.string.other_fees)}: ${concert.otherFees} ${concert.otherFeesCurrency}"

        // 更新演出者信息
        updatePerformersDisplay(concert.performers)

        // 更新嘉宾信息
        updateGuestsDisplay(concert.guests)
        
        // 设置演出标题
        tvConcertTitle.text = concert.title
    }

    private fun setupClickListeners() {
    }

    private fun showOptionsMenu() {
        val popupMenu = PopupMenu(this, findViewById(android.R.id.content))
        popupMenu.menuInflater.inflate(R.menu.concert_options_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    // 跳转到编辑页面
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra(EXTRA_CONCERT, concert)
                    startActivity(intent)
                    true
                }
                R.id.action_delete -> {
                    // 显示确认对话框
                    AlertDialog.Builder(this)
                        .setTitle("删除演出")
                        .setMessage("确定要删除这场演出吗？此操作无法撤销。")
                        .setPositiveButton("确定") { _, _ ->
                            // 删除演出
                            concertViewModel.deleteConcertById(concert.id)
                            Toast.makeText(this, "演出已删除", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .setNegativeButton("取消", null)
                        .show()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun showQuickRatingDialog() {
        // 创建一个AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("快速评价")
        
        // 创建RatingBar控件
        val ratingBar = android.widget.RatingBar(this).apply {
            numStars = 5
            stepSize = 1.0f
            rating = concert.rating.toFloat()
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        
        // 创建布局容器
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }
        
        // 添加说明文本
        val textView = android.widget.TextView(this).apply {
            text = "请为演出评分："
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16f)
            setPadding(0, 0, 0, 16)
        }
        
        layout.addView(textView)
        layout.addView(ratingBar)
        
        builder.setView(layout)
        
        // 设置确定按钮
        builder.setPositiveButton("确定") { _, _ ->
            val selectedRating = ratingBar.rating.toInt()
            
            // 如果评分没有变化，则不执行任何操作
            if (selectedRating == concert.rating) {
                Toast.makeText(this, "评分未改变", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            
            // 创建新的演出对象（因为Concert是不可变的data class）
            val updatedConcert = Concert(
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
                performers = concert.performers,
                guests = concert.guests,
                status = concert.status,
                category = concert.category,
                rating = selectedRating
            )
            
            // 更新数据库
            concertViewModel.updateConcert(
                ConcertEntity(
                    id = updatedConcert.id,
                    title = updatedConcert.title,
                    venue = updatedConcert.venue,
                    date = updatedConcert.date,
                    notes = updatedConcert.notes,
                    posterResId = updatedConcert.posterResId,
                    posterPath = updatedConcert.posterPath,
                    ticketPrice = updatedConcert.ticketPrice,
                    ticketPriceCurrency = updatedConcert.ticketPriceCurrency,
                    actualPaid = updatedConcert.actualPaid,
                    actualPaidCurrency = updatedConcert.actualPaidCurrency,
                    otherFees = updatedConcert.otherFees,
                    otherFeesCurrency = updatedConcert.otherFeesCurrency,
                    performers = updatedConcert.performers.map { it.name }, // 转换为字符串列表
                    guests = updatedConcert.guests.map { it.name }, // 转换为字符串列表
                    status = updatedConcert.status,
                    category = updatedConcert.category,
                    rating = updatedConcert.rating
                )
            )
            
            // 更新本地演出对象并刷新UI
            concert = updatedConcert
            populateViews(concert)
            
            // 显示成功提示
            Toast.makeText(this, "评价已更新", Toast.LENGTH_SHORT).show()
        }
        
        // 添加取消按钮
        builder.setNegativeButton("取消", null)
        
        // 显示对话框
        builder.show()
    }

    private fun showChangeStatusDialog() {
        val statusOptions = resources.getStringArray(R.array.status_options)
        
        // 创建对话框
        val builder = AlertDialog.Builder(this)
        builder.setTitle("修改演出状态")
        
        // 计算当前状态的索引
        val currentIndex = statusOptions.indexOfFirst { it == concert.status }
        
        // 创建单选列表
        builder.setSingleChoiceItems(statusOptions, currentIndex) { dialog, which ->
            val selectedStatus = statusOptions[which]
            
            // 如果状态没有变化，则不执行任何操作
            if (selectedStatus == concert.status) {
                Toast.makeText(this, "状态未改变", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                return@setSingleChoiceItems
            }
            
            // 创建新的演出对象（因为Concert是不可变的data class）
            val updatedConcert = Concert(
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
                performers = concert.performers,
                guests = concert.guests,
                status = selectedStatus,
                category = concert.category,
                rating = concert.rating
            )
            
            // 更新数据库
            concertViewModel.updateConcert(
                ConcertEntity(
                    id = updatedConcert.id,
                    title = updatedConcert.title,
                    venue = updatedConcert.venue,
                    date = updatedConcert.date,
                    notes = updatedConcert.notes,
                    posterResId = updatedConcert.posterResId,
                    posterPath = updatedConcert.posterPath,
                    ticketPrice = updatedConcert.ticketPrice,
                    ticketPriceCurrency = updatedConcert.ticketPriceCurrency,
                    actualPaid = updatedConcert.actualPaid,
                    actualPaidCurrency = updatedConcert.actualPaidCurrency,
                    otherFees = updatedConcert.otherFees,
                    otherFeesCurrency = updatedConcert.otherFeesCurrency,
                    performers = updatedConcert.performers.map { it.name }, // 转换为字符串列表
                    guests = updatedConcert.guests.map { it.name }, // 转换为字符串列表
                    status = updatedConcert.status,
                    category = updatedConcert.category,
                    rating = updatedConcert.rating
                )
            )
            
            // 更新本地演出对象并刷新UI
            concert = updatedConcert
            populateViews(concert)
            
            // 显示成功提示
            Toast.makeText(this, "状态已更新", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        
        builder.setNegativeButton("取消", null)
        builder.show()
    }
    
    private fun updatePerformersDisplay(performers: List<Performer>) {
        // 如果没有演出者信息，隐藏演出者标题和容器
        if (performers.isEmpty()) {
            val tvPerformers = findViewById<TextView>(R.id.tvPerformers)
            tvPerformers.visibility = View.GONE
            llPerformersContainer.visibility = View.GONE
            return
        }
        
        // 显示演出者标题和容器
        val tvPerformers = findViewById<TextView>(R.id.tvPerformers)
        tvPerformers.visibility = View.VISIBLE
        llPerformersContainer.visibility = View.VISIBLE
        
        llPerformersContainer.removeAllViews()
        for (performer in performers) {
            val performerTag = TextView(this)
            performerTag.text = performer.name
            // 应用淡橙色圆角框样式
            performerTag.setBackgroundResource(R.drawable.performer_tag_background)
            performerTag.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            performerTag.setPadding(16, 8, 16, 8)

            // 使用FlexboxLayout.LayoutParams设置布局参数
            val layoutParams = com.google.android.flexbox.FlexboxLayout.LayoutParams(
                com.google.android.flexbox.FlexboxLayout.LayoutParams.WRAP_CONTENT,
                com.google.android.flexbox.FlexboxLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 8, 8)
            }
            performerTag.layoutParams = layoutParams

            // 添加演出者标签到容器中
            llPerformersContainer.addView(performerTag)
        }
    }
    
    private fun updateGuestsDisplay(guests: List<Performer>) {
        // 如果没有嘉宾信息，隐藏嘉宾标题和容器
        if (guests.isEmpty()) {
            val tvGuests = findViewById<TextView>(R.id.tvGuests)
            tvGuests.visibility = View.GONE
            llGuestsContainer.visibility = View.GONE
            return
        }
        
        // 显示嘉宾标题和容器
        val tvGuests = findViewById<TextView>(R.id.tvGuests)
        tvGuests.visibility = View.VISIBLE
        llGuestsContainer.visibility = View.VISIBLE
        
        llGuestsContainer.removeAllViews()
        for (guest in guests) {
            val guestTag = TextView(this)
            guestTag.text = guest.name
            // 应用淡橙色圆角框样式
            guestTag.setBackgroundResource(R.drawable.performer_tag_background)
            guestTag.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            guestTag.setPadding(16, 8, 16, 8)

            // 使用FlexboxLayout.LayoutParams设置布局参数
            val layoutParams = com.google.android.flexbox.FlexboxLayout.LayoutParams(
                com.google.android.flexbox.FlexboxLayout.LayoutParams.WRAP_CONTENT,
                com.google.android.flexbox.FlexboxLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 8, 8)
            }
            guestTag.layoutParams = layoutParams

            // 添加嘉宾标签到容器中
            llGuestsContainer.addView(guestTag)
        }
    }
}