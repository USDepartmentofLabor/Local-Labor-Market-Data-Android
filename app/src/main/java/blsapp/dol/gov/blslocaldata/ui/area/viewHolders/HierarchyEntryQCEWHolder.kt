package blsapp.dol.gov.blslocaldata.ui.area.viewHolders

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.hierarchy_item.view.*
import kotlinx.android.synthetic.main.hierarchy_item_qcew.view.*

/**
 * HierarchyEntryHolder - holds the inflated view for the Industry Entries view
 */

class HierarchyEntryQCEWHolder(mView: View) : HierarchyEntryHolder(mView) {
    val twelveMonthValueLabel: TextView = mView.twelveMonthValueLabel
    val twelveMonthPercentLabel: TextView = mView.twelveMonthPercentLabel
    val nationalTwelveMonthValueLabel: TextView = mView.nationalTwelveMonthValueLabel
    val nationalTwelveMonthPercentLabel: TextView = mView.nationalTwelveMonthPercentLabel

    override fun toString(): String {
        return super.toString() + " '" + mIndustryTitle.text + "'"
    }
}

