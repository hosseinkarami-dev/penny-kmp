package io.github.hosseinkarami_dev.penny

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.Image
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.Canvas as DrawCanvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.material3.Text as MaterialText
import androidx.compose.runtime.*
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import io.github.timoptr.mdiicons.Mdi
import io.github.timoptr.mdiicons.rememberImageVector
import io.github.hosseinkarami_dev.penny.domain.model.MoneyTransaction
import io.github.hosseinkarami_dev.penny.domain.model.AppearanceMode
import io.github.hosseinkarami_dev.penny.domain.model.DisplayCurrency
import io.github.hosseinkarami_dev.penny.domain.model.BudgetProgress
import io.github.hosseinkarami_dev.penny.domain.model.CategoryBudget
import io.github.hosseinkarami_dev.penny.domain.model.RecurringRule
import io.github.hosseinkarami_dev.penny.domain.model.RecurrenceFrequency
import io.github.hosseinkarami_dev.penny.domain.model.TransactionType
import io.github.hosseinkarami_dev.penny.domain.model.FinanceCategory
import io.github.hosseinkarami_dev.penny.domain.model.AppLanguage
import io.github.hosseinkarami_dev.penny.domain.backup.CreateEncryptedBackup
import io.github.hosseinkarami_dev.penny.domain.backup.RestoreEncryptedBackup
import io.github.hosseinkarami_dev.penny.domain.usecase.SaveTransaction
import io.github.hosseinkarami_dev.penny.domain.usecase.DeleteTransaction
import io.github.hosseinkarami_dev.penny.domain.usecase.ObserveLanguage
import io.github.hosseinkarami_dev.penny.domain.usecase.SetLanguage
import io.github.hosseinkarami_dev.penny.domain.usecase.ObserveRemindersEnabled
import io.github.hosseinkarami_dev.penny.domain.usecase.SetRemindersEnabled
import io.github.hosseinkarami_dev.penny.domain.usecase.WasBudgetReminderSent
import io.github.hosseinkarami_dev.penny.domain.usecase.MarkBudgetReminderSent
import io.github.hosseinkarami_dev.penny.domain.usecase.ClearBudgetReminderMarkers
import io.github.hosseinkarami_dev.penny.domain.usecase.SaveBudget
import io.github.hosseinkarami_dev.penny.domain.usecase.SaveRecurringRule
import io.github.hosseinkarami_dev.penny.domain.usecase.DeleteRecurringRule
import io.github.hosseinkarami_dev.penny.domain.usecase.nextRecurringOccurrence
import io.github.hosseinkarami_dev.penny.domain.usecase.monthKey
import io.github.hosseinkarami_dev.penny.domain.usecase.ObserveAppearanceMode
import io.github.hosseinkarami_dev.penny.domain.usecase.SetAppearanceMode
import io.github.hosseinkarami_dev.penny.domain.usecase.ObserveDisplayCurrency
import io.github.hosseinkarami_dev.penny.domain.usecase.SetDisplayCurrency
import io.github.hosseinkarami_dev.penny.domain.usecase.ObserveCategories
import io.github.hosseinkarami_dev.penny.domain.usecase.SaveCategories
import io.github.hosseinkarami_dev.penny.domain.usecase.ObserveAppLockEnabled
import io.github.hosseinkarami_dev.penny.domain.usecase.SetAppLockEnabled
import io.github.hosseinkarami_dev.penny.domain.usecase.ExportTransactionsCsv
import io.github.hosseinkarami_dev.penny.presentation.DashboardViewModel
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.painterResource
import penny.shared.generated.resources.Res
import penny.shared.generated.resources.penny_mark

private val Ink: Color @Composable get() = MaterialTheme.colorScheme.onSurface
private val Muted: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val Canvas: Color @Composable get() = MaterialTheme.colorScheme.background
private val Green: Color @Composable get() = MaterialTheme.colorScheme.primary
private val Mint: Color @Composable get() = MaterialTheme.colorScheme.secondaryContainer
private val Coral: Color @Composable get() = MaterialTheme.colorScheme.error
private val LocalCurrency = staticCompositionLocalOf { DisplayCurrency.USD }
private val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.ENGLISH }

private enum class ToastTone { SUCCESS, INFO, ERROR }
private enum class AppLockChangeResult { UPDATED, VERIFICATION_FAILED, SAVE_FAILED }

private fun toastTone(message: String): ToastTone {
    val normalized = message.lowercase()
    val errorTerms = listOf(
        "could not", "couldn't", "failed", "failure", "error", "exception", "invalid",
        "unable", "denied", "not found", "does not match", "do not match", "incorrect",
        "wrong passphrase", "corrupt", "damaged", "unsupported",
    )
    if (errorTerms.any(normalized::contains)) return ToastTone.ERROR
    if (normalized.startsWith("choose ") || normalized.startsWith("select ")) return ToastTone.INFO
    return ToastTone.SUCCESS
}

@Composable
private fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Clip,
    style: TextStyle = LocalTextStyle.current,
) {
    MaterialText(
        text = localize(text, LocalAppLanguage.current), modifier = modifier, color = color,
        fontSize = fontSize, fontWeight = fontWeight, letterSpacing = letterSpacing,
        lineHeight = lineHeight, maxLines = maxLines, minLines = minLines,
        overflow = overflow, style = style,
    )
}

private fun localize(value: String, language: AppLanguage): String {
    if (language == AppLanguage.ENGLISH) return value
    if (value.startsWith("Next · ")) {
        val prefix = when (language) {
            AppLanguage.PERSIAN -> "سررسید بعدی · "
            AppLanguage.ARABIC -> "التالي · "
            AppLanguage.SPANISH -> "Próximo · "
            AppLanguage.FRENCH -> "Prochain · "
            AppLanguage.CHINESE -> "下次 · "
            AppLanguage.JAPANESE -> "次回 · "
            AppLanguage.KOREAN -> "다음 · "
            AppLanguage.ENGLISH -> "Next · "
        }
        return localizeDigits(prefix + value.removePrefix("Next · "), language)
    }
    if (value.startsWith("Amount (")) {
        val translatedAmount = if (language == AppLanguage.PERSIAN) "مبلغ" else appTranslation("Amount", language) ?: "Amount"
        return "$translatedAmount (${value.substringAfter('(')}"
    }
    if (value.startsWith("Monthly limit (")) {
        val label = when (language) {
            AppLanguage.PERSIAN -> "محدودیت ماهانه"
            AppLanguage.ARABIC -> "الحد الشهري"
            AppLanguage.SPANISH -> "Límite mensual"
            AppLanguage.FRENCH -> "Limite mensuelle"
            AppLanguage.CHINESE -> "每月限额"
            AppLanguage.JAPANESE -> "月間上限"
            AppLanguage.KOREAN -> "월 한도"
            AppLanguage.ENGLISH -> "Monthly limit"
        }
        return "$label (${value.substringAfter('(')}"
    }
    if (value.startsWith("Over by ")) {
        val amount = value.removePrefix("Over by ")
        val label = when (language) {
            AppLanguage.PERSIAN -> "بیش از حد: "
            AppLanguage.ARABIC -> "تجاوز بمقدار "
            AppLanguage.SPANISH -> "Excedido por "
            AppLanguage.FRENCH -> "Dépassement de "
            AppLanguage.CHINESE -> "超出 "
            AppLanguage.JAPANESE -> "超過額 "
            AppLanguage.KOREAN -> "초과 금액 "
            AppLanguage.ENGLISH -> "Over by "
        }
        return label + amount
    }
    if (value.endsWith("% saved")) {
        val percent = value.removeSuffix("% saved")
        val suffix = when (language) {
            AppLanguage.PERSIAN -> "٪ ذخیره‌شده"
            AppLanguage.ARABIC -> "٪ تم ادخاره"
            AppLanguage.SPANISH -> "% ahorrado"
            AppLanguage.FRENCH -> "% épargné"
            AppLanguage.CHINESE -> "% 已储蓄"
            AppLanguage.JAPANESE -> "% を貯蓄"
            AppLanguage.KOREAN -> "% 저축"
            AppLanguage.ENGLISH -> "% saved"
        }
        return localizeDigits(percent + suffix, language)
    }
    if (value.endsWith(" categories")) {
        val count = value.removeSuffix(" categories")
        val suffix = when (language) {
            AppLanguage.PERSIAN -> " دسته‌بندی"
            AppLanguage.ARABIC -> " فئة"
            AppLanguage.SPANISH -> " categorías"
            AppLanguage.FRENCH -> " catégories"
            AppLanguage.CHINESE -> " 个类别"
            AppLanguage.JAPANESE -> "カテゴリ"
            AppLanguage.KOREAN -> "개 카테고리"
            AppLanguage.ENGLISH -> " categories"
        }
        return localizeDigits(count + suffix, language)
    }
    val translated = if (language == AppLanguage.PERSIAN) persianLabels[value] else appTranslation(value, language)
    return localizeDigits(translated ?: value, language)
}

private fun persianDigits(value: String) = value.map { char -> if (char in '0'..'9') "۰۱۲۳۴۵۶۷۸۹"[char - '0'] else char }.joinToString("")

private fun localizeDigits(value: String, language: AppLanguage): String = when (language) {
    AppLanguage.PERSIAN -> persianDigits(value)
    AppLanguage.ARABIC -> value.map { char -> if (char in '0'..'9') "٠١٢٣٤٥٦٧٨٩"[char - '0'] else char }.joinToString("")
    else -> value
}

