package com.ds.liverecorder.ui

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.ds.liverecorder.R
import com.ds.liverecorder.data.entity.ConcertEntity
import com.ds.liverecorder.data.viewmodel.ConcertViewModel
import com.ds.liverecorder.ui.model.Concert
import com.google.android.flexbox.FlexboxLayout
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class AddConcertDialogFragment : DialogFragment() {

    private lateinit var etName: EditText
    private lateinit var btnSave: Button
    private lateinit var btnSelectVenue: Button
    private lateinit var etDateTime: EditText
    private lateinit var spnStatus: Spinner
    private lateinit var spnCategory: Spinner
    private lateinit var btnAddPoster: Button
    private lateinit var etPerformer: EditText
    private lateinit var btnAddPerformer: Button
    private lateinit var llPerformersContainer: FlexboxLayout
    private lateinit var etGuest: EditText
    private lateinit var btnAddGuest: Button
    private lateinit var llGuestsContainer: FlexboxLayout
    private lateinit var btnAddGuests: Button
    private lateinit var ratingBar: RatingBar
    private lateinit var etTicketPrice: EditText
    private lateinit var etActualPaid: EditText
    private lateinit var etOtherFees: EditText
    private lateinit var spnTicketPriceCurrency: Spinner
    private lateinit var spnActualPaidCurrency: Spinner
    private lateinit var spnOtherFeesCurrency: Spinner
    private lateinit var etSeatNumber: EditText
    private lateinit var etNotes: EditText
    private lateinit var btnBack: ImageButton // 新增返回按钮引用

    private lateinit var concertViewModel: ConcertViewModel
    private var selectedCalendar: Calendar = Calendar.getInstance()
    private var editingConcert: Concert? = null
    private var isEditMode = false
    private var selectedImageUri: Uri? = null
    private var posterImagePath: String? = null
    private var performersList: MutableList<String> = mutableListOf()
    private var guestsList: MutableList<String> = mutableListOf() // 添加嘉宾列表

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onStart() {
        super.onStart()
        // 让DialogFragment铺满屏幕
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_add_concert, container, false)
        
        // 设置动画效果
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
        
        initViews(view)
        initViewModel()
        initCurrencySpinners()
        setupListeners()
        setupEditMode()
        updateDateTimeDisplay()
        setupDefaultCurrency()
        
        return view
    }

    private fun initViews(view: View) {
        etName = view.findViewById(R.id.etName)
        btnSave = view.findViewById(R.id.btnSave)
        btnSelectVenue = view.findViewById(R.id.btnSelectVenue)
        etDateTime = view.findViewById(R.id.etDateTime)
        spnStatus = view.findViewById(R.id.spnStatus)
        spnCategory = view.findViewById(R.id.spnCategory)
        btnAddPoster = view.findViewById(R.id.btnAddPoster)
        etPerformer = view.findViewById(R.id.etPerformer)
        btnAddPerformer = view.findViewById(R.id.btnAddPerformer)
        llPerformersContainer = view.findViewById(R.id.llPerformersContainer)
        etGuest = view.findViewById(R.id.etGuest) // 初始化嘉宾输入框
        btnAddGuest = view.findViewById(R.id.btnAddGuest) // 初始化添加嘉宾按钮
        llGuestsContainer = view.findViewById(R.id.llGuestsContainer) // 初始化嘉宾容器
        btnAddGuests = view.findViewById(R.id.btnAddGuests)
        ratingBar = view.findViewById(R.id.ratingBar)
        etTicketPrice = view.findViewById(R.id.etTicketPrice)
        etActualPaid = view.findViewById(R.id.etActualPaid)
        etOtherFees = view.findViewById(R.id.etOtherFees)
        spnTicketPriceCurrency = view.findViewById(R.id.spnTicketPriceCurrency)
        spnActualPaidCurrency = view.findViewById(R.id.spnActualPaidCurrency)
        spnOtherFeesCurrency = view.findViewById(R.id.spnOtherFeesCurrency)
        etSeatNumber = view.findViewById(R.id.etSeatNumber)
        etNotes = view.findViewById(R.id.etNotes)
        btnBack = view.findViewById(R.id.btnBack)
    }

    private fun initViewModel() {
        // 获取数据库实例
        val database = com.ds.liverecorder.data.ConcertDatabase.getDatabase(requireContext())
        val concertDao = database.concertDao()
        val performerDao = database.performerDao()
        
        // 创建repository
        val repository = com.ds.liverecorder.data.repository.ConcertRepository(concertDao, performerDao)
        
        // 使用Factory创建ViewModel
        val factory = com.ds.liverecorder.data.viewmodel.ConcertViewModelFactory(repository)
        concertViewModel = androidx.lifecycle.ViewModelProvider(this, factory)[ConcertViewModel::class.java]
    }

    private fun initCurrencySpinners() {
        val currencyAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.currency_options,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        spnTicketPriceCurrency.adapter = currencyAdapter
        spnActualPaidCurrency.adapter = currencyAdapter
        spnOtherFeesCurrency.adapter = currencyAdapter
    }

    private fun setupListeners() {
        btnSave.setOnClickListener { saveConcert() }
        btnBack.setOnClickListener { dismiss() }
        btnAddPoster.setOnClickListener { openImagePicker() }
        btnAddPerformer.setOnClickListener { addPerformer() }
        btnAddGuest.setOnClickListener { addGuest() } // 添加嘉宾按钮点击事件
        etDateTime.setOnClickListener { showDateTimePicker() }
    }

    private fun setupEditMode() {
        @Suppress("DEPRECATION")
        val concertArg = arguments?.getSerializable("concert") as? Concert
        if (concertArg != null) {
            editingConcert = concertArg
            isEditMode = true
            populateFields(editingConcert!!)
            // 修复：在编辑模式下也应该显示"保存"而不是"编辑"
            btnSave.text = getString(R.string.save)
        } else {
            // 设置默认状态为"待看"
            val statusOptions = resources.getStringArray(R.array.status_options)
            val defaultStatusIndex = statusOptions.indexOfFirst { it == "待看" }
            if (defaultStatusIndex >= 0) {
                spnStatus.setSelection(defaultStatusIndex)
            }
            
            // 设置默认分类为"现场"
            val categoryOptions = resources.getStringArray(R.array.category_options)
            val defaultCategoryIndex = categoryOptions.indexOfFirst { it == "现场" }
            if (defaultCategoryIndex >= 0) {
                spnCategory.setSelection(defaultCategoryIndex)
            }
        }
    }

    private fun setupDefaultCurrency() {
        if (!isEditMode) {
            spnTicketPriceCurrency.setSelection(0)
            spnActualPaidCurrency.setSelection(0)
            spnOtherFeesCurrency.setSelection(0)
        }
    }

    private fun addPerformer() {
        val performerName = etPerformer.text.toString().trim()
        if (performerName.isNotEmpty()) {
            // 添加到列表
            performersList.add(performerName)
            
            // 创建带删除按钮的演出者标签
            val performerItem = layoutInflater.inflate(R.layout.item_performer_with_delete, null)
            val tvPerformerName = performerItem.findViewById<TextView>(R.id.tvPerformerName)
            val ivDelete = performerItem.findViewById<ImageView>(R.id.ivDelete)
            
            tvPerformerName.text = performerName
            
            // 设置删除按钮点击事件
            ivDelete.setOnClickListener {
                // 从列表中移除
                performersList.remove(performerName)
                // 从容器中移除视图
                llPerformersContainer.removeView(performerItem)
            }
            
            // 添加到容器
            llPerformersContainer.addView(performerItem)
            
            // 清空输入框
            etPerformer.setText("")
        } else {
            Toast.makeText(requireContext(), "请输入演出者名称", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addGuest() {
        val guestName = etGuest.text.toString().trim()
        if (guestName.isNotEmpty()) {
            // 添加到列表
            guestsList.add(guestName)
            
            // 创建带删除按钮的嘉宾标签
            val guestItem = layoutInflater.inflate(R.layout.item_performer_with_delete, null)
            val tvGuestName = guestItem.findViewById<TextView>(R.id.tvPerformerName)
            val ivDelete = guestItem.findViewById<ImageView>(R.id.ivDelete)
            
            tvGuestName.text = guestName
            
            // 设置删除按钮点击事件
            ivDelete.setOnClickListener {
                // 从列表中移除
                guestsList.remove(guestName)
                // 从容器中移除视图
                llGuestsContainer.removeView(guestItem)
            }
            
            // 添加到容器
            llGuestsContainer.addView(guestItem)
            
            // 清空输入框
            etGuest.setText("")
        } else {
            Toast.makeText(requireContext(), "请输入嘉宾名称", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
            selectedImageUri = result.data?.data
            try {
                posterImagePath = saveImageToInternalStorage(selectedImageUri!!)
                Toast.makeText(requireContext(), "已选择海报图片", Toast.LENGTH_SHORT).show()
                btnAddPoster.text = "更改海报"
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "无法处理选择的图片", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun saveConcert() {
        val title = etName.text.toString().trim()
        val venue = btnSelectVenue.text.toString().trim()
        val date = selectedCalendar.time
        val notes = etNotes.text.toString().trim()
        val ticketPrice = etTicketPrice.text.toString().trim()
        val actualPaid = etActualPaid.text.toString().trim()
        val otherFees = etOtherFees.text.toString().trim()
        val status = spnStatus.selectedItem.toString()
        val category = spnCategory.selectedItem.toString()
        val rating = ratingBar.rating.toInt()
        val ticketPriceCurrency = spnTicketPriceCurrency.selectedItem.toString()
        val actualPaidCurrency = spnActualPaidCurrency.selectedItem.toString()
        val otherFeesCurrency = spnOtherFeesCurrency.selectedItem.toString()

        // 修复：在编辑模式下，如果没有选择新海报，则保留原有海报路径
        val finalPosterPath = if (isEditMode) {
            // 如果是编辑模式，且没有选择新海报，则使用原有海报路径
            posterImagePath ?: editingConcert?.posterPath
        } else {
            // 如果是新增模式，则使用选择的海报路径（可能为null）
            posterImagePath
        }

        val concertEntity = ConcertEntity(
            id = if (isEditMode) editingConcert!!.id else 0,
            title = title.ifEmpty { editingConcert?.title ?: "" },
            venue = venue.ifEmpty { editingConcert?.venue ?: "" },
            date = date,
            notes = notes.ifEmpty { editingConcert?.notes ?: "" },
            posterResId = editingConcert?.posterResId ?: R.drawable.ic_launcher_background,
            posterPath = finalPosterPath,
            ticketPrice = ticketPrice.ifEmpty { editingConcert?.ticketPrice ?: "0.00" },
            ticketPriceCurrency = ticketPriceCurrency,
            actualPaid = actualPaid.ifEmpty { editingConcert?.actualPaid ?: "0.00" },
            actualPaidCurrency = actualPaidCurrency,
            otherFees = otherFees.ifEmpty { editingConcert?.otherFees ?: "0.00" },
            otherFeesCurrency = otherFeesCurrency,
            performers = performersList.toList(), // 使用performersList的副本
            guests = guestsList.toList(), // 使用guestsList的副本
            status = status,
            category = category,
            rating = rating
        )

        if (isEditMode) {
            concertViewModel.updateConcert(concertEntity)
        } else {
            concertViewModel.insertConcert(concertEntity)
        }

        dismiss()
    }

    private fun populateFields(concert: Concert) {
        etName.setText(concert.title)
        btnSelectVenue.setText(concert.venue)
        selectedCalendar = Calendar.getInstance()
        selectedCalendar.time = concert.date
        updateDateTimeDisplay()
        spnStatus.setSelection(getIndexFromValue(concert.status, resources.getStringArray(R.array.status_options)))
        spnCategory.setSelection(getIndexFromValue(concert.category, resources.getStringArray(R.array.category_options)))
        ratingBar.rating = concert.rating.toFloat()
        etTicketPrice.setText(concert.ticketPrice)
        etActualPaid.setText(concert.actualPaid)
        etOtherFees.setText(concert.otherFees)
        spnTicketPriceCurrency.setSelection(getIndexFromValue(concert.ticketPriceCurrency, resources.getStringArray(R.array.currency_options)))
        spnActualPaidCurrency.setSelection(getIndexFromValue(concert.actualPaidCurrency, resources.getStringArray(R.array.currency_options)))
        spnOtherFeesCurrency.setSelection(getIndexFromValue(concert.otherFeesCurrency, resources.getStringArray(R.array.currency_options)))
        etNotes.setText(concert.notes)

        // 处理阵容
        performersList.clear()
        // 确保llPerformersContainer已初始化后再使用
        if (::llPerformersContainer.isInitialized) {
            llPerformersContainer.removeAllViews()
            concert.performers.forEach { performer ->
                val performerName = performer.name // 从Performer对象中提取名称
                performersList.add(performerName)
                val performerItem = layoutInflater.inflate(R.layout.item_performer_with_delete, null)
                val tvPerformerName = performerItem.findViewById<TextView>(R.id.tvPerformerName)
                val ivDelete = performerItem.findViewById<ImageView>(R.id.ivDelete)
                
                tvPerformerName.text = performerName
                
                ivDelete.setOnClickListener {
                    performersList.remove(performerName)
                    llPerformersContainer.removeView(performerItem)
                }
                
                llPerformersContainer.addView(performerItem)
            }
        }

        // 处理嘉宾
        guestsList.clear()
        // 确保llGuestsContainer已初始化后再使用
        if (::llGuestsContainer.isInitialized) {
            llGuestsContainer.removeAllViews()
            concert.guests.forEach { guest ->
                val guestName = guest.name // 从Performer对象中提取名称
                guestsList.add(guestName)
                val guestItem = layoutInflater.inflate(R.layout.item_performer_with_delete, null)
                val tvGuestName = guestItem.findViewById<TextView>(R.id.tvPerformerName)
                val ivDelete = guestItem.findViewById<ImageView>(R.id.ivDelete)
                
                tvGuestName.text = guestName
                
                ivDelete.setOnClickListener {
                    guestsList.remove(guestName)
                    llGuestsContainer.removeView(guestItem)
                }
                
                llGuestsContainer.addView(guestItem)
            }
        }
        
        // 显示已有的海报信息
        if (concert.posterPath != null) {
            btnAddPoster.text = "更改海报"
        }
    }

    private fun getIndexFromValue(value: String, options: Array<String>): Int {
        val index = options.indexOfFirst { it == value }
        return if (index >= 0) index else 0
    }

    @SuppressLint("SetTextI18n")
    private fun updateDateTimeDisplay() {
        val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        etDateTime.setText("${dateFormat.format(selectedCalendar.time)} ${timeFormat.format(selectedCalendar.time)}")
    }

    private fun showDateTimePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedCalendar.set(year, month, dayOfMonth)
                val timePickerDialog = TimePickerDialog(
                    requireContext(),
                    { _, hour, minute ->
                        selectedCalendar.set(Calendar.HOUR_OF_DAY, hour)
                        selectedCalendar.set(Calendar.MINUTE, minute)
                        updateDateTimeDisplay()
                    },
                    selectedCalendar.get(Calendar.HOUR_OF_DAY),
                    selectedCalendar.get(Calendar.MINUTE),
                    true
                )
                timePickerDialog.show()
            },
            selectedCalendar.get(Calendar.YEAR),
            selectedCalendar.get(Calendar.MONTH),
            selectedCalendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        val inputStream: InputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val file = File(requireContext().filesDir, "poster_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        outputStream.close()
        inputStream.close()
        return file.absolutePath
    }
}