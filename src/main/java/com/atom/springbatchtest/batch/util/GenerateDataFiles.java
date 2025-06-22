package com.atom.springbatchtest.batch.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class GenerateDataFiles {

    private static final String OUTPUT_DIR = "src/main/resources/data/";

    private static final AtomicInteger id = new AtomicInteger(1);

    public static void main(String[] args) {
        generateFile(OUTPUT_DIR + "data1.csv", 1000);
        generateFile(OUTPUT_DIR + "data2.csv", 1000);
    }

    private static void generateFile(String filename, int numRows) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // 写入表头
            if (filename.contains("data1.csv")) {
                writer.write("id,name,email");
            } else if (filename.contains("data2.csv")) {
                writer.write("id,username,email");
            }
            writer.newLine();

            // 写入数据行
            for (int i = 1; i <= numRows; i++) {
                String name = "User" + id;
                String email = "user" + id + "@example.com";
                writer.write(id + "," + name + "," + email);
                writer.newLine();
                id.addAndGet(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
