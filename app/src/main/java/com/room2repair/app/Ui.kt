@file:OptIn(ExperimentalMaterial3Api::class)

package com.room2repair.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.meta.wearable.dat.core.types.Permission
import com.meta.wearable.dat.core.types.PermissionStatus
import com.meta.wearable.dat.core.types.RegistrationState
import java.io.File
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ---------------------------------------------------------------- palette
// Calm, neutral palette: warm off-white, deep slate, terracotta accent, sage green.
private val Indigo = Color(0xFF34403D) // deep slate (primary)
private val Violet = Color(0xFF34403D)
private val Tangerine = Color(0xFFC2673F) // terracotta accent
private val Sun = Color(0xFFE2C994) // sand
private val Mint = Color(0xFF4E8C6E) // sage green
private val Good = Color(0xFF3D7A5D)
private val Warn = Color(0xFFB07A2A)
private val Bad = Color(0xFFB4483A)
private val Paper = Color(0xFFFFFEFB)
private val Bg = Color(0xFFF4F2EE)
private val Ink = Color(0xFF1F2422)
private val Muted = Color(0xFF6E736F)
private val HeaderBrush = Brush.linearGradient(listOf(Color(0xFF34403D), Color(0xFF3E4C48)))
private val CtaBrush = Brush.horizontalGradient(listOf(Color(0xFFC2673F), Color(0xFFB85C36)))

private fun priorityColor(p: String) = when (p) { "High" -> Bad; "Medium" -> Warn; else -> Good }

private fun priorityEmoji(p: String) = when (p) { "High" -> "🚨"; "Medium" -> "⚠️"; else -> "🧽" }

private fun tradeEmoji(t: String) =
    when (t) {
      "Painting/Drywall" -> "🎨"
      "Flooring" -> "🪵"
      "Plumbing" -> "🚰"
      "Electrical" -> "⚡"
      "Appliance" -> "🔌"
      "Carpentry" -> "🪚"
      "HVAC" -> "❄️"
      "Cleaning" -> "🧹"
      "Pest" -> "🐜"
      "Windows/Doors" -> "🚪"
      else -> "🔧"
    }

private fun whoPays(r: String) = when (r) { "Tenant" -> "🧑 Tenant pays"; "Owner" -> "🏢 Owner pays"; else -> "🤔 Needs review" }

private fun money(n: Int) = "$" + "%,d".format(n)

private val FACTORY_STEPS =
    listOf("📸  Photo captured", "🧩  Matching move-in photo", "🔍  Spotting new damage", "📝  Writing the ticket", "💲  Pricing · Atlanta, GA", "✋  Your approval")
private val MOVE_IN_STEPS = listOf("📸  Photo captured", "🏷️  Labelling the photo", "✅  Saved to move-in album")

