package com.example.mobil

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("v4/latest/USD")  //api nin yolunu verdik, get isteği yolluyoruz

    fun getRates(): Call<KurCevabi>  //geri dönecek veri tipi KurCevabi türünde olacak

}