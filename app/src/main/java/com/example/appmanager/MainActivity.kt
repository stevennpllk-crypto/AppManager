package com.example.appmanager

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.StatFs
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appmanager.ui.theme.AppManagerTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppManagerTheme { AppManagerApp() } }
    }
}

data class AppEntry(
    val label: String, val packageName: String, val version: String,
    val sizeBytes: Long, val isSystem: Boolean, val icon: android.graphics.drawable.Drawable?, val launchable: Boolean
)
data class DeviceSnapshot(val usedRam: Long, val totalRam: Long, val storageUsed: Long, val storageTotal: Long, val battery: Int, val temperature: Float)

data class PermissionState(val name: String, val granted: Boolean, val explanation: String)

class AppManagerViewModel : ViewModel() {
    var apps by mutableStateOf<List<AppEntry>>(emptyList()); private set
    var snapshot by mutableStateOf(DeviceSnapshot(0, 0, 0, 0, -1, -1f)); private set
    var query by mutableStateOf("")
    var showSystem by mutableStateOf(true)
    var message by mutableStateOf<String?>(null)

    fun refresh(context: Context) {
        val pm = context.packageManager
        @Suppress("DEPRECATION")
        val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        apps = installed.mapNotNull { info ->
            val pkg = info.packageName
            val pi = try { pm.getPackageInfo(pkg, 0) } catch (_: Exception) { return@mapNotNull null }
            AppEntry(pm.getApplicationLabel(info).toString(), pkg, pi.versionName ?: "—", appSize(pm, pkg), info.flags and ApplicationInfo.FLAG_SYSTEM != 0, info.loadIcon(pm), pm.getLaunchIntentForPackage(pkg) != null)
        }.sortedBy { it.label.lowercase(Locale.getDefault()) }
        updateSnapshot(context)
    }

    private fun appSize(pm: PackageManager, pkg: String): Long = try { pm.getApplicationInfo(pkg, 0).sourceDir.let { java.io.File(it).length() } } catch (_: Exception) { 0 }

    fun updateSnapshot(context: Context) {
        val am = context.getSystemService(ActivityManager::class.java)
        val mi = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        val stat = StatFs(context.filesDir.absolutePath)
        val total = stat.totalBytes; val free = stat.availableBytes
        val bm = context.getSystemService(BatteryManager::class.java)
        val battery = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val intent = context.registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val temp = (intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1) / 10f
        snapshot = DeviceSnapshot(mi.totalMem - mi.availMem, mi.totalMem, total - free, total, battery, temp)
    }

    fun safeClean(context: Context) {
        // Official API only: asks this process to trim its own caches. Android does not allow
        // third-party apps to force-stop or clean RAM belonging to other applications.
        context.getSystemService(ActivityManager::class.java).trimMemory(ActivityManager.TRIM_MEMORY_RUNNING_LOW)
        message = context.getString(R.string.clean_result)
        updateSnapshot(context)
    }

    fun filtered(): List<AppEntry> = apps.filter { (showSystem || !it.isSystem) && (query.isBlank() || it.label.contains(query, true) || it.packageName.contains(query, true)) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppManagerApp(vm: AppManagerViewModel = viewModel()) {
    val context = LocalContext.current
    var tab by remember { mutableStateOf(0) }
    var showCleanInfo by remember { mutableStateOf(false) }
    val snack = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { vm.refresh(context) }
    LaunchedEffect(vm.message) { vm.message?.let { snack.showSnackbar(it); vm.message = null } }
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }, actions = { IconButton({ context.startActivity(Intent(Settings.ACTION_SETTINGS)) }) { Icon(Icons.Default.Settings, null) } }) }, snackbarHost = { SnackbarHost(snack) }, bottomBar = {
        NavigationBar { listOf(R.string.dashboard to Icons.Default.Dashboard, R.string.apps to Icons.Default.Apps, R.string.permissions to Icons.Default.Security).forEachIndexed { i, item -> NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = { Icon(item.second, null) }, label = { Text(stringResource(item.first)) }) } }
    }) { pad ->
        Surface(Modifier.fillMaxSize().padding(pad)) {
            when (tab) { 0 -> Dashboard(vm, { showCleanInfo = true }); 1 -> AppsScreen(vm); else -> PermissionsScreen() }
        }
    }
    if (showCleanInfo) AlertDialog(onDismissRequest = { showCleanInfo = false }, title = { Text(stringResource(R.string.clean_title)) }, text = { Text(stringResource(R.string.clean_explanation)) }, confirmButton = { TextButton({ showCleanInfo = false; vm.safeClean(context) }) { Text(stringResource(R.string.continue_action)) } }, dismissButton = { TextButton({ showCleanInfo = false }) { Text(stringResource(R.string.cancel)) } })
}

