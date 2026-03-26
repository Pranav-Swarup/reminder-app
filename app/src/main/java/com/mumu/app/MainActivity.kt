package com.mumu.app

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

// ═══════════════════════════════ COLORS ═══════════════════════════════
// Dark
val DkBg = Color(0xFF121212); val DkCard = Color(0xFF1E1E1E); val DkSurface = Color(0xFF181818)
// Light
val LtBg = Color(0xFFF5F0EB); val LtCard = Color(0xFFFFFFFF); val LtSurface = Color(0xFFF0EBE5)

val Lavender = Color(0xFFCDB4DB); val Peach = Color(0xFFFFC8A2); val Mint = Color(0xFFBDE0C4)
val SoftPink = Color(0xFFFFAFCC); val SoftBlue = Color(0xFFA2D2FF); val SoftYellow = Color(0xFFFDFFB6)
val LavenderDim = Color(0xFF3D2E4A); val PeachDim = Color(0xFF4A3228); val MintDim = Color(0xFF2A3E30); val PinkDim = Color(0xFF4A2838)
val LavenderLt = Color(0xFFE8D5F0); val PeachLt = Color(0xFFFFE4CC); val MintLt = Color(0xFFD4F0DC); val PinkLt = Color(0xFFFFD6E8)
val OffWhite = Color(0xFFEDEDED); val MutedGray = Color(0xFF8A8A8A); val DimGray = Color(0xFF555555)
val UrgentRed = Color(0xFFFF6B6B); val DarkText = Color(0xFF2C2C2C)
val PastelColors = listOf(Lavender, Peach, Mint, SoftPink, SoftBlue, SoftYellow)
val DimColorsDk = listOf(LavenderDim, PeachDim, MintDim, PinkDim)
val DimColorsLt = listOf(LavenderLt, PeachLt, MintLt, PinkLt)

// ═══════════════════════════════ THEME STATE ═══════════════════════════════
object ThemeState {
    var isDark by mutableStateOf(true)
    val bg get() = if (isDark) DkBg else LtBg
    val card get() = if (isDark) DkCard else LtCard
    val surface get() = if (isDark) DkSurface else LtSurface
    val text get() = if (isDark) OffWhite else DarkText
    val subtext get() = MutedGray
    val dimColors get() = if (isDark) DimColorsDk else DimColorsLt
}

@Composable fun AppTheme(content: @Composable () -> Unit) {
    val cs = if (ThemeState.isDark) darkColorScheme(primary=Lavender,secondary=Peach,tertiary=Mint,background=DkBg,surface=DkSurface,onBackground=OffWhite,onSurface=OffWhite,error=UrgentRed)
    else lightColorScheme(primary=Lavender,secondary=Peach,tertiary=Mint,background=LtBg,surface=LtSurface,onBackground=DarkText,onSurface=DarkText,error=UrgentRed)
    MaterialTheme(colorScheme=cs, typography=Typography(
        displayLarge=TextStyle(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.SemiBold,fontSize=28.sp),
        headlineMedium=TextStyle(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.Medium,fontSize=22.sp),
        titleLarge=TextStyle(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.Medium,fontSize=18.sp),
        titleMedium=TextStyle(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.Medium,fontSize=15.sp),
        bodyLarge=TextStyle(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.Normal,fontSize=15.sp),
        bodyMedium=TextStyle(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.Normal,fontSize=13.sp),
        labelLarge=TextStyle(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.Medium,fontSize=13.sp),
        labelSmall=TextStyle(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.Normal,fontSize=11.sp)
    ), content=content)
}

// ═══════════════════════════════ DATA ═══════════════════════════════
enum class NotifMode { ALERT, PASSIVE }
data class TodayItem(val id:String=UUID.randomUUID().toString(),val title:String,val time:String?=null,val timeMillis:Long?=null,val mode:NotifMode=NotifMode.ALERT,val anonymous:Boolean=false,val completed:Boolean=false)
data class TodoItem(val id:String=UUID.randomUUID().toString(),val title:String,val description:String="",val completed:Boolean=false)
data class ReminderItem(val id:String=UUID.randomUUID().toString(),val title:String,val date:String="",val time:String?=null,val dateMillis:Long=0,val timeMillis:Long?=null,val mode:NotifMode=NotifMode.ALERT,val anonymous:Boolean=false,val completed:Boolean=false)
data class NoteItem(val id:String=UUID.randomUUID().toString(),val title:String,val content:String="",val color:Int=0)

// ═══════════════════════════════ PERSISTENCE ═══════════════════════════════
object Store {
    lateinit var prefs: SharedPreferences
    val gson = Gson()
    fun init(ctx:Context){prefs=ctx.getSharedPreferences("pebbleping",Context.MODE_PRIVATE)}
    inline fun <reified T> load(key:String):MutableList<T>{val j=prefs.getString(key,null)?:return mutableListOf();return try{gson.fromJson(j,object:TypeToken<MutableList<T>>(){}.type)}catch(e:Exception){mutableListOf()}}
    fun <T> save(key:String,list:List<T>){prefs.edit().putString(key,gson.toJson(list)).apply()}
    fun getString(key:String,def:String=""):String=prefs.getString(key,def)?:def
    fun putString(key:String,v:String){prefs.edit().putString(key,v).apply()}
    fun getBool(key:String,def:Boolean=false):Boolean=prefs.getBoolean(key,def)
    fun putBool(key:String,v:Boolean){prefs.edit().putBoolean(key,v).apply()}
}

