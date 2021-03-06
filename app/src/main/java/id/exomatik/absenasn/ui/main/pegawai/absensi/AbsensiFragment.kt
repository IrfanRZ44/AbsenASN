package id.exomatik.absenasn.ui.main.pegawai.absensi

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import id.exomatik.absenasn.R
import id.exomatik.absenasn.model.ModelAbsensi
import id.exomatik.absenasn.model.ModelHariKerja
import id.exomatik.absenasn.model.ModelUser
import id.exomatik.absenasn.services.notification.model.Notification
import id.exomatik.absenasn.services.notification.model.Sender
import id.exomatik.absenasn.utils.*
import kotlinx.android.synthetic.main.activity_kirim_absen.*
import kotlinx.android.synthetic.main.fragment_absensi.view.*
import java.lang.Math.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

class AbsensiFragment : Fragment() {
    private lateinit var savedData: DataSave
    private lateinit var v : View
    private var dataHariAbsen: ModelHariKerja? = null
    private var idAbsensi : String? = ""
    private var latit = ""
    private var longit = ""
    private var cancellationSignal: CancellationSignal? = null
    private val  authenticationCallback: BiometricPrompt.AuthenticationCallback
        get() =
            @RequiresApi(Build.VERSION_CODES.P)
            object: BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    super.onAuthenticationError(errorCode, errString)
                    notifyUser("Authentikasi error: $errString")
                }
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                    super.onAuthenticationSucceeded(result)

                    notifyUser("Authentikasi Berhasil!")
                    navigateRequest()
                }
            }

    override fun onCreateView(paramLayoutInflater: LayoutInflater, paramViewGroup: ViewGroup?, paramBundle: Bundle?): View {
        v = paramLayoutInflater.inflate(R.layout.fragment_absensi, paramViewGroup, false)

        savedData = DataSave(context)
        init()
        onClick()

        return v
    }


    private fun init() {
        v.btnAbsen.isEnabled = false

        activity?.let { checkBiometricSupport(it) }
        getDataHariAbsen(getDateNow(Constant.dateFormat1))
    }

    private fun onClick(){
        v.swipeRefresh.setOnRefreshListener {
            v.swipeRefresh.isRefreshing = false
            getDataHariAbsen(getDateNow(Constant.dateFormat1))
        }

        v.btnAbsen.setOnClickListener {
            checkIMEI()
        }
    }

    @SuppressLint("HardwareIds", "SetTextI18n")
    private fun checkIMEI(){
        val androidId = Settings.Secure.getString(context?.contentResolver, Settings.Secure.ANDROID_ID)

        if (androidId == savedData.getDataUser()?.imei){
            onClickDatang()
        }
        else{
            v.textStatus.text = "Maaf, mohon menggunakan HP Anda sendiri untuk melakukan Absensi"
            Toast.makeText(context, "Maaf, mohon menggunakan HP Anda sendiri untuk melakukan Absensi", Toast.LENGTH_LONG).show()
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
            activity?.let { checkPermissionLocation(it) }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun checkPermissionLocation(act: Activity){
        if (ContextCompat.checkSelfPermission(act, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                act, Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            v.textStatus.text = "Anda belum mengizinkan akses lokasi aplikasi ini"

            ActivityCompat.requestPermissions(
                act,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                Constant.codeRequestLocation
            )
        } else {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(act)

            fusedLocationClient?.lastLocation?.addOnSuccessListener { location: Location? ->
                if (location?.latitude != null){

                    showLog(comparingLocation(location.latitude, location.longitude).toString() + " tess")
                    if (comparingLocation(location.latitude, location.longitude)
                        < savedData.getDataApps()?.batasJarak?.toDouble()?:Constant.defaultRadiusJarak) {
                        latit = location.latitude.toString()
                        longit = location.longitude.toString()
                        showFingerPrint(act)
                    } else{
                        v.progress.visibility = View.GONE
                        v.textStatus.text = "Error, pastikan Anda berada 100 meter dalam lingkup kampus UIN Alauddin Makassar"
                    }
                }
                else{
                    v.progress.visibility = View.GONE
                    v.textStatus.text = "Error, gagal mendapatkan lokasi terkini"
                }

            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constant.codeRequestStorage -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                activity?.let { checkPermissionLocation(it) }
            }
            Constant.codeRequestLocation -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                activity?.let { checkPermissionLocation(it) }
            }
            else ->
                v.textStatus.text = "Mohon izinkan penyimpanan, camera dan lokasi"
        }
    }

    private fun comparingLocation(lat2: Double, lng2: Double): Double {
//        val lat1 = Constant.defaultLatitudeUIN
//        val lng1 = Constant.defaultLongitudeUIN
        val lat1 = savedData.getDataApps()?.latitude?.toDouble()?:Constant.defaultLatitudeUIN
        val lng1 = savedData.getDataApps()?.longitude?.toDouble()?:Constant.defaultLongitudeUIN
        val earthRadius = 6371.0
        val dLat = toRadians(lat2 - lat1)
        val dLng = toRadians(lng2 - lng1)
        val sindLat = sin(dLat / 2)
        val sindLng = sin(dLng / 2)
        val a = sindLat.pow(2.0) + (sindLng.pow(2.0)
                * cos(toRadians(lat1)) * cos(toRadians(lat2)))
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c // output distance, in MILES
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

    @SuppressLint("SetTextI18n")
    private fun getDataAbsensi(hariAbsen: ModelHariKerja) {
        val indexHariUsername = "${hariAbsen.id}__${savedData.getDataUser()?.username}"
        v.progress.visibility = View.VISIBLE

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                val timeNow = getDateNow(Constant.timeFormat)
                v.textStatus.text = result.message
                v.progress.visibility = View.GONE
                if (comparingTimesAfter(hariAbsen.jamPulang, timeNow, false) &&
                    comparingTimesAfter(hariAbsen.jamMasuk, timeNow, true)){
                    v.btnAbsen.isEnabled = true
                    v.textKeterangan2.text = "Absen sudah dimulai, silahkan melakukan absensi"
                }
                else{
                    v.btnAbsen.isEnabled = false
                    v.textKeterangan2.text = "Absen belum dimulai"
                }
            }

            override fun onDataChange(result: DataSnapshot) {
                v.progress.visibility = View.GONE
                val timeNow = getDateNow(Constant.timeFormat)

                if (result.exists()) {
                    var izin = false
                    var masuk = false
                    var tempJenis = ""
                    var tempStatus = ""

                    for (snapshot in result.children) {
                        val data = snapshot.getValue(ModelAbsensi::class.java)

                        if (data != null && data.indexHariUsername == indexHariUsername){
                            tempJenis = data.jenis
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
                                        masuk = false
                                    }
                                    else{
                                        tempStatus = data.status
                                        masuk = true
                                    }
                                }
                            }
                        }
                    }

                    if (izin){
                        v.btnAbsen.isEnabled = false
                        v.textKeterangan2.text = "Anda sudah dikonfirmasi $tempJenis oleh Admin"
                    }
                    else if (!izin && !masuk){
                        if (comparingTimesAfter(hariAbsen.jamPulang, timeNow, false) &&
                            comparingTimesAfter(hariAbsen.jamMasuk, timeNow, true)){
                            v.btnAbsen.isEnabled = true
                            v.textKeterangan2.text = "Absen Anda sebelumnya ditolak oleh Admin, silahkan melakukan Absensi ulang"
                        }
                        else{
                            v.btnAbsen.isEnabled = false
                            v.textKeterangan2.text = "Absen Anda sebelumnya ditolak oleh Admin"
                        }
                    }
                    else if (!izin && masuk){
                        v.btnAbsen.isEnabled = false
                        if (tempStatus == Constant.statusActive){
                            v.textKeterangan2.text = "Anda sudah melakukan Absensi hari ini"
                        }
                        else{
                            v.textKeterangan2.text = "Anda sudah melakukan Absensi hari ini, silahkan menunggu konfirmasi Admin"
                        }
                    }
                    else{
                        if (comparingTimesAfter(hariAbsen.jamPulang, timeNow, false) &&
                            comparingTimesAfter(hariAbsen.jamMasuk, timeNow, true)){
                            v.btnAbsen.isEnabled = true
                            v.textKeterangan2.text = "Absen sudah dimulai, silahkan melakukan absensi"
                        }
                        else{
                            v.btnAbsen.isEnabled = false
                            v.textKeterangan2.text = "Absen belum dimulai"
                        }
                    }
                }
                else{
                    if (comparingTimesAfter(hariAbsen.jamPulang, timeNow, false) &&
                        comparingTimesAfter(hariAbsen.jamMasuk, timeNow, true)){
                        v.btnAbsen.isEnabled = true
                        v.textKeterangan2.text = "Absen sudah dimulai, silahkan melakukan absensi"
                    }
                    else{
                        v.btnAbsen.isEnabled = false
                        v.textKeterangan2.text = "Absen belum dimulai"
                    }
                }
            }
        }

        FirebaseUtils.searchDataWith1ChildObject(
            Constant.reffAbsensi, Constant.indexHariUsername, indexHariUsername, valueEventListener
        )
    }

    private fun showFingerPrint(act: Activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val biometricPrompt : BiometricPrompt = BiometricPrompt.Builder(act)
                .setTitle("Absen ASN")
                .setSubtitle("Authentikasi dibutuhkan")
                .setDescription("Fingerprint")
                .setNegativeButton("Batalkan", act.mainExecutor, { _, _ ->
                }).build()
            biometricPrompt.authenticate(getCancellationSignal(), act.mainExecutor, authenticationCallback)
        }
        else {
            notifyUser("Maaf, HP Anda belum support untuk melakukan authentikasi fingerprint")
        }
    }

    private fun navigateRequest(){
        if (latit.isNotEmpty() && longit.isNotEmpty()){
            progress.visibility = View.VISIBLE
            validateData(latit, longit)
        }
        else{
            notifyUser("Error, lokasi tidak ditemukan")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun validateData(latitude: String, longitude: String){
        progress.visibility = View.VISIBLE

        val dateCreated = getDateNow(Constant.dateFormat1)
        val dateTimeCreated = getDateNow(Constant.dateTimeFormat1)
        val timeCreated = getDateNow(Constant.timeFormat)
        val usernameUser = savedData.getDataUser()?.username
        val namaUser = savedData.getDataUser()?.nama
        val nip = savedData.getDataUser()?.nip
        val pangkatUser = savedData.getDataUser()?.pangkat
        val jabatanUser = savedData.getDataUser()?.jabatan
        val unitOrganisasiUser = savedData.getDataUser()?.unit_organisasi
        val hariId = dataHariAbsen?.id
        val jenis = dataHariAbsen?.jenisAbsen

        if (!usernameUser.isNullOrEmpty() && !hariId.isNullOrEmpty() && !jenis.isNullOrEmpty()
            && !namaUser.isNullOrEmpty() && !nip.isNullOrEmpty()
            && !pangkatUser.isNullOrEmpty() && !jabatanUser.isNullOrEmpty() && !unitOrganisasiUser.isNullOrEmpty()
        ){
            val tglSplit = dateCreated.split("-")
            val resultAbsen = ModelAbsensi("", usernameUser, hariId, namaUser, nip, pangkatUser, jabatanUser, unitOrganisasiUser,
                latitude, longitude, Constant.absenHadir, Constant.statusRequest,
                tglSplit[0], tglSplit[1], tglSplit[2], dateCreated, timeCreated,
                "${hariId}__${usernameUser}", dateTimeCreated, dateTimeCreated
            )

            val id = idAbsensi
            if (!id.isNullOrEmpty()){
                resultAbsen.id = id
                updateAbsensi(resultAbsen)
            }
            else{
                createAbsen(resultAbsen)
            }
        }
        else{
            when {
                usernameUser.isNullOrEmpty() -> {
                    progress.visibility = View.GONE
                    textStatus.text = "Error, terjadi kesalahan sistem database"
                }
                namaUser.isNullOrEmpty() -> {
                    progress.visibility = View.GONE
                    textStatus.text = "Error, nama user tidak ditemukan, silahkan login ulang"
                }
                nip.isNullOrEmpty() -> {
                    progress.visibility = View.GONE
                    textStatus.text = "Error, NIP/NIDN/ID tidak ditemukan, silahkan login ulang"
                }
                pangkatUser.isNullOrEmpty() -> {
                    progress.visibility = View.GONE
                    textStatus.text = "Error, pangkat user tidak ditemukan, silahkan login ulang"
                }
                jabatanUser.isNullOrEmpty() -> {
                    progress.visibility = View.GONE
                    textStatus.text = "Error, jabatan user tidak ditemukan, silahkan login ulang"
                }
                unitOrganisasiUser.isNullOrEmpty() -> {
                    progress.visibility = View.GONE
                    textStatus.text = "Error, unit kerja user tidak ditemukan, silahkan login ulang"
                }
                jenis.isNullOrEmpty() -> {
                    progress.visibility = View.GONE
                    textStatus.text = "Error, terjadi kesalahan sistem database"
                }
                else -> {
                    progress.visibility = View.GONE
                    textStatus.text = "Error, mohon login ulang"
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun createAbsen(dataAbsen: ModelAbsensi){
        progress.visibility = View.VISIBLE

        val onCompleteListener = OnCompleteListener<Void> { result ->
            progress.visibility = View.GONE
            if (result.isSuccessful) {
                textStatus.text = "Berhasil absensi"
                Toast.makeText(activity, "Berhasil absensi", Toast.LENGTH_LONG).show()

                sendNotification()
                v.swipeRefresh.isRefreshing = false
                latit = ""
                longit = ""
                getDataHariAbsen(getDateNow(Constant.dateFormat1))
            } else {
                textStatus.text = "Gagal absensi"
            }
        }

        val onFailureListener = OnFailureListener { result ->
            progress.visibility = View.GONE
            textStatus.text = result.message
        }

        FirebaseUtils.saveAbsensiWithUnique1Child(
            Constant.reffAbsensi, dataAbsen, onCompleteListener, onFailureListener
        )
    }

    @SuppressLint("SetTextI18n")
    private fun updateAbsensi(dataAbsen: ModelAbsensi){
        progress.visibility = View.VISIBLE

        val onCompleteListener =
            OnCompleteListener<Void> { result ->
                progress.visibility = View.GONE
                if (result.isSuccessful) {
                    textStatus.text = "Berhasil melakukan absensi ulang"
                    Toast.makeText(activity, "Berhasil melakukan absensi ulang", Toast.LENGTH_LONG).show()

                    sendNotification()
                    v.swipeRefresh.isRefreshing = false
                    latit = ""
                    longit = ""
                    getDataHariAbsen(getDateNow(Constant.dateFormat1))
                } else {
                    textStatus.text = "Gagal absensi"
                }
            }

        val onFailureListener = OnFailureListener { result ->
            progress.visibility = View.GONE
            textStatus.text = result.message
        }

        FirebaseUtils.setValueWith1ChildObject(
            Constant.reffAbsensi, dataAbsen.id, dataAbsen, onCompleteListener, onFailureListener
        )
    }

    private fun sendNotification(){
        val notification = Notification(
            "${savedData.getDataUser()?.nama} sudah melakukan absensi",
            "Absen ASN"
            , "id.exomatik.absenasn.fcm_TARGET_NOTIFICATION_ADMIN"
        )

        getDataAdmin(notification)
    }

    private fun getDataAdmin(notification: Notification){
        progress.visibility = View.VISIBLE

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                textStatus.text = result.message
                progress.visibility = View.GONE
            }

            override fun onDataChange(result: DataSnapshot) {
                progress.visibility = View.GONE
                if (result.exists()) {
                    for (snapshot in result.children) {
                        val data = snapshot.getValue(ModelUser::class.java)

                        val sender = Sender(notification, data?.token)
                        FirebaseUtils.sendNotif(sender)
                    }
                }
                else{
                    textStatus.text = Constant.noData
                }
            }
        }

        FirebaseUtils.searchDataWith1ChildObject(
            Constant.reffUser, Constant.reffJenisAkun, Constant.levelAdmin, valueEventListener
        )
    }

    private fun notifyUser(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }
    private fun getCancellationSignal(): CancellationSignal {
        cancellationSignal = CancellationSignal()
        cancellationSignal?.setOnCancelListener {
            notifyUser("Authentication was cancelled by the user")
        }
        return cancellationSignal as CancellationSignal
    }

    private fun checkBiometricSupport(act: Activity): Boolean {
        val keyguardManager : KeyguardManager = act.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if(!keyguardManager.isKeyguardSecure) {
            notifyUser("Fingerprint hs not been enabled in settings.")
            return false
        }
        if (ActivityCompat.checkSelfPermission(act, Manifest.permission.USE_BIOMETRIC) !=PackageManager.PERMISSION_GRANTED) {
            notifyUser("Fingerprint hs not been enabled in settings.")
            return false
        }
        return if (act.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            true
        } else true
    }
}