@Composable fun Dashboard(vm: AppManagerViewModel, onClean: () -> Unit) {
    val s = vm.snapshot; val context = LocalContext.current
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text(stringResource(R.string.dashboard), style = MaterialTheme.typography.headlineMedium); Text(stringResource(R.string.official_only), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { MetricCard(stringResource(R.string.ram), formatBytes(s.usedRam), formatBytes(s.totalRam), Icons.Default.Memory) }
        item { MetricCard(stringResource(R.string.storage), formatBytes(s.storageUsed), formatBytes(s.storageTotal), Icons.Default.Info) }
        item { MetricCard(stringResource(R.string.battery), if (s.battery >= 0) "${s.battery}%" else "—", if (s.temperature >= 0) "${s.temperature}°C" else stringResource(R.string.unavailable), Icons.Default.BatteryFull) }
        item { MetricCard(stringResource(R.string.cpu), stringResource(R.string.cpu_unavailable), stringResource(R.string.api_limit), Icons.Default.Android) }
        item { Card { Column(Modifier.padding(16.dp)) { Text(stringResource(R.string.device_info), style = MaterialTheme.typography.titleLarge); Spacer(Modifier.height(8.dp)); Text("${Build.MANUFACTURER} ${Build.MODEL}"); Text("Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"); Text(stringResource(R.string.installed_count, vm.apps.size)); Spacer(Modifier.height(10.dp)); TextButton(onClick = { vm.updateSnapshot(context) }) { Text(stringResource(R.string.refresh)) } } } }
        item { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CleaningServices, null); Column(Modifier.padding(start = 12.dp).weight(1f)) { Text(stringResource(R.string.safe_memory)); Text(stringResource(R.string.safe_memory_desc)) }; TextButton(onClick = onClean) { Text(stringResource(R.string.run)) } } } }
    }
}

@Composable fun MetricCard(title: String, main: String, detail: String, icon: androidx.compose.ui.graphics.vector.ImageVector) { Card { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary); Column(Modifier.padding(start = 14.dp)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(main, style = MaterialTheme.typography.headlineSmall); Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant) } } } }

@Composable fun AppsScreen(vm: AppManagerViewModel) { val context = LocalContext.current; Column(Modifier.fillMaxSize().padding(16.dp)) { Text(stringResource(R.string.apps), style = MaterialTheme.typography.headlineMedium); OutlinedTextField(vm.query, { vm.query = it }, Modifier.fillMaxWidth().padding(vertical = 10.dp), label = { Text(stringResource(R.string.search)) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)); Row(verticalAlignment = Alignment.CenterVertically) { FilterChip(vm.showSystem, { vm.showSystem = !vm.showSystem }, label = { Text(stringResource(R.string.show_system)) }); Text(stringResource(R.string.count, vm.filtered().size), Modifier.padding(start = 12.dp)) }; LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(vm.filtered(), key = { it.packageName }) { app -> AppRow(app, context) } } } }

@Composable fun AppRow(app: AppEntry, context: Context) { Card { Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) { app.icon?.let { Image(it.toBitmap().asImageBitmap(), null, Modifier.size(44.dp)) } ?: Icon(Icons.Default.Android, null, Modifier.size(44.dp)); Column(Modifier.padding(horizontal = 12.dp).weight(1f)) { Text(app.label, style = MaterialTheme.typography.titleMedium); Text(app.packageName, style = MaterialTheme.typography.bodySmall); Text("${app.version} • ${formatBytes(app.sizeBytes)}${if (app.isSystem) " • ${context.getString(R.string.system)}" else ""}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; if (app.launchable) TextButton({ context.packageManager.getLaunchIntentForPackage(app.packageName)?.let(context::startActivity) }) { Text(stringResource(R.string.open)) }; if (!app.isSystem) TextButton({ context.startActivity(Intent(Intent.ACTION_DELETE, Uri.parse("package:${app.packageName}"))) }) { Text(stringResource(R.string.uninstall)) }; IconButton({ context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${app.packageName}"))) }) { Icon(Icons.Default.Info, null) } } } }

@Composable fun PermissionsScreen() { Column(Modifier.fillMaxSize().padding(16.dp)) { Text(stringResource(R.string.permissions), style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.height(12.dp)); Card { Column(Modifier.padding(16.dp)) { Icon(Icons.Default.Security, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.height(8.dp)); Text(stringResource(R.string.no_permissions_title), style = MaterialTheme.typography.titleLarge); Text(stringResource(R.string.no_permissions_desc), Modifier.padding(top = 8.dp)); Divider(Modifier.padding(vertical = 12.dp)); Text(stringResource(R.string.permission_privacy_note), color = MaterialTheme.colorScheme.onSurfaceVariant) } } } }

fun formatBytes(value: Long): String { if (value <= 0) return "—"; val units = arrayOf("B", "KB", "MB", "GB", "TB"); var v = value.toDouble(); var i = 0; while (v >= 1024 && i < units.lastIndex) { v /= 1024; i++ }; return "%.1f %s".format(Locale.US, v, units[i]) }