// ═══════════════════════════════ NOTIFICATIONS ═══════════════════════════════
object Notifs {
    const val CH_ALERT="pp_alert";const val CH_PASSIVE="pp_passive"
    fun createChannels(ctx:Context){val nm=ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(NotificationChannel(CH_ALERT,"Alerts",NotificationManager.IMPORTANCE_HIGH).apply{enableVibration(true)})
        nm.createNotificationChannel(NotificationChannel(CH_PASSIVE,"Passive",NotificationManager.IMPORTANCE_LOW).apply{enableVibration(false);setSound(null,null)})}
    fun schedule(ctx:Context,id:String,title:String,triggerMs:Long,mode:NotifMode,anonymous:Boolean,targetTab:Int=0){
        val am=ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent=Intent(ctx,NotifReceiver::class.java).apply{putExtra("id",id);putExtra("title",title);putExtra("mode",mode.name);putExtra("anon",anonymous);putExtra("tab",targetTab)}
        val pi=PendingIntent.getBroadcast(ctx,abs(id.hashCode()),intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        try{am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,triggerMs,pi)}catch(_:SecurityException){am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,triggerMs,pi)}}
}
class NotifReceiver:BroadcastReceiver(){override fun onReceive(ctx:Context,intent:Intent){
    val id=intent.getStringExtra("id")?:return;val title=intent.getStringExtra("title")?:"Reminder"
    val mode=intent.getStringExtra("mode")?:"ALERT";val anon=intent.getBooleanExtra("anon",false);val tab=intent.getIntExtra("tab",0)
    val ch=if(mode=="PASSIVE")Notifs.CH_PASSIVE else Notifs.CH_ALERT;val dt=if(anon)"pebble-ping says hi!" else title;val db=if(anon)"check your reminders" else null
    val oi=PendingIntent.getActivity(ctx,tab,Intent(ctx,MainActivity::class.java).apply{flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP;putExtra("open_tab",tab)},PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    val b=NotificationCompat.Builder(ctx,ch).setSmallIcon(android.R.drawable.ic_popup_reminder).setContentTitle(dt).setPriority(if(mode=="PASSIVE")NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_HIGH).setContentIntent(oi).setAutoCancel(true)
    if(db!=null)b.setContentText(db);(ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(abs(id.hashCode()),b.build())}}

// ═══════════════════════════════ ACTIVITY ═══════════════════════════════
class MainActivity:FragmentActivity(){
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);Notifs.createChannels(this);Store.init(this)
        ThemeState.isDark=Store.getBool("dark_theme",true)
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU&&ContextCompat.checkSelfPermission(this,android.Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),1)
        val startTab=intent.getIntExtra("open_tab",0)
        setContent{AppTheme{Surface(Modifier.fillMaxSize(),color=ThemeState.bg){Root(startTab,this)}}}
    }
}

