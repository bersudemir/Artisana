package com.example.mobil

import java.io.Serializable
data class SepetUrun(
    val urun_id: String = "",
    val urun_ad: String = "",
    val urun_fiyat: Double = 0.0,
    val urun_resim_url: String = "",
    var adet: Int = 1
) : Serializable
