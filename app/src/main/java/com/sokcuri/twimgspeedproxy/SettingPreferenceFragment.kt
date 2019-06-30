package com.sokcuri.twimgspeedproxy

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.preference.*
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.R.id.edit
import android.content.SharedPreferences
import android.content.Context.MODE_PRIVATE
import android.R.attr.key




class SettingPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.app_preferences);

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        findPreference("versionInfo").summary = BuildConfig.VERSION_NAME
        findPreference("proxyPort").summary =
            sharedPref.getString("proxyPort", "57572")
        findPreference("cdnServer").summary =
            sharedPref.getString("cdnServer", "Akamai")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findPreference("proxyPort").onPreferenceChangeListener =
            object : Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(p0: Preference?, p1: Any?): Boolean {
                    if (!TextUtils.isDigitsOnly(p1 as String) ||
                        TextUtils.isEmpty(p1) ||
                        Integer.parseInt(p1) === 0) {
                        return false
                    }

                    LittleProxy.port = Integer.parseInt(p1)

                    val intent = Intent(context, ProxyService::class.java)
                    intent.action = ProxyService.ActionRestartForegroundService

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context?.startForegroundService(intent)
                    } else {
                        context?.startService(intent)
                    }

                    p0?.summary = LittleProxy.port.toString()
                    Log.d("proxyPort", p1.toString())
                    return true
                }
            }

        findPreference("cdnServer").onPreferenceChangeListener =
            object : Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(p0: Preference?, p1: Any?): Boolean {
                    LittleProxy.cdnServer = p1 as String
                    p0?.summary = p1.toString()
                    Log.d("cdnServer", p1.toString())

                    val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                    val editor = sharedPref.edit()
                    editor.putString("cdnServer", p1)
                    editor.commit()

                    val intent = Intent(context, ProxyService::class.java)
                    intent.action = ProxyService.ActionRestartForegroundService
                    context?.startService(intent)

                    Snackbar.make(view!!, "CDN 서버 변경은 트위터를 종료 후 다시 시작해야 적용됩니다",
                        Snackbar.LENGTH_SHORT).show()
                    return true
                }
            }
    }

    override fun onCreateAdapter(preferenceScreen: PreferenceScreen?): RecyclerView.Adapter<*> {
        return object : PreferenceGroupAdapter(preferenceScreen) {
            @SuppressLint("RestrictedApi")
            override fun onBindViewHolder(holder: PreferenceViewHolder, position: Int) {
                super.onBindViewHolder(holder, position)
                val preference = getItem(position)
                if (preference is PreferenceCategory)
                    setZeroPaddingToLayoutChildren(holder.itemView)
                else
                    holder.itemView.findViewById<View?>(R.id.icon_frame)?.visibility = if (preference.icon == null) View.GONE else View.VISIBLE
            }
        }
    }

    private fun setZeroPaddingToLayoutChildren(view: View) {
        if (view !is ViewGroup)
            return
        val childCount = view.childCount
        for (i in 0 until childCount) {
            setZeroPaddingToLayoutChildren(view.getChildAt(i))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                view.setPaddingRelative(0, view.paddingTop, view.paddingEnd, view.paddingBottom)
            else
                view.setPadding(0, view.paddingTop, view.paddingRight, view.paddingBottom)
        }
    }
}