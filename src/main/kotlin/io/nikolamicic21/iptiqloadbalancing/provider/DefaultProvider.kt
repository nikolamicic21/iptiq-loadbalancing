package io.nikolamicic21.iptiqloadbalancing.provider

class DefaultProvider(private val _identifier: String) : Provider<String> {

    override val identifier: String
        get() = _identifier

    override val maxNumberOfParallelRequests: Int
        get() = 10

    var active = true

    override fun get(): String = identifier

    override fun check() = active
}
