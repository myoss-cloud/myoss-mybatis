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

package app.myoss.cloud.sequence.spring.boot.autoconfigure;

import java.util.Optional;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.alibaba.fastjson.JSON;

import app.myoss.cloud.mybatis.table.TableMetaObject;
import app.myoss.cloud.sequence.Sequence;
import app.myoss.cloud.sequence.SequenceRepository;
import app.myoss.cloud.sequence.constants.SequenceConstants;
import app.myoss.cloud.sequence.impl.DefaultSequenceImpl;
import app.myoss.cloud.sequence.impl.RdsSequenceRepository;
import app.myoss.cloud.sequence.utils.DefaultSequenceUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 自动配置使用关系数据库生成序列
 *
 * @author Jerry.Chen
 * @since 2018年12月17日 下午1:54:47
 */
@Slf4j
@EnableConfigurationProperties({ RdsSequenceProperties.class })
@ConditionalOnProperty(prefix = SequenceConstants.RDS_CONFIG_PREFIX, value = "enabled", matchIfMissing = false)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@Configuration
public class RdsSequenceAutoConfiguration {
    private RdsSequenceProperties properties;

    /**
     * 初始化
     *
     * @param properties 使用关系数据库生成序列，属性配置
     */
    public RdsSequenceAutoConfiguration(RdsSequenceProperties properties) {
        this.properties = properties;
        if (log.isInfoEnabled()) {
            log.info("init RdsSequence auto configuration, properties: {}", JSON.toJSONString(properties));
        }
    }

    /**
     * 构建 "使用关系数据库生成序列" 实例对象
     *
     * @param applicationContext Spring Application Context
     * @return "使用关系数据库生成序列" 实例对象
     */
    @Primary
    @ConditionalOnMissingBean
    @Bean(initMethod = "init")
    public RdsSequenceRepository rdsSequenceRepository(ApplicationContext applicationContext) {
        return DefaultSequenceUtils.buildRdsSequenceRepository(applicationContext, this.properties, false);
    }

    /**
     * 初始默认的序列号生成器
     * {@link app.myoss.cloud.sequence.impl.DefaultSequenceImpl}，用于数据库表生成主键id
     *
     * @param sequenceRepository SequenceRepository 实例对象
     * @param applicationContext Spring Application Context
     * @return 空对象
     */
    @ConditionalOnClass({ Sequence.class, TableMetaObject.class })
    @ConditionalOnBean(SequenceRepository.class)
    @ConditionalOnMissingBean(name = "initDefaultSequence")
    @Bean
    public Optional<DefaultSequenceImpl> initDefaultSequence(SequenceRepository sequenceRepository,
                                                             ApplicationContext applicationContext) {
        DefaultSequenceUtils.initDefaultSequence(TableMetaObject.getSequenceBeanMap().values(), sequenceRepository,
                applicationContext);
        return Optional.empty();
    }
}
