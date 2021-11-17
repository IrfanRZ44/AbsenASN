@file:Suppress("DEPRECATION")

package id.exomatik.absenasn.ui.auth

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings.Secure
import android.telephony.TelephonyManager
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.load
import coil.request.CachePolicy
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
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


class RegisterActivity : AppCompatActivity(){
    private var etFotoProfil : Uri? = null
    private var unverify = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)
        myCodeHere()
    }

    private fun myCodeHere() {
        supportActionBar?.hide()
        val dataUser = intent.getParcelableExtra<ModelUser>(Constant.reffUser)
        if (dataUser != null){
            setDataUser(
                intent.getParcelableExtra(Constant.reffFotoUser), dataUser
            )
        }
        etTglLahir.editText?.keyListener = null

        onClick()
    }

    private fun onClick() {
        etNoHp.editText?.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onClickRegister()
                return@OnEditorActionListener false
            }
            false
        })

        btnSignUp.setOnClickListener {
            onClickRegister()
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
            }
        }
    }

    private fun setDataUser(fotoProfil: Uri?, data: ModelUser){
        etUsername.editText?.setText(data.username)
        etId.editText?.setText(data.nip)
        etPassword.editText?.setText(data.password)
        etConfirmPassword.editText?.setText(data.password)
        etNamaLengkap.editText?.setText(data.nama)
        etAlamat.editText?.setText(data.alamat)
        etJabatan.editText?.setText(data.jabatan)
        etPangkat.editText?.setText(data.pangkat)
        etUnitOrganisasi.editText?.setText(data.unit_organisasi)
        etTglLahir.editText?.setText(data.tanggalLahir)
        etTempatLahir.editText?.setText(data.tempatLahir)
        etNoHp.editText?.setText(data.phone.replaceFirst("+62", "0"))
        etFotoProfil = fotoProfil

        imgFotoProfil.load(fotoProfil) {
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
        val androidId = Secure.getString(contentResolver, Secure.ANDROID_ID)
        val id = etId.editText?.text.toString()
        val password = etPassword.editText?.text.toString()
        val confirmPassword = etConfirmPassword.editText?.text.toString()
        val namaLengkap = etNamaLengkap.editText?.text.toString()
        val tglLahir = etTglLahir.editText?.text.toString()
        val tempatLahir = etTempatLahir.editText?.text.toString()
        val alamat = etAlamat.editText?.text.toString()
        val pangkat = etPangkat.editText?.text.toString()
        val jabatan = etJabatan.editText?.text.toString()
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

            val dataUser = ModelUser(
                username,
                password,
                hp,
                androidId,
                "",
                namaLengkap,
                jenisKelamin,
                Constant.levelUser,
                id,
                tempatLahir,
                tglLahir,
                alamat,
                pangkat,
                jabatan,
                unitOrganisasi,
                Constant.statusRequest,
                "",
                "",
                tglSekarang,
                tglSekarang,
                tglSekarang
            )

            cekUserName(dataUser)
        }
        else{
            if (fotoProfil == null){
                textStatus.text = "Mohon upload foto profil"
            }
            else if (username.isEmpty()){
                setTextError("Error, mohon masukkan username", etUsername)
            }
            else if (id.isEmpty()){
                setTextError("Error, mohon masukkan NIP/NIDN/ID", etId)
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
            else if (pangkat.isEmpty()){
                setTextError("Error, mohon masukkan pangkat", etPangkat)
            }
            else if (jabatan.isEmpty()){
                setTextError("Error, mohon masukkan jabatan", etJabatan)
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
            datePickerDialog = DatePickerDialog(
                this,
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

    private fun cekUserName(dataUser: ModelUser) {
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                cekHandphone(dataUser)
            }

            override fun onDataChange(result: DataSnapshot) {
                if (result.exists()) {
                    progress.visibility = View.GONE
                    setTextError("Gagal, Username sudah digunakan", etUsername)
                } else {
                    cekHandphone(dataUser)
                }
            }
        }
        FirebaseUtils.searchDataWith1ChildObject(
            Constant.reffUser, Constant.username, dataUser.username, valueEventListener
        )
    }

    private fun cekHandphone(dataUser: ModelUser) {
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                cekID(dataUser)
            }

            override fun onDataChange(result: DataSnapshot) {
                if (result.exists()) {
                    progress.visibility = View.GONE
                    setTextError("Gagal, No Handphone sudah terdaftar", etNoHp)
                } else {
                    cekID(dataUser)
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
                signUp(dataUser)
            }

            override fun onDataChange(result: DataSnapshot) {
                if (result.exists()) {
                    progress.visibility = View.GONE
                    setTextError("Gagal, NIP/NIDN/ID sudah terdaftar", etId)
                } else {
                    signUp(dataUser)
                }
            }
        }

        FirebaseUtils.searchDataWith1ChildObject(
            Constant.reffUser, Constant.nip, dataUser.nip, valueEventListener
        )
    }

    private fun signUp(dataUser: ModelUser) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                if (unverify) {
                    signIn(credential, dataUser)
                }
                unverify = false
            }

            @SuppressLint("SetTextI18n")
            override fun onVerificationFailed(e: FirebaseException) {
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        textStatus.text = "Error, Nomor Handphone tidak Valid"
                    }
                    is FirebaseTooManyRequestsException -> {
                        textStatus.text =
                            "Error, Anda sudah terlalu banyak mengirimkan permintaan coba lagi nanti"
                    }
                    else -> {
                        textStatus.text = e.message
                    }
                }
                progress.visibility = View.GONE
            }

            @Suppress("DEPRECATION")
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                object : CountDownTimer(8000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                    }

                    override fun onFinish() {
                        if (unverify) {
                            progress.visibility = View.GONE
                            moveVerifyRegister(verificationId, dataUser)
                        }
                    }
                }.start()
            }
        }

        try {
            FirebaseUtils.registerUser(
                dataUser.phone,
                callbacks, this
            )
        } catch (e: Exception) {
            textStatus.text = e.message
            progress.visibility = View.GONE
        }
    }

    private fun moveVerifyRegister(verificationId: String, dataUser: ModelUser){
        val intent = Intent(this, VerifyRegisterActivity::class.java)
        intent.putExtra("verifyId", verificationId)
        intent.putExtra("auth", true)
        intent.putExtra("dataUser", dataUser)
        intent.putExtra(Constant.reffFotoUser, etFotoProfil)
        startActivity(intent)
        unverify = false
        finish()
    }

    @SuppressLint("SetTextI18n")
    fun signIn(credential: AuthCredential, dataUser: ModelUser) {
        progress.visibility = View.VISIBLE

        val onCoCompleteListener =
            OnCompleteListener<AuthResult> { task ->
                if (task.isSuccessful) {
                    etFotoProfil?.let { saveFoto(it, dataUser) }
                } else {
                    progress.visibility = View.GONE
                    textStatus.text = "Gagal masuk ke Akun Anda"
                }
            }

        FirebaseUtils.signIn(credential, onCoCompleteListener)
    }

    private fun saveFoto(image: Uri, dataUser: ModelUser){
        val onSuccessListener = OnSuccessListener<UploadTask.TaskSnapshot> {
            getUrlFoto(it, dataUser)
        }

        val onFailureListener = OnFailureListener {
            textStatus.text = it.message
            progress.visibility = View.GONE
        }

        FirebaseUtils.simpanFoto(
            Constant.reffFotoUser, dataUser.username, image, onSuccessListener, onFailureListener
        )
    }

    private fun getUrlFoto(uploadTask: UploadTask.TaskSnapshot, dataUser: ModelUser) {
        val onSuccessListener = OnSuccessListener<Uri?>{
            dataUser.fotoProfil = it.toString()

            addUserToFirebase(dataUser)
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

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun dialogSucces() {
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Pendaftaran Berhasil")
        alert.setMessage("Mohon tunggu proses verifikasi dalam waktu 1x24 jam")
        alert.setCancelable(false)

        alert.setPositiveButton(
            Constant.iya
        ) { dialog, _ ->
            dialog.dismiss()
            val intent = Intent(this, SplashActivity::class.java)
            startActivity(intent)
            finish()
        }

        alert.show()
    }
}