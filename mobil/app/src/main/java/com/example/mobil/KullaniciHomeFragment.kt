package com.example.mobil

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mobil.databinding.FragmentKullaniciHomeBinding
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class KullaniciHomeFragment : Fragment() {
    private var _binding: FragmentKullaniciHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var urunListesi: MutableList<Urun>
    private val tumUrunListesi = mutableListOf<Urun>()
    private val userFavoriList = mutableListOf<String>()
    private lateinit var urunDatabase: DatabaseReference
    private lateinit var userFavDatabase: DatabaseReference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var urunAdapter: UrunAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentKullaniciHomeBinding.inflate(inflater, container, false)

        urunDatabase = FirebaseDatabase.getInstance().getReference("urunler")

        urunListesi = mutableListOf()
        binding.rvHome.layoutManager = GridLayoutManager(requireContext(), 2)

        urunAdapter = UrunAdapter(requireContext(), urunListesi, userFavoriList,
            onItemClick = { urun ->
                val bundle = Bundle().apply {
                    putParcelable("tasinanUrun", urun)
                }
                findNavController().navigate(R.id.action_kullaniciHomeFragment_to_UrunDetayFragment, bundle)
            },
            onFavClick = { urun ->
                favoriDurumunuGuncelle(urun)
            },
            onSepetClick = { urun ->
                sepeteEkle(urun)
            }
        )
        binding.rvHome.adapter = urunAdapter

        tumUrunleriGetir()
        setupKategoriFiltreleme()

        if (userId != null) {
            userFavDatabase = FirebaseDatabase.getInstance().getReference("favoriler").child(userId)
            kullaniciFavorileriniGetir()
        }

        return binding.root
    }

    private fun setupKategoriFiltreleme() {
        renkleriGuncelle(R.id.chipTumu)
        binding.chipGroupKategori.setOnCheckedChangeListener { _, checkedId ->
            renkleriGuncelle(checkedId)
            filtrele(checkedId)
        }
    }

    private fun renkleriGuncelle(secilenId: Int) {
        binding.chipGroupKategori.children.forEach { view ->
            val chip = view as Chip
            if (chip.id == secilenId) {
                chip.setChipBackgroundColorResource(R.color.logoYesili)
                chip.setTextColor(Color.WHITE)
            } else {
                chip.setChipBackgroundColorResource(R.color.white)
                context?.let {
                    chip.setTextColor(ContextCompat.getColor(it, R.color.textPrimaryColor))
                }
            }
        }
    }

    private fun tumUrunleriGetir() {
        urunDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // fragment kapanmışsa işlemi durdur, hata almamak için
                if (_binding == null) return

                //önce listeyi temzile sonre veri çek
                tumUrunListesi.clear()

                for (data in snapshot.children) {
                    val urun = data.getValue(Urun::class.java)

                    if (urun != null) {
                        // Sadece aktif ve stokta olanları al
                        if (urun.aktif_mi == true && urun.stok > 0) {
                            urun.urun_id = data.key.toString()
                            tumUrunListesi.add(urun)
                        }
                    }
                }

                // veriler hazır, şimdi ekrana basmak için filtrele fonksiyonunu çağır
                filtrele(binding.chipGroupKategori.checkedChipId)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun filtrele(checkedId: Int) {
        urunListesi.clear()

        // Ana havuzdan (tumUrunListesi) seçip görünen listeye (urunListesi) ekle
        when (checkedId) {
            R.id.chipTumu -> urunListesi.addAll(tumUrunListesi)
            R.id.chipCanta -> urunListesi.addAll(tumUrunListesi.filter { it.kategori_id == 1 })
            R.id.chipCuzdan -> urunListesi.addAll(tumUrunListesi.filter { it.kategori_id == 2 })
            R.id.chipAksesuar -> urunListesi.addAll(tumUrunListesi.filter { it.kategori_id == 3 })
            else -> urunListesi.addAll(tumUrunListesi)
        }

        // Adapter'a "liste değişti, kendini yenile" diyoruz
        urunAdapter.notifyDataSetChanged()
    }

    private fun kullaniciFavorileriniGetir() {
        userFavDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return
                userFavoriList.clear()
                snapshot.children.forEach {
                    it.key?.let { id -> userFavoriList.add(id) }
                }
                urunAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun favoriDurumunuGuncelle(urun: Urun) {
        if (userId == null) return
        val isFav = userFavoriList.contains(urun.urun_id)
        if (isFav) {
            userFavDatabase.child(urun.urun_id).removeValue()
        } else {
            userFavDatabase.child(urun.urun_id).setValue(urun)
        }
    }

    private fun sepeteEkle(urun: Urun) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val sepetRef = FirebaseDatabase.getInstance().getReference("sepet").child(uid).child(urun.urun_id)

        sepetRef.get().addOnSuccessListener { snapshot ->
            if (_binding == null) return@addOnSuccessListener
            if (snapshot.exists()) {
                val mevcutAdet = snapshot.child("adet").getValue(Int::class.java) ?: 0
                sepetRef.child("adet").setValue(mevcutAdet + 1)
            }
            else {
                val yeniSepetUrun = SepetUrun(
                    urun_id = urun.urun_id,
                    urun_ad = urun.urun_ad ?: "",
                    urun_fiyat = urun.urun_fiyat,
                    urun_resim_url = urun.urun_resim_url ?: "",
                    adet = 1
                )
                sepetRef.setValue(yeniSepetUrun)
            }
            Toast.makeText(requireContext(), "Sepete Eklendi!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}