package com.example.mobil

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobil.databinding.FragmentAdminUrunYonetimiBinding
import com.google.firebase.database.*

class AdminUrunYonetimi : Fragment() {

    private var _binding: FragmentAdminUrunYonetimiBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var urunListesi: ArrayList<Urun>
    private lateinit var adapter: AdminUrunAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminUrunYonetimiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Firebase Bağlantısı
        database = FirebaseDatabase.getInstance().getReference("urunler")

        // RecyclerView Ayarları
        binding.rvUrunListesi.setHasFixedSize(true)
        binding.rvUrunListesi.layoutManager = LinearLayoutManager(requireContext())

        urunListesi = ArrayList()

        // ADAPTER TANIMLAMA
        adapter = AdminUrunAdapter(
            requireContext(),
            urunListesi,
            // GÜNCELLE TIKLANIRSA:
            onGuncelleClick = { secilenUrun ->
                // Seçilen ürünü paketle ve güncelleme sayfasına gönder
                val bundle = Bundle()
                bundle.putParcelable("secilenUrun", secilenUrun)

                // NOT: NavGraph'te ok çizdiğinden ve ID'sinin bu olduğundan emin ol
                findNavController().navigate(R.id.action_adminUrunYonetimi_to_urunGuncelleFragment, bundle)
            },
            // SİL TIKLANIRSA:
            onSilClick = { secilenUrun ->
                urunSilmeOnayiGoster(secilenUrun)
            },
            // DURUM DEĞİŞTİR (STOK KONTROLLÜ):
            onDurumDegistir = { urun, yeniDurum ->
                // EĞER SATIŞA AÇMAYA ÇALIŞIYORSA (yeniDurum == true) VE STOK YOKSA
                if (yeniDurum && urun.stok <= 0) {
                    Toast.makeText(requireContext(), "Stok 0 iken ürün satışa açılamaz!", Toast.LENGTH_LONG).show()
                    // Listeyi yenile ki switch görsel olarak geri kapalı konuma dönsün
                    adapter.notifyDataSetChanged()
                } else {
                    // Sorun yoksa güncelle
                    urunDurumunuGuncelle(urun, yeniDurum)
                }
            }
        )

        binding.rvUrunListesi.adapter = adapter

        // Yeni Ürün Ekle Butonu
        binding.btnYeniUrunEkle.setOnClickListener {
            findNavController().navigate(R.id.action_adminUrunYonetimi_to_urunEkle)
        }

        // Verileri Çek
        tumUrunleriGetir()

        // Geri Butonu
        binding.cardGeriBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun tumUrunleriGetir() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                urunListesi.clear()
                if (snapshot.exists()) {
                    for (urunSnapshot in snapshot.children) {
                        val urun = urunSnapshot.getValue(Urun::class.java)
                        if (urun != null) {
                            urunListesi.add(urun)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (_binding != null) {
                    Toast.makeText(requireContext(), "Hata: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun urunSilmeOnayiGoster(urun: Urun) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Ürünü Sil")
        builder.setMessage("${urun.urun_ad} silinsin mi?")
        builder.setPositiveButton("Evet") { _, _ ->
            database.child(urun.urun_id!!).removeValue()
        }
        builder.setNegativeButton("İptal", null)
        builder.show()
    }

    private fun urunDurumunuGuncelle(urun: Urun, yeniDurum: Boolean) {
        database.child(urun.urun_id!!).child("aktif_mi").setValue(yeniDurum)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}