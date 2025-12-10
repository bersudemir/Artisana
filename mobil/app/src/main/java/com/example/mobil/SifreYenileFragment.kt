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
import com.example.mobil.databinding.FragmentSifreYenileBinding
import com.google.firebase.auth.FirebaseAuth

class SifreYenileFragment : Fragment() {
    private var _binding: FragmentSifreYenileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSifreYenileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        binding.sifreSifirlaButton.setOnClickListener {
            val email = binding.emailSifirlaEditText.text.toString().trim()

            //kontroller
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.bos_alan_hatasi), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(requireContext(), getString(R.string.gecersiz_eposta_hatasi), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.sifreSifirlaButton.isEnabled = false
            binding.sifreSifirlaButton.text = "Gönderiliyor..."

            // Firebase Şifre Sıfırlama İsteği
            auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                // Çökme koruması: kullanıcı sayfadan çıktıysa işlemi durdur
                if (!isAdded) return@addOnCompleteListener

                // Butonu eski haline getir
                binding.sifreSifirlaButton.isEnabled = true
                binding.sifreSifirlaButton.text = getString(R.string.sifre_sifirla)

                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Şifre sıfırlama bağlantısı e-postanıza gönderildi.", Toast.LENGTH_LONG).show()

                    // işlem başarılı kullanıcıyı giriş ekranına geri gönder
                    findNavController().navigate(R.id.action_sifreYenileFragment_to_girisFragment)
                } else {
                    Toast.makeText(requireContext(), "E-posta gönderilemedi. Lütfen adresi kontrol edin.", Toast.LENGTH_LONG).show()
                    Log.e("SifreYenile", "Hata: ${task.exception?.message}")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}