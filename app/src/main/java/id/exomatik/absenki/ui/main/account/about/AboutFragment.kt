package id.exomatik.absenki.ui.main.account.about

import id.exomatik.absenki.R
import id.exomatik.absenki.base.BaseFragmentBind
import id.exomatik.absenki.databinding.FragmentAboutBinding

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