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

import android.graphics.Color
import androidx.databinding.BindingAdapter
import at.guger.moneybook.core.formatter.CurrencyFormat
import at.guger.moneybook.data.model.BudgetWithBalance
import at.guger.strokepiechart.Entry
import at.guger.strokepiechart.StrokePieChart

/**
 * Binding adapters for the budgets screen.
 */

@BindingAdapter("budgets", requireAll = true)
fun StrokePieChart.setBudgets(budgets: List<BudgetWithBalance>?) {
    val entries = ArrayList<Entry>()

    val leftSum = budgets?.sumByDouble { it.budget - it.balance } ?: 0.0
    val budgetSum = budgets?.sumByDouble { it.budget } ?: 0.0

    budgets?.forEach { budget ->
        if (budget.balance > 0.0) entries.add(Entry(budget.balance.toFloat(), budget.color))
    }

    if (budgetSum - leftSum > 0.01) {
        entries.add(Entry(leftSum.toFloat(), Color.BLACK))
    } else if (leftSum + budgetSum == 0.0) {
        entries.add(Entry(1.0f, Color.BLACK))
    }

    setEntries(entries)

    text = CurrencyFormat.format(leftSum)

    startAnimation()
}