package com.example.mobil

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.mobil.databinding.FragmentGirisBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlin.jvm.java

class GirisFragment : Fragment() {
    private var _binding: FragmentGirisBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGirisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        binding.girisYapButton.setOnClickListener {
            val email = binding.emailGirisEditText.text.toString().trim()
            val sifre = binding.passwordGirisEditText.text.toString().trim()

            if (email.isEmpty() || sifre.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.bos_alan_hatasi), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Çift tıklamayı önlemek için butonu pasif yap
            binding.girisYapButton.isEnabled = false
            binding.girisYapButton.text = "Giriş yapılıyor..."

            auth.signInWithEmailAndPassword(email, sifre).addOnCompleteListener { task ->
                // Fragment çökme koruması
                if (!isAdded) return@addOnCompleteListener

                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        val database = FirebaseDatabase.getInstance().reference

                        // Hesap Dondurulmuş mu?
                        database.child("users").child(uid).child("isFrozen").get()
                            .addOnSuccessListener { snapshot ->
                                val isFrozen = snapshot.getValue(Boolean::class.java) ?: false

                                if (isFrozen) {
                                    // hesap donmuşsa
                                    android.app.AlertDialog.Builder(requireContext())
                                        .setTitle("Hesap Dondurulmuş")
                                        .setMessage("Bu hesabı daha önce dondurmuşsunuz. Tekrar aktif etmek ister misiniz?")
                                        .setPositiveButton("Evet, Aktif Et") { _, _ ->

                                            // isFrozen = false yap
                                            database.child("users").child(uid).child("isFrozen")
                                                .setValue(false)
                                                .addOnSuccessListener {
                                                    Toast.makeText(requireContext(), "Hesabınız tekrar aktif edildi!", Toast.LENGTH_SHORT).show()

                                                    // Kilit kalktı, şimdi rolüne bakıp içeri alalım
                                                    roluKontrolEtVeYonlendir(database, uid)
                                                }
                                        }
                                        .setNegativeButton("Hayır, Çık") { _, _ ->
                                            auth.signOut()
                                            binding.girisYapButton.isEnabled = true
                                            binding.girisYapButton.text = getString(R.string.giris_yap)
                                        }
                                            .setCancelable(false).show()

                                } else {
                                    // hesap donmamışsa
                                    // Direkt rolüne bakıp içeri al
                                    roluKontrolEtVeYonlendir(database, uid)
                                }
                            }
                    }
                } else {
                    // Giriş hatalı
                    binding.girisYapButton.isEnabled = true
                    binding.girisYapButton.text = getString(R.string.giris_yap)
                    Toast.makeText(requireContext(), getString(R.string.giris_basarisiz), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Şifremi unuttum tıkla
        binding.sifremiUnuttumLink.setOnClickListener {
            findNavController().navigate(R.id.action_girisFragment_to_sifreYenileFragment)
        }

        // Kayıt ol tıkla
        binding.kaydolLink.setOnClickListener {
            findNavController().navigate(R.id.action_girisFragment_to_kayitFragment)
        }

    }

    private fun roluKontrolEtVeYonlendir(database: com.google.firebase.database.DatabaseReference, uid: String) {
        database.child("users").child(uid).child("role").get()
            .addOnSuccessListener { roleSnapshot ->
                val role = roleSnapshot.getValue(String::class.java)

                if (role == "admin") {
                    val intent = Intent(requireContext(), AdminActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                else {
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
            .addOnFailureListener {
                // Veri okunamadıysa standart kullanıcı gibi açalım ki çökmesin
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}