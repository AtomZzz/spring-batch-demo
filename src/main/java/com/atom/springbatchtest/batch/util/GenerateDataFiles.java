package com.atom.springbatchtest.batch.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class GenerateDataFiles {

    private static final String OUTPUT_DIR = "src/main/resources/data/virtualTest/";

    private static final AtomicInteger id = new AtomicInteger(0);

    public static void main(String[] args) {
        for (int i = 1; i <= 50; i++){
            int finalI = i;
            new Thread(() -> {
                generateFile(OUTPUT_DIR + "data" + finalI + ".csv", 10000);
            }).start();
        }
    }

    private static void generateFile(String filename, int numRows) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // 写入表头
            writer.write("id,name,email");
            writer.newLine();
            // 写入数据行
            for (int i = 1; i <= numRows; i++) {
                int currentId = id.incrementAndGet(); //保证线程安全
                String name = "User" + currentId;
                String email = "user" + currentId + "@example.com";
                writer.write(currentId + "," + name + "," + email);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