private val persianLabels = mapOf(
    "PERSONAL FINANCE" to "مدیریت مالی شخصی", "Home" to "خانه", "Activity" to "تراکنش‌ها", "Insights" to "تحلیل‌ها", "Settings" to "تنظیمات",
    "Categories" to "دسته‌بندی‌ها", "Manage your budget and transaction labels" to "دسته‌بندی تراکنش‌ها و بودجه‌ها را مدیریت کنید",
    "Total balance" to "موجودی کل", "TOTAL BALANCE" to "موجودی کل", "Income" to "درآمد", "Expenses" to "هزینه‌ها", "All time" to "از ابتدا",
    "Recent activity" to "فعالیت‌های اخیر", "See all" to "مشاهده همه", "Your money story starts here" to "داستان مالی شما از اینجا شروع می‌شود",
    "Add your first transaction to see your activity." to "برای دیدن فعالیت‌ها، اولین تراکنش خود را ثبت کنید.", "Cash flow" to "جریان نقدی",
    "Income and expenses at a glance" to "نمای کلی درآمد و هزینه‌ها", "Insights will appear as you log activity." to "با ثبت تراکنش‌ها، تحلیل‌ها نمایش داده می‌شوند.",
    "You're building a clearer picture of your finances." to "با ثبت تراکنش‌ها تصویر روشن‌تری از وضعیت مالی خود بسازید.",
    "Add transaction" to "افزودن تراکنش", "Edit transaction" to "ویرایش تراکنش", "Save transaction" to "ذخیره تراکنش", "Save changes" to "ذخیره تغییرات",
    "Expense" to "هزینه", "Description" to "شرح", "Amount" to "مبلغ", "Note (optional)" to "یادداشت (اختیاری)", "Repeat" to "تکرار",
    "Never" to "هرگز", "Weekly" to "هفتگی", "Monthly" to "ماهانه", "Daily" to "روزانه", "Cancel" to "لغو", "Delete" to "حذف", "Edit" to "ویرایش",
    "Apply date" to "اعمال تاریخ", "DATE" to "تاریخ", "New category" to "دسته‌بندی جدید", "Category name" to "نام دسته‌بندی",
    "Give this category a name. You can use it for transactions and budgets." to "برای دسته‌بندی نامی انتخاب کنید تا در تراکنش‌ها و بودجه‌ها استفاده شود.",
    "Display currency" to "واحد پول", "Changing this updates the currency label across your ledger. Amounts are not converted." to "واحد پول در برنامه تغییر می‌کند؛ مبلغ‌های ثبت‌شده تبدیل نمی‌شوند.",
    "Choose a theme" to "انتخاب پوسته", "System" to "سیستم", "Light" to "روشن", "Dark" to "تیره", "Language" to "زبان", "English" to "انگلیسی", "Persian" to "فارسی",
    "Upcoming recurring transactions and budgets nearing their limit" to "یادآوری تراکنش‌های دوره‌ای و بودجه‌های نزدیک به سقف", "Enable reminders" to "فعال‌سازی یادآوری‌ها", "Categories" to "دسته‌بندی‌ها",
    "Manage categories" to "مدیریت دسته‌بندی‌ها", "Backup & restore" to "پشتیبان‌گیری و بازیابی", "Create backup" to "ایجاد نسخه پشتیبان",
    "Restore backup" to "بازیابی نسخه پشتیبان", "Export CSV" to "خروجی CSV", "Share CSV" to "اشتراک‌گذاری CSV", "Clear filters" to "پاک کردن فیلترها",
    "No transactions found" to "تراکنشی پیدا نشد", "Budget" to "بودجه", "Save budget" to "ذخیره بودجه", "Add budget" to "افزودن بودجه",
    "Recurring transactions" to "تراکنش‌های دوره‌ای", "Add recurring transaction" to "افزودن تراکنش دوره‌ای", "Edit recurring transaction" to "ویرایش تراکنش دوره‌ای",
    "Next due date (YYYY-MM-DD)" to "تاریخ سررسید بعدی", "Save" to "ذخیره", "Search" to "جستجو", "All time" to "از ابتدا",
    "1 year" to "۱ سال", "30 days" to "۳۰ روز", "90 days" to "۹۰ روز", "All" to "همه", "All categories" to "همه دسته‌بندی‌ها",
    "APPEARANCE" to "ظاهر", "CURRENCY" to "واحد پول", "CATEGORY" to "دسته‌بندی", "RECURRING" to "تکرارشونده", "SPENT" to "هزینه‌شده", "NET SAVINGS" to "پس‌انداز خالص", "IN USE" to "در حال استفاده", "YOUR CATEGORIES" to "دسته‌بندی‌های شما",
    "Add a description." to "شرحی وارد کنید.", "Add category" to "افزودن دسته‌بندی", "Add transactions to see your monthly trend." to "برای دیدن روند ماهانه، تراکنش ثبت کنید.", "App lock" to "قفل برنامه",
    "Backup passphrase" to "گذرواژه پشتیبان", "Backup restored" to "نسخه پشتیبان بازیابی شد", "Budget saved" to "بودجه ذخیره شد", "CSV is ready to share" to "فایل CSV آماده اشتراک‌گذاری است",
    "Category" to "دسته‌بندی", "Category added" to "دسته‌بندی افزوده شد", "Choose a category." to "یک دسته‌بندی انتخاب کنید.", "Choose a location to save your CSV" to "محل ذخیره فایل CSV را انتخاب کنید",
    "Choose a repeat schedule when adding a transaction." to "هنگام افزودن تراکنش، برنامه تکرار را انتخاب کنید.", "Choose where to save your backup" to "محل ذخیره نسخه پشتیبان را انتخاب کنید", "Choose your preferred look" to "ظاهر دلخواهتان را انتخاب کنید", "Coming up" to "به‌زودی",
    "Confirm passphrase" to "تأیید گذرواژه", "Could not create backup" to "ایجاد نسخه پشتیبان ممکن نشد", "Could not delete recurring transaction" to "حذف تراکنش دوره‌ای ممکن نشد", "Could not delete transaction" to "حذف تراکنش ممکن نشد",
    "Could not open the selected backup" to "باز کردن نسخه پشتیبان انتخاب‌شده ممکن نشد", "Could not restore backup." to "بازیابی نسخه پشتیبان ممکن نشد.", "Could not save transaction" to "ذخیره تراکنش ممکن نشد", "Could not update recurring schedule" to "به‌روزرسانی برنامه تکرار ممکن نشد",
    "Create encrypted backup" to "ایجاد نسخه پشتیبان رمزگذاری‌شده", "Create and organize your spending labels" to "دسته‌بندی‌های هزینه خود را بسازید و مدیریت کنید", "Enter a category name." to "نام دسته‌بندی را وارد کنید.",
    "Enter a category." to "یک دسته‌بندی انتخاب کنید.", "Enter a limit greater than zero." to "محدودی بیشتر از صفر وارد کنید.", "Enter a valid date as YYYY-MM-DD." to "تاریخ معتبر با قالب سال-ماه-روز وارد کنید.", "Enter an amount greater than zero." to "مبلغی بیشتر از صفر وارد کنید.",
    "Monthly budgets" to "بودجه‌های ماهانه", "Monthly limit exceeded" to "از سقف ماهانه عبور کردید", "No transactions match those filters." to "تراکنشی با این فیلترها پیدا نشد.", "Nothing scheduled yet" to "هنوز چیزی زمان‌بندی نشده", "Other" to "سایر", "Period" to "بازه زمانی",
    "Personal finance, made simple" to "مدیریت ساده امور مالی شخصی", "Require your device authentication" to "نیازمند احراز هویت دستگاه", "Restore data" to "بازیابی داده‌ها",
    "Restoring replaces the current transactions, budgets, and settings with the contents of this backup." to "بازیابی، تراکنش‌ها، بودجه‌ها و تنظیمات فعلی را با محتوای این نسخه جایگزین می‌کند.",
    "Save CSV file" to "ذخیره فایل CSV", "Save a protected copy of transactions, budgets, categories, and settings. Keep the passphrase safe; Penny cannot recover it." to "نسخه‌ای رمزگذاری‌شده از تراکنش‌ها، بودجه‌ها، دسته‌بندی‌ها و تنظیمات ذخیره کنید. گذرواژه را نگه دارید؛ امکان بازیابی آن وجود ندارد.",
    "Save backup" to "ذخیره نسخه پشتیبان", "Scheduled transactions" to "تراکنش‌های زمان‌بندی‌شده", "Search transactions" to "جستجوی تراکنش‌ها", "See the patterns behind your spending" to "الگوهای هزینه‌های خود را ببینید",
    "Set a category limit to track monthly spending." to "برای پیگیری هزینه‌های ماهانه، سقف دسته‌بندی تعیین کنید.", "Set monthly budget" to "تعیین بودجه ماهانه", "Spending" to "هزینه‌ها", "Spending by category" to "هزینه بر اساس دسته‌بندی", "Spent" to "هزینه‌شده",
    "That category already exists." to "این دسته‌بندی از قبل وجود دارد.", "The passphrases do not match." to "گذرواژه‌ها یکسان نیستند.", "Theme" to "پوسته", "Transaction deleted" to "تراکنش حذف شد", "Transaction saved" to "تراکنش ذخیره شد", "Transaction updated" to "تراکنش به‌روزرسانی شد",
    "Unlock Penny" to "باز کردن قفل Penny", "Use at least 8 characters." to "حداقل ۸ نویسه وارد کنید.", "Verify your device to view your finances." to "برای مشاهده امور مالی، هویت دستگاه را تأیید کنید.", "You're close to this month's limit" to "به سقف هزینه این ماه نزدیک شده‌اید", "Your Penny space" to "فضای Penny شما",
    "Jan" to "ژانویه", "Feb" to "فوریه", "Mar" to "مارس", "Apr" to "آوریل", "May" to "مه", "Jun" to "ژوئن", "Jul" to "ژوئیه", "Aug" to "اوت", "Sep" to "سپتامبر", "Oct" to "اکتبر", "Nov" to "نوامبر", "Dec" to "دسامبر",
    "January" to "ژانویه", "February" to "فوریه", "March" to "مارس", "April" to "آوریل", "June" to "ژوئن", "July" to "ژوئیه", "August" to "اوت", "September" to "سپتامبر", "October" to "اکتبر", "November" to "نوامبر", "December" to "دسامبر",
    "Food" to "خوراک", "Groceries" to "خرید روزانه", "Dining" to "رستوران", "Transport" to "حمل‌ونقل", "Shopping" to "خرید", "Travel" to "سفر", "Housing" to "مسکن", "Health" to "سلامت", "Utilities" to "قبوض", "Salary" to "حقوق", "General" to "عمومی",
    "1Y" to "۱ سال", "30D" to "۳۰ روز", "90D" to "۹۰ روز", "Filter" to "فیلتر", "Type" to "نوع", "Date" to "تاریخ", "Apply" to "اعمال", "Reset" to "بازنشانی",
)
private enum class InsightPeriod(val label: String, val days: Int?) {
    MONTH("30D", 30), QUARTER("90D", 90), YEAR("1Y", 365), ALL("All", null),
}
private enum class ActivityPeriod(val label: String, val days: Int?) {
    ALL("All time", null), MONTH("30 days", 30), QUARTER("90 days", 90), YEAR("1 year", 365),
}
private val PennyTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, letterSpacing = (-1.5).sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, letterSpacing = (-.8).sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, letterSpacing = (-.4).sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
)
@Composable
fun App() {
    KoinApplication(application = { modules(io.github.hosseinkarami_dev.penny.di.appModule) }) {
        val settingsScope = rememberCoroutineScope()
        val observeAppearance: ObserveAppearanceMode = koinInject()
        val setAppearance: SetAppearanceMode = koinInject()
        val observeCurrency: ObserveDisplayCurrency = koinInject()
        val setCurrency: SetDisplayCurrency = koinInject()
        val exportCsv: ExportTransactionsCsv = koinInject()
        val observeCategories: ObserveCategories = koinInject()
        val saveCategories: SaveCategories = koinInject()
        val observeAppLock: ObserveAppLockEnabled = koinInject()
        val setAppLock: SetAppLockEnabled = koinInject()
        val observeLanguage: ObserveLanguage = koinInject()
        val setLanguage: SetLanguage = koinInject()
        val observeReminders: ObserveRemindersEnabled = koinInject()
        val setReminders: SetRemindersEnabled = koinInject()
        val clearBudgetReminderMarkers: ClearBudgetReminderMarkers = koinInject()
        val appearanceMode by observeAppearance().collectAsState(AppearanceMode.SYSTEM)
        val currency by observeCurrency().collectAsState(DisplayCurrency.USD)
        val categories by observeCategories().collectAsState(emptyList())
        val appLockEnabled by observeAppLock().collectAsState(false)
        val language by observeLanguage().collectAsState(AppLanguage.ENGLISH)
        val remindersEnabled by observeReminders().collectAsState(false)
        var lockStateReady by remember { mutableStateOf(false) }
        var isUnlocked by remember { mutableStateOf(false) }
        var keepSessionUnlocked by remember { mutableStateOf(false) }
        LaunchedEffect(appLockEnabled) {
            if (keepSessionUnlocked) keepSessionUnlocked = false else isUnlocked = !appLockEnabled
            lockStateReady = true
        }
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner, appLockEnabled) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP && appLockEnabled) isUnlocked = false
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
        val dark = when (appearanceMode) {
            AppearanceMode.SYSTEM -> isSystemInDarkTheme()
            AppearanceMode.LIGHT -> false
            AppearanceMode.DARK -> true
        }
        val scheme = if (dark) darkColorScheme(
            primary = Color(0xFF83DDB4), onPrimary = Color(0xFF073A2A), primaryContainer = Color(0xFF194E3C), onPrimaryContainer = Color(0xFFC5F4D9),
            secondary = Color(0xFFB7DCC7), secondaryContainer = Color(0xFF263E33), onSecondaryContainer = Color(0xFFD3EDDE),
            background = Color(0xFF0D1512), surface = Color(0xFF151F1B), surfaceVariant = Color(0xFF24312B), onSurface = Color(0xFFE4EEE8), onSurfaceVariant = Color(0xFFB0BFB6),
            error = Color(0xFFFFB4A6), onError = Color(0xFF5A160D),
        ) else lightColorScheme(
            primary = Color(0xFF147A55), onPrimary = Color.White, primaryContainer = Color(0xFFD4F3E2), onPrimaryContainer = Color(0xFF082F20),
            secondary = Color(0xFF39785A), secondaryContainer = Color(0xFFE1F1E7), onSecondaryContainer = Color(0xFF183B2A),
            background = Color(0xFFF4F7F3), surface = Color(0xFFFFFFFF), surfaceVariant = Color(0xFFE8EEE9), onSurface = Color(0xFF16251E), onSurfaceVariant = Color(0xFF64736A),
            error = Color(0xFFB9382B), onError = Color.White,
        )
        MaterialTheme(colorScheme = scheme, typography = PennyTypography) {
            CompositionLocalProvider(
                LocalCurrency provides currency,
                LocalAppLanguage provides language,
                LocalLayoutDirection provides if (language.isRightToLeft) LayoutDirection.Rtl else LayoutDirection.Ltr,
            ) {
                if (!lockStateReady) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Green) }
                } else if (appLockEnabled && !isUnlocked) {
                    AppLockScreen(onUnlock = { authenticateDevice { success -> isUnlocked = success } })
                } else DashboardScreen(
                    appearanceMode = appearanceMode,
                    onAppearanceModeChange = { mode -> settingsScope.launch { setAppearance(mode) } },
                    currency = currency,
                    onCurrencyChange = { selected -> settingsScope.launch { setCurrency(selected) } },
                    language = language,
                    onLanguageChange = { selected -> settingsScope.launch { setLanguage(selected) } },
                    remindersEnabled = remindersEnabled,
                    onRemindersEnabledChange = { enabled -> settingsScope.launch { setReminders(enabled); if (!enabled) clearBudgetReminderMarkers() } },
                    exportTransactionsCsv = exportCsv,
                    categories = categories,
                    onSaveCategories = { values -> settingsScope.launch { saveCategories(values) } },
                    appLockEnabled = appLockEnabled,
                    onAppLockEnabledChange = { enabled, onResult ->
                        authenticateDevice { success ->
                            if (success) {
                                keepSessionUnlocked = true
                                isUnlocked = true
                                settingsScope.launch {
                                    runCatching { setAppLock(enabled) }
                                        .onSuccess { onResult(AppLockChangeResult.UPDATED) }
                                        .onFailure { onResult(AppLockChangeResult.SAVE_FAILED) }
                                }
                            } else {
                                onResult(AppLockChangeResult.VERIFICATION_FAILED)
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun DashboardScreen(
    appearanceMode: AppearanceMode,
    onAppearanceModeChange: (AppearanceMode) -> Unit,
    currency: DisplayCurrency,
    onCurrencyChange: (DisplayCurrency) -> Unit,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    remindersEnabled: Boolean,
    onRemindersEnabledChange: (Boolean) -> Unit,
    exportTransactionsCsv: ExportTransactionsCsv,
    categories: List<FinanceCategory>,
    onSaveCategories: (List<FinanceCategory>) -> Unit,
    appLockEnabled: Boolean,
    onAppLockEnabledChange: (Boolean, (AppLockChangeResult) -> Unit) -> Unit,
    viewModel: DashboardViewModel = koinInject(),
    saveTransaction: SaveTransaction = koinInject(),
    saveBudget: SaveBudget = koinInject(),
    saveRecurringRule: SaveRecurringRule = koinInject(),
    deleteRecurringRule: DeleteRecurringRule = koinInject(),
    wasBudgetReminderSent: WasBudgetReminderSent = koinInject(),
    markBudgetReminderSent: MarkBudgetReminderSent = koinInject(),
    deleteTransaction: DeleteTransaction = koinInject(),
    createEncryptedBackup: CreateEncryptedBackup = koinInject(),
    restoreEncryptedBackup: RestoreEncryptedBackup = koinInject(),
) {
    val summary by viewModel.summary.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    val recurringRules by viewModel.recurringRules.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddCategory by remember { mutableStateOf(false) }
    var showCreateBackup by remember { mutableStateOf(false) }
    var showRestoreBackup by remember { mutableStateOf(false) }
    var restoreBackupContents by remember { mutableStateOf<String?>(null) }
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryError by remember { mutableStateOf<String?>(null) }
    var transactionBeingEdited by remember { mutableStateOf<MoneyTransaction?>(null) }
    var transactionPendingDelete by remember { mutableStateOf<MoneyTransaction?>(null) }
    var recurringRulePendingDelete by remember { mutableStateOf<RecurringRule?>(null) }
    var recurringRuleBeingEdited by remember { mutableStateOf<RecurringRule?>(null) }
    var selectedTab by remember { mutableStateOf("Home") }
    var transactionEditorOrigin by remember { mutableStateOf("Home") }
    val isSecondaryScreen = selectedTab in setOf("Categories", "TransactionEditor", "Backup", "Scheduled")
    val returnToMainScreen = {
        selectedTab = when (selectedTab) {
            "TransactionEditor" -> transactionEditorOrigin
            "Categories", "Backup", "Scheduled" -> "Settings"
            else -> "Home"
        }
    }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val contentScrollState = rememberScrollState()
    var previousScrollPosition by remember { mutableIntStateOf(0) }
    var fabVisible by remember { mutableStateOf(true) }
    val hasScrolled by remember { derivedStateOf { contentScrollState.value > 0 } }
    BackHandler(enabled = isSecondaryScreen) { returnToMainScreen() }
    LaunchedEffect(remindersEnabled) {
        if (remindersEnabled) requestLocalReminderPermission() else cancelAllLocalReminders()
    }
    LaunchedEffect(remindersEnabled, recurringRules, language) {
        if (!remindersEnabled) {
            cancelAllLocalReminders()
        } else {
            val now = Clock.System.now().toEpochMilliseconds()
            recurringRules.filter { it.nextDueAtEpochMillis > now }.forEach { rule ->
                val message = when (language) {
                    AppLanguage.ENGLISH -> "${rule.category} payment is coming up"
                    AppLanguage.PERSIAN -> "تراکنش دوره‌ای ${rule.category} سررسید می‌شود"
                    AppLanguage.ARABIC -> "اقترب موعد المعاملة المتكررة: ${rule.category}"
                    AppLanguage.SPANISH -> "Se acerca el pago recurrente: ${rule.category}"
                    AppLanguage.FRENCH -> "Le paiement récurrent approche : ${rule.category}"
                    AppLanguage.CHINESE -> "即将到期的定期交易：${rule.category}"
                    AppLanguage.JAPANESE -> "定期取引の予定日が近づいています：${rule.category}"
                    AppLanguage.KOREAN -> "반복 거래 예정일이 다가옵니다: ${rule.category}"
                }
                scheduleLocalReminder(rule.id, rule.title, message, rule.nextDueAtEpochMillis)
            }
        }
    }
    LaunchedEffect(remindersEnabled, budgets, language) {
        if (remindersEnabled) {
            val now = Clock.System.now().toEpochMilliseconds()
            budgets.filter { it.isNearLimit }.forEach { progress ->
                if (!wasBudgetReminderSent(progress.budget.id)) {
                    markBudgetReminderSent(progress.budget.id)
                    val title = when (language) {
                        AppLanguage.ENGLISH -> "Budget is nearing its limit"
                        AppLanguage.PERSIAN -> "نزدیک‌شدن به سقف بودجه"
                        AppLanguage.ARABIC -> "اقتربت الميزانية من حدها"
                        AppLanguage.SPANISH -> "El presupuesto se acerca al límite"
                        AppLanguage.FRENCH -> "Le budget approche de sa limite"
                        AppLanguage.CHINESE -> "预算即将达到上限"
                        AppLanguage.JAPANESE -> "予算が上限に近づいています"
                        AppLanguage.KOREAN -> "예산 한도에 가까워지고 있습니다"
                    }
                    val category = if (language == AppLanguage.PERSIAN) persianLabels[progress.budget.category] ?: progress.budget.category else appTranslation(progress.budget.category, language) ?: progress.budget.category
                    val percent = (progress.spentMinor.toDouble() * 100.0 / progress.budget.limitMinor).toInt().coerceAtLeast(0)
                    val message = when (language) {
                        AppLanguage.ENGLISH -> if (progress.isOverBudget) "Spending in $category has exceeded its budget" else "Spending in $category has reached $percent% of its budget"
                        AppLanguage.PERSIAN -> if (progress.isOverBudget) "هزینه ${category} از سقف بودجه بیشتر شده است" else "هزینه ${category} به ${persianDigits(percent.toString())}٪ بودجه رسیده است"
                        AppLanguage.ARABIC -> if (progress.isOverBudget) "تجاوز الإنفاق في $category حد الميزانية" else "وصل الإنفاق في $category إلى ${localizeDigits(percent.toString(), language)}٪ من الميزانية"
                        AppLanguage.SPANISH -> if (progress.isOverBudget) "El gasto de $category superó el presupuesto" else "El gasto de $category alcanzó el ${percent}% del presupuesto"
                        AppLanguage.FRENCH -> if (progress.isOverBudget) "Les dépenses de $category dépassent le budget" else "Les dépenses de $category atteignent $percent % du budget"
                        AppLanguage.CHINESE -> if (progress.isOverBudget) "$category 支出已超出预算" else "$category 支出已达到预算的 $percent%"
                        AppLanguage.JAPANESE -> if (progress.isOverBudget) "$category の支出が予算を超えました" else "$category の支出が予算の $percent% に達しました"
                        AppLanguage.KOREAN -> if (progress.isOverBudget) "$category 지출이 예산을 초과했습니다" else "$category 지출이 예산의 $percent%에 도달했습니다"
                    }
                    scheduleLocalReminder("budget-${progress.budget.id}", title, message, now + 1_000L)
                }
            }
        }
    }
    LaunchedEffect(contentScrollState, selectedTab) {
        contentScrollState.scrollTo(0)
        previousScrollPosition = 0
        fabVisible = true
        scrollBehavior.state.heightOffset = 0f
        scrollBehavior.state.contentOffset = 0f
    }
    LaunchedEffect(contentScrollState, selectedTab) {
        snapshotFlow { contentScrollState.value }.collect { position ->
            if (position < previousScrollPosition) fabVisible = true
            else if (position > previousScrollPosition && position > 24) fabVisible = false
            previousScrollPosition = position
        }
    }
    Scaffold(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), containerColor = Canvas, snackbarHost = {}, topBar = {
        Surface(color = if (hasScrolled) MaterialTheme.colorScheme.surface else Canvas, shadowElevation = if (hasScrolled) 6.dp else 0.dp) {
            if (selectedTab in setOf("Categories", "TransactionEditor", "Backup", "Scheduled")) {
                val isTransactionEditor = selectedTab == "TransactionEditor"
                val isBackup = selectedTab == "Backup"
                val isScheduled = selectedTab == "Scheduled"
                TopAppBar(
                    title = {
                        Column {
                            Text(when { isTransactionEditor -> if (transactionBeingEdited == null) "Add transaction" else "Edit transaction"; isBackup -> "Backup & restore"; isScheduled -> "Scheduled transactions"; else -> "Categories" }, color = Ink, fontSize = 18.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold)
                            Text(when { isTransactionEditor -> "Record the details of your transaction"; isBackup -> "Keep a secure copy of your Penny data"; isScheduled -> "Manage transactions you repeat"; else -> "Manage your budget and transaction labels" }, color = Muted, fontSize = 10.sp, lineHeight = 13.sp)
                        }
                    },
                    navigationIcon = { IconButton(onClick = returnToMainScreen) { PennyIcon("arrow-left", tint = Ink, modifier = Modifier.size(21.dp), contentDescription = localize("Back", LocalAppLanguage.current)) } },
                    actions = {},
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent),
                )
            } else CenterAlignedTopAppBar(
                title = { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Surface(shape = RoundedCornerShape(15.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(40.dp)) {
                        Image(painter = painterResource(Res.drawable.penny_mark), contentDescription = "Penny", modifier = Modifier.fillMaxSize().padding(5.dp))
                    }
                    Column { Text("Penny", fontSize = 19.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold, color = Ink, letterSpacing = (-.5).sp); Text("PERSONAL FINANCE", fontSize = 8.sp, lineHeight = 10.sp, letterSpacing = 1.35.sp, color = Muted, fontWeight = FontWeight.SemiBold) }
                } },
                navigationIcon = {}, actions = {}, scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent),
            )
        }
    }, floatingActionButton = {
        AnimatedVisibility(visible = selectedTab in setOf("Home", "Activity", "Categories") && fabVisible, enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(), exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()) {
            val fabDescription = localize(if (selectedTab == "Categories") "Add category" else "Add transaction", LocalAppLanguage.current)
            FloatingActionButton(
                onClick = {
                    if (selectedTab == "Categories") {
                        newCategoryName = ""
                        newCategoryError = null
                        showAddCategory = true
                    } else {
                        transactionBeingEdited = null
                        transactionEditorOrigin = selectedTab
                        selectedTab = "TransactionEditor"
                    }
                },
                containerColor = Green,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 7.dp, pressedElevation = 10.dp),
                shape = CircleShape,
                modifier = Modifier.size(60.dp).semantics {
                    contentDescription = fabDescription
                },
            ) {
                AnimatedContent(targetState = selectedTab == "Categories", label = "fab-action-icon") { addingCategory ->
                    PennyIcon(if (addingCategory) "plus" else "plus", tint = Color.White, modifier = Modifier.size(25.dp))
                }
            }
        }
    }, bottomBar = {
        if (selectedTab !in setOf("TransactionEditor", "Categories", "Backup", "Scheduled")) Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .55f), thickness = 1.dp)
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                    listOf("home-variant" to "Home", "format-list-bulleted" to "Activity", "chart-donut" to "Insights", "cog-outline" to "Settings").forEach { (icon, label) ->
                        val selected = (if (selectedTab == "Categories") "Settings" else selectedTab) == label
                        NavigationBarItem(
                            selected = selected,
                            onClick = { if (selectedTab != label) selectedTab = label },
                            icon = { PennyIcon(icon, tint = if (selected) Green else Muted, modifier = Modifier.size(22.dp)) },
                            label = { Text(label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Green,
                                selectedTextColor = Green,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = Muted,
                                unselectedTextColor = Muted,
                            ),
                        )
                    }
                }
            }
        }
    }) { padding ->
        Box(
            Modifier.fillMaxSize()
                .padding(
                    top = padding.calculateTopPadding(),
                    bottom = if (selectedTab == "TransactionEditor") 0.dp else padding.calculateBottomPadding(),
                ),
        ) {
        if (selectedTab == "Backup") {
            BackupManagementScreen(
                onCreateBackup = { showCreateBackup = true },
                onRestoreBackup = {
                    pickEncryptedBackup { contents ->
                        if (contents == null) scope.launch { snackbarHostState.showSnackbar("Could not open the selected backup") }
                        else { restoreBackupContents = contents; showRestoreBackup = true }
                    }
                },
            )
        } else if (selectedTab == "TransactionEditor") {
            AddTransactionScreen(
                initialTransaction = transactionBeingEdited,
                categories = categories.map { it.name },
                onDismiss = { transactionBeingEdited = null; selectedTab = transactionEditorOrigin },
                onSave = { transaction, frequency ->
                    val wasEditing = transactionBeingEdited != null
                    scope.launch {
                        runCatching {
                            saveTransaction(transaction)
                            frequency?.let { selected ->
                                saveRecurringRule(RecurringRule(
                                    id = "recurring_${transaction.id}", title = transaction.title, amountMinor = transaction.amountMinor,
                                    type = transaction.type, category = transaction.category, frequency = selected,
                                    nextDueAtEpochMillis = nextRecurringOccurrence(selected, transaction.occurredAtEpochMillis),
                                ))
                            }
                            if (frequency == null && wasEditing) deleteRecurringRule("recurring_${transaction.id}")
                        }.onSuccess {
                            transactionBeingEdited = null
                            selectedTab = transactionEditorOrigin
                            snackbarHostState.showSnackbar(if (wasEditing) "Transaction updated" else "Transaction saved")
                        }.onFailure { snackbarHostState.showSnackbar("Could not save transaction") }
                    }
                },
            )
        } else Column(Modifier.fillMaxSize().verticalScroll(contentScrollState).padding(horizontal = 22.dp)) {
            Spacer(Modifier.height(14.dp))
            when (selectedTab) {
                "Activity" -> ActivityContent(
                    transactions,
                    onExport = { exportRows, save ->
                        val csv = exportTransactionsCsv(exportRows, currency.code)
                        if (save) {
                            saveCsvFile(csv)
                            scope.launch { snackbarHostState.showSnackbar("Choose a location to save your CSV") }
                        } else {
                            createAndShareCsvFile(csv)
                            scope.launch { snackbarHostState.showSnackbar("CSV is ready to share") }
                        }
                    },
                    onEdit = { transaction -> transactionBeingEdited = transaction; transactionEditorOrigin = "Activity"; selectedTab = "TransactionEditor" },
                    onDelete = { transaction -> transactionPendingDelete = transaction },
                )
                "Insights" -> CategoryInsights(transactions, budgets, categories.map { it.name }, onSaveBudget = { budget -> scope.launch { runCatching { saveBudget(budget) }.onSuccess { snackbarHostState.showSnackbar("Budget saved") }.onFailure { snackbarHostState.showSnackbar("Could not save budget") } } })
                "Scheduled" -> ScheduledTransactionsScreen(
                    rules = recurringRules,
                    onEdit = { recurringRuleBeingEdited = it },
                    onDelete = { recurringRulePendingDelete = it },
                )
                "Settings" -> ProfileContent(appearanceMode, { mode -> onAppearanceModeChange(mode); scope.launch { snackbarHostState.showSnackbar("Theme updated") } }, currency, { value -> onCurrencyChange(value); scope.launch { snackbarHostState.showSnackbar("Currency updated") } }, language, { value -> onLanguageChange(value); scope.launch { snackbarHostState.showSnackbar("Language updated") } }, remindersEnabled, onRemindersEnabledChange, appLockEnabled, onAppLockEnabledChange = { enabled, onResult -> onAppLockEnabledChange(enabled) { result ->
                    onResult(result)
                    val message = when (result) {
                        AppLockChangeResult.UPDATED -> "App lock updated"
                        AppLockChangeResult.VERIFICATION_FAILED -> "Device verification failed. Check your screen lock or biometric settings and try again."
                        AppLockChangeResult.SAVE_FAILED -> "Could not update app lock"
                    }
                    scope.launch { snackbarHostState.showSnackbar(message) }
                } }, onManageCategories = { selectedTab = "Categories" }, onManageBackup = { selectedTab = "Backup" })
                "Categories" -> CategoryManagementScreen(
                    categories = categories,
                    used = transactions.map { it.category }.toSet() + budgets.map { it.budget.category } + recurringRules.map { it.category },
                    onSave = { values -> onSaveCategories(values); scope.launch { snackbarHostState.showSnackbar("Categories updated") } },
                )
                else -> {
                    BalanceCard(summary.balanceMinor)
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryCard("Income", summary.incomeMinor, "trending-up", Modifier.weight(1f), Green)
                        SummaryCard("Expenses", summary.expenseMinor, "trending-down", Modifier.weight(1f), Coral)
                    }
                    Spacer(Modifier.height(25.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Recent activity", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Row(Modifier.clickable { selectedTab = "Activity" }, verticalAlignment = Alignment.CenterVertically) { Text("See all", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Green); Spacer(Modifier.width(3.dp)); PennyIcon("arrow-right", tint = Green, modifier = Modifier.size(14.dp)) }
                    }
                    Spacer(Modifier.height(12.dp))
                    if (summary.recentTransactions.isEmpty()) EmptyState() else summary.recentTransactions.forEach { transaction ->
                        TransactionRow(transaction, onEdit = { transactionBeingEdited = transaction; transactionEditorOrigin = "Home"; selectedTab = "TransactionEditor" }, onDelete = { transactionPendingDelete = transaction })
                        Spacer(Modifier.height(8.dp))
                    }
                    Spacer(Modifier.height(20.dp))
                    InsightsCard(summary.expenseMinor, summary.incomeMinor)
                    Spacer(Modifier.height(14.dp))
                    UpcomingRecurringCard(recurringRules, onManage = { selectedTab = "Scheduled" })
                }
            }
            Spacer(Modifier.height(22.dp))
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        ) { snackbarData ->
            val tone = toastTone(snackbarData.visuals.message)
            val accent = when (tone) {
                ToastTone.SUCCESS -> MaterialTheme.colorScheme.secondary
                ToastTone.INFO -> MaterialTheme.colorScheme.primary
                ToastTone.ERROR -> MaterialTheme.colorScheme.error
            }
            val iconContainer = when (tone) {
                ToastTone.SUCCESS -> MaterialTheme.colorScheme.secondaryContainer
                ToastTone.INFO -> MaterialTheme.colorScheme.primaryContainer
                ToastTone.ERROR -> MaterialTheme.colorScheme.errorContainer
            }
            val iconTint = when (tone) {
                ToastTone.SUCCESS -> MaterialTheme.colorScheme.onSecondaryContainer
                ToastTone.INFO -> MaterialTheme.colorScheme.onPrimaryContainer
                ToastTone.ERROR -> MaterialTheme.colorScheme.onErrorContainer
            }
            val iconName = when (tone) {
                ToastTone.SUCCESS -> "check"
                ToastTone.INFO -> "information-outline"
                ToastTone.ERROR -> "alert-circle-outline"
            }
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 12.dp,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (tone == ToastTone.ERROR) accent.copy(alpha = .35f)
                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = .5f),
                ),
                modifier = Modifier.fillMaxWidth().widthIn(max = 440.dp),
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    Surface(shape = CircleShape, color = iconContainer, modifier = Modifier.size(34.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            PennyIcon(iconName, tint = iconTint, modifier = Modifier.size(18.dp))
                        }
                    }
                    Text(snackbarData.visuals.message, modifier = Modifier.weight(1f), color = if (tone == ToastTone.ERROR) accent else Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    IconButton(onClick = { snackbarData.dismiss() }, modifier = Modifier.size(40.dp)) { PennyIcon("close", tint = Muted, modifier = Modifier.size(16.dp), contentDescription = localize("Dismiss message", LocalAppLanguage.current)) }
                }
            }
        }
        }
    }
    if (showAddCategory) {
        AlertDialog(
            onDismissRequest = { showAddCategory = false },
            title = { Text("New category", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Give this category a name. You can use it for transactions and budgets.", color = Muted, fontSize = 13.sp)
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it; newCategoryError = null },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Category name") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                    )
                    newCategoryError?.let { Text(it, color = Coral, fontSize = 12.sp) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val normalized = newCategoryName.trim()
                    when {
                        normalized.isBlank() -> newCategoryError = "Enter a category name."
                        categories.any { it.name.equals(normalized, ignoreCase = true) } -> newCategoryError = "That category already exists."
                        else -> {
                            onSaveCategories(categories + FinanceCategory(normalized))
                            showAddCategory = false
                            scope.launch { snackbarHostState.showSnackbar("Category added") }
                        }
                    }
                }) { Text("Add category") }
            },
            dismissButton = { TextButton(onClick = { showAddCategory = false }) { Text("Cancel", color = Muted) } },
        )
    }
    if (showCreateBackup) {
        var password by remember { mutableStateOf("") }
        var confirmation by remember { mutableStateOf("") }
        var backupError by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showCreateBackup = false },
            title = { Text("Create encrypted backup", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("Save a protected copy of transactions, budgets, categories, and settings. Keep the passphrase safe; Penny cannot recover it.", color = Muted, fontSize = 12.sp)
                    OutlinedTextField(password, { password = it; backupError = null }, modifier = Modifier.fillMaxWidth(), label = { Text("Backup passphrase") }, singleLine = true)
                    OutlinedTextField(confirmation, { confirmation = it; backupError = null }, modifier = Modifier.fillMaxWidth(), label = { Text("Confirm passphrase") }, singleLine = true)
                    backupError?.let { Text(it, color = Coral, fontSize = 12.sp) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    when {
                        password.length < 8 -> backupError = "Use at least 8 characters."
                        password != confirmation -> backupError = "The passphrases do not match."
                        else -> scope.launch {
                            runCatching { createEncryptedBackup(password) }
                                .onSuccess { contents -> saveEncryptedBackup(contents); showCreateBackup = false; snackbarHostState.showSnackbar("Choose where to save your backup") }
                                .onFailure { snackbarHostState.showSnackbar(it.message ?: "Could not create backup") }
                        }
                    }
                }) { Text("Save backup") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { showCreateBackup = false }) { Text("Cancel", color = Muted) }
                }
            },
        )
    }
    if (showRestoreBackup && restoreBackupContents != null) {
        var password by remember { mutableStateOf("") }
        var restoreError by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showRestoreBackup = false; restoreBackupContents = null },
            title = { Text("Restore backup", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("Restoring replaces the current transactions, budgets, and settings with the contents of this backup.", color = Muted, fontSize = 12.sp)
                    OutlinedTextField(password, { password = it; restoreError = null }, modifier = Modifier.fillMaxWidth(), label = { Text("Backup passphrase") }, singleLine = true)
                    restoreError?.let { Text(it, color = Coral, fontSize = 12.sp) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        runCatching { restoreEncryptedBackup(restoreBackupContents!!, password) }
                            .onSuccess { showRestoreBackup = false; restoreBackupContents = null; snackbarHostState.showSnackbar("Backup restored") }
                            .onFailure { restoreError = it.message ?: "Could not restore backup." }
                    }
                }) { Text("Restore data") }
            },
            dismissButton = { TextButton(onClick = { showRestoreBackup = false; restoreBackupContents = null }) { Text("Cancel", color = Muted) } },
        )
    }
    transactionPendingDelete?.let { transaction ->
        AlertDialog(
            onDismissRequest = { transactionPendingDelete = null },
            title = { Text("Delete transaction?") },
            text = { Text("${transaction.title} · ${formatMoney(transaction.amountMinor)}") },
            confirmButton = {
                TextButton(onClick = { scope.launch { runCatching { deleteTransaction(transaction.id) }.onSuccess { snackbarHostState.showSnackbar("Transaction deleted") }.onFailure { snackbarHostState.showSnackbar("Could not delete transaction") } }; transactionPendingDelete = null }) {
                    Text("Delete", color = Coral)
                }
            },
            dismissButton = { TextButton(onClick = { transactionPendingDelete = null }) { Text("Cancel") } },
        )
    }
    recurringRuleBeingEdited?.let { rule ->
        RecurringRuleEditDialog(rule, onDismiss = { recurringRuleBeingEdited = null }, onSave = { amountMinor, frequency, dueAt ->
            scope.launch {
                runCatching { saveRecurringRule(rule.copy(amountMinor = amountMinor, frequency = frequency, nextDueAtEpochMillis = dueAt)) }
                    .onSuccess { snackbarHostState.showSnackbar("Recurring schedule updated") }
                    .onFailure { snackbarHostState.showSnackbar("Could not update recurring schedule") }
            }
            recurringRuleBeingEdited = null
        })
    }
    recurringRulePendingDelete?.let { rule ->
        AlertDialog(
            onDismissRequest = { recurringRulePendingDelete = null },
            title = { Text("Delete recurring transaction?") },
            text = { Text("${rule.title} · ${formatMoney(rule.amountMinor)}") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        runCatching { deleteRecurringRule(rule.id) }
                            .onSuccess { snackbarHostState.showSnackbar("Recurring transaction deleted") }
                            .onFailure { snackbarHostState.showSnackbar("Could not delete recurring transaction") }
                    }
                    recurringRulePendingDelete = null
                }) { Text("Delete", color = Coral) }
            },
            dismissButton = { TextButton(onClick = { recurringRulePendingDelete = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun ActivityContent(transactions: List<MoneyTransaction>, onExport: (List<MoneyTransaction>, Boolean) -> Unit, onEdit: (MoneyTransaction) -> Unit, onDelete: (MoneyTransaction) -> Unit) {
    var query by remember { mutableStateOf("") }
    var typeFilter by remember { mutableStateOf<TransactionType?>(null) }
    var categoryFilter by remember { mutableStateOf<String?>(null) }
    var activityPeriod by remember { mutableStateOf(ActivityPeriod.ALL) }
    var filtersExpanded by remember { mutableStateOf(false) }
    val cutoff = activityPeriod.days?.let { Clock.System.now().toEpochMilliseconds() - it * 86_400_000L }
    val filtered = transactions.filter { item ->
        (typeFilter == null || item.type == typeFilter) &&
            (categoryFilter == null || item.category == categoryFilter) &&
            (cutoff == null || item.occurredAtEpochMillis >= cutoff) &&
            (query.isBlank() || item.title.contains(query.trim(), ignoreCase = true) || item.category.contains(query.trim(), ignoreCase = true))
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Activity", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = Ink)
        Box {
            var exportMenu by remember { mutableStateOf(false) }
            TextButton(onClick = { exportMenu = true }, enabled = transactions.isNotEmpty()) {
                PennyIcon("file-export-outline", tint = if (transactions.isEmpty()) Muted else Green, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Export CSV")
            }
            DropdownMenu(expanded = exportMenu, onDismissRequest = { exportMenu = false }) {
                DropdownMenuItem(text = { Text("Share CSV") }, leadingIcon = { PennyIcon("share-variant-outline", tint = Green, modifier = Modifier.size(18.dp)) }, onClick = { exportMenu = false; onExport(filtered, false) })
                DropdownMenuItem(text = { Text("Save CSV file") }, leadingIcon = { PennyIcon("download-outline", tint = Green, modifier = Modifier.size(18.dp)) }, onClick = { exportMenu = false; onExport(filtered, true) })
            }
        }
    }
    val categories = transactions.map { it.category }.distinct().sorted()
    Spacer(Modifier.height(10.dp))
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Search transactions", fontSize = 13.sp) },
            leadingIcon = { PennyIcon("magnify", tint = Muted, modifier = Modifier.size(19.dp)) },
            trailingIcon = if (query.isNotEmpty()) ({ IconButton(onClick = { query = "" }) { PennyIcon("close", tint = Muted, modifier = Modifier.size(17.dp), contentDescription = localize("Clear search", LocalAppLanguage.current)) } }) else null,
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
        )
        FilledTonalIconButton(onClick = { filtersExpanded = !filtersExpanded }, shape = RoundedCornerShape(15.dp)) {
            PennyIcon(if (filtersExpanded) "tune-variant" else "filter-variant", tint = if (categoryFilter != null || activityPeriod != ActivityPeriod.ALL) Green else Ink, modifier = Modifier.size(20.dp), contentDescription = localize("Filters", LocalAppLanguage.current))
        }
    }
    Spacer(Modifier.height(9.dp))
    Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .62f), RoundedCornerShape(15.dp)).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        listOf(null, TransactionType.EXPENSE, TransactionType.INCOME).forEach { type ->
            val selected = typeFilter == type
            Box(
                Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if (selected) MaterialTheme.colorScheme.surface else Color.Transparent).clickable { typeFilter = type }.padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(when (type) { null -> "All"; TransactionType.EXPENSE -> "Expenses"; TransactionType.INCOME -> "Income" }, color = if (selected) Ink else Muted, fontSize = 12.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
            }
        }
    }
    if (filtersExpanded) {
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActivityFilterMenu("Period", activityPeriod.label, ActivityPeriod.entries.map { it.label }, { label -> activityPeriod = ActivityPeriod.entries.first { it.label == label } }, Modifier.weight(1f))
            ActivityFilterMenu("Category", categoryFilter ?: "All categories", listOf("All categories") + categories, { label -> categoryFilter = label.takeUnless { it == "All categories" } }, Modifier.weight(1f))
        }
    }
    Spacer(Modifier.height(13.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("${filtered.size} ${if (filtered.size == 1) "transaction" else "transactions"}", color = Muted, fontSize = 11.sp)
        if (categoryFilter != null || activityPeriod != ActivityPeriod.ALL || typeFilter != null || query.isNotEmpty()) {
            TextButton(onClick = { query = ""; typeFilter = null; categoryFilter = null; activityPeriod = ActivityPeriod.ALL }, modifier = Modifier.heightIn(min = 48.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                PennyIcon("filter-remove-outline", tint = Green, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(6.dp)); Text("Clear filters", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    if (filtered.isEmpty()) {
        if (transactions.isEmpty()) EmptyState() else Text("No transactions match those filters.", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(vertical = 18.dp))
    } else filtered.forEach { transaction ->
        TransactionRow(transaction, onEdit = { onEdit(transaction) }, onDelete = { onDelete(transaction) })
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ActivityFilterMenu(title: String, selected: String, options: List<String>, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.Start) { Text(title.uppercase(), color = Muted, fontSize = 8.sp, letterSpacing = .6.sp); Text(selected, color = Ink, fontSize = 11.sp, maxLines = 1) }
                PennyIcon("chevron-down", tint = Muted, modifier = Modifier.size(16.dp))
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false }) }
        }
    }
}

@Composable
private fun CategoryInsights(transactions: List<MoneyTransaction>, budgets: List<BudgetProgress>, categories: List<String>, onSaveBudget: (CategoryBudget) -> Unit) {
    var showBudgetDialog by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf(InsightPeriod.MONTH) }
    val now = Clock.System.now().toEpochMilliseconds()
    val periodTransactions = remember(transactions, selectedPeriod, now / 60_000) {
        val start = selectedPeriod.days?.let { now - it * 86_400_000L }
        if (start == null) transactions else transactions.filter { it.occurredAtEpochMillis >= start }
    }
    val expenses = periodTransactions.filter { it.type == TransactionType.EXPENSE }
    val totals = expenses.groupBy { it.category }.mapValues { (_, rows) -> rows.sumOf { it.amountMinor } }.toList().sortedByDescending { it.second }
    val totalExpenses = totals.sumOf { it.second }
    Text("Insights", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = Ink)
    Spacer(Modifier.height(5.dp)); Text("See the patterns behind your spending", color = Muted, fontSize = 12.sp)
    Spacer(Modifier.height(13.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        InsightPeriod.entries.forEach { period ->
            FilterChip(selected = selectedPeriod == period, onClick = { selectedPeriod = period }, label = { Text(period.label) })
        }
    }
    val periodIncome = periodTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amountMinor }
    val netSavings = periodIncome - totalExpenses
    val savingsRate = if (periodIncome > 0) (netSavings.toDouble() * 100.0 / periodIncome.toDouble()).toLong().coerceIn(-100, 100) else 0
    Spacer(Modifier.height(10.dp))
    SavingsSummaryCard(periodIncome, totalExpenses, netSavings, savingsRate)
    Spacer(Modifier.height(12.dp))
    MonthlyTrendChart(transactions)
    Spacer(Modifier.height(17.dp))
    Text("Spending by category", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Ink)
    Spacer(Modifier.height(10.dp))
    if (totals.isNotEmpty()) SpendingDonutChart(totals)
    if (totals.isEmpty()) EmptyState() else totals.forEach { (category, amount) ->
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(15.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(category, color = Ink, fontWeight = FontWeight.SemiBold); Text(formatMoney(amount), color = Ink, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.height(8.dp)); LinearProgressIndicator(progress = { if (totalExpenses == 0L) 0f else (amount.toFloat() / totalExpenses).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(6.dp), color = Green, trackColor = Mint)
            }
        }
    }
    Spacer(Modifier.height(20.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column { Text("Monthly budgets", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Ink); Text("Track spending against your limits", fontSize = 11.sp, color = Muted) }
        Text("+ Set", color = Green, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { showBudgetDialog = true })
    }
    Spacer(Modifier.height(10.dp))
    val thisMonth = monthKey(Clock.System.now().toEpochMilliseconds())
    val currentBudgets = budgets.filter { it.budget.monthKey == thisMonth }
    if (currentBudgets.isEmpty()) {
        Text("Set a category limit to track monthly spending.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 10.dp))
    } else currentBudgets.forEach { progress -> BudgetProgressCard(progress) }
    if (showBudgetDialog) BudgetDialog(
        categories = categories,
        onDismiss = { showBudgetDialog = false },
        onSave = { category, limit ->
            val budget = CategoryBudget("$thisMonth:${category.lowercase()}", category, limit, thisMonth)
            onSaveBudget(budget)
            showBudgetDialog = false
        },
    )
}

@Composable
private fun SavingsSummaryCard(income: Long, expenses: Long, net: Long, savingsRate: Long) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text("NET SAVINGS", color = Muted, fontSize = 9.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold); Text(formatMoney(net), color = if (net >= 0) Green else Coral, fontSize = 23.sp, fontWeight = FontWeight.Bold) }
                Surface(shape = RoundedCornerShape(14.dp), color = if (savingsRate >= 0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        PennyIcon(if (savingsRate >= 0) "trending-up" else "trending-down", tint = if (savingsRate >= 0) Green else Coral, modifier = Modifier.size(15.dp))
                        Text("$savingsRate% saved", color = if (savingsRate >= 0) Green else Coral, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(13.dp))
            MetricBar("Income", income, maxOf(income, expenses), Green)
            Spacer(Modifier.height(10.dp))
            MetricBar("Spending", expenses, maxOf(income, expenses), Coral)
        }
    }
}

