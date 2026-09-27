package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.VinSearchEntity
import com.example.data.model.DamagePhoto
import com.example.data.model.VehicleHistoryReport
import com.example.data.pdf.PdfReportGenerator
import com.example.data.remote.VinHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class VinUiState(
    val queryVin: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentReport: VehicleHistoryReport? = null,
    val generatedPdfFile: File? = null,
    val isGeneratingPdf: Boolean = false,
    val selectedPhotoForPreview: DamagePhoto? = null,
    val isHistorySheetOpen: Boolean = false,
    val showSuccessSnackbar: String? = null
)

class VinViewModel(
    private val repository: VinHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VinUiState())
    val uiState: StateFlow<VinUiState> = _uiState.asStateFlow()

    val searchHistory: StateFlow<List<VinSearchEntity>> = repository.allSearches
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onVinChanged(newVin: String) {
        val filtered = newVin.uppercase().filter { it.isLetterOrDigit() }.take(17)
        _uiState.update { it.copy(queryVin = filtered, errorMessage = null) }
    }

    fun searchVin(overrideVin: String? = null) {
        val vinToSearch = (overrideVin ?: _uiState.value.queryVin).trim().uppercase()
        if (vinToSearch.length != 17) {
            _uiState.update {
                it.copy(
                    errorMessage = "لطفاً شماره کامل 17 رقمی شاسی (VIN) را وارد نمایید. (طول فعلی: ${vinToSearch.length})"
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                queryVin = vinToSearch,
                isLoading = true,
                errorMessage = null,
                generatedPdfFile = null
            )
        }

        viewModelScope.launch {
            val result = repository.fetchVehicleHistory(vinToSearch)
            result.onSuccess { report ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentReport = report,
                        errorMessage = null,
                        showSuccessSnackbar = "اطلاعات خودرو ${report.make} ${report.model} با موفقیت دریافت شد."
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "خطای ناشناخته در استعلام VIN"
                    )
                }
            }
        }
    }

    fun loadFromHistory(entity: VinSearchEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, queryVin = entity.vin, isHistorySheetOpen = false) }
            val cached = repository.getCachedReport(entity.vin)
            if (cached != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentReport = cached,
                        errorMessage = null,
                        generatedPdfFile = null
                    )
                }
            } else {
                searchVin(entity.vin)
            }
        }
    }

    fun toggleFavorite(vin: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(vin, isFavorite)
        }
    }

    fun deleteHistoryItem(vin: String) {
        viewModelScope.launch {
            repository.deleteSearch(vin)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun generatePdf(context: Context, onComplete: ((File) -> Unit)? = null) {
        val report = _uiState.value.currentReport ?: return
        _uiState.update { it.copy(isGeneratingPdf = true) }

        viewModelScope.launch {
            val result = PdfReportGenerator.generatePdfReport(context, report)
            result.onSuccess { file ->
                _uiState.update {
                    it.copy(
                        isGeneratingPdf = false,
                        generatedPdfFile = file,
                        showSuccessSnackbar = "گزارش PDF آماده چاپ و اشتراک‌گذاری است."
                    )
                }
                onComplete?.invoke(file)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isGeneratingPdf = false,
                        errorMessage = "خطا در ایجاد فایل PDF: ${error.localizedMessage}"
                    )
                }
            }
        }
    }

    fun setPhotoPreview(photo: DamagePhoto?) {
        _uiState.update { it.copy(selectedPhotoForPreview = photo) }
    }

    fun setHistorySheetOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isHistorySheetOpen = isOpen) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(showSuccessSnackbar = null) }
    }

    class Factory(private val repository: VinHistoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VinViewModel::class.java)) {
                return VinViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