// ═══════════════════════════════ LOCK SCREEN ═══════════════════════════════
@Composable fun LockScreen(activity:FragmentActivity,onUnlocked:()->Unit){
    val lockType=Store.getString("lock_type","") // "password" or "biometric"
    val passHash=Store.getString("lock_pass","")
    var input by remember{mutableStateOf("")}
    var showForgot by remember{mutableStateOf(false)}
    var forgotAnswer by remember{mutableStateOf("")}
    var error by remember{mutableStateOf(false)}

    // Biometric auto-trigger
    if(lockType=="biometric"){
        LaunchedEffect(Unit){delay(300)
            val prompt=BiometricPrompt(activity,ContextCompat.getMainExecutor(activity),object:BiometricPrompt.AuthenticationCallback(){
                override fun onAuthenticationSucceeded(result:BiometricPrompt.AuthenticationResult){onUnlocked()}
                override fun onAuthenticationError(code:Int,msg:CharSequence){}
                override fun onAuthenticationFailed(){}
            })
            prompt.authenticate(BiometricPrompt.PromptInfo.Builder().setTitle("PebblePing").setSubtitle("Unlock with fingerprint").setNegativeButtonText("Cancel").build())
        }
    }

    Box(Modifier.fillMaxSize().background(ThemeState.bg),contentAlignment=Alignment.Center){
        Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.padding(40.dp)){
            Text("🌸",fontSize=64.sp)
            Spacer(Modifier.height(32.dp))
            if(lockType=="password"){
                if(!showForgot){
                    BasicTextField(value=input,onValueChange={input=it;error=false},singleLine=true,
                        visualTransformation=PasswordVisualTransformation(),
                        textStyle=MaterialTheme.typography.titleLarge.copy(color=ThemeState.text,textAlign=androidx.compose.ui.text.style.TextAlign.Center),
                        cursorBrush=SolidColor(Lavender),
                        modifier=Modifier.width(200.dp).clip(RoundedCornerShape(12.dp)).background(ThemeState.card).padding(16.dp,14.dp),
                        decorationBox={inner->Box(contentAlignment=Alignment.Center){if(input.isEmpty())Text("password",style=MaterialTheme.typography.titleLarge,color=DimGray);inner()}})
                    if(error)Text("wrong password",style=MaterialTheme.typography.bodyMedium,color=UrgentRed,modifier=Modifier.padding(top=8.dp))
                    Spacer(Modifier.height(16.dp))
                    Btn("unlock",input.isNotBlank(),Lavender){if(hash(input)==passHash)onUnlocked() else{error=true;input=""}}
                    Spacer(Modifier.height(24.dp))
                    Text("forgot password?",style=MaterialTheme.typography.labelSmall,color=DimGray.copy(0.5f),modifier=Modifier.clickable{showForgot=true})
                } else {
                    val q=Store.getString("lock_question","")
                    Text(q.ifBlank{"Security question not set"},style=MaterialTheme.typography.bodyMedium,color=ThemeState.subtext)
                    Spacer(Modifier.height(12.dp))
                    BasicTextField(value=forgotAnswer,onValueChange={forgotAnswer=it},singleLine=true,
                        textStyle=MaterialTheme.typography.bodyLarge.copy(color=ThemeState.text,textAlign=androidx.compose.ui.text.style.TextAlign.Center),
                        cursorBrush=SolidColor(Lavender),
                        modifier=Modifier.width(240.dp).clip(RoundedCornerShape(12.dp)).background(ThemeState.card).padding(16.dp,14.dp),
                        decorationBox={inner->Box(contentAlignment=Alignment.Center){if(forgotAnswer.isEmpty())Text("answer",style=MaterialTheme.typography.bodyLarge,color=DimGray);inner()}})
                    Spacer(Modifier.height(12.dp))
                    val ansHash=Store.getString("lock_answer","")
                    Btn("verify",forgotAnswer.isNotBlank(),Lavender){if(hash(forgotAnswer.lowercase().trim())==ansHash){Store.putString("lock_type","");Store.putString("lock_pass","");onUnlocked()} else{error=true}}
                    if(error)Text("wrong answer",style=MaterialTheme.typography.bodyMedium,color=UrgentRed,modifier=Modifier.padding(top=8.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("back",style=MaterialTheme.typography.labelSmall,color=DimGray,modifier=Modifier.clickable{showForgot=false;error=false})
                }
            } else {
                Text("unlock",style=MaterialTheme.typography.titleLarge,color=ThemeState.subtext)
                Spacer(Modifier.height(16.dp))
                Text("tap to retry",style=MaterialTheme.typography.bodyMedium,color=DimGray,modifier=Modifier.clickable{
                    val prompt=BiometricPrompt(activity,ContextCompat.getMainExecutor(activity),object:BiometricPrompt.AuthenticationCallback(){
                        override fun onAuthenticationSucceeded(result:BiometricPrompt.AuthenticationResult){onUnlocked()}})
                    prompt.authenticate(BiometricPrompt.PromptInfo.Builder().setTitle("PebblePing").setSubtitle("Unlock with fingerprint").setNegativeButtonText("Cancel").build())
                })
            }
        }
    }
}

fun hash(s:String):String{val b=java.security.MessageDigest.getInstance("SHA-256").digest(s.toByteArray());return b.joinToString(""){ "%02x".format(it)}}

// ═══════════════════════════════ ROOT (lock gate) ═══════════════════════════════
@Composable fun Root(startTab:Int,activity:FragmentActivity){
    val lockType=Store.getString("lock_type","")
    var unlocked by remember{mutableStateOf(lockType.isBlank())}
    if(!unlocked) LockScreen(activity){unlocked=true} else App(startTab)
}

// ═══════════════════════════════ APP ═══════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun App(startTab:Int=0){
    var tab by remember{mutableIntStateOf(startTab)};var showAdd by remember{mutableStateOf(false)};var showSettings by remember{mutableStateOf(false)}
    val ctx=LocalContext.current
    val todayItems=remember{mutableStateListOf<TodayItem>().also{it.addAll(Store.load("today"))}}
    val todoItems=remember{mutableStateListOf<TodoItem>().also{it.addAll(Store.load("todos"))}}
    val reminderItems=remember{mutableStateListOf<ReminderItem>().also{it.addAll(Store.load("reminders"))}}
    val noteItems=remember{mutableStateListOf<NoteItem>().also{it.addAll(Store.load("notes"))}}
    fun saveAll(){Store.save("today",todayItems.toList());Store.save("todos",todoItems.toList());Store.save("reminders",reminderItems.toList());Store.save("notes",noteItems.toList())}
    val snackbar=remember{SnackbarHostState()};val scope=rememberCoroutineScope();var editingNote by remember{mutableStateOf<NoteItem?>(null)}
    fun undo(msg:String,restore:()->Unit){scope.launch{val r=snackbar.showSnackbar(msg,"Undo",duration=SnackbarDuration.Short);if(r==SnackbarResult.ActionPerformed){restore();saveAll()}}}

    Scaffold(containerColor=ThemeState.bg,
        snackbarHost={SnackbarHost(snackbar){Snackbar(snackbarData=it,containerColor=ThemeState.card,contentColor=ThemeState.text,actionColor=Lavender,shape=RoundedCornerShape(14.dp))}},
        bottomBar={BBar(tab){tab=it}},
        floatingActionButton={FloatingActionButton(onClick={showAdd=true},shape=CircleShape,containerColor=listOf(UrgentRed,Peach,Lavender,SoftBlue)[tab],contentColor=if(ThemeState.isDark)DkBg else DarkText,modifier=Modifier.size(56.dp)){Icon(Icons.Rounded.Add,"Add",Modifier.size(28.dp))}}
    ){pad->
        Box(Modifier.fillMaxSize().padding(pad)){
            when(tab){
                0->TodayTab(todayItems,{t->val i=todayItems.indexOfFirst{it.id==t.id};if(i>=0)todayItems[i]=todayItems[i].copy(completed=!todayItems[i].completed);saveAll()},{t->todayItems.removeAll{it.id==t.id};saveAll();undo("Deleted"){todayItems.add(t)}})
                1->TodoTab(todoItems,{t->val i=todoItems.indexOfFirst{it.id==t.id};if(i>=0)todoItems[i]=todoItems[i].copy(completed=!todoItems[i].completed);saveAll()},{t->todoItems.removeAll{it.id==t.id};saveAll();undo("Deleted"){todoItems.add(t)}})
                2->ReminderTab(reminderItems,{t->val i=reminderItems.indexOfFirst{it.id==t.id};if(i>=0)reminderItems[i]=reminderItems[i].copy(completed=!reminderItems[i].completed);saveAll()},{t->reminderItems.removeAll{it.id==t.id};saveAll();undo("Deleted"){reminderItems.add(t)}})
                3->NotesTab(noteItems,{editingNote=it},{n->noteItems.removeAll{it.id==n.id};saveAll();undo("Note deleted"){noteItems.add(n)}})
            }
            IconButton(onClick={showSettings=true},modifier=Modifier.align(Alignment.TopEnd).padding(8.dp)){Icon(Icons.Rounded.Settings,"Settings",tint=ThemeState.subtext,modifier=Modifier.size(22.dp))}
        }
    }
    if(showAdd){when(tab){
        3->NoteEditorDialog(null,{noteItems.add(it);saveAll()},null){showAdd=false}
        else->AddDialog(tab,ctx,{when(tab){0->{todayItems.add(it as TodayItem);saveAll()};1->{todoItems.add(it as TodoItem);saveAll()};2->{reminderItems.add(it as ReminderItem);saveAll()}}; showAdd=false}){showAdd=false}
    }}
    if(editingNote!=null){val n=editingNote!!;NoteEditorDialog(n,{u->val i=noteItems.indexOfFirst{it.id==u.id};if(i>=0)noteItems[i]=u;saveAll()},{noteItems.removeAll{it.id==n.id};saveAll();editingNote=null;undo("Note deleted"){noteItems.add(n)}}){editingNote=null}}
    if(showSettings)SettingsDialog{showSettings=false}
}

