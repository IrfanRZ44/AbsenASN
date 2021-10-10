package id.exomatik.absenasn.ui.main.pegawai.absensi

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import id.exomatik.absenasn.R
import id.exomatik.absenasn.model.ModelAbsensi
import id.exomatik.absenasn.model.ModelHariKerja
import id.exomatik.absenasn.ui.auth.ForgetPasswordActivity
import id.exomatik.absenasn.ui.main.pegawai.kirimAbsen.KirimAbsenActivity
import id.exomatik.absenasn.utils.*
import kotlinx.android.synthetic.main.fragment_absensi.view.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class AbsensiFragment : Fragment() {
    private lateinit var savedData: DataSave
    private lateinit var v : View
    private var dataHariAbsen: ModelHariKerja? = null
    private var idAbsensi : String? = ""
    private var urlFoto : String? = ""

    override fun onCreateView(paramLayoutInflater: LayoutInflater, paramViewGroup: ViewGroup?, paramBundle: Bundle?): View {
        v = paramLayoutInflater.inflate(R.layout.fragment_absensi, paramViewGroup, false)

        savedData = DataSave(context)
        init()
        onClick()

        return v
    }


    private fun init() {
        getDataHariAbsen(getDateNow(Constant.dateFormat1))
    }

    private fun onClick(){
        v.swipeRefresh.setOnRefreshListener {
            v.swipeRefresh.isRefreshing = false
            getDataHariAbsen(getDateNow(Constant.dateFormat1))
        }

        v.btnAbsen.setOnClickListener {
            onClickDatang()
        }
    }

    @Suppress("DEPRECATION")
    private fun checkPermissionStorage(ctx: Context){
        val permissionStorage = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(permissionStorage, Constant.codeRequestStorage)
        }
        else{
            checkPermissionCamera(ctx)
        }
    }

    @SuppressLint("SetTextI18n")
    @Suppress("DEPRECATION")
    private fun checkPermissionCamera(ctx: Context?){
        val permissionsCamera = arrayOf(Manifest.permission.CAMERA)

        if (ctx != null){
            if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissionsCamera, Constant.codeRequestCamera)
            }
            else{
                navigateRequest()
            }
        }
        else{
            v.textStatus.text = "Error, mohon mulai ulang aplikasi"
        }
    }

    @SuppressLint("SetTextI18n")
    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constant.codeRequestStorage -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissionCamera(context)
            }
            Constant.codeRequestCamera ->if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                navigateRequest()
            }
            else ->
                v.textStatus.text = "Mohon izinkan penyimpanan dan camera"

        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun comparingTimesAfter(waktuMulai: String, waktuDipilih: String, isAfter: Boolean) : Boolean{
        try {
            val time1 = SimpleDateFormat(Constant.timeFormat).parse(waktuMulai)
            val d = SimpleDateFormat(Constant.timeFormat).parse(waktuDipilih)

            val calendar1 = Calendar.getInstance()
            val calendar3 = Calendar.getInstance()

            return if (time1 != null && d != null){
                calendar1.time = time1
                calendar1.add(Calendar.DATE, 1)
                calendar3.time = d
                calendar3.add(Calendar.DATE, 1)

                val x = calendar3.time
                if (isAfter){
                    x.after(calendar1.time)
                }
                else{
                    x.before(calendar1.time)
                }
            } else{
                false
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            return false
        }
    }

    @SuppressLint("SetTextI18n")
    private fun checkGPS() : Boolean{
        val service = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        val enabled = service?.isProviderEnabled(LocationManager.GPS_PROVIDER)

        // Check if enabled and if not send user to the GPS settings
        return if (enabled != null && !enabled) {
            v.textStatus.text = "Nyalakan GPS terlebih dahulu"
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            activity?.startActivity(intent)
            false
        } else{
            true
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onClickDatang(){
        if (dataHariAbsen != null){
            if (checkGPS()){
                activity?.let { checkPermissionStorage(it) }
            }
        }
        else{
            v.textStatus.text = "Error, hari ini tidak tersedia untuk melakukan absensi"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getDataHariAbsen(tanggalKerja: String) {
        v.progress.visibility = View.VISIBLE

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                v.textStatus.text = result.message
                v.progress.visibility = View.GONE
                v.btnAbsen.isEnabled = false
                v.textKeterangan.text = "Sekarang bukan hari kerja"
            }

            override fun onDataChange(result: DataSnapshot) {
                v.progress.visibility = View.GONE
                if (result.exists()) {
                    for (snapshot in result.children) {
                        val data = snapshot.getValue(ModelHariKerja::class.java)

                        if (data != null && data.tanggalKerja == tanggalKerja){
                            dataHariAbsen = data
                        }
                    }

                    val hariAbsen = dataHariAbsen
                    if (hariAbsen != null && hariAbsen.tanggalKerja == tanggalKerja){
                        v.textKeterangan.text = "Hari ini adalah hari upacara dengan jam absen " +
                                "${hariAbsen.jamMasuk}-${hariAbsen.jamPulang}." +
                                "\n\n Catatan : Jika Anda tidak absen hingga jam selesai maka Anda tidak dapat " +
                                "absen dan dinyatakan Alfa atau tidak hadir"
                        getDataAbsensi(hariAbsen)
                    }
                    else{
                        v.textKeterangan.text = "Sekarang bukan hari upacara"
                        v.btnAbsen.isEnabled = false
                    }
                }
                else{
                    v.textKeterangan.text = "Sekarang bukan hari upacara"
                }
            }
        }

        FirebaseUtils.searchDataWith1ChildObject(
            Constant.reffHariAbsen, Constant.indexTanggalKerja, tanggalKerja, valueEventListener
        )
    }

    private fun getDataAbsensi(hariAbsen: ModelHariKerja) {
        val indexHariUsername = "${hariAbsen.id}__${savedData.getDataUser()?.username}"
        v.progress.visibility = View.VISIBLE

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                val timeNow = getDateNow(Constant.timeFormat)
                v.textStatus.text = result.message
                v.progress.visibility = View.GONE
                v.btnAbsen.isEnabled = comparingTimesAfter(hariAbsen.jamPulang, timeNow, false) &&
                        comparingTimesAfter(hariAbsen.jamMasuk, timeNow, true)
            }

            override fun onDataChange(result: DataSnapshot) {
                v.progress.visibility = View.GONE
                val timeNow = getDateNow(Constant.timeFormat)

                if (result.exists()) {
                    var izin = false
                    var masuk = false

                    for (snapshot in result.children) {
                        val data = snapshot.getValue(ModelAbsensi::class.java)

                        if (data != null && data.indexHariUsername == indexHariUsername){
                            if (data.jenis == Constant.absenAlpa) {
                                if (!izin) {
                                    izin = true
                                }
                            }
                            else if (data.jenis == Constant.absenCuti){
                                if (!izin){
                                    izin = true
                                }
                            }
                            else if (data.jenis == Constant.absenIzin){
                                if (!izin){
                                    izin = true
                                }
                            }
                            else if (data.jenis == Constant.absenSakit){
                                if (!izin){
                                    izin = true
                                }
                            }
                            else if (data.jenis == Constant.absenHadir){
                                if (!izin && !masuk){
                                    if (data.status == Constant.statusRejected){
                                        idAbsensi = data.id
                                        urlFoto = data.foto_absensi
                                        masuk = false
                                    }
                                    else{
                                        masuk = true
                                    }
                                }
                            }
                        }
                    }

                    if (izin){
                        v.btnAbsen.isEnabled = false
                    }
                    else if (!izin && !masuk){
                        v.btnAbsen.isEnabled = comparingTimesAfter(hariAbsen.jamPulang, timeNow, false) &&
                                comparingTimesAfter(hariAbsen.jamMasuk, timeNow, true)
                    }
                    else if (!izin && masuk){
                        v.btnAbsen.isEnabled = false
                    }
                    else{
                        v.btnAbsen.isEnabled = comparingTimesAfter(hariAbsen.jamPulang, timeNow, false) &&
                                comparingTimesAfter(hariAbsen.jamMasuk, timeNow, true)
                    }
                }
                else{
                    v.btnAbsen.isEnabled = comparingTimesAfter(hariAbsen.jamPulang, timeNow, false) &&
                            comparingTimesAfter(hariAbsen.jamMasuk, timeNow, true)
                }
            }
        }

        FirebaseUtils.searchDataWith1ChildObject(
            Constant.reffAbsensi, Constant.indexHariUsername, indexHariUsername, valueEventListener
        )
    }

    private fun navigateRequest(){
        val intent = Intent(activity, KirimAbsenActivity::class.java)
        intent.putExtra(Constant.idHari, dataHariAbsen?.id)
        intent.putExtra(Constant.idAbsen, idAbsensi)
        intent.putExtra(Constant.reffFotoUser, urlFoto)
        intent.putExtra(Constant.jenis, dataHariAbsen?.jenisAbsen)
        activity?.startActivity(intent)
        activity?.finish()
    }
}