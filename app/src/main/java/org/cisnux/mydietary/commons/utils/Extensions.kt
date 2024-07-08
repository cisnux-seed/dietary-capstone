package org.cisnux.mydietary.commons.utils

import android.content.Context
import android.content.ContextWrapper
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.cisnux.mydietary.data.remotes.bodyrequests.UserAccountBodyRequest
import org.cisnux.mydietary.domain.models.UserAccount
import org.cisnux.mydietary.domain.models.EditableUserProfile
import org.cisnux.mydietary.presentation.addmyprofile.MyProfile
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

val Instant.age: Int
    get() {
        val clock = Clock.fixed(this, ZoneId.systemDefault())
        val birthDate = LocalDate.now(clock)
        val today = LocalDate.now()
        return Period.between(birthDate, today).years
    }

val Context.activity: AppCompatActivity?
    get() {
        var currentContext = this
        while (currentContext is ContextWrapper) {
            if (currentContext is AppCompatActivity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        return null
    }

val Context.isDevModeActive: Boolean
    get() = Settings.Secure.getInt(
        this.contentResolver,
        Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
        0
    ) != 0

fun String.isResetCodeValid(): Boolean = this.trim().length == 6

val MyProfile.asEditableUserProfile: EditableUserProfile
    get() = EditableUserProfile(
        username = username,
        age = age.toInt(),
        weight = weight.toFloat(),
        height = height.toFloat(),
        gender = gender,
        goal = goal,
        weightTarget = weightTarget.toFloat(),
        activityLevel = activityLevel,
        waistCircumference = waistCircumference.toFloat()
    )

val UserAccount.userAccountBodyRequest
    get() = UserAccountBodyRequest(
        emailAddress = emailAddress, password = password
    )

fun String.isEmailValid(): Boolean {
    val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\$"
    return Regex(emailRegex).matches(this)
}

fun String.isPasswordSecure(): Boolean =
    Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!%*#?&]{8,}$").matches(this)

fun String.isUsernameValid(): Boolean {
    val usernameRegex = "^[a-z0-9_]{6,20}\$"
    return Regex(usernameRegex).matches(this)
}

fun String.isAgeValid(): Boolean = try {
    val value = this.toInt()
    value >= 18
} catch (e: NumberFormatException) {
    false
}

fun String.isFloatAndGreaterThanZero(): Boolean = try {
    val number = this.toFloat()
    number > 0
} catch (e: NumberFormatException) {
    false
}

fun String.isFloatAndGreaterAndEqualToZero(): Boolean = try {
    val value = this.toFloat()
    value >= 0
} catch (e: NumberFormatException) {
    false
}

val Int.foodDiaryCategory: FoodDiaryCategory
    get() = when (this) {
        0 -> FoodDiaryCategory.BREAKFAST
        1 -> FoodDiaryCategory.LUNCH
        else -> FoodDiaryCategory.DINNER
    }

val Int.reportCategory: ReportCategory
    get() = when (this) {
        0 -> ReportCategory.WEEKLY
        else -> ReportCategory.MONTHLY
    }

val String.asInstant: Instant
    get() = DateTimeFormatter.ISO_OFFSET_DATE_TIME.run {
        // Convert LocalDateTime to milliseconds
        LocalDateTime.parse(this@asInstant, this).atZone(ZoneId.systemDefault()).toInstant()
    }

val String.fromDateMonthYearToDateAndMonth: String
    get() =
        DateTimeFormatter.ofPattern(DATE_MONTH_YEAR).run {
            val locale = Locale.getDefault()
            LocalDate.parse(this@fromDateMonthYearToDateAndMonth, this@run)
                .format(DateTimeFormatter.ofPattern(FULL_DATE_MONTH, locale))
        }

val String.fromDateMonthYearToDayDateMonth: String
    get() = DateTimeFormatter.ofPattern(DATE_MONTH_YEAR).run {
        val locale = Locale.getDefault()
        LocalDate.parse(this@fromDateMonthYearToDayDateMonth, this@run)
            .format(DateTimeFormatter.ofPattern(DAY_DATE_MONTH, locale))
    }


val String.fromDateMonthYearToDay: String
    get() =
        DateTimeFormatter.ofPattern(DATE_MONTH_YEAR).run {
            val locale = Locale.getDefault()
            LocalDate.parse(this@fromDateMonthYearToDay, this@run)
                .format(DateTimeFormatter.ofPattern("EE", locale))
        }
val String.fromIsoOffsetDateTimeToDayDateMonth: String
    get() = DateTimeFormatter.ISO_OFFSET_DATE_TIME.run {
        val locale = Locale.getDefault()
        LocalDate.parse(this@fromIsoOffsetDateTimeToDayDateMonth, this@run)
            .format(DateTimeFormatter.ofPattern(DAY_DATE_MONTH, locale))
    }

val Instant.fromMillisToDayDateMonthYear: String
    get() {
        val locale = Locale.getDefault()
        val clock = Clock.fixed(this, ZoneId.systemDefault())
        val localDate = LocalDate.now(clock)
        return DateTimeFormatter.ofPattern(DAY_DATE_MONTH_YEAR, locale).format(localDate)
    }

val Instant.fromMillisToHoursAndMinutes: String
    get() {
        val locale = Locale.getDefault()
        val clock = Clock.fixed(this, ZoneId.systemDefault())
        val localTime = LocalTime.now(clock)
        return DateTimeFormatter.ofPattern(HOURS_AND_MINUTES, locale).format(localTime)
    }

val currentLocalDateTime: String
    get() =
        DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now(Clock.systemDefaultZone()))

val Instant.fromMillisToIsoLocalDate: String
    get() {
        // Convert current time to Instant
        val currentInstant = Clock.fixed(this, ZoneId.systemDefault())

        // Format Instant to custom Date Time Format without the offset
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE

        return formatter.format(LocalDateTime.now(currentInstant))
    }

inline fun View.afterMeasured(crossinline block: () -> Unit) {
    if (measuredWidth > 0 && measuredHeight > 0) block()
    else viewTreeObserver.addOnGlobalLayoutListener {
        if (measuredWidth > 0 && measuredHeight > 0) {
            block()
        }
    }
}

fun String.isHttps(): Boolean =
    Regex("^https://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(?:[/?#][^ \\t\\n\\r]*|\$)").matches(
        input = this
    )