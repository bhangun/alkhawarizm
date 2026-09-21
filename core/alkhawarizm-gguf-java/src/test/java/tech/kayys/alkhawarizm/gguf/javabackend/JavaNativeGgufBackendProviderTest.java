package tech.kayys.alkhawarizm.gguf.javabackend;

import org.junit.jupiter.api.Test;
import tech.kayys.alkhawarizm.gguf.api.GgufBackendProvider;

import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the Java-native GGUF backend provider registration and
 * properties. Tests that load a real model file are integration tests and live
 * in the test resources directory alongside actual GGUF fixtures.
 */
class JavaNativeGgufBackendProviderTest {

    @Test
    void providerIsAlwaysAvailable() {
        JavaNativeGgufBackendProvider provider = new JavaNativeGgufBackendProvider();
        assertTrue(provider.isAvailable(), "Pure-Java backend should always be available");
    }

    @Test
    void providerIdIsJava() {
        JavaNativeGgufBackendProvider provider = new JavaNativeGgufBackendProvider();
        assertEquals("java", provider.id());
    }

    @Test
    void providerPriorityOutranksLlamaCpp() {
        JavaNativeGgufBackendProvider provider = new JavaNativeGgufBackendProvider();
        // Java (10) > llamacpp (-10): once Java becomes ready, AUTO must prefer it.
        assertTrue(provider.priority() > 0,
                "Java provider priority should be positive to outrank llama.cpp in AUTO mode");
    }

    @Test
    void providerAliasesIncludeExpectedTokens() {
        JavaNativeGgufBackendProvider provider = new JavaNativeGgufBackendProvider();
        assertTrue(provider.aliases().contains("java-native"));
        assertTrue(provider.aliases().contains("pure-java"));
    }

    @Test
    void serviceLoaderDiscoversJavaProvider() {
        boolean found = ServiceLoader.load(GgufBackendProvider.class).stream()
                .anyMatch(p -> p.type().equals(JavaNativeGgufBackendProvider.class));
        assertTrue(found, "JavaNativeGgufBackendProvider should be discoverable via ServiceLoader");
    }
}
