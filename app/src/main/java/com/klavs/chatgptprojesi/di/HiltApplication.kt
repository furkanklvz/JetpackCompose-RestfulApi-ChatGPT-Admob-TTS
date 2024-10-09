package com.klavs.chatgptprojesi.di

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HiltApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) {}
        val requestConfiguration = RequestConfiguration.Builder()
            .setTestDeviceIds(listOf("ABCDEF012345"))
            .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
            .build()

        MobileAds.setRequestConfiguration(requestConfiguration)
    }
}