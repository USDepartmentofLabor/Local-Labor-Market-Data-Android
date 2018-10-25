package blsapp.dol.gov.blslocaldata.ui.area

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.occupational_employment.view.*

class OccupationalEmploymentHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
    val mDataValueTextView: TextView = mView.dataValueTextView
    val mAreaTitleTextView: TextView = mView.areaTextView
    val mMonthYearTextView: TextView = mView.monthYearTextView

    override fun toString(): String {
        return super.toString() + " '" + mDataValueTextView.text + "'"
    }
}

