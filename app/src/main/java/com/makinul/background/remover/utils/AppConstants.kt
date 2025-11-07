package com.makinul.background.remover.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.makinul.background.remover.data.model.Angle
import com.makinul.background.remover.data.model.Point
import java.io.File
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

object AppConstants {
    const val KEY_IMAGE_RESOURCE: String = "image_resource"
    const val KEY_BITMAP_IMAGE: String = "bitmap_image"

    const val KEY_IMAGE_TYPE: String = "image_type"
    const val KEY_IMAGE_TYPE_URI: String = "image_type_uri"
    const val KEY_IMAGE_TYPE_ASSET: String = "image_type_asset"
    const val KEY_IMAGE_PATH: String = "image_path"

    fun getAssetBitmap(context: Context?, filePath: String?): Bitmap? {
        if (context == null || filePath == null) return null
        val assetManager = context.assets
        val inputStream = assetManager.open(filePath)
        return BitmapFactory.decodeStream(inputStream)
    }

    fun getUriBitmap(context: Context?, uriPath: String?): Bitmap? {
        if (context == null || uriPath == null) return null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(
                context.contentResolver, Uri.parse(uriPath)
            )
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(
                context.contentResolver, Uri.parse(uriPath)
            )
        }.copy(Bitmap.Config.ARGB_8888, true)?.let { bitmap ->
            return bitmap
        }

