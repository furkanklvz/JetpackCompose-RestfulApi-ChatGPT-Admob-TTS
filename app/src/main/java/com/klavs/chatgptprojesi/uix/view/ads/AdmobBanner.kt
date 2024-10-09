package com.klavs.chatgptprojesi.uix.view.ads

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.klavs.chatgptprojesi.utils.ApiConstants

@Composable
fun AdmobBanner(modifier: Modifier){
    AndroidView(modifier = modifier,factory = {context->
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = ApiConstants.BANNER_AD_ID
            //adUnitId = "ca-app-pub-3940256099942544/6300978111"
            loadAd(AdRequest.Builder().build())
        }

    })
}

@Preview
@Composable
fun AdReview(){
    AdmobBanner(modifier = Modifier)

}