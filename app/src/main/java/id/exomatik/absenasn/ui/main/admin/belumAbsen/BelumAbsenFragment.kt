package id.exomatik.absenasn.ui.main.admin.belumAbsen

import android.annotation.SuppressLint
import android.content.Intent
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
import id.exomatik.absenasn.model.ModelHariKerja
import id.exomatik.absenasn.model.ModelUser
import id.exomatik.absenasn.ui.main.admin.belumAbsen.detailPegawai.DetailPegawaiActivity
import id.exomatik.absenasn.utils.*
import kotlinx.android.synthetic.main.fragment_belum_absen.view.*

class BelumAbsenFragment : Fragment() {
    private lateinit var savedData: DataSave
    private lateinit var v : View
    val listData = ArrayList<ModelUser>()
    var adapter: AdapterBelumAbsen? = null
    private var dataHariKerja: ModelHariKerja? = null

    override fun onCreateView(paramLayoutInflater: LayoutInflater, paramViewGroup: ViewGroup?, paramBundle: Bundle?): View {
        v = paramLayoutInflater.inflate(R.layout.fragment_belum_absen, paramViewGroup, false)

        savedData = DataSave(context)
        init()

        return v
    }

    private fun init() {
        initAdapter()

        getHariKerja(getDateNow(Constant.dateFormat1))

        v.swipeRefresh.setOnRefreshListener {
            getHariKerja(getDateNow(Constant.dateFormat1))
            v.swipeRefresh.isRefreshing = false
        }
    }

    private fun initAdapter() {
        adapter = AdapterBelumAbsen(listData, activity) { dataData: ModelUser -> onClickItem(dataData) }
        v.rvData.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        v.rvData.adapter = adapter
    }

    private fun getHariKerja(indexKodeTanggal: String) {
        listData.clear()
        adapter?.notifyDataSetChanged()
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
                        getDataPegawai(hariKerja)
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

    private fun getDataPegawai(hariKerja: ModelHariKerja) {
        v.progress.visibility = View.VISIBLE

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                v.textStatus.text = result.message
                v.progress.visibility = View.GONE

                cekList()
            }

            override fun onDataChange(result: DataSnapshot) {
                v.progress.visibility = View.GONE
                if (result.exists()) {
                    for (snapshot in result.children) {
                        val data = snapshot.getValue(ModelUser::class.java)

                        if (data != null && data.status == Constant.statusActive
                            && data.jenisAkun == Constant.levelUser){
                            getUserNotAbsensi(hariKerja, data)
                        }
                    }

                    cekList()
                }
                else{
                    v.textStatus.text = Constant.noDataBelumAbsen
                }
            }
        }

        FirebaseUtils.searchDataWith1ChildObject(
            Constant.reffUser, Constant.status, Constant.statusActive, valueEventListener
        )
    }

    private fun getUserNotAbsensi(hariKerja: ModelHariKerja, dataUser: ModelUser) {
        val indexHariUsername = "${hariKerja.id}__${dataUser.username}"
        v.progress.visibility = View.VISIBLE

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                v.progress.visibility = View.GONE

                listData.add(dataUser)
                adapter?.notifyDataSetChanged()
                cekList()
            }

            override fun onDataChange(result: DataSnapshot) {
                v.progress.visibility = View.GONE

                if (!result.exists()) {
                    listData.add(dataUser)
                    adapter?.notifyDataSetChanged()
                }

                cekList()
            }
        }

        FirebaseUtils.searchDataWith1ChildObject(
            Constant.reffAbsensi, Constant.indexHariUsername, indexHariUsername, valueEventListener
        )
    }

    private fun cekList(){
        if (listData.size == 0){
            v.textStatus.text = Constant.noDataBelumAbsen
        }
        else{
            v.textStatus.text = ""
        }
    }

    private fun onClickItem(data: ModelUser) {
        val intent = Intent(activity, DetailPegawaiActivity::class.java)
        intent.putExtra(Constant.reffUser, data)
        intent.putExtra(Constant.reffHariAbsen, dataHariKerja)
        activity?.startActivity(intent)
        activity?.finish()
    }
}