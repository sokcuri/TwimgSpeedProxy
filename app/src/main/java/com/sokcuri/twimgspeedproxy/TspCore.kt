package com.sokcuri.twimgspeedproxy

import android.app.Application
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric

class TspCore : Application() {

    override fun onCreate() {
        super.onCreate()

        // Crashlytics에 연동해두면 강제종료 발생시 기기 정보는 물론
        // 다른 스레드의 스택트레이스도 보고됩니다.
        Fabric.with(this, Crashlytics())
    }
}
