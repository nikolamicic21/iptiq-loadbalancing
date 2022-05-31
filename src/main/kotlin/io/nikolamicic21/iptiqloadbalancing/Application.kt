package io.nikolamicic21.iptiqloadbalancing

import io.nikolamicic21.iptiqloadbalancing.loadbalancer.RandomLoadBalancer
import io.nikolamicic21.iptiqloadbalancing.loadbalancer.RoundRobinLoadBalancer
import io.nikolamicic21.iptiqloadbalancing.provider.DefaultProvider
import java.util.concurrent.CompletableFuture

fun main() {
    val provider1 = DefaultProvider("Provider 1")
    val provider2 = DefaultProvider("Provider 2")
    val provider3 = DefaultProvider("Provider 3")

    val randomLoadBalancer = RandomLoadBalancer()
    randomLoadBalancer.registerProviders(listOf(provider1, provider2, provider3))

    println("============ RANDOM LOAD BALANCER")

    println(randomLoadBalancer.get())
    println(randomLoadBalancer.get())
    println(randomLoadBalancer.get())
    println(randomLoadBalancer.get())
    println(randomLoadBalancer.get())

    println("============ RANDOM LOAD BALANCER - exclude Provider 1")

    randomLoadBalancer.excludeProvider("Provider 1")

    println(randomLoadBalancer.get())
    println(randomLoadBalancer.get())
    println(randomLoadBalancer.get())
    println(randomLoadBalancer.get())
    println(randomLoadBalancer.get())

    println("============ RANDOM LOAD BALANCER - include Provider 1")

    randomLoadBalancer.includeProvider("Provider 1")

    println(randomLoadBalancer.get())
    println(randomLoadBalancer.get())
    println(randomLoadBalancer.get())
    println(randomLoadBalancer.get())
    println(randomLoadBalancer.get())

    println("============ ROUND ROBIN LOAD BALANCER")

    val roundRobinLoadBalancer = RoundRobinLoadBalancer()
    roundRobinLoadBalancer.registerProviders(listOf(provider1, provider2, provider3))

    println(roundRobinLoadBalancer.get())
    println(roundRobinLoadBalancer.get())
    println(roundRobinLoadBalancer.get())
    println(roundRobinLoadBalancer.get())
    println(roundRobinLoadBalancer.get())

    println("============ ROUND ROBIN LOAD BALANCER - exclude Provider 1")

    roundRobinLoadBalancer.excludeProvider("Provider 1")

    println(roundRobinLoadBalancer.get())
    println(roundRobinLoadBalancer.get())
    println(roundRobinLoadBalancer.get())
    println(roundRobinLoadBalancer.get())
    println(roundRobinLoadBalancer.get())

    println("============ ROUND ROBIN LOAD BALANCER - include Provider 1")

    roundRobinLoadBalancer.includeProvider("Provider 1")

    println(roundRobinLoadBalancer.get())
    println(roundRobinLoadBalancer.get())
    println(roundRobinLoadBalancer.get())
    println(roundRobinLoadBalancer.get())
    println(roundRobinLoadBalancer.get())

    println("=========== Hearth Beat Check Provider 2 = false")

    provider2.active = false

    Thread.sleep(200L)

    println(roundRobinLoadBalancer.get())
    println(roundRobinLoadBalancer.get())
    println(roundRobinLoadBalancer.get())
    println(roundRobinLoadBalancer.get())
    println(roundRobinLoadBalancer.get())

    println("=========== Hearth Beat Check Provider 2 = true")

    provider2.active = true

    Thread.sleep(200L)

    println(roundRobinLoadBalancer.get())
    println(roundRobinLoadBalancer.get())
    println(roundRobinLoadBalancer.get())
    println(roundRobinLoadBalancer.get())
    println(roundRobinLoadBalancer.get())

    println("============ Concurrent Requests")

    CompletableFuture.allOf(
        CompletableFuture.runAsync { println(roundRobinLoadBalancer.get()) },
        CompletableFuture.runAsync { println(roundRobinLoadBalancer.get()) },
        CompletableFuture.runAsync { println(roundRobinLoadBalancer.get()) },
        CompletableFuture.runAsync { println(roundRobinLoadBalancer.get()) },
        CompletableFuture.runAsync { println(roundRobinLoadBalancer.get()) },
        CompletableFuture.runAsync { println(roundRobinLoadBalancer.get()) },
        CompletableFuture.runAsync { println(roundRobinLoadBalancer.get()) },
        CompletableFuture.runAsync { println(roundRobinLoadBalancer.get()) },
        CompletableFuture.runAsync { println(roundRobinLoadBalancer.get()) },
        CompletableFuture.runAsync { println(roundRobinLoadBalancer.get()) },
        CompletableFuture.runAsync { println(roundRobinLoadBalancer.get()) },
        CompletableFuture.runAsync { println(roundRobinLoadBalancer.get()) },
    ).join()
}