@Composable
private fun MonthlyTrendChart(transactions: List<MoneyTransaction>) {
    val monthValues = transactions.groupBy { monthKey(it.occurredAtEpochMillis) }.toList().sortedBy { it.first }.takeLast(6)
    val incomeColor = Green
    val expenseColor = Coral
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text("Cash flow trend", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold); Text("Income vs spending by month", color = Muted, fontSize = 10.sp) }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { ChartLegendDot("Income", Green); ChartLegendDot("Spent", Coral) }
            }
            Spacer(Modifier.height(15.dp))
            if (monthValues.isEmpty()) {
                Text("Add transactions to see your monthly trend.", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(vertical = 18.dp))
            } else {
                val maxValue = monthValues.maxOf { (_, items) ->
                    maxOf(items.filter { it.type == TransactionType.INCOME }.sumOf { it.amountMinor }, items.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountMinor })
                }.coerceAtLeast(1)
                DrawCanvas(Modifier.fillMaxWidth().height(116.dp)) {
                    val groupWidth = size.width / monthValues.size
                    val barWidth = (groupWidth * .24f).coerceAtMost(17.dp.toPx())
                    monthValues.forEachIndexed { index, (_, items) ->
                        val income = items.filter { it.type == TransactionType.INCOME }.sumOf { it.amountMinor }
                        val expenses = items.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountMinor }
                        val base = size.height - 20.dp.toPx()
                        val maxHeight = size.height - 30.dp.toPx()
                        val x = groupWidth * index + groupWidth / 2
                        val incomeHeight = maxHeight * income.toFloat() / maxValue.toFloat()
                        val expenseHeight = maxHeight * expenses.toFloat() / maxValue.toFloat()
                        drawRect(incomeColor, Offset(x - barWidth - 2.dp.toPx(), base - incomeHeight), Size(barWidth, incomeHeight))
                        drawRect(expenseColor, Offset(x + 2.dp.toPx(), base - expenseHeight), Size(barWidth, expenseHeight))
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    monthValues.forEach { (key, _) ->
                        val month = key.substringAfter('-').toIntOrNull()?.minus(1)?.coerceIn(0, 11) ?: 0
                        Text(listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")[month], color = Muted, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartLegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(Modifier.size(7.dp), shape = CircleShape, color = color) {}
        Text(label, color = Muted, fontSize = 9.sp)
    }
}

@Composable
private fun SpendingDonutChart(totals: List<Pair<String, Long>>) {
    val palette = listOf(Color(0xFF20A879), Color(0xFFFF806E), Color(0xFF6488E8), Color(0xFFE4A33A), Color(0xFF9B72D0))
    val visible = if (totals.size > 5) totals.take(4) + ("Other" to totals.drop(4).sumOf { it.second }) else totals
    val total = visible.sumOf { it.second }.coerceAtLeast(1)
    Card(Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(148.dp), contentAlignment = Alignment.Center) {
                DrawCanvas(Modifier.fillMaxSize()) {
                    var start = -90f
                    val diameterInset = 12.dp.toPx()
                    visible.forEachIndexed { index, (_, amount) ->
                        val sweep = 360f * amount.toFloat() / total.toFloat()
                        drawArc(palette[index % palette.size], start + 1.5f, (sweep - 3f).coerceAtLeast(0f), false, Offset(diameterInset, diameterInset), Size(size.width - diameterInset * 2, size.height - diameterInset * 2), style = Stroke(20.dp.toPx(), cap = StrokeCap.Round))
                        start += sweep
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("SPENT", color = Muted, fontSize = 9.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold); Text(formatMoney(totalExpensesForChart(visible)), color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                visible.forEachIndexed { index, (category, amount) ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Surface(Modifier.size(8.dp), shape = CircleShape, color = palette[index % palette.size]) {}
                        Column(Modifier.weight(1f)) { Text(category, color = Ink, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis); Text("${(amount * 100 / total)}%", color = Muted, fontSize = 10.sp) }
                    }
                }
            }
        }
    }
}

private fun totalExpensesForChart(items: List<Pair<String, Long>>) = items.sumOf { it.second }

@Composable
private fun BudgetProgressCard(progress: BudgetProgress) {
    val warning = progress.isOverBudget || progress.isNearLimit
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(15.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(progress.budget.category, color = Ink, fontWeight = FontWeight.SemiBold)
                Text(if (progress.isOverBudget) "Over by ${formatMoney(progress.spentMinor - progress.budget.limitMinor)}" else "${formatMoney(progress.remainingMinor)} left", color = if (warning) Coral else Green, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
            if (warning) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    PennyIcon("alert-circle-outline", tint = Coral, modifier = Modifier.size(14.dp))
                    Text(if (progress.isOverBudget) "Monthly limit exceeded" else "You're close to this month's limit", color = Coral, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(5.dp)); Text("${formatMoney(progress.spentMinor)} of ${formatMoney(progress.budget.limitMinor)}", color = Muted, fontSize = 11.sp)
            Spacer(Modifier.height(8.dp)); LinearProgressIndicator(progress = { progress.progress }, modifier = Modifier.fillMaxWidth().height(6.dp), color = if (warning) Coral else Green, trackColor = Mint)
        }
    }
}

@Composable
private fun BudgetDialog(categories: List<String>, onDismiss: () -> Unit, onSave: (String, Long) -> Unit) {
    var category by remember { mutableStateOf(categories.firstOrNull().orEmpty()) }
    var amount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Set monthly budget") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CategoryPicker(category, categories, { category = it })
            OutlinedTextField(value = amount, onValueChange = { amount = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text("Monthly limit (${LocalCurrency.current.code})") }, singleLine = true)
            error?.let { Text(it, color = Coral, fontSize = 12.sp) }
        }
    }, confirmButton = { TextButton(onClick = {
        val limit = amount.toMinorUnits()
        if (category.isBlank()) error = "Enter a category."
        else if (limit == null || limit <= 0) error = "Enter a limit greater than zero."
        else onSave(category.trim(), limit)
    }) { Text("Save budget") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Muted) } })
}

