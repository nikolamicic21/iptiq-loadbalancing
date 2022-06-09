package io.nikolamicic21.iptiqloadbalancing.provider

interface Provider<T> {

    val identifier: String

    fun get(): T

    fun check(): Boolean

}
