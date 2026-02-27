package space.rnpp.apt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import space.rnpp.apt.model.*
import space.rnpp.apt.util.OccupationValidator

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OccupationFormViewModel(
    private val repository: OccupationRepository = DummyOccupationRepository(),
    private val validator:  OccupationValidator   = OccupationValidator()
) : ViewModel() {

    private val _uiState = MutableStateFlow(OccupationFormUiState())
    val uiState: StateFlow<OccupationFormUiState> = _uiState.asStateFlow()

    private var companyNameValidationJob: Job? = null
    private var companySuggestJob:        Job? = null   // 500 ms suggest debounce
    private var addressDebounceJob:       Job? = null
    private var cityDebounceJob:          Job? = null
    private var phoneDebounceJob:         Job? = null
    private var npwpDebounceJob:          Job? = null

    //region : Fungsi Rekomendasi Company
    fun onCompanyNameChanged(value: String) {
        _uiState.update { it.copy(input = it.input.copy(companyName = value)) }

        companyNameValidationJob?.cancel()
        companyNameValidationJob = viewModelScope.launch {
            delay(VALIDATION_DEBOUNCE_MS)
            val error = validator.validateCompanyName(value)
            _uiState.update { it.copy(errors = it.errors.copy(companyNameError = error)) }
            recomputeNextEnabled()
        }

        companySuggestJob?.cancel()
        if (value.length > 3) {
            companySuggestJob = viewModelScope.launch {
                delay(SUGGEST_DEBOUNCE_MS)
                val suggestions = repository.getCompanySuggestions(value)
                _uiState.update {
                    it.copy(
                        companySuggestions = suggestions,
                        showSuggestions    = suggestions.isNotEmpty()
                    )
                }
            }
        } else {
            _uiState.update { it.copy(companySuggestions = emptyList(), showSuggestions = false) }
        }
    }
    fun onCompanySuggestionSelected(suggestion: CompanySuggestion) {
        _uiState.update {
            it.copy(
                input = it.input.copy(companyName = suggestion.name),
                companySuggestions = emptyList(),
                showSuggestions    = false,

                errors = it.errors.copy(companyNameError = null)
            )
        }
        recomputeNextEnabled()
    }
    fun onDismissSuggestions() {
        _uiState.update { it.copy(showSuggestions = false) }
    }
    //endregion

    fun onCompanyAddressChanged(value: String) {
        _uiState.update { it.copy(input = it.input.copy(companyAddress = value)) }
        addressDebounceJob?.cancel()
        addressDebounceJob = viewModelScope.launch {
            delay(VALIDATION_DEBOUNCE_MS)
            val error = validator.validateCompanyAddress(value)
            _uiState.update { it.copy(errors = it.errors.copy(companyAddressError = error)) }
            recomputeNextEnabled()
        }
    }
    fun onCityNameChanged(value: String) {
        _uiState.update { it.copy(input = it.input.copy(cityName = value)) }
        cityDebounceJob?.cancel()
        cityDebounceJob = viewModelScope.launch {
            delay(VALIDATION_DEBOUNCE_MS)
            val error = validator.validateCityName(value)
            _uiState.update { it.copy(errors = it.errors.copy(cityNameError = error)) }
            recomputeNextEnabled()
        }
    }
    fun onPhoneNumberChanged(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        _uiState.update { it.copy(input = it.input.copy(phoneNumber = digitsOnly)) }
        phoneDebounceJob?.cancel()
        phoneDebounceJob = viewModelScope.launch {
            delay(VALIDATION_DEBOUNCE_MS)
            val error = validator.validatePhoneNumber(digitsOnly)
            _uiState.update { it.copy(errors = it.errors.copy(phoneNumberError = error)) }
            recomputeNextEnabled()
        }
    }
    fun onNpwpChanged(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        _uiState.update { it.copy(input = it.input.copy(npwp = digitsOnly)) }
        npwpDebounceJob?.cancel()
        npwpDebounceJob = viewModelScope.launch {
            delay(VALIDATION_DEBOUNCE_MS)
            val error = validator.validateNpwp(digitsOnly)
            _uiState.update { it.copy(errors = it.errors.copy(npwpError = error)) }
            recomputeNextEnabled()
        }
    }
    fun onNext() {
        val fullErrors = validator.validateAll(_uiState.value.input)
        if (fullErrors.hasErrors) {
            _uiState.update { it.copy(errors = fullErrors) }
            recomputeNextEnabled()
            return
        }
        // TODO: do next action
    }

    private fun recomputeNextEnabled() { //re-check field if value changed
        val input  = _uiState.value.input
        val errors = _uiState.value.errors

        val mandatoryFilled =
            input.companyName.isNotBlank()    &&
                    input.companyAddress.isNotBlank() &&
                    input.cityName.isNotBlank()       &&
                    input.phoneNumber.isNotBlank()

        _uiState.update { it.copy(isNextEnabled = mandatoryFilled && !errors.hasErrors) }
    }

    companion object {
        private const val VALIDATION_DEBOUNCE_MS = 300L
        private const val SUGGEST_DEBOUNCE_MS    = 500L
    }
}