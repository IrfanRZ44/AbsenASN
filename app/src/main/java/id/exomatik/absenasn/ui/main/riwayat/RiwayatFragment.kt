package id.exomatik.absenasn.ui.main.riwayat

import android.annotation.SuppressLint
import android.app.DatePickerDialog
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
import id.exomatik.absenasn.model.ModelAbsensi
import id.exomatik.absenasn.model.ModelHariKerja
import id.exomatik.absenasn.model.ModelUser
import id.exomatik.absenasn.ui.main.admin.sudahAbsen.detailAbsensi.DetailAbsensiActivity
import id.exomatik.absenasn.utils.*
import kotlinx.android.synthetic.main.fragment_riwayat.view.*
import java.text.SimpleDateFormat
import java.util.*

class RiwayatFragment : Fragment() {
    private lateinit var savedData: DataSave
    private lateinit var v : View
    private val listData = ArrayList<ModelAbsensi>()
    private var adapter: AdapterRiwayat? = null
    private var dataHariKerja: ModelHariKerja? = null

    override fun onCreateView(paramLayoutInflater: LayoutInflater, paramViewGroup: ViewGroup?, paramBundle: Bundle?): View {
        v = paramLayoutInflater.inflate(R.layout.fragment_riwayat, paramViewGroup, false)

        savedData = DataSave(context)
        init()
        onClick()
        return v
    }

    private fun init() {
        v.etPickDate.editText?.keyListener = null

        initAdapter()

        val tglSelected = getDateNow(Constant.dateFormat1)
        v.etPickDate.editText?.setText(tglSelected)
        validateDataSearch(tglSelected)
    }

    private fun onClick() {
        v.btnPickDate.setOnClickListener {
            getPickDate()
        }

        v.swipeRefresh.setOnRefreshListener {
            v.swipeRefresh.isRefreshing = false
            listData.clear()
            adapter?.notifyDataSetChanged()
            val tglSelected = getDateNow(Constant.dateFormat1)
            v.etPickDate.editText?.setText(tglSelected)
            validateDataSearch(tglSelected)
        }
    }

    private fun initAdapter() {
        adapter = AdapterRiwayat(
            listData,
            { dataAbsen: ModelAbsensi, dataUser: ModelUser? -> onClickItem(dataAbsen, dataUser) },
            activity
        )
        v.rvData.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        v.rvData.adapter = adapter
        v.rvData.isNestedScrollingEnabled = true
    }

    private fun getPickDate() {
        activity?.let { dismissKeyboard(it) }
        val datePickerDialog: DatePickerDialog
        val localCalendar = Calendar.getInstance()

        try {
            datePickerDialog = DatePickerDialog(
                activity ?: throw Exception("Error, mohon mulai ulang aplikasi"),
                { _, paramAnonymousInt1, paramAnonymousInt2, paramAnonymousInt3 ->
                    val dateSelected = Calendar.getInstance()
                    dateSelected[paramAnonymousInt1, paramAnonymousInt2] = paramAnonymousInt3
                    val dateFormatter = SimpleDateFormat(Constant.dateFormat1, Locale.US)
                    val tglSelected = dateFormatter.format(dateSelected.time)

                    v.etPickDate.editText?.setText(tglSelected)
                    listData.clear()
                    adapter?.notifyDataSetChanged()
                    validateDataSearch(tglSelected)
                },
                localCalendar[Calendar.YEAR],
                localCalendar[Calendar.MONTH],
                localCalendar[Calendar.DATE]
            )

            datePickerDialog.show()
        } catch (e: java.lang.Exception) {
            v.textStatus.text = e.message
        }
    }

    @SuppressLint("SetTextI18n")
    private fun validateDataSearch(tglSelected: String){
        val usernameUser = savedData.getDataUser()?.username

        if (savedData.getDataUser()?.jenisAkun == Constant.levelAdmin){
            getHariKerjaOwner(tglSelected)
        }
        else if (savedData.getDataUser()?.jenisAkun == Constant.levelUser && !usernameUser.isNullOrEmpty()){
            getHariKerjaUser(tglSelected, usernameUser)
        }
        else{
            v.textStatus.text = "Error, terjadi kesalahan database, mohon login ulang"
        }
    }

