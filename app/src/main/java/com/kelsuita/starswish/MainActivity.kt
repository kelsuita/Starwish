package com.kelsuita.starswish

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.os.Bundle
import android.provider.Settings
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.Serializable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import top.yukonga.miuix.kmp.utils.PagerNavigationSpringSpec
import top.yukonga.miuix.kmp.utils.PagerGestureNestedScrollConnection
import top.yukonga.miuix.kmp.utils.pagerGestureOverride
import top.yukonga.miuix.kmp.utils.springAnimateToPage
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.NavDisplayEffects
import top.yukonga.miuix.kmp.nav.core.NavKey
import top.yukonga.miuix.kmp.nav.core.rememberNavBackStack
import top.yukonga.miuix.kmp.nav.core.rememberNavSystemCornerRadius
import top.yukonga.miuix.kmp.nav.transition.NavSwipeDirection
import top.yukonga.miuix.kmp.nav.transition.NavTransitions
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.preference.CheckboxPreference
import top.yukonga.miuix.kmp.preference.RadioButtonPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.menu.OverlayIconDropdownMenu
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet
import top.yukonga.miuix.kmp.window.WindowDialog
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.BankCards
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Link
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.squircle.isSquircleEnabled
import top.yukonga.miuix.kmp.squircle.addSquircleRect
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import com.kelsuita.starswish.data.*
import com.kelsuita.starswish.ui.LocalStockPalette
import com.kelsuita.starswish.ui.isDarkScheme
import com.kelsuita.starswish.ui.primaryButtonColors
import com.kelsuita.starswish.ui.primaryTextButtonColors
import com.kelsuita.starswish.data.fetchHitokoto
import com.kelsuita.starswish.sync.XiaoxingAccessibilityService
import com.kelsuita.starswish.sync.WebDavClient
import com.kelsuita.starswish.sync.WebDavSettings
import com.kelsuita.starswish.sync.SnapshotBalanceReader
import com.kelsuita.starswish.sync.WebDavAccountSelection
import com.kelsuita.starswish.sync.WebDavBalanceChange
import com.kelsuita.starswish.sync.WebDavImportPlan
import com.kelsuita.starswish.sync.WebDavPickerAccount
import com.kelsuita.starswish.sync.XiaoxingBackupDecryptor
import com.kelsuita.starswish.sync.SyncError
import java.io.File
import java.text.Collator
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt
import org.json.JSONObject
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle

private val Blue = Color(0xFF3975F6)
private val AccentColors = listOf(Blue, Color(0xFF2EAA8C), Color(0xFFB6573B), Color(0xFF8068C8))

// Cards sit 16dp inside the page edge, so a 16dp title inset keeps section headings one grid step
// to their right, like the Miuix settings examples. Shared so every page uses the same gutter.
private val SectionTitlePadding = PaddingValues(start = 16.dp, top = 8.dp, bottom = 8.dp)
// Miuix only applies the palette style and colour spec when Monet is given a key colour; without
// one it returns the platform palette as-is. Read the wallpaper seed the platform palette is built
// from so those two settings work for Monet too. Null means the device has no wallpaper palette
// (Android 8-11), where Monet falls back to the stock Miuix colours.
@SuppressLint("InlinedApi")
private fun monetSeedColor(context: Context): Color? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) runCatching {
        val json = Settings.Secure.getString(context.contentResolver, "theme_customization_overlay_packages")
        val hex = json?.let { JSONObject(it).optString("android.theme.customization.system_palette") }.orEmpty()
        if (hex.isNotBlank()) {
            return Color(android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"))
        }
    }
    return Color(context.resources.getColor(android.R.color.system_accent1_500, context.theme))
}
@Serializable
private data class AppRoute(val page: Int) : NavKey
private val NavIcons = listOf(MiuixIcons.Home, MiuixIcons.BankCards, MiuixIcons.Settings)
/** All transient notices share one short, consistent Snackbar duration. */
private val NoticeDuration = SnackbarDuration.Custom(durationMillis = 2000)
// A full backup can approach the Bundle transaction limit. Persist the pending import across
// recreation only when it is comfortably small; otherwise the user can pick the file again.
private const val PENDING_BACKUP_SAVE_LIMIT = 256 * 1024
private val PendingBackupSaver = Saver<String?, String>(
    save = { json -> json?.takeIf { it.length <= PENDING_BACKUP_SAVE_LIMIT } },
    restore = { it },
)

class MainActivity : ComponentActivity() {
    private var revision by mutableIntStateOf(0)
    private var confirmBalanceRequested by mutableStateOf(false)
    override fun onResume() { super.onResume(); revision++ }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        confirmBalanceRequested = intent.action == ACTION_CONFIRM_BALANCE
        revision++
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        confirmBalanceRequested = intent.action == ACTION_CONFIRM_BALANCE
        enableEdgeToEdge()
        val store = SavingsStore(this)
        // The language lives outside the appearance snapshot: every screen reads it through
        // Languages.tr while composing, so updating it here repaints without a restart.
        Languages.set(AppLanguage.entries[store.language()])
        // Upgrade goal links before the banner or cards read the stored wishes.
        store.migrateGoalLinks()
        setContent {
            val appearance = remember(revision) { store.appearance() }
            val dark = when (appearance.themeMode) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }
            // Fixed accents derive their palette from the chosen seed. Monet derives it from the
            // wallpaper seed so the palette style and colour spec selections apply there as well;
            // without a seed Miuix would hand back the platform palette and ignore both settings.
            val monetSeed = remember { monetSeedColor(this@MainActivity) }
            val seed = if (appearance.accentIndex == 1) monetSeed else AccentColors.getOrNull(appearance.accentIndex - 2)
            val paletteStyle = ThemePaletteStyle.entries[appearance.paletteStyle]
            val colorSpec = ThemeColorSpec.entries[appearance.colorSpec]
            val controller = remember(seed, dark, paletteStyle, colorSpec, appearance.accentIndex) {
                ThemeController(
                    colorSchemeMode = when (appearance.accentIndex) {
                        0 -> if (dark) ColorSchemeMode.Dark else ColorSchemeMode.Light
                        1 -> ColorSchemeMode.MonetSystem
                        else -> if (dark) ColorSchemeMode.MonetDark else ColorSchemeMode.MonetLight
                    },
                    keyColor = seed,
                    paletteStyle = paletteStyle,
                    colorSpec = colorSpec,
                    isDark = dark,
                )
            }
            SideEffect {
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
            MiuixTheme(controller = controller) {
                CompositionLocalProvider(
                    LocalContentColor provides MiuixTheme.colorScheme.onBackground,
                    LocalStockPalette provides (appearance.accentIndex == 0),
                ) {
                    SavingsApp(store, revision, { revision++ }, appearance, dark, monetSeed, ::isReaderEnabled, ::openReaderSettings, ::openXiaoxing,
                        confirmBalanceRequested, { confirmBalanceRequested = false; intent.action = null })
                }
            }
        }
    }
    private fun isReaderEnabled(): Boolean {
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).orEmpty()
        val component = ComponentName(this, XiaoxingAccessibilityService::class.java)
        return enabled.split(':').any { ComponentName.unflattenFromString(it) == component }
    }
    private fun openReaderSettings() { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
    private fun openXiaoxing(): Boolean {
        val intent = packageManager.getLaunchIntentForPackage(XiaoxingAccessibilityService.XIAOXING_PACKAGE)
        return if (intent != null) {
            runCatching { startActivity(intent); true }.getOrDefault(false)
        } else false
    }
    companion object {
        const val ACTION_CONFIRM_BALANCE = "com.kelsuita.starswish.action.CONFIRM_BALANCE"
    }
}

