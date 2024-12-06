package com.codetech.lib.helper

object NotificationDataSetup{
    private var isRingtoneEnabled = false


    fun setRingtone(isEnable:Boolean){
        isRingtoneEnabled =isEnable
    }

    fun getRingtone():Boolean= isRingtoneEnabled
}