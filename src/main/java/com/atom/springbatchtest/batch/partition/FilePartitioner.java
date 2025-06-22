package com.atom.springbatchtest.batch.partition;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class FilePartitioner implements Partitioner {

    @Value("${data.path}")
    private String dataPath;

    public FilePartitioner() {
        log.info("FilePartitioner init...");
        log.info("dataPath: {}", dataPath);
        log.info("FilePartitioner={}",this.hashCode());
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> partitionMap = new HashMap<>();

        File inputDir = new File(dataPath);
        if (!inputDir.exists() || !inputDir.isDirectory()) {
            throw new IllegalArgumentException("Invalid directory: " + dataPath);
        }

        File[] files = inputDir.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".csv") ||
                        name.toLowerCase().endsWith(".txt"));

        if (files == null || files.length == 0) {
            throw new IllegalStateException("No files found in: " + dataPath);
        }

        int partitionIndex = 0;
        for (File file : files) {
            ExecutionContext context = new ExecutionContext();
            context.putString("filePath", file.getAbsolutePath());
            context.putString("fileName", file.getName());

            partitionMap.put("partition" + partitionIndex++, context);
        }

        return partitionMap;
    }
}