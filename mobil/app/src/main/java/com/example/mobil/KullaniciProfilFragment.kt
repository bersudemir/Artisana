package com.example.mobil

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mobil.databinding.FragmentKullaniciProfilBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class KullaniciProfilFragment : Fragment() {
    private var _binding: FragmentKullaniciProfilBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentKullaniciProfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Bilgileri Çek
        mevcutBilgileriGetir()

        // Butonları Ayarla
        setupButtons()
    }

    private fun setupButtons() {
        binding.butonGuncelle.setOnClickListener {
            profilGuncelle()
        }
        binding.btnSifreDegistirDialog.setOnClickListener {
            sifreGuncelleDialogGoster()
        }
        binding.butonSifreSifirla.setOnClickListener {
            sifreSifirlamaMailiGonder()
        }
        binding.butonDondur.setOnClickListener {
            hesapDondurDialogGoster()
        }
        binding.butonCikis.setOnClickListener {
            cikisYap()
        }

        // Instagram linki
        binding.layoutInstagram.setOnClickListener {
            val uri = android.net.Uri.parse("http://instagram.com/_u/deri_atolyesii")
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)

            // Instagram uygulaması yüklüyse direk onu açmaya çalış
            intent.setPackage("com.instagram.android")

            try {
                startActivity(intent)
            } catch (e: android.content.ActivityNotFoundException) {
                // Uygulama yüklü değilse, tarayıcıdan aç
                val webIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("http://instagram.com/deri_atolyesii"))
                startActivity(webIntent)
            }
        }
    }

    private fun mevcutBilgileriGetir() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            database.child("users").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("name").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java) ?: auth.currentUser?.email

                    binding.edittextAd.setText(name)
                    binding.edittextEmail.setText(email)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Veri çekilemedi: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun profilGuncelle() {
        val user = auth.currentUser
        val uid = user?.uid

        if (uid != null) {
            val yeniAdSoyad = binding.edittextAd.text.toString().trim()
            val yeniEmail = binding.edittextEmail.text.toString().trim()

            if (yeniAdSoyad.isEmpty() || yeniEmail.isEmpty()) {
                Toast.makeText(requireContext(), "Ad ve E-posta boş olamaz", Toast.LENGTH_SHORT).show()
                return
            }

            binding.butonGuncelle.isEnabled = false
            binding.butonGuncelle.text = "İşleniyor..."

            val guncelVeri = HashMap<String, Any>()
            guncelVeri["name"] = yeniAdSoyad

            // Veritabanını Güncelle
            database.child("users").child(uid).updateChildren(guncelVeri)
                .addOnSuccessListener {
                    // E-posta değişikliği varsa
                    if (yeniEmail != user.email) {
                        user.verifyBeforeUpdateEmail(yeniEmail).addOnSuccessListener {
                                Toast.makeText(requireContext(), "İsim güncellendi. Yeni e-posta için doğrulama linki gönderildi.", Toast.LENGTH_LONG).show()
                                // DB'de de maili güncelleyelim
                                database.child("users").child(uid).child("email").setValue(yeniEmail)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "E-posta hatası: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(requireContext(), "Bilgiler başarıyla güncellendi ✅", Toast.LENGTH_SHORT).show()
                    }

                    binding.butonGuncelle.isEnabled = true
                    binding.butonGuncelle.text = "BİLGİLERİ GÜNCELLE"
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Hata oluştu", Toast.LENGTH_SHORT).show()
                    binding.butonGuncelle.isEnabled = true
                    binding.butonGuncelle.text = "BİLGİLERİ GÜNCELLE"
                }
        }
    }

    private fun sifreGuncelleDialogGoster() {
        val builder = AlertDialog.Builder(requireContext())

        val view = layoutInflater.inflate(R.layout.dialog_sifre_guncelle, null)

        val editEski = view.findViewById<EditText>(R.id.editEskiSifre)
        val editYeni = view.findViewById<EditText>(R.id.editYeniSifre)

        builder.setView(view)
        builder.setPositiveButton("Güncelle") { _, _ ->
            val eskiSifre = editEski.text.toString().trim()
            val yeniSifre = editYeni.text.toString().trim()

            if (eskiSifre.isNotEmpty() && yeniSifre.isNotEmpty()) {
                if(yeniSifre.length >= 6) {
                    firebaseSifreDegistir(eskiSifre, yeniSifre)
                } else {
                    Toast.makeText(context, "Yeni şifre en az 6 karakter olmalı", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("İptal") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    private fun firebaseSifreDegistir(eskiSifre: String, yeniSifre: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email

        if (user != null && email != null) {
            //email doğrulaması güvenlik
            val credential = EmailAuthProvider.getCredential(email, eskiSifre)

            user.reauthenticate(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user.updatePassword(yeniSifre).addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            Toast.makeText(context, "Şifreniz başarıyla güncellendi! ✅", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Hata: ${updateTask.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Eski şifreniz hatalı, lütfen kontrol edin.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun sifreSifirlamaMailiGonder() {
        val email = auth.currentUser?.email
        if (email != null) {
            auth.sendPasswordResetEmail(email).addOnSuccessListener {
                AlertDialog.Builder(requireContext()).setTitle("Mail Gönderildi")
                    .setMessage("Şifre sıfırlama bağlantısı $email adresine gönderildi. Spam klasörünü kontrol etmeyi unutmayın.")
                    .setPositiveButton("Tamam", null).show()
            }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Hata: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun hesapDondurDialogGoster() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Hesabı Dondur")
        builder.setMessage("Hesabınızı dondurursanız, tekrar giriş yapana kadar hesabınız askıya alınır. Devam edilsin mi?")

        builder.setPositiveButton("Evet, Dondur") { _, _ ->
            val uid = auth.currentUser?.uid
            if (uid != null) {
                database.child("users").child(uid).child("isFrozen").setValue(true)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Hesabınız donduruldu.", Toast.LENGTH_SHORT).show()
                        cikisYap()
                    }
            }
        }
        builder.setNegativeButton("İptal", null)
        builder.show()
    }

    private fun cikisYap() {
        auth.signOut()
        val intent = Intent(requireActivity(), AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}