package blsapp.dol.gov.blslocaldata.ui.viewmodel

import blsapp.dol.gov.blslocaldata.model.reports.AreaReport
import blsapp.dol.gov.blslocaldata.model.reports.ReportType

enum class ReportRowType {
    HEADER,
    SUB_HEADER,
    UNEMPLOYMENAT_RATE_ITEM,
    INDUSTRY_EMPLOYMENT_ITEM,
    OCCUPATIONAL_EMPLOYMENT_ITEM,
    EMPLOYMENT_WAGES_ITEM,
    OWNERSHIP_EMPLOYMENT_WAGES_ITEM,
    HISTORY_ITEM,
    UNKNOWN
}

enum class ReportWageVsLevelType {
    ANNUAL_MEAN_WAGE,
    EMPLOYMENT_LEVEL
}

/**
 * ReportRow - Report Entry information for display from the ViewModel
 */

data class AreaReportRow(var type: ReportRowType,
                         val areaType: String?,
                         val areaReports: List<AreaReport>?,
                         var period: String? = null,
                         var year: String? = null,
                         var header: String?,
                         val reportType: ReportType? = null,
                         var headerCollapsed: Boolean = true,
                         var subIndustries: Boolean = false,
                         var subReportRows: List<AreaReportRow>? = null,
                         var headerType: ReportRowType? = ReportRowType.UNKNOWN)
