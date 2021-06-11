@file:Suppress("DEPRECATION")

package id.exomatik.absenki.ui.auth.verifyRegister

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.iid.InstanceIdResult
import com.google.firebase.storage.UploadTask
import id.exomatik.absenki.R
import id.exomatik.absenki.base.BaseViewModel
import id.exomatik.absenki.model.ModelUser
import id.exomatik.absenki.services.timer.TListener
import id.exomatik.absenki.services.timer.TimeFormatEnum
import id.exomatik.absenki.services.timer.TimerView
import id.exomatik.absenki.ui.auth.register.RegisterFragment
import id.exomatik.absenki.utils.*
import id.exomatik.absenki.utils.Constant.reffUser
import java.util.concurrent.TimeUnit

@SuppressLint("StaticFieldLeak")
class VerifyRegisterViewModel(
    private val dataSave: DataSave,
    private val activity: Activity?,
    private val progressTimer: TimerView,
    private val etText1 : AppCompatEditText,
    private val etText2 : AppCompatEditText,
    private val etText3 : AppCompatEditText,
    private val etText4 : AppCompatEditText,
    private val etText5 : AppCompatEditText,
    private val etText6 : AppCompatEditText,
    private val navController: NavController
) : BaseViewModel() {
    val phoneCode = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()
    var unverify = true
    var verifyId = ""
    lateinit var etFotoProfil: Uri
    lateinit var dataUser : ModelUser

    fun onClick(requestEvent: Int) {
        activity?.let { dismissKeyboard(it) }

        when (requestEvent) {
            2 -> {
                isShowLoading.value = true
                verifyUser()
            }
            3 -> {
                isShowLoading.value = true
                sendCode()
            }
        }
    }

    fun onClickBack(){
        val bundle = Bundle()
        val fragmentTujuan = RegisterFragment()
        bundle.putParcelable(reffUser, dataUser)
        bundle.putParcelable(Constant.reffFotoUser, etFotoProfil)
        fragmentTujuan.arguments = bundle

        val navOption = NavOptions.Builder().setPopUpTo(R.id.registerFragment, true).build()
        navController.navigate(R.id.registerFragment,bundle, navOption)
    }

    private fun verifyUser() {
        try {
            val credential = PhoneAuthProvider.getCredential(
                verifyId
                , phoneCode.value ?: throw Exception("Error, kode verifikasi tidak boleh kosong")
            )

            val onCoCompleteListener =
                OnCompleteListener<AuthResult> { task ->
                    if (task.isSuccessful) {
                        isShowLoading.value = false

                        saveFoto(etFotoProfil)
                    } else {
                        message.value = "Error, kode verifikasi salah"
                        isShowLoading.value = false
                        setEmptyEditText()
                    }
                }

            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(onCoCompleteListener)
        } catch (e: java.lang.Exception) {
            message.value = e.message
            isShowLoading.value = false
            setEmptyEditText()
        }
    }

    private fun setEmptyEditText() {
        etText6.setText("")
        etText5.setText("")
        etText4.setText("")
        etText3.setText("")
        etText2.setText("")
        etText1.setText("")
        etText6.clearFocus()
        etText1.findFocus()
        etText1.requestFocus()
    }

    fun saveFoto(image : Uri){
        val onSuccessListener = OnSuccessListener<UploadTask.TaskSnapshot> {
            message.value = "Berhasil menyimpan foto"
            getUrlFoto(it)
        }

        val onFailureListener = OnFailureListener {
            message.value = it.message
            isShowLoading.value = false
        }

        FirebaseUtils.simpanFoto(Constant.reffFotoUser, dataUser.username
            , image, onSuccessListener, onFailureListener)
    }

    private fun getUrlFoto(uploadTask: UploadTask.TaskSnapshot) {
        val onSuccessListener = OnSuccessListener<Uri?>{
            dataUser.fotoProfil = it.toString()

            getUserToken()
        }

        val onFailureListener = OnFailureListener {
            message.value = it.message
            isShowLoading.value = false
        }

        FirebaseUtils.getUrlFoto(uploadTask, onSuccessListener, onFailureListener)
    }

    @Suppress("DEPRECATION")
    private fun getUserToken() {
        isShowLoading.value = true
        val onCompleteListener =
            OnCompleteListener<InstanceIdResult> { result ->
                if (result.isSuccessful) {
                    val token = result.result?.token

                    if (!token.isNullOrEmpty()){
                        dataUser.token = token
                        val md5Password = stringToMD5(dataUser.password)
                        dataUser.password = md5Password

                        addUserToFirebase()
                    }
                    else{
                        isShowLoading.value = false
                        message.value = "Gagal mendapatkan token"
                        setEmptyEditText()
                    }

                } else {
                    isShowLoading.value = false
                    message.value = "Gagal mendapatkan token"
                    setEmptyEditText()
                }
            }

        FirebaseUtils.getUserToken(
            onCompleteListener
        )
    }

    private fun addUserToFirebase() {
        val onCompleteListener =
            OnCompleteListener<Void> { result ->
                if (result.isSuccessful) {
                    dataSave.setDataObject(
                        dataUser, reffUser
                    )

                    isShowLoading.value = false
                    message.value = "Berhasil menyimpan user"
                    navController.navigate(R.id.splashFragment)
                } else {
                    isShowLoading.value = false
                    message.value = "Gagal menyimpan data user"
                    setEmptyEditText()
                }
            }

        val onFailureListener = OnFailureListener { result ->
            isShowLoading.value = false
            message.value = result.message
            setEmptyEditText()
        }

        FirebaseUtils.setValueObject(
            reffUser
            , dataUser.username
            , dataUser
            , onCompleteListener
            , onFailureListener
        )
    }

    private fun sendCode() {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                unverify = false
                message.value =
                    "Berhasil memverifikasi nomor " + dataUser.phone
                isShowLoading.value = false
                loading.value = true

                saveFoto(etFotoProfil)
                isShowLoading.value = false
            }

            override fun onVerificationFailed(e: FirebaseException) {
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        message.value = "Error, Nomor Handphone tidak Valid"
                        setEmptyEditText()
                    }
                    is FirebaseTooManyRequestsException -> {
                        message.value = "Error, Anda sudah terlalu banyak mengirimkan permintaan"
                        setEmptyEditText()
                    }
                    else -> {
                        message.value = e.message
                        setEmptyEditText()
                    }
                }
                isShowLoading.value = false
                loading.value = true
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                object : CountDownTimer(2000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                    }

                    override fun onFinish() {
                        if (unverify) {
                            isShowLoading.value = false
                            loading.value = true

                            message.value = "Kami sudah mengirimkan kode verifikasi ke nomor ${dataUser.phone}"
                            unverify = false
                            setProgress()

                            verifyId = verificationId
                        }
                    }
                }.start()
            }
        }

        try {
            FirebaseUtils.registerUser(
                dataUser.phone
                , callbacks, activity ?: throw Exception("Error, Mohon mulai ulang aplikasi")
            )
        } catch (e: Exception) {
            message.value = e.message
            isShowLoading.value = false
        }
    }

    private fun setProgress() {
        progressTimer.setCircularTimerListener(object : TListener {
            override fun updateDataOnTick(remainingTimeInMs: Long): String {
                // long seconds = (milliseconds / 1000);
                val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingTimeInMs)
                progressTimer.prefix = ""
                progressTimer.suffix = " detik"
                return seconds.toString()
            }

            override fun onTimerFinished() {
                progressTimer.prefix = ""
                progressTimer.suffix = ""
                progressTimer.text = "Kirim Ulang?"
                isShowLoading.value = false
                loading.value = false
            }
        }, 60, TimeFormatEnum.SECONDS, 1)

        progressTimer.progress = 0F
        progressTimer.startTimer()
    }
}