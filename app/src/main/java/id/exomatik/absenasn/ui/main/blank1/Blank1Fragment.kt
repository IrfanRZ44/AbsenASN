package id.exomatik.absenasn.ui.main.blank1

import id.exomatik.absenasn.R
import id.exomatik.absenasn.base.BaseFragmentBind
import id.exomatik.absenasn.databinding.FragmentBlank1Binding

class Blank1Fragment : BaseFragmentBind<FragmentBlank1Binding>() {
    private lateinit var viewModel: Blank1ViewModel

    override fun getLayoutResource(): Int = R.layout.fragment_blank1

    override fun myCodeHere() {
        init()
    }

    private fun init() {
        bind.lifecycleOwner = this
        viewModel = Blank1ViewModel()
        bind.viewModel = viewModel
    }

}