// ================================================================= app shell
@Composable
fun Room2RepairApp(
    vm: InspectionViewModel,
    requestWearablesPermission: suspend (Permission) -> PermissionStatus,
    requestPhoneMic: suspend () -> Boolean,
    connectGlasses: () -> Unit,
    disconnectGlasses: () -> Unit,
) {
  MaterialTheme(colorScheme = lightColorScheme(primary = Indigo, secondary = Tangerine, background = Bg, surface = Color.White, secondaryContainer = Color(0xFFE3E6E1))) {
    val ui by vm.ui.collectAsState()
    val tickets by vm.store.tickets.collectAsState()
    val registered = ui.registration == RegistrationState.REGISTERED
    var tab by rememberSaveable { mutableStateOf(-1) }
    val current = if (tab >= 0) tab else if (registered && vm.store.apiKey.isNotBlank()) 0 else 2
    val open = tickets.count { it.status != "Done" }

    Box(Modifier.fillMaxSize()) {
      Scaffold(
          containerColor = Bg,
          topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Bg),
                title = {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🕶️", fontSize = 22.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("Room", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Ink)
                    Text("2", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Tangerine)
                    Text("Repair", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Indigo)
                  }
                },
            )
          },
          bottomBar = {
            NavigationBar(containerColor = Color.White) {
              NavigationBarItem(selected = current == 0, onClick = { tab = 0 },
                  icon = { Icon(Icons.Filled.Visibility, null) }, label = { Text("Inspect") })
              NavigationBarItem(selected = current == 1, onClick = { tab = 1 },
                  icon = { Icon(Icons.Filled.Build, null) }, label = { Text(if (open > 0) "Tickets ($open)" else "Tickets") })
              NavigationBarItem(selected = current == 2, onClick = { tab = 2 },
                  icon = { Icon(Icons.Filled.Settings, null) }, label = { Text("Setup") })
            }
          },
      ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
          when (current) {
            0 -> InspectScreen(vm, ui, requestWearablesPermission, requestPhoneMic)
            1 -> TicketsScreen(vm)
            else -> SetupScreen(vm, ui, connectGlasses, disconnectGlasses)
          }
        }
      }
      ui.celebrate?.let { msg -> Celebration(msg) { vm.dismissCelebration() } }
    }
  }
}

