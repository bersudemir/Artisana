package com.example.mobil

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.mobil.databinding.FragmentAdminDashboardBinding

class AdminDashboard : Fragment() {
    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardUrun.setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboard_to_adminUrunYonetimi)
        }
        binding.cardKullanici.setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboard_to_adminKullaniciTakibi)
        }
        binding.cardSiparis.setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboard_to_adminSiparisTakibi)
        }
        binding.cardProfile.setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboard_to_adminProfil)
        }

        RetrofitClient.apiService.getRates().enqueue(object : retrofit2.Callback<KurCevabi> {

            override fun onResponse(
                call: retrofit2.Call<KurCevabi>,
                response: retrofit2.Response<KurCevabi>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()

                    val usdToTry = data?.rates?.get("TRY")

                    // TEXTVİEW'A YAZDIR
                    binding.txtKurDegeri.text = "USD/TRY: $usdToTry"
                }
            }

            override fun onFailure(call: retrofit2.Call<KurCevabi>, t: Throwable) {
                binding.txtKurDegeri.text = "Hata!"
            }
        })


    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Memory leak önlemek için binding'i temizledik
    }
}