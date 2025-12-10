package com.example.mobil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mobil.databinding.FragmentKullaniciFavBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class KullaniciFavFragment : Fragment() {
    private var _binding: FragmentKullaniciFavBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: UrunAdapter
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val favoriUrunlerListesi = ArrayList<Urun>()
    private val userFavoriIdListesi = ArrayList<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentKullaniciFavBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        setupRecyclerView()
        favorileriGetir()
    }

    private fun setupRecyclerView() {
        if (context == null) return

        adapter = UrunAdapter(requireContext(), favoriUrunlerListesi, userFavoriIdListesi,
            onItemClick = { urun ->
                val bundle = Bundle().apply {
                    putParcelable("tasinanUrun", urun)
                }
                findNavController().navigate(R.id.action_kullaniciFavFragment_to_UrunDetayFragment, bundle)
            },
            onFavClick = { urun ->
                favoridenCikar(urun)
            },
            onSepetClick = { urun ->
                sepeteEkle(urun)
            }
        )

        binding.rvFav.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@KullaniciFavFragment.adapter
        }
    }

    private fun favorileriGetir() {
        val uid = auth.currentUser?.uid ?: return
        val urunlerRef = FirebaseDatabase.getInstance().getReference("urunler")

        database.child("favoriler").child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return

                favoriUrunlerListesi.clear()
                userFavoriIdListesi.clear()

                if (!snapshot.exists()) {
                    adapter.verileriGuncelle(favoriUrunlerListesi, userFavoriIdListesi)
                    binding.layoutBosUyari.visibility = View.VISIBLE
                    return
                }

                for (d in snapshot.children) {
                    val urunId = d.key.toString()

                    urunlerRef.child(urunId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(urunSnapshot: DataSnapshot) {
                            val guncelUrun = urunSnapshot.getValue(Urun::class.java)
                            //ürün bir şekilde satışta değilse favdan kaldır
                            if (guncelUrun == null || guncelUrun.aktif_mi == false || guncelUrun.stok <= 0) {
                                d.ref.removeValue()
                            } else {
                                guncelUrun.urun_id = urunId
                                favoriUrunlerListesi.add(guncelUrun)
                                userFavoriIdListesi.add(urunId)

                                // Adapter'ı güncelle
                                adapter.verileriGuncelle(favoriUrunlerListesi, userFavoriIdListesi)
                                binding.rvFav.visibility = View.VISIBLE
                                binding.layoutBosUyari.visibility = View.GONE
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun sepeteEkle(urun: Urun) {
        val uid = auth.currentUser?.uid ?: return
        val sepetRef = database.child("sepet").child(uid).child(urun.urun_id)

        sepetRef.get().addOnSuccessListener { snapshot ->
            if (_binding == null) return@addOnSuccessListener

            if (snapshot.exists()) {
                val mevcutAdet = snapshot.child("adet").getValue(Int::class.java) ?: 0
                sepetRef.child("adet").setValue(mevcutAdet + 1)
            } else {
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

    private fun favoridenCikar(urun: Urun) {
        val uid = auth.currentUser?.uid ?: return
        database.child("favoriler").child(uid).child(urun.urun_id).removeValue()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}