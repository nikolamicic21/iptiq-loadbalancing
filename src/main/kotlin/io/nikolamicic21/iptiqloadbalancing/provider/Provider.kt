package io.nikolamicic21.iptiqloadbalancing.provider

interface Provider<T> {

    val identifier: String
    val maxNumberOfParallelRequests: Int

    fun get(): T

    fun check(): Boolean

}
