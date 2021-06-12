@file:Suppress("DEPRECATION")

package id.exomatik.absenki.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import id.exomatik.absenki.R
import id.exomatik.absenki.utils.adapter.SectionsPagerAdapter
import id.exomatik.absenki.databinding.FragmentMainBinding
import com.google.android.material.tabs.TabLayout
import id.exomatik.absenki.base.BaseFragmentBind
import id.exomatik.absenki.ui.main.account.AccountFragment
import id.exomatik.absenki.ui.main.blank1.Blank1Fragment
import id.exomatik.absenki.ui.main.blank2.Blank2Fragment

class MainFragment : BaseFragmentBind<FragmentMainBinding>() {
    private lateinit var viewModel: MainViewModel
    override fun getLayoutResource(): Int = R.layout.fragment_main

    override fun myCodeHere() {
        supportActionBar?.hide()
        init()
    }

    private fun init() {
        bind.lifecycleOwner = this
        viewModel = MainViewModel()
        bind.viewModel = viewModel
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupViewPager(bind.viewPager)
        bind.tabs.setupWithViewPager(bind.viewPager)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Suppress("DEPRECATION")
    private fun setupViewPager(pager: ViewPager) {
        val adapter = SectionsPagerAdapter(childFragmentManager)
        adapter.addFragment(Blank1Fragment(), "Dashboard")
        adapter.addFragment(AccountFragment(), "Account")

        pager.adapter = adapter

        bind.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        bind.tabs.getTabAt(0)?.icon = resources.getDrawable(R.drawable.ic_logo_white)
                        bind.tabs.getTabAt(1)?.icon = resources.getDrawable(R.drawable.ic_profile_gray)
                    }
                    else -> {
                        bind.tabs.getTabAt(0)?.icon = resources.getDrawable(R.drawable.ic_logo_gray)
                        bind.tabs.getTabAt(1)?.icon = resources.getDrawable(R.drawable.ic_profile_white)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
}