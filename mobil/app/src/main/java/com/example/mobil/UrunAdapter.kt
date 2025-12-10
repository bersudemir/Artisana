package com.example.mobil

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobil.databinding.UrunCardTasarimBinding

class UrunAdapter(
    private val context: Context,
    private var urunListesi: List<Urun>,
    private var userFavoriList: List<String>,
    private val onItemClick: (Urun) -> Unit,
    private val onFavClick: (Urun) -> Unit,
    private val onSepetClick: (Urun) -> Unit
) : RecyclerView.Adapter<UrunAdapter.UrunViewHolder>() {

    inner class UrunViewHolder(val binding: UrunCardTasarimBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrunViewHolder {
        val binding = UrunCardTasarimBinding.inflate(LayoutInflater.from(context), parent, false)
        return UrunViewHolder(binding)
    }

    override fun getItemCount(): Int = urunListesi.size

    //veriyi karta işleme
    override fun onBindViewHolder(holder: UrunViewHolder, position: Int) {
        val urun = urunListesi[position]

        holder.binding.apply {
            urunAd.text = urun.urun_ad
            urunFiyat.text = String.format("%.2f TL", urun.urun_fiyat)

            //resim yükleme
            try {
                val imageBytes = android.util.Base64.decode(urun.urun_resim_url, android.util.Base64.DEFAULT)
                Glide.with(holder.itemView.context).load(imageBytes).placeholder(R.drawable.ilk_logo).into(holder.binding.urunResim) // XML'deki id neyse onu yaz (imgUrun, imageView vs.)
            } catch (e: Exception) {
                Glide.with(holder.itemView.context).load(urun.urun_resim_url).placeholder(R.drawable.ilk_logo).into(holder.binding.urunResim)
            }

            // Fav kontrolü
            val isFav = userFavoriList.contains(urun.urun_id)
            urunFavori.setImageResource(if (isFav) R.drawable.fav_dolu_ikon else R.drawable.fav_ikon)

            // tıklama olayları
            urunFavori.setOnClickListener {
                if (holder.bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onFavClick(urunListesi[holder.bindingAdapterPosition])
                }
            }

            btnSepeteEkle.setOnClickListener {
                if (holder.bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onSepetClick(urunListesi[holder.bindingAdapterPosition])
                }
            }

            root.setOnClickListener {
                if (holder.bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(urunListesi[holder.bindingAdapterPosition])
                }
            }
        }
    }

    fun verileriGuncelle(yeniUrunListesi: List<Urun>, yeniFavListesi: List<String>) {
        this.urunListesi = yeniUrunListesi
        this.userFavoriList = yeniFavListesi
        notifyDataSetChanged() // listeyi güncelle
    }
}