package com.serveterdogan.lume.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.serveterdogan.lume.MainActivity
import com.serveterdogan.lume.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.Calendar

const val STORY_CHANNEL_ID = "luma_story_channel"
const val STORY_NOTIFICATION_ID = 1001

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settingsRepository: com.serveterdogan.lume.domain.repository.SettingsRepository

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notificationsEnabled = settingsRepository.notificationsFlow.first()
                if (notificationsEnabled) {
                    sendStoryNotification(context)
                }
            } finally {
                // Her gün tekrar etmesi için alarmı bir sonraki güne tekrar kuruyoruz
                scheduleStoryNotification(context)
                pendingResult.finish()
            }
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            scheduleStoryNotification(context)
        }
    }
}

fun sendStoryNotification(context: Context) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            STORY_CHANNEL_ID,
            "Günün Hikayesi",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Günün hikayesini oluşturmaya hazır olduğunuzda bildirim alın."
        }
        notificationManager.createNotificationChannel(channel)
    }

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra("navigate_to", "daily_story")
    }
    
    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, STORY_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("🌙 Günün Hikayesi Hazır!")
        .setContentText("Bugünü nasıl geçirdiğini kaydetmeye hazır mısın?")
        .setStyle(
            NotificationCompat.BigTextStyle()
                .bigText("Bugünü nasıl geçirdiğini kaydetmeye hazır mısın? Günün hikayesini oluşturmak için dokun. ✨")
        )
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(STORY_NOTIFICATION_ID, notification)
}

fun scheduleStoryNotification(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        STORY_NOTIFICATION_ID,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val targetTime = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 21)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        
        // Eğer 21:00 geçtiyse bir sonraki güne kur
        if (before(Calendar.getInstance())) {
            add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    // Android 12+ için exact alarm iznini kontrol et
    val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }

    if (canScheduleExact) {
        // Tam saatinde uyandırması için
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            targetTime.timeInMillis,
            pendingIntent
        )
    } else {
        // İzin yoksa esnek (inexact) alarm kur
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            targetTime.timeInMillis,
            pendingIntent
        )
    }
}
