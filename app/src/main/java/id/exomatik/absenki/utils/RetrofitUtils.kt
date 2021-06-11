package id.exomatik.absenki.utils

import id.exomatik.absenki.model.ModelBarang
import id.exomatik.absenki.model.response.ModelResponseBarang
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitUtils{
    private val retrofit = Retrofit.Builder()
        .baseUrl(RetrofitApi.baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val api = retrofit.create(RetrofitApi::class.java)

    fun getBarang(callback: Callback<ModelResponseBarang>){
        val call = api.getBarang()
        call.enqueue(callback)
    }

    fun createBarang(dataUser: ModelBarang, callback: Callback<ModelResponseBarang>){
        val call = api.createBarang(dataUser.nama, dataUser.jenis, dataUser.harga)
        call.enqueue(callback)
    }

    fun updateBarang(dataUser: ModelBarang, callback: Callback<ModelResponseBarang>){
        val call = api.updateBarang(dataUser.id, dataUser.nama, dataUser.jenis, dataUser.harga)
        call.enqueue(callback)
    }

    fun deleteBarang(dataUser: ModelBarang, callback: Callback<ModelResponseBarang>){
        val call = api.deleteBarang(dataUser.id)
        call.enqueue(callback)
    }
}