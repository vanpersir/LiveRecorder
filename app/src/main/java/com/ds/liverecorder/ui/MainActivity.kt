package com.ds.liverecorder.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ds.liverecorder.R
import com.ds.liverecorder.data.ConcertDatabase
import com.ds.liverecorder.data.repository.ConcertRepository
import com.ds.liverecorder.data.viewmodel.ConcertViewModel
import com.ds.liverecorder.data.viewmodel.ConcertViewModelFactory
import com.ds.liverecorder.databinding.ActivityMainBinding
import com.ds.liverecorder.ui.AddConcertDialogFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var recordFragment: RecordFragment? = null
    private var upcomingConcertsFragment: UpcomingConcertsFragment? = null
    private lateinit var concertViewModel: ConcertViewModel
    private var lastUpdateTime: Long = 0
    private val updateInterval: Long = 5 * 60 * 1000 // 5分钟更新间隔

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化ViewModel
        val concertDao = ConcertDatabase.getDatabase(this).concertDao()
        val performerDao = ConcertDatabase.getDatabase(this).performerDao()
        val repository = ConcertRepository(concertDao, performerDao)
        val factory = ConcertViewModelFactory(repository)
        concertViewModel = factory.create(ConcertViewModel::class.java)

        // 检查并更新过期演出的状态
        concertViewModel.updateExpiredConcertsStatus()

        // 初始化RecordFragment
        if (savedInstanceState == null) {
            recordFragment = RecordFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, recordFragment!!)
                .commit()
        } else {
            recordFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? RecordFragment
        }

        // 设置底部导航
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_record -> {
                    // 切换到记录页面
                    if (recordFragment == null) {
                        recordFragment = RecordFragment()
                    }
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, recordFragment!!)
                        .commit()
                    true
                }
                R.id.menu_upcoming -> {
                    // 切换到待看列表
                    if (upcomingConcertsFragment == null) {
                        upcomingConcertsFragment = UpcomingConcertsFragment()
                    }
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, upcomingConcertsFragment!!)
                        .commit()
                    true
                }
                R.id.menu_add -> {
                    // 添加演出
                    val addConcertDialog = AddConcertDialogFragment()
                    addConcertDialog.show(supportFragmentManager, "AddConcertDialog")
                    true
                }
                R.id.menu_imprint -> {
                    // TODO: 切换到印记页面
                    true
                }
                R.id.menu_settings -> {
                    // TODO: 切换到设置页面
                    true
                }
                else -> false
            }
        }

    }

    override fun onResume() {
        super.onResume()
        // 检查是否需要更新演出状态（应用从后台回到前台时）
        checkAndUpdateConcertStatuses()
    }

    /**
     * 检查并更新演出状态
     * 控制更新频率，避免过于频繁的更新
     */
    private fun checkAndUpdateConcertStatuses() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTime > updateInterval) {
            concertViewModel.updateExpiredConcertsStatus()
            lastUpdateTime = currentTime
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_card_view -> {
                recordFragment?.switchDisplayMode("card")
                upcomingConcertsFragment?.switchDisplayMode("card")
                true
            }
            R.id.menu_timeline_view -> {
                recordFragment?.switchDisplayMode("timeline")
                upcomingConcertsFragment?.switchDisplayMode("timeline")
                true
            }
            R.id.menu_poster_view -> {
                recordFragment?.switchDisplayMode("poster")
                upcomingConcertsFragment?.switchDisplayMode("poster")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}