package com.itsybitso.executor;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.Future;


/**
 * Base class for common code in async executors.
 */
public class AsyncExecutor {

    static List<Integer> totalCounts = new ArrayList<>();
    static List<String> futureResults = new ArrayList<>();
    static List<Future<String>> futures = new ArrayList<>();
    static List<Integer> warnings = new ArrayList<>();
    static int defaultNumThreads = 5;


    static {
        totalCounts = new ArrayList<>();
        futureResults = new ArrayList<>();
        futures = new ArrayList<>();
        warnings = new ArrayList<>();
    }

}
