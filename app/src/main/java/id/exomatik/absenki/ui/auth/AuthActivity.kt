package id.exomatik.absenki.ui.auth

import android.view.View
import androidx.navigation.fragment.NavHostFragment
import id.exomatik.absenki.R
import id.exomatik.absenki.base.BaseActivity
import kotlinx.android.synthetic.main.activity_auth.*

class AuthActivity : id.exomatik.absenki.base.BaseActivity() {
    override fun getLayoutResource(): Int = R.layout.activity_auth

    override fun myCodeHere() {
        NavHostFragment.create(R.navigation.auth_nav)
        viewParent.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }
}
