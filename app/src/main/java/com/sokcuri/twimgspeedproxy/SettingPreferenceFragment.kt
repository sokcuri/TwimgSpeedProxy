package com.sokcuri.twimgspeedproxy

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.support.v7.preference.*
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import kotlinx.android.synthetic.main.content_main.*


class SettingPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.app_preferences);

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        findPreference("versionInfo").summary = BuildConfig.VERSION_NAME
        findPreference("proxyPort").summary =
            sharedPref.getString("proxyPort", "57572")
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
                    if (ServiceController.isServiceRunning) {
                        MainActivity.serviceController?.stopService()
                        MainActivity.serviceController?.startService()
                    }
                    p0?.summary = LittleProxy.port.toString()
                    Log.d("proxyPort", p1.toString())
                    return true
                }
            }

        findPreference("enableTwimgSpeed").onPreferenceChangeListener =
            object : Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(p0: Preference?, p1: Any?): Boolean {
                    LittleProxy.twimg = p1 as Boolean
                    Log.d("enableTwimgSpeed", p1.toString())
                    return true
                }
            }

        findPreference("enableTwvideoSpeed").onPreferenceChangeListener =
            object : Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(p0: Preference?, p1: Any?): Boolean {
                    LittleProxy.twvideo = p1 as Boolean
                    Log.d("enableTwvideoSpeed", p1.toString())
                    return true
                }
            }

        findPreference("enableTwabsSpeed").onPreferenceChangeListener =
            object : Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(p0: Preference?, p1: Any?): Boolean {
                    LittleProxy.twabs = p1 as Boolean
                    Log.d("enableTwabsSpeed", p1.toString())
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