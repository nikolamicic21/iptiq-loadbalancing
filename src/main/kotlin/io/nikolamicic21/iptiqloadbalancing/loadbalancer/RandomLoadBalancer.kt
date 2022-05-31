package io.nikolamicic21.iptiqloadbalancing.loadbalancer

class RandomLoadBalancer : LoadBalancer<String>() {

    override fun getProviderIdentifier(): String = visibleProviders.random().identifier

}
