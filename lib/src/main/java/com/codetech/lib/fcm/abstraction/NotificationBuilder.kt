package com.codetech.lib.fcm.abstraction

import android.content.Context

class NotificationBuilder private constructor(
    private val mContext: Context,
    private val mTopic: String,
    private val mRingtone: Boolean
){

    class Builder(){

        private lateinit var mContext: Context
        private lateinit var mTopic:String
        private var mRingtone:Boolean=false

        fun setContext(context: Context)=apply {
            this.mContext=context
        }

        fun setSubscribeTopic(topic: String)=apply{
            this.mTopic=topic
        }

        fun isEnableRingtone(isEnable:Boolean)=apply{
            this.mRingtone=isEnable
        }

        fun init(): NotificationBuilder {
            return NotificationBuilder(
                mContext, mTopic, mRingtone
            )
        }
    }

    fun monitor(): NotificationMonitor = NotificationMonitorImp(mContext, mTopic, mRingtone)
}