// ═══════════════════════════════ SETTINGS ═══════════════════════════════
@Composable fun SettingsDialog(onDismiss:()->Unit){
    var lockType by remember{mutableStateOf(Store.getString("lock_type",""))}
    var showPassSetup by remember{mutableStateOf(false)}
    var newPass by remember{mutableStateOf("")};var newQ by remember{mutableStateOf("")};var newA by remember{mutableStateOf("")}

    Dialog(onDismissRequest=onDismiss,properties=DialogProperties(usePlatformDefaultWidth=false)){
        Box(Modifier.fillMaxSize().padding(16.dp),contentAlignment=Alignment.Center){
            Card(Modifier.fillMaxWidth().fillMaxHeight(0.75f),shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(containerColor=ThemeState.bg)){
                Column(Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())){
                    Row(verticalAlignment=Alignment.CenterVertically){Text("settings",style=MaterialTheme.typography.headlineMedium,color=ThemeState.text);Spacer(Modifier.weight(1f));IconButton(onClick=onDismiss){Icon(Icons.Rounded.Close,"Close",tint=ThemeState.subtext)}}
                    Spacer(Modifier.height(24.dp))

                    // Theme toggle
                    Text("APPEARANCE",style=MaterialTheme.typography.labelLarge,color=ThemeState.subtext)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(ThemeState.card).clickable{ThemeState.isDark=!ThemeState.isDark;Store.putBool("dark_theme",ThemeState.isDark)}.padding(16.dp),verticalAlignment=Alignment.CenterVertically){
                        Icon(if(ThemeState.isDark)Icons.Rounded.DarkMode else Icons.Rounded.LightMode,null,tint=Lavender,modifier=Modifier.size(22.dp));Spacer(Modifier.width(12.dp))
                        Text(if(ThemeState.isDark)"Dark mode" else "Light mode",style=MaterialTheme.typography.bodyLarge,color=ThemeState.text);Spacer(Modifier.weight(1f))
                        Text("tap to switch",style=MaterialTheme.typography.labelSmall,color=DimGray)
                    }

                    Spacer(Modifier.height(24.dp))
                    Text("SECURITY",style=MaterialTheme.typography.labelLarge,color=ThemeState.subtext)
                    Spacer(Modifier.height(8.dp))

                    if(!showPassSetup){
                        // Lock options
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(ThemeState.card).clickable{
                            if(lockType=="password"){lockType="";Store.putString("lock_type","");Store.putString("lock_pass","")} else showPassSetup=true
                        }.padding(16.dp),verticalAlignment=Alignment.CenterVertically){
                            Icon(Icons.Rounded.Lock,null,tint=Peach,modifier=Modifier.size(22.dp));Spacer(Modifier.width(12.dp))
                            Text("Password lock",style=MaterialTheme.typography.bodyLarge,color=ThemeState.text);Spacer(Modifier.weight(1f))
                            Text(if(lockType=="password")"ON" else "OFF",style=MaterialTheme.typography.labelLarge,color=if(lockType=="password")Mint else DimGray)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(ThemeState.card).clickable{
                            if(lockType=="biometric"){lockType="";Store.putString("lock_type","")} else{lockType="biometric";Store.putString("lock_type","biometric")}
                        }.padding(16.dp),verticalAlignment=Alignment.CenterVertically){
                            Icon(Icons.Rounded.Fingerprint,null,tint=Mint,modifier=Modifier.size(22.dp));Spacer(Modifier.width(12.dp))
                            Text("Biometric lock",style=MaterialTheme.typography.bodyLarge,color=ThemeState.text);Spacer(Modifier.weight(1f))
                            Text(if(lockType=="biometric")"ON" else "OFF",style=MaterialTheme.typography.labelLarge,color=if(lockType=="biometric")Mint else DimGray)
                        }
                    } else {
                        // Password setup
                        Text("Set password",style=MaterialTheme.typography.titleMedium,color=ThemeState.text)
                        Spacer(Modifier.height(8.dp))
                        Input(newPass,{newPass=it},"Password")
                        Spacer(Modifier.height(8.dp))
                        Input(newQ,{newQ=it},"Backup question (e.g. pet name?)")
                        Spacer(Modifier.height(8.dp))
                        Input(newA,{newA=it},"Answer")
                        Spacer(Modifier.height(12.dp))
                        Btn("Set lock",newPass.isNotBlank()&&newQ.isNotBlank()&&newA.isNotBlank(),Peach){
                            Store.putString("lock_type","password");Store.putString("lock_pass",hash(newPass));Store.putString("lock_question",newQ);Store.putString("lock_answer",hash(newA.lowercase().trim()))
                            lockType="password";showPassSetup=false
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("cancel",style=MaterialTheme.typography.labelSmall,color=DimGray,modifier=Modifier.clickable{showPassSetup=false}.padding(8.dp))
                    }

                    Spacer(Modifier.weight(1f))
                    Text("made by pranav :)",style=MaterialTheme.typography.labelSmall,color=DimGray.copy(0.4f),modifier=Modifier.align(Alignment.CenterHorizontally).padding(top=24.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════ BOTTOM BAR ═══════════════════════════════
@Composable fun BBar(tab:Int,onTab:(Int)->Unit){
    val items=listOf("today" to Icons.Rounded.WbSunny,"todos" to Icons.Rounded.CheckCircleOutline,"reminders" to Icons.Rounded.Notifications,"notes" to Icons.Rounded.StickyNote2)
    val tints=listOf(Peach,Mint,Lavender,SoftPink)
    NavigationBar(containerColor=ThemeState.surface,tonalElevation=0.dp){items.forEachIndexed{i,(l,ic)->val s=tab==i
        NavigationBarItem(selected=s,onClick={onTab(i)},icon={Icon(ic,l,tint=if(s)tints[i] else MutedGray,modifier=Modifier.size(24.dp))},label={Text(l,style=MaterialTheme.typography.labelSmall,color=if(s)tints[i] else MutedGray)},
            colors=NavigationBarItemDefaults.colors(indicatorColor=(if(s)tints[i] else MutedGray).copy(alpha=0.1f)))}}
}

// ═══════════════════════════════ TODAY TAB ═══════════════════════════════
@Composable fun TodayTab(items:List<TodayItem>,onToggle:(TodayItem)->Unit,onDelete:(TodayItem)->Unit){
    val day=remember{SimpleDateFormat("EEE, MMMM d",Locale.getDefault()).format(Date())}
    val timed=items.filter{it.time!=null&&!it.completed};val timeless=items.filter{it.time==null&&!it.completed};val done=items.filter{it.completed}
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(bottom=100.dp)){
        item{Text(day,style=MaterialTheme.typography.displayLarge,color=ThemeState.text,modifier=Modifier.padding(20.dp,16.dp))}
        if(timed.isNotEmpty()){item{Lbl("scheduled",UrgentRed)};items(timed,key={it.id}){item->
            val sub=buildString{append(item.time);if(item.mode==NotifMode.PASSIVE)append(" · passive");if(item.anonymous)append(" · hidden")}
            ItemCard(item.title,sub,UrgentRed,PinkDim,item.completed,{onToggle(item)},{onDelete(item)})}}
        if(timeless.isNotEmpty()){item{Lbl("pending",Peach)};items(timeless,key={it.id}){item->ItemCard(item.title,null,Peach,PeachDim,item.completed,{onToggle(item)},{onDelete(item)})}}
        if(done.isNotEmpty()){item{Lbl("done",Mint.copy(0.5f))};items(done,key={it.id}){item->ItemCard(item.title,item.time,Mint,MintDim,item.completed,{onToggle(item)},{onDelete(item)})}}
        if(items.isEmpty()){item{Column(Modifier.fillMaxWidth().padding(48.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("🌸",fontSize=40.sp);Spacer(Modifier.height(12.dp));Text("enjoy the calm",style=MaterialTheme.typography.bodyLarge,color=ThemeState.subtext)}}}
    }
}

// ═══════════════════════════════ TODO TAB ═══════════════════════════════
@Composable fun TodoTab(items:List<TodoItem>,onToggle:(TodoItem)->Unit,onDelete:(TodoItem)->Unit){
    val active=items.filter{!it.completed};val done=items.filter{it.completed}
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(bottom=100.dp)){
        item{Column(Modifier.padding(20.dp,16.dp)){Text("todos",style=MaterialTheme.typography.displayLarge,color=ThemeState.text);if(active.isNotEmpty())Text("${active.size} remaining",style=MaterialTheme.typography.bodyMedium,color=Peach)}}
        items(active,key={it.id}){item->ItemCard(item.title,item.description.ifBlank{null},Peach,PeachDim,false,{onToggle(item)},{onDelete(item)})}
        if(done.isNotEmpty()){item{Lbl("completed",Mint.copy(0.6f))};items(done,key={it.id}){item->ItemCard(item.title,null,Mint,MintDim,true,{onToggle(item)},{onDelete(item)})}}
    }
}

// ═══════════════════════════════ REMINDER TAB ═══════════════════════════════
@Composable fun ReminderTab(items:List<ReminderItem>,onToggle:(ReminderItem)->Unit,onDelete:(ReminderItem)->Unit){
    val up=items.filter{!it.completed}.sortedBy{it.dateMillis};val done=items.filter{it.completed}
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(bottom=100.dp)){
        item{Text("reminders",style=MaterialTheme.typography.displayLarge,color=ThemeState.text,modifier=Modifier.padding(20.dp,16.dp))}
        if(up.isNotEmpty()){items(up,key={it.id}){item->val sub=buildString{append(item.date);if(item.time!=null)append(" at ${item.time}");if(item.mode==NotifMode.PASSIVE)append(" · passive");if(item.anonymous)append(" · hidden")}
            ItemCard(item.title,sub,Lavender,LavenderDim,false,{onToggle(item)},{onDelete(item)})}}
        if(done.isNotEmpty()){item{Lbl("done",Mint.copy(0.5f))};items(done,key={it.id}){item->ItemCard(item.title,item.date,Mint,MintDim,true,{onToggle(item)},{onDelete(item)})}}
    }
}

// ═══════════════════════════════ NOTES TAB ═══════════════════════════════
@Composable fun NotesTab(notes:List<NoteItem>,onNoteClick:(NoteItem)->Unit,onDelete:(NoteItem)->Unit){
    Column(Modifier.fillMaxSize()){Text("notes",style=MaterialTheme.typography.displayLarge,color=ThemeState.text,modifier=Modifier.padding(20.dp,16.dp))
        if(notes.isEmpty())return
        LazyVerticalStaggeredGrid(columns=StaggeredGridCells.Fixed(2),contentPadding=PaddingValues(horizontal=12.dp,vertical=4.dp),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalItemSpacing=8.dp){
            items(notes,key={it.id}){note->val c=PastelColors.getOrElse(note.color){Lavender};val dc=ThemeState.dimColors.getOrElse(note.color%ThemeState.dimColors.size){LavenderDim}
                Card(Modifier.fillMaxWidth().clickable{onNoteClick(note)},colors=CardDefaults.cardColors(containerColor=dc.copy(0.6f)),shape=RoundedCornerShape(16.dp)){Column(Modifier.padding(16.dp)){
                    Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(8.dp).clip(CircleShape).background(c));Spacer(Modifier.width(10.dp));Text(note.title,style=MaterialTheme.typography.titleMedium,color=ThemeState.text,maxLines=1,overflow=TextOverflow.Ellipsis,modifier=Modifier.weight(1f))}
                    if(note.content.isNotBlank())Text(note.content,style=MaterialTheme.typography.bodyMedium,color=ThemeState.subtext,maxLines=4,overflow=TextOverflow.Ellipsis,modifier=Modifier.padding(top=8.dp))
                }}}
        }
    }
}

// ═══════════════════════════════ NOTE EDITOR ═══════════════════════════════
@Composable fun NoteEditorDialog(note:NoteItem?,onSave:(NoteItem)->Unit,onDelete:(()->Unit)?,onDismiss:()->Unit){
    val id=remember{note?.id?:UUID.randomUUID().toString()};var title by remember{mutableStateOf(note?.title?:"")};var content by remember{mutableStateOf(note?.content?:"")}
    var color by remember{mutableIntStateOf(note?.color?:0)};val focus=remember{FocusRequester()};LaunchedEffect(Unit){delay(200);focus.requestFocus()}
    fun doSave(){if(title.isBlank()&&content.isBlank()){if(note!=null)onDelete?.invoke()} else onSave(NoteItem(id=id,title=title.trim().ifBlank{"Untitled"},content=content.trim(),color=color))}
    Dialog(onDismissRequest={doSave();onDismiss()},properties=DialogProperties(usePlatformDefaultWidth=false)){
        Box(Modifier.fillMaxSize().padding(12.dp),contentAlignment=Alignment.Center){
            Card(Modifier.fillMaxWidth().fillMaxHeight(0.9f),shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(containerColor=ThemeState.bg)){Column(Modifier.fillMaxSize().padding(20.dp)){
                Row(verticalAlignment=Alignment.CenterVertically){IconButton(onClick={doSave();onDismiss()}){Icon(Icons.Rounded.ArrowBack,"Back",tint=ThemeState.subtext)};Spacer(Modifier.weight(1f))
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically){PastelColors.take(4).forEachIndexed{i,c->Box(Modifier.size(20.dp).clip(CircleShape).background(c.copy(if(color==i)1f else 0.3f)).clickable{color=i})}
                        if(onDelete!=null){Spacer(Modifier.width(8.dp));IconButton(onClick={onDelete()},modifier=Modifier.size(32.dp)){Icon(Icons.Rounded.Delete,"Delete",tint=UrgentRed.copy(0.7f),modifier=Modifier.size(20.dp))}}}}
                Spacer(Modifier.height(16.dp))
                BasicTextField(title,{title=it},singleLine=true,textStyle=MaterialTheme.typography.headlineMedium.copy(color=ThemeState.text,fontWeight=FontWeight.SemiBold),cursorBrush=SolidColor(PastelColors.getOrElse(color){Lavender}),modifier=Modifier.fillMaxWidth().focusRequester(focus),
                    decorationBox={inner->Box{if(title.isEmpty())Text("Title",style=MaterialTheme.typography.headlineMedium,color=DimGray);inner()}})
                Spacer(Modifier.height(12.dp))
                BasicTextField(content,{content=it},singleLine=false,textStyle=MaterialTheme.typography.bodyLarge.copy(color=ThemeState.text,lineHeight=24.sp),cursorBrush=SolidColor(PastelColors.getOrElse(color){Lavender}),modifier=Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
                    decorationBox={inner->Box{if(content.isEmpty())Text("Start writing...",style=MaterialTheme.typography.bodyLarge,color=DimGray);inner()}})
            }}
        }
    }
}

