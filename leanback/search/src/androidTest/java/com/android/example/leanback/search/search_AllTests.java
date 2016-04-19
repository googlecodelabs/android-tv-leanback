package com.android.example.leanback.search;

import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.Test;

public class search_AllTests {
    public static Test suite() {
        return new TestSuiteBuilder(search_AllTests.class)
                .includeAllPackagesUnderHere()
                .build();
    }
}
// EOF
