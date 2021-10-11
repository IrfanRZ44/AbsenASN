package id.exomatik.absenasn.ui.main.admin.hariAbsen

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionHelper
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RFACLabelItem
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RapidFloatingActionContentLabelList
import id.exomatik.absenasn.R
import id.exomatik.absenasn.model.ModelHariKerja
import id.exomatik.absenasn.utils.*
import kotlinx.android.synthetic.main.fragment_hari_absen.view.*
import java.text.SimpleDateFormat
import java.util.*

class HariAbsenFragment : Fragment() {
    private lateinit var savedData: DataSave
    private lateinit var v : View
    lateinit var alert : AlertDialog
    val listData = ArrayList<ModelHariKerja>()
    var adapter: AdapterHariAbsen? = null
    private val listJenisAbsen = ArrayList<String>()
    private lateinit var adapterJenisAbsen : SpinnerJenisAbsenAdapter
    private lateinit var spinnerJenisAbsen : AppCompatSpinner
    private lateinit var etTglKerja : TextInputLayout
    private lateinit var etTimeStart : TextInputLayout
    private lateinit var etTimeEnd : TextInputLayout
    private lateinit var textStatus : AppCompatTextView
    private lateinit var btnGetDate : AppCompatButton
    private lateinit var btnGetTimeStart : AppCompatButton
    private lateinit var btnGetTimeEnd : AppCompatButton
    private lateinit var btnBatal : AppCompatButton
    private lateinit var btnTambah : AppCompatButton

    override fun onCreateView(paramLayoutInflater: LayoutInflater, paramViewGroup: ViewGroup?, paramBundle: Bundle?): View {
        v = paramLayoutInflater.inflate(R.layout.fragment_hari_absen, paramViewGroup, false)

        savedData = DataSave(context)
        init()
        onClick()

        return v
    }

    private fun init() {
        initAdapter()
        activity?.let { it -> showDialogInput(it) }
        getDataHariKerja()

        v.swipeRefresh.setOnRefreshListener {
            v.swipeRefresh.isRefreshing = false
            getDataHariKerja()
        }
    }

    fun onClick(){
        floatingAction()
    }

    private fun floatingAction() {
        val rfaContent = RapidFloatingActionContentLabelList(context)
        val item = listOf(
            RFACLabelItem<Int>()
                .setLabel("Tambah Hari Absen")
                .setResId(R.drawable.ic_add_white)
                .setIconNormalColor(0xffd84315.toInt())
                .setIconPressedColor(0xffbf360c.toInt())
                .setWrapper(0)
        )

        rfaContent.setItems(item).setIconShadowColor(0xff888888.toInt())

        val rfabHelper = RapidFloatingActionHelper(
            context,
            v.rfaLayout,
            v.rfaBtn,
            rfaContent
        ).build()

        rfaContent.setOnRapidFloatingActionContentLabelListListener(object :
            RapidFloatingActionContentLabelList.OnRapidFloatingActionContentLabelListListener<Any> {
            override fun onRFACItemLabelClick(position: Int, item: RFACLabelItem<Any>?) {
                when(position) {
                    0 -> {
                        onClickTambah()
                    }
                }
                rfabHelper.toggleContent()
            }

            override fun onRFACItemIconClick(position: Int, item: RFACLabelItem<Any>?) {
                when(position) {
                    0 -> {
                        onClickTambah()
                    }
                }
                rfabHelper.toggleContent()
            }
        })
    }

    private fun initAdapter() {
        adapter = AdapterHariAbsen(listData
        ) { dataData: ModelHariKerja, position: Int -> onClickItemDelete(dataData, position) }
        v.rvData.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        v.rvData.adapter = adapter
        v.rvData.isNestedScrollingEnabled = false
    }

    private fun setAdapterJenisAbsen() {
        listJenisAbsen.clear()
        listJenisAbsen.add(Constant.jenisAbsen)
        listJenisAbsen.add(Constant.absen1)
        listJenisAbsen.add(Constant.absen2)
        listJenisAbsen.add(Constant.absen3)

        adapterJenisAbsen = SpinnerJenisAbsenAdapter(activity, listJenisAbsen, true)
        spinnerJenisAbsen.adapter = adapterJenisAbsen
    }

    private fun showDialogInput(activity: Activity){
        alert = AlertDialog.Builder(activity).create()
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_input_hari_absen, null)
        alert.setView(dialogView)

        spinnerJenisAbsen = dialogView.findViewById(R.id.spinnerJenisAbsen)
        etTglKerja = dialogView.findViewById(R.id.etTglKerja)
        etTimeStart = dialogView.findViewById(R.id.etTimeStart)
        etTimeEnd = dialogView.findViewById(R.id.etTimeEnd)
        textStatus = dialogView.findViewById(R.id.textStatus)
        btnGetDate = dialogView.findViewById(R.id.btnGetDate)
        btnGetTimeStart = dialogView.findViewById(R.id.btnGetTimeStart)
        btnGetTimeEnd = dialogView.findViewById(R.id.btnGetTimeEnd)
        btnBatal = dialogView.findViewById(R.id.btnBatal)
        btnTambah = dialogView.findViewById(R.id.btnTambah)

