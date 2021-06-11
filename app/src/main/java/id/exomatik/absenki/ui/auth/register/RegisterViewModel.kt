@file:Suppress("DEPRECATION")

package id.exomatik.absenki.ui.auth.register

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
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
import com.google.firebase.iid.InstanceIdResult
import com.google.firebase.storage.UploadTask
import id.exomatik.absenki.R
import id.exomatik.absenki.base.BaseViewModel
import id.exomatik.absenki.model.ModelUser
import id.exomatik.absenki.ui.auth.verifyRegister.VerifyRegisterFragment
import id.exomatik.absenki.utils.*
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("StaticFieldLeak")
class RegisterViewModel(
    private val activity: Activity?,
    private val dataSave: DataSave?,
    private val navController: NavController,
    private val editUsername: TextInputLayout,
    private val editPassword: TextInputLayout,
    private val editConfirmPassword: TextInputLayout,
    private val editNamaLengkap: TextInputLayout,
    private val editAlamat: TextInputLayout,
    private val editTempatLahir: TextInputLayout,
    private val editTanggalLahir: TextInputLayout,
    private val editNoHp: TextInputLayout,
    private val radioJK: RadioGroup
) : BaseViewModel() {
    val etUsername = MutableLiveData<String>()
    val etPhone = MutableLiveData<String>()
    val etNama = MutableLiveData<String>()
    val etFotoProfil = MutableLiveData<Uri>()
    val etPassword = MutableLiveData<String>()
    val etPasswordConfirm = MutableLiveData<String>()
    val etAlamat = MutableLiveData<String>()
    val etTempatLahir = MutableLiveData<String>()
    val etTanggalLahir = MutableLiveData<String>()
    var unverify = true

    fun onClickLogin(){
        activity?.let { dismissKeyboard(it) }
        navController.navigate(R.id.loginFragment)
    }

    fun setDataUser(fotoProfil: Uri?, data: ModelUser){
        etUsername.value = data.username
        etPassword.value = data.password
        etPasswordConfirm.value = data.password
        etNama.value = data.nama
        etAlamat.value = data.alamat
        etTanggalLahir.value = data.tanggalLahir
        etTempatLahir.value = data.tempatLahir
        etPhone.value = data.phone.replaceFirst("+62", "0")
        etFotoProfil.value = fotoProfil

        if (data.jk == "Perempuan"){
            radioJK.check(R.id.rbJk2)
        }
        else{
            radioJK.check(R.id.rbJk1)
        }
    }

    fun onClickRegister(){
        activity?.let { dismissKeyboard(it) }
        setNullError()

        val radio = radioJK.checkedRadioButtonId
        val btn = activity?.findViewById<RadioButton?>(radio)
        val jenisKelamin = btn?.text.toString()
        val username = etUsername.value
        val password = etPassword.value
        val confirmPassword = etPasswordConfirm.value
        val namaLengkap = etNama.value
        val tglLahir = etTanggalLahir.value
        val tempatLahir = etTempatLahir.value
        val alamat = etAlamat.value
        val noHp = etPhone.value
        val fotoProfil = etFotoProfil.value?.path
        val tglSekarang = getDateNow(Constant.dateTimeFormat1)

        if (!username.isNullOrEmpty() && !password.isNullOrEmpty()
            && !confirmPassword.isNullOrEmpty() && (password == confirmPassword)
            && password.length >= 6 && isContainNumber(password)
            && (isContainSmallText(password) || isContainBigText(password))
            && !namaLengkap.isNullOrEmpty() && !tglLahir.isNullOrEmpty() && !tempatLahir.isNullOrEmpty()
            && !fotoProfil.isNullOrEmpty() && !noHp.isNullOrEmpty() && !alamat.isNullOrEmpty()
            && (noHp.length in 10..13) && radio > 0 && jenisKelamin.isNotEmpty()
        ) {
            val hp = noHp.replaceFirst("0", "+62")
            isShowLoading.value = true

            val dataUser = ModelUser(
                username, password, hp, "", namaLengkap,
                jenisKelamin, tempatLahir, tglLahir, alamat, "", tglSekarang,
                tglSekarang, tglSekarang
            )

            cekUserName(dataUser)
        }
        else{
            if (fotoProfil.isNullOrEmpty()){
                message.value = "Mohon upload foto profil"
            }
            else if (username.isNullOrEmpty()){
                setTextError("Error, mohon masukkan username", editUsername)
            }
            else if (password.isNullOrEmpty()){
                setTextError("Error, Mohon masukkan password", editPassword)
            }
            else if (password != confirmPassword){
                setTextError("Error, password yang Anda masukkan berbeda", editConfirmPassword)
            }
            else if (password.length < 6){
                setTextError("Error, password harus minimal 6 digit", editPassword)
            }
            else if (!isContainNumber(password)){
                setTextError("Error, password harus memiliki kombinasi angka", editPassword)
            }
            else if (!isContainSmallText(password) && !isContainBigText(password)){
                setTextError("Error, password harus memiliki kombinasi huruf", editPassword)
            }
            else if (namaLengkap.isNullOrEmpty()){
                setTextError("Error, Mohon masukkan nama lengkap", editNamaLengkap)
            }
            else if (jenisKelamin.isEmpty()){
                editNamaLengkap.clearFocus()
                editPassword.clearFocus()
                editConfirmPassword.clearFocus()
                editUsername.clearFocus()
                message.value = "Error, Mohon pilih jenis kelamin"
            }
            else if (alamat.isNullOrEmpty()){
                setTextError("Error, mohon masukkan alamat", editAlamat)
            }
            else if (tempatLahir.isNullOrEmpty()){
                setTextError("Error, Mohon masukkan tempat lahir", editTempatLahir)
            }
            else if (tglLahir.isNullOrEmpty()){
                message.value = "Error, Mohon pilih tanggal lahir"
                editNamaLengkap.clearFocus()
                editPassword.clearFocus()
                editConfirmPassword.clearFocus()
                editUsername.clearFocus()
                message.value = "Error, Mohon pilih jenis kelamin"
                editTanggalLahir.requestFocus()
                editTanggalLahir.findFocus()
                editTanggalLahir.error = "Error, Mohon pilih tanggal lahir"
            }
            else if (noHp.isNullOrEmpty()){
                setTextError("Error, Mohon masukkan nomor HP yang valid", editNoHp)
            }
            else if (noHp.take(1) != "0"){
                setTextError("Error, mohon masukkan nomor HP dengan awalan 0", editNoHp)
            }
            else if (noHp.length !in 10..13){
                setTextError("Error, nomor HP  harus 10-13 digit", editNoHp)
            }
        }
    }

    fun getDateTglLahir() {
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
                    etTanggalLahir.value = dateFormatter.format(dateSelected.time)
                },
                localCalendar[Calendar.YEAR],
                localCalendar[Calendar.MONTH],
                localCalendar[Calendar.DATE]
            )

            datePickerDialog.show()
        } catch (e: java.lang.Exception) {
            message.value = e.message
        }
    }

    private fun setNullError(){
        editUsername.error = null
        editPassword.error = null
        editConfirmPassword.error = null
        editNamaLengkap.error = null
        editTanggalLahir.error = null
        editTempatLahir.error = null
        editAlamat.error = null
        editNoHp.error = null
    }

    private fun setTextError(msg: String, editText: TextInputLayout){
        message.value = msg
        editText.error = msg
        editText.requestFocus()
        editText.findFocus()
    }

    private fun cekUserName(dataUser: ModelUser) {
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                isShowLoading.value = false

                showLog("Cek Username onCancelled")
//                cekHandphone(dataUser)
            }

            override fun onDataChange(result: DataSnapshot) {
                if (result.exists()) {
                    isShowLoading.value = false
                    setTextError("Gagal, Username sudah digunakan", editUsername)
                } else {
                    isShowLoading.value = false
                    showLog("Cek Username Success")
//                    cekHandphone(dataUser)
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
                isShowLoading.value = false
                signUp(dataUser)
            }

            override fun onDataChange(result: DataSnapshot) {
                if (result.exists()) {
                    isShowLoading.value = false
                    setTextError("Gagal, No Handphone sudah terdaftar", editUsername)
                } else {
                    showLog("cekHandphone Success")
                    signUp(dataUser)
                }
            }
        }

        FirebaseUtils.searchDataWith1ChildObject(
            Constant.reffUser, Constant.phone, dataUser.phone, valueEventListener
        )
    }

    private fun signUp(dataUser: ModelUser) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                if (unverify) {
                    showLog("onVerificationCompleted Success")
                    signIn(credential, dataUser)
                }
                unverify = false
            }

            override fun onVerificationFailed(e: FirebaseException) {
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        message.value = "Error, Nomor Handphone tidak Valid"
                    }
                    is FirebaseTooManyRequestsException -> {
                        message.value = "Error, Anda sudah terlalu banyak mengirimkan permintaan coba lagi nanti"
                    }
                    else -> {
                        message.value = e.message
                    }
                }
                isShowLoading.value = false
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
                            isShowLoading.value = false

                            val bundle = Bundle()
                            val fragmentTujuan = VerifyRegisterFragment()
                            bundle.putString("verifyId", verificationId)
                            bundle.putBoolean("auth", true)
                            bundle.putParcelable("dataUser", dataUser)
                            bundle.putParcelable(Constant.reffFotoUser, etFotoProfil.value)

                            fragmentTujuan.arguments = bundle
                            navController.navigate(R.id.verifyRegisterFragment, bundle)
                            unverify = false
                        }
                    }
                }.start()
            }
        }

        try {
            FirebaseUtils.registerUser(
                dataUser.phone,
                callbacks,
                activity ?: throw Exception("Mohon mulai ulang aplikasi")
            )
        } catch (e: Exception) {
            message.value = e.message
            isShowLoading.value = false
        }
    }

    fun signIn(credential: AuthCredential, dataUser: ModelUser) {
        isShowLoading.value = true

        val onCoCompleteListener =
            OnCompleteListener<AuthResult> { task ->
                if (task.isSuccessful) {
                    etFotoProfil.value?.let { saveFoto(it, dataUser) }
                } else {
                    isShowLoading.value = false
                    message.value = "Gagal masuk ke Akun Anda"
                }
            }

        FirebaseUtils.signIn(credential, onCoCompleteListener)
    }

    fun saveFoto(image: Uri, dataUser: ModelUser){
        val onSuccessListener = OnSuccessListener<UploadTask.TaskSnapshot> {
            message.value = "Berhasil menyimpan foto"
            getUrlFoto(it, dataUser)
        }

        val onFailureListener = OnFailureListener {
            message.value = it.message
            isShowLoading.value = false
        }

        FirebaseUtils.simpanFoto(Constant.reffFotoUser, dataUser.username
            , image, onSuccessListener, onFailureListener)
    }

    private fun getUrlFoto(uploadTask: UploadTask.TaskSnapshot, dataUser: ModelUser) {
        val onSuccessListener = OnSuccessListener<Uri?>{
            dataUser.fotoProfil = it.toString()

            getUserToken(dataUser)
        }

        val onFailureListener = OnFailureListener {
            message.value = it.message
            isShowLoading.value = false
        }

        FirebaseUtils.getUrlFoto(uploadTask, onSuccessListener, onFailureListener)
    }

    @Suppress("DEPRECATION")
    private fun getUserToken(dataUser: ModelUser) {
        val onCompleteListener =
            OnCompleteListener<InstanceIdResult> { result ->
                if (result.isSuccessful) {
                    message.value = "Berhasil mendapatkan token"

                    dataUser.token = result.result?.token ?: ""
                    addUserToFirebase(dataUser)
                } else {
                    isShowLoading.value = false
                    message.value = "Gagal mendapatkan token"
                }
            }

        FirebaseUtils.getUserToken(
            onCompleteListener
        )
    }

    private fun addUserToFirebase(dataUser: ModelUser) {
        val onCompleteListener =
            OnCompleteListener<Void> { result ->
                if (result.isSuccessful) {
                    dataSave?.setDataObject(
                        dataUser, Constant.reffUser
                    )

                    navController.navigate(R.id.splashFragment)
                } else {
                    isShowLoading.value = false
                    message.value = "Gagal menyimpan data user"
                }
            }

        val onFailureListener = OnFailureListener { result ->
            isShowLoading.value = false
            message.value = result.message
        }
        FirebaseUtils.setValueObject(
            Constant.reffUser, dataUser.username, dataUser, onCompleteListener, onFailureListener
        )
    }
}