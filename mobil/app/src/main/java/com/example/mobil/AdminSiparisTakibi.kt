package com.example.mobil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobil.databinding.FragmentAdminSiparisTakibiBinding
import com.google.firebase.database.*

class AdminSiparisTakibi : Fragment() {
    private var _binding: FragmentAdminSiparisTakibiBinding? = null
    private val binding get() = _binding!!
    private lateinit var siparisListesi: ArrayList<Siparis>
    private lateinit var adapter: SiparisAdapter
    private lateinit var database: DatabaseReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminSiparisTakibiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().reference.child("siparisler")

        binding.rvSiparisler.layoutManager = LinearLayoutManager(requireContext())
        siparisListesi = ArrayList()

        // Geri Butonu
        binding.cardGeriBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        verileriGetir()
    }

    private fun verileriGetir() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return

                siparisListesi.clear()
                if (snapshot.exists()) {
                    for (siparisSnapshot in snapshot.children) {
                        try {
                            val siparis = siparisSnapshot.getValue(Siparis::class.java)
                            if (siparis != null) {
                                if (siparis.siparis_id.isNullOrEmpty()) {
                                    siparis.siparis_id = siparisSnapshot.key
                                }
                                siparisListesi.add(siparis)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    siparisListesi.reverse() // Yeniler üstte

                    // DİKKAT: onDetayClick kısmını BOŞ {} geçiyoruz çünkü Adapter kendisi hallediyor.
                    adapter = SiparisAdapter(siparisListesi,
                        onOnaylaClick = { siparis -> siparisOnayla(siparis) },
                        onDetayClick = {}
                    )
                    binding.rvSiparisler.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (context != null) {
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun siparisOnayla(siparis: Siparis) {
        val id = siparis.siparis_id
        if (id.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Hata: Sipariş ID yok!", Toast.LENGTH_SHORT).show()
            return
        }

        val updateMap = HashMap<String, Any>()
        updateMap["durum"] = "Onaylandı"

        database.child(id).updateChildren(updateMap)
            .addOnSuccessListener {
                if (context != null)
                    Toast.makeText(requireContext(), "Sipariş Onaylandı ✅", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                if (context != null)
                    Toast.makeText(requireContext(), "Hata: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}