        return null
    }

    val listOfDemoImagesPath = listOf(
        "images/demo_image_0.png",
        "images/demo_image_1.jpg",
        "images/demo_image_2.jpg",
        "images/demo_image_3.jpg",
        "images/demo_image_4.jpg"
    )

    fun getDistance(pointA: Point, pointB: Point): Float {
        return sqrt(((pointB.x - pointA.x) * (pointB.x - pointA.x)) + ((pointB.y - pointA.y) * (pointB.y - pointA.y)))
    }

    val RECORD_AUDIO_PERMISSION =
        mutableListOf(
            android.Manifest.permission.RECORD_AUDIO
        ).toTypedArray()

    const val FIRST_NAME_QUESTION_ID = 1
    const val LAST_NAME_QUESTION_ID = 2
    const val DOB_QUESTION_ID = 6
    const val HEIGHT_QUESTION_ID = 4
    const val WEIGHT_QUESTION_ID = 5
    const val GENDER_QUESTION_ID = 8
    const val ETHNIC_QUESTION_ID = 10000023
    const val DOMINANT_QUESTION_ID = 14
    const val OCCUPATION_QUESTION_ID = 403

    const val GENDER_MALE: String = "Male"
    const val GENDER_FEMALE: String = "Female"
    const val DOMINANT_HAND_RIGHT: String = "Right"
    const val DOMINANT_HAND_LEFT: String = "Left"

    const val KEY_FROM_REGISTRATION = "fromRegistration"
    const val KEY_START_ASSESSMENT: String = "startAssessment"
    const val KEY_ASSESSMENT: String = "assessment"
    const val KEY_ASSESSMENT_FROM_INVITATION: String = "assessmentFromInvitation"
    const val KEY_NEED_TO_RESUME_BOT: String = "needToResumeBot"
    const val KEY_OPEN_REPORT: String = "openReport"

    const val RATE_YOUR_LEVEL_OF_PAIN: String = "RATE YOUR LEVEL OF PAIN"
    const val DESCRIBE_YOUR_PAIN_AT_ITS_WORST_FREQUENCY =
        "DESCRIBE YOUR PAIN AT ITS WORST- FREQUENCY"

    val PAIN_LEVEL_QUESTION_IDS = arrayListOf(1394, 10001442)

    const val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$"

    const val DUMMY_TEST_ID: String = "5021"
    const val STATUS_OK = 200
    const val UNABLE_TO_RESOLVE_HOST = "Unable to resolve host"
    const val NO_INTERNET = "No Internet"
    const val UNAUTHORIZED_MESSAGE = "Unauthorized"
    const val UNAUTHORIZED_CODE = 401
    const val BEARER = "Bearer"

    const val REQUEST_FOR_HOME_DASHBOARD: Int = 701
    const val REQUEST_FOR_ASSESSMENTS: Int = 702

    const val BODY_REGION_POSTURE: Int = 5

    const val EMAIL_VERIFICATION_INVITED = 2
    const val EMAIL_VERIFICATION_NOT_FOUND = 3
    const val EMAIL_VERIFICATION_ALREADY_EXISTS = 1

    const val HOME_ACTION_MENU_ICON_NOTIFICATION: Int = 1
    const val HOME_ACTION_MENU_ICON_VOICE_INSTRUCTION_STATE: Int = 2
    const val HOME_ACTION_MENU_ICON_INFO: Int = 3
    const val HOME_ACTION_MENU_ICON_REFRESH: Int = 100

    const val ASSESSMENT_TYPE_SELF = 1
    const val ASSESSMENT_TYPE_TELEMEDICINE = 3
    const val ASSESSMENT_TYPE_IN_CLINIC = 4

    const val ASSESSMENT_LIST_PAGE_SIZE = 50

    const val KEY_POSITION = "position"
    const val KEY_POSE = "pose"
    const val KEY_NEED_TO_START_DEMOGRAPHIC = "needToStartDemographic"
    const val KEY_RENDERED_FOLDER = "rendered"
    const val KEY_RAW_FILE_PATH = "rawFilePath"
    const val KEY_RENDERED_FILE_PATH = "renderedFilePath"
    const val KEY_VIDEO_FILE_PATH = "videoFilePath"
    const val KEY_RAW = "raw"
    const val KEY_JPG = ".jpg"
    const val KEY_MP4 = ".mp4"
    const val KEY_TXT = ".txt"
    const val KEY_PDF = ".pdf"

    const val KEY_CAMERA_COMPATIBILITY = "cameraCompatibility"
    const val DEFAULT_PROVIDER_NAME = "My Medical Hub"
    const val DEFAULT_ASSESSMENT_NAME = "Self"

    const val KEY_BIOMETRIC = "biometric"
    const val KEY_BIOMETRIC_LIST = "biometricList"

    const val KEY_BIOMETRIC_COMPLETED = "completed"
    const val KEY_BIOMETRIC_HUMAN_EXPERT = "human-expert"
    const val KEY_BIOMETRIC_RETAKE = "retake"
    const val KEY_BIOMETRIC_SKIPPED = "skipped"
    const val KEY_BIOMETRIC_PROCESSING = "processing"
    const val KEY_BIOMETRIC_WAITING = "waiting"
    const val POSTURE_SIDE_VIEW_CODE_NAME: String = "postureAnalysis"
    const val KEY_IS_VIEW_MODE: String = "isViewMode"

    const val COMMA_SEPARATOR = ","

    var SLIDE_ANIMATION_DURATION_START = 400
    var SLIDE_ANIMATION_DURATION_END = 200

    const val BIOMETRIC_MOVEMENT_REFRESH_TIME: Long = 15000

    const val KEY_QUESTION_TYPE_SINGLE_CHOICE = "BUTTON"
    const val KEY_QUESTION_TYPE_MULTIPLE_CHOICE = "CHECKBOX"
    const val KEY_QUESTION_TYPE_TEXT_FIELD = "TEXT"
    const val KEY_QUESTION_TYPE_DROPDOWN = "DROPDOWN"

    const val TEXT_TO_VOICE_UTTERANCE_ID_TRY_AGAIN = "tryAgain"
    const val TEXT_TO_VOICE_UTTERANCE_ID_WARNING_MESSAGE = "warningMessage"

    const val KEY_ASSESSMENT_GROUP: String = "assessmentGroup"
    const val KEY_ASSESSMENT_DETAILS: String = "assessmentDetails"

    fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) {
            val fileList = fileOrDirectory.listFiles()
            if (!fileList.isNullOrEmpty())
                for (child in fileList) {
                    deleteRecursive(child)
                }
        }

        fileOrDirectory.delete()
    }

    fun getAngleInDegree(angle: Angle?, clockWise: Boolean = false): Float {
        if (angle == null)
            return -1.0f
        return getAngleInDegree(angle.pointA, angle.pointB, angle.pointC, clockWise)
    }

    fun getAngleInDegree(
        startPoint: Point,
        middlePoint: Point = Point(0f, 0f),
        endPoint: Point = Point(1f, 0f),
        clockWise: Boolean = false
    ): Float {
        if ((middlePoint != Point(0f, 0f)) && (endPoint != Point(1f, 0f))) {
            val vectorBA = Point(startPoint.x - middlePoint.x, startPoint.y - middlePoint.y)
            val vectorBC = Point(endPoint.x - middlePoint.x, endPoint.y - middlePoint.y)
            val vectorBAAngle = getAngleInDegree(vectorBA)
            val vectorBCAngle = getAngleInDegree(vectorBC)
            var angleValue = if (vectorBAAngle > vectorBCAngle) {
                vectorBAAngle - vectorBCAngle
            } else {
                360 + vectorBAAngle - vectorBCAngle
            }
            if (clockWise) {
                angleValue = 360 - angleValue
            }
            return angleValue
        } else {
            val x = startPoint.x
            val y = startPoint.y
            val magnitude = sqrt((x * x + y * y).toDouble())
            var angleValue = if (magnitude >= 0.0001) {
                acos(x / magnitude)
            } else {
                0
            }
            angleValue = Math.toDegrees(angleValue.toDouble())
            if (y < 0) {
                angleValue = 360 - angleValue
            }
            return angleValue.toFloat()
        }
    }

    fun findDummyPointParallelToYAxis(pointA: Point, pointB: Point): Point {
        val distance = sqrt(
            (pointA.x - pointB.x).pow(2) +
                    (pointA.y - pointB.y).pow(2)
        )
        val pointCy = pointB.y + distance
        val pointCx = pointB.x

        return Point(pointCx, pointCy)
    }

    fun getPoint(
        landmark: NormalizedLandmark,
        imageWidth: Int,
        imageHeight: Int,
        scaleFactor: Float
    ): Point {
        val x = landmark.x() * imageWidth * scaleFactor
        val y = landmark.y() * imageHeight * scaleFactor
        val z = landmark.z() * imageWidth * scaleFactor
        return Point(x, y, z)
    }

    fun findPointC(A: Point, B: Point, d: Double = 5.0): Point {
        // Calculate the vector from A to B (AB)
        val AB = Point(B.x - A.x, B.y - A.y)

        // Calculate the length of AB
        val lengthAB = sqrt(AB.x * AB.x + AB.y * AB.y)

        // Calculate the unit vector of AB
        val unitVectorAB = Point(AB.x / lengthAB, AB.y / lengthAB)

        // Calculate point C
        val C = Point(B.x + lengthAB * unitVectorAB.x, B.y + lengthAB * unitVectorAB.y)

        return C
    }

    fun getLinePaint(): Paint {
        val paint = Paint()

        paint.strokeWidth = 5f
        paint.color = Color.RED

        return paint
    }

    fun getDottedLinePaint(): Paint {
        val paint = Paint()

        paint.strokeWidth = 5f
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.pathEffect = DashPathEffect(floatArrayOf(10f, 20f), 0f)

        return paint
    }

    fun getPointPaint(): Paint {
        val paint = Paint()

        paint.strokeWidth = 10f
        paint.color = Color.RED

        return paint
    }

    fun getAnglePaint(): Paint {
        val paint = Paint()

        paint.strokeWidth = 5f
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE

        return paint
    }

    //Dashboard item id
    const val PENDING = 0
    const val NOT_SUBMITTED = 19
    const val RETURNED_BY_TECHNICAL_MANAGER = 21
    const val RETURNED_BY_QUALITY_MANAGER = -1
    const val WAITING_FOR_APPROVAL = -1

    var selectedPhaseId: Int = -1
    var userId: Int = -1
    var userEmail: String? = null
    var userFirstName: String? = null
    var userLastName: String? = null
    var userFullName: String? = null
    var isUserLoggedIn: Boolean = false
    var token: String? = null
    var selectedTenantId: Int = -1
    var selectedTenantName: String? = null

    const val LOADING = "Loading"

    //key for save user data to shared preferences
    const val KEY_USER_INVITATION_ID = "userInvitationId"
    const val KEY_USER_INVITATION_EMAIL_ADDRESS = "userInvitationEmailAddress"
    const val KEY_USER_INVITATION_FIRST_NAME = "userInvitationFirstName"
    const val KEY_USER_INVITATION_LAST_NAME = "userInvitationLastName"
    const val KEY_USER_INVITATION_VERIFY_STATUS = "userInvitationVerifyStatus"
    const val KEY_USER_INVITATION_VERIFY_STATUS_TEXT = "userInvitationVerifyStatusText"
    const val KEY_USER_INVITATION_REFER_TYPE = "userInvitationReferType"
    const val KEY_USER_INVITATION_REFER_TYPE_TEXT = "userInvitationReferTypeText"
    const val KEY_USER_INVITATION_TENANT = "userInvitationTenant"

    const val KEY_IS_USER_LOGGED_IN = "isUserLoggedIn"
    const val KEY_USER_ID = "userId"
    const val KEY_USER_EMAIL = "userEmail"
    const val KEY_USER_NAME = "userName"
    const val KEY_USER_FULL_NAME = "userFullName"
    const val KEY_USER_LAST_NAME = "userLastName"
    const val KEY_USER_FIRST_NAME = "userFirstName"
    const val KEY_USER_GENDER = "userGender"
    const val KEY_USER_TOKEN = "userToken"
    const val KEY_REFRESH_TOKEN = "RefreshToken"
    const val KEY_USER_ROLE_NAME = "userRoleName"
    const val KEY_USER_IS_REFERRED = "userIsReferred"
    const val KEY_USER_HAS_DOCTOR = "userHasDoctor"
    const val KEY_USER_TIME_ZONE = "timeZone"
    const val KEY_USER_HEIGHT = "userHeight"
    const val KEY_USER_WEIGHT = "userWeight"
    const val KEY_USER_DATE_OF_BIRTH = "userDateOfBirth"
    const val KEY_USER_AGE = "userAge"
    const val KEY_USER_IS_SUPER_ADMIN = "userIsSuperAdmin"
    const val KEY_PATIENT_ID = "patientId"
    const val KEY_TITLE = "title"
    const val KEY_QUESTION_DATA_FIRST_TIME_LOADED = "isQuestionDataFirstTimeLoaded"
    const val KEY_IS_INSTRUCTION_VOICE_ACTIVE = "isInstructionVoiceActive"
    const val KEY_USER_DOMINANT_HAND = "userDominantHand"
    const val KEY_USER_ETHNIC_REGION = "userEthnicRegion"
    const val KEY_USER_OCCUPATION = "userOccupation"
    const val KEY_IS_DEMOGRAPHIC_DATA_PREPARED = "isDemographicDataPrepared"
    const val KEY_IS_CHIEF_COMPLAINT_ADDED = "isChiefComplaintAdded"
    const val KEY_TEST_ID = "testId"
    const val KEY_BODY_REGION_ID_LIST = "bodyRegionIdList"
    const val KEY_AUTHORIZATION = "Authorization"
    const val KEY_GROUP_ID = "groupId"
    const val KEY_READ_MODE_ONLY = "readModeOnly"

    const val TENANT_VATB = "vatb"
    const val TENANT_EMMA = "emma"
    const val KEY_SELECTED_TENANT_ID = "selectedTenantId"
    const val KEY_SELECTED_TENANT_NAME = "selectedTenantName"

    fun showLog(tag: String, message: String = "Test message") {
        Log.v(tag, message)
    }

