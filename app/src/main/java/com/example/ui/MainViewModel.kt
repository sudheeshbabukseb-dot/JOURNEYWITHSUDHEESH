package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.SampleData
import com.example.data.VerificationRecord
import com.example.util.FileUtils
import com.example.util.LocationHelper
import com.example.util.PdfGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FormUiState(
    val currentRecordId: Long = 0,
    val applicantName: String = "",
    val applicationNo: String = "",
    val applicantContactNo: String = "",
    val completionCertificate: Boolean = true,
    val ownershipCertificate: Boolean = true,
    val wiring: String = "Good",
    val earthResistance: String = "Good",
    val rccbCompanyName: String = "Legrand 30mA 40A",
    val requirementPower: String = "Single Phase",
    val nearestConsumerNo: String = "",
    val purposeOfConnection: String = "Domestic",
    val tariff: String = "LT-1A",
    val poleNo: String = "",
    val weatherproofWireLength: String = "",
    val otherConsentRequired: Boolean = false,
    val gpsCoordinates: String = "",
    val gpsAccuracy: String = "",
    val transformerName: String = "",
    val photoUri: String? = null,
    val dateTime: String = "",
    val googleDriveLink: String = "",
    val estimatePreparedBy: String = "Sudheesh Babu (Sub Engineer)",
    val remarks: String = "",
    val isGpsLoading: Boolean = false,
    val isGeneratingPdf: Boolean = false,
    val validationError: String? = null,
    val statusMessage: String? = null,
    val currentViewRecord: VerificationRecord? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.verificationDao()

    private val _uiState = MutableStateFlow(FormUiState())
    val uiState: StateFlow<FormUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val recordsList: StateFlow<List<VerificationRecord>> = dao.getAllRecords()
        .combine(_searchQuery) { records, query ->
            if (query.isBlank()) {
                records
            } else {
                records.filter {
                    it.applicantName.contains(query, ignoreCase = true) ||
                    it.applicationNo.contains(query, ignoreCase = true) ||
                    it.nearestConsumerNo.contains(query, ignoreCase = true) ||
                    it.purposeOfConnection.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        initializeCurrentDateTime()
        seedSampleDataIfEmpty()
    }

    private fun seedSampleDataIfEmpty() {
        viewModelScope.launch {
            if (dao.getRecordCount() == 0) {
                dao.insertRecord(SampleData.getInitialSample())
            }
        }
    }

    private fun initializeCurrentDateTime() {
        val now = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date())
        _uiState.value = _uiState.value.copy(dateTime = now)
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateApplicantName(value: String) { _uiState.value = _uiState.value.copy(applicantName = value, validationError = null) }
    fun updateApplicationNo(value: String) { _uiState.value = _uiState.value.copy(applicationNo = value, validationError = null) }
    fun updateApplicantContactNo(value: String) { _uiState.value = _uiState.value.copy(applicantContactNo = value) }
    fun updateCompletionCertificate(value: Boolean) { _uiState.value = _uiState.value.copy(completionCertificate = value) }
    fun updateOwnershipCertificate(value: Boolean) { _uiState.value = _uiState.value.copy(ownershipCertificate = value) }
    fun updateWiring(value: String) { _uiState.value = _uiState.value.copy(wiring = value) }
    fun updateEarthResistance(value: String) { _uiState.value = _uiState.value.copy(earthResistance = value) }
    fun updateRccbCompanyName(value: String) { _uiState.value = _uiState.value.copy(rccbCompanyName = value) }
    fun updateRequirementPower(value: String) { _uiState.value = _uiState.value.copy(requirementPower = value) }
    fun updateNearestConsumerNo(value: String) { _uiState.value = _uiState.value.copy(nearestConsumerNo = value, validationError = null) }
    fun updatePurposeOfConnection(value: String) { _uiState.value = _uiState.value.copy(purposeOfConnection = value) }
    fun updateTariff(value: String) { _uiState.value = _uiState.value.copy(tariff = value) }
    fun updatePoleNo(value: String) { _uiState.value = _uiState.value.copy(poleNo = value) }
    fun updateWeatherproofWireLength(value: String) { _uiState.value = _uiState.value.copy(weatherproofWireLength = value) }
    fun updateOtherConsentRequired(value: Boolean) { _uiState.value = _uiState.value.copy(otherConsentRequired = value) }
    fun updateTransformerName(value: String) { _uiState.value = _uiState.value.copy(transformerName = value) }
    fun updateDateTime(value: String) { _uiState.value = _uiState.value.copy(dateTime = value) }
    fun updateGoogleDriveLink(value: String) { _uiState.value = _uiState.value.copy(googleDriveLink = value) }
    fun updateEstimatePreparedBy(value: String) { _uiState.value = _uiState.value.copy(estimatePreparedBy = value) }
    fun updateRemarks(value: String) {
        // Enforce 500 word limit
        val words = value.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (words.size <= 500 || value.length < _uiState.value.remarks.length) {
            _uiState.value = _uiState.value.copy(remarks = value)
        }
    }

    fun setPhotoUri(uri: String?) {
        _uiState.value = _uiState.value.copy(photoUri = uri)
    }

    fun fetchGpsLocation(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGpsLoading = true, statusMessage = "Fetching GPS coordinates...")
            val result = LocationHelper.getCurrentLocation(context)
            if (result != null) {
                _uiState.value = _uiState.value.copy(
                    gpsCoordinates = result.coordinates,
                    gpsAccuracy = result.accuracy,
                    isGpsLoading = false,
                    statusMessage = "GPS coordinates captured successfully"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isGpsLoading = false,
                    statusMessage = "Unable to fetch GPS. Ensure Location is turned ON and permissions granted."
                )
            }
        }
    }

    fun resetForm() {
        val now = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date())
        _uiState.value = FormUiState(
            dateTime = now,
            estimatePreparedBy = "Sudheesh Babu (Sub Engineer)"
        )
    }

    fun loadRecordForEdit(record: VerificationRecord) {
        _uiState.value = _uiState.value.copy(
            currentRecordId = record.id,
            applicantName = record.applicantName,
            applicationNo = record.applicationNo,
            applicantContactNo = record.applicantContactNo,
            completionCertificate = record.completionCertificate,
            ownershipCertificate = record.ownershipCertificate,
            wiring = record.wiring,
            earthResistance = record.earthResistance,
            rccbCompanyName = record.rccbCompanyName,
            requirementPower = record.requirementPower,
            nearestConsumerNo = record.nearestConsumerNo,
            purposeOfConnection = record.purposeOfConnection,
            tariff = record.tariff,
            poleNo = record.poleNo,
            weatherproofWireLength = record.weatherproofWireLength,
            otherConsentRequired = record.otherConsentRequired,
            gpsCoordinates = record.gpsCoordinates,
            gpsAccuracy = record.gpsAccuracy,
            transformerName = record.transformerName,
            photoUri = record.photoUri,
            dateTime = record.dateTime,
            googleDriveLink = record.googleDriveLink,
            estimatePreparedBy = record.estimatePreparedBy,
            remarks = record.remarks,
            validationError = null,
            statusMessage = null,
            currentViewRecord = record
        )
    }

    fun setViewRecord(record: VerificationRecord) {
        _uiState.value = _uiState.value.copy(currentViewRecord = record)
    }

    fun saveVerificationForm(onSuccess: (VerificationRecord) -> Unit) {
        val state = _uiState.value

        // Validate Nearest Consumer Number (Mandatory)
        if (state.nearestConsumerNo.isBlank()) {
            _uiState.value = state.copy(validationError = "Nearest Consumer Number is Mandatory!")
            return
        }

        if (state.applicantName.isBlank()) {
            _uiState.value = state.copy(validationError = "Please enter Applicant Name.")
            return
        }

        val record = VerificationRecord(
            id = state.currentRecordId,
            applicantName = state.applicantName.trim(),
            applicationNo = state.applicationNo.trim(),
            applicantContactNo = state.applicantContactNo.trim(),
            completionCertificate = state.completionCertificate,
            ownershipCertificate = state.ownershipCertificate,
            wiring = state.wiring,
            earthResistance = state.earthResistance,
            rccbCompanyName = state.rccbCompanyName.trim(),
            requirementPower = state.requirementPower,
            nearestConsumerNo = state.nearestConsumerNo.trim(),
            purposeOfConnection = state.purposeOfConnection.trim(),
            tariff = state.tariff.trim(),
            poleNo = state.poleNo.trim(),
            weatherproofWireLength = state.weatherproofWireLength.trim(),
            otherConsentRequired = state.otherConsentRequired,
            gpsCoordinates = state.gpsCoordinates.trim(),
            gpsAccuracy = state.gpsAccuracy.trim(),
            transformerName = state.transformerName.trim(),
            photoUri = state.photoUri,
            dateTime = state.dateTime.ifBlank {
                SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date())
            },
            googleDriveLink = state.googleDriveLink.trim(),
            estimatePreparedBy = state.estimatePreparedBy.trim(),
            remarks = state.remarks.trim(),
            createdAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            val recordId = if (record.id > 0) {
                dao.updateRecord(record)
                record.id
            } else {
                dao.insertRecord(record)
            }

            val savedRecord = record.copy(id = recordId)
            _uiState.value = _uiState.value.copy(
                currentRecordId = recordId,
                currentViewRecord = savedRecord,
                statusMessage = "Field Verification Record saved successfully!",
                validationError = null
            )
            onSuccess(savedRecord)
        }
    }

    fun deleteRecord(record: VerificationRecord) {
        viewModelScope.launch {
            dao.deleteRecord(record)
        }
    }

    fun generatePdfForRecord(context: Context, record: VerificationRecord, onComplete: (File) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGeneratingPdf = true)
            val file = PdfGenerator.generateVerificationPdf(context, record)
            _uiState.value = _uiState.value.copy(isGeneratingPdf = false)
            onComplete(file)
        }
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null, validationError = null)
    }
}