// ================================================================= Inspect
@Composable
private fun InspectScreen(
    vm: InspectionViewModel,
    ui: UiState,
    requestWearablesPermission: suspend (Permission) -> PermissionStatus,
    requestPhoneMic: suspend () -> Boolean,
) {
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  val link by vm.glasses.link.collectAsState()
  val cameraReady by vm.glasses.cameraReady.collectAsState()
  val baselines by vm.store.baselines.collectAsState()
  val album = baselines.filter { it.unit.equals(ui.unit, true) }.sortedByDescending { it.takenAt }
  val phoneCamera = rememberPhoneCamera(context) { bytes -> vm.onPhonePhoto(bytes) }
  val moveIn = ui.mode == Mode.MOVE_IN

  Column(
      Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    // ---------- header card
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(HeaderBrush).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
          Text("NOW INSPECTING", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
          Text(ui.unit, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
        }
        Text(if (moveIn) "🏠" else "🔍", fontSize = 40.sp)
      }
      Row(
          Modifier.fillMaxWidth().clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.18f)).padding(4.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        ModePill("🏠 Move-in", moveIn, Modifier.weight(1f)) { vm.setModeNow(Mode.MOVE_IN) }
        ModePill("🔍 Inspection", !moveIn, Modifier.weight(1f)) { vm.setModeNow(Mode.INSPECTION) }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        GlassPill("🕶️ Glasses", link == Glasses.Link.READY)
        GlassPill("📷 Camera", cameraReady)
        GlassPill(if (ui.listeningWith == "off") "🎙️ Off" else "🎙️ ${ui.listeningWith}", ui.listeningWith != "off")
      }
    }

    // ---------- start button
    if (!ui.handsFree) {
      Box(
          Modifier.fillMaxWidth().height(68.dp).clip(RoundedCornerShape(22.dp)).background(CtaBrush).clickable {
            scope.launch {
              val mic = requestPhoneMic()
              vm.startHandsFree(requestWearablesPermission, phoneMicAllowed = mic)
            }
          },
          contentAlignment = Alignment.Center,
      ) {
        Text("🚀  Start hands-free", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
      }
      if (ui.registration != RegistrationState.REGISTERED) {
        Text("👉 Connect your glasses on the Setup tab first.", color = Warn, fontWeight = FontWeight.SemiBold)
      }
    }

    // ---------- what to say
    SayCard(moveIn, ui.draft != null)

    // ---------- conversation bubbles
    if (ui.lastSaid.isNotBlank()) Bubble("🔊", ui.lastSaid, fromApp = true)
    if (ui.lastHeard.isNotBlank()) Bubble("🎙️", ui.lastHeard, fromApp = false)

    // ---------- the ticket factory (or move-in saving)
    if (ui.stage >= 0 || ui.error != null) {
      Factory(ui, moveIn)
    }

    // ---------- photos
    if (ui.lastAfter != null) {
      if (moveIn) {
        StampedPhoto(ui.lastAfter, "MOVE-IN", stamp = if (ui.outcome == "saved") "SAVED ✓" else null, stampColor = Good, caption = ui.lastRoom ?: "")
      } else {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Box(Modifier.weight(1f)) {
            StampedPhoto(ui.lastBefore, "BEFORE", stamp = null, stampColor = Good, caption = "Move-in", empty = "No matching\nmove-in photo")
          }
          Box(Modifier.weight(1f)) {
            StampedPhoto(
                ui.lastAfter, "AFTER",
                stamp = when (ui.outcome) { "damage" -> "NEW DAMAGE"; "clean" -> "ALL GOOD"; else -> null },
                stampColor = if (ui.outcome == "damage") Bad else Good,
                caption = "Now · ${ui.lastRoom ?: "…"}",
            )
          }
        }
      }
    }

    // ---------- drafted tickets
    ui.draft?.let { d ->
      Text("🧾 Draft ticket${if (d.issues.size > 1) "s" else ""} — needs your OK", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Ink)
      d.issues.forEachIndexed { i, issue -> TicketStub(issue, d.room, d.unit, number = null, appearDelay = i * 150) }
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(onClick = { vm.skipNow() }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(18.dp)) {
          Text("🗑️  Skip", fontSize = 16.sp)
        }
        Button(
            onClick = { vm.approveNow() },
            modifier = Modifier.weight(1.4f).height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Mint),
        ) {
          Text("✅  Approve", fontSize = 17.sp, fontWeight = FontWeight.Black)
        }
      }
      Text("…or just say \"approve\" to your glasses", color = Muted, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }

    if (ui.outcome == "clean" && ui.draft == null) {
      Text(
          "No new damage spotted. If you can see something, just describe it out loud (\"there's a crack in the tile\") and I'll write the ticket.",
          color = Muted, fontSize = 14.sp,
      )
    }

    // ---------- capture buttons
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      Button(
          onClick = { vm.captureNow() },
          enabled = cameraReady && ui.busy == null,
          modifier = Modifier.weight(1f).height(54.dp),
          shape = RoundedCornerShape(18.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Indigo),
      ) { Text(if (moveIn) "🕶️ Move-in photo" else "🕶️ Glasses photo", fontWeight = FontWeight.Bold) }
      OutlinedButton(onClick = phoneCamera, modifier = Modifier.weight(1f).height(54.dp), shape = RoundedCornerShape(18.dp)) {
        Text("📱 Phone photo", fontWeight = FontWeight.Bold)
      }
    }

    // ---------- move-in album
    Card {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text("🏠 Move-in album · ${album.size}", fontWeight = FontWeight.Black, color = Ink, modifier = Modifier.weight(1f))
        if (album.isNotEmpty()) TextButton(onClick = { vm.clearMoveIn() }) { Text("Clear", color = Bad) }
      }
      if (album.isEmpty()) {
        Text("Empty. Switch to 🏠 Move-in and say \"take a photo\" in each clean room.", color = Muted, fontSize = 13.sp)
      } else {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(album, key = { it.photo }) { b ->
            Column(Modifier.width(96.dp)) {
              Thumb(b.photo, Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp)))
              Text(b.room, fontSize = 11.sp, color = Muted, maxLines = 1)
            }
          }
        }
      }
    }

    // ---------- mic + stop
    if (ui.handsFree) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text("🎙️  Also listen with the phone mic", modifier = Modifier.weight(1f), color = Ink)
        Switch(checked = ui.listeningWith.contains("phone"), onCheckedChange = { on ->
          scope.launch { vm.togglePhoneMic(on && requestPhoneMic()) }
        })
      }
      TextButton(onClick = { vm.stopHandsFree() }) { Text("⏹  Stop hands-free") }
    }

    // ---------- activity
    if (ui.log.isNotEmpty()) {
      var showLog by remember { mutableStateOf(false) }
      TextButton(onClick = { showLog = !showLog }) { Text(if (showLog) "Hide activity" else "Show activity (${ui.log.size})") }
      if (showLog) ui.log.take(20).forEach { Text(it, fontSize = 12.sp, color = Muted) }
    }
    Spacer(Modifier.height(24.dp))
  }
}