//    fun showCurrentVersion(): String {
//        return if (BuildConfig.DEBUG) {
//            "${BuildConfig.VERSION_NAME} (v${BuildConfig.VERSION_CODE})"
//        } else {
//            BuildConfig.VERSION_NAME
//        }
//    }

    const val INDEX_DEMOGRAPHICS = 0
    const val INDEX_ADL = 1
    const val INDEX_MEDICAL_HISTORY = 2
    const val INDEX_CHIEF_COMPLAINTS = 3
    const val INDEX_BIOMETRIC = 4
    const val INDEX_TELEMEDICINE = 5
    const val INDEX_OUTCOME_ASSESSMENT = 6

    const val KEY_ID_HEADER = -1
    const val KEY_ID_DEMOGRAPHICS = 1
    const val KEY_ID_MEDICAL_HISTORY = 2
    const val KEY_ID_ADL = 3
    const val KEY_ID_CHIEF_COMPLAINTS = 4
    const val KEY_ID_BIOMETRIC = 5
    const val KEY_ID_TELEMEDICINE = 6
    const val KEY_ID_OUTCOME_ASSESSMENT = 8
    const val KEY_ID_IS_REPORT_READY = 200
    const val KEY_ID_HAS_REPORT_URL = 201
    const val KEY_ID_FOOTER = -100

    const val ORDER_HEADER = 0
    const val ORDER_DEMOGRAPHICS = 1
    const val ORDER_ADL = 2
    const val ORDER_MEDICAL_HISTORY = 3
    const val ORDER_CHIEF_COMPLAINTS = 4
    const val ORDER_OUTCOME_ASSESSMENT = 5
    const val ORDER_BIOMETRIC = 6
    const val ORDER_TELEMEDICINE = 6
    const val ORDER_FOOTER = 1000

    fun getTimeLeft(speedInBPerMs: Float, progressPercent: Int, lengthInBytes: Long): String {
        val speedInBPerSecond = speedInBPerMs * 1000
        val bytesLeft = (lengthInBytes * (100 - progressPercent) / 100).toFloat()

        val secondsLeft = bytesLeft / speedInBPerSecond
        val minutesLeft = secondsLeft / 60f
        val hoursLeft = minutesLeft / 60f

//        return when {
//            secondsLeft < 60 -> "%.0f s left".format(secondsLeft)
//            minutesLeft < 3 -> "%.0f mins %.0f s left".format(minutesLeft, secondsLeft % 60)
//            minutesLeft < 60 -> "%.0f mins left".format(minutesLeft)
//            minutesLeft < 300 -> "%.0f hrs and %.0f mins left".format(hoursLeft, minutesLeft % 60)
//            else -> "%.0f hrs left".format(hoursLeft)
//        }

        return when {
            hoursLeft >= 1 -> "%.0f hr".format(hoursLeft)
            minutesLeft < 60 && minutesLeft >= 1 -> "%.0f min".format(minutesLeft)
            secondsLeft < 60 && secondsLeft > 0 -> "%.0f sec".format(secondsLeft)
            else -> "0 sec"
        }
    }

    fun getSpeed(speedInBPerMs: Float): String {
        var value = speedInBPerMs * 1000
        val units = arrayOf("b/s", "kb/s", "mb/s", "gb/s")
        var unitIndex = 0

        while (value >= 500 && unitIndex < units.size - 1) {
            value /= 1024
            unitIndex++
        }

        return "%.2f %s".format(value, units[unitIndex])
    }

    fun getTotalLength(lengthInBytes: Long): String {
        var value = lengthInBytes.toFloat()
        val units = arrayOf("b", "kb", "mb", "gb")
        var unitIndex = 0

        while (value >= 500 && unitIndex < units.size - 1) {
            value /= 1024
            unitIndex++
        }

        return "%.2f %s".format(value, units[unitIndex])
    }

