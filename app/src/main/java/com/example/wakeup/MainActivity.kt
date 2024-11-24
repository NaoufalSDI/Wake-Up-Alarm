package com.example.wakeup

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {
    private val alarms = mutableListOf<Alarms>()
    private lateinit var alarmAdapter: AlarmAdapter

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_main)

        setupFabScrollBehavior()
        configureSystemBars()
        initializeRecyclerView()
        loadAlarms()
        updateAlarmFoundVisibility()

        handleNewAlarmFromIntent(intent)
    }

    private fun configureSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.parseColor("#1B1F24")
    }

    private fun initializeRecyclerView() {
        alarmAdapter = AlarmAdapter(alarms) { alarm, position ->
            val intent = Intent(this, EditAlarmActivity::class.java).apply {
                putExtra("alarmData", Gson().toJson(alarm))
                putExtra("alarmPosition", position)
            }
            startActivity(intent)
        }

        findViewById<RecyclerView>(R.id.recyclerView).apply {
            adapter = alarmAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }
    private fun setupFabScrollBehavior() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val fab = findViewById<ImageView>(R.id.fab)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var totalScrolledDistance = 0f
            private var maxScrollDistance = fab.height.toFloat()

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                maxScrollDistance = (alarmAdapter.itemCount * 10).coerceAtLeast(fab.height).toFloat()
                totalScrolledDistance = (totalScrolledDistance + dy).coerceIn(0f, maxScrollDistance)
                fab.alpha = if (alarmAdapter.itemCount > 0) 1 - (totalScrolledDistance / maxScrollDistance) else 1f
            }
        })

        fab.setOnClickListener { openEditAlarmActivity() }
    }

    private fun openEditAlarmActivity() {
        startActivity(Intent(this, EditAlarmActivity::class.java))
    }

    private fun handleNewAlarmFromIntent(intent: Intent?) {
        intent?.let {
            val newAlarm = intent.toAlarm()
            val alarmPosition = intent.getIntExtra("alarmPosition", -1)

            if (newAlarm != null) {
                if (alarmPosition >= 0) {
                    alarms[alarmPosition] = newAlarm
                    alarmAdapter.notifyItemChanged(alarmPosition)
                } else {
                    alarms.add(newAlarm)
                    alarmAdapter.notifyItemInserted(alarms.size - 1)
                }
                manageAlarm(newAlarm)
                updateAlarmFoundVisibility()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNewAlarmFromIntent(intent)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.toAlarm()?.let { newAlarm ->
            val alarmPosition = data.getIntExtra("alarmPosition", -1)
            if (alarmPosition >= 0) {
                alarms[alarmPosition] = newAlarm
            } else {
                alarms.add(newAlarm)
            }
            alarmAdapter.notifyDataSetChanged()
        }
    }

    fun updateAlarmFoundVisibility() {
        val alarmFound = findViewById<TextView>(R.id.alarmFound)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        if (alarms.isEmpty()) {
            alarmFound.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            alarmFound.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun saveAlarms() {
        val sharedPreferences = getSharedPreferences("AlarmApp", MODE_PRIVATE)
        sharedPreferences.edit().putString("alarms", Gson().toJson(alarms)).apply()
    }

    private fun loadAlarms() {
        val savedAlarmsJson = getSharedPreferences("AlarmApp", MODE_PRIVATE)
            .getString("alarms", "[]")
        val type = object : TypeToken<MutableList<Alarms>>() {}.type
        val savedAlarms: List<Alarms> = Gson().fromJson(savedAlarmsJson, type)
        alarms.clear()
        alarms.addAll(savedAlarms)
        alarms.forEach { manageAlarm(it) }
    }

    private fun manageAlarm(alarm: Alarms) {
        if (alarm.isEnabled) {
            AlarmUtil.setAlarm(this, alarm.hour, alarm.minute, alarm.ringtone)
        } else {
            AlarmUtil.cancelAlarm(this)
        }
    }

    override fun onPause() {
        super.onPause()
        saveAlarms()
    }

    override fun onDestroy() {
        super.onDestroy()
        saveAlarms()
    }

    private fun Intent.toAlarm(): Alarms? {
        val days = getStringArrayListExtra("selectedDays") ?: listOf()
        val hour = getIntExtra("selectedHour", -1)
        val minute = getIntExtra("selectedMinute", -1)
        val ringtone = getStringExtra("selectedRingtone") ?: "Default Ringtone"

        return if (hour >= 0 && minute >= 0) {
            Alarms(hour, minute, days, ringtone, true)
        } else null
    }
}