@Composable
private fun ModePill(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
  Box(
      modifier
          .clip(RoundedCornerShape(50))
          .background(if (selected) Color.White else Color.Transparent)
          .clickable(onClick = onClick)
          .padding(vertical = 10.dp),
      contentAlignment = Alignment.Center,
  ) {
    Text(label, fontWeight = FontWeight.Black, color = if (selected) Indigo else Color.White)
  }
}

@Composable
private fun GlassPill(label: String, on: Boolean) {
  Row(
      Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = if (on) 0.25f else 0.1f)).padding(horizontal = 10.dp, vertical = 5.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(Modifier.size(8.dp).clip(CircleShape).background(if (on) Color(0xFF9FD8B9) else Color.White.copy(alpha = 0.35f)))
    Spacer(Modifier.width(6.dp))
    Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
  }
}

@Composable
private fun Card(content: @Composable () -> Unit) {
  Column(
      Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White).padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
  ) { content() }
}

@Composable
private fun SayCard(moveIn: Boolean, waitingApproval: Boolean) {
  val pulse = rememberInfiniteTransition(label = "say")
  val s by pulse.animateFloat(1f, 1.04f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "sayScale")
  val line =
      when {
        waitingApproval -> "\"approve\"  or  \"skip\""
        moveIn -> "\"take a photo\"  in each clean room"
        else -> "\"take a photo\"  of the damage"
      }
  Row(
      Modifier.fillMaxWidth().scale(s).clip(RoundedCornerShape(18.dp)).background(Color(0xFFF7EEDC)).border(1.5.dp, Sun, RoundedCornerShape(18.dp)).padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Text("🗣️", fontSize = 26.sp)
    Spacer(Modifier.width(10.dp))
    Column {
      Text("SAY TO YOUR GLASSES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9A6B24), letterSpacing = 1.2.sp)
      Text(line, fontSize = 17.sp, fontWeight = FontWeight.Black, color = Ink)
    }
  }
}

@Composable
private fun Bubble(icon: String, text: String, fromApp: Boolean) {
  Row(Modifier.fillMaxWidth(), horizontalArrangement = if (fromApp) Arrangement.Start else Arrangement.End) {
    Row(
        Modifier.fillMaxWidth(0.88f)
            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = if (fromApp) 4.dp else 18.dp, bottomEnd = if (fromApp) 18.dp else 4.dp))
            .background(if (fromApp) Color(0xFFE9EAE6) else Color(0xFFF5E6DC))
            .padding(12.dp),
    ) {
      Text(icon, fontSize = 16.sp)
      Spacer(Modifier.width(8.dp))
      Text(if (fromApp) text else "\"$text\"", color = Ink, fontSize = 14.sp)
    }
  }
}

