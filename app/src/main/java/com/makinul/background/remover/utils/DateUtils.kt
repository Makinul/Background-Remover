package com.makinul.background.remover.utils

import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by MAKINUL on 03/09/2024.
 */
object DateUtils {
    private const val TAG = "DateUtils"

    private val calendar: Calendar
        get() = Calendar.getInstance()

    fun getCurrentDate(dateFormat: String): String {
        var date = ""
        val formatter = SimpleDateFormat(dateFormat, Locale.ENGLISH)
        date = formatter.format(calendar.time)
        return date
    }

    private fun getCalendar(date: Date): Calendar {
        val cal = calendar
        cal.time = date
        return cal
    }

    fun currentYear(): String {
        val calendar = calendar
        return calendar[Calendar.YEAR].toString()
    }


    fun todayYearMonthDate(): Long {
        val calendar = calendar
        val dayOfMonth = calendar[Calendar.DAY_OF_MONTH]
        val month = calendar[Calendar.MONTH]
        val year = calendar[Calendar.YEAR]
        return (year.toString() + twoDigitIntFormatter(month) + twoDigitIntFormatter(
            dayOfMonth
        )).toLong()
    }

    fun todayCalendar(): Calendar {
        return Calendar.getInstance()
    }

    fun todayDate(): Date {
        val calendar = calendar
        return calendar.time
    }

    fun nextMonthDate(): Date {
        val calendar = calendar
        calendar.add(Calendar.MONTH, 1)
        return calendar.time
    }

    val currentDate: Date
        get() = calendar.time

    fun getCurrentDate(currentTime: Long): Date {
        return getCalendar(Date(currentTime)).time
    }

    val currentDateWithMillisecond: Date
        get() = Calendar.getInstance().time

    private fun getFormattedDateString(date: Date, requestFor: String): String {
        return SimpleDateFormat(requestFor, Locale.ENGLISH).format(date)
    }

    const val voiceDateFormat1 = "dd-MM-yyyy"
    const val voiceDateFormat2 = "MM-dd-yyyy"
    const val voiceDateFormat13 = "dd-MM-yy"
    const val voiceDateFormat14 = "MM-dd-yy"
    const val voiceDateFormat3 = "MMMM dd yyyy"
    const val voiceDateFormat4 = "MMM dd yyyy"
    const val voiceDateFormat5 = "dd MMMM yyyy"
    const val voiceDateFormat6 = "dd MMM yyyy"
    const val voiceDateFormat7 = "MMMM dd yyyy"
    const val voiceDateFormat8 = "MMM dd yyyy"
    const val voiceDateFormat9 = "yyyy MMM dd"
    const val voiceDateFormat10 = "yyyy MMMM dd"
    const val voiceDateFormat11 = "yyyy dd MMM"
    const val voiceDateFormat12 = "yyyy dd MMMM"

    const val dateFormat1 = "dd-MM-yyyy"
    const val dateFormat2 = "MMMM dd, yyyy"
    const val dateFormat3 = "MMMM yyyy"
    const val dateFormat4 = "yyyy-MM-dd"
    const val dateFormat5 = "MMM dd, yyyy"
    const val dateFormat6 = "yyyyMMdd"
    const val dateFormat7 = "dd MMMM, yyyy"
    const val dateFormat8 = "MMMM dd yyyy"
    const val dateFormat9 = "dd MMMM yyyy"
    const val dateFormat10 = "yyyy-MM-dd'T'HH:mm:ss"// 'T'HH:mm:ss.SSSXXX
    const val dateFormat11 = "yyyyMM"
    const val dateFormat12 = "EEE d MMM, hh:mm aa"
    const val dateFormat13 = "d MMM, hh:mm aa"
    const val dateFormat14 = "EEE d MMM, yyyy hh:mm aa"
    const val dateFormat15 = "EEE dd MMM, yyyy"
    const val dateFormat16 = "dd/MM/yyyy"
    const val dateFormat17 = "yyyy/MM/dd"
    const val dateFormat18 = "EEE dd MMM"
    const val dateFormat19 = "dd MMMM, yyyy hh:mm aa"
    const val dateFormat20 = "MMM dd, yy"
    const val dateFormat21 = "MMM dd"
    const val dateFormat22 = "dd MMM yyyy"
    const val dateFormatBooking = "EEE d MMM, yyyy hh:mm aa"
    const val timeFormat1 = "hh:mm aa"
    const val dateFormatOD = "yyyy/MM/dd hh:mm"

    const val meetingDateTime = "d MMM, yyyy hh:mm aa" // 9 May, 2024 at 07:20 PM
    const val americanDateFormat = "MM-dd-yyyy"
    const val upcomingDateTime = "MMM dd, yyyy hh:mm aa"

