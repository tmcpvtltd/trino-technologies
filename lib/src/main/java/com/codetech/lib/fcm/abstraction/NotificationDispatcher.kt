package com.codetech.lib.fcm.abstraction

import android.content.Context




class NotificationDispatcher {

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

        fun build(): NotificationMonitor {
            if (!::mContext.isInitialized) throw IllegalStateException("Context is not initialized must be call before build")
            if (!::mTopic.isInitialized) throw IllegalStateException("Notification Subscribe Topic is not initialized must be call before build")
            return NotificationBuilder.Builder()
                .setContext(mContext)
                .setSubscribeTopic(mTopic)
                .isEnableRingtone(mRingtone)
                .init()
                .monitor()
        }
    }
}
