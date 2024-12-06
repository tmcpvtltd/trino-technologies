package com.codetech.lib.fcm.abstraction

import android.content.Context

class NotificationMonitorImp(
    override var mContext: Context,
    override var mTopic: String,
    override var mRingtone: Boolean
) : NotificationMonitor() {
    private val mNotificationService by
    lazy {
        NotificationService(
            mContext, mTopic, mRingtone
        )
    }

    override fun startService() {
        mNotificationService.startService()
    }

    override fun cancelService() {
        mNotificationService.stopService()
    }
}