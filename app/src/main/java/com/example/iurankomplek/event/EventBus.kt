package com.example.iurankomplek.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central EventBus for cross-ViewModel communication using SharedFlow.
 * 
 * Usage:
 * 1. Inject EventBus into ViewModels that need to emit or observe events
 * 2. Call publish(event) to emit an event
 * 3. Collect events.flow in ViewModel init to observe events
 * 
 * Example:
 * ```
 * @HiltViewModel
 * class MyViewModel @Inject constructor(
 *     private val eventBus: EventBus
 * ) : ViewModel() {
 *     
 *     init {
 *         viewModelScope.launch {
 *             eventBus.events.collect { event ->
 *                 when (event) {
 *                     is AppEvent.PaymentCompleted -> refreshData()
 *                     else -> {}
 *                 }
 *             }
 *         }
 *     }
 *     
 *     fun doSomething() {
 *         viewModelScope.launch {
 *             eventBus.publish(AppEvent.SomeEvent())
 *         }
 *     }
 * }
 * ```
 */
@Singleton
class EventBus @Inject constructor() {
    
    /**
     * Mutable shared flow for emitting events.
     * replay=0 ensures only new subscribers receive new events.
     * extraBufferCapacity=64 prevents event loss during bursts.
     */
    private val _events = MutableSharedFlow<AppEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )
    
    /**
     * Public read-only flow for observing events.
     */
    val events: SharedFlow<AppEvent> = _events.asSharedFlow()
    
    /**
     * Publish an event to all subscribers.
     * This is a suspend function and must be called from a coroutine.
     * 
     * @param event The event to publish
     * @return true if the event was successfully emitted, false otherwise
     */
    suspend fun publish(event: AppEvent): Boolean {
        return _events.tryEmit(event)
    }
    
    /**
     * Publish an event with suspension if buffer is full.
     * Use this when event delivery is critical and cannot be dropped.
     * 
     * @param event The event to publish
     */
    suspend fun publishBlocking(event: AppEvent) {
        _events.emit(event)
    }
}
