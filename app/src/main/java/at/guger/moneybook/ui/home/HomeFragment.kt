/*
 * Copyright 2019 Daniel Guger
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package at.guger.moneybook.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.TooltipCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import at.guger.moneybook.MainNavDirections
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.fragment.BaseFragment
import at.guger.moneybook.core.ui.viewmodel.EventObserver
import at.guger.moneybook.ui.home.accounts.AccountsFragment
import at.guger.moneybook.ui.home.budgets.BudgetsFragment
import at.guger.moneybook.ui.home.dues.DuesFragment
import at.guger.moneybook.ui.home.overview.OverviewFragment
import at.guger.moneybook.ui.main.MainActivity
import at.guger.moneybook.util.DataUtils
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_home.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/**
 * Fragment containing all home fragments.
 */
class HomeFragment : BaseFragment() {

    //region Variables

    private val destinations = Destination.values()

    private val viewModel: HomeViewModel by sharedViewModel()

    //endregion

    //region Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLayout()

        setupEventListeners()
    }

    //endregion

    //region Menu

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        viewModel.coloredAccounts.observe(viewLifecycleOwner, Observer { menu.findItem(R.id.actionAddAccount)?.isVisible = mHomeViewPager.currentItem == 1 && it.size < DataUtils.MAX_ACCOUNTS })
    }

    //endregion

    //region Methods

    private fun setupLayout() {
        TooltipCompat.setTooltipText(fabAdd, getString(R.string.NewTransaction))

        mHomeViewPager.adapter = object : FragmentStateAdapter(requireActivity()) {

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> OverviewFragment.instantiate()
                    1 -> AccountsFragment.instantiate()
                    2 -> DuesFragment.instantiate()
                    else -> BudgetsFragment.instantiate()
                }
            }

            override fun getItemCount(): Int = destinations.size
        }

        mHomeViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                requireAppCompatActivity<MainActivity>().invalidateOptionsMenu()
            }
        })

        TabLayoutMediator(mHomeTabs, mHomeViewPager) { tab, position ->
            val destination = destinations[position]
            tab.apply {
                setText(destination.title)
                setIcon(destination.icon)
            }
        }.attach()

        fabAdd.setOnClickListener { findNavController().navigate(MainNavDirections.actionGlobalAddEditTransactionDialogFragment()) }
    }

    private fun setupEventListeners() {
        viewModel.navigateToPage.observe(viewLifecycleOwner, EventObserver { destination ->
            val destinationIndex = when (destination) {
                Destination.OVERVIEW -> 0
                Destination.ACCOUNTS -> 1
                Destination.DUES -> 2
                Destination.BUDGETS -> 3
            }

            mHomeViewPager.setCurrentItem(destinationIndex, true)
        })

        viewModel.showAccount.observe(viewLifecycleOwner, EventObserver { accountId ->
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAccountDetailFragment(accountId))
        })
    }

    //endregion

    //region Callback

    //endregion

    enum class Destination(@StringRes val title: Int, @DrawableRes val icon: Int) {
        OVERVIEW(R.string.Overview, R.drawable.ic_overview),
        ACCOUNTS(R.string.Accounts, R.drawable.ic_accounts),
        DUES(R.string.Dues, R.drawable.ic_dues),
        BUDGETS(R.string.Budgets, R.drawable.ic_budgets)
    }
}