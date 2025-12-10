package com.example.mobil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobil.databinding.FragmentKullaniciSepetBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class KullaniciSepetFragment : Fragment() {
    private var _binding: FragmentKullaniciSepetBinding? = null
    private val binding get() = _binding!!
    private lateinit var sepetAdapter: SepetAdapter
    private val sepetListesi = ArrayList<SepetUrun>()
    private lateinit var database: DatabaseReference
    private val auth = FirebaseAuth.getInstance()

    // Kullanıcı bilgilerini tutmak için
    private var currentUserName: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentKullaniciSepetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().reference
        val currentUser = auth.currentUser

        if (currentUser == null) {
            bosSepetGoster(true)
            Toast.makeText(requireContext(), "Lütfen giriş yapınız.", Toast.LENGTH_SHORT).show()
            return
        }

        // Kullanıcının ismini db den çek, sipariş oluştururken lazım
        kullaniciBilgisiGetir(currentUser.uid)

        setupRecyclerView()
        sepetVerileriniGetir(currentUser.uid)

        binding.butonOdemeyeGec.setOnClickListener {
            if (sepetListesi.isNotEmpty()) {
                satinAlmayiBaslat()
            } else {
                Toast.makeText(requireContext(), "Sepetiniz boş!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun kullaniciBilgisiGetir(uid: String) {
        database.child("users").child(uid).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val user = snapshot.getValue(User::class.java)
                currentUserName = user?.name ?: "Bilinmeyen Kullanıcı"
            }
        }
    }

    private fun satinAlmayiBaslat() {
        binding.butonOdemeyeGec.isEnabled = false
        val userId = auth.currentUser?.uid ?: return

        val yeniSiparisId = database.child("siparisler").push().key
        if (yeniSiparisId == null) {
            Toast.makeText(context, "Hata oluştu", Toast.LENGTH_SHORT).show()
            return
        }

        // Sepetteki ürünleri SiparisDetay listesine çevir
        val siparisDetaylari = ArrayList<SiparisDetay>()
        var toplamTutar = 0.0

        for (sepetUrun in sepetListesi) {
            val detay = SiparisDetay(
                urun_id = sepetUrun.urun_id,
                urun_ad = sepetUrun.urun_ad,
                urun_resim_url = sepetUrun.urun_resim_url,
                adet = sepetUrun.adet,
                satis_fiyati = sepetUrun.urun_fiyat
            )
            siparisDetaylari.add(detay)
            toplamTutar += (sepetUrun.urun_fiyat * sepetUrun.adet)
        }

        val yeniSiparis = Siparis(
            siparis_id = yeniSiparisId,
            user_id = userId,
            user_name = currentUserName,
            tarih = System.currentTimeMillis(),
            toplam_tutar = toplamTutar,
            durum = "Sipariş Alındı",
            siparis_urunleri = siparisDetaylari
        )

        //siparişleri kaydet
        database.child("siparisler").child(yeniSiparisId).setValue(yeniSiparis)
            .addOnSuccessListener {
                // Sipariş başarıyla kaydedildi, şimdi stok düş ve sepeti temizle
                stokDusVeSepetiTemizle(userId)
            }
            .addOnFailureListener {
                binding.butonOdemeyeGec.isEnabled = true
                Toast.makeText(requireContext(), "Sipariş oluşturulamadı: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun stokDusVeSepetiTemizle(userId: String) {
        val urunlerRef = database.child("urunler")

        // stok azalt
        for (sepetUrun in sepetListesi) {
            urunlerRef.child(sepetUrun.urun_id).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val mevcutStok = snapshot.child("stok").getValue(Int::class.java) ?: 0
                    var yeniStok = mevcutStok - sepetUrun.adet
                    if (yeniStok < 0) yeniStok = 0

                    val updateMap = HashMap<String, Any>()
                    updateMap["stok"] = yeniStok
                    if (yeniStok == 0) {
                        updateMap["aktif_mi"] = false
                    }
                    urunlerRef.child(sepetUrun.urun_id).updateChildren(updateMap)
                }
            }
        }

        // sepeti sil ve yönlendir
        database.child("sepet").child(userId).removeValue()
            .addOnSuccessListener {
                if (isAdded){//fragment hala ekranda mı kontrolu
                    Toast.makeText(requireContext(), "Siparişiniz başarıyla alındı!", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                if (isAdded) {
                    binding.butonOdemeyeGec.isEnabled = true
                    Toast.makeText(requireContext(), "Bir hata oluştu.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun setupRecyclerView() {
        sepetAdapter = SepetAdapter(
            requireContext(),
            sepetListesi,
            onMiktarDegistir = { urun, adet -> miktarGuncelle(urun, adet) },
            onSilClick = { urun -> urunuSil(urun) }
        )
        binding.rvSepetim.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sepetAdapter
            setHasFixedSize(true)
        }
    }

    private fun sepetVerileriniGetir(userId: String) {
        val urunlerRef = FirebaseDatabase.getInstance().getReference("urunler")

        database.child("sepet").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (_binding == null) return

                    sepetListesi.clear()

                    if (!snapshot.exists()) {
                        sepetAdapter.notifyDataSetChanged()
                        bosSepetGoster(true)
                        return
                    }

                    for (data in snapshot.children) {
                        val urunId = data.key ?: continue
                        urunlerRef.child(urunId).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(urunSnapshot: DataSnapshot) {
                                val guncelUrun = urunSnapshot.getValue(Urun::class.java)

                                if (guncelUrun == null || guncelUrun.aktif_mi == false || guncelUrun.stok <= 0) {
                                    data.ref.removeValue()
                                }
                                else {
                                    try {
                                        val urunAd = guncelUrun.urun_ad ?: ""
                                        val resimUrl = guncelUrun.urun_resim_url ?: ""
                                        val urunFiyat = guncelUrun.urun_fiyat ?: 0.0
                                        val adetObj = data.child("adet").value
                                        val adet = when (adetObj) {
                                            is Long -> adetObj.toInt()
                                            is Double -> adetObj.toInt()
                                            is String -> adetObj.toIntOrNull() ?: 1
                                            else -> 1
                                        }

                                        val sepetUrun = SepetUrun(urunId, urunAd, urunFiyat, resimUrl, adet)
                                        sepetListesi.add(sepetUrun)

                                        var toplamTutar = 0.0
                                        sepetListesi.forEach { toplamTutar += (it.urun_fiyat * it.adet) }

                                        sepetAdapter.notifyDataSetChanged()
                                        binding.textToplamFiyat.text = String.format("%.2f ₺", toplamTutar)
                                        bosSepetGoster(sepetListesi.isEmpty())

                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun miktarGuncelle(sepetUrun: SepetUrun, yeniAdet: Int) {
        val userId = auth.currentUser?.uid ?: return
        if (yeniAdet < 1) return
        database.child("sepet").child(userId).child(sepetUrun.urun_id)
            .child("adet").setValue(yeniAdet)
    }

    private fun urunuSil(sepetUrun: SepetUrun) {
        val userId = auth.currentUser?.uid ?: return
        database.child("sepet").child(userId).child(sepetUrun.urun_id).removeValue()
    }

    private fun bosSepetGoster(isBos: Boolean) {
        if (_binding == null) return
        binding.layoutBosSepet.visibility = if (isBos) View.VISIBLE else View.GONE
        binding.rvSepetim.visibility = if (isBos) View.GONE else View.VISIBLE
        binding.butonOdemeyeGec.isEnabled = !isBos
        binding.butonOdemeyeGec.alpha = if (isBos) 0.5f else 1.0f
        if (isBos) binding.textToplamFiyat.text = "0.00 ₺"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}