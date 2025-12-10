package com.example.mobil

//gelen veriyi temsil edecek sınıf
data class KurCevabi( val base: String,
                      val date: String,
                      val rates: Map<String, Double> )