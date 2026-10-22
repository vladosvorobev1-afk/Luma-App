package com.example.formamind.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // ID упражнения по умолчанию
        val defaultExerciseId = "anxiety_1"
        NotificationHelper.showReminder(context, defaultExerciseId)
    }
}