// ═══════════════════════════════ ADD DIALOG (today/todo/reminder) ═══════════════════════════════
@Composable fun AddDialog(tab:Int,ctx:Context,onAdd:(Any)->Unit,onDismiss:()->Unit){
    Dialog(onDismissRequest=onDismiss,properties=DialogProperties(usePlatformDefaultWidth=false)){
        Box(Modifier.fillMaxSize().padding(horizontal=12.dp,vertical=24.dp),contentAlignment=Alignment.TopCenter){
            Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(containerColor=ThemeState.bg)){
                Column(Modifier.padding(20.dp).verticalScroll(rememberScrollState())){
                    when(tab){0->AddTodayContent(ctx,onAdd);1->AddTodoContent(onAdd);2->AddReminderContent(ctx,onAdd)}
                }
            }
        }
    }
}

@Composable fun AddTodayContent(ctx:Context,onAdd:(Any)->Unit){
    var title by remember{mutableStateOf("")};var timeStr by remember{mutableStateOf<String?>(null)};var timeMs by remember{mutableStateOf<Long?>(null)}
    var mode by remember{mutableStateOf(NotifMode.ALERT)};var anon by remember{mutableStateOf(false)}
    val fmt=remember{SimpleDateFormat("h:mm a",Locale.getDefault())};val focus=remember{FocusRequester()};LaunchedEffect(Unit){delay(200);focus.requestFocus()}
    fun doSave(){if(title.isNotBlank()){val item=TodayItem(title=title.trim(),time=timeStr,timeMillis=timeMs,mode=mode,anonymous=anon);if(timeMs!=null)Notifs.schedule(ctx,item.id,item.title,timeMs!!,mode,anon,0);onAdd(item)}}
    Text("add to today",style=MaterialTheme.typography.headlineMedium,color=ThemeState.text);Spacer(Modifier.height(16.dp))
    FocusInput(title,{title=it},"What needs doing?",focus,onDone={doSave()});Spacer(Modifier.height(12.dp))
    TP(timeStr,UrgentRed){h,m->val c=Calendar.getInstance().apply{set(Calendar.HOUR_OF_DAY,h);set(Calendar.MINUTE,m);set(Calendar.SECOND,0)};timeStr=fmt.format(c.time);timeMs=c.timeInMillis}
    if(timeStr!=null){Spacer(Modifier.height(12.dp));Text("NOTIFICATION",style=MaterialTheme.typography.labelLarge,color=ThemeState.subtext);Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Chip("Alert",mode==NotifMode.ALERT,UrgentRed){mode=NotifMode.ALERT};Chip("Passive",mode==NotifMode.PASSIVE,UrgentRed){mode=NotifMode.PASSIVE}}
        if(mode==NotifMode.PASSIVE)Text("Shows silently when you open your phone",style=MaterialTheme.typography.bodyMedium,color=ThemeState.subtext,modifier=Modifier.padding(top=4.dp))
        Spacer(Modifier.height(8.dp));Toggle("Anonymous","pebble-ping says hi!",anon,{anon=it},UrgentRed)}
    Spacer(Modifier.height(20.dp))
    Btn("Save",title.isNotBlank(),UrgentRed){doSave()}
}

