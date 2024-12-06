package com.codetech.lib.fcm.abstraction

import android.content.Context

abstract class NotificationMonitor{
    abstract  var mContext: Context
    abstract var mTopic:String
    abstract var mRingtone:Boolean

    abstract fun startService()

    abstract fun cancelService()
}