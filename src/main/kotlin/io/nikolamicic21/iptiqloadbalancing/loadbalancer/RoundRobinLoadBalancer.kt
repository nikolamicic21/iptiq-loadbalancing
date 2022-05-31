package io.nikolamicic21.iptiqloadbalancing.loadbalancer

class RoundRobinLoadBalancer : LoadBalancer<String>() {

    override fun getProviderIdentifier(): String {
        val provider = visibleProviders[0]
        providers.remove(provider)
        providers.add(providers.size, provider)

        return provider.identifier
    }
}
