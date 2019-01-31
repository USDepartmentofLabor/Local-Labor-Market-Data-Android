package blsapp.dol.gov.blslocaldata.ui.area

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import blsapp.dol.gov.blslocaldata.BLSApplication
import blsapp.dol.gov.blslocaldata.R
import blsapp.dol.gov.blslocaldata.db.entity.*
import blsapp.dol.gov.blslocaldata.model.ReportError
import blsapp.dol.gov.blslocaldata.model.reports.ReportManager
import blsapp.dol.gov.blslocaldata.model.reports.SeasonalAdjustment
import blsapp.dol.gov.blslocaldata.ui.area.viewModel.AreaViewModel
import blsapp.dol.gov.blslocaldata.ui.info.InfoActivity
import blsapp.dol.gov.blslocaldata.ui.viewmodel.CountyViewModel
import blsapp.dol.gov.blslocaldata.ui.viewmodel.MetroStateViewModel
import kotlinx.android.synthetic.main.activity_metro_state.*
import kotlinx.android.synthetic.main.fragment_area_header.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import android.view.accessibility.AccessibilityEvent
import android.content.Context.ACCESSIBILITY_SERVICE
import android.support.v4.content.ContextCompat.getSystemService
import android.view.accessibility.AccessibilityManager
import android.content.Context
import blsapp.dol.gov.blslocaldata.ui.UIUtil


class AreaReportActivity : AppCompatActivity(), ReportListAdapter.OnReportItemClickListener {
    companion object {
        const val KEY_AREA = "Area"
    }

    lateinit var mArea: AreaEntity
    private lateinit var viewModel: AreaViewModel
    private lateinit var adapter: ReportListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_metro_state)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        var leftAreaImage: Drawable? = null
        var rightAreaImage: Drawable? = null
        mArea = intent.getSerializableExtra(KEY_AREA) as AreaEntity
        when (mArea) {
            is NationalEntity -> {
                title = "National"
                leftButton.visibility = View.GONE
                rightButton.visibility = View.GONE
            }
            is MetroEntity -> {
                title = "Metro"
                leftButton.text = "State"
                leftAreaImage = ContextCompat.getDrawable(this,R.drawable.ic_left_arrow_up)
                rightAreaImage = ContextCompat.getDrawable(this,R.drawable.ic_right_arrow_down)
            }
            is StateEntity -> {
                title = "State"
                leftButton.text = "Metro"
                leftAreaImage = ContextCompat.getDrawable(this,R.drawable.ic_left_arrow_down)
                rightAreaImage = ContextCompat.getDrawable(this,R.drawable.ic_right_arrow_down)
            }
            is CountyEntity -> {
                title = "County"
                leftButton.text = "State"
                rightButton.text = "Metro"
                leftAreaImage = ContextCompat.getDrawable(this,R.drawable.ic_left_arrow_up)
                rightAreaImage = ContextCompat.getDrawable(this,R.drawable.ic_right_arrow_up)
            }

        }
        leftButton.contentDescription = "Display " + leftButton.text + " report for " + title
        rightButton.contentDescription = "Display " + rightButton.text + " report for " + title


        leftAreaImage?.let {
            leftButton.setCompoundDrawablesWithIntrinsicBounds(it, null, null, null)
        }
        rightAreaImage?.let {
            rightButton.setCompoundDrawablesWithIntrinsicBounds(null, null, it, null)
        }

        areaTitleTextView.text = mArea.title
        areaTitleTextView.contentDescription = mArea.accessibilityStr
        viewModel = createViewModel(mArea)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        adapter = ReportListAdapter(this, this)
        recyclerView.apply {
            adapter = this@AreaReportActivity.adapter
            layoutManager = LinearLayoutManager(this@AreaReportActivity)
        }
        attachObserver()

        val decorator = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(this,R.drawable.divider)?.let {
                decorator.setDrawable(it) }
        recyclerView.addItemDecoration(decorator)