@SuppressLint("NewApi")
@Composable
private fun SavingsApp(
    store: SavingsStore, revision: Int, refresh: () -> Unit,
    appearance: Appearance,
    dark: Boolean,
    monetSeed: Color?,
    readerEnabled: () -> Boolean, openReaderSettings: () -> Unit, openXiaoxing: () -> Boolean,
    confirmBalanceRequested: Boolean, onConfirmationOpened: () -> Unit,
) {
    val context = LocalContext.current
    val backStack = rememberNavBackStack<AppRoute>(AppRoute(0))
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    // Serialize sorting writes and keep list serialization off the UI thread.
    val sortCommitLock = remember { Mutex() }
    var sortDataVersion by remember { mutableIntStateOf(0) }
    var sortUndoJob by remember { mutableStateOf<Job?>(null) }
    // Name ordering follows the app language, not just the system one: pinyin for Chinese, so
    // Chinese titles do not end up in UTF-16 code point order.
    val nameCollator = remember(Languages.current) {
        Collator.getInstance(Locale.forLanguageTag(Languages.current.pack.ifEmpty { "en" }))
    }
    val goalNameOrder = remember(nameCollator) {
        Comparator<SavingGoal> { a, b -> nameCollator.compare(a.title, b.title) }
    }
    val accountNameOrder = remember(nameCollator) {
        Comparator<BankAccount> { a, b -> nameCollator.compare(a.name, b.name) }
    }
    // A sorting session can produce several drops; writes are coalesced and flushed on page exit.
    val goalSortQueue = remember { SortWriteQueue(scope) }
    val accountSortQueue = remember { SortWriteQueue(scope) }
    val vaultSortQueue = remember { SortWriteQueue(scope) }
    var navigationJob by remember { mutableStateOf<Job?>(null) }
    val selectedTab = pagerState.targetPage
    // Compact/medium/expanded breakpoints follow the Miuix adaptive shell: phones keep the
    // bottom bar, landscape phones and tablets switch to a side rail, and the widest windows
    // expand the rail. Content stays centred instead of stretching across a large window.
    val configuration = LocalConfiguration.current
    val wideLayout = configuration.screenWidthDp >= 600
    val landscape = configuration.screenWidthDp > configuration.screenHeightDp
    val useFloatingNav = appearance.floatingNavBar
    val useIosBar = useFloatingNav && appearance.floatingNavBarStyle == 1
    var iosBarBlurRadius by remember(store) { mutableIntStateOf(store.iosBarBlurRadius()) }
    val expandedNavigation = configuration.screenWidthDp >= 1000
    // Use the expandable rail overload so Miuix supplies its own toggle button. The width only
    // supplies the initial state; afterwards the user owns it and it survives rotation.
    val railState = rememberNavigationRailState(
        initialValue = if (expandedNavigation) NavigationRailValue.Expanded else NavigationRailValue.Collapsed,
    )
    val goalGrid = configuration.screenWidthDp >= 840
    // Landscape phones stay below the tablet breakpoint but are still wide enough for two
    // account columns, so the account grid also opens up when the window is short and wide.
    val accountGrid = goalGrid || (landscape && configuration.screenWidthDp >= 640)
    val layoutDirection = LocalLayoutDirection.current
    val versionName = remember { context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty() }
    // Only used by the "add everything up" banner; null when the platform hides system accounts.
    val deviceAccountName = remember { DeviceAccountName.resolve(context) }
    var hitokoto by remember { mutableStateOf(store.hitokoto()) }
    // The quote is a Chinese-only extra. A null choice follows the language default (on for
    // Chinese); once the user toggles it, that stored choice is kept across language switches.
    // Switching away from Chinese hides and effectively disables the quote without overwriting
    // the user's choice, so returning to Chinese can restore it unless it was manually disabled.
    var quoteChoice by remember { mutableStateOf(store.quoteEnabled()) }
    val quoteEnabled = quoteChoice ?: Languages.current.isChinese
    val quoteShown = Languages.current.isChinese && quoteEnabled
    var quoteLoading by remember { mutableStateOf(false) }
    // Fetches only when there is nothing cached; a manual tap refreshes regardless. Language
    // switches and the switch itself must not replace content the user is already reading.
    suspend fun loadQuote() {
        quoteLoading = true
        try {
            val fetched = fetchHitokoto()
            if (fetched != null) {
                store.saveHitokoto(fetched)
                hitokoto = fetched
            } else if (hitokoto == null) {
                // Keep the failure in memory only: the next launch should retry instead of
                // treating an error message as saved content (and exporting it in backups).
                hitokoto = tr("settings.quote_unavailable")
            }
        } finally {
            quoteLoading = false
        }
    }
    LaunchedEffect(quoteShown) {
        if (!quoteShown || hitokoto != null) return@LaunchedEffect
        loadQuote()
    }
    fun navigate(page: Int) {
        if (page >= 3) {
            if (backStack.lastOrNull() != AppRoute(page)) backStack.add(AppRoute(page))
        }
        else {
            while (backStack.size > 1) backStack.removeLastOrNull()
            navigationJob?.cancel()
            navigationJob = scope.launch { pagerState.springAnimateToPage(page) }
        }
    }
    fun popRoute() {
        if (backStack.size > 1) backStack.removeLastOrNull()
    }
    var editingGoalId by rememberSaveable { mutableStateOf<String?>(null) }
    var goalEditor by rememberSaveable { mutableStateOf(false) }
    var editingAccountId by rememberSaveable { mutableStateOf<String?>(null) }
    var accountEditor by rememberSaveable { mutableStateOf(false) }
    var disclosure by rememberSaveable { mutableStateOf(false) }
    var webDavSheet by rememberSaveable { mutableStateOf(false) }
    var webDavBusy by remember { mutableStateOf(false) }
    // Keep the request pending across Activity recreation until it gets a terminal result.
    var vaultRefreshRequest by rememberSaveable { mutableStateOf(0L) }
    var vaultRefreshHandled by rememberSaveable { mutableStateOf(0L) }
    var vaultRefreshInConfirmation by remember { mutableStateOf<Long?>(null) }
    var webDavUrl by rememberSaveable { mutableStateOf("") }
    var webDavAccount by rememberSaveable { mutableStateOf("") }
    var webDavPassword by rememberSaveable { mutableStateOf("") }
    // True once the user edits the password field. Used to keep the stored "connected" flag
    // only when the credentials really changed, and to avoid overwriting typing during a
    // background decrypt.
    var webDavPasswordEdited by remember { mutableStateOf(false) }
    // Accounts the pending read is limited to; empty means the whole backup. A goal card fills
    // it with that goal's linked accounts so the read touches nothing else.
    var webDavAccountIds by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var webDavPlan by remember { mutableStateOf<WebDavImportPlan?>(null) }
    var webDavChangesVisible by remember { mutableStateOf(false) }
    // Kept aside until the credential sheet has finished hiding: stacking two bottom sheets
    // leaves the outgoing one visible behind the confirmation and both disappear on drag.
    var pendingWebDavPlan by remember { mutableStateOf<WebDavImportPlan?>(null) }
    // The account picker opens only for the panel buttons, which have no target local account:
    // it lists the accounts found in the backup after the connection succeeds.
    var webDavPicker by remember { mutableStateOf<List<WebDavPickerAccount>>(emptyList()) }
    var webDavPickerVisible by remember { mutableStateOf(false) }
    var webDavPickerSelection by remember { mutableStateOf<Set<Long>>(emptySet()) }
    // Same reason as [pendingWebDavPlan]: the picker must wait for the credentials sheet to
    // finish hiding before it can animate in.
    var pendingWebDavPicker by remember { mutableStateOf<List<WebDavPickerAccount>?>(null) }
    // The goal sheet can request the account sheet; open it only after the goal sheet is gone.
    var openAccountAfterGoal by remember { mutableStateOf(false) }
    var aboutIconTaps by rememberSaveable { mutableIntStateOf(0) }
    // Delete targets are intentionally not saved across recreation: the confirmation carries
    // no user input, so restoring it is not worth serializing a whole model object. Visibility
    // is separate from the target so the dialog keeps its content while it hides.
    var deletingGoal by remember { mutableStateOf<SavingGoal?>(null) }
    var deletingAccount by remember { mutableStateOf<BankAccount?>(null) }
    var deletingGoalVisible by remember { mutableStateOf(false) }
    var deletingAccountVisible by remember { mutableStateOf(false) }
    // Deleting an account that wishes still link asks twice: the plain confirmation first, then a
    // second dialog spelling out which contribution is lost. [accountDeletePending] carries the
    // hand-off while the first dialog is still animating away.
    var accountDeleteStep2 by remember { mutableStateOf(false) }
    var accountDeletePending by remember { mutableStateOf(false) }
    var archivingGoal by remember { mutableStateOf<SavingGoal?>(null) }
    var archivingVisible by remember { mutableStateOf(false) }
    var removingGoal by remember { mutableStateOf<SavingGoal?>(null) }
    var removingVisible by remember { mutableStateOf(false) }
    // The restore confirmation explains which deleted accounts no longer contribute.
    var restoringMissingGoal by remember { mutableStateOf<SavingGoal?>(null) }
    var restoringMissingAccounts by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var restoringMissingVisible by remember { mutableStateOf(false) }
    // The pending import is kept as JSON so the confirmation survives recreation.
    var pendingBackupJson by rememberSaveable(stateSaver = PendingBackupSaver) { mutableStateOf<String?>(null) }
    var importVisible by rememberSaveable { mutableStateOf(false) }
    // The banner decimal count is edited in a sheet, so the draft only hits storage on confirm.
    var bannerDecimalsSheet by remember { mutableStateOf(false) }
    var bannerDecimalsDraft by remember { mutableIntStateOf(0) }
    // Account ids a goal card still has to read through the accessibility service, in order. The
    // queue keeps 「更新余额」 on the goal card instead of sending the user to the accounts page.
    var captureQueue by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var captureTotal by rememberSaveable { mutableIntStateOf(0) }
    val hapticFeedback = LocalHapticFeedback.current
    val pendingBackup = remember(pendingBackupJson) {
        pendingBackupJson?.let { runCatching { BackupData.fromJson(it) }.getOrNull() }
    }
    // A backup too large for saved state is intentionally dropped on recreation; close the
    // confirmation instead of leaving an invisible dialog blocking back navigation.
    LaunchedEffect(importVisible, pendingBackup) {
        if (importVisible && pendingBackup == null) importVisible = false
    }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(confirmBalanceRequested) {
        if (confirmBalanceRequested) {
            // A goal card's read keeps the user on the home page: the confirmation panel is
            // rendered there too, and the queue continues with the next account after each
            // confirm. Only a read started elsewhere needs the accounts page.
            if (captureQueue.isEmpty()) navigate(1)
            goalEditor = false
            accountEditor = false
            disclosure = false
            deletingGoalVisible = false
            deletingAccountVisible = false
            importVisible = false
            refresh()
            onConfirmationOpened()
        }
    }
    val storedAccounts = remember(revision) { store.accounts() }
    val storedGoals = remember(revision) { store.goals() }
    val goalIds = remember(storedGoals) { storedGoals.filterNot { it.archived }.map { it.id } }
    val vaultIds = remember(storedGoals) { storedGoals.filter { it.archived }.map { it.id } }
    val accountIds = remember(storedAccounts) { storedAccounts.map { it.id } }
    val goalSort = remember(goalIds.toSet(), sortDataVersion) {
        SortPageState(goalIds, store.sortSettings(SortList.GOALS))
    }
    val vaultSort = remember(vaultIds.toSet(), sortDataVersion) {
        SortPageState(vaultIds, store.sortSettings(SortList.VAULT))
    }
    val accountSort = remember(accountIds.toSet(), sortDataVersion) {
        SortPageState(accountIds, store.sortSettings(SortList.ACCOUNTS))
    }
    val goalsById = remember(storedGoals) { storedGoals.associateBy { it.id } }
    val accountsById = remember(storedAccounts) { storedAccounts.associateBy { it.id } }
    val goals = remember(goalsById, goalSort.order.value, vaultSort.order.value) {
        (goalSort.order.value + vaultSort.order.value).mapNotNull(goalsById::get)
    }
    val accounts = remember(accountsById, accountSort.order.value) { accountSort.order.value.mapNotNull(accountsById::get) }
    val developerMode = remember(revision) { store.developerModeEnabled() }
    val webDavMode = remember(revision) { store.webDavBalanceEnabled() }
    val webDavConnected = remember(revision) { store.webDavConnected() }
    val webDavActive = developerMode && webDavMode
    val editingGoal = goals.firstOrNull { it.id == editingGoalId }
    val editingAccount = accounts.firstOrNull { it.id == editingAccountId }
    val candidate = remember(revision) { store.candidate() }
    val request = remember(revision) { store.request() }
    val enabled = remember(revision) { readerEnabled() }
    // Drop a pending read when its two-minute window closes; otherwise the "waiting to read"
    // panel would stay on screen until the next unrelated recomposition.
    LaunchedEffect(request?.expiresAt) {
        val expiresAt = request?.expiresAt ?: return@LaunchedEffect
        val remaining = expiresAt - System.currentTimeMillis()
        if (remaining > 0) delay(remaining)
        val stillExpired = store.request()?.let { it.expiresAt <= System.currentTimeMillis() } == true
        if (stillExpired) {
            runCatching { store.cancelCapture() }
            refresh()
        }
    }
    fun notify(text: String) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = text,
                withDismissAction = true,
                duration = NoticeDuration,
            )
        }
    }
    fun mutate(block: () -> Unit): Boolean = runCatching { block(); refresh(); true }
        .getOrElse { notify(it.message ?: tr("common.operation_failed")); false }
    fun invalidateSortUndo() {
        sortUndoJob?.cancel()
        sortUndoJob = null
    }
    fun sortState(list: SortList) = when (list) {
        SortList.GOALS -> goalSort
        SortList.VAULT -> vaultSort
        SortList.ACCOUNTS -> accountSort
    }
    fun sortQueue(list: SortList) = when (list) {
        SortList.GOALS -> goalSortQueue
        SortList.VAULT -> vaultSortQueue
        SortList.ACCOUNTS -> accountSortQueue
    }
    fun storedSortIds(list: SortList) = when (list) {
        SortList.GOALS -> store.goals().filterNot { it.archived }.map { it.id }
        SortList.VAULT -> store.goals().filter { it.archived }.map { it.id }
        SortList.ACCOUNTS -> store.accounts().map { it.id }
    }
    fun persistSortOrder(list: SortList, state: SortPageState, ids: List<String>, settings: SortSettings, immediate: Boolean) {
        val queue = sortQueue(list)
        queue.revision++
        val writeRevision = queue.revision
        queue.submit {
            sortCommitLock.withLock {
                if (writeRevision != queue.revision) return@withLock
                try {
                    withContext(Dispatchers.IO) {
                        if (list == SortList.ACCOUNTS) store.reorderAccounts(ids, settings)
                        else store.reorderGoals(ids, settings, list)
                    }
                } catch (error: Exception) {
                    if (error is CancellationException) throw error
                    if (writeRevision == queue.revision) {
                        invalidateSortUndo()
                        state.drag.clear()
                        state.order.value = storedSortIds(list)
                        state.settings.value = store.sortSettings(list)
                        notify(error.message ?: tr("common.operation_failed"))
                        refresh()
                    }
                }
            }
        }
        if (immediate) queue.flush()
    }
    fun applySortOrder(state: SortPageState, list: SortList, ids: List<String>, settings: SortSettings) {
        state.drag.clear()
        state.order.value = ids
        state.settings.value = settings
        persistSortOrder(list, state, ids, settings, immediate = true)
    }
    fun saveManualSort(state: SortPageState, list: SortList, ids: List<String>) {
        invalidateSortUndo()
        val settings = SortSettings(manualOrder = ids)
        state.settings.value = settings
        persistSortOrder(list, state, ids, settings, immediate = false)
    }
    fun sortWithUndo(state: SortPageState, list: SortList, next: List<String>, nextSettings: SortSettings) {
        val previous = state.snapshot()
        if (previous.order == next && previous.settings == nextSettings) return
        val queue = sortQueue(list)
        queue.revision++
        queue.cancel()
        invalidateSortUndo()
        applySortOrder(state, list, next, nextSettings)
        sortUndoJob = scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = tr("sort.done"),
                actionLabel = tr("common.undo"),
                withDismissAction = true,
                duration = SnackbarDuration.Custom(5000L),
            )
            if (result == SnackbarResult.ActionPerformed && !state.drag.active &&
                state.snapshot() == SortSnapshot(next, nextSettings)) {
                val liveIds = storedSortIds(list)
                if (liveIds.toSet() == next.toSet()) {
                    applySortOrder(state, list, previous.order, previous.settings)
                }
            }
            sortUndoJob = null
        }
    }
    fun applySortCriterion(list: SortList, index: Int, descending: Boolean) {
        val state = sortState(list)
        val sortingGoals = list != SortList.ACCOUNTS
        val current = state.snapshot().order
        val sorted = if (sortingGoals) {
            val wishes = current.mapNotNull(goalsById::get)
            when (index) {
                0 -> wishes.sortedWith(goalNameOrder)
                1 -> wishes.sortedBy { it.createdAt }
                2 -> wishes.sortedBy { it.target }
                3 -> wishes.sortedByDescending { it.progress(accounts) }
                4 -> wishes.sortedBy { it.remaining(accounts) }
                6 -> wishes.sortedBy { it.archivedAt }
                else -> wishes.sortedWith(compareBy<SavingGoal> { it.deadline.isEmpty() }.thenBy { it.deadline })
            }.map { it.id }
        } else {
            val linkedAccounts = current.mapNotNull(accountsById::get)
            when (index) {
                0 -> linkedAccounts.sortedWith(accountNameOrder)
                1 -> linkedAccounts.sortedBy { it.createdAt }
                2 -> linkedAccounts.sortedBy { it.balance }
                else -> linkedAccounts.sortedByDescending { account -> goals.count { account.id in it.accountIds } }
            }.map { it.id }
        }
        val next = if (!descending) sorted else if (sortingGoals && index == 5) {
            val datedCount = sorted.count { goalsById.getValue(it).deadline.isNotEmpty() }
            sorted.take(datedCount).reversed() + sorted.drop(datedCount)
        } else sorted.reversed()
        val settings = SortSettings(index, descending,
            state.settings.value.manualOrder ?: current)
        sortWithUndo(state, list, next, settings)
    }
    fun toggleDescending(list: SortList) {
        val state = sortState(list)
        val criterion = state.settings.value.criterion
        // Direction only applies to a criterion; a hand-made order has no natural reverse.
        if (criterion >= 0) applySortCriterion(list, criterion, !state.settings.value.descending)
    }
    fun restoreManualOrder(list: SortList) {
        val state = sortState(list)
        val manual = state.settings.value.manualOrder ?: return
        val live = state.order.value.toSet()
        val restored = manual.filter { it in live }.distinct() + state.snapshot().order.filterNot { it in manual }
        sortWithUndo(state, list, restored, SortSettings(manualOrder = restored))
    }
    var backupBusy by remember { mutableStateOf(false) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            backupBusy = true
            try {
                val text = withContext(Dispatchers.IO) { store.exportBackup().toJson() }
                withContext(Dispatchers.IO) {
                    checkNotNull(context.contentResolver.openOutputStream(uri, "wt")) { tr("backup.error_write_selected") }
                        .bufferedWriter(Charsets.UTF_8).use { it.write(text) }
                }
                notify(tr("backup.exported"))
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                notify(tr("backup.export_failed", error.message ?: tr("backup.error_write")))
            } finally { backupBusy = false }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            backupBusy = true
            try {
                pendingBackupJson = withContext(Dispatchers.IO) {
                    val bytes = checkNotNull(context.contentResolver.openInputStream(uri)) { tr("backup.error_read_selected") }
                        .use { input ->
                            val output = java.io.ByteArrayOutputStream()
                            val buffer = ByteArray(8192)
                            while (true) {
                                val count = input.read(buffer)
                                if (count < 0) break
                                require(output.size() + count <= BackupData.MAX_BYTES) { tr("backup.error_over_1mb") }
                                output.write(buffer, 0, count)
                            }
                            output.toByteArray()
                        }
                    require(bytes.size <= BackupData.MAX_BYTES) { tr("backup.error_over_1mb") }
                    bytes.toString(Charsets.UTF_8).also { BackupData.fromJson(it) }
                }
                importVisible = true
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                notify(tr("backup.import_failed", error.message ?: tr("backup.error_read")))
            } finally { backupBusy = false }
        }
    }
    fun capture(account: BankAccount) {
        if (!readerEnabled()) { disclosure = true; return }
        // A single read from the account card replaces any goal-card queue.
        captureQueue = emptyList()
        captureTotal = 0
        if (mutate { store.startCapture(account.id) }) {
            if (!openXiaoxing()) {
                mutate { store.cancelCapture() }
                notify(tr("accessibility.error_xiaoxing_missing"))
            } else notify(tr("accessibility.open_account_page", account.name))
        }
    }
    /** Reads the next queued account; every account needs its own pass through the reader. */
    fun startNextCapture() {
        while (captureQueue.isNotEmpty()) {
            val account = store.accounts().firstOrNull { it.id == captureQueue.first() }
            if (account == null) { captureQueue = captureQueue.drop(1); continue }
            if (!readerEnabled()) { captureQueue = emptyList(); disclosure = true; return }
            if (!mutate { store.startCapture(account.id) }) { captureQueue = emptyList(); return }
            if (!openXiaoxing()) {
                mutate { store.cancelCapture() }
                captureQueue = emptyList()
                notify(tr("accessibility.error_xiaoxing_missing"))
            } else notify(tr("accessibility.open_account_page", account.name))
            return
        }
    }
    /** Confirms the shown candidate and moves on to the next queued account, if any. */
    fun acceptCapturedBalance() {
        if (!mutate { store.acceptCandidate() }) return
        val remaining = captureQueue.drop(1)
        captureQueue = remaining
        if (remaining.isNotEmpty()) {
            startNextCapture()
        } else {
            // The last confirmation ends the read, so no "waiting to read" panel lingers behind it.
            mutate { store.cancelCapture() }
            if (captureTotal > 1) notify(tr("accessibility.queue_done", captureTotal))
        }
    }
    fun cancelCaptureQueue() {
        captureQueue = emptyList()
        captureTotal = 0
    }
    fun webDavReadBlocked(): Boolean = webDavBusy || webDavChangesVisible || webDavPickerVisible ||
        webDavPlan != null || webDavPicker.isNotEmpty() || pendingWebDavPlan != null || pendingWebDavPicker != null

    fun beginWebDavRead(accountIds: List<String> = emptyList()) {
        if (!store.developerModeEnabled() || !store.webDavBalanceEnabled()) return
        if (webDavReadBlocked()) {
            notify(tr("webdav.finish_current_read"))
            return
        }
        webDavAccountIds = accountIds
        webDavUrl = store.webDavUrl()
        webDavAccount = store.webDavAccount()
        webDavPasswordEdited = false
        webDavPassword = ""
        webDavSheet = true
        // Decrypting touches the keystore, so keep it off the UI thread. The sheet opens with an
        // empty password and fills in as soon as the stored secret is available.
        scope.launch {
            val stored = withContext(Dispatchers.IO) {
                runCatching { WebDavSettings.decryptPassword(store.encryptedWebDavPassword()) }
            }
            stored.exceptionOrNull()?.let { notify(it.message ?: tr("common.operation_failed")) }
            if (!webDavPasswordEdited) webDavPassword = stored.getOrDefault("")
        }
    }
    fun presentWebDavPicker(options: List<WebDavPickerAccount>) {
        webDavPicker = options
        webDavPickerSelection = WebDavAccountSelection.defaultSelection(options)
        webDavPickerVisible = true
    }
    /** Applies a plan in one store mutation; returns false when the store rejected it. */
    fun applyWebDavPlan(plan: WebDavImportPlan): Boolean {
        val now = System.currentTimeMillis()
        val applied = mutate { store.applyWebDavPlan(plan, now) }
        if (applied) notify(when {
            plan.changes.isEmpty() && plan.additions.isEmpty() -> tr("webdav.no_changes")
            plan.additions.isEmpty() -> tr("webdav.success", plan.changes.size)
            else -> tr("webdav.success_with_new", plan.changes.size, plan.additions.size)
        })
        return applied
    }
    /** The picker is the last step of a panel read, so the chosen plan is applied right away. */
    fun confirmWebDavPicker() {
        if (!webDavPickerVisible) return
        val selected = webDavPicker.filter { it.note == null && it.id in webDavPickerSelection }
        if (selected.isEmpty()) return
        val plan = try {
            WebDavAccountSelection.plan(webDavPicker, webDavPickerSelection, store.accounts())
        } catch (error: IllegalArgumentException) {
            notify(error.message ?: tr("common.save_failed"))
            return
        } catch (error: SyncError) {
            notify(error.arg?.let { tr(error.key, it) } ?: tr(error.key))
            return
        }
        if (!applyWebDavPlan(plan)) return
        webDavPickerVisible = false
    }
    fun readWebDav(
        accountIds: List<String> = emptyList(),
        chooseAccounts: Boolean = true,
        vaultRequest: Long? = null,
    ) {
        if (!store.developerModeEnabled() || !store.webDavBalanceEnabled()) return
        if (webDavReadBlocked()) {
            if (vaultRequest == null) notify(tr("webdav.finish_current_read"))
            return
        }
        val saveCredentials = webDavSheet
        val requested = if (saveCredentials) webDavAccountIds else accountIds
        // Only a real credential edit should invalidate the stored "connected" state.
        val credentialsEdited = saveCredentials && (webDavPasswordEdited ||
            webDavUrl.trim() != store.webDavUrl() || webDavAccount.trim() != store.webDavAccount())
        webDavBusy = true
        if (!saveCredentials && vaultRequest == null) notify(tr("webdav.reading_toast"))
        scope.launch {
            val temp = File(context.cacheDir, "webdav-${UUID.randomUUID()}").apply { mkdirs() }
            var client: WebDavClient? = null
            try {
                val url = if (saveCredentials) webDavUrl else store.webDavUrl()
                val username = if (saveCredentials) webDavAccount else store.webDavAccount()
                val password = if (saveCredentials) webDavPassword
                    else withContext(Dispatchers.IO) { WebDavSettings.decryptPassword(store.encryptedWebDavPassword()) }
                if (username.isBlank() || password.isBlank()) throw SyncError("webdav.error_credentials")
                val webdav = WebDavClient(url, username, password)
                client = webdav
                val backup = webdav.latestBackup()
                val archive = File(temp, "backup.lch")
                val database = File(temp, "snapshot.mdb")
                withContext(Dispatchers.IO) { webdav.download(backup, archive) }
                withContext(Dispatchers.IO) { XiaoxingBackupDecryptor.decrypt(archive, database) }
                val balances = SnapshotBalanceReader.read(database)
                if (saveCredentials) withContext(Dispatchers.IO) {
                    store.saveWebDavSettings(url, username, WebDavSettings.encryptPassword(password), credentialsEdited)
                }
                if (requested.isEmpty() && chooseAccounts) {
                    // Connecting from the panel lets the user choose which of the backup's
                    // accounts to take before computing the changes.
                    val options = WebDavAccountSelection.options(balances, store.accounts())
                    if (options.none { it.note == null }) throw SyncError("webdav.error_empty_accounts")
                    val fromCredentialsSheet = webDavSheet
                    webDavSheet = false
                    webDavAccountIds = emptyList()
                    if (fromCredentialsSheet) {
                        // Wait for the credential sheet's hide animation, then reveal the picker.
                        pendingWebDavPicker = options
                    } else presentWebDavPicker(options)
                    return@launch
                }
                val localAccounts = store.accounts()
                val localNameCounts = localAccounts.groupingBy { it.name }.eachCount()
                val current = if (requested.isEmpty()) localAccounts else localAccounts.filter { it.id in requested }
                if (current.isEmpty()) throw SyncError("webdav.error_empty_accounts")
                var matched = 0
                val changes = current.mapNotNull { account ->
                    val matches = balances.filter { it.name == account.name }
                    // A full re-read keeps accounts the backup does not contain; a read narrowed
                    // to a single account reports the missing match instead. A goal reads several
                    // accounts at once, so an account missing from the backup is simply skipped.
                    if (matches.isEmpty()) {
                        if (requested.size == 1) throw SyncError("webdav.error_empty_accounts")
                        return@mapNotNull null
                    }
                    if (matches.size != 1 || localNameCounts[account.name] != 1) {
                        throw SyncError("webdav.error_match", account.name)
                    }
                    val match = matches.single()
                    if (match.currency != 0) throw SyncError("webdav.error_currency", account.name)
                    matched++
                    if (match.cents == account.balance) null
                    else WebDavBalanceChange(account.id, account.name, account.balance, match.cents)
                }
                if (matched == 0) throw SyncError("webdav.error_empty_accounts")
                // Balance writes and the first successful sync require explicit confirmation.
                refresh()
                val fromCredentialsSheet = webDavSheet
                webDavSheet = false
                webDavAccountIds = emptyList()
                if (fromCredentialsSheet) {
                    // Wait for the credential sheet's hide animation, then reveal the confirmation.
                    pendingWebDavPlan = WebDavImportPlan(changes, emptyList())
                } else {
                    webDavPlan = WebDavImportPlan(changes, emptyList())
                    vaultRefreshInConfirmation = vaultRequest
                    webDavChangesVisible = true
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                if (vaultRequest != null) vaultRefreshHandled = vaultRequest
                Log.w("StarwishWebDav", "WebDAV balance import failed", error)
                if (error is SyncError && error.key == "webdav.error_auth") {
                    // Rejected credentials must not keep showing as a working connection.
                    runCatching { store.markWebDavDisconnected() }
                    refresh()
                }
                val message = if (error is SyncError) error.arg?.let { tr(error.key, it) } ?: tr(error.key)
                    else tr("webdav.error_read")
                notify(message)
            } finally {
                client?.close()
                temp.deleteRecursively()
                webDavBusy = false
            }
        }
    }

    // Several archives before the next read can share one full refresh.
    val vaultRefreshBlocked = archivingGoal != null || webDavSheet || webDavReadBlocked()
    LaunchedEffect(vaultRefreshRequest, vaultRefreshHandled, vaultRefreshBlocked, webDavActive) {
        if (vaultRefreshRequest == vaultRefreshHandled) return@LaunchedEffect
        if (!webDavActive) {
            vaultRefreshHandled = vaultRefreshRequest
        } else if (!vaultRefreshBlocked) {
            if (store.accounts().isEmpty()) vaultRefreshHandled = vaultRefreshRequest
            else readWebDav(chooseAccounts = false, vaultRequest = vaultRefreshRequest)
        }
    }
    /**
     * Refreshes the balances of the accounts linked to [goal] from the same source the account
     * page uses, without leaving the page: the WebDAV backup when it is on, the reader otherwise.
     */
    fun updateGoalBalances(goal: SavingGoal) {
        val ids = store.accounts().filter { it.id in goal.accountIds }.map { it.id }
        // Never let a leftover accessibility queue from an earlier read bleed into this one.
        captureQueue = emptyList()
        captureTotal = 0
        if (ids.isEmpty()) {
            notify(tr("goal.no_linked_accounts"))
            return
        }
        if (webDavActive) {
            if (webDavConnected) {
                readWebDav(ids)
            } else beginWebDavRead(ids)
        } else {
            captureTotal = ids.size
            captureQueue = ids
            startNextCapture()
        }
    }
    fun closeWebDavChanges() {
        vaultRefreshInConfirmation?.let { vaultRefreshHandled = it }
        vaultRefreshInConfirmation = null
        webDavChangesVisible = false
    }
    fun confirmWebDavChanges() {
        if (!webDavChangesVisible) return
        val plan = webDavPlan ?: return
        if (applyWebDavPlan(plan)) closeWebDavChanges()
    }

    fun webDavPickerRemainingSlots(): Int = WebDavAccountSelection.newAccountSlots(accounts.size) -
        webDavPicker.count { it.creates && it.id in webDavPickerSelection }
    val accent = MiuixTheme.colorScheme.primary
    // Grouped cards use surfaceContainer; surface keeps their boundary visible in
    // the stock palette too (background and surfaceContainer are both white).
    val pageBackground = if (dark && appearance.pureBlackBackground) Color.Black else MiuixTheme.colorScheme.surface
    // Pager destinations share a single root route. Register their back handler above NavDisplay
    // so the entry's own navigation-event bridge cannot take priority over it.
    val pagerBackState = rememberNavigationEventState(NavigationEventInfo.None)
    val pagerBackEnabled = backStack.size == 1 && selectedTab != 0 &&
        !goalEditor && !accountEditor && !disclosure &&
        !deletingGoalVisible && !deletingAccountVisible && !importVisible && !bannerDecimalsSheet &&
        !archivingVisible && !removingVisible && !restoringMissingVisible && !accountDeleteStep2
    if (appearance.predictiveBackEnabled) {
        NavigationBackHandler(
            state = pagerBackState,
            isBackEnabled = pagerBackEnabled,
            onBackCompleted = { navigate(0) },
        )
    } else {
        // Keep normal back navigation while opting out of the predictive transition.
        BackHandler(enabled = pagerBackEnabled) { navigate(0) }
    }
    val pagerBackInProgress = pagerBackState.transitionState as? NavigationEventTransitionState.InProgress
    val backProgress = if (appearance.predictiveBackEnabled && pagerBackEnabled) {
        pagerBackInProgress?.latestEvent?.progress ?: 0f
    } else 0f

    // NavDisplay has its own predictive handler for pushed routes, and the activity can animate
    // to home as well. When the preference is off, take over back with a plain high-priority
    // callback so no predictive gesture starts anywhere in the app. Dialogs keep their own
    // handlers; the pager root uses BackHandler below on older APIs.
    val blockPredictive = !appearance.predictiveBackEnabled &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        !goalEditor && !accountEditor && !disclosure &&
        !deletingGoalVisible && !deletingAccountVisible && !importVisible && !bannerDecimalsSheet &&
        !accountDeleteStep2
    if (blockPredictive) {
        val activity = context as? ComponentActivity
        DisposableEffect(activity, backStack.size, selectedTab, goalEditor, accountEditor, disclosure,
            deletingGoalVisible, deletingAccountVisible, importVisible, archivingVisible, removingVisible,
            restoringMissingVisible, accountDeleteStep2) {
            val callback = OnBackInvokedCallback {
                when {
                    backStack.size > 1 -> popRoute()
                    selectedTab != 0 -> navigate(0)
                    else -> activity?.finish()
                }
            }
            activity?.onBackInvokedDispatcher?.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_OVERLAY, callback,
            )
            onDispose {
                activity?.onBackInvokedDispatcher?.unregisterOnBackInvokedCallback(callback)
            }
        }
    }
    // Miuix owns the cross-cutting effect layer: the device screen corner clips the sliding page on
    // a phone, while a wide-shell pane is not a screen edge and keeps square corners. The backdrop
    // fills whatever the scaled-down layers reveal so it reads as the page background.
    val navCornerRadius = rememberNavSystemCornerRadius()
    val navEffects = remember(wideLayout, navCornerRadius, pageBackground) {
        NavDisplayEffects(
            cornerClipRadius = if (wideLayout) 0.dp else navCornerRadius,
            backdropColor = pageBackground,
        )
    }
    // Secondary pages keep the rail visible and map back to the tab that owns them, so the
    // window-size change does not introduce a second navigation state. Tab destinations all
    // live in the pager under AppRoute(0), so only a pushed secondary route overrides the tab.
    val topRoute = (backStack.lastOrNull() as? AppRoute)?.page ?: 0
    val railPage = if (backStack.size > 1 && topRoute >= 3) when (topRoute) {
        6 -> 0
        7 -> 1
        9 -> 0
        10 -> 0
        else -> 2
    } else selectedTab
    Row(Modifier.fillMaxSize()) {
    if (wideLayout && !useFloatingNav) {
        NavigationRail(
            state = railState,
            color = pageBackground,
            showDivider = true,
            expandContentDescription = tr("nav.expand"),
            collapseContentDescription = tr("nav.collapse"),
        ) {
            listOf(tr("nav.home") to NavIcons[0], tr("account.title") to NavIcons[1], tr("settings.title") to NavIcons[2]).forEachIndexed { index, item ->
                NavigationRailItem(
                    selected = railPage == index,
                    onClick = { navigate(index) },
                    icon = item.second,
                    label = item.first,
                )
            }
        }
    }
    BoxWithConstraints(
        // Route transitions translate/scale the outgoing entry. In the wide shell the rail is a
        // sibling before NavDisplay, so an unclipped outgoing entry would paint over the rail and
        // leave a ghost there until the next full redraw.
        modifier = Modifier.weight(1f).fillMaxHeight()
            // Nothing else paints the pane, so give it the page colour; the top bar and the
            // scrolling content apply their own display-cutout insets.
            .background(pageBackground)
            .clipToBounds(),
    ) {
    // The pane width is measured (rather than derived from the rail constants) so the title can
    // line up with the centred content column even when system insets widen the rail.
    val paneWidth = maxWidth
    // Export the complete pane once, then draw the glass bar as an overlay sibling. Keeping the
    // bar outside the exported layer is what lets it sample the page underneath without recursion.
    Box(
        Modifier.fillMaxSize(),
    ) {
    NavDisplay(
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        onBack = { popRoute() },
        transition = NavTransitions.MiuixDefault,
        effects = navEffects,
    ) {
      // NavDisplay owns both system predictive back and in-content swipe dismissal, including
      // cancellation and settling. Physical swipe directions must be selected explicitly for RTL.
      entry<AppRoute>(
          swipeDismiss = when {
              !appearance.predictiveBackEnabled -> NavSwipeDirection.None
              layoutDirection == androidx.compose.ui.unit.LayoutDirection.Rtl -> NavSwipeDirection.RightToLeft
              else -> NavSwipeDirection.LeftToRight
          },
      ) { route ->
        val page = if (route.page >= 3) route.page else selectedTab
        // Wide layouts keep the content in a centred 880dp column. TopAppBar already applies the
        // display-cutout inset to itself, so the title gutter only needs the centring offset plus
        // whatever the column adds on top of that cutout. Subtracting the cutout keeps the large
        // title on the same edge as the cards instead of 30dp to their right.
        val cutoutInsets = WindowInsets.displayCutout.asPaddingValues()
        val cutoutStart = cutoutInsets.calculateStartPadding(layoutDirection)
        val cutoutEnd = cutoutInsets.calculateEndPadding(layoutDirection)
        val columnStart = ((paneWidth - 880.dp) * 0.5f).coerceAtLeast(0.dp) + maxOf(32.dp, cutoutStart)
        val barTitlePadding = if (wideLayout) (columnStart - cutoutStart).coerceAtLeast(0.dp)
        else TopAppBarDefaults.TitlePadding
        val blurActive = appearance.blurEnabled && android.os.Build.VERSION.SDK_INT >= 33 && isRuntimeShaderSupported()
        // A LayerBackdrop has one recording owner. Pager neighbours must not overwrite it.
        val backdrops = List(if (route.page < 3) 3 else 1) {
            if (blurActive) rememberLayerBackdrop {
                drawRect(pageBackground)
                drawContent()
            } else null
        }
        // The bottom bar samples the pager's stable parent, not an individual page.
        // A page's layer can be detached during tab animation or a rapid second tap.
        val bottomBackdrop = if (blurActive && (route.page < 3 || (wideLayout && useFloatingNav))) rememberLayerBackdrop {
            drawRect(pageBackground)
            drawContent()
        } else null
        val barColor = if (blurActive) Color.Transparent else pageBackground
        // The stock palette's primary is Miuix blue. In the default theme the selected tab stays
        // neutral instead, so the bottom bar matches the rest of the chrome (this reads as black in
        // the light theme and white in the dark one). Other accents keep colouring the selection.
        val navSelectedColor = if (appearance.accentIndex == 0) MiuixTheme.colorScheme.onBackground else accent
        Scaffold(
          bottomBar = {
            // Phones show the bar on the three tab pages only. A wide window normally uses the
            // rail, but the floating bar replaces it and therefore stays visible on secondary
            // pages as well, mapping the selection back to the owning tab like the rail does.
            val showBottomBar = if (wideLayout) useFloatingNav else page < 3
            if (showBottomBar) {
                val navItems = listOf(tr("nav.home") to NavIcons[0], tr("account.title") to NavIcons[1],
                    tr("settings.title") to NavIcons[2])
                val navIndex = if (wideLayout) railPage else page
                when {
                    !useFloatingNav -> Box(Modifier.fillMaxWidth()
                        .then(if (bottomBackdrop != null) Modifier.textureBlur(bottomBackdrop, RectangleShape, BlurDefaults.BlurRadius,
                            colors = BlurDefaults.blurColors(blendColors = listOf(BlendColorEntry(pageBackground.copy(alpha = .8f))))) else Modifier)
                        .background(barColor)) {
                        NavigationBar(color = barColor) {
                            navItems.forEachIndexed { index, item ->
                                // NavigationBarItem already applies weight(1f) inside its RowScope.
                                NavigationBarItem(selected = navIndex == index, onClick = { navigate(index) },
                                    icon = item.second, label = item.first,
                                    colors = NavigationBarDefaults.navigationBarItemColors(selectedContentColor = navSelectedColor))
                            }
                        }
                    }
                    appearance.floatingNavBarStyle == 0 -> FloatingNavigationBar {
                        navItems.forEachIndexed { index, item ->
                            FloatingNavigationBarItem(selected = navIndex == index, onClick = { navigate(index) },
                                icon = item.second, label = item.first,
                                colors = NavigationBarDefaults.navigationBarItemColors(selectedContentColor = navSelectedColor))
                        }
                    }
                    // The iOS-like bar lives in the entry's bottomBar, just like the Miuix
                    // floating bar, so it participates in predictive-back scene transitions.
                    else -> IosLiquidGlassNavigationBar(
                        items = navItems,
                        selectedIndex = navIndex,
                        onItemClick = { navigate(it) },
                        backdrop = bottomBackdrop,
                        isBlurActive = blurActive,
                        isDark = dark,
                        barBlurRadiusPx = iosBarBlurRadius.toFloat(),
                    )
                }
            }
          },
          snackbarHost = { SnackbarHost(snackbarHostState) },
          containerColor = pageBackground,
          // Each page's TopAppBar owns the status-bar inset. Do not offset the pager.
          contentWindowInsets = WindowInsets(0, 0, 0, 0),
        ) { padding ->
            val pageContent: @Composable (Int) -> Unit = { displayedPage ->
        val backdrop = backdrops[if (route.page < 3) displayedPage else 0]
        val contentScrollBehavior = MiuixScrollBehavior()
        val pageScrollState = key(displayedPage) { rememberScrollState() }
        var viewportTop by remember { mutableFloatStateOf(0f) }
        var viewportHeight by remember { mutableFloatStateOf(0f) }
        Scaffold(topBar = {
            Box(Modifier.fillMaxWidth()
                .then(if (backdrop != null) Modifier.textureBlur(backdrop, RectangleShape, BlurDefaults.BlurRadius,
                    colors = BlurDefaults.blurColors(blendColors = listOf(BlendColorEntry(pageBackground.copy(alpha = .8f))))) else Modifier)
                .background(if (backdrop != null) Color.Transparent else pageBackground)) {
                TopAppBar(
                    title = when (displayedPage) { 0 -> tr("app.name"); 1 -> tr("account.title"); 2 -> tr("settings.title"); 4 -> tr("theme.title"); 5 -> tr("settings.import_export"); 6 -> tr("sort.goals_title"); 7 -> tr("sort.accounts_title"); 8 -> tr("settings.developer_options"); 9 -> tr("vault.title"); 10 -> tr("vault.sort_title"); else -> tr("about.title") },
                    subtitle = "",
                    color = barColor,
                    scrollBehavior = contentScrollBehavior,
                    titlePadding = barTitlePadding,
                    bottomContent = {
                        if (displayedPage == 1 || displayedPage == 2 || displayedPage == 9) {
                            // Unlike TopAppBar.subtitle, this caption does not reappear below the small title.
                            // On wide windows the bar spans the whole pane, so centre the caption on the
                            // same 880dp column as the cards to keep its left edge aligned with them.
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                                Box(Modifier.then(if (wideLayout) Modifier.widthIn(max = 880.dp) else Modifier)
                                    .fillMaxWidth()) {
                                    Box(Modifier.layout { measurable, constraints ->
                                        val placeable = measurable.measure(constraints)
                                        val visibleHeight = (placeable.height *
                                            (1f - contentScrollBehavior.state.collapsedFraction).coerceIn(0f, 1f)).toInt()
                                        layout(placeable.width, visibleHeight) { placeable.placeRelative(0, 0) }
                                    }.clipToBounds()) {
                                        val side = if (wideLayout) barTitlePadding else TopAppBarDefaults.TitlePadding
                                        val caption = when {
                                            displayedPage == 9 -> if (goals.none { it.archived }) tr("vault.empty") else null
                                            displayedPage == 2 && quoteShown ->
                                                if (quoteLoading) tr("settings.quote_loading") else hitokoto ?: tr("settings.quote_loading")
                                            else -> null
                                        }
                                        if (caption != null) Text(caption,
                                            modifier = Modifier.fillMaxWidth()
                                                .then(if (displayedPage == 2) Modifier.clickable(enabled = !quoteLoading) {
                                                    scope.launch { loadQuote() }
                                                } else Modifier)
                                                .padding(start = side, end = side, bottom = 8.dp),
                                            style = MiuixTheme.textStyles.body2,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                                    }
                                }
                            }
                        }
                    },
                actions = {
                    if (displayedPage == 6 || displayedPage == 7 || displayedPage == 10) {
                        val list = when (displayedPage) {
                            6 -> SortList.GOALS
                            10 -> SortList.VAULT
                            else -> SortList.ACCOUNTS
                        }
                        val state = sortState(list)
                        val (criterion, descending, manualOrder) =
                            state.settings.value
                        val criteria = when {
                            // Vault cards are always complete, and 剩余金额 says nothing there.
                            displayedPage == 10 -> listOf(
                                tr("sort.name") to 0, tr("sort.time") to 1, tr("sort.price") to 2,
                                tr("sort.deadline") to 5, tr("sort.achieved") to 6,
                            )
                            list == SortList.GOALS -> listOf(
                                tr("sort.name") to 0, tr("sort.time") to 1, tr("sort.price") to 2,
                                tr("sort.progress") to 3, tr("sort.remaining") to 4, tr("sort.deadline") to 5,
                            )
                            else -> listOf(
                                tr("sort.name") to 0, tr("sort.time") to 1,
                                tr("sort.balance") to 2, tr("sort.links") to 3,
                            )
                        }
                        // The menu fits seven rows, so direction is shown on the active criterion
                        // (tap it again to flip) instead of costing an eighth row.
                        val sortEntry = DropdownEntry(criteria.map { (label, index) ->
                            DropdownItem(
                                if (criterion == index) "$label ${if (descending) "↓" else "↑"}" else label,
                                selected = criterion == index,
                                onClick = {
                                    val latest = state.settings.value
                                    if (latest.criterion == index) toggleDescending(list)
                                    else applySortCriterion(list, index, latest.descending)
                                })
                        })
                        val orderEntry = DropdownEntry(listOfNotNull(
                            manualOrder?.takeIf { criterion >= 0 }?.let {
                                DropdownItem(tr("sort.custom"), onClick = { restoreManualOrder(list) })
                            },
                        ))
                        OverlayIconDropdownMenu(entries = listOfNotNull(sortEntry, orderEntry.takeIf { it.items.isNotEmpty() }),
                            collapseOnSelection = true) {
                            Icon(MiuixIcons.More, contentDescription = tr("sort.menu"), modifier = Modifier.size(24.dp))
                        }
                    }
                    if (displayedPage == 9) {
                        OverlayIconDropdownMenu(entry = DropdownEntry(listOf(
                            DropdownItem(tr("sort.title"), onClick = { navigate(10) }),
                        ))) { Icon(MiuixIcons.More, contentDescription = tr("vault.menu"), modifier = Modifier.size(24.dp)) }
                    }
                },
                navigationIcon = {
                    if (displayedPage >= 3) IconButton(onClick = { popRoute() }) {
                        Icon(MiuixIcons.Back, contentDescription = if (displayedPage == 6 || displayedPage == 9) tr("common.back_to_goals")
        else if (displayedPage == 10) tr("vault.back")
                            else if (displayedPage == 7) tr("common.back_to_accounts") else tr("common.back_to_settings"))
                    }
                })
            }
        }, containerColor = pageBackground) { pagePadding ->
        Box(Modifier.fillMaxSize().onGloballyPositioned {
            viewportTop = it.positionInWindow().y
            viewportHeight = it.size.height.toFloat()
        }
            .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier)) {
        // The wide layout pins content to an 880dp column, so it cannot use the page scaffold's
        // insets, yet a landscape camera cutout still occupies one edge. Take the largest of the
        // base margin, the scaffold inset and the cutout: the cutout is then applied exactly once
        // and the content never slides under the camera.
        val baseSide = if (wideLayout) 32.dp else 16.dp
        val sideStart = maxOf(baseSide, pagePadding.calculateStartPadding(layoutDirection), cutoutStart)
        val sideEnd = maxOf(baseSide, pagePadding.calculateEndPadding(layoutDirection), cutoutEnd)
        // A balance read can start from a goal card, so its status and confirmation are shared by
        // the home page and the accounts page instead of living only where the read used to start.
        val captureStatus: @Composable () -> Unit = {
            val shown = candidate
            if (shown != null) {
                val account = accounts.firstOrNull { it.id == shown.accountId }
                Panel {
                    StatusPill(tr("accessibility.waiting_confirm"))
                    Text(account?.name ?: tr("account.error_already_deleted"), style = MiuixTheme.textStyles.title3, fontWeight = FontWeight.Bold)
                    Text("¥ ${Money.format(shown.balance)}", style = MiuixTheme.textStyles.title2, fontWeight = FontWeight.Bold)
                    if (captureTotal > 1) Note(tr("accessibility.queue_progress",
                        (captureTotal - captureQueue.size + 1).coerceAtLeast(1), captureTotal))
                    Note(tr("accessibility.recognized_at", timeLabel(shown.capturedAt)))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(text = tr("common.ignore"), onClick = { cancelCaptureQueue(); mutate { store.clearCandidate() } },
                            modifier = Modifier.weight(1f))
                        TextButton(text = tr("accessibility.confirm_sync"), onClick = { acceptCapturedBalance() },
                            enabled = account != null, modifier = Modifier.weight(1f),
                            colors = primaryTextButtonColors())
                    }
                }
            }
            if (!webDavActive && request != null && request.expiresAt > System.currentTimeMillis()) {
                Panel {
                    Text(tr("accessibility.waiting_page"), style = MiuixTheme.textStyles.title3, fontWeight = FontWeight.Bold)
                    if (captureTotal > 1) Note(tr("accessibility.queue_progress",
                        (captureTotal - captureQueue.size + 1).coerceAtLeast(1), captureTotal))
                    Note(tr("accessibility.read_steps_detail"))
                    TextButton(text = tr("accessibility.cancel_read"),
                        onClick = { cancelCaptureQueue(); mutate { store.cancelCapture() } })
                }
            }
        }
        Column(Modifier.fillMaxHeight()
            .then(if (wideLayout) Modifier.widthIn(max = 880.dp) else Modifier)
            .fillMaxWidth()
            .align(Alignment.TopCenter)
            .background(pageBackground)
            .overScrollVertical()
            .nestedScroll(contentScrollBehavior.nestedScrollConnection)
            .verticalScroll(pageScrollState)
            .padding(start = sideStart, end = sideEnd,
                top = pagePadding.calculateTopPadding(),
                bottom = maxOf(padding.calculateBottomPadding(), pagePadding.calculateBottomPadding())),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Spacer(Modifier.height(2.dp))
            when (displayedPage) {
                0 -> {
                    // 珍藏馆 keeps the finished wishes out of the active list (and the banner).
                    val activeGoals = goals.filter { !it.archived }
                    Overview(activeGoals, accounts, appearance, accent, deviceAccountName,
                        onOpenVault = { navigate(9) })
                    captureStatus()
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        SmallTitle(tr("home.goal_list"), insideMargin = SectionTitlePadding)
                        OverlayIconDropdownMenu(entry = DropdownEntry(listOf(
                            DropdownItem(tr("goal.new_goal"), onClick = { editingGoalId = null; goalEditor = true }),
                            DropdownItem(tr("sort.title"), onClick = { navigate(6) }),
                        ))) { Icon(MiuixIcons.More, contentDescription = tr("goal.menu"), modifier = Modifier.size(24.dp)) }
                    }
                    if (activeGoals.isEmpty()) {
                        EmptyCard("star", tr("home.first_wish_here"), tr("goal.create_intro"), tr("goal.create_my_goal")) {
                            editingGoalId = null; goalEditor = true
                        }
                    }
                    val goalCard: @Composable (SavingGoal) -> Unit = { goal ->
                        GoalCard(goal, accounts,
                            onEdit = { editingGoalId = goal.id; goalEditor = true },
                            onDelete = { deletingGoal = goal; deletingGoalVisible = true },
                            onSync = { updateGoalBalances(goal) },
                            onArchive = { archivingGoal = goal; archivingVisible = true },
                            collapseActions = appearance.collapseCardActions,
                            decimals = appearance.bannerDecimals)
                    }
                    if (goalGrid) {
                        // Wider windows use two card columns; the sort page remains a single
                        // column because its drag maths is slot based.
                        activeGoals.chunked(2).forEach { pair ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                pair.forEach { goal -> Box(Modifier.weight(1f)) { goalCard(goal) } }
                                if (pair.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    } else {
                        activeGoals.forEach { goalCard(it) }
                    }
                }
                9 -> {
                    val vaultGoals = goals.filter { it.archived }
                    // While the vault is empty the caption sits under the large title (it is
                    // drawn by the top bar); the body then stays blank.
                    if (vaultGoals.isNotEmpty()) {
                        val vaultCard: @Composable (SavingGoal) -> Unit = { goal ->
                            GoalCard(goal, accounts,
                                onEdit = { removingGoal = goal; removingVisible = true },
                                onDelete = { deletingGoal = goal; deletingGoalVisible = true },
                                onSync = {},
                                onArchive = {},
                                collapseActions = appearance.collapseCardActions,
                                decimals = appearance.bannerDecimals)
                        }
                        if (goalGrid) {
                            vaultGoals.chunked(2).forEach { pair ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    pair.forEach { goal -> Box(Modifier.weight(1f)) { vaultCard(goal) } }
                                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        } else {
                            vaultGoals.forEach { goal -> vaultCard(goal) }
                        }
                    }
                }
                10 -> {
                    DisposableEffect(vaultSortQueue, vaultSort) {
                        onDispose {
                            vaultSort.finishDrag()
                            vaultSortQueue.flush()
                        }
                    }
                    vaultSort.order.value.forEach { id -> key(id) {
                        val goal = goalsById[id]
                        if (goal != null) {
                            SortCard(goal.title, Money.format(goal.target), goal.id, vaultSort.order,
                                pageScrollState, viewportTop, viewportHeight, vaultSort.drag,
                                onPersist = { ids -> saveManualSort(vaultSort, SortList.VAULT, ids) })
                        }
                    } }
                }
                1 -> {
                    captureStatus()
                    Panel {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            IconTile("link")
                            Column(Modifier.weight(1f)) {
                                Text(tr("app.xiaoxing"), style = MiuixTheme.textStyles.title4.copy(fontSize = 19.sp), fontWeight = FontWeight.Bold)
                                Text(when {
                                    webDavActive -> tr("account.webdav_read_summary")
                                    enabled -> tr("accessibility.read_enabled")
                                    else -> tr("accessibility.enable_read_summary")
                                }, style = MiuixTheme.textStyles.footnote2,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                            }
                            StatusPill(when {
                                webDavActive -> if (webDavConnected) tr("account.webdav_connected")
                                    else tr("account.webdav_not_connected")
                                enabled -> tr("accessibility.granted")
                                else -> tr("accessibility.not_granted")
                            })
                        }
                        if (!webDavActive) {
                            Note(tr("accessibility.no_public_api_note"))
                            Button(onClick = {
                                if (enabled) {
                                    openReaderSettings()
                                    notify(tr("accessibility.disable_in_settings"))
                                } else {
                                    disclosure = true
                                }
                            }, modifier = Modifier.fillMaxWidth(),
                                colors = if (enabled) primaryButtonColors()
                                else ButtonDefaults.buttonColors()) {
                                Text(if (enabled) tr("accessibility.disable_service") else tr("accessibility.set_up_permission"))
                            }
                        } else {
                            Button(onClick = { beginWebDavRead() }, modifier = Modifier.fillMaxWidth(),
                                colors = primaryButtonColors(), enabled = !webDavBusy) {
                                Text(tr("webdav.button"))
                            }
                            if (webDavConnected) {
                                Button(onClick = { readWebDav(chooseAccounts = false) }, modifier = Modifier.fillMaxWidth(), enabled = !webDavBusy) {
                                    Text(tr("webdav.reread"))
                                }
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        SmallTitle(tr("account.my_accounts"), insideMargin = SectionTitlePadding)
                        OverlayIconDropdownMenu(entry = DropdownEntry(listOf(
                            DropdownItem(tr("account.add"), onClick = { editingAccountId = null; accountEditor = true }),
                            DropdownItem(tr("sort.title"), onClick = { navigate(7) }),
                        ))) { Icon(MiuixIcons.More, contentDescription = tr("account.menu"), modifier = Modifier.size(24.dp)) }
                    }
                    if (accounts.isEmpty()) EmptyCard("bank", tr("goal.connect_first"), tr("account.empty_intro"), tr("account.add_first_account")) {
                        editingAccountId = null; accountEditor = true
                    }
                    val accountCard: @Composable (BankAccount) -> Unit = { account ->
                        // 读取小星余额 / 编辑 / 删除 live here in both layouts; the corner menu only
                        // decides whether they are rendered inline or folded away.
                        val readBalance = {
                            if (webDavActive) {
                                if (webDavConnected) {
                                    readWebDav(listOf(account.id))
                                } else beginWebDavRead(listOf(account.id))
                            } else capture(account)
                        }
                        Panel {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                IconTile("bank")
                                Column(Modifier.weight(1f)) {
                                    Text(account.name, style = MiuixTheme.textStyles.title4.copy(fontSize = 19.sp), fontWeight = FontWeight.Bold, maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                    Text(tr(account.source), style = MiuixTheme.textStyles.footnote2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                                }
                                if (appearance.collapseCardActions) {
                                    OverlayIconDropdownMenu(
                                        entry = DropdownEntry(listOf(
                                            DropdownItem(tr("accessibility.read_balance"), onClick = readBalance),
                                            DropdownItem(tr("common.edit"), onClick = { editingAccountId = account.id; accountEditor = true }),
                                            DropdownItem(tr("common.delete"), onClick = { deletingAccount = account; deletingAccountVisible = true }),
                                        )),
                                    ) { Icon(MiuixIcons.More, contentDescription = tr("account.menu"), modifier = Modifier.size(20.dp)) }
                                }
                            }
                            Text("¥ ${Money.format(account.balance)}", style = MiuixTheme.textStyles.title2, fontWeight = FontWeight.Bold)
                            Text(tr("account.updated_at", timeLabel(account.updatedAt)), style = MiuixTheme.textStyles.footnote1,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                            if (!appearance.collapseCardActions) {
                                // The read action names the source page, so it is longer than the other
                                // two in every language: give it a full row and share the second row.
                                TextButton(text = tr("accessibility.read_balance"), onClick = readBalance,
                                    modifier = Modifier.fillMaxWidth(), colors = primaryTextButtonColors())
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    TextButton(text = tr("common.edit"), onClick = { editingAccountId = account.id; accountEditor = true },
                                        modifier = Modifier.weight(1f))
                                    TextButton(text = tr("common.delete"), onClick = { deletingAccount = account; deletingAccountVisible = true },
                                        modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    if (accountGrid) {
                        accounts.chunked(2).forEach { pair ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                pair.forEach { account -> Box(Modifier.weight(1f)) { accountCard(account) } }
                                if (pair.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    } else {
                        accounts.forEach { accountCard(it) }
                    }
                }
                6 -> {
                    DisposableEffect(goalSortQueue, goalSort) {
                        onDispose {
                            goalSort.finishDrag()
                            goalSortQueue.flush()
                        }
                    }
                    goalSort.order.value.forEach { id -> key(id) {
                        val goal = goalsById[id]
                        if (goal != null) {
                            SortCard(goal.title, Money.format(goal.target), goal.id, goalSort.order,
                                pageScrollState, viewportTop, viewportHeight, goalSort.drag,
                                onPersist = { ids -> saveManualSort(goalSort, SortList.GOALS, ids) })
                        }
                    } }
                }
                7 -> {
                    DisposableEffect(accountSortQueue, accountSort) {
                        onDispose {
                            accountSort.finishDrag()
                            accountSortQueue.flush()
                        }
                    }
                    accountSort.order.value.forEach { id -> key(id) {
                        val account = accountsById[id]
                        if (account != null) {
                            SortCard(account.name, Money.format(account.balance), account.id, accountSort.order,
                                pageScrollState, viewportTop, viewportHeight, accountSort.drag,
                                onPersist = { ids -> saveManualSort(accountSort, SortList.ACCOUNTS, ids) })
                        }
                    } }
                }
                2 -> {
                    SmallTitle(text = tr("settings.language"), insideMargin = SectionTitlePadding)
                    Card(Modifier.fillMaxWidth()) {
                        OverlayDropdownPreference(title = tr("settings.language"),
                            summary = if (Languages.choice == AppLanguage.FollowSystem)
                                tr("settings.follow_system_language", Languages.current.nativeName)
                            else tr("settings.choose_language"),
                            items = AppLanguage.entries.map {
                                if (it == AppLanguage.FollowSystem) tr("settings.follow_system") else it.nativeName
                            },
                            selectedIndex = Languages.choice.ordinal,
                            onSelectedIndexChange = { value -> mutate {
                                store.saveLanguage(value)
                                Languages.set(AppLanguage.entries[value])
                            } })
                    }
                    SmallTitle(text = tr("settings.appearance"), insideMargin = SectionTitlePadding)
                    Card(Modifier.fillMaxWidth()) {
                        ArrowPreference(title = tr("theme.title"), summary = tr("settings.theme_summary"), onClick = { navigate(4) })
                        if (Languages.current.isChinese) {
                            SwitchPreference(title = tr("settings.quote_enable"), summary = tr("settings.quote_summary"),
                                checked = quoteEnabled,
                                onCheckedChange = { value -> mutate {
                                    store.saveQuoteEnabled(value)
                                    quoteChoice = value
                                } })
                        }
                    }
                    SmallTitle(text = tr("settings.backup"), insideMargin = SectionTitlePadding)
                    Card(Modifier.fillMaxWidth()) {
                        ArrowPreference(title = tr("settings.import_export"), summary = tr("settings.import_export_summary"), onClick = { navigate(5) })
                    }
                    SmallTitle(text = tr("settings.app"), insideMargin = SectionTitlePadding)
                    Card(Modifier.fillMaxWidth()) {
                        ArrowPreference(title = tr("about.title"), summary = tr("about.help_and_privacy"), onClick = { navigate(3) })
                        if (developerMode) {
                            ArrowPreference(title = tr("settings.developer_options"),
                                summary = tr("settings.developer_options_summary"), onClick = { navigate(8) })
                        }
                    }
                }
                4 -> {
                    SmallTitle(text = tr("settings.theme"), insideMargin = SectionTitlePadding)
                    Card(Modifier.fillMaxWidth()) {
                        OverlayDropdownPreference(title = tr("theme.mode"), summary = tr("theme.choose_mode"),
                            items = listOf(tr("settings.follow_system"), tr("theme.light"), tr("theme.dark")), selectedIndex = appearance.themeMode,
                            onSelectedIndexChange = { value -> mutate { store.saveAppearance(store.appearance().copy(themeMode = value)) } })
                        SwitchPreference(title = tr("theme.pure_black"), summary = tr("theme.pure_black_summary"),
                            checked = appearance.pureBlackBackground,
                            onCheckedChange = { value -> mutate { store.saveAppearance(store.appearance().copy(pureBlackBackground = value)) } })
                        SwitchPreference(title = tr("theme.blur"), summary = if (android.os.Build.VERSION.SDK_INT >= 33) tr("settings.blur_summary") else tr("theme.needs_android13"),
                            enabled = android.os.Build.VERSION.SDK_INT >= 33,
                            checked = appearance.blurEnabled,
                            onCheckedChange = { value -> mutate { store.saveAppearance(store.appearance().copy(blurEnabled = value)) } })
                        OverlayDropdownPreference(title = tr("theme.accent"), summary = tr("theme.choose_accent"),
                            items = listOf(tr("theme.palette_default"), tr("theme.monet"), tr("theme.color_blue"), tr("theme.color_green"), tr("theme.color_orange"), tr("theme.color_purple")),
                            selectedIndex = appearance.accentIndex,
                            onSelectedIndexChange = { value -> mutate { store.saveAppearance(store.appearance().copy(accentIndex = value)) } })
                        AnimatedVisibility(
                            visible = appearance.accentIndex != 0,
                            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                        ) {
                            Column {
                                OverlayDropdownPreference(title = tr("theme.palette_style"), summary = tr("theme.monet_summary"),
                                    enabled = appearance.accentIndex >= 2 || (appearance.accentIndex == 1 && monetSeed != null),
                                    items = ThemePaletteStyle.entries.map { it.name }, selectedIndex = appearance.paletteStyle,
                                    onSelectedIndexChange = { value -> mutate { store.saveAppearance(store.appearance().copy(paletteStyle = value)) } })
                                OverlayDropdownPreference(title = tr("theme.color_spec"), summary = tr("theme.material3_palette"),
                                    enabled = appearance.accentIndex >= 2 || (appearance.accentIndex == 1 && monetSeed != null),
                                    items = listOf("2021", "2025"), selectedIndex = appearance.colorSpec,
                                    onSelectedIndexChange = { value -> mutate { store.saveAppearance(store.appearance().copy(colorSpec = value)) } })
                            }
                        }
                        SwitchPreference(title = tr("settings.floating_bar_enabled"), summary = tr("settings.floating_bar_summary"),
                            checked = appearance.floatingNavBar,
                            onCheckedChange = { value -> mutate { store.saveAppearance(store.appearance().copy(floatingNavBar = value)) } })
                        AnimatedVisibility(visible = appearance.floatingNavBar) {
                            Column {
                                OverlayDropdownPreference(title = tr("settings.floating_bar_style"),
                                    items = listOf(tr("theme.nav_style_miuix"), tr("theme.nav_style_ios")),
                                    selectedIndex = appearance.floatingNavBarStyle,
                                    onSelectedIndexChange = { value -> mutate { store.saveAppearance(store.appearance().copy(floatingNavBarStyle = value)) } })
                            }
                        }
                    }
                    SmallTitle(text = tr("nav.home"), insideMargin = SectionTitlePadding)
                    Card(Modifier.fillMaxWidth()) {
                        OverlayDropdownPreference(title = tr("home.banner"), items = listOf(tr("theme.light"), tr("theme.dark")),
                            selectedIndex = appearance.bannerStyle,
                            onSelectedIndexChange = { value -> mutate { store.saveAppearance(store.appearance().copy(bannerStyle = value)) } })
                        ArrowPreference(title = tr("settings.banner_decimals"),
                            summary = tr("settings.banner_decimals_summary", appearance.bannerDecimals),
                            onClick = {
                                bannerDecimalsDraft = appearance.bannerDecimals
                                bannerDecimalsSheet = true
                            })
                        // Keep the entry visible while the aggregate is on even if every wish was
                        // deleted; otherwise the user would have no way back to 自动选择.
                        if (goals.any { !it.archived } || appearance.bannerAllGoals) OverlayDropdownPreference(title = tr("home.banner_goal"),
                            items = listOf(tr("theme.auto"), tr("theme.all_goals")) + goals.filter { !it.archived }.map { it.title },
                            selectedIndex = when {
                                appearance.bannerAllGoals -> 1
                                else -> goals.filter { !it.archived }.indexOfFirst { it.id == appearance.featuredGoalId }
                                    .let { if (it < 0) 0 else it + 2 }
                            },
                            onSelectedIndexChange = { value ->
                                mutate {
                                    store.saveAppearance(store.appearance().copy(
                                        bannerAllGoals = value == 1,
                                        featuredGoalId = if (value >= 2) goals[value - 2].id else ""))
                                }
                            })
                        SwitchPreference(title = tr("settings.collapse_card_actions"), summary = tr("settings.collapse_card_actions_summary"),
                            checked = appearance.collapseCardActions,
                            onCheckedChange = { value -> mutate { store.saveAppearance(store.appearance().copy(collapseCardActions = value)) } })
                        SwitchPreference(title = tr("settings.predictive_back"), summary = tr("settings.predictive_back_summary"),
                            checked = appearance.predictiveBackEnabled,
                            onCheckedChange = { value -> mutate { store.saveAppearance(store.appearance().copy(predictiveBackEnabled = value)) } })
                    }
                    OverlayBottomSheet(
                        show = bannerDecimalsSheet,
                        title = tr("settings.banner_decimals"),
                        startAction = {
                            IconButton(onClick = { bannerDecimalsSheet = false }) {
                                Icon(MiuixIcons.Close, contentDescription = tr("common.cancel"),
                                    tint = MiuixTheme.colorScheme.onBackground)
                            }
                        },
                        endAction = {
                            IconButton(onClick = {
                                mutate { store.saveAppearance(store.appearance().copy(bannerDecimals = bannerDecimalsDraft)) }
                                bannerDecimalsSheet = false
                            }) {
                                Icon(MiuixIcons.Ok, contentDescription = tr("common.confirm"),
                                    tint = MiuixTheme.colorScheme.onBackground)
                            }
                        },
                        onDismissRequest = { bannerDecimalsSheet = false },
                        insideMargin = DpSize(24.dp, 48.dp),
                    ) {
                        // The sheet paints itself with colorScheme.background. The stock palette sets
                        // surfaceContainer to that same color, so use [sheetCardColors] here too.
                        Card(Modifier.fillMaxWidth(), colors = sheetCardColors()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Text(tr("settings.banner_decimals_hint"), modifier = Modifier.weight(1f),
                                        style = MiuixTheme.textStyles.footnote1,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                                    Text(tr("settings.banner_decimals_value", bannerDecimalsDraft),
                                        style = MiuixTheme.textStyles.title3, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = bannerDecimalsDraft.toFloat(),
                                    onValueChange = { raw ->
                                        val next = raw.roundToInt().coerceIn(0, 5)
                                        if (next != bannerDecimalsDraft) {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                            bannerDecimalsDraft = next
                                        }
                                    },
                                    modifier = Modifier.pointerInput(Unit) {
                                        val sliderSize = size
                                        awaitEachGesture {
                                            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                                            waitForUpOrCancellation(pass = PointerEventPass.Initial) ?: return@awaitEachGesture
                                            val thumbRadius = sliderSize.height / 2f
                                            val available = (sliderSize.width - 2f * thumbRadius).coerceAtLeast(1f)
                                            val fraction = ((down.position.x - thumbRadius) / available).coerceIn(0f, 1f)
                                            val next = (fraction * 5f).roundToInt().coerceIn(0, 5)
                                            if (next != bannerDecimalsDraft) {
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                                bannerDecimalsDraft = next
                                            }
                                        }
                                    },
                                    valueRange = 0f..5f,
                                    steps = 4,
                                    showKeyPoints = true,
                                    keyPoints = listOf(0f, 1f, 2f, 3f, 4f, 5f),
                                    hapticEffect = SliderDefaults.SliderHapticEffect.None,
                                )
                            }
                        }
                    }
                }
                5 -> {
                    Card(Modifier.fillMaxWidth()) {
                        ArrowPreference(title = tr("backup.export"), summary = tr("backup.export_summary"),
                            enabled = !backupBusy, onClick = {
                                exportLauncher.launch("starswish-backup-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT).format(Date())}.json")
                            })
                        ArrowPreference(title = tr("backup.import"), summary = tr("settings.import_export_summary_short"),
                            enabled = !backupBusy, onClick = { importLauncher.launch(arrayOf("application/json", "text/*", "application/octet-stream")) })
                    }
                    if (backupBusy) Note(tr("backup.processing"))
                    SmallTitle(tr("common.notice"), insideMargin = SectionTitlePadding)
                    Panel {
                        Note(tr("backup.plain_json_note"))
                        Note(tr("backup.excludes_note"))
                    }
                }
                3 -> {
                    Panel {
                        Box(Modifier.squircleClip(cornerRadius = 15.dp).clickable {
                            aboutIconTaps++
                            if (aboutIconTaps >= 10 && !store.developerModeEnabled()) {
                                aboutIconTaps = 0
                                if (mutate { store.setDeveloperMode(true) }) notify(tr("webdav.developer_enabled"))
                            }
                        }) { IconTile("star") }
                        Text(tr("app.name_full"), style = MiuixTheme.textStyles.title2, fontWeight = FontWeight.Bold)
                        Text("$versionName  /  ${tr("about.made_by_kelsuita")}",
                            style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                    }
                    Card(Modifier.fillMaxWidth()) {
                        ArrowPreference(title = tr("about.github_link"), summary = tr("about.project_note"), onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/kelsuita/Starwish")))
                        })
                        // Informational only: which branch this copy was built from.
                        BasicComponent(
                            title = tr("about.channel"),
                            endActions = {
                                Text(BuildConfig.BUILD_CHANNEL, style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                            },
                        )
                        ArrowPreference(title = tr("about.donate"), summary = tr("about.donate_summary"), onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://afdian.com/a/kelsuita")))
                        })
                    }
                    SmallTitle(tr("accessibility.steps"), insideMargin = SectionTitlePadding)
                    Panel {
                        HelpStep("01", tr("account.add"), tr("account.name_must_match"))
                        HelpStep("02", tr("goal.create_wish"), tr("goal.form_intro"))
                        HelpStep("03", tr("accessibility.read_and_confirm"), tr("accessibility.read_help"))
                    }
                    SmallTitle(tr("about.data_stays_local"), insideMargin = SectionTitlePadding)
                    Panel {
                        Note(tr("about.privacy_note"))
                        Note(tr("accessibility.read_privacy_note"))
                        TextButton(text = tr("accessibility.manage_permission"), onClick = { disclosure = true },
                            modifier = Modifier.fillMaxWidth())
                    }
                    SmallTitle(tr("goal.how_progress_works"), insideMargin = SectionTitlePadding)
                    Panel {
                        Note(tr("goal.calc_help_balance"))
                        Note(tr("goal.edit_help"))
                    }
                    Button(onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://cxincx.com/support/scheme.html")))
                    }, modifier = Modifier.fillMaxWidth()) { Text(tr("about.xiaoxing_docs")) }
                }
                8 -> {
                    Card(Modifier.fillMaxWidth()) {
                        SwitchPreference(title = tr("settings.developer_options"),
                            summary = tr("settings.developer_options_summary"),
                            checked = developerMode,
                            onCheckedChange = { value -> mutate { store.setDeveloperMode(value) } })
                    }
                    if (developerMode) {
                        SmallTitle(text = tr("settings.developer_accounts"), insideMargin = SectionTitlePadding)
                        Card(Modifier.fillMaxWidth()) {
                            SwitchPreference(title = tr("settings.webdav_balance"),
                                summary = tr("settings.webdav_balance_summary"),
                                checked = webDavMode,
                                onCheckedChange = { value -> mutate { store.setWebDavBalanceEnabled(value) } })
                        }
                        SmallTitle(text = tr("settings.developer_interface"), insideMargin = SectionTitlePadding)
                        IosBarBlurPreference(
                            enabled = useIosBar,
                            radius = iosBarBlurRadius,
                            onRadiusChange = { iosBarBlurRadius = it },
                            onRadiusChangeFinished = { store.setIosBarBlurRadius(iosBarBlurRadius) },
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        }
        }
        }
        Box(Modifier.fillMaxSize()
            .then(if (route.page < 3 && selectedTab != 0) Modifier.graphicsLayer {
                translationX = size.width * 0.06f * backProgress
                scaleX = 1f - 0.04f * backProgress
                scaleY = 1f - 0.04f * backProgress
            } else Modifier)) {
        Box(Modifier.fillMaxSize()
            .then(if (bottomBackdrop != null) Modifier.layerBackdrop(bottomBackdrop) else Modifier)) {
                if (route.page >= 3) pageContent(route.page)
                else HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize().pagerGestureOverride(pagerState),
                    userScrollEnabled = false,
                    pageNestedScrollConnection = PagerGestureNestedScrollConnection,
                    flingBehavior = PagerDefaults.flingBehavior(pagerState, snapAnimationSpec = PagerNavigationSpringSpec),
                    verticalAlignment = Alignment.Top,
                    beyondViewportPageCount = 0,
                ) { displayedPage -> pageContent(displayedPage) }
            }
        }
        OverlayBottomSheet(
            show = webDavSheet,
            title = tr("webdav.title"),
            startAction = { IconButton(onClick = { webDavSheet = false }) { Icon(MiuixIcons.Close, contentDescription = tr("common.cancel"), tint = MiuixTheme.colorScheme.onBackground) } },
            endAction = { IconButton(onClick = { readWebDav() }, enabled = !webDavBusy) { Icon(MiuixIcons.Ok, contentDescription = tr("common.confirm"), tint = MiuixTheme.colorScheme.onBackground) } },
            onDismissRequest = { if (!webDavBusy) webDavSheet = false },
            onDismissFinished = {
                val picker = pendingWebDavPicker
                val plan = pendingWebDavPlan
                if (picker != null) {
                    pendingWebDavPicker = null
                    presentWebDavPicker(picker)
                } else if (plan != null) {
                    pendingWebDavPlan = null
                    webDavPlan = plan
                    webDavChangesVisible = true
                }
            },
            insideMargin = DpSize(24.dp, 48.dp),
        ) {
            Column(Modifier.fillMaxWidth()) {
                SmallTitle(text = tr("webdav.account_config"), insideMargin = SectionTitlePadding)
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.defaultColors(
                        color = MiuixTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MiuixTheme.colorScheme.onSurfaceContainer,
                    ),
                ) {
                    Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextField(value = webDavUrl, onValueChange = { webDavUrl = it }, label = tr("webdav.url"),
                            singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                            colors = sheetFieldColors())
                        TextField(value = webDavAccount, onValueChange = { webDavAccount = it }, label = tr("webdav.account"),
                            singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                            colors = sheetFieldColors())
                        TextField(value = webDavPassword, onValueChange = { webDavPassword = it; webDavPasswordEdited = true }, label = tr("webdav.password"),
                            visualTransformation = PasswordVisualTransformation(), singleLine = true,
                            colors = sheetFieldColors())
                    }
                }
                if (webDavBusy) Text(tr("webdav.reading"), color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
        OverlayBottomSheet(
            show = webDavPickerVisible,
            title = tr("webdav.picker_title"),
            startAction = { IconButton(onClick = { webDavPickerVisible = false }) { Icon(MiuixIcons.Close, contentDescription = tr("common.cancel"), tint = MiuixTheme.colorScheme.onBackground) } },
            endAction = {
                IconButton(onClick = { confirmWebDavPicker() },
                    enabled = webDavPicker.any { it.note == null && it.id in webDavPickerSelection }) {
                    Icon(MiuixIcons.Ok, contentDescription = tr("common.confirm"), tint = MiuixTheme.colorScheme.onBackground)
                }
            },
            onDismissRequest = { webDavPickerVisible = false },
            onDismissFinished = {
                webDavPicker = emptyList()
                webDavPickerSelection = emptySet()
            },
            insideMargin = DpSize(24.dp, 48.dp),
        ) {
            Column(Modifier.fillMaxWidth().heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(tr("webdav.picker_summary"), modifier = Modifier.padding(horizontal = 16.dp),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                Card(Modifier.fillMaxWidth(), colors = sheetCardColors()) {
                    val remainingSlots = webDavPickerRemainingSlots()
                    webDavPicker.forEach { option ->
                        val note = WebDavAccountSelection.selectionNote(option, webDavPickerSelection, remainingSlots)
                        val amount = "¥ ${Money.format(option.cents)}"
                        CheckboxPreference(
                            title = option.name,
                            summary = when {
                                note != null -> "$amount · ${tr(note)}"
                                option.creates -> "$amount · ${tr("webdav.picker_create")}"
                                else -> amount
                            },
                            checked = option.id in webDavPickerSelection,
                            enabled = note == null,
                        onCheckedChange = { checked ->
                                if (!checked) webDavPickerSelection -= option.id
                                else if (WebDavAccountSelection.selectionNote(option, webDavPickerSelection,
                                        webDavPickerRemainingSlots()) == null) {
                                    webDavPickerSelection += option.id
                                }
                            },
                        )
                    }
                }
            }
        }
        OverlayBottomSheet(
            show = webDavChangesVisible,
            title = tr("webdav.confirm_title"),
            startAction = { IconButton(onClick = { closeWebDavChanges() }) { Icon(MiuixIcons.Close, contentDescription = tr("common.cancel"), tint = MiuixTheme.colorScheme.onBackground) } },
            endAction = { IconButton(onClick = { confirmWebDavChanges() }) { Icon(MiuixIcons.Ok, contentDescription = tr("common.confirm"), tint = MiuixTheme.colorScheme.onBackground) } },
            onDismissRequest = { closeWebDavChanges() },
            onDismissFinished = { webDavPlan = null },
            insideMargin = DpSize(24.dp, 48.dp),
        ) {
            val plan = webDavPlan
            // The card's bottom edge sits 48dp above the screen bottom (sheet inside margin).
            Card(Modifier.fillMaxWidth().heightIn(max = 480.dp), colors = sheetCardColors()) {
                Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (plan == null || (plan.changes.isEmpty() && plan.additions.isEmpty())) {
                        Text(tr("webdav.no_changes"))
                    }
                    plan?.changes?.forEach { change ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(change.name, style = MiuixTheme.textStyles.body1, maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f))
                            Text("¥ ${Money.format(change.before)} → ¥ ${Money.format(change.after)}",
                                style = MiuixTheme.textStyles.body1, fontWeight = FontWeight.Bold)
                        }
                    }
                    plan?.additions?.forEach { addition ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(addition.name, style = MiuixTheme.textStyles.body1, maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f))
                            Text("${tr("webdav.new_account")} ¥ ${Money.format(addition.cents)}",
                                style = MiuixTheme.textStyles.body1, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        }
      }
    }
    }
    }
    DialogHost(importVisible) { finished ->
        pendingBackup?.let { backup ->
            AppDialog(
                show = importVisible,
                title = tr("backup.import_title"),
                onDismiss = { if (!backupBusy) importVisible = false },
                onDismissFinished = { pendingBackupJson = null; finished() },
            ) {
                Note(tr("backup.confirm_summary", backup.accounts.size, backup.goals.size))
                if (backup.exportedAt > 0L) {
                    val date = java.text.DateFormat.getDateTimeInstance().format(Date(backup.exportedAt))
                    Note(tr("backup.created_at", date))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(text = tr("common.cancel"), onClick = { importVisible = false },
                        enabled = !backupBusy, modifier = Modifier.weight(1f))
                    TextButton(text = tr("backup.confirm_import"), onClick = {
                        scope.launch {
                            backupBusy = true
                            try {
                                invalidateSortUndo()
                                goalSortQueue.flush()
                                vaultSortQueue.flush()
                                accountSortQueue.flush()
                                sortCommitLock.withLock {
                                    withContext(Dispatchers.IO) { store.importBackup(backup) }
                                    goalSortQueue.revision++
                                    vaultSortQueue.revision++
                                    accountSortQueue.revision++
                                }
                                sortDataVersion++
                                iosBarBlurRadius = store.iosBarBlurRadius()
                                hitokoto = backup.hitokoto
                                quoteChoice = backup.quoteEnabled
                                Languages.set(AppLanguage.entries[backup.language])
                                importVisible = false
                                refresh()
                                notify(tr("backup.imported"))
                            } catch (error: Exception) {
                                if (error is CancellationException) throw error
                                notify(tr("backup.import_failed", error.message ?: tr("common.save_failed")))
                            } finally { backupBusy = false }
                        }
                    }, enabled = !backupBusy, modifier = Modifier.weight(1f),
                        colors = primaryTextButtonColors())
                }
            }
        }
    }
    DialogHost(goalEditor) { finished ->
        GoalEditor(
            show = goalEditor,
            existing = editingGoal,
            accounts = accounts,
            onDismiss = { goalEditor = false },
            onDismissFinished = {
                editingGoalId = null
                finished()
                if (openAccountAfterGoal) {
                    openAccountAfterGoal = false
                    navigate(1)
                    editingAccountId = null
                    accountEditor = true
                }
            },
            onSave = { goal ->
                if (mutate { store.saveGoal(goal) }) {
                    goalEditor = false
                    // Saving without an account is allowed, but the read action needs one.
                    if (goal.accountIds.isEmpty()) notify(tr("goal.no_accounts_notice"))
                }
            },
            onAddAccount = { openAccountAfterGoal = true; goalEditor = false },
        )
    }
    DialogHost(accountEditor) { finished ->
        AccountEditor(
            show = accountEditor,
            existing = editingAccount,
            onDismiss = { accountEditor = false },
            onDismissFinished = { editingAccountId = null; finished() },
            goals = goals,
            onSave = { account, updatedGoals, blankBalance ->
                if (mutate {
                        store.saveAccount(account)
                        updatedGoals.forEach { store.saveGoal(it) }
                    }) {
                    accountEditor = false
                    // Say it out loud: a saved 0 is easy to mistake for a failed read.
                    if (blankBalance) notify(tr("account.balance_blank_notice"))
                }
            },
        )
    }
    DialogHost(disclosure) { finished ->
        AppDialog(
            show = disclosure,
            title = tr("accessibility.read_ledger_balance"),
            onDismiss = { disclosure = false },
            onDismissFinished = finished,
        ) {
            Note(tr("accessibility.enable_in_settings_note"))
            Note(tr("accessibility.permission_summary"))
            Note(tr("accessibility.privacy_detail"))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(text = tr("common.not_now"), onClick = { disclosure = false }, modifier = Modifier.weight(1f))
                TextButton(text = tr("accessibility.learn_and_open_settings"), onClick = {
                    disclosure = false
                    openReaderSettings()
                }, modifier = Modifier.weight(1f), colors = primaryTextButtonColors())
            }
        }
    }
    DialogHost(archivingVisible) { finished ->
        archivingGoal?.let { goal ->
            AppDialog(
                show = archivingVisible,
                title = tr("goal.bittersweet"),
                onDismiss = { archivingVisible = false },
                onDismissFinished = { archivingGoal = null; finished() },
            ) {
                // The congratulations gets its own line at title size; the instruction stays a
                // muted note so the two do not compete.
                Text(tr("goal.bittersweet_congrats"), style = MiuixTheme.textStyles.title3,
                    fontWeight = FontWeight.Bold)
                Note(if (webDavActive) tr("goal.bittersweet_webdav") else tr("goal.bittersweet_local"))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(text = tr("common.cancel"), onClick = { archivingVisible = false },
                        modifier = Modifier.weight(1f))
                    TextButton(text = tr("goal.cheer"), onClick = {
                        if (mutate {
                                store.saveGoal(goal.copy(archived = true, archivedAt = System.currentTimeMillis()))
                            }) {
                            archivingVisible = false
                            if (webDavActive) vaultRefreshRequest++
                        }
                    }, modifier = Modifier.weight(1f), colors = primaryTextButtonColors())
                }
            }
        }
    }
    DialogHost(removingVisible) { finished ->
        removingGoal?.let { goal ->
            AppDialog(
                show = removingVisible,
                title = tr("vault.remove_title"),
                onDismiss = { removingVisible = false },
                onDismissFinished = {
                    removingGoal = null
                    finished()
                    if (restoringMissingGoal != null) restoringMissingVisible = true
                },
            ) {
                Note(tr("vault.remove_message", goal.title))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(text = tr("common.cancel"), onClick = { removingVisible = false },
                        modifier = Modifier.weight(1f))
                    TextButton(text = tr("vault.remove_title"), onClick = {
                        val missingIds = goal.accountIds.filter { id -> accounts.none { it.id == id } }
                        val legacyNames = store.deletedAccountNames(missingIds)
                        val deletedNames = goal.deletedLinkedAccounts + missingIds.associateWith { id ->
                            legacyNames[id] ?: tr("vault.restore_missing_unknown")
                        }
                        if (deletedNames.isEmpty()) {
                            if (mutate { store.saveGoal(restoreGoalFromVault(goal, emptySet())) }) {
                                removingVisible = false
                            }
                        } else {
                            restoringMissingGoal = goal
                            restoringMissingAccounts = deletedNames
                            removingVisible = false
                        }
                    }, modifier = Modifier.weight(1f), colors = primaryTextButtonColors())
                }
            }
        }
    }
    DialogHost(restoringMissingVisible) { finished ->
        restoringMissingGoal?.let { goal ->
            AppDialog(
                show = restoringMissingVisible,
                title = tr("vault.restore_missing_title"),
                onDismiss = { restoringMissingVisible = false },
                onDismissFinished = { restoringMissingGoal = null; finished() },
            ) {
                Note(tr("vault.restore_missing_message", restoringMissingAccounts.values.joinToString("、")))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(text = tr("common.cancel"), onClick = { restoringMissingVisible = false },
                        modifier = Modifier.weight(1f))
                    TextButton(text = tr("vault.restore_confirm"), onClick = {
                        // Only legacy links still need to be detached.
                        val ids = restoringMissingAccounts.keys.intersect(goal.accountIds)
                        val restored = restoreGoalFromVault(goal, ids)
                        // Another vaulted wish may still link the same deleted account, so keep the
                        // name it was recorded under until no archived wish refers to it.
                        val stillLinked = goals.filter { it.id != goal.id && it.archived }
                            .flatMap { it.accountIds }.toSet()
                        if (mutate { store.saveGoal(restored); store.forgetDeletedAccountNames(ids - stillLinked) }) {
                            restoringMissingVisible = false
                        }
                    }, modifier = Modifier.weight(1f), colors = primaryTextButtonColors())
                }
            }
        }
    }
    DialogHost(deletingGoalVisible) { finished ->
        deletingGoal?.let { goal ->
            AppDialog(
                show = deletingGoalVisible,
                title = tr("goal.delete_title"),
                onDismiss = { deletingGoalVisible = false },
                onDismissFinished = { deletingGoal = null; finished() },
            ) {
                Note(tr("goal.delete_message", goal.title))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(text = tr("common.cancel"), onClick = { deletingGoalVisible = false },
                        modifier = Modifier.weight(1f))
                    TextButton(text = tr("common.delete"), onClick = {
                        if (mutate { store.deleteGoal(goal.id) }) deletingGoalVisible = false
                    }, modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(color = MiuixTheme.colorScheme.error,
                            textColor = MiuixTheme.colorScheme.onError))
                }
            }
        }
    }
    DialogHost(deletingAccountVisible) { finished ->
        deletingAccount?.let { account ->
            AppDialog(
                show = deletingAccountVisible,
                title = tr("account.delete_title"),
                onDismiss = { deletingAccountVisible = false },
                onDismissFinished = {
                    // A linked account moves on to the second confirmation instead of finishing.
                    if (accountDeletePending) {
                        accountDeletePending = false
                        accountDeleteStep2 = true
                    } else {
                        deletingAccount = null
                    }
                    finished()
                },
            ) {
                val linkedCount = goals.count { account.id in it.accountIds }
                Note(tr("account.delete_plain_message", account.name))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(text = tr("common.cancel"), onClick = { deletingAccountVisible = false },
                        modifier = Modifier.weight(1f))
                    TextButton(text = tr("common.delete"), onClick = {
                        if (linkedCount > 0) {
                            accountDeletePending = true
                            deletingAccountVisible = false
                        } else if (mutate { store.deleteAccount(account.id) }) {
                            deletingAccountVisible = false
                        }
                    }, modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(color = MiuixTheme.colorScheme.error,
                            textColor = MiuixTheme.colorScheme.onError))
                }
            }
        }
    }
    // Second confirmation, only for an account that wishes still link: the balance it holds is
    // about to leave those wishes, so name the account and the number of affected wishes.
    DialogHost(accountDeleteStep2) { finished ->
        deletingAccount?.let { account ->
            val linkedCount = goals.count { account.id in it.accountIds }
            AppDialog(
                show = accountDeleteStep2,
                title = tr("account.delete_confirm_title"),
                onDismiss = { accountDeleteStep2 = false },
                onDismissFinished = { deletingAccount = null; finished() },
            ) {
                Note(tr("account.delete_linked_message", account.name, linkedCount))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(text = tr("common.cancel"), onClick = { accountDeleteStep2 = false },
                        modifier = Modifier.weight(1f))
                    TextButton(text = tr("account.delete_confirm_label"), onClick = {
                        if (mutate { store.deleteAccount(account.id) }) accountDeleteStep2 = false
                    }, modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(color = MiuixTheme.colorScheme.error,
                            textColor = MiuixTheme.colorScheme.onError))
                }
            }
        }
    }
}
}

/** Banner percentage: whole numbers at 0 decimals, otherwise exactly [decimals] places. */
private fun formatBannerPercent(progress: Float, decimals: Int): String {
    val percent = (progress * 100f).coerceIn(0f, 100f)
    return if (decimals <= 0) "${percent.toInt()}%"
    else String.format(Locale.ROOT, "%.${decimals.coerceIn(1, 5)}f%%", percent)
}

@Composable private fun Overview(goals: List<SavingGoal>, accounts: List<BankAccount>, appearance: Appearance,
    accent: Color, deviceAccountName: String?, onOpenVault: () -> Unit) {
    // "Add everything up" replaces the featured wish with the merged progress of every wish.
    // It needs at least one wish, otherwise the empty-state banner stays as it is.
    val aggregate = appearance.bannerAllGoals && goals.isNotEmpty()
    val featured = if (aggregate) null
        else goals.firstOrNull { it.id == appearance.featuredGoalId } ?: goals.firstOrNull()
    val aggregated = remember(goals, accounts) { BannerAggregate.result(goals, accounts) }
    val saved = if (aggregate) aggregated.saved else featured?.saved(accounts) ?: 0L
    val target = if (aggregate) aggregated.target else featured?.target ?: 0L
    val rawProgress = if (aggregate) aggregated.progress
        else if (target <= 0L) 0f else (saved.toDouble() / target).toFloat().coerceIn(0f, 1f)
    val completed = target > 0L && saved >= target
    val progress by animateFloatAsState(rawProgress, tween(650), label = "featured-progress")
    val percentText = formatBannerPercent(progress, appearance.bannerDecimals)
    val title = when {
        aggregate -> deviceAccountName ?: tr("home.all_wishes")
        featured != null -> featured.title
        else -> tr("home.empty_create_goal")
    }
    // The caption names what the banner covers, following the normal wish banner's caption + title
    // pair. In aggregate mode the title is the phone account when Android exposes one, so the
    // caption keeps saying "all wishes"; when the title itself has to fall back to "all wishes",
    // the caption names the aggregation instead of repeating the title.
    val label = when {
        !aggregate -> tr("home.current_wish")
        title == tr("home.all_wishes") -> tr("home.banner_total")
        else -> tr("home.all_wishes")
    }
    val status = appearance.bannerStyle == 0
    val foreground = if (status) MiuixTheme.colorScheme.onPrimaryContainer else MiuixTheme.colorScheme.onPrimary
    val subtle = foreground.copy(alpha = if (status) .7f else .82f)
    val base = if (status) MiuixTheme.colorScheme.primaryContainer else accent
    val fill = if (status) MiuixTheme.colorScheme.onPrimaryContainer else MiuixTheme.colorScheme.onPrimary
    Card(
        modifier = Modifier.fillMaxWidth()
            .clearAndSetSemantics {
                contentDescription = tr("home.banner_description", title,
                    if (completed) tr("home.completed") else percentText)
                // A long press has no visual affordance, so offer it as an explicit action too.
                customActions = listOf(CustomAccessibilityAction(tr("vault.title")) {
                    onOpenVault()
                    true
                })
            },
        cornerRadius = 20.dp,
        insideMargin = PaddingValues(0.dp),
        colors = CardDefaults.defaultColors(color = base),
        pressFeedbackType = PressFeedbackType.Tilt,
        showIndication = false,
        onLongPress = onOpenVault,
    ) {
        Box(Modifier.fillMaxWidth()) {
            Box(Modifier.align(Alignment.TopEnd).padding(18.dp)) {
                if (completed) Text("🎉", fontSize = 44.sp)
                else Symbol("star", foreground.copy(alpha = .22f), Modifier.size(44.dp))
            }
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // The caption line is part of the header in every mode, so the banner keeps the same
                // height whether it shows one wish or the aggregate and switching targets never
                // reflows the page.
                if (label != title) Text(label, style = MiuixTheme.textStyles.footnote1, color = subtle)
                Text(title, style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold, color = foreground, maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (featured == null && !aggregate) tr("account.add_before_goal") else
                        "¥ ${Money.format(saved)} / ¥ ${Money.format(target)}",
                        style = MiuixTheme.textStyles.footnote1, color = subtle, modifier = Modifier.weight(1f))
                    Text(if (completed) tr("home.completed") else percentText,
                        style = MiuixTheme.textStyles.title3, fontWeight = FontWeight.Bold, color = foreground)
                }
                LinearProgressIndicator(progress = progress, height = 8.dp,
                    colors = ProgressIndicatorDefaults.progressIndicatorColors(
                        foregroundColor = fill, backgroundColor = fill.copy(alpha = .20f)))
                if (aggregate || featured != null) {
                    val remaining = (target - saved).coerceAtLeast(0L)
                    val remainingLabel = if (remaining == 0L) tr("goal.achieved")
                        else tr("goal.remaining_amount", Money.format(remaining))
                    // Only a single wish has a deadline to show; the aggregate covers all of them.
                    Text(if (featured != null && featured.deadline.isNotBlank())
                        tr("format.title_dot_detail", remainingLabel, featured.deadline) else remainingLabel,
                        color = subtle, style = MiuixTheme.textStyles.footnote2, maxLines = 1)
                }
            }
        }
    }
}

@Composable private fun GoalCard(goal: SavingGoal, accounts: List<BankAccount>, onEdit: () -> Unit, onDelete: () -> Unit, onSync: () -> Unit, onArchive: () -> Unit, collapseActions: Boolean, decimals: Int) {
    val color = MiuixTheme.colorScheme.primary
    // On the stock palette in dark mode the card accent moves to the container tint so the bar and
    // the percentage read as accents on the dark surface instead of the heavier primary.
    val accentColor = if (LocalStockPalette.current && isDarkScheme()) MiuixTheme.colorScheme.primaryContainer else color
    // A vault card is a finished wish: it always reads as complete, whatever the balances do.
    val archived = goal.archived
    val shownProgress = if (archived) 1f else goal.progress(accounts)
    val completed = goal.target > 0L && goal.saved(accounts) >= goal.target
    val progress by animateFloatAsState(shownProgress, tween(650), label = "goal-progress")
    val saved = goal.saved(accounts)
    val percentLabel = formatBannerPercent(shownProgress, decimals)
    Panel {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconTile("star")
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(goal.title, style = MiuixTheme.textStyles.title4.copy(fontSize = 19.sp), fontWeight = FontWeight.Bold, maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Text(when {
                    // A vault card reports when it got there instead of how it is measured.
                    archived && goal.archivedAt > 0L -> tr("goal.achieved_on", achievedDate(goal.archivedAt))
                    goal.mode == GoalMode.BALANCE -> tr("goal.mode_balance")
                    else -> tr("goal.count_from_creation")
                }, style = MiuixTheme.textStyles.footnote2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            }
            // Collapsed cards hand the corner to the menu and drop the chip entirely, 已达成
            // included: the vault is what marks a finished wish there.
            if (!collapseActions) {
                StatusPill(if (completed || archived) tr("goal.completed") else tr("goal.in_progress"))
            }
            if (collapseActions) {
                // 苦尽甘来 comes first: a finished wish is about to leave this list, so its one
                // meaningful action should not sit under the routine ones.
                val actions = buildList {
                    if (!archived && completed) add(DropdownItem(tr("goal.bittersweet"), onClick = onArchive))
                    if (!archived) add(DropdownItem(tr("account.update_balance"), onClick = onSync,
                        enabled = goal.accountIds.isNotEmpty()))
                    add(DropdownItem(tr("common.edit"), onClick = onEdit))
                    add(DropdownItem(tr("common.delete"), onClick = onDelete))
                }
                OverlayIconDropdownMenu(
                    entry = DropdownEntry(actions),
                    modifier = Modifier.padding(start = 4.dp),
                ) { Icon(MiuixIcons.More, contentDescription = tr("goal.menu"), modifier = Modifier.size(20.dp)) }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                // A vault card reports the wish itself: the amount reads as its target, so the
                // 「目标 ¥…」 line underneath would only repeat the same number.
                Text("¥ ${Money.format(if (archived) goal.target else saved)}",
                    style = MiuixTheme.textStyles.title2, fontWeight = FontWeight.Bold)
                if (!archived) {
                    // Without an account there is no target to compare against, so the line explains
                    // the missing link instead of showing an amount nothing feeds.
                    Text(if (goal.accountIds.isEmpty()) tr("goal.no_accounts")
                            else tr("goal.target_amount_line", Money.format(goal.target)),
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                }
            }
            Text(percentLabel, style = MiuixTheme.textStyles.title3, fontWeight = FontWeight.Bold, color = accentColor)
        }
        // The bar reports its range itself, so the description only has to name the goal and the
        // exact percentage, including the decimals the user picked.
        LinearProgressIndicator(progress = progress, height = 8.dp,
            modifier = Modifier.semantics { contentDescription = tr("home.progress_percent", percentLabel) },
            colors = ProgressIndicatorDefaults.progressIndicatorColors(foregroundColor = accentColor, backgroundColor = MiuixTheme.colorScheme.secondaryContainer))
        if (!collapseActions) {
            // At the target the read shrinks to half a row so 苦尽甘来 can sit beside it; a vault
            // card keeps neither, only the edit / delete pair.
            when {
                archived -> Unit
                completed -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(text = tr("account.update_balance"), onClick = onSync, modifier = Modifier.weight(1f),
                        colors = primaryTextButtonColors())
                    TextButton(text = tr("goal.bittersweet"), onClick = onArchive, modifier = Modifier.weight(1f),
                        colors = primaryTextButtonColors())
                }
                else -> TextButton(text = tr("account.update_balance"), onClick = onSync,
                    modifier = Modifier.fillMaxWidth(), colors = primaryTextButtonColors(),
                    enabled = goal.accountIds.isNotEmpty())
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(text = tr("common.edit"), onClick = onEdit, modifier = Modifier.weight(1f))
                TextButton(text = tr("common.delete"), onClick = onDelete, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable private fun GoalEditor(
    show: Boolean,
    existing: SavingGoal?,
    accounts: List<BankAccount>,
    onDismiss: () -> Unit,
    onDismissFinished: () -> Unit,
    onSave: (SavingGoal) -> Unit,
    onAddAccount: () -> Unit,
) {
    var title by rememberSaveable(existing?.id) { mutableStateOf(existing?.title.orEmpty()) }
    var amount by rememberSaveable(existing?.id) { mutableStateOf(existing?.let { Money.input(it.target) }.orEmpty()) }
    var date by rememberSaveable(existing?.id) { mutableStateOf(existing?.deadline.orEmpty()) }
    var selected by rememberSaveable(existing?.id) { mutableStateOf(existing?.accountIds?.toList() ?: emptyList<String>()) }
    var growth by rememberSaveable(existing?.id) { mutableStateOf(existing?.mode == GoalMode.GROWTH) }
    var titleError by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf("") }
    var dateError by remember { mutableStateOf("") }
    AppDialog(
        show = show,
        title = if (existing == null) tr("goal.new_wish") else tr("goal.edit_wish"),
        onDismiss = onDismiss,
        onDismissFinished = onDismissFinished,
    ) {
        TextField(value = title, onValueChange = { title = it.take(40); titleError = "" },
            label = tr("goal.name_placeholder"), singleLine = true,
            colors = dialogFieldColors(titleError.isNotEmpty()))
        FieldError(titleError)
        TextField(value = amount, onValueChange = { amount = it.take(18); amountError = "" }, label = tr("goal.target_amount"), singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = dialogFieldColors(amountError.isNotEmpty()))
        FieldError(amountError)
        TextField(value = date, onValueChange = { date = it.take(10); dateError = "" }, label = tr("goal.deadline_placeholder"), singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            colors = dialogFieldColors(dateError.isNotEmpty()))
        FieldError(dateError)
        SmallTitle(text = tr("goal.linked_accounts"), insideMargin = SectionTitlePadding)
        if (accounts.isEmpty()) {
            Note(tr("goal.no_accounts"))
            Button(onClick = onAddAccount, modifier = Modifier.fillMaxWidth()) { Text(tr("account.add_savings")) }
        } else {
            Card(Modifier.fillMaxWidth(), colors = sheetCardColors()) {
                accounts.forEach { account ->
                    CheckboxPreference(
                        title = account.name,
                        summary = "¥ ${Money.format(account.balance)}",
                        checked = account.id in selected,
                        onCheckedChange = { isChecked ->
                            selected = if (isChecked) selected + account.id else selected - account.id
                        },
                    )
                }
            }
        }
        SmallTitle(text = tr("goal.calculation_mode"), insideMargin = SectionTitlePadding)
        Card(Modifier.fillMaxWidth(), colors = sheetCardColors()) {
            RadioButtonPreference(
                title = tr("goal.current_balance"),
                summary = tr("goal.mode_balance_summary"),
                selected = !growth,
                onClick = { growth = false },
            )
            RadioButtonPreference(
                title = tr("goal.start_now"),
                summary = tr("goal.mode_growth_summary"),
                selected = growth,
                onClick = { growth = true },
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(text = tr("common.cancel"), onClick = onDismiss, modifier = Modifier.weight(1f))
            TextButton(text = if (existing == null) tr("goal.start_wish") else tr("common.save_changes"), onClick = {
                val target = Money.parse(amount)
                val parsedDate = if (date.isBlank()) null else runCatching { LocalDate.parse(date) }.getOrNull()
                titleError = if (title.isBlank()) tr("goal.name_empty_error") else ""
                amountError = if (target == null || target <= 0) tr("goal.error_invalid_amount") else ""
                dateError = when {
                    date.isNotBlank() && (parsedDate == null || parsedDate.toString() != date) ->
                        tr("goal.error_date_format")
                    parsedDate != null && parsedDate < LocalDate.now() && date != existing?.deadline ->
                        tr("goal.error_past_date")
                    else -> ""
                }
                if (titleError.isEmpty() && amountError.isEmpty() &&
                    dateError.isEmpty() && target != null) {
                    val mode = if (growth) GoalMode.GROWTH else GoalMode.BALANCE
                    // "Start from now" snapshots each account on its own, so adding an account to an
                    // existing wish starts that account at today's balance without resetting the
                    // progress already accumulated on the accounts that were there before.
                    val now = System.currentTimeMillis()
                    val previousLinks = existing?.links?.associateBy { it.accountId }.orEmpty()
                    val keepLinks = existing != null && existing.mode == GoalMode.GROWTH
                    val links = if (!growth) emptyList() else selected.map { id ->
                        previousLinks[id]?.takeIf { keepLinks } ?: GoalAccountLink(id, GoalMode.GROWTH,
                            accounts.first { it.id == id }.balance, now)
                    }
                    onSave(SavingGoal(existing?.id ?: UUID.randomUUID().toString(), title.trim(), target,
                        selected.toSet(), mode, if (growth) links.sumOf { it.baseline } else 0, date,
                        existing?.colorIndex ?: 0, links = links))
                }
            }, modifier = Modifier.weight(1f),
                colors = primaryTextButtonColors())
        }
    }
}

@Composable private fun AccountEditor(
    show: Boolean,
    existing: BankAccount?,
    goals: List<SavingGoal>,
    onDismiss: () -> Unit,
    onDismissFinished: () -> Unit,
    onSave: (BankAccount, List<SavingGoal>, Boolean) -> Unit,
) {
    var name by rememberSaveable(existing?.id) { mutableStateOf(existing?.name.orEmpty()) }
    var balance by rememberSaveable(existing?.id) { mutableStateOf(existing?.let { Money.input(it.balance) }.orEmpty()) }
    var nameError by remember { mutableStateOf("") }
    var balanceError by remember { mutableStateOf("") }
    // Which wishes this account takes part in, kept as a draft until 保存 is pressed.
    var linkedGoalIds by remember(existing?.id) {
        mutableStateOf(existing?.let { account ->
            goals.filter { account.id in it.accountIds }.map { it.id }.toSet()
        } ?: emptySet())
    }
    AppDialog(
        show = show,
        title = if (existing == null) tr("account.add_savings") else tr("account.edit"),
        onDismiss = onDismiss,
        onDismissFinished = onDismissFinished,
    ) {
        TextField(value = name, onValueChange = { name = it.take(40); nameError = "" }, label = tr("account.name_placeholder"), singleLine = true,
            colors = dialogFieldColors(nameError.isNotEmpty()))
        FieldError(nameError)
        TextField(value = balance, onValueChange = { balance = it.take(18); balanceError = "" }, label = tr("account.current_cny_balance"), singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = dialogFieldColors(balanceError.isNotEmpty()))
        FieldError(balanceError)
        // Existing accounts can be linked from either side; the wish editor shows the mirror
        // image of this list. Archived wishes are left out: they are no longer editable.
        val editableGoals = goals.filter { !it.archived }
        if (existing != null && editableGoals.isNotEmpty()) {
            SmallTitle(text = tr("account.linked_goals"), insideMargin = SectionTitlePadding)
            Card(Modifier.fillMaxWidth(), colors = sheetCardColors()) {
                editableGoals.forEach { goal ->
                    val isLinked = goal.id in linkedGoalIds
                    CheckboxPreference(
                        title = goal.title,
                        summary = tr("goal.target_amount_line", Money.format(goal.target)),
                        checked = isLinked,
                        onCheckedChange = { checked ->
                            linkedGoalIds = if (checked) linkedGoalIds + goal.id else linkedGoalIds - goal.id
                        },
                    )
                }
            }
            Note(tr("account.linked_goals_note"))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(text = tr("common.cancel"), onClick = onDismiss, modifier = Modifier.weight(1f))
            TextButton(text = tr("account.save"), onClick = {
                // An empty balance means zero: an account can be created before its first
                // reading, and clearing the field should not block saving.
                val cents = if (balance.isBlank()) 0L else Money.parse(balance)
                nameError = if (name.isBlank()) tr("account.error_name_required") else ""
                balanceError = if (cents == null) tr("account.error_invalid_balance") else ""
                if (nameError.isEmpty() && balanceError.isEmpty() && cents != null) {
                    val balanceChanged = existing == null || cents != existing.balance
                    val account = BankAccount(existing?.id ?: UUID.randomUUID().toString(), name.trim(), cents,
                        if (balanceChanged) System.currentTimeMillis() else existing.updatedAt,
                        if (balanceChanged) "account.source_manual" else existing.source)
                    onSave(account, linkAccountToGoals(account, goals, linkedGoalIds), balance.isBlank())
                }
            }, modifier = Modifier.weight(1f), colors = primaryTextButtonColors())
        }
    }
}

/** Card colors that stay visible on a bottom-sheet background in the stock palette. */
@Composable private fun sheetCardColors(): CardColors {
    val surface = MiuixTheme.colorScheme.surfaceContainer
    return if (surface != MiuixTheme.colorScheme.background) {
        CardDefaults.defaultColors()
    } else {
        CardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.secondaryContainer,
            contentColor = MiuixTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable private fun sheetFieldColors(): TextFieldColors = TextFieldDefaults.textFieldColors(
    backgroundColor = MiuixTheme.colorScheme.background,
    labelColor = MiuixTheme.colorScheme.onSurfaceVariantSummary,
    borderColor = MiuixTheme.colorScheme.primary,
)

@Composable private fun dialogFieldColors(invalid: Boolean): TextFieldColors = TextFieldDefaults.textFieldColors(
    backgroundColor = if (invalid) MiuixTheme.colorScheme.errorContainer else MiuixTheme.colorScheme.secondaryContainer,
    labelColor = if (invalid) MiuixTheme.colorScheme.onErrorContainer else MiuixTheme.colorScheme.onSecondaryContainer,
    borderColor = if (invalid) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.primary,
)

@Composable private fun FieldError(text: String) {
    if (text.isNotEmpty()) Text(text, style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.error)
}

/**
 * Keeps a dialog composed until Miuix reports that its hide animation finished, so the exit
 * animation is not cut off by removing the composable. Draft state is keyed by the edited id,
 * so it is dropped together with the host and a later reopen starts from saved values.
 */
@Composable private fun DialogHost(show: Boolean, content: @Composable (onDismissFinished: () -> Unit) -> Unit) {
    var mounted by remember { mutableStateOf(show) }
    LaunchedEffect(show) { if (show) mounted = true }
    if (mounted) content { mounted = false }
}

@Composable private fun AppDialog(
    show: Boolean,
    title: String,
    onDismiss: () -> Unit,
    onDismissFinished: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    WindowDialog(
        show = show,
        title = title,
        onDismissRequest = onDismiss,
        onDismissFinished = onDismissFinished,
    ) {
        // Dialogs are bottom-attached on phones, so keep long forms scrollable instead of letting
        // them grow past the IME. WindowDialog supplies the 24dp inside margin itself.
        Column(Modifier.fillMaxWidth().heightIn(max = 560.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            content()
        }
    }
}

@Composable private fun Symbol(kind: String, color: Color, modifier: Modifier) {
    val icon = when (kind) {
        "star" -> MiuixIcons.Favorites
        "bank" -> MiuixIcons.BankCards
        "link" -> MiuixIcons.Link
        else -> MiuixIcons.More
    }
    Icon(icon, contentDescription = null, modifier = modifier, tint = color)
}

/**
 * Coalesces reorder writes so a sorting session persists the final order once instead of on every
 * drop. Leaving the page flushes the pending write, so a drop is never lost to cancellation.
 */
private class SortWriteQueue(private val scope: CoroutineScope, private val delayMillis: Long = 400L) {
    var revision = 0
    private var job: Job? = null
    private var pending: (suspend () -> Unit)? = null

    fun submit(write: suspend () -> Unit) {
        pending = write
        job?.cancel()
        job = scope.launch {
            delay(delayMillis)
            // Clear the handle instead of cancelling: this coroutine has to run the write itself.
            job = null
            val work = pending
            pending = null
            work?.invoke()
        }
    }

    /** Runs the pending write immediately on page exit. */
    fun flush() {
        take()?.let { work -> scope.launch { work() } }
    }

    /** Invalidates a delayed write when a newer explicit operation takes ownership. */
    fun cancel() {
        pending = null
        job?.cancel()
        job = null
    }

    private fun take(): (suspend () -> Unit)? {
        val work = pending ?: return null
        pending = null
        job?.cancel()
        job = null
        return work
    }
}

private data class SortSnapshot(val order: List<String>, val settings: SortSettings)

private class SortPageState(ids: List<String>, initialSettings: SortSettings) {
    val order = mutableStateOf(ids)
    val settings = mutableStateOf(initialSettings)
    val drag = SortDragState()

    fun snapshot() = SortSnapshot(
        if (drag.settling) drag.move(order.value) ?: order.value else order.value,
        settings.value,
    )

    fun finishDrag() {
        if (drag.settling) drag.finish(order) else drag.clear()
    }
}

/**
 * Shared by every card in one sortable page. The backing list is not reordered while a drag is
 * in progress; only these values change, and one final move is committed after the drop.
 */
private class SortDragState {
    var draggedId by mutableStateOf<String?>(null)
    var startIndex by mutableIntStateOf(-1)
    var targetIndex by mutableIntStateOf(-1)
    var startY by mutableFloatStateOf(0f)
    var pointerY by mutableFloatStateOf(0f)
    var scrollY by mutableFloatStateOf(0f)
    var stride by mutableFloatStateOf(0f)
    var settling by mutableStateOf(false)
        private set
    var settleFraction by mutableFloatStateOf(0f)

    val active: Boolean get() = draggedId != null
    val distance: Float get() = pointerY - startY + scrollY

    fun begin(id: String, index: Int, y: Float, stride: Float) {
        draggedId = id
        startIndex = index
        targetIndex = index
        startY = y
        pointerY = y
        scrollY = 0f
        this.stride = stride
        settling = false
    }

    // A small hysteresis band around each midpoint stops a finger resting on a threshold from
    // swapping the same two slots back and forth.
    fun updateTarget(lastIndex: Int) {
        if (!active || stride <= 0f || lastIndex < 0) return
        val slot = startIndex + distance / stride
        val wanted = kotlin.math.round(slot).toInt().coerceIn(0, lastIndex)
        if (wanted == targetIndex) return
        val crossed = if (wanted > targetIndex) slot >= targetIndex + .58f
            else slot <= targetIndex - .58f
        if (crossed) targetIndex = wanted
    }

    fun end() { if (active) settling = true }

    fun clear() {
        draggedId = null
        startIndex = -1
        targetIndex = -1
        startY = 0f
        pointerY = 0f
        scrollY = 0f
        settling = false
        settleFraction = 0f
    }

    fun move(list: List<String>): List<String>? {
        if (list.getOrNull(startIndex) != draggedId || targetIndex !in list.indices || startIndex == targetIndex) return null
        return list.toMutableList().apply { add(targetIndex, removeAt(startIndex)) }
    }

    fun finish(order: MutableState<List<String>>) {
        move(order.value)?.let { order.value = it }
        clear()
    }
}

@Composable private fun SortCard(
    title: String, amount: String, id: String, order: MutableState<List<String>>, scrollState: ScrollState,
    viewportTop: Float, viewportHeight: Float, drag: SortDragState,
    onPersist: (List<String>) -> Unit,
) {
    val color = MiuixTheme.colorScheme.onSurfaceVariantActions
    val density = androidx.compose.ui.platform.LocalDensity.current
    val cornerRadius = CardDefaults.CornerRadius
    val cornerRadiusPx = with(density) { cornerRadius.toPx() }
    val squircleEnabled = isSquircleEnabled()
    val cardShape = remember(cornerRadiusPx, squircleEnabled) {
        GenericShape { size, _ ->
            addSquircleRect(size.width, size.height, cornerRadiusPx, squircleEnabled = squircleEnabled)
        }
    }
    val haptics = LocalHapticFeedback.current
    var rowHeight by remember { mutableFloatStateOf(64f) }
    var handleCoordinates by remember { mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(null) }
    val index = order.value.indexOf(id)
    val lastIndex = order.value.lastIndex
    val stride = rowHeight + with(density) { 12.dp.toPx() }
    val isDragged = drag.draggedId == id
    val dragActive = isDragged && drag.active
    val currentOnPersist by rememberUpdatedState(onPersist)
    // Committing a drop replaces the order state instance. pointerInput keeps its original
    // coroutine, so read the latest instance instead of the one captured at first composition.
    val currentOrder by rememberUpdatedState(order)

    // Picking a card up gets the heavier tick; every slot it crosses gets a light one.
    LaunchedEffect(dragActive, drag.targetIndex) {
        if (!dragActive) return@LaunchedEffect
        haptics.performHapticFeedback(
            if (drag.targetIndex == drag.startIndex) HapticFeedbackType.LongPress
            else HapticFeedbackType.TextHandleMove
        )
    }

    // Other cards slide one slot while the list itself keeps its current order.
    val shiftTarget = when {
        !drag.active || dragActive -> 0f
        index > drag.startIndex && index <= drag.targetIndex -> -stride
        index >= drag.targetIndex && index < drag.startIndex -> stride
        else -> 0f
    }
    val animatedShift by animateFloatAsState(shiftTarget,
        animationSpec = if (drag.active) tween(140) else snap(), label = "sortShift")

    LaunchedEffect(dragActive, drag.settling) {
        if (dragActive && drag.settling) {
            animate(0f, 1f, animationSpec = tween(170)) { fraction, _ ->
                drag.settleFraction = fraction
            }
            // Rebase all cards onto the new layout together, on actual animation completion.
            if (drag.draggedId == id && drag.settling) drag.finish(currentOrder)
        }
    }
    val lift by animateFloatAsState(if (dragActive && !drag.settling) 1f else 0f,
        animationSpec = tween(160), label = "sortLift")

    val reorder: (Int) -> Boolean = { step ->
        val list = currentOrder.value
        val from = list.indexOf(id)
        val to = from + step
        if (drag.active || from !in list.indices || to !in list.indices) false else {
            val next = list.toMutableList().apply { add(to, removeAt(from)) }
            currentOrder.value = next
            currentOnPersist(next)
            true
        }
    }
    val reorderTo: (Int) -> Boolean = { target ->
        val list = currentOrder.value
        val from = list.indexOf(id)
        if (drag.active || from !in list.indices) false else {
            val to = target.coerceIn(0, list.lastIndex)
            if (to == from) false else {
                val next = list.toMutableList().apply { add(to, removeAt(from)) }
                currentOrder.value = next
                currentOnPersist(next)
                true
            }
        }
    }
    val edge = with(density) { 64.dp.toPx() }
    val direction = if (dragActive && !drag.settling && viewportHeight > 0f) when {
        drag.pointerY < viewportTop + edge -> -1
        drag.pointerY > viewportTop + viewportHeight - edge -> 1
        else -> 0
    } else 0
    // The deeper the finger sits inside the edge band, the faster the list scrolls: dragging a
    // long list a screen or two would otherwise feel stuck at one speed.
    LaunchedEffect(direction, scrollState) {
        while (direction != 0 && drag.draggedId == id && !drag.settling) {
            val depth = if (direction < 0)
                ((viewportTop + edge - drag.pointerY) / edge).coerceIn(0f, 1f)
            else
                ((drag.pointerY - (viewportTop + viewportHeight - edge)) / edge).coerceIn(0f, 1f)
            val step = with(density) { (10f + 18f * depth).dp.toPx() }
            val scrolled = scrollState.scrollBy(direction * step)
            if (scrolled == 0f) break
            drag.scrollY += scrolled
            drag.updateTarget(currentOrder.value.lastIndex)
            delay(16)
        }
    }

    Card(Modifier.fillMaxWidth().zIndex(when {
        dragActive -> 2f
        shiftTarget != 0f -> 1f
        else -> 0f
    })
        .onSizeChanged { rowHeight = it.height.toFloat() }
        .graphicsLayer {
            translationY = when {
                drag.draggedId == id -> {
                    val dragStride = drag.stride
                    val from = drag.distance.coerceIn(
                        -drag.startIndex * dragStride, (lastIndex - drag.startIndex) * dragStride,
                    )
                    val to = (drag.targetIndex - drag.startIndex) * dragStride
                    from + (to - from) * drag.settleFraction
                }
                drag.active -> animatedShift
                else -> 0f
            }
            scaleX = 1f + .015f * lift
            scaleY = 1f + .015f * lift
            shape = cardShape
            shadowElevation = with(density) { 8.dp.toPx() } * lift
        }, cornerRadius = cornerRadius) {
        Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text(title, modifier = Modifier.weight(1f), maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                style = MiuixTheme.textStyles.body1)
            Spacer(Modifier.width(8.dp))
            Text("¥ $amount", modifier = Modifier.widthIn(max = 132.dp), style = MiuixTheme.textStyles.footnote1, maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            Box(Modifier.size(48.dp).onGloballyPositioned { handleCoordinates = it }
                .semantics {
                    contentDescription = tr("sort.drag_handle", title)
                    stateDescription = tr("sort.position_of", index.coerceAtLeast(0) + 1, order.value.size)
                    customActions = listOf(
                        CustomAccessibilityAction(tr("common.move_up")) { reorder(-1) },
                        CustomAccessibilityAction(tr("common.move_down")) { reorder(1) },
                        CustomAccessibilityAction(tr("sort.move_top")) { reorderTo(0) },
                        CustomAccessibilityAction(tr("sort.move_bottom")) { reorderTo(order.value.lastIndex) },
                    )
                }
                .pointerInput(id) {
                    detectDragGestures(
                        onDragStart = { local ->
                            if (drag.settling) drag.finish(currentOrder)
                            val from = currentOrder.value.indexOf(id)
                            if (from >= 0) {
                                val start = handleCoordinates?.takeIf { it.isAttached }
                                    ?.localToWindow(local)?.y ?: 0f
                                drag.begin(id, from, start, rowHeight + with(density) { 12.dp.toPx() })
                            }
                        },
                        onDragEnd = {
                            if (drag.draggedId == id) {
                                drag.move(currentOrder.value)?.let { currentOnPersist(it) }
                                drag.end()
                            }
                        },
                        onDragCancel = {
                            if (drag.draggedId == id) drag.clear()
                        },
                    ) { change, dragAmount ->
                        change.consume()
                        // The handle sits inside the translated card, so read the real pointer
                        // position in window coordinates instead of accumulating moved layout.
                        val windowY = handleCoordinates?.takeIf { it.isAttached }
                            ?.localToWindow(change.position)?.y ?: (drag.pointerY + dragAmount.y)
                        drag.pointerY = windowY
                        drag.updateTarget(currentOrder.value.lastIndex)
                    }
                }, contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(22.dp)) {
                    val stroke = 1.8.dp.toPx()
                    listOf(.28f, .5f, .72f).forEach { fraction ->
                        val y = size.height * fraction
                        drawLine(color, Offset(size.width * .17f, y), Offset(size.width * .83f, y), stroke,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    }
                }
            }
        }
    }
}

@Composable private fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth().animateContentSize(),
        insideMargin = PaddingValues(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}
@Composable private fun Note(text: String) {
    Text(text, style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
}
/**
 * Status chip and icon block colours, following Miuix's colour roles: [tertiaryContainer] is the
 * spec's light accent wash and [onTertiaryContainer] its accent-coloured text (light #EAF2FF on
 * #3482FF, dark #2B3B54 on #4788FF) — a soft tint with accent content, not a heavy filled block.
 */

@Composable private fun statusChipBackground(): Color =
    if (LocalStockPalette.current) MiuixTheme.colorScheme.tertiaryContainer
    else MiuixTheme.colorScheme.primaryContainer

@Composable private fun statusChipContent(): Color =
    if (LocalStockPalette.current) MiuixTheme.colorScheme.onTertiaryContainer
    else MiuixTheme.colorScheme.onPrimaryContainer

/** Small status pill. Miuix has no chip component, so keep it on the theme container roles. */
@Composable private fun StatusPill(text: String) {
    Box(Modifier.squircleSurface(color = statusChipBackground(), cornerRadius = 8.dp)
        .padding(horizontal = 9.dp, vertical = 5.dp)) {
        Text(text, style = MiuixTheme.textStyles.footnote2,
            color = statusChipContent(), fontWeight = FontWeight.Medium)
    }
}
@Composable private fun IconTile(kind: String) {
    Box(Modifier.size(44.dp).squircleSurface(color = statusChipBackground(), cornerRadius = 15.dp),
        contentAlignment = Alignment.Center) {
        Symbol(kind, statusChipContent(), Modifier.size(24.dp))
    }
}
@Composable private fun EmptyCard(kind: String, title: String, body: String, action: String, onClick: () -> Unit) {
    Panel {
        Column(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            IconTile(kind)
            Text(title, style = MiuixTheme.textStyles.title3, fontWeight = FontWeight.Bold)
            Text(body, style = MiuixTheme.textStyles.body2, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            Button(onClick = onClick, colors = primaryButtonColors()) { Text(action) }
        }
    }
}
@Composable private fun HelpStep(number: String, title: String, body: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatusPill(number)
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, style = MiuixTheme.textStyles.body1, fontWeight = FontWeight.Medium)
            Note(body)
        }
    }
}
private fun timeLabel(millis: Long): String = SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(Date(millis))

/** Achievement day shown on a vault card, written the way the current language writes dates. */
private fun achievedDate(millis: Long): String {
    val pack = Languages.current.pack
    val pattern = when (pack) {
        "zh-CN", "zh-TW" -> "yyyy年MM月dd日"
        "ja" -> "yyyy年M月d日"
        else -> "MMM d, yyyy"
    }
    val locale = if (pack.isEmpty()) Locale.getDefault() else Locale.forLanguageTag(pack)
    return SimpleDateFormat(pattern, locale).format(Date(millis))
}
