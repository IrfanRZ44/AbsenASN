@file:Suppress("DEPRECATION")

package id.exomatik.absenasn.ui.auth

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.request.CachePolicy
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import id.exomatik.absenasn.R
import id.exomatik.absenasn.model.ModelUser
import id.exomatik.absenasn.utils.*
import kotlinx.android.synthetic.main.activity_register.*
import java.text.SimpleDateFormat
import java.util.*

class RegisterAgainActivity : AppCompatActivity(){
    private lateinit var savedData : DataSave
    private var etFotoProfil : Uri? = null
    private var dataUser : ModelUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)
        myCodeHere()
    }

    private fun myCodeHere() {
        savedData = DataSave(this)

        supportActionBar?.hide()
        val data = intent.getParcelableExtra<ModelUser>(Constant.reffUser)
        if (data != null){
            dataUser = data
            setDataUser(data)
        }
        etTglLahir.editText?.keyListener = null

        onClick()
    }

    private fun onClick() {
        etNoHp.editText?.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (progress.visibility == View.GONE){
                    onClickRegister()
                }
                return@OnEditorActionListener false
            }
            false
        })

        btnSignUp.setOnClickListener {
            if (progress.visibility == View.GONE){
                onClickRegister()
            }
        }

        textLogin.setOnClickListener {
            dismissKeyboard(this)
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        cardFotoProfil.setOnClickListener {
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAllowFlipping(true)
                .setAllowRotation(true)
                .setAspectRatio(1, 1)
                .start(this)
        }

        btnTglLahir.setOnClickListener {
            getDateTglLahir()
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)

            if (resultCode == Activity.RESULT_OK){
                val imageUri = result.uri
                etFotoProfil = imageUri

                imgFotoProfil.load(imageUri) {
                    crossfade(true)
                    placeholder(R.drawable.ic_camera_white)
                    error(R.drawable.ic_camera_white)
                    fallback(R.drawable.ic_camera_white)
                    memoryCachePolicy(CachePolicy.ENABLED)
                }

                dataUser?.username?.let { saveFoto(imageUri, it) }
            }
        }
    }

    private fun setDataUser(data: ModelUser){
        etUsername.editText?.setText(data.username)
        etId.editText?.setText(data.nip)
        etUsername.visibility = View.GONE
        viewHaveAccount.visibility = View.GONE
        etUsername.editText?.isFocusable = false
        etNamaLengkap.editText?.setText(data.nama)
        etJabatan.editText?.setText(data.jabatan)
        etPangkat.editText?.setText(data.pangkat)
        etUnitOrganisasi.editText?.setText(data.unit_organisasi)
        etAlamat.editText?.setText(data.alamat)
        etTglLahir.editText?.setText(data.tanggalLahir)
        etTempatLahir.editText?.setText(data.tempatLahir)
        etNoHp.editText?.setText(data.phone.replaceFirst("+62", "0"))
        etFotoProfil = Uri.parse(data.fotoProfil)

        imgFotoProfil.load(data.fotoProfil) {
            crossfade(true)
            placeholder(R.drawable.ic_camera_white)
            error(R.drawable.ic_camera_white)
            fallback(R.drawable.ic_camera_white)
            memoryCachePolicy(CachePolicy.ENABLED)
        }

        if (data.jk == "Perempuan"){
            rgJK.check(R.id.rbJk2)
        }
        else{
            rgJK.check(R.id.rbJk1)
        }
    }

    @SuppressLint("SetTextI18n", "HardwareIds")
    fun onClickRegister(){
        dismissKeyboard(this)
        setNullError()

        val radio = rgJK.checkedRadioButtonId
        val btn = findViewById<RadioButton?>(radio)
        val jenisKelamin = btn?.text.toString()
        val username = etUsername.editText?.text.toString()
        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val id = etId.editText?.text.toString()
        val password = etPassword.editText?.text.toString()
        val confirmPassword = etConfirmPassword.editText?.text.toString()
        val namaLengkap = etNamaLengkap.editText?.text.toString()
        val tglLahir = etTglLahir.editText?.text.toString()
        val tempatLahir = etTempatLahir.editText?.text.toString()
        val alamat = etAlamat.editText?.text.toString()
        val jabatan = etJabatan.editText?.text.toString()
        val pangkat = etPangkat.editText?.text.toString()
        val unitOrganisasi = etUnitOrganisasi.editText?.text.toString()
        val noHp = etNoHp.editText?.text.toString()
        val fotoProfil = etFotoProfil
        val tglSekarang = getDateNow(Constant.dateTimeFormat1)

        if (username.isNotEmpty() && id.isNotEmpty() && password.isNotEmpty()
            && confirmPassword.isNotEmpty() && (password == confirmPassword)
            && password.length >= 6 && isContainNumber(password)
            && (isContainSmallText(password) || isContainBigText(password))
            && namaLengkap.isNotEmpty() && tglLahir.isNotEmpty() && tempatLahir.isNotEmpty()
            && fotoProfil != null && noHp.isNotEmpty() && alamat.isNotEmpty()
            && pangkat.isNotEmpty() && jabatan.isNotEmpty() && unitOrganisasi.isNotEmpty()
            && (noHp.length in 10..13) && radio > 0 && jenisKelamin.isNotEmpty()
        ) {
            val hp = noHp.replaceFirst("0", "+62")
            progress.visibility = View.VISIBLE

            val tempData = ModelUser(
                username, password, hp, androidId, "", namaLengkap,
                jenisKelamin, Constant.levelUser, id, tempatLahir, tglLahir, alamat, pangkat, jabatan, unitOrganisasi,
                Constant.statusRequest, "", dataUser?.fotoProfil?:"", tglSekarang,
                tglSekarang, tglSekarang
            )

            cekHandphone(tempData)
        }
        else{
            if (fotoProfil == null){
                textStatus.text = "Mohon upload foto profil"
            }
            else if (username.isEmpty()){
                setTextError("Error, mohon masukkan username", etUsername)
            }
            else if (password.isEmpty()){
                setTextError("Error, Mohon masukkan password", etPassword)
            }
            else if (password != confirmPassword){
                setTextError("Error, password yang Anda masukkan berbeda", etConfirmPassword)
            }
            else if (password.length < 6){
                setTextError("Error, password harus minimal 6 digit", etPassword)
            }
            else if (!isContainNumber(password)){
                setTextError("Error, password harus memiliki kombinasi angka", etPassword)
            }
            else if (!isContainSmallText(password) && !isContainBigText(password)){
                setTextError("Error, password harus memiliki kombinasi huruf", etPassword)
            }
            else if (namaLengkap.isEmpty()){
                setTextError("Error, Mohon masukkan nama lengkap", etNamaLengkap)
            }
            else if (jenisKelamin.isEmpty()){
                etNamaLengkap.clearFocus()
                etPassword.clearFocus()
                etConfirmPassword.clearFocus()
                etUsername.clearFocus()
                textStatus.text = "Error, Mohon pilih jenis kelamin"
            }
            else if (alamat.isEmpty()){
                setTextError("Error, mohon masukkan alamat", etAlamat)
            }
            else if (jabatan.isEmpty()){
                setTextError("Error, mohon masukkan jabatan", etJabatan)
            }
            else if (pangkat.isEmpty()){
                setTextError("Error, mohon masukkan pangkat", etPangkat)
            }
            else if (unitOrganisasi.isEmpty()){
                setTextError("Error, mohon masukkan unit kerja", etUnitOrganisasi)
            }
            else if (tempatLahir.isEmpty()){
                setTextError("Error, Mohon masukkan tempat lahir", etTempatLahir)
            }
            else if (tglLahir.isEmpty()){
                textStatus.text = "Error, Mohon pilih tanggal lahir"
                etNamaLengkap.clearFocus()
                etPassword.clearFocus()
                etConfirmPassword.clearFocus()
                etUsername.clearFocus()
                etTglLahir.requestFocus()
                etTglLahir.findFocus()
                etTglLahir.error = "Error, Mohon pilih tanggal lahir"
            }
            else if (noHp.isEmpty()){
                setTextError("Error, Mohon masukkan nomor HP yang valid", etNoHp)
            }
            else if (noHp.take(1) != "0"){
                setTextError("Error, mohon masukkan nomor HP dengan awalan 0", etNoHp)
            }
            else if (noHp.length !in 10..13){
                setTextError("Error, nomor HP  harus 10-13 digit", etNoHp)
            }
        }
    }

    private fun getDateTglLahir() {
        dismissKeyboard(this)
        val datePickerDialog: DatePickerDialog
        val localCalendar = Calendar.getInstance()

        try {
            datePickerDialog = DatePickerDialog(this,
                { _, paramAnonymousInt1, paramAnonymousInt2, paramAnonymousInt3 ->
                    val dateSelected = Calendar.getInstance()
                    dateSelected[paramAnonymousInt1, paramAnonymousInt2] = paramAnonymousInt3
                    val dateFormatter = SimpleDateFormat(Constant.dateFormat1, Locale.US)
                    etTglLahir.editText?.setText(dateFormatter.format(dateSelected.time))
                },
                localCalendar[Calendar.YEAR],
                localCalendar[Calendar.MONTH],
                localCalendar[Calendar.DATE]
            )

            datePickerDialog.show()
        } catch (e: java.lang.Exception) {
            textStatus.text = e.message
        }
    }

    private fun setNullError(){
        etUsername.error = null
        etPassword.error = null
        etConfirmPassword.error = null
        etNamaLengkap.error = null
        etTglLahir.error = null
        etTempatLahir.error = null
        etAlamat.error = null
        etNoHp.error = null
    }

    private fun setTextError(msg: String, editText: TextInputLayout){
        textStatus.text = msg
        editText.error = msg
        editText.requestFocus()
        editText.findFocus()
    }

    private fun cekHandphone(dataUser: ModelUser) {
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                if (dataUser.nip.isEmpty()){
                    addUserToFirebase(dataUser)
                }
                else{
                    cekID(dataUser)
                }
            }

            override fun onDataChange(result: DataSnapshot) {
                if (result.exists()) {
                    var tempData = ModelUser()
                    for (snapshot in result.children) {
                        val data = snapshot.getValue(ModelUser::class.java)
                        if(data != null){
                            tempData = data
                        }
                    }

                    if (tempData.phone == dataUser.phone){
                        if (dataUser.nip.isEmpty()){
                            addUserToFirebase(dataUser)
                        }
                        else{
                            cekID(dataUser)
                        }
                    }
                    else{
                        progress.visibility = View.GONE
                        setTextError("Gagal, No Handphone sudah terdaftar", etNoHp)
                    }
                } else {
                    if (dataUser.nip.isEmpty()){
                        addUserToFirebase(dataUser)
                    }
                    else{
                        cekID(dataUser)
                    }
                }
            }
        }

        FirebaseUtils.searchDataWith1ChildObject(
            Constant.reffUser, Constant.phone, dataUser.phone, valueEventListener
        )
    }

    private fun cekID(dataUser: ModelUser) {
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                addUserToFirebase(dataUser)
            }

            override fun onDataChange(result: DataSnapshot) {
                if (result.exists()) {
                    var tempData = ModelUser()
                    for (snapshot in result.children) {
                        val data = snapshot.getValue(ModelUser::class.java)
                        if(data != null){
                            tempData = data
                        }
                    }

                    if (tempData.nip == dataUser.nip){
                        addUserToFirebase(dataUser)
                    }
                    else{
                        progress.visibility = View.GONE
                        setTextError("Gagal, NIP/NIDN/ID sudah terdaftar", etId)
                    }
                } else {
                    addUserToFirebase(dataUser)
                }
            }
        }

        FirebaseUtils.searchDataWith1ChildObject(
            Constant.reffUser, Constant.nip, dataUser.nip, valueEventListener
        )
    }

    private fun saveFoto(image: Uri, username: String){
        val onSuccessListener = OnSuccessListener<UploadTask.TaskSnapshot> {
            getUrlFoto(it, username)
        }

        val onFailureListener = OnFailureListener {
            textStatus.text = it.message
            progress.visibility = View.GONE
        }

        FirebaseUtils.simpanFoto(Constant.reffFotoUser, username
            , image, onSuccessListener, onFailureListener)
    }

    private fun getUrlFoto(uploadTask: UploadTask.TaskSnapshot, username: String) {
        val onSuccessListener = OnSuccessListener<Uri?>{
            dataUser?.fotoProfil = it.toString()

            saveUrlFoto(it.toString(), username)
        }

        val onFailureListener = OnFailureListener {
            textStatus.text = it.message
            progress.visibility = View.GONE
        }

        FirebaseUtils.getUrlFoto(uploadTask, onSuccessListener, onFailureListener)
    }

    @SuppressLint("SetTextI18n")
    private fun addUserToFirebase(dataUser: ModelUser) {
        val md5Password = stringToMD5(dataUser.password)
        dataUser.password = md5Password

        val onCompleteListener =
            OnCompleteListener<Void> { result ->
                if (result.isSuccessful) {
                    dialogSucces()
                } else {
                    progress.visibility = View.GONE
                    textStatus.text = "Gagal menyimpan data user"
                }
            }

        val onFailureListener = OnFailureListener { result ->
            progress.visibility = View.GONE
            textStatus.text = result.message
        }
        FirebaseUtils.setValueObject(
            Constant.reffUser, dataUser.username, dataUser, onCompleteListener, onFailureListener
        )
    }

    @SuppressLint("SetTextI18n")
    private fun saveUrlFoto(urlFoto: String, username: String) {
        val onCompleteListener =
            OnCompleteListener<Void> { result ->
                if (result.isSuccessful) {
                    textStatus.text = "Berhasil menyimpan foto"
                } else {
                    progress.visibility = View.GONE
                    textStatus.text = "Gagal menyimpan foto"
                }
            }

        val onFailureListener = OnFailureListener { result ->
            progress.visibility = View.GONE
            textStatus.text = result.message
        }

        FirebaseUtils.setValueWith2ChildString(
            Constant.reffUser, username, "fotoProfil", urlFoto, onCompleteListener, onFailureListener
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun dialogSucces() {
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Pendaftaran Ulang Berhasil")
        alert.setMessage("Mohon tunggu proses verifikasi dalam waktu 1x24 jam")
        alert.setCancelable(false)

        alert.setPositiveButton(
            "Tutup"
        ) { dialog, _ ->
            dialog.dismiss()
            val intent = Intent(this, SplashActivity::class.java)
            startActivity(intent)
            finish()
        }

        alert.show()
    }
}