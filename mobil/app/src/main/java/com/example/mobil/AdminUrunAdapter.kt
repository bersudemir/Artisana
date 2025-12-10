package com.example.mobil

import android.content.Context
import android.util.Base64 // Base64 için gerekli kütüphane
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobil.databinding.AdminUrunItemBinding

class AdminUrunAdapter (
    private val mContext: Context,
    private val urunListesi : List<Urun>,
    private val onGuncelleClick: (Urun) -> Unit,
    private val onSilClick: (Urun) -> Unit,
    private val onDurumDegistir: (Urun, Boolean) -> Unit) : RecyclerView.Adapter<AdminUrunAdapter.CardTasarimTutucu>() {

    inner class CardTasarimTutucu(val binding: AdminUrunItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardTasarimTutucu {
        val binding = AdminUrunItemBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return CardTasarimTutucu(binding)
    }

    override fun onBindViewHolder(holder: CardTasarimTutucu, position: Int) {
        val urun = urunListesi[position]
        val t = holder.binding

        t.tvUrunAd.text = urun.urun_ad
        t.tvUrunFiyat.text = "Fiyat: ${urun.urun_fiyat} ₺"
        t.tvUrunStok.text = "Stok: ${urun.stok}"

        // resim yükleme (base64 + glide)
        try {
            // önce gelen veriyi base64 gibi çözmeye çalış
            val resimVerisi = Base64.decode(urun.urun_resim_url, Base64.DEFAULT)

            Glide.with(mContext)
                .load(resimVerisi) // çözülmüş resmi yükle
                .placeholder(R.drawable.ilk_logo)
                .into(t.imgUrunResim)
        } catch (e: Exception) {
            // Eğer Base64 değilse (eski bir linkse veya boşsa) normal yüklemeyi dene
            Glide.with(mContext)
                .load(urun.urun_resim_url)
                .placeholder(R.drawable.ilk_logo)
                .error(R.drawable.ilk_logo)
                .into(t.imgUrunResim)
        }

        // switch ayarları
        t.switch2.setOnCheckedChangeListener(null)
        t.switch2.isChecked = urun.aktif_mi == true

        // buton tıklamaları
        t.btnGuncelle.setOnClickListener {
            onGuncelleClick(urun)
        }
        t.btnSil.setOnClickListener {
            onSilClick(urun)
        }

        // switch değişince çalışacak kod
        t.switch2.setOnCheckedChangeListener { _, isChecked ->
            onDurumDegistir(urun, isChecked)
        }
    }

    override fun getItemCount(): Int {
        return urunListesi.size
    }
}