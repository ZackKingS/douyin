package com.example.douyinandroid.core

import com.example.douyinandroid.common.common_utils.LogUtil

object ServiceLocator {

    private val services = mutableMapOf<Class<*>, Any>()

    fun <T> register(clazz: Class<T>, instance: T) {
        services[clazz] = instance as Any
        LogUtil.d("ServiceLocator", "Registered: ${clazz.simpleName}")
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(clazz: Class<T>): T {
        return services[clazz] as? T ?: throw IllegalStateException(
            "Service ${clazz.simpleName} not found. Please register it in ServiceLocator."
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getOrNull(clazz: Class<T>): T? {
        return services[clazz] as? T
    }

    inline fun <reified T> get(): T = get(T::class.java)

    inline fun <reified T> getOrNull(): T? = getOrNull(T::class.java)
}
