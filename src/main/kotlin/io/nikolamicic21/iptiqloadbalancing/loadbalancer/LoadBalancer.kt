package io.nikolamicic21.iptiqloadbalancing.loadbalancer

import io.nikolamicic21.iptiqloadbalancing.provider.Provider
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.fixedRateTimer

abstract class LoadBalancer<T> {

    companion object {
        private const val MAX_REQUESTS_PER_PROVIDER = 1
        private const val HEART_BEAT_CHECK_IN_MILLIS = 250L
    }

    protected var providers = CopyOnWriteArrayList<Provider<T>>()

    protected val visibleProviders: List<Provider<T>>
        get() = providers.filter { !excludedProviderIdentifiers.contains(it.identifier) }.toList()

    private val excludedProviderIdentifiers = mutableListOf<String>()

    private val numberOfParallelRequests = AtomicInteger(0)
    private val maxNumberOfRequests: Int
        get() = visibleProviders.size * MAX_REQUESTS_PER_PROVIDER

    private val providerRetries = mutableMapOf<String, Int>()

    init {
        startHearthBeatCheck()
    }

    fun get(): T {
        if (numberOfParallelRequests.addAndGet(1) <= maxNumberOfRequests) {
            val providerIdentifier = getProviderIdentifier()
            val provider = providers.find { it.identifier == providerIdentifier }!!
            val providerValue = provider.get()
            numberOfParallelRequests.getAndDecrement()

            return providerValue
        } else {
            println("No more than $maxNumberOfRequests requests allowed")
            throw RuntimeException("No more than $maxNumberOfRequests requests allowed")
        }
    }

    fun excludeProvider(identifier: String) {
        if (providers.any { it.identifier == identifier } && !excludedProviderIdentifiers.contains(identifier)) {
            excludedProviderIdentifiers.add(identifier)
        }
    }

    fun includeProvider(identifier: String) {
        if (excludedProviderIdentifiers.contains(identifier)) {
            excludedProviderIdentifiers.remove(identifier)
        }
    }

    fun registerProvider(provider: Provider<T>) {
        if (providers.size < 10) {
            providers.add(provider)
        }
    }

    fun registerProviders(providers: List<Provider<T>>) {
        if (this.providers.size + providers.size <= 10) {
            this.providers.addAll(providers)
        }
    }

    protected abstract fun getProviderIdentifier(): String

    private fun startHearthBeatCheck() {
        fixedRateTimer("hearthBeatCheck-thread", true, Date(), HEART_BEAT_CHECK_IN_MILLIS) {
            providers.forEach { provider ->
                if (provider.check()) {
                    if (excludedProviderIdentifiers.contains(provider.identifier)) {
                        if (providerRetries.contains(provider.identifier)) {
                            val retriedTimes = providerRetries.getValue(provider.identifier)
                            if (retriedTimes == 1) {
                                includeProvider(provider.identifier)
                                providerRetries[provider.identifier] = 0
                            } else {
                                providerRetries[provider.identifier] = retriedTimes + 1
                            }
                        } else {
                            providerRetries[provider.identifier] = 0
                        }
                    }
                } else {
                    excludeProvider(provider.identifier)
                    providerRetries[provider.identifier] = 0
                }
            }
        }
    }
}
