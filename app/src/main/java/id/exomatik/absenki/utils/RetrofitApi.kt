package id.exomatik.absenki.utils

import id.exomatik.absenki.model.response.ModelResponseBarang
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by IrfanRZ on 02/08/2019.
 */
interface RetrofitApi {
    @Headers("Accept:application/json")
    @GET(Constant.reffGetBarang)
    fun getBarang(): Call<ModelResponseBarang>

    @Headers("Accept:application/json")
    @FormUrlEncoded
    @POST(Constant.reffCreateBarang)
    fun createBarang(
        @Field("nama") nama: String,
        @Field("jenis") jenis: String,
        @Field("harga") harga: Long
    ): Call<ModelResponseBarang>

    @Headers("Accept:application/json")
    @FormUrlEncoded
    @POST(Constant.reffUpdateBarang)
    fun updateBarang(
        @Field("id") id: Int,
        @Field("nama") nama: String,
        @Field("jenis") jenis: String,
        @Field("harga") harga: Long
    ): Call<ModelResponseBarang>

    @Headers("Accept:application/json")
    @FormUrlEncoded
    @POST(Constant.reffDeleteBarang)
    fun deleteBarang(
        @Field("id") id: Int
    ): Call<ModelResponseBarang>

    companion object {
        const val baseUrl = Constant.reffBaseURL
    }
}