        setAdapterJenisAbsen()

        btnGetDate.setOnClickListener {
            alert.dismiss()
            getTanggalAbsen()
        }

        btnGetTimeStart.setOnClickListener {
            alert.dismiss()
            getTimeStart()
        }

        btnGetTimeEnd.setOnClickListener {
            alert.dismiss()
            getTimeEnd()
        }

        btnBatal.setOnClickListener {
            alert.dismiss()
            dismissKeyboard(activity)
        }
    }

    private fun getTanggalAbsen() {
        activity?.let { dismissKeyboard(it) }
        val datePickerDialog: DatePickerDialog
        val localCalendar = Calendar.getInstance()

        try {
            datePickerDialog = DatePickerDialog(
                activity ?: throw Exception("Error, mohon mulai ulang aplikasi"),
                { _, paramAnonymousInt1, paramAnonymousInt2, paramAnonymousInt3 ->
                    val dateSelected = Calendar.getInstance()
                    dateSelected[paramAnonymousInt1, paramAnonymousInt2] = paramAnonymousInt3
                    val dateFormatter1 = SimpleDateFormat(Constant.dateFormat1, Locale.US)
                    etTglKerja.editText?.setText(dateFormatter1.format(dateSelected.time))

                    alert.show()
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

    @SuppressLint("SimpleDateFormat")
    private fun getTimeStart() {
        val cal = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            etTimeStart.editText?.setText(SimpleDateFormat(Constant.timeFormat).format(cal.time))
            alert.show()
        }
        TimePickerDialog(
            context,
            timeSetListener,
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    @SuppressLint("SimpleDateFormat")
    private fun getTimeEnd() {
        val cal = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            etTimeEnd.editText?.setText(SimpleDateFormat(Constant.timeFormat).format(cal.time))
            alert.show()
        }
        TimePickerDialog(
            context,
            timeSetListener,
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun cekList() {
        v.progress.visibility = View.GONE

        if (listData.size == 0) v.textStatus.text = Constant.noDataHariAbsen
        else v.textStatus.text = ""
    }

    @SuppressLint("SetTextI18n")
    private fun onClickItemDelete(item: ModelHariKerja, position: Int) {
        val ctx = context

        if (ctx != null){
            val alertDelete = AlertDialog.Builder(ctx)
            alertDelete.setTitle(Constant.attention)
            alertDelete.setMessage(Constant.yakinDelete)
            alertDelete.setCancelable(true)
            alertDelete.setPositiveButton(
                Constant.iya
            ) { dialog, _ ->
                dialog.dismiss()
                deleteItem(item, position)
            }

            alertDelete.setNegativeButton(
                Constant.tidak
            ) { dialog, _ ->
                dialog.dismiss()
            }

            alertDelete.show()
        }
        else{
            v.textStatus.text = "Error, terjadi kesalahan yang tidak diketahui"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onClickTambah(){
        etTglKerja.error = null
        etTimeStart.error = null
        etTimeEnd.error = null
        textStatus.text = ""
        spinnerJenisAbsen.setSelection(0)
        etTglKerja.editText?.setText("")
        etTimeStart.editText?.setText("")
        etTimeEnd.editText?.setText("")
        etTglKerja.clearFocus()
        etTimeStart.clearFocus()
        etTimeEnd.clearFocus()
        alert.show()

        btnTambah.text = "Tambah"

        btnTambah.setOnClickListener {
            etTglKerja.error = null
            etTimeStart.error = null
            etTimeEnd.error = null
            activity?.let { it1 -> dismissKeyboard(it1) }

            val jenisAbsen = listJenisAbsen[spinnerJenisAbsen.selectedItemPosition]
            val tglKerja = etTglKerja.editText?.text.toString()
            val timeStart = etTimeStart.editText?.text.toString()
            val timeEnd = etTimeEnd.editText?.text.toString()
            val usernameOwner = savedData.getDataUser()?.username

            if (jenisAbsen.isNotEmpty() && jenisAbsen != Constant.jenisAbsen && tglKerja.isNotEmpty()
                && timeStart.isNotEmpty() && timeEnd.isNotEmpty()
                && !usernameOwner.isNullOrEmpty()){
                v.progress.visibility = View.VISIBLE
                val dateTimeNow = getDateNow(Constant.dateTimeFormat1)
                val tglSplit = tglKerja.split("-")

                val dataHariKerja = ModelHariKerja("", jenisAbsen, tglSplit[0], tglSplit[1], tglSplit[2],
                    tglKerja, timeStart, timeEnd, dateTimeNow, dateTimeNow)

                cekHariKerja(dataHariKerja)
                alert.dismiss()
            }
            else{
                when {
                    jenisAbsen.isEmpty() || jenisAbsen == Constant.jenisAbsen -> {
                        textStatus.text = "Mohon pilih salah satu jenis absen"
                    }
                    tglKerja.isEmpty() -> {
                        textStatus.text = "Tanggal tidak boleh kosong"
                        etTglKerja.requestFocus()
                        etTglKerja.findFocus()
                        etTglKerja.error = "Tanggal tidak boleh kosong"
                    }
                    timeStart.isEmpty() -> {
                        textStatus.text = "Jam Masuk tidak boleh kosong"
                        etTimeStart.requestFocus()
                        etTimeStart.findFocus()
                        etTimeStart.error = "Jam Masuk tidak boleh kosong"
                    }
                    timeEnd.isEmpty() -> {
                        textStatus.text = "Jam Pulang tidak boleh kosong"
                        etTimeEnd.requestFocus()
                        etTimeEnd.findFocus()
                        etTimeEnd.error = "Jam Pulang tidak boleh kosong"
                    }
                    usernameOwner.isNullOrEmpty() -> {
                        textStatus.text = "Error, terjadi kesalahan database"
                    }
                    else -> {
                        textStatus.text = "Error, terjadi kesalahan yang tidak diketahui"
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun deleteItem(item: ModelHariKerja, position: Int){
        v.progress.visibility = View.VISIBLE

        val onCompleteListener =
            OnCompleteListener<Void> { result ->
                v.progress.visibility = View.GONE
                if (result.isSuccessful) {
                    v.textStatus.text = "Berhasil menghapus data"
                    listData.removeAt(position)
                    adapter?.notifyItemRemoved(position)

                    cekList()
                } else {
                    v.textStatus.text = "Gagal menghapus data"
                }
            }

        val onFailureListener = OnFailureListener { result ->
            v.progress.visibility = View.GONE
            v.textStatus.text = result.message
        }

        FirebaseUtils.deleteValueWith1Child(Constant.reffHariAbsen, item.id, onCompleteListener, onFailureListener)
    }

    private fun cekHariKerja(dataHariKerja: ModelHariKerja) {
        v.progress.visibility = View.VISIBLE

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                v.progress.visibility = View.GONE
                addHariKerja(dataHariKerja)
            }

            @SuppressLint("SetTextI18n")
            override fun onDataChange(result: DataSnapshot) {
                v.progress.visibility = View.GONE
                if (result.exists()) {
                    textStatus.text = "Error, tanggal tersebut sudah memiliki jadwal absen"
                    etTglKerja.requestFocus()
                    etTglKerja.findFocus()
                    etTglKerja.error = "Error, tanggal tersebut sudah memiliki jadwal absen"
                    alert.show()
                } else {
                    addHariKerja(dataHariKerja)
                }
            }
        }
        FirebaseUtils.searchDataWith1ChildObject(
            Constant.reffHariAbsen, Constant.indexTanggalKerja, dataHariKerja.tanggalKerja, valueEventListener
        )
    }

    @SuppressLint("SetTextI18n")
    private fun addHariKerja(dataHariKerja: ModelHariKerja) {
        v.progress.visibility = View.VISIBLE

        val onCompleteListener =
            OnCompleteListener<Void> { result ->
                v.progress.visibility = View.GONE
                if (result.isSuccessful) {
                    listData.add(dataHariKerja)
                    adapter?.notifyDataSetChanged()
                    v.textStatus.text = ""
                    alert.dismiss()
                    Toast.makeText(activity, "Berhasil menambah hari kerja", Toast.LENGTH_LONG).show()
                    sortingJadwal()
                } else {
                    v.textStatus.text = "Gagal menambah hari kerja"
                }
            }

        val onFailureListener = OnFailureListener { result ->
            v.progress.visibility = View.GONE
            v.textStatus.text = result.message
        }

        FirebaseUtils.saveHariKerjaWithUnique1Child(
            Constant.reffHariAbsen, dataHariKerja, onCompleteListener, onFailureListener
        )
    }

    private fun getDataHariKerja() {
        listData.clear()
        adapter?.notifyDataSetChanged()
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
                        val data = snapshot.getValue(ModelHariKerja::class.java)

                        if (data != null){
                            listData.add(data)
                            adapter?.notifyDataSetChanged()
                        }

                        sortingJadwal()
                    }
                }
                else{
                    v.textStatus.text = Constant.noDataHariAbsen
                }
            }
        }

        FirebaseUtils.getDataObject(
            Constant.reffHariAbsen, valueEventListener
        )
    }

    @Suppress("DEPRECATION")
    private fun sortingJadwal() {
        val listSorted = listData.sortedWith(compareBy<ModelHariKerja> { it.tahun }.thenBy { it.bulan }.thenBy { it.tanggal }).reversed()
        listData.clear()
        adapter?.notifyDataSetChanged()

        for (i: Int in listSorted.indices) {
            listData.add(listSorted[i])
            adapter?.notifyDataSetChanged()
        }

        cekList()
    }
}

