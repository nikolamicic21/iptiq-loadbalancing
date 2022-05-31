package io.nikolamicic21.iptiqloadbalancing.loadbalancer

import io.nikolamicic21.iptiqloadbalancing.provider.Provider
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.fixedRateTimer

abstract class LoadBalancer<T> {

    protected var providers = CopyOnWriteArrayList<Provider<T>>()

    protected val visibleProviders: List<Provider<T>>
        get() = providers.filter { !excludedProviderIdentifiers.contains(it.get()) }.toList()

    private val excludedProviderIdentifiers = mutableMapOf<T, Int>()
    private val providerParallelRequests = ConcurrentHashMap<T, Int>()
    private var heartBeatCheckInMillis = 100L

    init {
        startHearthBeatCheck()
    }

    fun get(): T {
        val providerIdentifier = getProviderIdentifier()
        if (providerParallelRequests.contains(providerIdentifier)) {
            val numberOfParallelRequests = providerParallelRequests.getValue(providerIdentifier)
            val provider = providers.find { it.identifier == providerIdentifier }
            if (numberOfParallelRequests < provider!!.maxNumberOfParallelRequests) {
                providerParallelRequests[providerIdentifier] = numberOfParallelRequests + 1
                val providerValue = provider.get()
                providerParallelRequests[providerIdentifier] = providerParallelRequests.getValue(providerIdentifier) - 1
                return providerValue
            } else {
                throw RuntimeException("No more than 10 requests allowed for provider $providerIdentifier")
            }
        } else {
            providerParallelRequests[providerIdentifier] = 1
            val providerValue = providers.find { it.identifier == providerIdentifier }!!.get()
            providerParallelRequests[providerIdentifier] = providerParallelRequests.getValue(providerIdentifier) - 1
            return providerValue
        }
    }

    fun excludeProvider(identifier: T) {
        if (providers.any { it.get() == identifier }) {
            excludedProviderIdentifiers[identifier] = 0
        }
    }

    fun includeProvider(identifier: T) {
        if (providers.any { it.get() == identifier } && excludedProviderIdentifiers.contains(identifier)) {
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

    protected abstract fun getProviderIdentifier(): T

    private fun startHearthBeatCheck() {
        fixedRateTimer("hearthBeatCheck-thread", true, Date(), heartBeatCheckInMillis) {
            providers.forEach {
                if (it.check()) {
                    if (excludedProviderIdentifiers.contains(it.get())) {
                        val retriedTimes = excludedProviderIdentifiers.getValue(it.get())
                        if (retriedTimes == 1) {
                            includeProvider(it.get())
                        } else {
                            excludedProviderIdentifiers[it.get()] = retriedTimes + 1
                        }
                    }
                } else {
                    excludeProvider(it.get())
                }
            }
        }
    }
}
