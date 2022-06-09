package io.nikolamicic21.iptiqloadbalancing.provider

class DefaultProvider(private val _identifier: String) : Provider<String> {

    override val identifier: String
        get() = _identifier

    var active = true

    override fun get(): String {
        Thread.sleep(1_000L)
        return identifier
    }

    override fun check() = active
}
