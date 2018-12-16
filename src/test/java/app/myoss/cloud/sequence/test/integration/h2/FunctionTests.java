/*
 * Copyright 2018-2018 https://github.com/myoss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package app.myoss.cloud.sequence.test.integration.h2;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import com.zaxxer.hikari.HikariDataSource;

import app.myoss.cloud.sequence.impl.DefaultSequenceImpl;
import app.myoss.cloud.sequence.impl.RdsSequenceRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * 序列生成器功能测试
 *
 * @author Jerry.Chen
 * @since 2018年12月16日 下午2:33:03
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { DataSourceAutoConfiguration.class, JdbcTemplateAutoConfiguration.class })
public class FunctionTests {
    @Autowired
    private HikariDataSource dataSource;
    @Autowired
    private JdbcTemplate     jdbcTemplate;

    @Test
    public void normTest1() {
        RdsSequenceRepository sequenceRepository = new RdsSequenceRepository();
        sequenceRepository.setAdjust(true);
        HashMap<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("defaultDataSource", dataSource);
        sequenceRepository.setDataSourceMap(dataSourceMap);
        sequenceRepository.setDataSourceCount(1);

        DefaultSequenceImpl defaultSequence = new DefaultSequenceImpl();
        defaultSequence.setName("seq_norm_test1");
        defaultSequence.setSequenceRepository(sequenceRepository);
        defaultSequence.init();

        Long a1 = defaultSequence.nextValue(500);
        assertEquals(a1, Long.valueOf(1500L));
        Long a2 = defaultSequence.nextValue(500);
        assertEquals(a2, Long.valueOf(2000L));

        Long nextId = null;
        for (int i = 0; i < 1000; i++) {
            nextId = defaultSequence.nextValue();
        }
        assertEquals(nextId, Long.valueOf(3000L));

        Map<String, Object> sequenceTableData = jdbcTemplate
                .queryForMap("select * from `sequence` where `name` = '" + defaultSequence.getName() + "'");
        log.info("sequence table data: {}{}", System.lineSeparator(), sequenceTableData);
        assertEquals(sequenceTableData.get("value"), 2000L);
    }

    @Test
    public void normTest2() {
        RdsSequenceRepository sequenceRepository = new RdsSequenceRepository();
        sequenceRepository.setAdjust(true);
        HashMap<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("defaultDataSource", dataSource);
        sequenceRepository.setDataSourceMap(dataSourceMap);
        sequenceRepository.setDataSourceCount(2);

        DefaultSequenceImpl defaultSequence = new DefaultSequenceImpl();
        defaultSequence.setName("seq_norm_test2");
        defaultSequence.setSequenceRepository(sequenceRepository);
        defaultSequence.init();

        Long a1 = defaultSequence.nextValue(500);
        assertEquals(a1, Long.valueOf(2500L));
        Long a2 = defaultSequence.nextValue(500);
        assertEquals(a2, Long.valueOf(3000L));

        Long nextId = null;
        for (int i = 0; i < 1000; i++) {
            nextId = defaultSequence.nextValue();
        }
        assertEquals(nextId, Long.valueOf(5000L));

        Map<String, Object> sequenceTableData = jdbcTemplate
                .queryForMap("select * from `sequence` where `name` = '" + defaultSequence.getName() + "'");
        log.info("sequence table data: {}{}", System.lineSeparator(), sequenceTableData);
        assertEquals(sequenceTableData.get("value"), 4000L);
    }
}
