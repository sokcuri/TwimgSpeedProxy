package com.sokcuri.twimgspeedproxy

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.preference.PreferenceManager
import android.util.Log
import android.widget.*
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import android.widget.CompoundButton


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    var FinishSettingActivity = 3000

    companion object {
        var mainActivity: MainActivity? = null
        fun setServiceSwitch(checked: Boolean) {
            if (mainActivity == null) return
            var serviceSwitch = MainActivity.mainActivity!!.findViewById<Switch>(R.id.serviceSwitch)
            serviceSwitch.setOnCheckedChangeListener(null)
            serviceSwitch.isChecked = checked
            serviceSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    mainActivity!!.startProxy()
                } else {
                    mainActivity!!.stopProxy()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mainActivity = this
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        var isWhiteListing: Boolean
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            isWhiteListing = pm.isIgnoringBatteryOptimizations(packageName)

            if (!isWhiteListing) {
                val dialog = AlertDialog.Builder(this@MainActivity)
                dialog.setTitle("권한이 필요합니다.")
                    .setMessage("원활한 앱 사용을 위해선 \"배터리 사용량 최적화\" 목록 제외 권한이 필요합니다. 계속할까요?")
                    .setPositiveButton("예") { _, _ ->
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                        intent.data = Uri.parse("package:$packageName")
                        startActivity(intent)
                    }
                    .setNegativeButton("아니오"
                    ) { _, _ ->
                        Toast.makeText(
                            this@MainActivity,
                            "권한 설정을 취소했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .create()
                    .show()
            }
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            shareTwitter("트위터 이미지 고속 프록시 사용중! 트위터 이미지 로딩이 느리다면 바로 사용해봐요~ https://play.google.com/store/apps/details?id=com.sokcuri.twimgspeedproxy")
        }

        var twitterImg = findViewById<ImageView>(R.id.twitterImg)
        twitterImg.setOnClickListener {
            openTwitter()
        }

        var readmeBtn = findViewById<Button>(R.id.readmeButton)
        readmeBtn.setOnClickListener {
            openBrowser(
            "https://docs.google.com/document/d/e/2PACX-1vSJr_ajbt" +
                    "PPDHl_9YXjl_-tr8eBprA0MJwN3PT8fU4-dOVpybbxOUVhDo0sOCMxiL86P1QhFDGp_M6e/pub")
        }

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        var versionInfo = findViewById<TextView>(R.id.versionInfo)
        var pInfo = packageManager.getPackageInfo(packageName, 0)
        var version = pInfo.versionName

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val serverArray = resources.getStringArray(R.array.servers_value)
        var cdnServer = serverArray.find {
            it == sharedPref.getString("cdnServer", "")
        }
        if (cdnServer == null) {
            cdnServer = serverArray[0]

            var editor = sharedPref.edit()
            editor.putString("cdnServer", cdnServer)
            editor.commit()
        }

        versionInfo.text = "$version - $cdnServer"
        var serviceSwitch = findViewById<Switch>(R.id.serviceSwitch)
        if (ProxyService.IsServiceRunning) {
            serviceSwitch.isChecked = true
        }

        serviceSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                mainActivity!!.startProxy()
            } else {
                mainActivity!!.stopProxy()
            }
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.home -> {
                // Handle the camera action
            }
            R.id.setting -> {
                val intent = Intent(this, SettingActivity::class.java)
                startActivityForResult(intent, FinishSettingActivity)
            }
            R.id.readme -> {
                openBrowser(
                    "https://docs.google.com/document/d/e/2PACX-1vSJr_ajbt" +
                    "PPDHl_9YXjl_-tr8eBprA0MJwN3PT8fU4-dOVpybbxOUVhDo0sOCMxiL86P1QhFDGp_M6e/pub")
            }
            R.id.sokcuri_twitter -> {
                openIntent("https://twitter.com/sokcuri")
            }
            R.id.source_code -> {
                openIntent("https://github.com/sokcuri/twimgspeedproxy")
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return false
    }

    private fun openBrowser(uriString: String) {
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(uriString)
            )

            val browseIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://"))
            val browseResolution = packageManager.resolveActivity(
                browseIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            intent.setPackage(browseResolution.activityInfo.applicationInfo.packageName);
            startActivity(intent)
        } catch(e: Exception) {
            openIntent(uriString)
        }
    }

    private fun openIntent(uriString: String) {
        val intent = Intent(Intent.ACTION_VIEW,
            Uri.parse(uriString))
        startActivity(intent)
    }

    private fun openTwitter() {
        val intent = Intent(Intent.ACTION_VIEW,
            Uri.parse("https://twitter.com/home"))
        intent.flags = intent.flags or Intent.FLAG_ACTIVITY_NO_HISTORY
        startActivity(intent)
    }

    private fun shareTwitter(message: String) {
        val tweetIntent = Intent(Intent.ACTION_SEND)
        tweetIntent.putExtra(Intent.EXTRA_TEXT, message)
        tweetIntent.type = "text/plain"

        val packManager = packageManager
        val resolvedInfoList = packManager.queryIntentActivities(tweetIntent, PackageManager.MATCH_DEFAULT_ONLY)

        var resolved = false
        for (resolveInfo in resolvedInfoList) {
            if (resolveInfo.activityInfo.packageName.startsWith("com.twitter.android")) {
                tweetIntent.setClassName(
                    resolveInfo.activityInfo.packageName,
                    resolveInfo.activityInfo.name
                )
                resolved = true
                break
            }
        }
        if (resolved) {
            startActivity(tweetIntent)
        } else {
            val i = Intent()
            i.putExtra(Intent.EXTRA_TEXT, message)
            i.action = Intent.ACTION_VIEW
            i.data = Uri.parse("https://twitter.com/intent/tweet?text=" + urlEncode(message))
            startActivity(i)
            Toast.makeText(this, "Twitter app isn't found", Toast.LENGTH_LONG).show()
        }
    }

    private fun urlEncode(s: String): String {
        try {
            return URLEncoder.encode(s, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            Log.wtf("FragmentActivity", "UTF-8 should always be supported", e)
            return ""
        }

    }

    private fun startProxy() {
        val intent = Intent(this@MainActivity, ProxyService::class.java)

        // https://stackoverflow.com/questions/44425584/context-startforegroundservice-did-not-then-call-service-startforeground
        val connection = ProxyService.getServiceConnection(this)
        try {
            this.bindService(
                intent, connection,
                Context.BIND_AUTO_CREATE
            )
        } catch (ignored: RuntimeException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.startForegroundService(intent)
            } else {
                this.startService(intent)
            }
        }
        MainActivity.setServiceSwitch(true)
    }

    private fun stopProxy() {
        val intent = Intent(this@MainActivity, ProxyService::class.java)
        intent.action = ProxyService.ActionStopForegroundService

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                FinishSettingActivity -> recreate()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}

private fun CompoundButton.setOnCheckedChangeListener() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}
