package com.atom.springbatchtest.batch.partition;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FilePartitioner implements Partitioner {
    private final String directory;

    public FilePartitioner(String directory) {
        this.directory = directory;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> map = new HashMap<>();
        File dir = new File(directory);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".csv"));
        if (files == null) return map;

        int i = 0;
        for (File file : files) {
            ExecutionContext context = new ExecutionContext();
            context.putString("fileName", file.getAbsolutePath());
            map.put("partition" + i, context);
            i++;
        }
        return map;
    }
} 