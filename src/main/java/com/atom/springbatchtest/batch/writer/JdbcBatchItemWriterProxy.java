package com.atom.springbatchtest.batch.writer;

import com.atom.springbatchtest.batch.model.User;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class JdbcBatchItemWriterProxy implements ItemWriter<User> {

    private final DataSource dataSource;
    private JdbcBatchItemWriter<User> delegate;

    @Autowired
    public JdbcBatchItemWriterProxy(DataSource dataSource) {
        this.dataSource = dataSource;
        this.delegate = new JdbcBatchItemWriterBuilder<User>()
                .dataSource(dataSource)
                .sql("INSERT INTO user (id, name, email, processor_thread, source_file) VALUES (?, ?, ?, ?, ?)")
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setLong(1, item.getId());
                    ps.setString(2, item.getName());
                    ps.setString(3, item.getEmail());
                    ps.setString(4, item.getProcessorThread());
                    ps.setString(5, item.getSourceFile());
                })
                .build();
    }

    @Override
    public void write(Chunk<? extends User> chunk) throws Exception {
        TimeUnit.SECONDS.sleep(5);
        List<? extends User> items = chunk.getItems();
        delegate.write(chunk);
        // 添加写入统计日志
        System.out.printf("[线程: %s] 写入 %d 条用户记录%n",
                Thread.currentThread().getName(), items.size());
    }
}