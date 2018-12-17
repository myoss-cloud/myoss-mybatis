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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;

import app.myoss.cloud.sequence.constants.SequenceConstants;
import app.myoss.cloud.sequence.impl.RdsSequenceRepository;
import lombok.Data;

/**
 * 使用关系数据库生成序列，属性配置
 *
 * @author Jerry.Chen
 * @since 2018年12月17日 下午1:55:47
 */
@Data
@ConfigurationProperties(prefix = SequenceConstants.RDS_CONFIG_PREFIX)
public class RdsSequenceProperties {
    /**
     * 是否启用
     */
    private boolean                 enabled;
    /**
     * 自适应调整
     */
    private boolean                 adjust                = true;
    /**
     * 重试次数，在多个DataSource的场景下，建议设置成1-2次。默认为2次
     */
    private int                     retryTimes            = RdsSequenceRepository.DEFAULT_RETRY_TIMES;
    /**
     * 数据库名：从哪些数据库中取ID，如果在末尾插入"-OFF",该源将被关掉，该源占据的SQL段会被保留。如果没有设置，会从
     * dataSourceBeanName 和 dataSourceMap 中取值
     */
    private List<String>            dbGroupKeys;
    /**
     * 数据源，配置 spring bean name
     */
    private Set<String>             dataSourceBeanName;
    /**
     * 数据源
     */
    private Map<String, DataSource> dataSourceMap;
    /**
     * 数据源的个数
     */
    private int                     dataSourceCount       = RdsSequenceRepository.DEFAULT_DATA_SOURCE_COUNT;
    /**
     * 内步长，默认为1000，取值在1-100000之间
     */
    private int                     innerStep             = RdsSequenceRepository.DEFAULT_INNER_STEP;
    /**
     * 序列数据库表名，默认为sequence
     */
    private String                  tableName             = RdsSequenceRepository.DEFAULT_TABLE_NAME;
    /**
     * 存储序列名称的列名，默认为name
     */
    private String                  nameColumnName        = RdsSequenceRepository.DEFAULT_NAME_COLUMN_NAME;
    /**
     * 存储序列值的列名，默认为value
     */
    private String                  valueColumnName       = RdsSequenceRepository.DEFAULT_VALUE_COLUMN_NAME;
    /**
     * 存储序列创建时间的列名，默认为gmt_created
     */
    private String                  gmtCreatedColumnName  = RdsSequenceRepository.DEFAULT_GMT_CREATED_COLUMN_NAME;
    /**
     * 存储序列最后更新时间的列名，默认为gmt_modified
     */
    private String                  gmtModifiedColumnName = RdsSequenceRepository.DEFAULT_GMT_MODIFIED_COLUMN_NAME;
}
