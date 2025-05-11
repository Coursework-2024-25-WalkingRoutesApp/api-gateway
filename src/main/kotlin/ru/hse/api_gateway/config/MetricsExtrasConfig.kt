package ru.hse.api_gateway.config


import io.github.mweirauch.micrometer.jvm.extras.ProcessMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.*
import io.micrometer.core.instrument.binder.logging.LogbackMetrics
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MetricsExtrasConfig {
    @Bean
    fun jvmGcMetrics(): JvmGcMetrics {
        return JvmGcMetrics()
    }

    @Bean
    fun fileDescriptorMetrics(): FileDescriptorMetrics {
        return FileDescriptorMetrics()
    }

    @Bean
    fun processMemoryMetrics(): ProcessMemoryMetrics {
        return ProcessMemoryMetrics()
    }

    @Bean
    fun classLoaderMetrics(): ClassLoaderMetrics {
        return ClassLoaderMetrics()
    }

    @Bean
    fun processorMetrics(): ProcessorMetrics {
        return ProcessorMetrics()
    }

    @Bean
    fun logbackMetrics(): LogbackMetrics {
        return LogbackMetrics()
    }
}

