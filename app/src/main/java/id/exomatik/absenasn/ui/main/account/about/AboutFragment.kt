package id.exomatik.absenasn.ui.main.account.about

import id.exomatik.absenasn.R
import id.exomatik.absenasn.base.BaseFragmentBind
import id.exomatik.absenasn.databinding.FragmentAboutBinding

class AboutFragment : BaseFragmentBind<FragmentAboutBinding>(){
    override fun getLayoutResource(): Int = R.layout.fragment_about
    lateinit var viewModel: AboutViewModel

    override fun myCodeHere() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Tentang Aplikasi"
        supportActionBar?.show()
        init()
    }

    private fun init() {
        bind.lifecycleOwner = this
        viewModel = AboutViewModel()
        bind.viewModel = viewModel
        viewModel.etAbout.value = savedData.getDataApps()?.aboutApps
    }
}