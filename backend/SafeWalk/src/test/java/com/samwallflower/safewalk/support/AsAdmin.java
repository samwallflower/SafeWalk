package com.samwallflower.safewalk.support;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Runs each test in the class as an authenticated ADMIN (passes SecurityUtils ownership checks).
 * Service tests that are about business rules use this; ownership rules are tested
 * explicitly in SecurityUtilsTest and by calling AuthenticatedAs.user(id) inside a test.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(AsAdmin.Ext.class)
public @interface AsAdmin {
    class Ext implements BeforeEachCallback, AfterEachCallback {
        @Override public void beforeEach(ExtensionContext c) { AuthenticatedAs.admin(); }
        @Override public void afterEach(ExtensionContext c) { AuthenticatedAs.clear(); }
    }
}
