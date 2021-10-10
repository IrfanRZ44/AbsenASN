package id.exomatik.absenasn.ui.main.admin.daftarPegawai

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
import id.exomatik.absenasn.model.ModelUser
import id.exomatik.absenasn.utils.Constant
import id.exomatik.absenasn.utils.DataSave
import id.exomatik.absenasn.utils.FirebaseUtils
import kotlinx.android.synthetic.main.fragment_daftar_pegawai.view.*

class DaftarPegawaiFragment : Fragment() {
    private lateinit var savedData: DataSave
    private lateinit var v : View
    private val listData = ArrayList<ModelUser>()
    private var adapter: AdapterDaftarPegawai? = null

    override fun onCreateView(paramLayoutInflater: LayoutInflater, paramViewGroup: ViewGroup?, paramBundle: Bundle?): View {
        v = paramLayoutInflater.inflate(R.layout.fragment_daftar_pegawai, paramViewGroup, false)

        savedData = DataSave(context)
        myCodeHere()

        return v
    }

    private fun myCodeHere() {
        initAdapter()

        getDaftarPegawai()

        v.swipeRefresh.setOnRefreshListener {
            listData.clear()
            adapter?.notifyDataSetChanged()
            v.swipeRefresh.isRefreshing = false
            getDaftarPegawai()
        }
    }

    private fun initAdapter() {
        adapter = AdapterDaftarPegawai(listData, activity)
        v.rcRequest.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        v.rcRequest.adapter = adapter
        v.rcRequest.isNestedScrollingEnabled = false
    }

    private fun cekList() {
        v.progress.visibility = View.GONE

        if (listData.size == 0) v.textStatus.text = Constant.noDataPegawaiVerified
        else v.textStatus.text = ""
    }

    private fun getDaftarPegawai() {
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
                        val data = snapshot.getValue(ModelUser::class.java)

                        if (data != null && data.jenisAkun == Constant.levelUser){
                            listData.add(data)
                            adapter?.notifyDataSetChanged()
                        }
                    }

                    cekList()
                }
                else{
                    v.textStatus.text = Constant.noDataPegawai
                }
            }
        }

        FirebaseUtils.searchDataWith1ChildObject(
            Constant.reffUser, Constant.status, Constant.statusActive, valueEventListener
        )
    }
}