    fun getDate(dateArray: Array<String>, requestBy: String): Date {
        val date = dateArray[0] + "-" + dateArray[1] + "-" + dateArray[2]
        return getDate(date, requestBy)
    }

    private fun getDate(dateAsString: String?, requestBy: String): Date {
        if (dateAsString == null) return todayDate()
        val simpleDateFormat = SimpleDateFormat(requestBy, Locale.ENGLISH)
        try {
            val date = simpleDateFormat.parse(dateAsString)
            if (date != null) {
                return date
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return Date(0)
    }

    fun getServerDate(
        dateAsString: String, requestBy: String
    ): Date? {
        val simpleDateFormat = SimpleDateFormat(requestBy, Locale.ENGLISH)

        return try {
            simpleDateFormat.parse(dateAsString)
        } catch (e: ParseException) {
//            e.printStackTrace()
            null
        }
    }

    fun getTimeZone(): String {
        val timeZone = TimeZone.getDefault()
//        val defaultTime = timeZone.getDisplayName(Locale.getDefault())
        return timeZone.getDisplayName(true, TimeZone.SHORT)

//        val now = Date()
//        val offset: Double = timeZone.getOffset(now.time) / 3600000.0
//        AppConstants.showLog(TAG)
    }

    fun isAssessmentExpired(meetingEndDate: String?): Boolean {
        val timeZone = TimeZone.getDefault()
        val endDate = getDate(meetingEndDate, serverFormat)

        val now = Date()
        val offset = timeZone.getOffset(now.time)

        val endTime = endDate.time + offset

        return now.time > endTime + SINGLE_HOUR
    }

    fun isAssessmentTimeNotReached(meetingStartDate: String?): Boolean {
        val timeZone = TimeZone.getDefault()
        val startDate = getDate(meetingStartDate, serverFormat)

        val now = Date()
        val offset = timeZone.getOffset(now.time)

        val startTime = startDate.time + offset

        return now.time < startTime - SINGLE_HOUR
    }

    fun getFormattedDateStringWithTimeZone(
        dateAsString: String?,
        requestBy: String,
        requestFor: String
    ): String {
        val timeZone = TimeZone.getDefault()
        val date = getDate(dateAsString, requestBy)

        val now = Date()
        val offset = timeZone.getOffset(now.time)

        val finalTime = date.time + offset
        return getFormattedDateString(Date(finalTime), requestFor)
    }

    fun getFormattedDateString(
        dateAsString: String?,
        requestBy: String,
        requestFor: String
    ): String {
        val simpleDateFormat = SimpleDateFormat(requestBy, Locale.ENGLISH)

        val date = try {
            if (dateAsString == null) {
                Date(0)
            } else {
                simpleDateFormat.parse(dateAsString)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            Date(0)
        }

        return if (date == null) {
            SimpleDateFormat(requestFor, Locale.ENGLISH).format(Date(0))
        } else {
            SimpleDateFormat(requestFor, Locale.ENGLISH).format(date)
        }
    }

    fun getFormattedDateString(
        calendar: Calendar, requestFor: String
    ): String {
        return SimpleDateFormat(requestFor, Locale.ENGLISH).format(calendar.time)
    }

    fun getCalendar(
        dateAsString: String, requestBy: String
    ): Calendar {
        val simpleDateFormat = SimpleDateFormat(requestBy, Locale.ENGLISH)
        return try {
            var date = simpleDateFormat.parse(dateAsString)
            if (date == null) date = Date(0)
            val calendar = Calendar.getInstance()
            calendar.time = date
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val min = calendar.get(Calendar.MINUTE)
            val sec = calendar.get(Calendar.SECOND)

            calendar.set(year, month, day, hour, min, sec)

            calendar
        } catch (e: ParseException) {
            val date = Date(0)
            val calendar = Calendar.getInstance()
            calendar.time = date

            calendar
        }
    }

    fun getYearMonth(calendar: Calendar): String {// month 0-11
        val month = calendar.get(Calendar.MONTH)
        val monthS = if (month <= 9) {
            "0$month"
        } else {
            month.toString()
        }
        return "" + calendar.get(Calendar.YEAR) + monthS
    }

    fun getCalendar(yearMonth: String): Calendar {
        val calendar = Calendar.getInstance()
        if (yearMonth.length == 6) {
            val year = yearMonth.substring(0, 4).toInt()
            val month = yearMonth.substring(4).toInt()

            calendar.set(year, month, 1)
        }

        return calendar
    }

    private const val SINGLE_HOUR = 1000 * 60 * 60
    private const val SINGLE_DAY = SINGLE_HOUR * 24

    fun differenceBetweenToday(selectedDateTimestamp: Long): Long {
        val differenceTime = selectedDateTimestamp - System.currentTimeMillis();
        return (differenceTime / SINGLE_DAY);
    }

    fun getStartDateTime(): String {
        val calendar = Calendar.getInstance()
//        calendar.add(Calendar.DAY_OF_MONTH, 15)
        return SimpleDateFormat(dateFormatBooking, Locale.ENGLISH).format(calendar.time)
    }

    fun getEndDateTime(): String {
        val calendar = Calendar.getInstance()
//        calendar.add(Calendar.DAY_OF_MONTH, 15)
        return SimpleDateFormat(dateFormatBooking, Locale.ENGLISH).format(calendar.time)
    }

    fun getDateTime(calendar: Calendar): String {
        return SimpleDateFormat(dateFormatBooking, Locale.ENGLISH).format(calendar.time)
    }

    fun getBookingTimeDate(dateTime: String): Date {
        val simpleDateFormat = SimpleDateFormat(dateFormatBooking, Locale.ENGLISH)

        val date = try {
            simpleDateFormat.parse(dateTime)
        } catch (e: ParseException) {
            e.printStackTrace()
            Date(0)
        }

        return date
    }

    const val am = "AM"
    const val pm = "PM"

    fun getHourMinFromTime(time: String): IntArray {
        val hourMin = intArrayOf(0, 0)
        if (time.contains(":")) {
            val part = time.split(":".toRegex()).toTypedArray()
            if (part.isNotEmpty()) {
                hourMin[0] = part[0].toInt()
                if (part.size > 1 && part[1].contains(" ")) {
                    val part1 = part[1].split(" ".toRegex()).toTypedArray()
                    if (part1.isNotEmpty()) {
                        hourMin[1] = part1[0].toInt()
                        if (part1.size > 1) {
                            if (part1[1].equals(pm, ignoreCase = true)) {
                                hourMin[0] = hourMin[0] + 12
                            } else {
                                if (hourMin[0] == 12) {
                                    hourMin[0] = 0
                                }
                            }
                        }
                    }
                }
            }
        }
        return hourMin
    }

    @Throws(NumberFormatException::class)
    fun getTime12Hr(time24Hr: String): String {
        var hr = 0
        var min = 0

        if (time24Hr.contains(":")) {
            val part = time24Hr.split(":".toRegex()).toTypedArray()
            if (part.isNotEmpty()) {
                hr = part[0].toInt()
                if (part.size == 2) {
                    min = part[1].toInt()
                }
            }
        }
        return getTimeFromHourMin(hr, min)
    }

    @Throws(NumberFormatException::class)
    fun getTimeFromHourMin(hour: Int, min: Int): String {
        val isAm: Boolean = hour < 12
        val nHour = hour % 12
        val aHour = if (nHour < 10) {
            if (nHour == 0) {
                "12"
            } else {
                "0$nHour"
            }
        } else {
            nHour.toString()
        }
        val nMin = if (min < 10) {
            "0$min"
        } else {
            min.toString()
        }
        return if (isAm) {
            "$aHour:$nMin $am"
        } else {
            "$aHour:$nMin $pm"
        }
    }

    fun getTimeFromHourMin(hour: Int, min: Int, am_pm: Int): String {
        val nHour = if (hour < 10) {
            if (hour == 0) {
                "12"
            } else {
                "0$hour"
            }
        } else {
            hour.toString()
        }
        val nMin = if (min < 10) {
            "0$min"
        } else {
            min.toString()
        }
        return if (am_pm == 0) {
            "$nHour:$nMin $am"
        } else {
            "$nHour:$nMin $pm"
        }
    }

    @Throws(NumberFormatException::class)
    fun getTime24Hr(time: String): String {
        var hour = 0
        var min = 0
        if (time.contains(":")) {
            val part = time.split(":".toRegex()).toTypedArray()
            if (part.isNotEmpty()) {
                hour = part[0].toInt()
                if (part.size > 1 && part[1].contains(" ")) {
                    val part1 = part[1].split(" ".toRegex()).toTypedArray()
                    if (part1.isNotEmpty()) {
                        min = part1[0].toInt()
                        if (part1.size > 1) {
                            if (part1[1].equals(pm, ignoreCase = true)) {
                                if (hour != 12) {
                                    hour += 12
                                }
                            }
                        }
                    }
                }
            }
        }

        return if (hour < 10) {
            if (min < 10) {
                "0$hour:0$min"
            } else {
                "0$hour:$min"
            }
        } else {
            if (min < 10) {
                "$hour:0$min"
            } else {
                "$hour:$min"
            }
        }
    }

    fun concatTimeWithDate(
        date: String, dateFormat: String, time: String?
    ): Date { // time must 24 hour format
        val d = getDate(date, dateFormat)
        if (time.isNullOrEmpty()) return d

        var hour = 0
        var min = 0
        if (time.contains(":")) {
            val part = time.split(":".toRegex()).toTypedArray()
            if (part.isNotEmpty()) {
                hour = part[0].toInt()
                if (part.size >= 2) {
                    min = part[1].toInt()
                }
            }
        }

        val currentCalendar = Calendar.getInstance()
        currentCalendar.time = d
        currentCalendar.set(
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH),
            hour,
            min
        )
        return currentCalendar.time
    }

    fun getTimeInMillSecond(
        dateAsString: String, requestBy: String
    ): Long {
        val simpleDateFormat = SimpleDateFormat(requestBy, Locale.ENGLISH)
        return try {
            val date = simpleDateFormat.parse(dateAsString)
            date?.time ?: 0
        } catch (e: ParseException) {
            0
        }
    }

    @Throws(NumberFormatException::class)
    fun differenceBetweenTime(graceTime: String, inTime: String): String {
        var iHour = 0
        var iMin = 0
        if (inTime.contains(":")) {
            val part = inTime.split(":".toRegex()).toTypedArray()
            if (part.isNotEmpty()) {
                iHour = part[0].toInt()
                if (part.size == 2) {
                    iMin = part[1].toInt()
                }
            }
        }
        if (iHour == 0 && iMin == 0) {
            return "Error"
        }

        var gHour = 0
        var gMin = 0

        if (graceTime.contains(":")) {
            val part = graceTime.split(":".toRegex()).toTypedArray()
            if (part.isNotEmpty()) {
                gHour = part[0].toInt()
                if (part.size == 2) {
                    gMin = part[1].toInt()
                }
            }
        }

        var dM = iMin - gMin
        var dH = iHour - gHour
        if (dM < 0) {
            dM += 60
            dH -= 1
        }

        val diff = if (dH > 0) {
            if (dM > 0) {
                "$dH Hour $dM Minute"
            } else {
                "$dH Hour"
            }
        } else {
            "$dM Minute"
        }
        return diff
    }

    private const val feedOutputFormat = "EEE, dd MMM yyyy"
    const val serverFormat = "yyyy-MM-dd'T'HH:mm:ss" // 'T'HH:mm:ss.SSSXXX 2022-01-25T11:12:05.81

    fun getFeedTime(dateAsString: String?): String {
        val simpleDateFormat = SimpleDateFormat(serverFormat, Locale.ENGLISH)
        val date: Date = if (dateAsString == null) {
            Date(0)
        } else {
            try {
                simpleDateFormat.parse(dateAsString) ?: Date(0)
            } catch (e: ParseException) {
                e.printStackTrace()
                Date(0)
            }
        }
        return getFeedTime(date)
    }

    private fun getFeedTime(date: Date): String {
        val currentTime = System.currentTimeMillis()
        val serverTime = date.time
        val difference = currentTime - serverTime

        val oneMin = 1000 * 60
        val oneHour = oneMin * 60
        val oneDay = oneHour * 24

        return if (difference < 2 * oneMin) {
            "Just now"
        } else if (difference < oneHour) {
            (difference / oneMin).toString() + " min ago"
        } else if (difference < oneDay) {
            (difference / oneHour).toString() + " hr ago"
        } else if (difference < 2 * oneDay) {
            (difference / oneDay).toString() + " day ago"
        } else if (difference < 10 * oneDay) {
            (difference / oneDay).toString() + " days ago"
        } else {
            SimpleDateFormat(feedOutputFormat, Locale.ENGLISH).format(date)
        }
    }

    fun twoDigitIntFormatter(number: Int): String? {
        val formatter = NumberFormat.getInstance(Locale.ENGLISH) as DecimalFormat
        formatter.applyPattern("00")
        return formatter.format(number.toLong())
    }

    fun isUnderNewRecentInvitations(createdOn: String?): Boolean {
        serverFormat
        val simpleDateFormat = SimpleDateFormat(serverFormat, Locale.ENGLISH)
        return if (createdOn == null) {
            false
        } else {
            try {
                val date = simpleDateFormat.parse(createdOn)
                if (date == null) {
                    false
                } else {
                    val currentTime = System.currentTimeMillis()
                    val serverTime = date.time
                    val difference = currentTime - serverTime

                    // 1 day or 24 hour
                    difference <= SINGLE_DAY
                }
            } catch (e: ParseException) {
                e.printStackTrace()
                false
            }
        }
    }
}