package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.model.AppUpdateInfo
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.NavyDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AppUpdateDialog(
    updateInfo: AppUpdateInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = {
            if (!updateInfo.isMandatory) onDismiss()
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(GoldAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.RocketLaunch,
                        contentDescription = "Update",
                        tint = GoldDark,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = updateInfo.updateTitle.ifBlank { "Naya Update Available Hai!" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = NavyDark
                    )
                    Text(
                        text = "Version ${updateInfo.latestVersionName} (Build ${updateInfo.latestVersionCode})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoldDark
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Version badge box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NavyDark.copy(alpha = 0.05f))
                        .border(1.dp, NavyDark.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Your Current App",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyDark
                            )
                        }
                        Text(
                            text = "➔",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldDark
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "New Version",
                                fontSize = 11.sp,
                                color = Color(0xFF16A34A)
                            )
                            Text(
                                text = "v${updateInfo.latestVersionName}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF16A34A)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (updateInfo.updateMessage.isNotBlank()) {
                    Text(
                        text = "What's New in this Update:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(0.8.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = updateInfo.updateMessage,
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFF334155)
                        )
                    }
                } else {
                    Text(
                        text = "Naseeb Lal Market app me naye features aur improvements add kiye gaye hain. Kripya naya version update karein.",
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFF475569)
                    )
                }

                if (updateInfo.releasedDate.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Released: ${updateInfo.releasedDate}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                if (updateInfo.downloadUrl.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                var urlToCopy = updateInfo.downloadUrl.trim()
                                if (!urlToCopy.startsWith("http://", ignoreCase = true) && !urlToCopy.startsWith("https://", ignoreCase = true)) {
                                    urlToCopy = "https://$urlToCopy"
                                }
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                clipboard?.setPrimaryClip(ClipData.newPlainText("APK Link", urlToCopy))
                                Toast.makeText(context, "Download link copy ho gaya!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy Link",
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Copy Link",
                                fontSize = 11.5.sp
                            )
                        }

                        Button(
                            onClick = {
                                var urlToSend = updateInfo.downloadUrl.trim()
                                if (!urlToSend.startsWith("http://", ignoreCase = true) && !urlToSend.startsWith("https://", ignoreCase = true)) {
                                    urlToSend = "https://$urlToSend"
                                }
                                val shareText = "🏢 *Naseeb Lal Market App Update (v${updateInfo.latestVersionName})*\n\nNaya version download karne ke liye is link par tap karein:\n$urlToSend"
                                try {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share APK Update Link via"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Share karne me samasya: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25D366),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share",
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "WhatsApp Share",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rawUrl = updateInfo.downloadUrl.trim()
                    if (rawUrl.isNotBlank()) {
                        val formattedUrl = if (!rawUrl.startsWith("http://", ignoreCase = true) && !rawUrl.startsWith("https://", ignoreCase = true)) {
                            "https://$rawUrl"
                        } else {
                            rawUrl
                        }

                        // Always copy to clipboard as safe backup
                        try {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            clipboard?.setPrimaryClip(ClipData.newPlainText("APK Link", formattedUrl))
                        } catch (_: Exception) {}

                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                            onDismiss()
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Direct browser nahi khula, par Download Link copy ho chuka hai! Browser (Chrome) me paste karein.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(context, "Master Admin ne direct link set nahi kiya hai. Master Admin se APK maangein.", Toast.LENGTH_LONG).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyDark,
                    contentColor = GoldAccent
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Update Now",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            if (!updateInfo.isMandatory) {
                TextButton(onClick = onDismiss) {
                    Text("Baad me Karein", color = Color.Gray)
                }
            }
        }
    )
}

@Composable
fun BroadcastUpdateDialog(
    currentUpdateInfo: AppUpdateInfo?,
    onDismiss: () -> Unit,
    onPublish: (AppUpdateInfo) -> Unit
) {
    var newVersionCode by remember {
        mutableStateOf(((currentUpdateInfo?.latestVersionCode ?: BuildConfig.VERSION_CODE) + 1).toString())
    }
    var newVersionName by remember {
        mutableStateOf(
            if (currentUpdateInfo != null && currentUpdateInfo.latestVersionCode > BuildConfig.VERSION_CODE) {
                currentUpdateInfo.latestVersionName
            } else {
                "2.1"
            }
        )
    }
    var updateTitle by remember {
        mutableStateOf(currentUpdateInfo?.updateTitle ?: "Naye Features & Improvements Available!")
    }
    var updateMessage by remember {
        mutableStateOf(currentUpdateInfo?.updateMessage ?: "• Naye improvements aur calculations add kiye gaye hain.\n• Performance enhance ki gayi hai.")
    }
    var downloadUrl by remember {
        mutableStateOf(currentUpdateInfo?.downloadUrl ?: "")
    }
    var isMandatory by remember {
        mutableStateOf(currentUpdateInfo?.isMandatory ?: false)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.SystemUpdate,
                    contentDescription = null,
                    tint = NavyDark
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Broadcast App Update",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = NavyDark
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Yahan se aap sabhi Sub-Admins aur devices par naye version ka alert bhej sakte hain. Jab wo app kholenge unhe pop-up aayega.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newVersionCode,
                        onValueChange = { newVersionCode = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Version Code") },
                        placeholder = { Text("3") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = newVersionName,
                        onValueChange = { newVersionName = it },
                        label = { Text("Version Name") },
                        placeholder = { Text("2.1 ya 3.0") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = updateTitle,
                    onValueChange = { updateTitle = it },
                    label = { Text("Update Title / Heading") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = updateMessage,
                    onValueChange = { updateMessage = it },
                    label = { Text("What's New (Points)") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = downloadUrl,
                    onValueChange = { downloadUrl = it },
                    label = { Text("APK Download Link / Web Link") },
                    placeholder = { Text("https://... ya Google Drive link") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mandatory Update?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Agar ON hoga toh bina update kiye app aage nahi badhega",
                            fontSize = 10.5.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = isMandatory,
                        onCheckedChange = { isMandatory = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = GoldAccent, checkedTrackColor = NavyDark)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val code = newVersionCode.toIntOrNull() ?: (BuildConfig.VERSION_CODE + 1)
                    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                    var cleanUrl = downloadUrl.trim()
                    if (cleanUrl.isNotBlank() && !cleanUrl.startsWith("http://", ignoreCase = true) && !cleanUrl.startsWith("https://", ignoreCase = true)) {
                        cleanUrl = "https://$cleanUrl"
                    }
                    val info = AppUpdateInfo(
                        latestVersionCode = code,
                        latestVersionName = newVersionName.ifBlank { "2.1" },
                        updateTitle = updateTitle.ifBlank { "Naya Update Available Hai!" },
                        updateMessage = updateMessage,
                        downloadUrl = cleanUrl,
                        isMandatory = isMandatory,
                        releasedDate = dateStr
                    )
                    onPublish(info)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyDark,
                    contentColor = GoldAccent
                )
            ) {
                Text("Broadcast to All Devices 🚀", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = {
                        val resetInfo = AppUpdateInfo(
                            latestVersionCode = BuildConfig.VERSION_CODE,
                            latestVersionName = BuildConfig.VERSION_NAME,
                            updateTitle = "",
                            updateMessage = "",
                            downloadUrl = "",
                            isMandatory = false,
                            releasedDate = ""
                        )
                        onPublish(resetInfo)
                        onDismiss()
                    }
                ) {
                    Text("Clear Alert (Reset v2.0)", color = Color(0xFFDC2626), fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
