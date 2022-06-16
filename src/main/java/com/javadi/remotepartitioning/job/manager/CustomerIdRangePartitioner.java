package com.javadi.remotepartitioning.job.manager;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

public class CustomerIdRangePartitioner implements Partitioner {

    private final int minId;
    private final int maxId;
    private final int gridSize;

    public CustomerIdRangePartitioner(int minId, int maxId, int gridSize) {
        this.minId = minId;
        this.maxId = maxId;
        this.gridSize = gridSize;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        int number = (maxId - minId) / this.gridSize + 1;

        Map<String, ExecutionContext> result = new HashMap<>();
        for (int i = 0; i < number; i++) {
            ExecutionContext executionContext = new ExecutionContext();
            int start = minId + (this.gridSize * i);
            int end = start + (this.gridSize * (i + 1));
            executionContext.putInt("minValue", start);
            executionContext.putInt("maxValue", Math.min(end, maxId));
            result.put("partition" + i, executionContext);
        }

        return result;
    }
}
