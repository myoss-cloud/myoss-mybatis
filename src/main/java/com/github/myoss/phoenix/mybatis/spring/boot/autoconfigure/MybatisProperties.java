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

package com.github.myoss.phoenix.mybatis.spring.boot.autoconfigure;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.github.myoss.phoenix.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration.AutoConfiguredMapperScannerRegistrar2;
import com.github.myoss.phoenix.mybatis.table.TableConfig;

import lombok.Data;

/**
 * MyBatis Spring Boot项目配置属性
 *
 * @author Jerry.Chen
 * @since 2018年4月24日 下午3:28:31
 */
@Data
@ConfigurationProperties(prefix = MybatisProperties.MYBATIS_PREFIX)
public class MybatisProperties {
    public static final String                   MYBATIS_PREFIX                = "mybatis";
    public static final String                   MYBATIS_MAPPER_SCANNER_PREFIX = "mybatis.mapper-scanner";
    private static final ResourcePatternResolver RESOURCE_RESOLVER             = new PathMatchingResourcePatternResolver();

    /**
     * Location of MyBatis xml config file.
     */
    private String                               configLocation;

    /**
     * Locations of MyBatis xml mapper files.
     */
    private String[]                             mapperLocations;

    /**
     * Packages to search type aliases. (Package delimiters are ",; \t\n")
     */
    private String                               typeAliasesPackage;

    /**
     * Packages to search for type handlers. (Package delimiters are ",; \t\n")
     */
    private String                               typeHandlersPackage;

    /**
     * Indicates whether perform presence check of the MyBatis xml config file.
     */
    private boolean                              checkConfigLocation           = false;

    /**
     * Execution mode for {@link org.mybatis.spring.SqlSessionTemplate}.
     */
    private ExecutorType                         executorType;

    /**
     * Externalized properties for MyBatis configuration.
     */
    private Properties                           configurationProperties;

    /**
     * A Configuration object for customize default settings. If
     * {@link #configLocation} is specified, this property is not used.
     */
    @NestedConfigurationProperty
    private Configuration                        configuration;

    /**
     * 实体类映射数据库表的全局配置
     * <p>
     * 优先级：实体类上的 {@link com.github.myoss.phoenix.mybatis.table.annotation.Table}
     * 注解 -> 全局配置 <code>tableConfig</code>
     */
    @NestedConfigurationProperty
    private TableConfig                          tableConfig;

    /**
     * 自动扫描 Mapper Interface 配置
     * <p>
     * see as {@link AutoConfiguredMapperScannerRegistrar2}
     */
    private MapperScanner                        mapperScanner;

    @Data
    @ConfigurationProperties(prefix = MYBATIS_MAPPER_SCANNER_PREFIX)
    public static class MapperScanner {
        /**
         * base package name
         */
        private String                      basePackage;
        /**
         * Bean name of the {@link org.apache.ibatis.session.SqlSessionFactory}
         */
        private String                      sqlSessionFactoryName;
        /**
         *
         */
        private String                      sqlSessionTemplateBeanName;
        /**
         * annotationClass annotation class
         */
        private Class<? extends Annotation> annotationClass;
        /**
         * superClass parent class
         */
        private Class<?>                    markerInterface;
    }

    public Resource[] resolveMapperLocations() {
        return Stream.of(Optional.ofNullable(this.mapperLocations).orElse(new String[0]))
                .flatMap(location -> Stream.of(getResources(location))).toArray(Resource[]::new);
    }

    private Resource[] getResources(String location) {
        try {
            return RESOURCE_RESOLVER.getResources(location);
        } catch (IOException e) {
            return new Resource[0];
        }
    }
}