// ---------------------------------------------------------------- ticket factory
@Composable
private fun Factory(ui: UiState, moveIn: Boolean) {
  val steps = if (moveIn) MOVE_IN_STEPS else FACTORY_STEPS
  val last = steps.size - 1
  val progress by animateFloatAsState(
      targetValue = if (ui.stage < 0) 0f else ((ui.stage + if (ui.stage >= last) 1 else 0).toFloat() / steps.size).coerceAtMost(1f),
      animationSpec = tween(500),
      label = "factory",
  )
  Card {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(if (moveIn) "🏠 Saving move-in photo" else "🏭 Ticket factory", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Ink, modifier = Modifier.weight(1f))
      Text("${(progress * 100).toInt()}%", fontWeight = FontWeight.Black, color = Indigo)
    }
    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(50)),
        color = if (ui.error != null) Bad else Mint,
        trackColor = Bg,
    )
    steps.forEachIndexed { i, label ->
      val done = ui.stage > i || (ui.stage == last && i == last && ui.outcome != null && ui.draft == null)
      val active = ui.stage == i && !done && ui.error == null
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Box(Modifier.size(26.dp), contentAlignment = Alignment.Center) {
          when {
            done -> Box(Modifier.size(24.dp).clip(CircleShape).background(Mint), contentAlignment = Alignment.Center) {
              Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            active -> CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 3.dp, color = Tangerine)
            else -> Box(Modifier.size(14.dp).clip(CircleShape).background(Color(0xFFDCDAD4)))
          }
        }
        Spacer(Modifier.width(10.dp))
        Text(
            label,
            fontSize = 15.sp,
            fontWeight = if (active) FontWeight.Black else FontWeight.Medium,
            color = when { done -> Ink; active -> Tangerine; else -> Muted },
        )
      }
    }
    ui.error?.let { Text("😬 $it", color = Bad, fontWeight = FontWeight.SemiBold) }
    if (!moveIn && ui.outcome == "clean") Text("✨ No new damage — no ticket needed.", color = Good, fontWeight = FontWeight.Bold)
  }
}

@Composable
private fun StampedPhoto(path: String?, label: String, stamp: String?, stampColor: Color, caption: String, empty: String = "No photo") {
  Column {
    Box(Modifier.fillMaxWidth().aspectRatio(4f / 3f).clip(RoundedCornerShape(16.dp)).background(Color(0xFFE7E4DD))) {
      if (path != null) Thumb(path, Modifier.fillMaxSize())
      else Text(empty, color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center))
      Text(
          label,
          color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp,
          modifier = Modifier.padding(8.dp).clip(RoundedCornerShape(50)).background(Ink.copy(alpha = 0.7f)).padding(horizontal = 8.dp, vertical = 3.dp),
      )
      if (stamp != null) {
        // Stamp "lands" on the photo: starts big and shrinks into place.
        var landed by remember(stamp) { mutableStateOf(false) }
        LaunchedEffect(stamp) { landed = true }
        val k by animateFloatAsState(if (landed) 1f else 2.2f, animationSpec = tween(350), label = "stamp")
        Text(
            stamp,
            color = stampColor, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.Center).scale(k).rotate(-12f).border(3.dp, stampColor, RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 4.dp),
        )
      }
    }
    if (caption.isNotBlank()) Text(caption, fontSize = 12.sp, color = Muted, modifier = Modifier.padding(top = 4.dp))
  }
}

/** A drafted or saved ticket, styled like a tear-off repair ticket. */
@Composable
private fun TicketStub(issue: Issue, room: String, unit: String, number: Long?, appearDelay: Int = 0) {
  var shown by remember { mutableStateOf(false) }
  LaunchedEffect(issue) {
    delay(appearDelay.toLong())
    shown = true
  }
  val pc = priorityColor(issue.priority)
  if (shown) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Paper).border(2.dp, pc.copy(alpha = 0.5f), RoundedCornerShape(20.dp))) {
      Row(Modifier.fillMaxWidth().background(pc).padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("${priorityEmoji(issue.priority)} ${issue.priority.uppercase()} PRIORITY", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp, letterSpacing = 1.sp, modifier = Modifier.weight(1f))
        Text(number?.let { "#$it" } ?: "DRAFT", color = Color.White, fontWeight = FontWeight.Black)
      }
      Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("${tradeEmoji(issue.trade)}  ${issue.title}", fontWeight = FontWeight.Black, fontSize = 19.sp, color = Ink)
        Text("📍 $unit · $room   ·   ${issue.trade}", color = Muted, fontSize = 13.sp)
        Text(issue.description, color = Ink, fontSize = 14.sp)
      }
      Canvas(Modifier.fillMaxWidth().height(2.dp).padding(horizontal = 12.dp)) {
        drawLine(Color(0xFFD8D3C8), Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)))
      }
      Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
          Text("ESTIMATE · ATLANTA, GA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Muted, letterSpacing = 1.sp)
          Text("${money(issue.costLow)} – ${money(issue.costHigh)}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Ink)
        }
        Column(horizontalAlignment = Alignment.End) {
          Text(whoPays(issue.responsibility), fontWeight = FontWeight.Bold, color = Ink, fontSize = 13.sp)
          Text("AI ${(issue.confidence * 100).toInt()}% sure", fontSize = 12.sp, color = Muted)
        }
      }
    }
  }
}

