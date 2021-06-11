package id.exomatik.absenki.ui.auth.splash

import android.os.Handler
import androidx.navigation.fragment.findNavController
import id.exomatik.absenki.R
import id.exomatik.absenki.base.BaseFragment


class SplashFragment : BaseFragment() {
    override fun getLayoutResource(): Int = R.layout.fragment_splash

    override fun myCodeHere() {
        Handler().postDelayed({
            findNavController().navigate(R.id.loginFragment)
        }, 2000L)
    }

}