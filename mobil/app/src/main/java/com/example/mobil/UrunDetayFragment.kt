package com.example.mobil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.mobil.databinding.FragmentUrunDetayBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UrunDetayFragment : Fragment() {
    private var _binding: FragmentUrunDetayBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUrunDetayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gelenUrun = arguments?.getParcelable<Urun>("tasinanUrun")

        if (gelenUrun == null) {
            Toast.makeText(context, "Ürün verisi yüklenemedi!", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        binding.txtUrunAdi.text = gelenUrun.urun_ad
        binding.txtUrunFiyat.text = "${gelenUrun.urun_fiyat} ₺"
        binding.txtUrunAciklama.text = gelenUrun.urun_aciklama

        try {
            val imageBytes = android.util.Base64.decode(gelenUrun.urun_resim_url, android.util.Base64.DEFAULT)
            Glide.with(requireContext()).load(imageBytes).placeholder(R.drawable.ilk_logo).into(binding.imgUrunDetay)
        } catch (e: Exception) {
            // Eğer Base64 değilse (url ise) normal yükle
            Glide.with(requireContext()).load(gelenUrun.urun_resim_url).placeholder(R.drawable.ilk_logo).into(binding.imgUrunDetay)
        }

        // firebase işlemleri
        val user = FirebaseAuth.getInstance().currentUser

        val urunId = gelenUrun.urun_id ?: "hatali_id"

        if (user != null) {
            val uid = user.uid
            val database = FirebaseDatabase.getInstance()
            val sepetRef = database.getReference("sepet").child(uid).child(urunId)
            val favRef = database.getReference("favoriler").child(uid).child(urunId)

            //fav mı kontrolü
            favRef.get().addOnSuccessListener {
                if (_binding != null && it.exists()) {
                    binding.btnFav.setImageResource(R.drawable.fav_dolu_ikon)
                } else if (_binding != null) {
                    binding.btnFav.setImageResource(R.drawable.fav_ikon)
                }
            }

            // fav butona tıklama
            binding.cardFavBtn.setOnClickListener {
                favRef.get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        favRef.removeValue() // Çıkar
                        binding.btnFav.setImageResource(R.drawable.fav_ikon)
                        Toast.makeText(context, "Favoriden Çıkarıldı!", Toast.LENGTH_SHORT).show()
                    } else {
                        favRef.setValue(gelenUrun).addOnSuccessListener { // Ekle
                            binding.btnFav.setImageResource(R.drawable.fav_dolu_ikon)
                            Toast.makeText(context, "Favorilere Eklendi!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            //sepete ekleme
            binding.btnSepeteEkle.setOnClickListener {
                sepetRef.child("adet").get().addOnSuccessListener { adetSnap ->
                    if (adetSnap.exists()) {
                        // Zaten sepette varsa adedi arttır
                        val eskiAdet = adetSnap.getValue(Int::class.java) ?: 1
                        sepetRef.child("adet").setValue(eskiAdet + 1)
                    } else {
                        // Sepette yoksa yeni ekle
                        val map = HashMap<String, Any>()
                        map["urun_id"] = urunId
                        map["urun_ad"] = gelenUrun.urun_ad ?: ""
                        map["urun_fiyat"] = gelenUrun.urun_fiyat
                        map["urun_resim_url"] = gelenUrun.urun_resim_url ?: ""
                        map["adet"] = 1

                        sepetRef.setValue(map)
                    }
                    Toast.makeText(context, "Sepete Eklendi!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Giriş Yapılmamışsa
            val uyari = { Toast.makeText(context, "Lütfen giriş yapınız.S", Toast.LENGTH_SHORT).show() }
            binding.cardFavBtn.setOnClickListener { uyari() }
            binding.btnSepeteEkle.setOnClickListener { uyari() }
        }

        // Geri Butonu
        binding.cardGeriBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}