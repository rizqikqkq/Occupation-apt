package space.rnpp.apt.model

interface OccupationRepository {
    fun getCompanySuggestions(query: String): List<CompanySuggestion>
}

class DummyOccupationRepository : OccupationRepository {
    private val allCompanies = listOf(
        CompanySuggestion(11, "PT Bank Nasional"),
        CompanySuggestion(12, "PT Bank BACA"),
        CompanySuggestion(13, "PT Bank Sendiri"),
        CompanySuggestion(22, "PT Digital Solusi"),
        CompanySuggestion(33, "PT Teknologi Nusantara"),
    )
    override fun getCompanySuggestions(query: String): List<CompanySuggestion> {
        if (query.length <= 3) return emptyList()
        return allCompanies.filter {
            it.name.contains(query, ignoreCase = true)
        }
    }
}