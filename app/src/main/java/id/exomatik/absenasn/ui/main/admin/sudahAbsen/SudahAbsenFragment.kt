package id.exomatik.absenasn.ui.main.admin.sudahAbsen

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import id.exomatik.absenasn.R
import id.exomatik.absenasn.model.ModelAbsensi
import id.exomatik.absenasn.model.ModelHariKerja
import id.exomatik.absenasn.model.ModelUser
import id.exomatik.absenasn.utils.*
import kotlinx.android.synthetic.main.fragment_sudah_absen.view.*

class SudahAbsenFragment : Fragment() {
    private lateinit var savedData: DataSave
    private lateinit var v : View
    private var adapter: AdapterSudahAbsen? = null
    private val listData = ArrayList<ModelAbsensi>()
    private var dataHariKerja: ModelHariKerja? = null

    override fun onCreateView(paramLayoutInflater: LayoutInflater, paramViewGroup: ViewGroup?, paramBundle: Bundle?): View {
        v = paramLayoutInflater.inflate(R.layout.fragment_sudah_absen, paramViewGroup, false)

        savedData = DataSave(context)
        init()

        return v
    }

    private fun init() {
        initAdapter()

        getHariKerja(getDateNow(Constant.dateFormat1))

        v.swipeRefresh.setOnRefreshListener {
            listData.clear()
            adapter?.notifyDataSetChanged()
            getHariKerja(getDateNow(Constant.dateFormat1))
            v.swipeRefresh.isRefreshing = false
        }
    }

    private fun initAdapter() {
        adapter = AdapterSudahAbsen(
            listData, activity
        ) { dataAbsen: ModelAbsensi, dataUser: ModelUser? -> onClickItem(dataAbsen, dataUser) }
        v.rvData.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        v.rvData.adapter = adapter
    }

    private fun getHariKerja(indexKodeTanggal: String) {
        v.progress.visibility = View.VISIBLE

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                v.textStatus.text = result.message
                v.progress.visibility = View.GONE
            }

            @SuppressLint("SetTextI18n")
            override fun onDataChange(result: DataSnapshot) {
                v.progress.visibility = View.GONE
                if (result.exists()) {
                    for (snapshot in result.children) {
                        val data = snapshot.getValue(ModelHariKerja::class.java)

                        if (data != null && data.tanggalKerja == indexKodeTanggal){
                            dataHariKerja = data
                        }
                    }

                    val hariKerja = dataHariKerja
                    if (hariKerja != null && hariKerja.tanggalKerja == indexKodeTanggal){
                        getUserAlreadyAbsensi(hariKerja)
                    }
                    else{
                        v.textStatus.text = "Sekarang bukan hari kerja"
                    }
                }
                else{
                    v.textStatus.text = "Sekarang bukan hari kerja"
                }
            }
        }

        FirebaseUtils.searchDataWith1ChildObject(
            Constant.reffHariAbsen, Constant.indexTanggalKerja, indexKodeTanggal, valueEventListener
        )
    }

    fun getUserAlreadyAbsensi(hariKerja: ModelHariKerja) {
        val indexKodeHari = hariKerja.id
        v.progress.visibility = View.VISIBLE

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                v.textStatus.text = result.message
                v.progress.visibility = View.GONE
            }

            override fun onDataChange(result: DataSnapshot) {
                v.progress.visibility = View.GONE

                if (result.exists()) {
                    for (snapshot in result.children) {
                        val data = snapshot.getValue(ModelAbsensi::class.java)

                        if (data != null && data.id_hari == indexKodeHari){
                            listData.add(data)
                            adapter?.notifyDataSetChanged()
                        }
                    }
                }
                else{
                    v.textStatus.text = Constant.noDataAbsen
                }
            }
        }

        FirebaseUtils.searchDataWith1ChildObject(
            Constant.reffAbsensi, Constant.indexIDHari, indexKodeHari, valueEventListener
        )
    }

    @SuppressLint("SetTextI18n")
    private fun onClickItem(dataAbsen: ModelAbsensi, dataUser: ModelUser?) {
        if (dataUser == null){
            v.textStatus.text = "Error, gagal mendapatkan data pegawai"
        }
        else{
            showLog("detail absensi")
//            val bundle = Bundle()
//            val fragmentTujuan = DetailAbsensiFragment()
//            bundle.putParcelable(Constant.reffAbsensi, dataAbsen)
//            bundle.putParcelable(Constant.reffUser, dataUser)
//            bundle.putBoolean(Constant.request, true)
//            fragmentTujuan.arguments = bundle
//            navController.navigate(R.id.detailAbsensiFragment, bundle)
        }
    }
}