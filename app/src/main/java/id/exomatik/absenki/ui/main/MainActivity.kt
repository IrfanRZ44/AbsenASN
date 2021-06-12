package id.exomatik.absenki.ui.main

import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import id.exomatik.absenki.R
import id.exomatik.absenki.base.BaseActivity
import id.exomatik.absenki.model.ModelUser
import id.exomatik.absenki.ui.auth.AuthActivity
import id.exomatik.absenki.utils.Constant
import id.exomatik.absenki.utils.FirebaseUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    override fun getLayoutResource(): Int = R.layout.activity_main

    @Suppress("DEPRECATION")
    override fun myCodeHere() {
        NavHostFragment.create(R.navigation.main_nav)
        viewParent.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        val username = savedData.getDataUser()?.username
        if (!username.isNullOrEmpty()){
            getDataUser(username)
        }

        val infoApps = savedData.getDataApps()?.informasi
        if (!infoApps.isNullOrEmpty()){
            alertInformation(infoApps)
        }
    }

    private fun getDataUser(username: String) {
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
            }

            override fun onDataChange(result: DataSnapshot) {
                if (result.exists()) {
                    val data = result.getValue(ModelUser::class.java)

                    when {
                        data?.token != savedData.getDataUser()?.token -> {
                            logout("Maaf, akun Anda sedang masuk dari perangkat lain")
                        }
                        data?.status != Constant.statusActive -> {
                            FirebaseUtils.stopRefresh()
                            data?.let { deleteToken(it) }
                        }
                        else -> {
                            savedData.setDataObject(data, Constant.reffUser)
                        }
                    }
                }
            }
        }

        FirebaseUtils.refreshDataWith1ChildObject1(
            Constant.reffUser
            , username
            , valueEventListener
        )
    }

    private fun deleteToken(dataUser: ModelUser) {
        val onCompleteListener = OnCompleteListener<Void> {
            logout("Maaf, akun Anda dibekukan")
        }
        val onFailureListener = OnFailureListener { }

        FirebaseUtils.setValueWith2ChildString(
            Constant.reffUser
            , dataUser.username
            , Constant.reffToken
            , ""
            , onCompleteListener
            , onFailureListener
        )
    }

    private fun logout(message: String){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        FirebaseUtils.signOut()
        savedData.setDataObject(ModelUser(), Constant.reffUser)
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun alertInformation(information: String?) {
        val alert = AlertDialog.Builder(this)
        alert.setTitle(Constant.attention)
        alert.setMessage(information)
        alert.setCancelable(true)
        alert.setPositiveButton(
            "Baik"
        ) { dialog, _ ->
            dialog.dismiss()
        }

        alert.show()
    }
}