@Composable fun AddTodoContent(onAdd:(Any)->Unit){
    var title by remember{mutableStateOf("")};var desc by remember{mutableStateOf("")}
    val focus=remember{FocusRequester()};LaunchedEffect(Unit){delay(200);focus.requestFocus()}
    fun doSave(){if(title.isNotBlank())onAdd(TodoItem(title=title.trim(),description=desc.trim()))}
    Text("add todo",style=MaterialTheme.typography.headlineMedium,color=ThemeState.text);Spacer(Modifier.height(16.dp))
    FocusInput(title,{title=it},"What needs doing?",focus,onDone={doSave()});Spacer(Modifier.height(10.dp));Input(desc,{desc=it},"Details (optional)");Spacer(Modifier.height(20.dp))
    Btn("Save",title.isNotBlank(),Peach){doSave()}
}

@Composable fun AddReminderContent(ctx:Context,onAdd:(Any)->Unit){
    var title by remember{mutableStateOf("")};var dateStr by remember{mutableStateOf<String?>(null)};var dateMs by remember{mutableStateOf(0L)}
    var timeStr by remember{mutableStateOf<String?>(null)};var timeMs by remember{mutableStateOf<Long?>(null)};var mode by remember{mutableStateOf(NotifMode.ALERT)};var anon by remember{mutableStateOf(false)}
    val df=remember{SimpleDateFormat("EEE, MMM d",Locale.getDefault())};val tf=remember{SimpleDateFormat("h:mm a",Locale.getDefault())}
    val focus=remember{FocusRequester()};LaunchedEffect(Unit){delay(200);focus.requestFocus()}
    fun doSave(){if(title.isNotBlank()&&dateStr!=null){
        val item=ReminderItem(title=title.trim(),date=dateStr?:"",time=timeStr,dateMillis=dateMs,timeMillis=timeMs,mode=mode,anonymous=anon)
        if(timeMs!=null&&dateMs>0){val dc=Calendar.getInstance().apply{timeInMillis=dateMs};val tc=Calendar.getInstance().apply{timeInMillis=timeMs!!};dc.set(Calendar.HOUR_OF_DAY,tc.get(Calendar.HOUR_OF_DAY));dc.set(Calendar.MINUTE,tc.get(Calendar.MINUTE));dc.set(Calendar.SECOND,0)
            if(dc.timeInMillis>System.currentTimeMillis())Notifs.schedule(ctx,item.id,item.title,dc.timeInMillis,mode,anon,2)};onAdd(item)}}
    Text("set reminder",style=MaterialTheme.typography.headlineMedium,color=ThemeState.text);Spacer(Modifier.height(16.dp))
    FocusInput(title,{title=it},"Remind me about...",focus,onDone={doSave()});Spacer(Modifier.height(12.dp))
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(ThemeState.card).clickable{val n=Calendar.getInstance();DatePickerDialog(ctx,{_,y,m,d->val c=Calendar.getInstance().apply{set(y,m,d)};dateStr=df.format(c.time);dateMs=c.timeInMillis},n.get(Calendar.YEAR),n.get(Calendar.MONTH),n.get(Calendar.DAY_OF_MONTH)).apply{datePicker.minDate=System.currentTimeMillis()-1000}.show()}.padding(16.dp,14.dp),verticalAlignment=Alignment.CenterVertically){
        Icon(Icons.Rounded.CalendarToday,null,tint=Lavender,modifier=Modifier.size(20.dp));Spacer(Modifier.width(12.dp));Text(dateStr?:"Pick a date",style=MaterialTheme.typography.bodyLarge,color=if(dateStr!=null)ThemeState.text else DimGray)}
    Spacer(Modifier.height(8.dp))
    TP(timeStr,Lavender){h,m->val c=Calendar.getInstance().apply{set(Calendar.HOUR_OF_DAY,h);set(Calendar.MINUTE,m);set(Calendar.SECOND,0)};timeStr=tf.format(c.time);timeMs=c.timeInMillis}
    Spacer(Modifier.height(14.dp));Text("NOTIFICATION",style=MaterialTheme.typography.labelLarge,color=ThemeState.subtext);Spacer(Modifier.height(6.dp))
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Chip("Alert",mode==NotifMode.ALERT,Lavender){mode=NotifMode.ALERT};Chip("Passive",mode==NotifMode.PASSIVE,Lavender){mode=NotifMode.PASSIVE}}
    if(mode==NotifMode.PASSIVE)Text("Shows silently when you open your phone",style=MaterialTheme.typography.bodyMedium,color=ThemeState.subtext,modifier=Modifier.padding(top=4.dp))
    Spacer(Modifier.height(8.dp));Toggle("Anonymous","pebble-ping says hi!",anon,{anon=it},Lavender)
    Spacer(Modifier.height(20.dp))
    Btn("Save",title.isNotBlank()&&dateStr!=null,Lavender){doSave()}
}

