package com.example.mobil

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobil.databinding.SepetCardTasarimBinding

class SepetAdapter(
    private val mContext: Context,
    private var sepetListesi: MutableList<SepetUrun>,
    private val onMiktarDegistir: (SepetUrun, Int) -> Unit,
    private val onSilClick: (SepetUrun) -> Unit
) : RecyclerView.Adapter<SepetAdapter.SepetViewHolder>() {

    inner class SepetViewHolder(val binding: SepetCardTasarimBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SepetViewHolder {
        val binding = SepetCardTasarimBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return SepetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SepetViewHolder, position: Int) {
        val urun = sepetListesi[position]

        holder.binding.apply {
            txtSepetUrunAd.text = urun.urun_ad
            txtAdet.text = urun.adet.toString()

            val toplam = urun.urun_fiyat * urun.adet
            txtSepetFiyat.text = String.format("%.2f ₺", toplam)

            try {
                val imageBytes = android.util.Base64.decode(urun.urun_resim_url, android.util.Base64.DEFAULT)
                Glide.with(holder.itemView.context).load(imageBytes).placeholder(R.drawable.ilk_logo).into(holder.binding.imgSepetUrun) // XML'deki id neyse onu yaz (imgUrun, imageView vs.)
            } catch (e: Exception) {
                Glide.with(holder.itemView.context).load(urun.urun_resim_url).placeholder(R.drawable.ilk_logo).into(holder.binding.imgSepetUrun)
            }

            btnArtir.setOnClickListener {
                onMiktarDegistir(urun, urun.adet + 1)
            }

            btnAzalt.setOnClickListener {
                if (urun.adet > 1) {
                    onMiktarDegistir(urun, urun.adet - 1)
                }
            }

            btnSepetSil.setOnClickListener {
                onSilClick(urun)
            }
        }
    }

    override fun getItemCount() = sepetListesi.size

}