// ---------------------------------------------------------------- celebration
private class Piece(val x: Float, val delay: Float, val speed: Float, val wobble: Float, val spin: Float, val color: Color, val w: Float, val h: Float)

@Composable
private fun Celebration(message: String, onDone: () -> Unit) {
  val t = remember(message) { Animatable(0f) }
  val pieces = remember(message) {
    val colors = listOf(Tangerine, Sun, Mint, Indigo, Color(0xFF8FB3C9), Color(0xFFD9A38A))
    List(90) {
      Piece(Random.nextFloat(), Random.nextFloat() * 0.35f, 0.8f + Random.nextFloat() * 0.7f, 1f + Random.nextFloat() * 3f,
          Random.nextFloat() * 4f - 2f, colors[it % colors.size], 10f + Random.nextFloat() * 10f, 6f + Random.nextFloat() * 6f)
    }
  }
  LaunchedEffect(message) {
    t.animateTo(1f, tween(2800, easing = LinearEasing))
    onDone()
  }
  Box(Modifier.fillMaxSize().background(Ink.copy(alpha = 0.35f)).clickable { onDone() }, contentAlignment = Alignment.Center) {
    Canvas(Modifier.fillMaxSize()) {
      pieces.forEach { p ->
        val k = ((t.value - p.delay) / (1f - p.delay)).coerceIn(0f, 1f)
        val x = p.x * size.width + sin(k * 6.28f * p.wobble) * 40f
        val y = -40f + k * p.speed * (size.height + 80f)
        rotate(degrees = k * 720f * p.spin, pivot = Offset(x, y)) {
          drawRect(p.color, topLeft = Offset(x, y), size = Size(p.w, p.h))
        }
      }
    }
    Column(
        Modifier.padding(32.dp).clip(RoundedCornerShape(28.dp)).background(Color.White).padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text("🎉", fontSize = 56.sp)
      Text("Approved!", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Ink)
      Spacer(Modifier.height(6.dp))
      Text(message, fontSize = 16.sp, color = Muted, textAlign = TextAlign.Center)
      Spacer(Modifier.height(6.dp))
      Text("🛠️ Added to the Tickets tab", fontSize = 14.sp, color = Good, fontWeight = FontWeight.Bold)
    }
  }
}

// ---------------------------------------------------------------- photos
@Composable
private fun Thumb(path: String, modifier: Modifier) {
  val bmp = remember(path) { loadThumb(path) }
  if (bmp != null) Image(bmp.asImageBitmap(), null, contentScale = ContentScale.Crop, modifier = modifier)
  else Box(modifier.background(Color(0xFFE7E4DD)))
}

private fun loadThumb(path: String): Bitmap? {
  val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
  BitmapFactory.decodeFile(path, bounds)
  var sample = 1
  while (bounds.outWidth / sample > 900) sample *= 2
  return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
}