@Composable
private fun CategoryPicker(selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Card(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .48f)),
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(11.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(38.dp)) {
                    Box(contentAlignment = Alignment.Center) { PennyIcon(categoryIcon(selected, TransactionType.EXPENSE), tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(19.dp)) }
                }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text("CATEGORY", color = Muted, fontSize = 9.sp, letterSpacing = .8.sp, fontWeight = FontWeight.SemiBold)
                    Text(selected, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                PennyIcon("chevron-down", tint = Muted, modifier = Modifier.size(19.dp))
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontWeight = if (option == selected) FontWeight.SemiBold else FontWeight.Normal) },
                    leadingIcon = { Surface(shape = RoundedCornerShape(9.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(32.dp)) { Box(contentAlignment = Alignment.Center) { PennyIcon(categoryIcon(option, TransactionType.EXPENSE), tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(16.dp)) } } },
                    trailingIcon = { if (option == selected) PennyIcon("check-circle", tint = Green, modifier = Modifier.size(19.dp)) },
                    onClick = { onSelect(option); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun CategoryManager(categories: List<FinanceCategory>, used: Set<String>, onSave: (List<FinanceCategory>) -> Unit) {
    var pendingDelete by remember { mutableStateOf<FinanceCategory?>(null) }
    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("YOUR CATEGORIES", color = Muted, fontSize = 10.sp, letterSpacing = 1.3.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text("${categories.size} categories", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                PennyIcon("shape-outline", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(10.dp).size(18.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                categories.forEachIndexed { index, category ->
                    Row(Modifier.fillMaxWidth().heightIn(min = 62.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(38.dp)) {
                            Box(contentAlignment = Alignment.Center) { PennyIcon(categoryIcon(category.name, TransactionType.EXPENSE), tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(19.dp)) }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(category.name, Modifier.weight(1f), color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        if (category.name in used) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                                Text("IN USE", color = Muted, fontSize = 9.sp, letterSpacing = .5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp))
                            }
                        } else {
                            IconButton(onClick = { pendingDelete = category }, modifier = Modifier.size(42.dp)) { PennyIcon("delete-outline", tint = Coral, modifier = Modifier.size(19.dp), contentDescription = localize("Delete category", LocalAppLanguage.current)) }
                        }
                    }
                    if (index < categories.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .5f))
                }
            }
        }
    }
    pendingDelete?.let { category ->
        AlertDialog(onDismissRequest = { pendingDelete = null }, title = { Text("Remove category?") }, text = { Text("${category.name} will no longer be available for new entries. Existing transactions will keep their category.") }, confirmButton = {
            TextButton(onClick = { onSave(categories.filterNot { it.name == category.name }); pendingDelete = null }) { Text("Remove", color = Coral) }
        }, dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } })
    }
}