//    ChiefComplainPoint(
//    positionX = 170.0,
//    positionY = 30.0,
//    positionName = "Head Front",
//    frontOrBack = 1,
//    positionKeyName = "head",
//    painValue = -100
//    ),
//    ChiefComplainPoint(
//    positionX = 187.0,
//    positionY = 30.0,
//    positionName = "Head Right",
//    frontOrBack = 1,
//    positionKeyName = "head_right",
//    painValue = -100
//    ),
//    ChiefComplainPoint(
//    positionX = 202.0,
//    positionY = 30.0,
//    positionName = "Head Left",
//    frontOrBack = 1,
//    positionKeyName = "head_left",
//    painValue = -100
//    ),

//    fun getFormattedHeight(height: Double): Array<Int> {
//        val ftAnswer: Int
//        val inAnswer: Int
//
//        if (height <= 0) {
//            ftAnswer = 0
//            inAnswer = 0
//        } else {
//            ftAnswer = (height / 12).toInt()
//            inAnswer = (height % 12).toInt()
//        }
//
//        return arrayOf(ftAnswer, inAnswer)
//    }

    fun getFormattedHeight(height: Double): String {
        val ftAnswer: String
        val inAnswer: String

        if (height <= 0) {
            ftAnswer = "0ft"
            inAnswer = "0in"
        } else {
            ftAnswer = "${(height / 12).toInt()}ft"
            inAnswer = "${(height % 12).toInt()}in"
        }

        return "$ftAnswer $inAnswer"
    }

    fun getFormattedWeight(weight: Double): String {
        return if (weight <= 0) {
            "0 lbs"
        } else {
            "${weight.toInt()} lbs"
        }
    }

    fun getColorFrom(serverString: String?): Int? {
        if (serverString == null)
            return null

        val color = serverString
            .replace("rgb", "")
            .replace("(", "")
            .replace(")", "")

        if (color.contains(COMMA_SEPARATOR)) {
            val part = color.split(COMMA_SEPARATOR)
            if (part.size >= 3) {
                try {
                    val r = part[0].trim().toInt()
                    val g = part[1].trim().toInt()
                    val b = part[2].trim().toInt()
                    return Color.rgb(r, g, b)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }
}