package com.joudy.wifipasswordfinder
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {
 private lateinit var wifi: WifiManager
 private lateinit var output: LinearLayout
 private lateinit var status: TextView
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  wifi = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
  val page = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(24,32,24,20); setBackgroundColor(0xFF101713.toInt()) }
  page.addView(TextView(this).apply { text="WiFi Password Finder"; textSize=25f; gravity=Gravity.CENTER; setTextColor(0xFF35D07F.toInt()) })
  status=TextView(this).apply { text="Find nearby Wi-Fi networks"; gravity=Gravity.CENTER; setTextColor(-1); setPadding(0,18,0,18) }
  page.addView(status)
  page.addView(Button(this).apply { text="SCAN NEARBY NETWORKS"; setOnClickListener { scan() } })
  page.addView(Button(this).apply { text="OPEN SAVED WI-FI SETTINGS"; setOnClickListener { startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) } })
  output=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
  page.addView(ScrollView(this).apply { addView(output) }, LinearLayout.LayoutParams(-1,0,1f))
  setContentView(page)
  val perms=mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
  if(android.os.Build.VERSION.SDK_INT>=33) perms.add(Manifest.permission.NEARBY_WIFI_DEVICES)
  val missing=perms.filter { checkSelfPermission(it)!=PackageManager.PERMISSION_GRANTED }
  if(missing.isNotEmpty()) requestPermissions(missing.toTypedArray(),7)
 }
 private fun scan() {
  if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED) { status.text="Allow location permission first"; return }
  if(!wifi.isWifiEnabled) { status.text="Turn Wi-Fi on first"; return }
  try { wifi.startScan() } catch(_:Exception) {}
  output.removeAllViews()
  val list=try { wifi.scanResults.sortedByDescending { it.level } } catch(_:Exception) { emptyList() }
  status.text=list.size.toString()+" networks found"
  list.forEach { n ->
   val sec=when { n.capabilities.contains("WPA3")->"WPA3"; n.capabilities.contains("WPA2")->"WPA2"; n.capabilities.contains("WPA")->"WPA"; n.capabilities.contains("WEP")->"WEP"; else->"Open / Unknown" }
   output.addView(TextView(this).apply { text=(n.SSID.ifBlank { "Hidden network" })+"\nSignal: "+n.level+" dBm  •  "+sec; textSize=15f; setTextColor(-1); setPadding(14,16,14,16); setBackgroundColor(0xFF1B2921.toInt()) })
  }
 }
}