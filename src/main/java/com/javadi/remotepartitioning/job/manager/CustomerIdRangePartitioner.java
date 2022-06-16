package com.javadi.remotepartitioning.job.manager;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

public class CustomerIdRangePartitioner implements Partitioner {

    private final int minId;
    private final int maxId;

    public CustomerIdRangePartitioner(int minId, int maxId) {
        this.minId = minId;
        this.maxId = maxId;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        int number = (maxId - minId) / gridSize + 1;

        Map<String, ExecutionContext> result = new HashMap<>();
        for (int i = 0; i < number; i++) {
            ExecutionContext executionContext = new ExecutionContext();
            int start = minId + (gridSize * i);
            int end = start + (gridSize * (i + 1));
            executionContext.putInt("minValue", start);
            executionContext.putInt("maxValue", Math.min(end, maxId));
            result.put("partition" + i, executionContext);
        }

        return result;
    }
}
