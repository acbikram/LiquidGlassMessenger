package com.example.liquidglass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ice = Color(0xFF83E8FF)
private val Ink = Color(0xFF080D18)
private data class Person(val name:String,val preview:String,val hue:Color,val unread:Int=0)
private data class Msg(val text:String,val mine:Boolean)

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState:Bundle?) { super.onCreate(savedInstanceState); enableEdgeToEdge(); setContent { MessengerApp() } }
}

@Composable private fun MessengerApp() {
 var dark by rememberSaveable { mutableStateOf(true) }
 var opacity by rememberSaveable { mutableFloatStateOf(.78f) }
 var blur by rememberSaveable { mutableFloatStateOf(.6f) }
 var reflection by rememberSaveable { mutableStateOf(true) }
 var tab by rememberSaveable { mutableStateOf("Chats") }
 var active by remember { mutableStateOf<Person?>(null) }
 val fg=if(dark) Color.White else Color(0xFF172033)
 val base=if(dark) Ink else Color(0xFFEAF0F8)
 val glass=(if(dark) Color(0xFF1A2A40) else Color.White).copy(alpha=opacity)
 val people=remember { listOf(Person("Sarah Mitchell","Sent you the updated design ✨",Color(0xFFB06CFF),2),Person("Ahmed Al-Fayed","Voice message · 0:18",Color(0xFF39C6E8)),Person("Design Team","Maya: Looking great!",Color(0xFFFF9A67),5),Person("Family Group","Mom: See you this weekend ❤️",Color(0xFF58D6A0)),Person("Alex Chen","Photo",Color(0xFFFF6C9B)),Person("Olivia Parker","Thanks! Talk soon.",Color(0xFF7795FF))) }
 Box(Modifier.fillMaxSize().background(base)) {
  Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF214B6B).copy(alpha=if(dark).42f else .15f),Color.Transparent),radius=900f)))
  if(active!=null) ChatPage(active!!,fg,glass, { active=null }) else Column(Modifier.fillMaxSize().statusBarsPadding()) {
   Row(Modifier.fillMaxWidth().padding(horizontal=22.dp,vertical=16.dp),verticalAlignment=Alignment.CenterVertically) {
    Column(Modifier.weight(1f)) { Text(if(tab=="Chats") "Messages" else tab,color=fg,fontSize=30.sp,fontWeight=FontWeight.Bold,letterSpacing=(-.8).sp); Text("Stay close to your people",color=fg.copy(alpha=.58f),fontSize=13.sp) }
    GlassButton(Icons.Default.Search,glass,fg) { tab="Search" }; Spacer(Modifier.width(9.dp)); GlassButton(Icons.Default.Edit,glass,fg) { tab="New message" }
   }
   when(tab) {
    "Settings" -> Column(Modifier.weight(1f).fillMaxWidth().padding(22.dp).clip(RoundedCornerShape(28.dp)).background(glass).padding(20.dp)) {
     Text("Liquid Glass",color=fg,fontSize=23.sp,fontWeight=FontWeight.Bold)
     Row(Modifier.fillMaxWidth().padding(top=16.dp),verticalAlignment=Alignment.CenterVertically) { Text("Dark appearance",color=fg,modifier=Modifier.weight(1f)); Switch(dark,{dark=it}) }
     Text("Glass opacity",color=fg); Slider(opacity,{opacity=it},valueRange=.25f..1f)
     Text("Frosting strength (visual approximation)",color=fg); Slider(blur,{blur=it},valueRange=0f..1f)
     Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) { Text("Reflection highlights",color=fg,modifier=Modifier.weight(1f)); Switch(reflection,{reflection=it}) }
     Text("This prototype uses translucent surfaces; it does not claim true system-wide backdrop blur.",color=fg.copy(alpha=.55f),fontSize=12.sp)
     Spacer(Modifier.height(12.dp)); Text("Messages are local demo data. Telegram is not connected.",color=fg.copy(alpha=.55f),fontSize=12.sp)
    }
    "Contacts","Calls","New message","Search" -> PeoplePage(tab,people,fg,glass) { active=it }
    else -> {
     Row(Modifier.padding(horizontal=20.dp,vertical=5.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) { listOf("All","Unread","Personal","Groups").forEachIndexed { i,s -> Text(s,color=if(i==0) Ice else fg.copy(alpha=.7f),fontSize=12.sp,fontWeight=FontWeight.SemiBold,modifier=Modifier.clip(CircleShape).background(if(i==0) Ice.copy(alpha=.16f) else glass).clickable{}.padding(horizontal=15.dp,vertical=9.dp)) } }
     LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) { items(people) { p ->
      Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(23.dp)).background(glass).clickable { active=p }.padding(13.dp),verticalAlignment=Alignment.CenterVertically) {
       Avatar(p,54); Spacer(Modifier.width(13.dp)); Column(Modifier.weight(1f)) { Text(p.name,color=fg,fontWeight=FontWeight.SemiBold,fontSize=15.sp); Spacer(Modifier.height(4.dp)); Text(p.preview,color=fg.copy(alpha=.58f),maxLines=1,overflow=TextOverflow.Ellipsis,fontSize=13.sp) }
       Column(horizontalAlignment=Alignment.End) { Text("12:45",color=fg.copy(alpha=.45f),fontSize=10.sp); if(p.unread>0) Text(p.unread.toString(),color=Ink,fontSize=11.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=6.dp).clip(CircleShape).background(Ice).padding(horizontal=7.dp,vertical=3.dp)) }
      }
     } }
    }
   }
   Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal=18.dp,vertical=9.dp).clip(CircleShape).background(glass).padding(vertical=7.dp),horizontalArrangement=Arrangement.SpaceEvenly) {
    listOf(Icons.Default.ChatBubble to "Chats",Icons.Default.Call to "Calls",Icons.Default.People to "Contacts",Icons.Default.Settings to "Settings").forEach { (icon,label) ->
     Column(Modifier.weight(1f).clickable { tab=label }.padding(vertical=5.dp),horizontalAlignment=Alignment.CenterHorizontally) { Icon(icon,null,tint=if(tab==label) Ice else fg.copy(alpha=.58f),modifier=Modifier.size(21.dp)); Text(label,color=if(tab==label) Ice else fg.copy(alpha=.58f),fontSize=10.sp) }
    }
   }
  }
 }
}

