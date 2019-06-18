package com.sokcuri.twimgspeedproxy

import android.app.AlertDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import android.content.Intent
import android.net.Uri
import android.widget.*


class MainActivity : AppCompatActivity() {
    private lateinit var mRunnable: Runnable
    private lateinit var context: Context
    private val serviceController = ServiceController(this, ProxyService::javaClass.get(ProxyService()))

    private fun getPortNumber(): Int {
        val sharedPref = context.getSharedPreferences("global", Context.MODE_PRIVATE) ?: return LittleProxy.port
        val defaultValue = Integer.parseInt(context.resources.getString(R.string.proxy_port))
        return sharedPref.getInt(context.getString(R.string.proxy_port), defaultValue)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        context = this

        var versionInfo = findViewById<TextView>(R.id.versionInfo)
        var pInfo = packageManager.getPackageInfo(packageName, 0)
        var version = pInfo.versionName
        versionInfo.text = version
        var serviceSwitch = findViewById<Switch>(R.id.serviceSwitch)
        if (ServiceController.isServiceRunning) {
            serviceSwitch.isChecked = true
        }
        serviceSwitch.setOnCheckedChangeListener { _, isChecked ->
            serviceSwitch.isClickable = false
            Handler().postDelayed({
                if (isChecked) {
                    startProxy()
                } else {
                    stopProxy()
                }
                Handler().postDelayed({
                    serviceSwitch.isClickable = true
                }, 500)
            }, 300)
        }

        var readmeButton = findViewById<Button>(R.id.readmeButton)
        readmeButton.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://docs.google.com/document/d/e/2PACX-1vSJr_ajbtPPDHl_9YXjl_-tr8eBprA0MJwN3PT8fU4-dOVpybbxOUVhDo0sOCMxiL86P1QhFDGp_M6e/pub"))
            startActivity(browserIntent)
        }

        var projectButton = findViewById<Button>(R.id.openProjectButton)
        projectButton.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://github.com/sokcuri/twimgspeedproxy"))
            startActivity(browserIntent)
        }

        var twitterButton = findViewById<Button>(R.id.openSokcuriTwitter)
        twitterButton.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://twitter.com/sokcuri")
            )
            startActivity(browserIntent)
        }

        var opentwitterButton = findViewById<Button>(R.id.openTwitter)
        twitterButton.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://twitter.com/sokcuri")
            )
            startActivity(browserIntent)
        }

        var portButton = findViewById<Button>(R.id.portButton)
        portButton.text = "PORT: " + getPortNumber().toString()
        portButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Port number")

            // Set up the input
            val input = EditText(this)
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_CLASS_NUMBER
            input.text = Editable.Factory.getInstance().newEditable(getPortNumber().toString())
            builder.setView(input)

            // Set up the buttons
            builder.setPositiveButton("OK") { dialog, which ->
                run {
                    setPortNumber(input.text.toString())
                }
            }
            builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

            builder.show()
        }
    }

    private fun setPortNumber(portText: String) {
        val oldPortNumber = LittleProxy.port
        if (!TextUtils.isEmpty(portText) && TextUtils.isDigitsOnly((portText)) &&
            Integer.parseInt(portText) !== 0) {
            LittleProxy.port = Integer.parseInt(portText)
            val sharedPref = this.getSharedPreferences("global", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putInt(getString(R.string.proxy_port), LittleProxy.port)
                commit()
            }
            portButton.text = "PORT: " + LittleProxy.port.toString()

            if (oldPortNumber !== LittleProxy.port && ServiceController.isServiceRunning) {
                startProxy()
            }
        } else {
            runOnUiThread { Toast.makeText(context, "올바르지 않는 포트 번호입니다", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun startProxy() {
        stopProxy()
        if (getTwimgServerList()) {
            serviceController.startService()
        } else {
            serviceSwitch.isChecked = false
        }
}

    private fun stopProxy() {
        serviceController.stopService()
    }

    private fun getTwimgServerList(): Boolean {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://raw.githubusercontent.com/sokcuri/TwimgSpeedPatch/master/data/server_ip.json")
            .build()

        val asyncTask = object : AsyncTask<Void, Void, String>() {
            override fun doInBackground(vararg params: Void): String? {
                return try {
                    val response = client.newCall(request).execute()
                    if (!response.isSuccessful) {
                        null
                    } else response.body().string()
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }

        val body = asyncTask.execute().get()
        if (body == null) {
            Toast.makeText(this, "서버 리스트를 가져올 수 없습니다. 인터넷 상태를 확인해 주세요",
                Toast.LENGTH_LONG).show()
            return false
        }

        var jsonArray: JSONArray
        try {
            jsonArray = JSONArray(body)
        } catch (ex: org.json.JSONException) {
            Toast.makeText(this, "서버 리스트 JSON을 읽지 못했습니다. 나중에 다시 시도해주세요",
                Toast.LENGTH_LONG).show()
            return false
        }

        HostPatch.twimgHost.clear()
        for (i in 0 until jsonArray.length()) {
            HostPatch.twimgHost.add(jsonArray.getString(i))
        }

        if (HostPatch.twimgHost.size == 0) {
            Toast.makeText(this, "서버 리스트가 비어있습니다. 프로젝트 페이지를 방문해주세요",
                Toast.LENGTH_LONG).show()
            return false
        }
    return true
    }

}