    private fun getHariKerjaOwner(tanggal: String) {
        v.progress.visibility = View.VISIBLE

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                v.textStatus.text = result.message
                v.progress.visibility = View.GONE
                dataHariKerja = null
            }

            @SuppressLint("SetTextI18n")
            override fun onDataChange(result: DataSnapshot) {
                v.progress.visibility = View.GONE
                if (result.exists()) {
                    for (snapshot in result.children) {
                        val data = snapshot.getValue(ModelHariKerja::class.java)

                        if (data != null && data.tanggalKerja == tanggal){
                            dataHariKerja = data
                        }
                    }

                    val hariKerja = dataHariKerja
                    if (hariKerja != null && hariKerja.tanggalKerja == tanggal){
                        v.textStatus.text = ""
                        getAbsensiOwner(hariKerja)
                    }
                    else{
                        v.textStatus.text = "$tanggal bukan hari absen"
                    }
                }
                else{
                    dataHariKerja = null
                    v.textStatus.text = "$tanggal bukan hari absen"
                }
            }
        }

        FirebaseUtils.searchDataWith1ChildObject(
            Constant.reffHariAbsen, Constant.indexTanggalKerja, tanggal, valueEventListener
        )
    }

    private fun getHariKerjaUser(tanggal: String, username: String) {
        v.progress.visibility = View.VISIBLE

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                v.textStatus.text = result.message
                v.progress.visibility = View.GONE
                dataHariKerja = null
            }

            @SuppressLint("SetTextI18n")
            override fun onDataChange(result: DataSnapshot) {
                v.progress.visibility = View.GONE
                if (result.exists()) {
                    for (snapshot in result.children) {
                        val data = snapshot.getValue(ModelHariKerja::class.java)

                        if (data != null && data.tanggalKerja == tanggal){
                            dataHariKerja = data
                        }
                    }

                    val hariKerja = dataHariKerja
                    if (hariKerja != null && hariKerja.tanggalKerja == tanggal){
                        v.textStatus.text = ""
                        getAbsensiUser(hariKerja, username)
                    }
                    else{
                        v.textStatus.text = "$tanggal bukan hari absen"
                    }
                }
                else{
                    dataHariKerja = null
                    v.textStatus.text = "$tanggal bukan hari absen"
                }
            }
        }

        FirebaseUtils.searchDataWith1ChildObject(
            Constant.reffHariAbsen, Constant.indexTanggalKerja, tanggal, valueEventListener
        )
    }

    private fun getAbsensiOwner(hariKerja: ModelHariKerja) {
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
                    v.textStatus.text = ""
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

    private fun getAbsensiUser(hariKerja: ModelHariKerja, username: String) {
        val indexKodeHariUsername = "${hariKerja.id}__$username"
        v.progress.visibility = View.VISIBLE

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                v.textStatus.text = result.message
                v.progress.visibility = View.GONE
            }

            override fun onDataChange(result: DataSnapshot) {
                v.progress.visibility = View.GONE

                if (result.exists()) {
                    v.textStatus.text = ""
                    for (snapshot in result.children) {
                        val data = snapshot.getValue(ModelAbsensi::class.java)

                        if (data != null && data.indexHariUsername == indexKodeHariUsername){
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
            Constant.reffAbsensi, Constant.indexHariUsername, indexKodeHariUsername, valueEventListener
        )
    }

    @SuppressLint("SetTextI18n")
    private fun onClickItem(dataAbsen: ModelAbsensi, dataUser: ModelUser?) {
        if (dataUser == null){
            v.textStatus.text = "Error, gagal mendapatkan data pegawai"
        }
        else{
            val intent = Intent(activity, DetailAbsensiActivity::class.java)
            intent.putExtra(Constant.reffAbsensi, dataAbsen)
            intent.putExtra(Constant.reffUser, dataUser)
            intent.putExtra(Constant.request, false)
            activity?.startActivity(intent)
            activity?.finish()
        }
    }
}