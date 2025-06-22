package com.atom.springbatchtest.batch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    private String email;

    // 用于标记处理线程
    private String processorThread;

    // 用于标记来源文件
    private String sourceFile;
} 