package id.exomatik.absenasn.ui.main.blank2

import id.exomatik.absenasn.R
import id.exomatik.absenasn.base.BaseFragmentBind
import id.exomatik.absenasn.databinding.FragmentBlank2Binding

class Blank2Fragment : BaseFragmentBind<FragmentBlank2Binding>() {
    private lateinit var viewModel: Blank2ViewModel

    override fun getLayoutResource(): Int = R.layout.fragment_blank2

    override fun myCodeHere() {
        init()
    }

    private fun init() {
        bind.lifecycleOwner = this
        viewModel = Blank2ViewModel()
        bind.viewModel = viewModel
    }

}

