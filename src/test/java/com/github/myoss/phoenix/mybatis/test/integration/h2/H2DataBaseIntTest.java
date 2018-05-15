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

package com.github.myoss.phoenix.mybatis.test.integration.h2;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.alibaba.fastjson.JSON;
import com.github.myoss.phoenix.mybatis.table.Sequence;

/**
 * 集成测试基础配置，使用H2内存数据库
 *
 * @author Jerry.Chen 2018年5月11日 上午11:02:05
 */
@Slf4j
public class H2DataBaseIntTest {
    @ImportAutoConfiguration(JdbcTemplateAutoConfiguration.class)
    @EnableAutoConfiguration
    @ComponentScan(basePackageClasses = H2DataBaseIntTest.class)
    @Configuration
    public static class IntAutoConfig {
        @Bean
        public SequenceCustomizer seqUserLog(JdbcTemplate jdbcTemplate) {
            return new SequenceCustomizer() {
                @Override
                public Object nextValue(Object parameter) {
                    Long nextId = jdbcTemplate.queryForObject("select ifnull(max(`id`) ,0) + 1 from t_sys_user_log",
                            Long.class);
                    log.info("nextId: {}, parameter: {}", nextId, JSON.toJSONString(parameter));
                    return nextId;
                }
            };
        }
    }

    public static class SequenceCustomizer implements Sequence {
        @Override
        public Object nextValue(Object parameter) {
            return null;
        }
    }
}
