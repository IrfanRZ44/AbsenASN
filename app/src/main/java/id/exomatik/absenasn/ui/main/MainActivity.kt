package id.exomatik.absenasn.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import id.exomatik.absenasn.R
import id.exomatik.absenasn.model.ModelUser
import id.exomatik.absenasn.ui.auth.SplashActivity
import id.exomatik.absenasn.ui.main.account.AccountFragment
import id.exomatik.absenasn.ui.main.beranda.BlankFragment
import id.exomatik.absenasn.ui.main.blank1.Blank1Fragment
import id.exomatik.absenasn.ui.main.blank2.Blank2Fragment
import id.exomatik.absenasn.utils.Constant
import id.exomatik.absenasn.utils.DataSave
import id.exomatik.absenasn.utils.FirebaseUtils
import id.exomatik.absenasn.utils.adapter.SectionsPagerAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(){
    private lateinit var savedData : DataSave

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        setupViewPager(viewPager)
        tabs.setupWithViewPager(viewPager)
        myCodeHere()
    }

    private fun myCodeHere() {
        savedData = DataSave(this)

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
        val intent = Intent(this, SplashActivity::class.java)
        startActivity(intent)
        finish()
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

    @SuppressLint("UseCompatLoadingForDrawables")
    @Suppress("DEPRECATION")
    private fun setupViewPager(pager: ViewPager) {
        val adapter = SectionsPagerAdapter(supportFragmentManager)
        adapter.addFragment(BlankFragment(), "Dashboard")
        adapter.addFragment(AccountFragment(), "Account")

        pager.adapter = adapter

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        tabs.getTabAt(0)?.icon = resources.getDrawable(R.drawable.ic_logo_white)
                        tabs.getTabAt(1)?.icon = resources.getDrawable(R.drawable.ic_profile_gray)
                    }
                    else -> {
                        tabs.getTabAt(0)?.icon = resources.getDrawable(R.drawable.ic_logo_gray)
                        tabs.getTabAt(1)?.icon = resources.getDrawable(R.drawable.ic_profile_white)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
}