package com.simplemobiletools.clock.models

data class Alarm(var id: Int, var timeInMinutes: Int, var days: Int, var isEnabled: Boolean, var vibrate: Boolean, var soundTitle: String,
                 var soundUri: String, var pillName: String, var pillDescription: String, var image: String, var date :String,var snoozeTime: Int)
