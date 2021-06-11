package id.exomatik.absenki.ui.main.beranda

import android.annotation.SuppressLint
import android.content.Context
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.exomatik.absenki.base.BaseViewModel
import id.exomatik.absenki.model.ModelBarang
import id.exomatik.absenki.model.response.ModelResponseBarang
import id.exomatik.absenki.utils.Constant
import id.exomatik.absenki.utils.Constant.noData
import id.exomatik.absenki.utils.RetrofitUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@SuppressLint("StaticFieldLeak")
class BerandaViewModel(
    private val rcData: RecyclerView,
    private val context: Context?,
    private val navController: NavController
) : BaseViewModel() {
    val listData = ArrayList<ModelBarang>()
    val listDataSearch = ArrayList<ModelBarang>()
    val listNama = ArrayList<ModelBarang>()
    var adapter: AdapterDataBeranda? = null

    fun initAdapter() {
        adapter = AdapterDataBeranda(
            listData,
            { dataData: ModelBarang -> onClickItem(dataData) },
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

    private fun onClickItem(data: ModelBarang) {
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
        listData.clear()
        listDataSearch.clear()
        adapter?.notifyDataSetChanged()

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

                        val listResult = result.data
                        if (!listResult.isNullOrEmpty()){
                            for (i in listResult.indices){
                                listData.add(listResult[i])
                                listDataSearch.add(listResult[i])
                                adapter?.notifyDataSetChanged()
                            }
                        }
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