// ═══════════════════════════════ SHARED ═══════════════════════════════
@Composable fun ItemCard(title:String,subtitle:String?,accent:Color,dim:Color,completed:Boolean,onToggle:()->Unit,onDelete:()->Unit){
    val dc=if(ThemeState.isDark)dim else ThemeState.dimColors.getOrElse(PastelColors.indexOf(accent).coerceIn(0,ThemeState.dimColors.size-1)){LavenderDim}
    Card(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=4.dp),colors=CardDefaults.cardColors(containerColor=dc.copy(0.5f)),shape=RoundedCornerShape(16.dp)){
        Row(Modifier.padding(horizontal=16.dp,vertical=14.dp),verticalAlignment=Alignment.CenterVertically){
            Box(Modifier.size(26.dp).clip(CircleShape).background(if(completed)accent.copy(0.8f) else accent.copy(0.2f)).clickable{onToggle()},contentAlignment=Alignment.Center){if(completed)Icon(Icons.Rounded.Check,"Done",tint=DkBg,modifier=Modifier.size(16.dp))}
            Spacer(Modifier.width(14.dp));Column(Modifier.weight(1f)){Text(title,style=MaterialTheme.typography.titleMedium,color=if(completed)MutedGray else ThemeState.text,textDecoration=if(completed)TextDecoration.LineThrough else null,maxLines=1,overflow=TextOverflow.Ellipsis)
                if(subtitle!=null)Text(subtitle,style=MaterialTheme.typography.bodyMedium,color=ThemeState.subtext,maxLines=1,overflow=TextOverflow.Ellipsis)}
            Icon(Icons.Rounded.Close,"Delete",tint=MutedGray.copy(0.4f),modifier=Modifier.size(18.dp).clickable{onDelete()})
        }
    }
}

