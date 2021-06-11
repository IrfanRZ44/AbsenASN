package id.exomatik.absenki.ui.auth.splash

import android.content.Intent
import android.net.Uri
import androidx.navigation.fragment.findNavController
import id.exomatik.absenki.R
import id.exomatik.absenki.base.BaseFragmentBind
import id.exomatik.absenki.databinding.FragmentSplashBinding

class SplashFragment : BaseFragmentBind<FragmentSplashBinding>() {
    override fun getLayoutResource(): Int = R.layout.fragment_splash
    lateinit var viewModel: SplashViewModel

    override fun myCodeHere() {
        supportActionBar?.hide()
        bind.lifecycleOwner = this
        viewModel = SplashViewModel(findNavController(), savedData, activity)
        bind.viewModel = viewModel
        viewModel.getInfoApps()

        bind.btnUpdate.setOnClickListener {
            activity?.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=${context?.packageName}")
                )
            )
        }
    }

}