package com.example.mobil

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobil.databinding.AdminSiparisCardBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SiparisAdapter(
    private val siparisListesi: ArrayList<Siparis>,
    private val onOnaylaClick: (Siparis) -> Unit,
    // onDetayClick'i artık içeride hallettiğimiz için boşta kalacak ama silmene gerek yok
    private val onDetayClick: (Siparis) -> Unit
) : RecyclerView.Adapter<SiparisAdapter.SiparisViewHolder>() {

    class SiparisViewHolder(val binding: AdminSiparisCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiparisViewHolder {
        val binding = AdminSiparisCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SiparisViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SiparisViewHolder, position: Int) {
        val siparis = siparisListesi[position]
        val context = holder.itemView.context

        // Tarih Formatlama
        val sdf = SimpleDateFormat("dd.MM.yyyy - HH.mm", Locale.getDefault())
        val tarihString = try {
            if (siparis.tarih != 0L) sdf.format(Date(siparis.tarih)) else "Tarih Yok"
        } catch (e: Exception) {
            "Hata"
        }

        // Karttaki verileri doldurma (Burası liste görünümü, değişmedi)
        holder.binding.apply {
            txtOrderId.text = "Sipariş ID: ${siparis.siparis_id}"
            txtOrderDate.text = "Tarih: $tarihString"
            txtCustomerName.text = "Müşteri: ${siparis.user_name}"
            txtTotalAmount.text = "Tutar: ${siparis.toplam_tutar} TL"
            txtStatus.text = "Durum: ${siparis.durum}"

            if (siparis.durum == "Onaylandı") {
                txtStatus.setTextColor(ContextCompat.getColor(context, R.color.successColor)) // Rengi kontrol et
            } else {
                txtStatus.setTextColor(ContextCompat.getColor(context, R.color.errorColor)) // Rengi kontrol et
            }
        }

        // Onayla Butonu Görünürlüğü
        if (siparis.durum == "Onaylandı") {
            holder.binding.btnApprove.visibility = View.GONE
        } else {
            holder.binding.btnApprove.visibility = View.VISIBLE
            holder.binding.btnApprove.setOnClickListener {
                onOnaylaClick(siparis)
            }
        }

        // --- DETAY BUTONU (Pencere ve QR İşlemi) ---
        holder.binding.btnDetails.setOnClickListener {

            // 1. Pencere tasarımını çağır
            val dialogView = LayoutInflater.from(context).inflate(R.layout.siparis_detay_dialog, null)

            val tvIcerik = dialogView.findViewById<TextView>(R.id.tvDialogIcerik)
            val imgQr = dialogView.findViewById<ImageView>(R.id.dialogQrImage)
            val btnKapat = dialogView.findViewById<Button>(R.id.btnKapat)

            // 2. SADECE ÜRÜNLERİ LİSTELEME
            val stringBuilder = StringBuilder()

            // Eğer sipariş ürünleri listesi boş değilse döngüye gir
            if (siparis.siparis_urunleri != null && siparis.siparis_urunleri!!.isNotEmpty()) {

                for (detay in siparis.siparis_urunleri!!) {
                    stringBuilder.append("• ${detay.urun_ad}\n")
                    stringBuilder.append("   ${detay.adet} Adet x ${detay.satis_fiyati} TL\n")
                    stringBuilder.append("   ----------------\n")
                }
            } else {
                stringBuilder.append("Ürün bilgisi bulunamadı.")
            }

            // Oluşturulan metni ekrana bas
            tvIcerik.text = stringBuilder.toString()


            // 3. WEB API - QR KOD OLUŞTURMA

            val irsaliyeMetni = """
                *** ARTISANA TESLIMAT ***
                -------------------------
                SIPARIS : ${siparis.siparis_id}
                MUSTERI : ${siparis.user_name}
                TUTAR   : ${siparis.toplam_tutar} TL
                DURUM   : ${siparis.durum}
                """.trimIndent()

            // 2. Bu metni link formatına uygun hale getiriyoruz (Boşluklar hata vermesin diye)
            val qrApiUrl = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=$irsaliyeMetni"

            Glide.with(context)
                .load(qrApiUrl)
                .placeholder(R.drawable.ilk_logo) // Yüklenirken logo görünsün (varsa)
                .into(imgQr)

            // 4. Pencereyi Göster
            val builder = AlertDialog.Builder(context)
            builder.setView(dialogView)
            val dialog = builder.create()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()

            btnKapat.setOnClickListener {
                dialog.dismiss()
            }
        }
    }

    override fun getItemCount(): Int {
        return siparisListesi.size
    }
}