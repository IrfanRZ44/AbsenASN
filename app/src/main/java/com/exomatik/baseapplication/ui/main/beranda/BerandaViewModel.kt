package com.exomatik.baseapplication.ui.main.beranda

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.exomatik.baseapplication.base.BaseViewModel
import com.exomatik.baseapplication.model.ModelBarang
import com.exomatik.baseapplication.model.ModelUser
import com.exomatik.baseapplication.model.response.ModelResponseBarang
import com.exomatik.baseapplication.utils.Constant
import com.exomatik.baseapplication.utils.Constant.noData
import com.exomatik.baseapplication.utils.RetrofitUtils
import com.exomatik.baseapplication.utils.showLog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@SuppressLint("StaticFieldLeak")
class BerandaViewModel(
    private val rcData: RecyclerView,
    private val context: Context?,
    private val navController: NavController
) : BaseViewModel() {
    val listData = ArrayList<ModelUser>()
    val listDataSearch = ArrayList<ModelUser>()
    val listNama = ArrayList<ModelUser>()
    var adapter: AdapterDataBeranda? = null

    fun initAdapter() {
        adapter = AdapterDataBeranda(
            listData,
            { dataData: ModelUser -> onClickItem(dataData) },
            navController
        )
        rcData.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rcData.adapter = adapter
        rcData.isNestedScrollingEnabled = false
    }

    fun cekList() {
        isShowLoading.value = false

        if (listData.size == 0) status.value = noData
        else status.value = ""
    }

    fun setData(){
        listData.clear()

        val tempData1 = ModelUser("", "", "", "", "",
            "Tes 2")

        listData.add(tempData1)
        listDataSearch.add(tempData1)
        listNama.add(tempData1)

        val tempData2 = ModelUser("", "", "", "", "",
            "Tes 1")

        listData.add(tempData2)
        listDataSearch.add(tempData2)
        listNama.add(tempData2)

        val tempData3 = ModelUser("", "", "", "", "",
            "Tes 10")

        listData.add(tempData3)
        listDataSearch.add(tempData3)
        listNama.add(tempData3)

        val tempData4 = ModelUser("", "", "", "", "",
            "Ada asdas")

        listData.add(tempData4)
        listDataSearch.add(tempData4)
        listNama.add(tempData4)

        val tempData5 = ModelUser("", "", "", "", "",
            "Base Application")

        listData.add(tempData5)
        listDataSearch.add(tempData5)
        listNama.add(tempData5)

        sortingData()
    }

    private fun sortingData() {
        val listSorted =
            listData.sortedWith(compareBy { it.nama })
        listData.clear()
        for (i: Int in listSorted.indices) {
            if (listData.size < 5) {
                listData.add(listSorted[i])
                adapter?.notifyDataSetChanged()
            }
        }

        cekList()
    }

    private fun onClickItem(data: ModelUser) {
        message.value = data.nama
    }

    fun createBarang(dataBarang: ModelBarang){
        RetrofitUtils.createBarang(dataBarang,
            object : Callback<ModelResponseBarang> {
                override fun onResponse(
                    call: Call<ModelResponseBarang>,
                    response: Response<ModelResponseBarang>
                ) {
                    isShowLoading.value = false
                    val result = response.body()

                    if (result?.message == Constant.reffSuccess){
                        message.value = "Berhasil membuat data"
                    }
                    else{
                        message.value = result?.message
                    }
                }

                override fun onFailure(
                    call: Call<ModelResponseBarang>,
                    t: Throwable
                ) {
                    isShowLoading.value = false
                    message.value = t.message
                }
            })
    }

    fun getBarang(){
        RetrofitUtils.getBarang(
            object : Callback<ModelResponseBarang> {
                override fun onResponse(
                    call: Call<ModelResponseBarang>,
                    response: Response<ModelResponseBarang>
                ) {
                    isShowLoading.value = false
                    val result = response.body()

                    if (result?.message == Constant.reffSuccess){
                        message.value = "Berhasil mengambil data"
                        showLog(result.data.toString())
                        Toast.makeText(context, result.data.toString(), Toast.LENGTH_LONG).show()
                    }
                    else{
                        message.value = result?.message
                    }
                }

                override fun onFailure(
                    call: Call<ModelResponseBarang>,
                    t: Throwable
                ) {
                    isShowLoading.value = false
                    message.value = t.message
                }
            })
    }
}