/** Phone-camera backup: returns a click handler that takes a photo and hands back a JPEG. */
@Composable
private fun rememberPhoneCamera(context: Context, onPhoto: (ByteArray) -> Unit): () -> Unit {
  val file = remember { File(File(context.cacheDir, "captures").apply { mkdirs() }, "phone.jpg") }
  val uri: Uri = remember { FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file) }
  val take =
      rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) uprightJpeg(file)?.let(onPhoto)
      }
  val askCamera =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) take.launch(uri) }
  return {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) take.launch(uri)
    else askCamera.launch(Manifest.permission.CAMERA)
  }
}

private fun uprightJpeg(file: File): ByteArray? {
  val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return null
  val orientation =
      runCatching { ExifInterface(file.absolutePath).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL) }
          .getOrDefault(ExifInterface.ORIENTATION_NORMAL)
  val degrees =
      when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
      }
  val upright =
      if (degrees == 0f) bitmap
      else Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(degrees) }, true)
  return Glasses.jpeg(upright)
}

// ================================================================= Tickets
@Composable
private fun TicketsScreen(vm: InspectionViewModel) {
  val tickets by vm.store.tickets.collectAsState()
  val context = LocalContext.current
  var filter by rememberSaveable { mutableStateOf("Open") }
  val shown = if (filter == "All") tickets else tickets.filter { it.status == filter }
  val open = tickets.filter { it.status != "Done" }
  val total = open.sumOf { (it.costLow + it.costHigh) / 2 }

  LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
    item {
      Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(HeaderBrush).padding(18.dp)) {
        Text("🛠️ Repair tickets", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(10.dp))
        Row {
          Stat("${open.size}", "open", Modifier.weight(1f))
          Stat(money(total), "est. repairs (GA)", Modifier.weight(1.4f))
          Stat("${tickets.count { it.status == "Done" }}", "done", Modifier.weight(1f))
        }
      }
    }
    item {
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        listOf("Open", "In progress", "Done", "All").forEach { f -> FilterChip(selected = filter == f, onClick = { filter = f }, label = { Text(f) }) }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = { share(context, "Room2Repair tickets", vm.store.csv()) }, enabled = tickets.isNotEmpty()) {
          Icon(Icons.Filled.Share, "Export CSV")
        }
      }
    }
    if (shown.isEmpty()) {
      item {
        Text(
            if (tickets.isEmpty()) "🕶️ No tickets yet.\nStart hands-free, say \"take a photo\" of some damage, then \"approve\"." else "Nothing here.",
            color = Muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(24.dp),
        )
      }
    }
    items(shown, key = { it.id }) { t -> SavedTicket(t, vm, context) }
  }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier) {
  Column(modifier) {
    Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
    Text(label, color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
  }
}

@Composable
private fun SavedTicket(t: Ticket, vm: InspectionViewModel, context: Context) {
  val issue = Issue(t.title, t.description, t.trade, t.priority, t.costLow, t.costHigh, t.responsibility, t.confidence)
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Box(Modifier.weight(1f)) { StampedPhoto(t.beforePhoto, "BEFORE", null, Good, "", empty = "No move-in photo") }
      Box(Modifier.weight(1f)) { StampedPhoto(t.afterPhoto.ifBlank { null }, "AFTER", null, Bad, "") }
    }
    TicketStub(issue, t.room, t.unit, number = t.id)
    if (t.note.isNotBlank()) Text("🎙️ Inspector said: \"${t.note}\"", fontSize = 13.sp, color = Muted)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
      listOf("Open" to "📬", "In progress" to "🔨", "Done" to "✅").forEach { (s, e) ->
        FilterChip(selected = t.status == s, onClick = { vm.store.setStatus(t.id, s) }, label = { Text("$e $s") })
      }
      Spacer(Modifier.weight(1f))
      IconButton(onClick = { share(context, t.title, t.shareText()) }) { Icon(Icons.Filled.Share, "Share") }
    }
  }
}

private fun share(context: Context, subject: String, text: String) {
  val send =
      Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
      }
  context.startActivity(Intent.createChooser(send, "Share"))
}

