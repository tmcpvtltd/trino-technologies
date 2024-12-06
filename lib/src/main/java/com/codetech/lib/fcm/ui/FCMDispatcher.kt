package com.codetech.lib.fcm.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.codetech.lib.R
import com.codetech.lib.helper.Utils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.concurrent.atomic.AtomicInteger

class FCMDispatcher : FirebaseMessagingService() {
    private val seed = AtomicInteger()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (!Utils.hasPermission(this, Manifest.permission.POST_NOTIFICATIONS)) return

        remoteMessage.data.takeIf { it.isNotEmpty() }?.let {
            Log.d(TAG, "Message data payload: $it")

            val icon = it["icon"]
            val title = remoteMessage.notification?.title ?: it["title"]
            val shortDesc = remoteMessage.notification?.body ?: it["short_desc"]
            val image = remoteMessage.notification?.imageUrl?.toString() ?: it["feature"]
            val packageName = it["package"]

            sendNotification(icon, title, shortDesc, image, packageName)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New Token: $token")
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun sendNotification(
        icon: String?,
        title: String?,
        shortDesc: String?,
        image: String?,
        storePackage: String?
    ) {
        val intent = createLaunchIntent(storePackage)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

        val remoteViews = createRemoteViews(title, shortDesc)
        val notificationBuilder = createNotificationBuilder(remoteViews, pendingIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        val notificationID = getNextInt()
        notificationManager.notify(notificationID, notificationBuilder.build())

        // Load images asynchronously
        loadImage(icon, R.id.iv_icon, remoteViews, notificationBuilder, notificationManager, notificationID)
        image?.let {
            remoteViews.setViewVisibility(R.id.iv_feature, View.VISIBLE)
            loadImage(it, R.id.iv_feature, remoteViews, notificationBuilder, notificationManager, notificationID)
        }
    }

    private fun createLaunchIntent(storePackage: String?): Intent {
        return if (isAppInstalled(storePackage)) {
            packageManager.getLaunchIntentForPackage(storePackage!!) ?: openHomeScreen()
        } else {
            setStoreIntent(storePackage)
        }.apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
    }

    private fun openHomeScreen(): Intent {
        return Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    private fun setStoreIntent(storePackage: String?): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$storePackage"))
            .takeIf { storePackage != null }
            ?: openHomeScreen()
    }

    private fun createRemoteViews(title: String?, shortDesc: String?): RemoteViews {
        return RemoteViews(packageName, R.layout.notification_view).apply {
            setTextViewText(R.id.tv_title, title)
            setTextViewText(R.id.tv_short_desc, shortDesc)
        }
    }

    private fun createNotificationBuilder(
        remoteViews: RemoteViews,
        pendingIntent: PendingIntent
    ): NotificationCompat.Builder {
        val channelId = getString(R.string.default_notification_channel_id)
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification_icon)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setCustomContentView(remoteViews)
            .setCustomBigContentView(remoteViews)
            .setAutoCancel(true)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getString(R.string.default_notification_channel_id),
                getString(R.string.default_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun loadImage(
        url: String?,
        viewId: Int,
        remoteViews: RemoteViews,
        builder: NotificationCompat.Builder,
        manager: NotificationManager,
        notificationID: Int
    ) {
        Glide.with(this).asBitmap().load(url).into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                remoteViews.setImageViewBitmap(viewId, resource)
                manager.notify(notificationID, builder.build())
            }

            override fun onLoadCleared(placeholder: Drawable?) {}
        })
    }

    private fun isAppInstalled(uri: String?): Boolean {
        return uri?.let {
            try {
                packageManager.getApplicationInfo(it, 0).enabled
            } catch (e: Exception) {
                false
            }
        } ?: false
    }

    private fun getNextInt(): Int {
        return seed.incrementAndGet()
    }

    companion object {
        private const val TAG = "FCMDispatcher"
    }
}


/*
class FCMDispatcher : FirebaseMessagingService() {
    private val seed = AtomicInteger()
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (!Utils.hasPermission(this, Manifest.permission.POST_NOTIFICATIONS)) return

        //Data available
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            //fetch data into variables
            val icon = remoteMessage.data["icon"]
            val title = remoteMessage.notification?.title ?: remoteMessage.data["title"]
            val shortDesc = remoteMessage.notification?.body ?: remoteMessage.data["short_desc"]
            val longDesc = remoteMessage.data["long_desc"]
            val image =
                remoteMessage.notification?.imageUrl?.toString() ?: remoteMessage.data["feature"]
            val packageName = remoteMessage.data["package"]

            sendNotification(
                icon = icon,
                title = title,
                shortDesc = shortDesc,
                image = image,
                storePackage = packageName
            )
        }
    }


    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.d(TAG, p0)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun sendNotification(
        icon: String?,
        title: String?,
        shortDesc: String?,
        image: String?,
        storePackage: String?
    ) {

        //Open PlayStore
        val intent = if (!isAppInstalled(storePackage, this)) {
            setStoreIntent(storePackage)
        } else {
            openApp()
        }


        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
            )
        } else {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        }


        //Make Remote Views For text
        val remoteViews = RemoteViews(packageName, R.layout.notification_view)
        remoteViews.setTextViewText(R.id.tv_title, title)
        remoteViews.setTextViewText(R.id.tv_short_desc, shortDesc)

        //Notification Parameters
        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification_icon)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setCustomContentView(remoteViews)
            .setCustomBigContentView(remoteViews)
            .setAutoCancel(true)

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.default_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        //Build Notification
        val notificationID = getNextInt()
        notificationManager.notify(notificationID, notificationBuilder.build())


        //Set Images into remoteViews
        try {
            Utils.loadBitmapFromUrl(this,icon){bitmap->
                if (bitmap==null) return@loadBitmapFromUrl
                remoteViews.setImageViewBitmap(R.id.iv_icon, bitmap)
                val notification = notificationBuilder.build()
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(notificationID, notification)
            }

            if (image != null) {
                remoteViews.setViewVisibility(R.id.iv_feature, View.VISIBLE)
                Utils.loadBitmapFromUrl(this,icon){bitmap->
                    if (bitmap==null) return@loadBitmapFromUrl
                    remoteViews.setImageViewBitmap(R.id.iv_feature, bitmap)
                    val notification = notificationBuilder.build()
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(notificationID, notification)
                }
            }
        } catch (_: Exception) {
        } catch (_: java.lang.Exception) {
        } catch (_: IllegalStateException) {
        } catch (_: IllegalArgumentException) {
        }
    }

    private fun openApp(): Intent {
        return packageManager.getLaunchIntentForPackage(this.packageName)
            ?: Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }.also {
                Log.e("OpenApp", "Launch intent not found for package: ${this.packageName}")
            }
    }


    private fun setStoreIntent(storePackage: String?): Intent {
        if (Utils.isEmptyStr(storePackage))  return openApp()
        return try {
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$storePackage"))
        } catch (e: ActivityNotFoundException) {
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$storePackage")
            )
        }
    }

    private fun isAppInstalled(uri: String?, context: Context): Boolean {
        if (Utils.isEmptyStr(uri)) return false
        val pm = context.packageManager
        return try {
            val applicationInfo = pm.getApplicationInfo(uri!!, 0)
            applicationInfo.enabled
        } catch (e: PackageManager.NameNotFoundException) {
            false
        } catch (e: NullPointerException) {
            false
        }
    }

    private fun getNextInt(): Int {
        return seed.incrementAndGet()
    }

    companion object {
        const val TAG = "PandaFirebaseMsgService"

    }
}*/