//        val headerDecoration = ReportHeaderItemDecoration(resources.getDrawable(R.drawable.divider))
//        recyclerView.addItemDecoration(headerDecoration)

        leftButton.setOnClickListener {
            displayLeftSubareas()
        }
        rightButton.setOnClickListener {
            displayRightSubareas()
        }

        viewModel.mAdjustment = ReportManager.adjustment
        mArea.let {
            viewModel.mArea = it
        }
        seasonallyAdjustedSwitch.isChecked = if (ReportManager.adjustment == SeasonalAdjustment.ADJUSTED) true else false
        seasonallyAdjustedSwitch.setOnClickListener {
            ReportManager.adjustment =
                    if (seasonallyAdjustedSwitch.isChecked) SeasonalAdjustment.ADJUSTED else SeasonalAdjustment.NOT_ADJUSTED
            viewModel.setAdjustment(ReportManager.adjustment)
        }

        viewModel.getReports()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_area, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_info -> {
            val intent = Intent(applicationContext, InfoActivity::class.java)
            startActivity(intent)
            true
        }
        android.R.id.home -> {
            finish()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClick(item: ReportRow) {
        viewModel.toggleSection(item)
    }

    private fun attachObserver() {
        viewModel.reportRows.observe(this, Observer<List<ReportRow>> {
            adapter.setReportRows(it!!)
        })
        viewModel.isLoading.observe(this, Observer<Boolean> {
            it?.let { showLoadingDialog(it) }
        })
        viewModel.reportError.observe(this, Observer<ReportError> {
            it?.let { showError(it) }
        })
    }

    private fun createViewModel(area: AreaEntity): AreaViewModel {
        when(area) {
            is CountyEntity -> return ViewModelProviders.of(this).get(CountyViewModel::class.java)
        }

        return ViewModelProviders.of(this).get(MetroStateViewModel::class.java)
    }


    private fun displayLeftSubareas() {
        doAsync {
            val repository = (application as BLSApplication).repository
            var subAreas: ArrayList<AreaEntity>? = null

            when (mArea) {
                // If current area is Metro, dislay States
                is MetroEntity -> {
                    subAreas = repository.getStateAreas(mArea) as? ArrayList<AreaEntity>
                }

                // If current area is State, display Metros
                is StateEntity -> {
                    subAreas = repository.getMetroAreas(mArea) as? ArrayList<AreaEntity>
                }

                // If current area is County, display State
                is CountyEntity -> {
                    subAreas = repository.getStateAreas(mArea) as? ArrayList<AreaEntity>
                }
            }
            uiThread {
                displaySubAreas(subAreas)
            }
        }
    }

    private fun displayRightSubareas() {
        doAsync {
            val repository = (application as BLSApplication).repository
            var subAreas: ArrayList<AreaEntity>?

            when (mArea) {
                is CountyEntity -> {
                    subAreas = repository.getMetroAreas(mArea) as ArrayList<AreaEntity>
                    if (subAreas.count() < 1) {
                        uiThread {
                            showMessage(getString(R.string.county_not_in_metro))
                        }
                    }
                } else -> {
                    // dislay Counties
                    subAreas = repository.getCountyAreas(mArea) as ArrayList<AreaEntity>
                }
            }

            uiThread {
                displaySubAreas(subAreas)
            }
        }
    }

    fun displaySubAreas(subAreas: ArrayList<AreaEntity>?) {

        subAreas?.let {
            if (subAreas.count() > 1) {
                val intent = Intent(applicationContext, AreaResultsActivity::class.java)
                intent.putExtra(AreaResultsActivity.KEY_CURRENT_AREA, mArea)
                intent.putExtra(AreaResultsActivity.KEY_SUB_AREAS, subAreas)
                startActivity(intent)
            } else if (subAreas.count() == 1) {
                val subArea = it.first()
                val intent: Intent
                if (subArea is CountyEntity) {
//                    intent = Intent(applicationContext, CountyActivity::class.java)
                    intent = Intent(applicationContext, AreaReportActivity::class.java)
                }
                else {
                    intent = Intent(applicationContext, AreaReportActivity::class.java)
                }

                intent.putExtra(KEY_AREA, subArea)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }
    }

    private fun showLoadingDialog(show: Boolean) {
        if (show) {
            progressBar.visibility = View.VISIBLE
            if (ReportManager.adjustment == SeasonalAdjustment.ADJUSTED)
                UIUtil.accessibilityAnnounce(applicationContext, getString(R.string.loading_seasonally_adjusted_reports))
            else
                UIUtil.accessibilityAnnounce(applicationContext, getString(R.string.loading_not_seasonally_adjusted_reports))
        } else progressBar.visibility = View.GONE

    }

    private fun showError(error: ReportError) {
        showMessage(error.displayMessage)
    }

    private fun showMessage(message: String) {
            Snackbar.make(recyclerView, message, Snackbar.LENGTH_LONG).show()
    }

}