@Composable
private fun CategorySettingsEntry(onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(13.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) { PennyIcon("shape-outline", tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Categories", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("Create and organize your spending labels", color = Muted, fontSize = 11.sp)
            }
            PennyIcon("chevron-right", tint = Muted, modifier = Modifier.size(19.dp))
        }
    }
}

@Composable
private fun BackupManagementScreen(onCreateBackup: () -> Unit, onRestoreBackup: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Row(Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = .75f), modifier = Modifier.size(54.dp)) {
                    Box(contentAlignment = Alignment.Center) { PennyIcon("shield-lock-outline", tint = Green, modifier = Modifier.size(27.dp)) }
                }
                Column(Modifier.weight(1f)) {
                    Text("Your finances, protected", color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Create a passphrase-protected backup or restore your data from a backup file.", color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
                }
            }
        }
        BackupActionCard(
            icon = "cloud-download-outline",
            title = "Create a backup",
            description = "Save an encrypted copy of your transactions, budgets, categories, and settings.",
            action = "Create backup",
            onClick = onCreateBackup,
        )
        BackupActionCard(
            icon = "backup-restore",
            title = "Restore a backup",
            description = "Choose a Penny backup file and enter its passphrase to restore your data.",
            action = "Choose backup file",
            onClick = onRestoreBackup,
        )
        Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .58f)) {
            Row(Modifier.fillMaxWidth().padding(15.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                PennyIcon("information-outline", tint = Muted, modifier = Modifier.size(18.dp))
                Text("Keep your backup passphrase somewhere safe. Penny cannot recover a forgotten passphrase.", color = Muted, fontSize = 11.sp, lineHeight = 16.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun BackupActionCard(icon: String, title: String, description: String, action: String, onClick: () -> Unit) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(11.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(13.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(42.dp)) {
                    Box(contentAlignment = Alignment.Center) { PennyIcon(icon, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(21.dp)) }
                }
                Text(title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            Text(description, color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
            Button(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp)) {
                Text(action, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun BackupSettingsEntry(onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(13.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) { PennyIcon("cloud-lock-outline", tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Backup & restore", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("Create a passphrase-protected backup file", color = Muted, fontSize = 11.sp)
            }
            PennyIcon("chevron-right", tint = Muted, modifier = Modifier.size(19.dp))
        }
    }
}

@Composable
private fun LanguageSetting(language: AppLanguage, onLanguageChange: (AppLanguage) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(13.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) { PennyIcon("translate", tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Language", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(language.displayName, color = Muted, fontSize = 11.sp)
            }
            Box {
                IconButton(onClick = { expanded = true }) { PennyIcon("chevron-down", tint = Muted, modifier = Modifier.size(21.dp)) }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    AppLanguage.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.displayName) },
                            leadingIcon = { if (option == language) PennyIcon("check", tint = Green, modifier = Modifier.size(18.dp)) },
                            onClick = { expanded = false; onLanguageChange(option) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderSetting(enabled: Boolean, onEnabledChange: (Boolean) -> Unit) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(13.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) { PennyIcon("bell-badge-outline", tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Local reminders", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("Upcoming recurring transactions and budgets nearing their limit", color = Muted, fontSize = 11.sp)
            }
            Switch(checked = enabled, onCheckedChange = onEnabledChange)
        }
    }
}

@Composable
private fun CategoryManagementScreen(
    categories: List<FinanceCategory>,
    used: Set<String>,
    onSave: (List<FinanceCategory>) -> Unit,
) {
    Spacer(Modifier.height(4.dp))
    CategoryManager(categories, used, onSave)
}

@Composable
private fun ProfileContent(
    appearanceMode: AppearanceMode,
    onAppearanceModeChange: (AppearanceMode) -> Unit,
    currency: DisplayCurrency,
    onCurrencyChange: (DisplayCurrency) -> Unit,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    remindersEnabled: Boolean,
    onRemindersEnabledChange: (Boolean) -> Unit,
    appLockEnabled: Boolean,
    onAppLockEnabledChange: (Boolean, (AppLockChangeResult) -> Unit) -> Unit,
    onManageCategories: () -> Unit,
    onManageBackup: () -> Unit,
) {
    Text("Settings", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Ink, letterSpacing = (-.6).sp)
    Spacer(Modifier.height(5.dp))
    Text("Make Penny feel like yours", color = Muted, fontSize = 13.sp)
    Spacer(Modifier.height(20.dp))
    Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = .78f), modifier = Modifier.size(52.dp)) {
                Box(contentAlignment = Alignment.Center) { PennyIcon("account-outline", tint = Green, modifier = Modifier.size(26.dp)) }
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text("Your Penny space", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Personal finance, made simple", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .74f), fontSize = 12.sp)
            }
        }
    }
    Spacer(Modifier.height(24.dp))
    Text("APPEARANCE", color = Muted, fontSize = 10.sp, letterSpacing = 1.3.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(9.dp))
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(13.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) { PennyIcon("theme-light-dark", tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp)) }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Theme", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("Choose your preferred look", color = Muted, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(15.dp))
            Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AppearanceMode.entries.forEach { mode ->
                    val selected = mode == appearanceMode
                    val modeBg by animateColorAsState(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent, label = "theme-choice")
                    val modeFg by animateColorAsState(if (selected) MaterialTheme.colorScheme.onPrimary else Ink, label = "theme-choice-text")
                    Row(
                        Modifier.weight(1f).clip(RoundedCornerShape(13.dp)).background(modeBg).clickable { onAppearanceModeChange(mode) }.padding(vertical = 11.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PennyIcon(when (mode) { AppearanceMode.SYSTEM -> "cellphone-cog"; AppearanceMode.LIGHT -> "white-balance-sunny"; AppearanceMode.DARK -> "moon-waning-crescent" }, tint = modeFg, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }, color = modeFg, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(14.dp))
    Text("CURRENCY", color = Muted, fontSize = 10.sp, letterSpacing = 1.3.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(9.dp))
    CurrencySetting(currency = currency, onCurrencyChange = onCurrencyChange)
    Spacer(Modifier.height(14.dp))
    LanguageSetting(language = language, onLanguageChange = onLanguageChange)
    Spacer(Modifier.height(14.dp))
    ReminderSetting(enabled = remindersEnabled, onEnabledChange = onRemindersEnabledChange)
    Spacer(Modifier.height(14.dp))
    CategorySettingsEntry(onClick = onManageCategories)
    Spacer(Modifier.height(14.dp))
    BackupSettingsEntry(onClick = onManageBackup)
    Spacer(Modifier.height(14.dp))
    AppLockSetting(appLockEnabled, onAppLockEnabledChange)
}

@Composable
private fun ScheduledTransactionsScreen(
    rules: List<RecurringRule>,
    onEdit: (RecurringRule) -> Unit,
    onDelete: (RecurringRule) -> Unit,
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (rules.isEmpty()) {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    PennyIcon("calendar-refresh-outline", tint = Green, modifier = Modifier.size(25.dp))
                    Text("Nothing scheduled yet", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("Choose a repeat schedule when adding a transaction.", color = Muted, fontSize = 12.sp)
                }
            }
        } else rules.sortedBy { it.nextDueAtEpochMillis }.forEach { rule ->
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(13.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(42.dp)) {
                        Box(contentAlignment = Alignment.Center) { PennyIcon(categoryIcon(rule.category, rule.type), tint = Green, modifier = Modifier.size(20.dp)) }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(rule.title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${rule.frequency.name.lowercase().replaceFirstChar { it.uppercase() }} · ${rule.category}", color = Muted, fontSize = 11.sp)
                        Text("Next · ${isoDateForEpoch(rule.nextDueAtEpochMillis)}", color = Muted, fontSize = 10.sp)
                    }
                    Text(formatMoney(rule.amountMinor), color = if (rule.type == TransactionType.INCOME) Green else Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { onEdit(rule) }, modifier = Modifier.size(40.dp)) {
                        PennyIcon("pencil-outline", tint = Green, modifier = Modifier.size(18.dp), contentDescription = localize("Edit recurring transaction", LocalAppLanguage.current))
                    }
                    IconButton(onClick = { onDelete(rule) }, modifier = Modifier.size(40.dp)) {
                        PennyIcon("delete-outline", tint = Coral, modifier = Modifier.size(20.dp), contentDescription = localize("Delete recurring transaction", LocalAppLanguage.current))
                    }
                }
            }
        }
    }
}

@Composable
private fun AppLockSetting(enabled: Boolean, onEnabledChange: (Boolean, (AppLockChangeResult) -> Unit) -> Unit) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(13.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) { PennyIcon("shield-lock-outline", tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("App lock", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("Require your device authentication", color = Muted, fontSize = 11.sp)
            }
            Switch(checked = enabled, onCheckedChange = { requested -> onEnabledChange(requested) { } })
        }
    }
}

@Composable
private fun AppLockScreen(onUnlock: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(76.dp)) {
            Box(contentAlignment = Alignment.Center) { PennyIcon("lock-outline", tint = Green, modifier = Modifier.size(34.dp)) }
        }
        Spacer(Modifier.height(20.dp))
        Text("Penny is locked", color = Ink, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(7.dp))
        Text("Verify your device to view your finances.", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(22.dp))
        Button(onClick = onUnlock, shape = RoundedCornerShape(16.dp)) {
            PennyIcon("fingerprint", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(8.dp)); Text("Unlock Penny")
        }
    }
}

