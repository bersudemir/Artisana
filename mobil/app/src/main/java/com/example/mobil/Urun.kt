package com.example.mobil

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Urun (var urun_id: String = "",
                 var urun_ad: String = "",
                 var urun_fiyat: Double = 0.0,
                 var urun_resim_url: String = "",
                 var urun_aciklama: String = "",
                 var kategori_id: Int = 0,
                 var stok: Int= 1,
                 var aktif_mi: Boolean= true): Parcelable {
}