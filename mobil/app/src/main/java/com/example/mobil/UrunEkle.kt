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
import com.example.mobil.databinding.FragmentUrunEkleBinding
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream

class UrunEkle : Fragment() {
    private var _binding: FragmentUrunEkleBinding? = null
    private val binding get() = _binding!!
    private var secilenGorselUri: Uri? = null

    private val galeriBaslatici = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            secilenGorselUri = uri
            binding.imgUrunSec.setImageURI(uri)
            binding.tvSecilenSayisi.text = "Fotoğraf seçildi."
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUrunEkleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnResimSec.setOnClickListener {
            galeriBaslatici.launch("image/*")
        }

        binding.btnUrunKaydet.setOnClickListener {
            val ad = binding.etUrunAd.text.toString().trim()
            val fiyatStr = binding.etUrunFiyat.text.toString().trim()
            val stokStr = binding.etUrunStok.text.toString().trim()
            val aciklama = binding.etUrunAciklama.text.toString().trim()
            val kategoriIdStr = binding.etUrunKategoriId.text.toString().trim()

            if (ad.isEmpty() || fiyatStr.isEmpty() || stokStr.isEmpty() || kategoriIdStr.isEmpty() || secilenGorselUri == null) {
                Toast.makeText(requireContext(), "Lütfen tüm alanları doldurun ve resim seçin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            resmiDonusturVeKaydet(secilenGorselUri!!, ad, aciklama, kategoriIdStr.toInt(), fiyatStr.toDouble(), stokStr.toInt()
            )
        }
        // Geri Butonu
        binding.cardGeriBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun resmiDonusturVeKaydet(uri: Uri, ad: String, aciklama: String, kategoriId: Int, fiyat: Double, stok: Int) {
        binding.btnUrunKaydet.isEnabled = false
        binding.tvSecilenSayisi.text = "Resim işleniyor..."

        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // max boyut 1024px (daha net)
            val kucukBitmap = bitmapKucult(bitmap, 1024)

            val outputStream = ByteArrayOutputStream()
            // kalite %90
            kucukBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)

            val byteDizisi = outputStream.toByteArray()
            val resimStringi = Base64.encodeToString(byteDizisi, Base64.DEFAULT)

            // GÜVENLİK: Eğer resim 2MB'dan büyükse tekrar sıkıştır (Çökmemesi için)
            if (resimStringi.length > 2000000) {
                Toast.makeText(requireContext(), "Resim optimize ediliyor...", Toast.LENGTH_SHORT).show()
                val outputStreamLow = ByteArrayOutputStream()
                kucukBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStreamLow) // Kaliteyi 70'e düşür
                val yeniString = Base64.encodeToString(outputStreamLow.toByteArray(), Base64.DEFAULT)

                veritabaninaKaydet(ad, aciklama, kategoriId, fiyat, stok, yeniString)
            } else {
                veritabaninaKaydet(ad, aciklama, kategoriId, fiyat, stok, resimStringi)
            }

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Resim hatası: ${e.message}", Toast.LENGTH_SHORT).show()
            binding.btnUrunKaydet.isEnabled = true
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

    private fun veritabaninaKaydet(ad: String, aciklama: String, kategoriId: Int, fiyat: Double, stok: Int, resimString: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("urunler")
        val yeniId = dbRef.push().key

        if (yeniId != null) {
            val yeniUrun = Urun(
                urun_id = yeniId,
                urun_ad = ad,
                urun_aciklama = aciklama,
                kategori_id = kategoriId,
                urun_fiyat = fiyat,
                stok = stok,
                aktif_mi = true,
                urun_resim_url = resimString,
            )

            dbRef.child(yeniId).setValue(yeniUrun)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Ürün Eklendi!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                .addOnFailureListener {
                    binding.btnUrunKaydet.isEnabled = true
                    Toast.makeText(requireContext(), "Hata oluştu!", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}