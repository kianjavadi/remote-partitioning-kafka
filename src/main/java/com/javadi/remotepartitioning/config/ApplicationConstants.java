package com.javadi.remotepartitioning.config;

public final class ApplicationConstants {

    public static final int JOB_OPERATOR_CORE_POOL_SIZE = 64;
    public static final int JOB_OPERATOR_MAX_POOL_SIZE = 256;
    public static final int MANAGER_PARTITIONER_GRID_SIZE = 10;

    public static final int WORKER_CHUNK_SIZE = 10;
    public static final int WORKER_PROCESSING_WAITE_TIME_MS = 3000;

    private ApplicationConstants() {}

}
