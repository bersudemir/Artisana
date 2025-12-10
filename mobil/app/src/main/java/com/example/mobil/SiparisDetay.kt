package com.example.mobil

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SiparisDetay(
    var urun_id: String = "",
    var urun_ad: String = "",
    var urun_resim_url: String = "",
    var adet: Int = 1,
    var satis_fiyati: Double = 0.0
) : Parcelable