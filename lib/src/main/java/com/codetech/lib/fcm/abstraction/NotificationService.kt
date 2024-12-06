package com.codetech.lib.fcm.abstraction

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.codetech.lib.R
import com.codetech.lib.helper.NotificationDataSetup
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class NotificationService(
    private val mContext: Context,
    private val mTopic: String,
    private val mRingtone: Boolean
) {

    fun startService() {
        initFirebase { isSuccess ->
            NotificationDataSetup.setRingtone(mRingtone)
            createChannelForFCM()
            FirebaseMessaging.getInstance().subscribeToTopic(mTopic)
        }
    }

    fun stopService(){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(mTopic)
    }
    private fun initFirebase(callBack:(isSuccess:Boolean)->Unit){
        try {
            FirebaseApp.initializeApp(mContext)
            callBack(true)
        }catch (e:Exception){
            e.printStackTrace()
            callBack(false)
        }
    }
    private fun createChannelForFCM() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = mContext.getString(R.string.default_notification_channel_id)
            val channelName = mContext.getString(R.string.default_notification_channel_id)
            val notificationManager =
                mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    channelName, NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
    }
}