// ================================================================= Setup
@Composable
private fun SetupScreen(vm: InspectionViewModel, ui: UiState, connect: () -> Unit, disconnect: () -> Unit) {
  var providerId by rememberSaveable { mutableStateOf(vm.store.provider.id) }
  val provider = Provider.entries.first { it.id == providerId }
  var key by rememberSaveable { mutableStateOf(vm.store.apiKey) }
  var unit by rememberSaveable { mutableStateOf(vm.store.unit) }
  var model by rememberSaveable { mutableStateOf(vm.store.visionModel) }
  var saved by remember { mutableStateOf(false) }
  val registered = ui.registration == RegistrationState.REGISTERED

  Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text("Let's get you set up ✨", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Ink)
    Step(1, "Developer Mode in Meta AI", "Meta AI → Settings → App Info → tap the App version 5 times → turn on Developer Mode → Enable. Then restart Meta AI.", done = registered)

    Step(2, "Connect the glasses", if (registered) "Connected 🎉" else "Opens Meta AI so you can approve Room2Repair.", done = registered) {
      if (!registered) {
        Button(onClick = connect, enabled = ui.sdkReady, shape = RoundedCornerShape(14.dp)) {
          Text(if (ui.registration == RegistrationState.REGISTERING) "Waiting for Meta AI…" else "🕶️ Connect")
        }
      } else {
        TextButton(onClick = disconnect) { Text("Disconnect") }
      }
    }

    Step(3, "Free AI key", provider.howTo, done = key.isNotBlank()) {
      Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Provider.entries.forEach { p ->
          FilterChip(
              selected = p == provider,
              onClick = {
                // Remember what was typed for the old service, then load the new one's saved key/model.
                vm.store.setKeyFor(provider, key)
                vm.store.setModelFor(provider, model)
                providerId = p.id
                key = vm.store.keyFor(p)
                model = vm.store.modelFor(p)
                saved = false
              },
              label = { Text(p.label) },
          )
        }
      }
      OutlinedTextField(value = key, onValueChange = { key = it; saved = false }, label = { Text(provider.keyHint) }, singleLine = true, modifier = Modifier.fillMaxWidth())
      Text("Tip: save keys for 2+ services, or paste several keys separated by commas. When one hits its limit, the app switches to the next on its own.", fontSize = 12.sp, color = Muted)
    }

    Step(4, "Unit you're inspecting", "You can also say \"unit 12A\" during the walkthrough.", done = true) {
      OutlinedTextField(value = unit, onValueChange = { unit = it; saved = false }, label = { Text("Unit") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    }

    Box(
        Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(18.dp)).background(CtaBrush).clickable {
          vm.store.provider = provider
          vm.store.setKeyFor(provider, key)
          vm.store.setModelFor(provider, model)
          vm.setUnitNow(unit)
          saved = true
        },
        contentAlignment = Alignment.Center,
    ) { Text(if (saved) "Saved ✓" else "💾 Save", color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp) }

    Text("Advanced", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp), color = Ink)
    OutlinedTextField(value = model, onValueChange = { model = it; saved = false }, label = { Text("${provider.label} vision model (default: ${provider.defaultModel})") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    Text("Prices are approximate Atlanta, GA estimates. Tickets and photos stay on this phone. · Room2Repair v1.4", fontSize = 12.sp, color = Muted)
  }
}

@Composable
private fun Step(n: Int, title: String, body: String, done: Boolean, content: (@Composable () -> Unit)? = null) {
  Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White).padding(16.dp)) {
    Box(Modifier.size(32.dp).clip(CircleShape).background(if (done) Mint else Indigo), contentAlignment = Alignment.Center) {
      if (done) Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
      else Text("$n", color = Color.White, fontWeight = FontWeight.Black)
    }
    Spacer(Modifier.width(12.dp))
    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text(title, fontWeight = FontWeight.Black, color = Ink)
      Text(body, fontSize = 13.sp, color = Muted)
      content?.invoke()
    }
  }
}
