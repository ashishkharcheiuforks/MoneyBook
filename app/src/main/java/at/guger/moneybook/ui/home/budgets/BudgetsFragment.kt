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

package at.guger.moneybook.ui.home.budgets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import at.guger.moneybook.MainNavDirections
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.fragment.BaseFragment
import at.guger.moneybook.core.ui.recyclerview.listener.OnItemTouchListener
import at.guger.moneybook.core.util.ext.setup
import at.guger.moneybook.data.model.Budget
import at.guger.moneybook.databinding.FragmentBudgetsBinding
import at.guger.moneybook.ui.home.HomeViewModel
import at.guger.moneybook.ui.main.MainActivity
import at.guger.moneybook.util.menu.BudgetMenuUtils
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import kotlinx.android.synthetic.main.fragment_budgets.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/**
 * Fragment for [home view pager's][ViewPager2] budgets content.
 */
class BudgetsFragment : BaseFragment(), OnItemTouchListener.ItemTouchListener {

    //region Variables

    private lateinit var adapter: BudgetsAdapter

    private val viewModel: HomeViewModel by sharedViewModel()

    //endregion

    //region Fragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentBudgetsBinding.inflate(inflater, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = BudgetsAdapter().apply { viewModel.budgetsWithBalance.observe(viewLifecycleOwner, Observer(::submitList)) }

        mBudgetsRecyclerView.setup(LinearLayoutManager(requireContext()), adapter, hasFixedSize = false) {
            addOnItemTouchListener(OnItemTouchListener(context, this, this@BudgetsFragment))
        }
    }

    //endregion

    //region Methods

    private fun editBudget(budget: Budget) {
        findNavController().navigate(MainNavDirections.actionGlobalAddEditBudgetBottomSheetDialogFragment(budget))
    }

    //endregion

    //region Callback

    override fun onItemClick(view: View, pos: Int, e: MotionEvent) {
        if (requireAppCompatActivity<MainActivity>().mCab.isActive()) {
            adapter.toggleChecked(pos)

            if (adapter.checkedCount > 0) {
                requireAppCompatActivity<MainActivity>().mCab!!.apply {
                    title(literal = getString(R.string.x_selected, adapter.checkedCount))

                    BudgetMenuUtils.prepareMenu(getMenu(), adapter)
                }
            } else {
                requireAppCompatActivity<MainActivity>().destroyCab()
            }
        }
    }

    override fun onItemLongClick(view: View, pos: Int, e: MotionEvent) {
        adapter.toggleChecked(pos)

        if (adapter.checkedCount > 0) {
            if (!requireAppCompatActivity<MainActivity>().mCab.isActive()) {
                requireAppCompatActivity<MainActivity>().attachCab(R.menu.menu_budget) {
                    title(literal = getString(R.string.x_selected, adapter.checkedCount))

                    onCreate { _, menu -> BudgetMenuUtils.prepareMenu(menu, adapter) }

                    onDestroy {
                        adapter.clearChecked()
                        true
                    }

                    onSelection { menuItem ->
                        BudgetMenuUtils.onItemSelected(menuItem, adapter, ::editBudget, viewModel::deleteBudget)
                        destroy()
                    }
                }
            } else {
                requireAppCompatActivity<MainActivity>().mCab!!.apply {
                    title(literal = getString(R.string.x_selected, adapter.checkedCount))

                    BudgetMenuUtils.prepareMenu(getMenu(), adapter)
                }
            }
        } else {
            requireAppCompatActivity<MainActivity>().destroyCab()
        }
    }

    //endregion

    companion object {
        fun instantiate(): BudgetsFragment = BudgetsFragment()
    }
}