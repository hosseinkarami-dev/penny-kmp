package io.github.hosseinkarami_dev.penny

import android.Manifest
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        io.github.hosseinkarami_dev.penny.data.local.database.initializeDatabase(this)
        val createCsvDocument = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri -> completeCsvSave(uri) }
        initializeCsvFileShare(this) { fileName -> createCsvDocument.launch(fileName) }
        val createBackupDocument = registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri -> completeBackupSave(uri) }
        val openBackupDocument = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> completeBackupOpen(uri) }
        initializeBackupFileAccess(
            this,
            launchSave = { fileName -> createBackupDocument.launch(fileName) },
            launchOpen = { openBackupDocument.launch(arrayOf("*/*")) },
        )
        val deviceCredential = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> deliverDeviceAuthenticationResult(result.resultCode == RESULT_OK) }
        initializeDeviceAuthentication {
            val intent = createDeviceCredentialIntent(this)
            if (intent == null) deliverDeviceAuthenticationResult(false) else deviceCredential.launch(intent)
        }
        val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
        initializeLocalReminders(this) {
            if (android.os.Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            App()
        }
    }

}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