@Composable
private fun RecurringRuleEditDialog(rule: RecurringRule, onDismiss: () -> Unit, onSave: (Long, RecurrenceFrequency, Long) -> Unit) {
    var amount by remember(rule.id) { mutableStateOf(minorUnitsInput(rule.amountMinor)) }
    var frequency by remember(rule.id) { mutableStateOf(rule.frequency) }
    var date by remember(rule.id) { mutableStateOf(isoDateForEpoch(rule.nextDueAtEpochMillis)) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit recurring transaction") },
        text = {
            Column(Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text(rule.title, color = Muted, fontSize = 12.sp)
                OutlinedTextField(value = amount, onValueChange = { amount = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text("Amount (${LocalCurrency.current.code})") }, singleLine = true)
                OutlinedTextField(value = date, onValueChange = { date = it.take(10) }, label = { Text("Next due date (YYYY-MM-DD)") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RecurrenceFrequency.entries.forEach { option ->
                        FilterChip(selected = frequency == option, onClick = { frequency = option }, label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) })
                    }
                }
                error?.let { Text(it, color = Coral, fontSize = 12.sp) }
            }
        },
        confirmButton = { TextButton(onClick = {
            val parsedAmount = amount.toMinorUnits()
            val parsedDate = runCatching { LocalDate.parse(date) }.getOrNull()
            if (parsedAmount == null || parsedAmount <= 0) error = "Enter an amount greater than zero."
            else if (parsedDate == null) error = "Enter a valid date as YYYY-MM-DD."
            else {
                val localTime = Instant.fromEpochMilliseconds(rule.nextDueAtEpochMillis).toLocalDateTime(TimeZone.currentSystemDefault()).time
                val dueAt = LocalDateTime(parsedDate, localTime).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                onSave(parsedAmount, frequency, dueAt)
            }
        }) { Text("Save changes") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Muted) } },
    )
}

