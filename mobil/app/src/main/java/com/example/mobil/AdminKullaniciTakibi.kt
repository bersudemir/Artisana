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
import com.example.mobil.databinding.FragmentAdminKullaniciTakibiBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*

class AdminKullaniciTakibi : Fragment() {
    private var _binding: FragmentAdminKullaniciTakibiBinding? = null
    private val binding get() = _binding!!
    private lateinit var userAdapter: UserAdapter
    private val userList = ArrayList<User>()
    private val database = FirebaseDatabase.getInstance().getReference("users")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminKullaniciTakibiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userAdapter = UserAdapter(
            userList,
            onUpdateClick = { user -> kullaniciGuncelleDialog(user) },
            onDeleteClick = { user -> kullaniciSilDialog(user) },
            onFreezeClick = { user -> kullaniciDondur(user) }
        )

        binding.rvKullanicilar.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userAdapter
        }

        kullanicilariGetir()

        // Geri Butonu
        binding.cardGeriBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun kullanicilariGetir() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return
                userList.clear()
                for (postSnapshot in snapshot.children) {
                    val user = postSnapshot.getValue(User::class.java)
                    if (user != null) {
                        userList.add(user)
                    }
                }
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Hata: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

private fun kullaniciGuncelleDialog(user: User) {
    val builder = AlertDialog.Builder(requireContext())

    // tasarımı bağla
    val view = layoutInflater.inflate(R.layout.admin_kullanici_guncelle, null)

    val etAd = view.findViewById<TextInputEditText>(R.id.etGuncelleAd)
    val etEmail = view.findViewById<TextInputEditText>(R.id.etGuncelleEmail)

    // mevcut verileri doldur
    etAd.setText(user.name)
    etEmail.setText(user.email)

    builder.setView(view)

    // butonları ekle
    builder.setPositiveButton("KAYDET", null) // Listener null, aşağıda tanımlayacağız
    builder.setNegativeButton("İPTAL") { dialog, _ -> dialog.dismiss() }

    val dialog = builder.create()
    dialog.show() // Göster


    // buton renkleri
    val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
    val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

    positiveButton.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.logoYesili))
    negativeButton.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.textSecondaryColor))
    positiveButton.typeface = android.graphics.Typeface.DEFAULT_BOLD

    // kaydetme
    positiveButton.setOnClickListener {
        val yeniIsim = etAd.text.toString().trim()
        val yeniEmail = etEmail.text.toString().trim()

        if (yeniIsim.isNotEmpty() && yeniEmail.isNotEmpty()) {
            val guncelVeriler = mapOf<String, Any>(
                "name" to yeniIsim,
                "email" to yeniEmail
            )

            database.child(user.uid).updateChildren(guncelVeriler)
                .addOnSuccessListener {
                    Toast.makeText(context, "Kullanıcı güncellendi ✅", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Hata: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Boş alan bırakmayınız.", Toast.LENGTH_SHORT).show()
        }
    }
}

    private fun kullaniciSilDialog(user: User) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Silme Onayı")
        builder.setMessage("${user.name} hesabını silmek istediğinize emin misiniz?")

        builder.setPositiveButton("EVET, SİL") { _, _ ->
            database.child(user.uid).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(context, "Kullanıcı silindi", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Hata oluştu", Toast.LENGTH_SHORT).show()
                }
        }
        builder.setNegativeButton("İPTAL") { dialog, _ -> dialog.dismiss() }

        val dialog = builder.create()
        dialog.show()


        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.errorColor))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.textSecondaryColor))
    }
    private fun kullaniciDondur(user: User) {
        // Veritabanından o anki durumu öğreniyoruz
        database.child(user.uid).child("isFrozen").get().addOnSuccessListener { snapshot ->

            // Veritabanından gelen değer (Yoksa false kabul et)
            val suankiDurum = snapshot.getValue(Boolean::class.java) ?: false

            // Dialog Kurulumu
            val builder = AlertDialog.Builder(requireContext())

            if (suankiDurum) {
                builder.setTitle("Hesabı Aktifleştir")
                builder.setMessage("${user.name} hesabının engelini kaldırmak istiyor musunuz?")
                builder.setPositiveButton("EVET, AÇ") { _, _ ->

                    val updateMap = HashMap<String, Any>()
                    updateMap["isFrozen"] = false

                    database.child(user.uid).updateChildren(updateMap)
                        .addOnSuccessListener { Toast.makeText(context, "Hesap açıldı ✅", Toast.LENGTH_SHORT).show() }
                }
            } else {
                builder.setTitle("Hesabı Dondur")
                builder.setMessage("${user.name} hesabını dondurmak istediğinize emin misiniz?")
                builder.setPositiveButton("EVET, DONDUR") { _, _ ->

                    val updateMap = HashMap<String, Any>()
                    updateMap["isFrozen"] = true

                    database.child(user.uid).updateChildren(updateMap)
                        .addOnSuccessListener { Toast.makeText(context, "Hesap donduruldu ❄️", Toast.LENGTH_SHORT).show() }
                }
            }

            builder.setNegativeButton("İPTAL") { dialog, _ -> dialog.dismiss() }

            val dialog = builder.create()
            dialog.show()

            val buttonColor = if (suankiDurum) R.color.logoYesili else R.color.errorColor
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), buttonColor))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.textSecondaryColor))

        }.addOnFailureListener {
            Toast.makeText(context, "Bağlantı hatası!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}