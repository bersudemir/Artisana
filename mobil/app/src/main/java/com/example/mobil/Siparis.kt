package com.example.mobil

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Siparis(
    var siparis_id: String? = "",
    var user_id: String? = "",
    var user_name: String? = "",
    var tarih: Long = 0L,
    var toplam_tutar: Double = 0.0,
    var durum: String? = "Sipariş Alındı",
    var siparis_urunleri: ArrayList<SiparisDetay>? = ArrayList()
) : Parcelable