@Composable
private fun CurrencySetting(currency: DisplayCurrency, onCurrencyChange: (DisplayCurrency) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(13.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) { PennyIcon("cash-multiple", tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp)) }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Display currency", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("${currency.displayName} (${currency.code})", color = Muted, fontSize = 11.sp)
                }
                Box {
                    IconButton(onClick = { expanded = true }) { PennyIcon("chevron-down", tint = Muted, modifier = Modifier.size(21.dp)) }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DisplayCurrency.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text("${option.displayName} (${option.code})") },
                                leadingIcon = { if (option == currency) PennyIcon("check", tint = Green, modifier = Modifier.size(18.dp)) },
                                onClick = { expanded = false; onCurrencyChange(option) },
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(9.dp))
            Text("Changing this updates the currency label across your ledger. Amounts are not converted.", color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun BalanceCard(balance: Long) {
    val balanceBackground = Color(0xFF12372D)
    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = balanceBackground), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(22.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("TOTAL BALANCE", color = Color(0xFFB7C9C1), fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.SemiBold); PennyIcon("wallet-outline", tint = Color(0xFFB7C9C1), modifier = Modifier.size(18.dp)) }
            Spacer(Modifier.height(10.dp)); Text(formatMoney(balance), color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp)
        }
    }
}

@Composable
private fun SummaryCard(label: String, amount: Long, icon: String, modifier: Modifier, accent: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(label, color = Muted, fontSize = 12.sp)
                Surface(shape = CircleShape, color = if (accent == Green) Mint else MaterialTheme.colorScheme.errorContainer, modifier = Modifier.size(30.dp)) { Box(contentAlignment = Alignment.Center) { PennyIcon(icon, tint = accent, modifier = Modifier.size(16.dp)) } }
            }
            Spacer(Modifier.height(9.dp)); Text(formatMoney(amount), color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis); Text("All time", color = Muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun EmptyState() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 26.dp, horizontal = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, color = Mint, modifier = Modifier.size(52.dp)) { Box(contentAlignment = Alignment.Center) { PennyIcon("wallet-plus-outline", tint = Green, modifier = Modifier.size(24.dp)) } }
            Spacer(Modifier.height(11.dp)); Text("Your money story starts here", fontWeight = FontWeight.SemiBold, color = Ink)
            Spacer(Modifier.height(4.dp)); Text("Add your first transaction to see your activity.", color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun TransactionRow(transaction: MoneyTransaction, onEdit: () -> Unit, onDelete: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = if (transaction.type == TransactionType.INCOME) Mint else MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(42.dp)) { Box(contentAlignment = Alignment.Center) { PennyIcon(categoryIcon(transaction.category, transaction.type), tint = if (transaction.type == TransactionType.INCOME) Green else Ink, modifier = Modifier.size(20.dp)) } }
        Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(transaction.title, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(transaction.category, color = Muted, fontSize = 11.sp) }
        Text((if (transaction.type == TransactionType.INCOME) "+ " else "− ") + formatMoney(transaction.amountMinor), color = if (transaction.type == TransactionType.INCOME) Green else Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Box {
            IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(34.dp)) { PennyIcon("dots-vertical", tint = Muted, modifier = Modifier.size(18.dp)) }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(text = { Text("Edit") }, leadingIcon = { PennyIcon("pencil-outline", tint = Ink, modifier = Modifier.size(18.dp)) }, onClick = { menuExpanded = false; onEdit() })
                DropdownMenuItem(text = { Text("Delete", color = Coral) }, leadingIcon = { PennyIcon("delete-outline", tint = Coral, modifier = Modifier.size(18.dp)) }, onClick = { menuExpanded = false; onDelete() })
            }
        }
    }
}

