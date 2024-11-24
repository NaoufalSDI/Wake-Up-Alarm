package com.example.wakeup

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AlarmAdapter(
    private val alarms: MutableList<Alarms>,
    private val onItemClick: (Alarms, Int) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    inner class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        val dayTextViews = listOf<TextView>(
            itemView.findViewById(R.id.monday),
            itemView.findViewById(R.id.tuesday),
            itemView.findViewById(R.id.wednesday),
            itemView.findViewById(R.id.thursday),
            itemView.findViewById(R.id.friday),
            itemView.findViewById(R.id.saturday),
            itemView.findViewById(R.id.sunday)
        )
        val switchButton: ImageView = itemView.findViewById(R.id.switchButton)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        AlarmViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.alarm_item, parent, false)
        )

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarms.getOrNull(position) ?: return

        holder.timeTextView.apply {
            text = String.format("%02d:%02d", alarm.hour, alarm.minute)
            setTextColor(if (alarm.isEnabled) Color.parseColor("#F8BDC5CA") else Color.parseColor("#BA606871"))
        }

        val daysAbbreviations = listOf("M", "Tu", "W", "Th", "F", "Sa", "Su")
        holder.dayTextViews.forEachIndexed { index, textView ->
            textView.apply {
                visibility = View.VISIBLE
                setTextColor(
                    when {
                        alarm.isEnabled && daysAbbreviations[index] in alarm.days -> Color.parseColor("#FD251E")
                        alarm.isEnabled -> Color.parseColor("#B1BEC0")
                        daysAbbreviations[index] in alarm.days -> Color.parseColor("#CCAF2A32")
                        else -> Color.parseColor("#BA606871")
                    }
                )
            }
        }

        holder.switchButton.apply {
            setImageResource(if (alarm.isEnabled) R.drawable.switch_on else R.drawable.switch_off)
            setOnClickListener {
                alarm.isEnabled = !alarm.isEnabled
                notifyItemChanged(position)
                if (alarm.isEnabled) {
                    AlarmUtil.setAlarm(it.context, alarm.hour, alarm.minute, alarm.ringtone)
                } else {
                    AlarmUtil.cancelAlarm(it.context)
                }
            }
        }

        holder.deleteButton.apply {
            setColorFilter(if (alarm.isEnabled) Color.parseColor("#EB2E28") else Color.parseColor("#CCAF2A39"))
            setOnClickListener {
                if (position in alarms.indices) {
                    alarms.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, alarms.size)
                    AlarmUtil.cancelAlarm(it.context)
                }
                if (it.context is MainActivity) {
                    (it.context as MainActivity).updateAlarmFoundVisibility()
                }
            }
        }

        holder.itemView.setOnClickListener { onItemClick(alarm, position) }
    }

    override fun getItemCount(): Int = if (alarms.isEmpty()) 0 else alarms.size
}
