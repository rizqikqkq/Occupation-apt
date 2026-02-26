package space.rnpp.apt.util

import space.rnpp.apt.model.OccupationFormErrors
import space.rnpp.apt.model.OccupationFormInput

class OccupationValidator {
    fun validateCompanyName(value: String): String? {
        if (value.isBlank())            return "Company name is required"
        if (value.trim().length < 2)    return "Company name must be at least 2 characters"
        if (value.trim().length > 100)  return "Company name must be at most 100 characters"
        return null
    }

    fun validateCompanyAddress(value: String): String? {
        if (value.isBlank())            return "Company address is required"
        if (value.trim().length < 5)    return "Address must be at least 5 characters"
        if (value.trim().length > 200)  return "Address must be at most 200 characters"
        return null
    }

    fun validateCityName(value: String): String? {
        if (value.isBlank())            return "City name is required"
        if (value.trim().length < 2)    return "City name must be at least 2 characters"
        if (value.trim().length > 60)   return "City name must be at most 60 characters"
        if (!value.trim().matches(Regex("^[a-zA-Z ]+$")))
            return "City name must contain only letters"
        return null
    }

    fun validatePhoneNumber(value: String): String? {
        if (value.isBlank())                return "Phone number is required"
        if (!value.all { it.isDigit() })    return "Phone number must contain digits only"
        if (value.length < 8)               return "Phone number must be at least 8 digits"
        if (value.length > 15)              return "Phone number must be at most 15 digits"
        return null
    }

    fun validateNpwp(value: String): String? {
        if (value.isBlank())             return null
        if (!value.all { it.isDigit() }) return "NPWP must contain digits only"
        if (value.length != 15)          return "NPWP must be exactly 15 digits"
        return null
    }

    fun validateAll(input: OccupationFormInput): OccupationFormErrors {
        return OccupationFormErrors(
            companyNameError    = validateCompanyName(input.companyName),
            companyAddressError = validateCompanyAddress(input.companyAddress),
            cityNameError       = validateCityName(input.cityName),
            phoneNumberError    = validatePhoneNumber(input.phoneNumber),
            npwpError           = validateNpwp(input.npwp)
        )
    }

    companion object {
        const val NPWP_LENGTH = 16
    }
}
