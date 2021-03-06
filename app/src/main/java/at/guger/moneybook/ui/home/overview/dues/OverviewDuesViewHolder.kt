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

package at.guger.moneybook.ui.home.overview.dues

import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.recyclerview.viewholder.BindingViewHolder
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.databinding.ItemOverviewDuesBinding
import at.guger.moneybook.ui.home.HomeViewModel
import kotlinx.android.synthetic.main.item_overview_dues.*
import kotlin.math.min

/**
 * [RecyclerView.ViewHolder] for the dues overview item.
 */
class OverviewDuesViewHolder(binding: ItemOverviewDuesBinding) : BindingViewHolder<ItemOverviewDuesBinding, HomeViewModel>(binding) {

    override fun bind(viewModel: HomeViewModel) {
        binding.viewModel = viewModel
        binding.executePendingBindings()

        viewModel.claimsAndDebts.observe(binding.lifecycleOwner!!, Observer { transactions ->
            mOverviewDuesDivider.setDistributions(
                listOf(
                    transactions.filter { it.type == Transaction.TransactionType.CLAIM }.sumByDouble { it.value }.toFloat(),
                    transactions.filter { it.type == Transaction.TransactionType.DEBT }.sumByDouble { it.value }.toFloat()
                ),
                colorsRes = listOf(
                    R.color.color_claim,
                    R.color.color_debt
                )
            )
        })

        with(mOverviewDuesRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = OverviewDuesListAdapter().apply {
                viewModel.claimsAndDebts.observe(binding.lifecycleOwner!!, Observer { transactions ->
                    val sortedTransactions = transactions.sortedBy { it.date }
                    submitList(sortedTransactions.subList(0, min(sortedTransactions.size, 3)))
                })
            }
        }
    }

    override fun clear() {}
}