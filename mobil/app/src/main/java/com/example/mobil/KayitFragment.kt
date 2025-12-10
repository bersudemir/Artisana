package com.example.mobil

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.mobil.databinding.FragmentKayitBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase

class KayitFragment : Fragment() {
    private var _binding: FragmentKayitBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentKayitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.KayitOlButton.setOnClickListener {
            kayitButonunaTiklandi()
        }

        binding.girisLink.setOnClickListener {
            findNavController().navigate(R.id.action_kayitFragment_to_girisFragment)
        }
    }

    private fun kayitButonunaTiklandi() {
        val name = binding.adSoyadKayitEditText.text.toString().trim()
        val email = binding.emailKayitEditText.text.toString().trim()
        val sifre = binding.passwordKayitEditText.text.toString().trim()

        if (!validateInputs(name, email, sifre)) return

        // Çift tıklamayı önlemek için butonu pasif yap
        binding.KayitOlButton.isEnabled = false
        binding.KayitOlButton.text = "Kaydediliyor..."

        kayitOlFirebase(email, sifre, name)
    }

    private fun validateInputs(name: String, email: String, sifre: String): Boolean {
        if (name.isEmpty() || email.isEmpty() || sifre.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.bos_alan_hatasi), Toast.LENGTH_SHORT).show()
            return false
        }
        if (sifre.length < 6) {
            Toast.makeText(requireContext(), getString(R.string.sifre_min_6_hatasi), Toast.LENGTH_SHORT).show()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), getString(R.string.gecersiz_eposta_hatasi), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun kayitOlFirebase(email: String, sifre: String, name: String) {
        auth.createUserWithEmailAndPassword(email, sifre).addOnCompleteListener { task ->
            // Çökme koruması için
            //Eğer Fragment artık ekranda değilse, devam eden işlemleri durduruyor ki uygulama çökmesin
            if (!isAdded) return@addOnCompleteListener

            if (task.isSuccessful) {
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    kayitBilgileriniDbKaydet(uid, name, email)
                } else {
                    butonlariSifirla()
                    Toast.makeText(requireContext(), "Kullanıcı ID alınamadı.", Toast.LENGTH_LONG).show()
                }
            } else {
                butonlariSifirla()
                if (task.exception is FirebaseAuthUserCollisionException) {
                    Toast.makeText(requireContext(), getString(R.string.eposta_zaten_kullanimda), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.kayit_basarisiz), Toast.LENGTH_LONG).show()
                    Log.e("KayitFragment", "Auth Hatası: ${task.exception?.message}")
                }
            }
        }
    }

    private fun kayitBilgileriniDbKaydet(uid: String, name: String, email: String) {
        val user = User(uid, name, email)

        val usersRef = database.reference.child("users").child(uid)

        usersRef.setValue(user).addOnCompleteListener { dbTask ->
            // Çökme koruması için
            if (!isAdded) return@addOnCompleteListener

            butonlariSifirla()

            if (dbTask.isSuccessful) {
                Toast.makeText(requireContext(), getString(R.string.kayit_basarili), Toast.LENGTH_SHORT).show()
                // Kayıt başarılı, giriş ekranına gönder
                findNavController().navigate(R.id.action_kayitFragment_to_girisFragment)
            } else {
                Toast.makeText(requireContext(), "Veritabanı hatası oluştu.", Toast.LENGTH_LONG).show()
                Log.e("KayitFragment", "DB Hatası: ${dbTask.exception?.message}")
            }
        }
    }

    private fun butonlariSifirla() {
        if (_binding != null) {
            binding.KayitOlButton.isEnabled = true
            binding.KayitOlButton.text = getString(R.string.kayit_ol)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}