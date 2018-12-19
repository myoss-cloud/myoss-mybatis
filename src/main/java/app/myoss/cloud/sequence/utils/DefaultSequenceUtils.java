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

package app.myoss.cloud.sequence.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import app.myoss.cloud.core.exception.BizRuntimeException;
import app.myoss.cloud.mybatis.table.TableSequence;
import app.myoss.cloud.sequence.Sequence;
import app.myoss.cloud.sequence.SequenceRepository;
import app.myoss.cloud.sequence.impl.DefaultSequenceImpl;
import app.myoss.cloud.sequence.impl.RdsSequenceRepository;
import app.myoss.cloud.sequence.spring.boot.autoconfigure.RdsSequenceProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认的序列号生成器工具类
 *
 * @author Jerry.Chen
 * @since 2018年12月17日 下午3:12:00
 */
@Slf4j
public class DefaultSequenceUtils {

    /**
     * 构建 "使用关系数据库生成序列" 实例对象
     *
     * @param applicationContext Spring Application Context
     * @param sequenceConfig Rds Sequence 属性配置
     * @param init 是否进行初始化
     * @return "使用关系数据库生成序列" 实例对象
     */
    public static RdsSequenceRepository buildRdsSequenceRepository(ApplicationContext applicationContext,
                                                                   RdsSequenceProperties sequenceConfig, boolean init) {
        RdsSequenceRepository repository = new RdsSequenceRepository();
        repository.setDbGroupKeys(sequenceConfig.getDbGroupKeys());
        repository.setDataSourceMap(sequenceConfig.getDataSourceMap());
        if (applicationContext != null && !CollectionUtils.isEmpty(sequenceConfig.getDataSourceBeanName())) {
            Map<String, DataSource> dataSourceMap = sequenceConfig.getDataSourceBeanName()
                    .stream()
                    .collect(Collectors.toMap(Function.identity(),
                            s -> applicationContext.getBean(s, DataSource.class)));
            if (!CollectionUtils.isEmpty(dataSourceMap)) {
                if (CollectionUtils.isEmpty(repository.getDataSourceMap())) {
                    repository.setDataSourceMap(dataSourceMap);
                } else {
                    repository.getDataSourceMap().putAll(dataSourceMap);
                }
            }
        }
        repository.setDataSourceCount(sequenceConfig.getDataSourceCount());
        repository.setInnerStep(sequenceConfig.getInnerStep());
        repository.setRetryTimes(sequenceConfig.getRetryTimes());
        repository.setTableName(sequenceConfig.getTableName());
        repository.setNameColumnName(sequenceConfig.getNameColumnName());
        repository.setValueColumnName(sequenceConfig.getValueColumnName());
        repository.setGmtCreatedColumnName(sequenceConfig.getGmtCreatedColumnName());
        repository.setGmtModifiedColumnName(sequenceConfig.getGmtModifiedColumnName());
        repository.setAdjust(sequenceConfig.isAdjust());
        if (init) {
            repository.init();
        }
        return repository;
    }

    /**
     * 初始化 {@link DefaultSequenceImpl} 序列生成器，并注册到 Spring Application Context 中
     *
     * @param sequences 待初始化的 Sequence 实例集合
     * @param sequenceRepository SequenceRepository 实例对象
     * @param applicationContext Spring Application Context
     */
    public static void initDefaultSequence(Collection<app.myoss.cloud.mybatis.table.Sequence> sequences,
                                           SequenceRepository sequenceRepository,
                                           ApplicationContext applicationContext) {
        Map<String, Sequence> map = new HashMap<>();
        for (app.myoss.cloud.mybatis.table.Sequence itemValue : sequences) {
            Class sequenceDelegateClass = itemValue.getSequenceDelegateClass();
            if (sequenceDelegateClass != Sequence.class) {
                continue;
            }
            if (itemValue.getSequenceDelegate() != null) {
                continue;
            }
            TableSequence tableSequence = itemValue.getTableInfo().getTableSequence();
            String sequenceName = tableSequence.getSequenceName();
            if (StringUtils.isBlank(sequenceName)) {
                throw new NullPointerException("sequenceName is null, tableInfo: " + itemValue.getTableInfo());
            }
            if (map.containsKey(sequenceName)) {
                Sequence sequence = map.get(sequenceName);
                itemValue.setSequenceDelegate(sequence);
                continue;
            }
            DefaultSequenceImpl sequence = initDefaultSequence(applicationContext, sequenceName, null,
                    sequenceRepository);
            if (sequence == null) {
                throw new BizRuntimeException("create DefaultSequenceImpl failed, sequenceName = " + sequenceName
                        + ", tableName = " + itemValue.getTableInfo().getTableName());
            }
            itemValue.setSequenceDelegate(sequence);
            map.put(sequenceName, sequence);
        }
    }

    /**
     * 初始化 {@link DefaultSequenceImpl} 序列生成器，并注册到 Spring Application Context 中
     *
     * @param applicationContext Spring Application Context
     * @param sequenceName 待初始化的 sequence 名称
     * @param sequenceRepositoryBeanName SequenceRepository 实例对象Bean Name【参数
     *            {@code sequenceRepositoryBeanName} 和
     *            {@code sequenceRepository} 二选一】
     * @param sequenceRepository SequenceRepository 实例对象【参数
     *            {@code sequenceRepositoryBeanName} 和
     *            {@code sequenceRepository} 二选一】
     * @return 生成的 {@link DefaultSequenceImpl} 序列生成器
     */
    public static DefaultSequenceImpl initDefaultSequence(ApplicationContext applicationContext, String sequenceName,
                                                          String sequenceRepositoryBeanName,
                                                          SequenceRepository sequenceRepository) {
        if (applicationContext.containsBean(sequenceName)) {
            return applicationContext.getBean(sequenceName, DefaultSequenceImpl.class);
        }
        if (sequenceRepository != null || StringUtils.isNotBlank(sequenceRepositoryBeanName)) {
            try {
                // 创建Bean
                DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext
                        .getAutowireCapableBeanFactory();
                BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                        .genericBeanDefinition(DefaultSequenceImpl.class);
                beanDefinitionBuilder.addPropertyValue("name", sequenceName);
                if (sequenceRepository != null) {
                    beanDefinitionBuilder.addPropertyValue("sequenceRepository", sequenceRepository);
                } else {
                    beanDefinitionBuilder.addPropertyValue("sequenceRepository",
                            new RuntimeBeanReference(sequenceRepositoryBeanName));
                }
                AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
                beanDefinition.setInitMethodName("init");

                // 注册Bean
                log.info("register DefaultSequenceImpl: {}", sequenceName);
                beanFactory.registerBeanDefinition(sequenceName, beanDefinition);
                return applicationContext.getBean(sequenceName, DefaultSequenceImpl.class);
            } catch (Exception ex) {
                throw new BizRuntimeException("create DefaultSequenceImpl failed, sequenceName = " + sequenceName, ex);
            }
        }
        return null;
    }
}
