package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object ReminderScheduler {
    fun scheduleDayReminder(
        context: Context,
        dayOfWeek: Int, // 1=Mon .. 7=Sun
        hour: Int,
        minute: Int,
        title: String,
        enabled: Boolean
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WorkoutReminderReceiver::class.java).apply {
            putExtra(WorkoutReminderReceiver.EXTRA_TITLE, "Shadow Boxing: $title")
            putExtra(
                WorkoutReminderReceiver.EXTRA_BODY,
                "Hunter Quest ready! Touch gloves and start today's session."
            )
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            100 + dayOfWeek,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (!enabled) {
            alarmManager.cancel(pendingIntent)
            return
        }

        val targetCalDay = when (dayOfWeek) {
            1 -> Calendar.MONDAY
            2 -> Calendar.TUESDAY
            3 -> Calendar.WEDNESDAY
            4 -> Calendar.THURSDAY
            5 -> Calendar.FRIDAY
            6 -> Calendar.SATURDAY
            else -> Calendar.SUNDAY
        }

        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, targetCalDay)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            AlarmManager.INTERVAL_DAY * 7,
            pendingIntent
        )
    }
}