@Composable
private fun InsightsCard(expense: Long, income: Long) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Text("Cash flow", color = Ink, fontWeight = FontWeight.Bold, fontSize = 15.sp); Text("Income and expenses at a glance", color = Muted, fontSize = 11.sp)
            Spacer(Modifier.height(17.dp)); MetricBar("Income", income, income, Green)
            Spacer(Modifier.height(12.dp)); MetricBar("Expenses", expense, income, Coral)
            Spacer(Modifier.height(12.dp)); Text(if (income == 0L && expense == 0L) "Insights will appear as you log activity." else "You're building a clearer picture of your finances.", color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun UpcomingRecurringCard(rules: List<RecurringRule>, onManage: () -> Unit) {
    Card(onClick = onManage, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Coming up", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Scheduled transactions", color = Muted, fontSize = 10.sp)
                }
                PennyIcon("calendar-clock-outline", tint = Green, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.height(8.dp))
            if (rules.isEmpty()) {
                Text("Nothing scheduled yet", color = Muted, fontSize = 11.sp)
            } else rules.sortedBy { it.nextDueAtEpochMillis }.take(3).forEach { rule ->
                Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(32.dp)) {
                        Box(contentAlignment = Alignment.Center) { PennyIcon(categoryIcon(rule.category, rule.type), tint = Green, modifier = Modifier.size(16.dp)) }
                    }
                    Spacer(Modifier.width(9.dp))
                    Column(Modifier.weight(1f)) {
                        Text(rule.title, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${rule.frequency.name.lowercase().replaceFirstChar { it.uppercase() }} · ${isoDateForEpoch(rule.nextDueAtEpochMillis)}", color = Muted, fontSize = 10.sp)
                    }
                    Text(formatMoney(rule.amountMinor), color = if (rule.type == TransactionType.INCOME) Green else Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("Manage scheduled transactions  ›", color = Green, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun MetricBar(label: String, value: Long, total: Long, color: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, color = Muted, fontSize = 11.sp); Text(formatMoney(value), color = Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
    Spacer(Modifier.height(6.dp)); LinearProgressIndicator(progress = { if (total == 0L) 0f else (value.toFloat() / total).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(6.dp), color = color, trackColor = color.copy(alpha = .12f))
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AddTransactionScreen(initialTransaction: MoneyTransaction?, categories: List<String>, onDismiss: () -> Unit, onSave: (MoneyTransaction, RecurrenceFrequency?) -> Unit) {
    var title by remember(initialTransaction?.id) { mutableStateOf(initialTransaction?.title.orEmpty()) }
    var amount by remember(initialTransaction?.id) { mutableStateOf(initialTransaction?.amountMinor?.let(::minorUnitsInput).orEmpty()) }
    var category by remember(initialTransaction?.id) { mutableStateOf(initialTransaction?.category ?: categories.firstOrNull() ?: "Other") }
    var selectedDate by remember(initialTransaction?.id) {
        mutableStateOf(initialTransaction?.occurredAtEpochMillis?.let(::dateForEpoch) ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var note by remember(initialTransaction?.id) { mutableStateOf(initialTransaction?.note.orEmpty()) }
    var type by remember(initialTransaction?.id) { mutableStateOf(initialTransaction?.type ?: TransactionType.EXPENSE) }
    var recurrence by remember(initialTransaction?.id) { mutableStateOf<RecurrenceFrequency?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = datePickerMillis(selectedDate))
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { selectedDate = dateForPickerMillis(it) }
                    showDatePicker = false
                }) { Text("Apply date") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = Muted) } },
        ) {
            DatePicker(state = pickerState, showModeToggle = false)
        }
    }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { TransactionType.entries.forEach { option ->
                val selected = type == option
                Surface(
                    onClick = { type = option },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .25f) else MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Row(Modifier.padding(vertical = 15.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        PennyIcon(if (option == TransactionType.EXPENSE) "arrow-top-right" else "arrow-bottom-left", tint = if (selected) Green else Muted, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(7.dp))
                        Text(if (option == TransactionType.EXPENSE) "Expense" else "Income", color = if (selected) Ink else Muted, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            } }
            OutlinedTextField(value = title, onValueChange = { title = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text("Description") }, singleLine = true, shape = RoundedCornerShape(16.dp))
            OutlinedTextField(value = amount, onValueChange = { amount = it.filter { ch -> ch.isDigit() || ch == '.' }; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text("Amount (${LocalCurrency.current.code})") }, singleLine = true, shape = RoundedCornerShape(16.dp))
            CategoryPicker(category, (categories + category).distinct(), { category = it })
            Card(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .48f)),
                ) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(11.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(38.dp)) {
                            Box(contentAlignment = Alignment.Center) { PennyIcon("calendar-month-outline", tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(19.dp)) }
                        }
                        Spacer(Modifier.width(11.dp))
                        Column(Modifier.weight(1f)) {
                            Text("DATE", color = Muted, fontSize = 9.sp, letterSpacing = .8.sp, fontWeight = FontWeight.SemiBold)
                            Text(formatDateLabel(selectedDate, LocalAppLanguage.current), color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        PennyIcon("pencil-outline", tint = Muted, modifier = Modifier.size(18.dp))
                    }
                }
            OutlinedTextField(value = note, onValueChange = { note = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Note (optional)") }, minLines = 2, maxLines = 3, shape = RoundedCornerShape(16.dp))
            Text("Repeat", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Ink)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = recurrence == null, onClick = { recurrence = null }, label = { Text("Never") })
                FilterChip(selected = recurrence == RecurrenceFrequency.WEEKLY, onClick = { recurrence = RecurrenceFrequency.WEEKLY }, label = { Text("Weekly") })
                FilterChip(selected = recurrence == RecurrenceFrequency.MONTHLY, onClick = { recurrence = RecurrenceFrequency.MONTHLY }, label = { Text("Monthly") })
            }
            error?.let { Text(it, color = Coral, fontSize = 12.sp) }
        }
        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
            Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 22.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) { Text("Cancel") }
                Button(onClick = {
            val parsedMinor = amount.toMinorUnits()
            when { title.isBlank() -> error = "Add a description."; parsedMinor == null || parsedMinor <= 0 -> error = "Enter an amount greater than zero."; category.isBlank() -> error = "Choose a category."; else -> {
                val zone = TimeZone.currentSystemDefault()
                val currentLocal = initialTransaction?.occurredAtEpochMillis?.let { Instant.fromEpochMilliseconds(it).toLocalDateTime(zone) }
                    ?: Clock.System.now().toLocalDateTime(zone)
                val timestamp = LocalDateTime(selectedDate, currentLocal.time).toInstant(zone).toEpochMilliseconds()
                onSave(MoneyTransaction(initialTransaction?.id ?: timestamp.toString(), title.trim(), parsedMinor, type, category.trim(), timestamp, note.trim()), recurrence)
            } }
                }, modifier = Modifier.weight(1.4f), shape = RoundedCornerShape(16.dp)) { Text(if (initialTransaction == null) "Save transaction" else "Save changes") }
            }
        }
    }
}

private fun isoDateForEpoch(epochMillis: Long): String = Instant.fromEpochMilliseconds(epochMillis)
    .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()

private fun dateForEpoch(epochMillis: Long): LocalDate = Instant.fromEpochMilliseconds(epochMillis)
    .toLocalDateTime(TimeZone.currentSystemDefault()).date

private fun datePickerMillis(date: LocalDate): Long = LocalDateTime(date, LocalTime(0, 0))
    .toInstant(TimeZone.UTC).toEpochMilliseconds()

private fun dateForPickerMillis(epochMillis: Long): LocalDate = Instant.fromEpochMilliseconds(epochMillis)
    .toLocalDateTime(TimeZone.UTC).date

private fun formatDateLabel(date: LocalDate, language: AppLanguage): String {
    val month = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
    val localizedMonth = when (language) {
        AppLanguage.ENGLISH -> month
        AppLanguage.PERSIAN -> persianLabels[month] ?: month
        else -> appTranslation(month, language) ?: month
    }
    val day = localizeDigits(date.day.toString(), language)
    val year = localizeDigits(date.year.toString(), language)
    val monthNumber = date.month.ordinal + 1
    return when (language) {
        AppLanguage.SPANISH -> "$day de $localizedMonth de $year"
        AppLanguage.FRENCH -> "$day $localizedMonth $year"
        AppLanguage.CHINESE -> "${year}年${monthNumber}月${day}日"
        AppLanguage.JAPANESE -> "${year}年${monthNumber}月${day}日"
        AppLanguage.KOREAN -> "${year}년 ${monthNumber}월 ${day}일"
        else -> "$localizedMonth $day, $year"
    }
}

@Composable
private fun formatMoney(minor: Long): String {
    val sign = if (minor < 0) "−" else ""
    val digits = minor.toString().removePrefix("-")
    val padded = digits.padStart(3, '0')
    val whole = padded.dropLast(2).chunkedFromRight(3).joinToString(",")
    val cents = padded.takeLast(2)
    return "${sign}${LocalCurrency.current.symbol}$whole.$cents"
}

private fun String.chunkedFromRight(size: Int): List<String> {
    val chunks = mutableListOf<String>()
    var end = length
    while (end > 0) { val start = (end - size).coerceAtLeast(0); chunks.add(substring(start, end)); end = start }
    return chunks.reversed()
}

private fun minorUnitsInput(minor: Long): String = "${minor / 100}.${(minor % 100).toString().padStart(2, '0')}"

private fun String.toMinorUnits(): Long? {
    val parts = trim().split('.')
    if (parts.size > 2 || parts[0].isEmpty() || parts[0].any { !it.isDigit() }) return null
    val fraction = parts.getOrNull(1).orEmpty()
    if (fraction.length > 2 || fraction.any { !it.isDigit() }) return null
    val whole = parts[0].toLongOrNull() ?: return null
    val cents = fraction.padEnd(2, '0').ifEmpty { "00" }.toLongOrNull() ?: return null
    if (whole > (Long.MAX_VALUE - cents) / 100) return null
    return whole * 100 + cents
}

private fun categoryIcon(category: String, type: TransactionType): String = when {
    type == TransactionType.INCOME -> "cash-plus"
    category.lowercase() in listOf("food", "groceries", "dining") -> "silverware-fork-knife"
    category.lowercase() in listOf("transport", "travel") -> "car-outline"
    category.equals("shopping", ignoreCase = true) -> "shopping-outline"
    else -> "shape-outline"
}

@Composable
private fun PennyIcon(name: String, tint: Color, modifier: Modifier = Modifier, contentDescription: String? = null) {
    val icon = remember(name) { Mdi.fromMdiName(name) ?: Mdi.fromMdiName("help-circle")!! }
    Icon(imageVector = icon.rememberImageVector(), contentDescription = contentDescription, modifier = modifier, tint = tint)
}