@Composable fun Input(value:String,onChange:(String)->Unit,placeholder:String){BasicTextField(value,onChange,singleLine=true,textStyle=MaterialTheme.typography.bodyLarge.copy(color=ThemeState.text),cursorBrush=SolidColor(Lavender),modifier=Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(ThemeState.card).padding(16.dp,14.dp),decorationBox={inner->Box{if(value.isEmpty())Text(placeholder,style=MaterialTheme.typography.bodyLarge,color=DimGray);inner()}})}
@Composable fun FocusInput(value:String,onChange:(String)->Unit,placeholder:String,fr:FocusRequester,onDone:(()->Unit)?=null){
    BasicTextField(value,onChange,singleLine=true,textStyle=MaterialTheme.typography.bodyLarge.copy(color=ThemeState.text),cursorBrush=SolidColor(Lavender),
        keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(imeAction=androidx.compose.ui.text.input.ImeAction.Done),
        keyboardActions=androidx.compose.foundation.text.KeyboardActions(onDone={onDone?.invoke()}),
        modifier=Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(ThemeState.card).padding(16.dp,14.dp).focusRequester(fr),decorationBox={inner->Box{if(value.isEmpty())Text(placeholder,style=MaterialTheme.typography.bodyLarge,color=DimGray);inner()}})}
@Composable fun TP(cur:String?,color:Color,onPick:(Int,Int)->Unit){val ctx=LocalContext.current;Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(ThemeState.card).clickable{val n=Calendar.getInstance();TimePickerDialog(ctx,{_,h,m->onPick(h,m)},n.get(Calendar.HOUR_OF_DAY),n.get(Calendar.MINUTE),false).show()}.padding(16.dp,14.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Rounded.Schedule,null,tint=color,modifier=Modifier.size(20.dp));Spacer(Modifier.width(12.dp));Text(cur?:"Set time (optional)",style=MaterialTheme.typography.bodyLarge,color=if(cur!=null)ThemeState.text else DimGray)}}
@Composable fun Chip(label:String,selected:Boolean,color:Color,onClick:()->Unit){Box(Modifier.clip(RoundedCornerShape(12.dp)).background(if(selected)color.copy(0.2f) else ThemeState.card).clickable{onClick()}.padding(horizontal=14.dp,vertical=10.dp)){Text(label,style=MaterialTheme.typography.labelLarge,color=if(selected)color else MutedGray)}}
@Composable fun Toggle(label:String,subtitle:String,checked:Boolean,onChange:(Boolean)->Unit,color:Color){Row(Modifier.fillMaxWidth().padding(vertical=4.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(label,style=MaterialTheme.typography.bodyLarge,color=ThemeState.text);Text(subtitle,style=MaterialTheme.typography.bodyMedium,color=ThemeState.subtext)};Switch(checked,onChange,colors=SwitchDefaults.colors(checkedThumbColor=color,checkedTrackColor=color.copy(0.3f),uncheckedThumbColor=MutedGray,uncheckedTrackColor=ThemeState.card))}}
@Composable fun Btn(text:String,enabled:Boolean,color:Color,onClick:()->Unit){Button(onClick,enabled=enabled,modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(16.dp),colors=ButtonDefaults.buttonColors(containerColor=color,contentColor=DkBg,disabledContainerColor=color.copy(0.2f),disabledContentColor=MutedGray)){Text(text,fontWeight=FontWeight.SemiBold)}}
@Composable fun Lbl(text:String,color:Color){Text(text.uppercase(),style=MaterialTheme.typography.labelLarge,color=color,fontWeight=FontWeight.SemiBold,modifier=Modifier.padding(horizontal=20.dp,vertical=8.dp))}
