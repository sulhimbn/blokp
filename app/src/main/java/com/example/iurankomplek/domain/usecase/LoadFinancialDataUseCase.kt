package com.example.iurankomplek.domain.usecase
import com.example.iurankomplek.utils.OperationResult
import com.example.iurankomplek.data.api.models.PemanfaatanResponse
import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.domain.model.FinancialItem

class LoadFinancialDataUseCase(
    private val pemanfaatanRepository: PemanfaatanRepository,
    private val validateFinancialDataUseCase: ValidateFinancialDataUseCase = ValidateFinancialDataUseCase()
) {
    
    suspend operator fun invoke(): OperationResult<PemanfaatanResponse> {
        return try {
            pemanfaatanRepository.getPemanfaatan()
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Failed to load financial data")
        }
    }
    
    suspend operator fun invoke(forceRefresh: Boolean): OperationResult<PemanfaatanResponse> {
        return try {
            pemanfaatanRepository.getPemanfaatan(forceRefresh = forceRefresh)
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Failed to load financial data")
        }
    }
    
    fun validateFinancialData(response: PemanfaatanResponse): Boolean {
        return try {
            response.data?.let { items ->
                val financialItems = FinancialItem.fromLegacyDataItemDtoList(items)
                validateFinancialDataUseCase.validateAll(financialItems)
            } ?: true
        } catch (e: Exception) {
            false
        }
    }
}
