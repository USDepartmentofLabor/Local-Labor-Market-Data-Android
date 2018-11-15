package blsapp.dol.gov.blslocaldata.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import blsapp.dol.gov.blslocaldata.R
import blsapp.dol.gov.blslocaldata.db.entity.*
import blsapp.dol.gov.blslocaldata.ioThread
import java.util.concurrent.Executors

class LocalRepository private constructor(private val mDatabase: BLSDatabase) {

    fun getMetroAreas(search: String?): List<MetroEntity> {
        if (search == null || search.isEmpty()) {
            return mDatabase.metroDAO().getAll()
        }
        search.toIntOrNull()?.let {
            val cbsaCodes = mDatabase.zipCbsaDAO().getCBSA(search)
            if (cbsaCodes.count() > 0) {
                return mDatabase.metroDAO().findByCodes(cbsaCodes)
            }
        }

        return mDatabase.metroDAO().findByTitle(search)
    }
    fun getCountyAreas(search: String?): List<CountyEntity> {
        if (search == null || search.isEmpty()) {
            return mDatabase.countyDAO().getAll()
        }

        search.toIntOrNull()?.let {
            val countyCodes = mDatabase.zipCountyDAO().getCountyCode(search)
            if (countyCodes.count() > 0) {
                return mDatabase.countyDAO().findByCodes(countyCodes)
            }
        }

        return mDatabase.countyDAO().findByTitle(search)
    }

    fun getStateAreas(search: String?): List<StateEntity> {
        if (search == null || search.isEmpty()) {
            return mDatabase.stateDAO().getAll()
        }

        search.toIntOrNull()?.let {
            val countyCodes = mDatabase.zipCountyDAO().getCountyCode(search)
            val stateCodes = countyCodes.map { it.substring(0,2) }.distinct()
            if (stateCodes.count() > 0) {
                return mDatabase.stateDAO().findByCodes(stateCodes)
            }
        }

        return mDatabase.stateDAO().findByTitle(search)
    }

    fun getNationalArea(): NationalEntity {
        return mDatabase.nationalDAO().getAll()
    }


    fun getCountyAreas(area: AreaEntity): List<CountyEntity>? {
        var counties: List<CountyEntity>? = null
        when(area) {
            is MetroEntity -> {
                val countyCodes = mDatabase.msaCountyDAO().getCountyCodes(area.code)
                counties = mDatabase.countyDAO().findByCodes(countyCodes)
            }

            is StateEntity -> {
                counties = mDatabase.countyDAO().findForStateCodes(area.code)
            }
        }

        return counties
    }

    fun getMetroAreas(area: AreaEntity): List<MetroEntity>? {

        var metros: List<MetroEntity>? = null
        when(area) {
            is CountyEntity -> {
                val metroCodes = mDatabase.msaCountyDAO().getMSACodes(area.code)
                metros = mDatabase.metroDAO().findByCodes(metroCodes)
            }

            is StateEntity -> {
                // Get Counties for State
                val counties = mDatabase.countyDAO().findForStateCodes(area.code)
                // From Counties, get their metro Areas
                val countyCodes = counties.map { it.code }
                val msaCodes = mDatabase.msaCountyDAO().getMSACodes(countyCodes)
                metros = mDatabase.metroDAO().findByCodes(msaCodes)
            }
        }
        return metros
    }

    fun getStateAreas(area: AreaEntity): List<StateEntity>? {

        var states: List<StateEntity>? = null
        when(area) {
            is CountyEntity -> {
                val stateCode = area.code.substring(0, 2)
                states = mDatabase.stateDAO().findByCodes(listOf(stateCode))
            }

            is MetroEntity -> {
                // Get Counties for Metro
                val counties = mDatabase.msaCountyDAO().getCountyCodes(area.code)
                val stateCodes = counties.map { it.substring(0, 2) }.distinct()
                states = mDatabase.stateDAO().findByCodes(stateCodes)
            }
        }
        return states
    }

    companion object {
        private var instance: LocalRepository? = null

        fun getInstance(database: BLSDatabase): LocalRepository {
            if (instance == null) {
                synchronized(LocalRepository::class.java) {
                    if (instance == null) {
                        instance = LocalRepository(database)
                    }
                }
            }
            return instance!!
        }
    }
}