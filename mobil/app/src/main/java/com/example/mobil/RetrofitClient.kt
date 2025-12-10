package com.example.mobil

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

object RetrofitClient { //retrofiti kurup her yerde kullanabilmek için
    private const val BASE_URL = "https://api.exchangerate-api.com/" //api isteklerinin göderileceği ana adres

    val apiService : ApiService by lazy { // by lazy -> ilk kez çağrıldığında oluşturulacak, performans artışı
             Retrofit.Builder()   // yeni bir retrofit oluşturmak için "yapı kurucusunu" başlatır (retrofit kurulumunu başlatır)
            .baseUrl(BASE_URL)  // retrofite “api ile konuşacağın temel adres budur” diyorsun  //Bunun üzerine @GET("v4/latest/USD") kısmı ekleniyor
            .addConverterFactory(GsonConverterFactory.create())  //api den json geliyor, json u data classa çevirmek için converter ekleniyor
                                                                         // (yani retrofit json u çözmesi için)
            .build().create(ApiService::class.java)   // yukarıdaki yapılan tüm ayarlar biirleşip kullanıma hazır retrofit nesnesini oluşturur
    }
}