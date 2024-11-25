package com.example.wakeup

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import java.util.Calendar

class EditAlarmActivity : AppCompatActivity() {

    private val selectedDaysState = mutableSetOf("M", "Tu", "W", "Th", "F", "Sa", "Su")
    private var selectedRingtone: String = "old_fashioned_alarm"
    private var tempSelectedRingtone: String = selectedRingtone
    private var selectedHour: Int = 0
    private var selectedMinute: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_edit_alarm)

        configureSystemBars()
        initializeAlarmData()

        findViewById<TimePicker>(R.id.timePicker).apply {
            setIs24HourView(true)
            hour = selectedHour
            minute = selectedMinute
            setOnTimeChangedListener { _, hour, minute ->
                selectedHour = hour
                selectedMinute = minute
            }
        }

        val ringtoneTextView: TextView = findViewById(R.id.ringtoneName)
        ringtoneTextView.text = selectedRingtone
        findViewById<LinearLayout>(R.id.ringtoneContainer).setOnClickListener {
            showRingtoneBottomSheet(ringtoneTextView)
        }

        findViewById<LinearLayout>(R.id.daysContainer).setOnClickListener {
            showDaysBottomSheet()
        }

        findViewById<ImageView>(R.id.saveAlarmButton).setOnClickListener { saveAlarmData() }
        findViewById<ImageView>(R.id.cancelAlarmButton).setOnClickListener { cancelEdit() }
    }

    private fun configureSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.parseColor("#1B1F24")
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
    }

    private fun initializeAlarmData() {
        val alarmDataJson = intent.getStringExtra("alarmData")

        if (alarmDataJson != null) {
            val alarm = Gson().fromJson(alarmDataJson, Alarms::class.java)
            selectedHour = alarm.hour
            selectedMinute = alarm.minute
            selectedDaysState.clear()
            selectedDaysState.addAll(alarm.days)
            selectedRingtone = alarm.ringtone
            updateRepeatDaysTextView()
        } else {
            val calendar = Calendar.getInstance()
            selectedHour = calendar.get(Calendar.HOUR_OF_DAY)
            selectedMinute = calendar.get(Calendar.MINUTE)
            selectedDaysState.clear()
            selectedDaysState.addAll(listOf("M", "Tu", "W", "Th", "F", "Sa", "Su"))
            selectedRingtone = "old_fashioned_alarm"
            updateRepeatDaysTextView()
        }
    }

    private fun updateRepeatDaysTextView() {
        val repeatDaysTextView: TextView = findViewById(R.id.repeatDays)
        repeatDaysTextView.text = if (selectedDaysState.size == 7) {
            "Daily"
        } else {
            selectedDaysState.joinToString("  ")
        }
    }

    private fun saveAlarmData() {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("selectedDays", ArrayList(selectedDaysState))
            putExtra("selectedRingtone", selectedRingtone)
            putExtra("selectedHour", selectedHour)
            putExtra("selectedMinute", selectedMinute)
            putExtra("alarmPosition", intent.getIntExtra("alarmPosition", -1))
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
        AlarmUtil.setAlarm(this, selectedHour, selectedMinute, selectedRingtone)
    }

    private fun cancelEdit() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showDaysBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = layoutInflater.inflate(R.layout.bottomsheet_days, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        val checkBoxes = mapOf(
            R.id.checkbox_monday to "M",
            R.id.checkbox_tuesday to "Tu",
            R.id.checkbox_wednesday to "W",
            R.id.checkbox_thursday to "Th",
            R.id.checkbox_friday to "F",
            R.id.checkbox_saturday to "Sa",
            R.id.checkbox_sunday to "Su"
        )

        checkBoxes.forEach { (checkboxId, day) ->
            val checkBox = bottomSheetView.findViewById<CheckBox>(checkboxId)
            checkBox.isChecked = selectedDaysState.contains(day)
            checkBox.setOnCheckedChangeListener { _, _ -> updateSaveButtonState(bottomSheetView) }
        }

        bottomSheetView.findViewById<ImageView>(R.id.saveBtn).setOnClickListener {
            selectedDaysState.clear()
            checkBoxes.forEach { (checkboxId, day) ->

                if (bottomSheetView.findViewById<CheckBox>(checkboxId).isChecked) {
                    selectedDaysState.add(day)
                }
            }
            updateRepeatDaysTextView()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun updateSaveButtonState(view: View) {
        val saveButton: ImageView = view.findViewById(R.id.saveBtn)
        val checkBoxes = listOf(
            R.id.checkbox_monday, R.id.checkbox_tuesday, R.id.checkbox_wednesday, R.id.checkbox_thursday,
            R.id.checkbox_friday, R.id.checkbox_saturday, R.id.checkbox_sunday
        )

        saveButton.visibility = if (checkBoxes.any { view.findViewById<CheckBox>(it).isChecked }) View.VISIBLE else View.GONE
    }

    private fun showRingtoneBottomSheet(ringtoneTextView: TextView) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = layoutInflater.inflate(R.layout.bottomsheet_ringtones, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        tempSelectedRingtone = selectedRingtone
        val ringtoneMap = mapOf(
            R.id.radiobutton1 to "old_fashioned_alarm",
            R.id.radiobutton2 to "reveille_loud",
            R.id.radiobutton3 to "ringtone_new_viral",
            R.id.radiobutton4 to "police_alarm"
        )

        ringtoneMap.forEach { (id, ringtone) ->
            val radioButton = bottomSheetView.findViewById<RadioButton>(id)
            radioButton.isChecked = ringtone == tempSelectedRingtone
            radioButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) tempSelectedRingtone = ringtone
            }
        }

        bottomSheetView.findViewById<ImageView>(R.id.saveBtn)?.setOnClickListener {
            selectedRingtone = tempSelectedRingtone
            ringtoneTextView.text = selectedRingtone
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }
}