@Composable private fun ChatPage(person:Person,fg:Color,glass:Color,onBack:()->Unit) {
 var draft by rememberSaveable(person.name) { mutableStateOf("") }
 val messages=remember(person.name) { mutableStateListOf(Msg("Hey! Are we still on for tomorrow?",false),Msg("Absolutely! See you at 10 😊",true),Msg("Perfect. I'll send the details tonight.",false)) }
 Column(Modifier.fillMaxSize().imePadding()) {
  Row(Modifier.fillMaxWidth().statusBarsPadding().padding(12.dp).clip(CircleShape).background(glass).padding(8.dp),verticalAlignment=Alignment.CenterVertically) { IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,null,tint=fg)}; Avatar(person,42); Column(Modifier.weight(1f).padding(start=10.dp)){Text(person.name,color=fg,fontWeight=FontWeight.SemiBold);Text("online recently",color=Ice,fontSize=11.sp)}; IconButton(onClick={}){Icon(Icons.Default.Call,null,tint=fg)} }
  LazyColumn(Modifier.weight(1f).fillMaxWidth(),contentPadding=PaddingValues(14.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) { items(messages){m -> Row(Modifier.fillMaxWidth(),horizontalArrangement=if(m.mine) Arrangement.End else Arrangement.Start){Column(Modifier.widthIn(max=300.dp).clip(RoundedCornerShape(20.dp)).background(if(m.mine) Color(0xFF245B70) else glass).padding(14.dp)){Text(m.text,color=fg,fontSize=15.sp);Text("12:45",color=fg.copy(alpha=.5f),fontSize=10.sp,modifier=Modifier.align(Alignment.End))}}} }
  Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(12.dp).clip(CircleShape).background(glass).padding(horizontal=8.dp,vertical=5.dp),verticalAlignment=Alignment.CenterVertically) { IconButton(onClick={}){Icon(Icons.Default.Add,null,tint=fg)}; BasicTextField(draft,{draft=it},Modifier.weight(1f).padding(12.dp),singleLine=true,textStyle=MaterialTheme.typography.bodyLarge.copy(color=fg),decorationBox={inner->Box{if(draft.isEmpty())Text("Message",color=fg.copy(alpha=.45f));inner()}}); IconButton(onClick={if(draft.isNotBlank()){messages.add(Msg(draft.trim(),true));draft=""}}){Icon(Icons.AutoMirrored.Filled.Send,null,tint=Ice)} }
 }
}

@Composable private fun PeoplePage(title:String,people:List<Person>,fg:Color,glass:Color,open:(Person)->Unit) {
 var query by rememberSaveable { mutableStateOf("") }
 Column(Modifier.fillMaxSize().padding(horizontal=20.dp)) { if(title=="Search") { BasicTextField(query,{query=it},Modifier.fillMaxWidth().padding(12.dp),textStyle=MaterialTheme.typography.bodyLarge.copy(color=fg),decorationBox={inner->Box{if(query.isEmpty())Text("Search names…",color=fg.copy(alpha=.5f));inner()}}) } else Text(title,color=fg,fontSize=22.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(bottom=12.dp))
  LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)){items(people.filter{it.name.contains(query,true)}){p->Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(glass).clickable{open(p)}.padding(12.dp),verticalAlignment=Alignment.CenterVertically){Avatar(p,46);Column(Modifier.padding(start=12.dp)){Text(p.name,color=fg,fontWeight=FontWeight.SemiBold);Text(p.preview,color=fg.copy(alpha=.55f),fontSize=12.sp)}}}}
 }
}
@Composable private fun Avatar(p:Person,size:Int){Box(Modifier.size(size.dp).clip(CircleShape).background(Brush.linearGradient(listOf(p.hue,p.hue.copy(alpha=.35f)))),contentAlignment=Alignment.Center){Text(p.name.split(" ").mapNotNull{it.firstOrNull()?.toString()}.take(2).joinToString(""),color=Color.White,fontSize=(size/3.2f).sp,fontWeight=FontWeight.Bold)}}
@Composable private fun GlassButton(icon:androidx.compose.ui.graphics.vector.ImageVector,glass:Color,fg:Color,click:()->Unit){Box(Modifier.size(44.dp).clip(CircleShape).background(glass).clickable(onClick=click),contentAlignment=Alignment.Center){Icon(icon,null,tint=fg,modifier=Modifier.size(21.dp))}}
