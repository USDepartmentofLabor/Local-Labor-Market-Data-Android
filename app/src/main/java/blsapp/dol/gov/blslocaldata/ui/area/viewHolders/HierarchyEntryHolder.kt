package blsapp.dol.gov.blslocaldata.ui.area.viewHolders

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.hierarchy_item.view.*

/**
 * HierarchyEntryHolder - holds the inflated view for the Industry Entries view
 */

open class HierarchyEntryHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
    val mIndustryTitle: TextView = mView.titleLabel
    val mIndustryLocalValue: TextView = mView.valueLabel
    val mIndustryNationalValue: TextView? = mView.nationalValueLabel
    val mSubIndustryIndicator: ImageView = mView.subIndustriesIndicator

    override fun toString(): String {
        return super.toString() + " '" + mIndustryTitle.text + "'"
    }
}

