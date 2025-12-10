package com.example.mobil

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.mobil.databinding.FragmentUrunGuncelleBinding
import com.google.firebase.database.*
import java.io.ByteArrayOutputStream

class UrunGuncelleFragment : Fragment() {
    private var _binding: FragmentUrunGuncelleBinding? = null
    private val binding get() = _binding!!
    private var gelenUrun: Urun? = null
    private var yeniSecilenUri: Uri? = null

    private val galeriBaslatici = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                yeniSecilenUri = uri
                binding.imgUrunGuncelle.setImageURI(uri)
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUrunGuncelleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gelenUrun = arguments?.getParcelable("secilenUrun")
        if (gelenUrun != null) {
            verileriDoldur()
        }

        binding.cardResimGuncelle.setOnClickListener {
            galeriBaslatici.launch("image/*")
        }

        binding.btnDegisiklikleriKaydet.setOnClickListener {
            guncellemeyiBaslat()
        }

        // Geri Butonu
        binding.cardGeriBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun verileriDoldur() {
        binding.etGuncelleAd.setText(gelenUrun?.urun_ad)
        binding.etGuncelleFiyat.setText(gelenUrun?.urun_fiyat.toString())
        binding.etGuncelleStok.setText(gelenUrun?.stok.toString())
        binding.etGuncelleAciklama.setText(gelenUrun?.urun_aciklama)
        binding.etGuncelleKategoriId.setText(gelenUrun?.kategori_id.toString())

        try {
            if (!gelenUrun?.urun_resim_url.isNullOrEmpty()) {
                val resimByte = Base64.decode(gelenUrun?.urun_resim_url, Base64.DEFAULT)
                Glide.with(this)
                    .load(resimByte)
                    .into(binding.imgUrunGuncelle)
            }
        } catch (e: Exception) {
            Glide.with(this)
                .load(gelenUrun?.urun_resim_url)
                .placeholder(R.drawable.ilk_logo)
                .into(binding.imgUrunGuncelle)
        }
    }

    private fun guncellemeyiBaslat() {
        val ad = binding.etGuncelleAd.text.toString().trim()
        val aciklama = binding.etGuncelleAciklama.text.toString().trim()
        val kategoriIdStr = binding.etGuncelleKategoriId.text.toString().trim()
        val fiyatStr = binding.etGuncelleFiyat.text.toString().trim()
        val stokStr = binding.etGuncelleStok.text.toString().trim()

        if (ad.isEmpty() || fiyatStr.isEmpty() || stokStr.isEmpty() || kategoriIdStr.isEmpty()) {
            Toast.makeText(requireContext(), "Lütfen tüm bilgileri doldurun", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnDegisiklikleriKaydet.isEnabled = false
        val kategoriId = kategoriIdStr.toInt()

        if (yeniSecilenUri != null) {
            resmiDonusturVeKaydet(yeniSecilenUri!!, ad, aciklama, kategoriId, fiyatStr.toDouble(), stokStr.toInt())
        } else {
            veritabaninaYaz(ad, aciklama, kategoriId, fiyatStr.toDouble(), stokStr.toInt(), gelenUrun?.urun_resim_url ?: "")
        }
    }

    private fun resmiDonusturVeKaydet(uri: Uri, ad: String, aciklama: String, kategoriId: Int, fiyat: Double, stok: Int) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // 1024 px (daha net görüntü)
            val kucukBitmap = bitmapKucult(bitmap, 1024)

            val outputStream = ByteArrayOutputStream()
            // %90 Kalite
            kucukBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)

            val byteDizisi = outputStream.toByteArray()
            val yeniResimString = Base64.encodeToString(byteDizisi, Base64.DEFAULT)

            // GÜVENLİK: 2MB Kontrolü
            if (yeniResimString.length > 2000000) {
                Toast.makeText(requireContext(), "Resim optimize ediliyor...", Toast.LENGTH_SHORT).show()
                val outputStreamLow = ByteArrayOutputStream()
                kucukBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStreamLow)
                val yeniStringLow = Base64.encodeToString(outputStreamLow.toByteArray(), Base64.DEFAULT)
                veritabaninaYaz(ad, aciklama, kategoriId, fiyat, stok, yeniStringLow)
            } else {
                veritabaninaYaz(ad, aciklama, kategoriId, fiyat, stok, yeniResimString)
            }

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
            binding.btnDegisiklikleriKaydet.isEnabled = true
        }
    }

    private fun bitmapKucult(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    private fun veritabaninaYaz(ad: String, aciklama: String, kategoriId: Int, fiyat: Double, stok: Int, resimUrl: String) {
        if (gelenUrun?.urun_id == null) return

        val dbRef = FirebaseDatabase.getInstance().getReference("urunler").child(gelenUrun!!.urun_id!!)

        var aktiflikDurumu = gelenUrun!!.aktif_mi

        if (stok <= 0) {
            aktiflikDurumu = false // Stok bittiyse kapat
        } else {
            aktiflikDurumu = true // Stok varsa aç
        }

        val guncelVeri = HashMap<String, Any>()
        guncelVeri["urun_ad"] = ad
        guncelVeri["urun_aciklama"] = aciklama
        guncelVeri["kategori_id"] = kategoriId
        guncelVeri["urun_fiyat"] = fiyat
        guncelVeri["stok"] = stok
        guncelVeri["aktif_mi"] = aktiflikDurumu
        guncelVeri["urun_resim_url"] = resimUrl

        dbRef.updateChildren(guncelVeri)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Ürün Güncellendi!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener {
                binding.btnDegisiklikleriKaydet.isEnabled = true
                Toast.makeText(requireContext(), "Hata oluştu!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun urunuTamamenSil(urunId: String) {
        val dbRef = FirebaseDatabase.getInstance().reference

        // ürünler tablosundan sil
        dbRef.child("urunler").child(urunId).removeValue()
            .addOnSuccessListener {
                // favorilerden temizle
                dbRef.child("favoriler").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (userSnapshot in snapshot.children) {
                            if (userSnapshot.hasChild(urunId)) {
                                userSnapshot.ref.child(urunId).removeValue()
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })

                // sepetten temizle
                dbRef.child("sepet").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (userSnapshot in snapshot.children) {
                            if (userSnapshot.hasChild(urunId)) {
                                userSnapshot.ref.child(urunId).removeValue()
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })

                Toast.makeText(context, "Ürün tamamen silindi.", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Silme hatası!", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}