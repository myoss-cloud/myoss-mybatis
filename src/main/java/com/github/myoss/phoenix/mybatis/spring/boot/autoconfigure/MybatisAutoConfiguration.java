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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.github.myoss.phoenix.mybatis.mapper.register.MapperInterfaceRegister;
import com.github.myoss.phoenix.mybatis.plugin.ParameterHandlerCustomizer;
import com.github.myoss.phoenix.mybatis.plugin.ParameterHandlerInterceptor;
import com.github.myoss.phoenix.mybatis.spring.boot.autoconfigure.MybatisProperties.MapperScanner;
import com.github.myoss.phoenix.mybatis.spring.mapper.ClassPathMapperScanner;
import com.github.myoss.phoenix.mybatis.spring.mapper.MapperScannerConfigurer;
import com.github.myoss.phoenix.mybatis.table.Sequence;
import com.github.myoss.phoenix.mybatis.table.TableConfig;
import com.github.myoss.phoenix.mybatis.table.TableMetaObject;

/**
 * MyBatis Spring Boot项目自动配置
 *
 * @author Jerry.Chen 2018年4月23日 上午11:07:07
 */
@Slf4j
@EnableConfigurationProperties({ MybatisProperties.class })
@ConditionalOnClass({ SqlSessionFactory.class, SqlSessionFactoryBean.class })
@ConditionalOnBean(DataSource.class)
@Configuration
public class MybatisAutoConfiguration {
    private final MybatisProperties             properties;
    private final Interceptor[]                 interceptors;
    private final ResourceLoader                resourceLoader;
    private final DatabaseIdProvider            databaseIdProvider;
    private final List<ConfigurationCustomizer> configurationCustomizers;
    private final ApplicationContext            applicationContext;

    public MybatisAutoConfiguration(MybatisProperties properties, ObjectProvider<Interceptor[]> interceptorsProvider,
                                    ResourceLoader resourceLoader,
                                    ObjectProvider<DatabaseIdProvider> databaseIdProvider,
                                    ObjectProvider<List<ConfigurationCustomizer>> configurationCustomizersProvider,
                                    ApplicationContext applicationContext) {
        this.properties = properties;
        if (this.properties.getTableConfig() == null) {
            log.debug("MybatisProperties.tableConfig not config, initialize default");
            this.properties.setTableConfig(new TableConfig());
        }
        this.interceptors = interceptorsProvider.getIfAvailable();
        this.resourceLoader = resourceLoader;
        this.databaseIdProvider = databaseIdProvider.getIfAvailable();
        this.configurationCustomizers = configurationCustomizersProvider.getIfAvailable();
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void checkConfigFileExists() {
        if (this.properties.isCheckConfigLocation() && StringUtils.hasText(this.properties.getConfigLocation())) {
            Resource resource = this.resourceLoader.getResource(this.properties.getConfigLocation());
            Assert.state(resource.exists(), "Cannot find config location: " + resource
                    + " (please add config file or check your Mybatis configuration)");
        }

        // 获取所有的 Sequence Bean，用于后面初始化 TableSequence
        Map<String, Sequence> beanMap = this.applicationContext.getBeansOfType(Sequence.class);
        if (!CollectionUtils.isEmpty(beanMap)) {
            for (Entry<String, Sequence> entry : beanMap.entrySet()) {
                TableMetaObject.addSequenceBean(entry.getKey(), entry.getValue());
            }
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        if (StringUtils.hasText(this.properties.getConfigLocation())) {
            factory.setConfigLocation(this.resourceLoader.getResource(this.properties.getConfigLocation()));
        }
        applyConfiguration(factory);
        if (this.properties.getConfigurationProperties() != null) {
            factory.setConfigurationProperties(this.properties.getConfigurationProperties());
        }
        applyPlugins(factory);
        if (this.databaseIdProvider != null) {
            factory.setDatabaseIdProvider(this.databaseIdProvider);
        }
        if (StringUtils.hasLength(this.properties.getTypeAliasesPackage())) {
            factory.setTypeAliasesPackage(this.properties.getTypeAliasesPackage());
        }
        if (StringUtils.hasLength(this.properties.getTypeHandlersPackage())) {
            factory.setTypeHandlersPackage(this.properties.getTypeHandlersPackage());
        }
        Resource[] mapperLocations = this.properties.resolveMapperLocations();
        if (!ObjectUtils.isEmpty(mapperLocations)) {
            factory.setMapperLocations(mapperLocations);
        }
        return factory.getObject();
    }

    private void applyPlugins(SqlSessionFactoryBean factory) {
        Map<String, ParameterHandlerCustomizer> beanMap = this.applicationContext
                .getBeansOfType(ParameterHandlerCustomizer.class);
        boolean emptyInterceptors = ObjectUtils.isEmpty(this.interceptors);
        if (CollectionUtils.isEmpty(beanMap)) {
            if (!emptyInterceptors) {
                // 如果没有 ParameterHandlerCustomizer Bean对象，则直接设置
                factory.setPlugins(this.interceptors);
            }
            return;
        }

        // 有 ParameterHandlerCustomizer Bean对象
        ParameterHandlerCustomizer parameterHandlerCustomizer = beanMap.entrySet().iterator().next().getValue();
        if (emptyInterceptors) {
            // 没有 interceptors ，则创建新的 ParameterHandlerInterceptor
            ParameterHandlerInterceptor interceptor = new ParameterHandlerInterceptor(parameterHandlerCustomizer);
            factory.setPlugins(new Interceptor[] { interceptor });
            return;
        }

        if (Stream.of(interceptors).anyMatch(s -> s instanceof ParameterHandlerInterceptor)) {
            // 已经有 ParameterHandlerInterceptor，则不处理
            factory.setPlugins(this.interceptors);
        } else {
            // 添加新的 ParameterHandlerInterceptor
            ParameterHandlerInterceptor interceptor = new ParameterHandlerInterceptor(parameterHandlerCustomizer);
            Interceptor[] plugins = ArrayUtils.add(this.interceptors, interceptor);
            factory.setPlugins(plugins);
        }
    }

    private void applyConfiguration(SqlSessionFactoryBean factory) {
        org.apache.ibatis.session.Configuration configuration = this.properties.getConfiguration();
        if (configuration == null && !StringUtils.hasText(this.properties.getConfigLocation())) {
            configuration = new org.apache.ibatis.session.Configuration();
        }
        if (configuration != null && !CollectionUtils.isEmpty(this.configurationCustomizers)) {
            for (ConfigurationCustomizer customizer : this.configurationCustomizers) {
                customizer.customize(configuration);
            }
        }
        factory.setConfiguration(configuration);
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        ExecutorType executorType = this.properties.getExecutorType();
        if (executorType != null) {
            return new SqlSessionTemplate(sqlSessionFactory, executorType);
        } else {
            return new SqlSessionTemplate(sqlSessionFactory);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public MapperInterfaceRegister mapperInterfaceRegister() {
        return new MapperInterfaceRegister(this.properties.getTableConfig());
    }

    /**
     * {@link org.mybatis.spring.annotation.MapperScan} ultimately ends up
     * creating instances of {@link MapperFactoryBean}. If
     * {@link org.mybatis.spring.annotation.MapperScan} is used then this
     * auto-configuration is not needed. If it is _not_ used, however, then this
     * will bring in a bean registrar and automatically register components
     * based on the same component-scanning path as Spring Boot itself.
     */
    @ConditionalOnMissingBean(MapperFactoryBean.class)
    @Import({ AutoConfiguredMapperScannerRegistrar.class })
    @Configuration
    public static class MapperScannerRegistrarNotFoundConfiguration {

        @PostConstruct
        public void afterPropertiesSet() {
            log.debug("No {} found.", MapperFactoryBean.class.getName());
        }

    }

    /**
     * This will just scan the same base package as Spring Boot does. If you
     * want more power, you can explicitly use
     * {@link org.mybatis.spring.annotation.MapperScan} but this will get typed
     * mappers working correctly, out-of-the-box, similar to using Spring Data
     * JPA repositories.
     */
    public static class AutoConfiguredMapperScannerRegistrar implements BeanFactoryAware,
            ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {
        @Setter
        private BeanFactory    beanFactory;
        @Setter
        private ResourceLoader resourceLoader;
        @Setter
        private Environment    environment;

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            log.debug("Searching for mappers annotated with @Mapper");
            ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
            try {
                if (this.resourceLoader != null) {
                    scanner.setResourceLoader(this.resourceLoader);
                }

                List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
                if (log.isDebugEnabled()) {
                    packages.forEach(pkg -> log.debug("Using auto-configuration base package '{}'", pkg));
                }

                // 暂时不启用，自动注入： {@link MybatisAutoConfiguration#mapperInterfaceRegister()}
                //                // 通用Mapper接口注册器
                //                Binder propertySourcesBinder = Binder.get(environment);
                //                MybatisProperties mybatisProperties = propertySourcesBinder.bind(MybatisProperties.MYBATIS_PREFIX,
                //                        MybatisProperties.class).get();
                //                MapperInterfaceRegister mapperInterfaceRegister = new MapperInterfaceRegister(
                //                        mybatisProperties.getTableConfig());
                //                scanner.setMapperInterfaceRegister(mapperInterfaceRegister);

                scanner.setAnnotationClass(Mapper.class);
                scanner.registerFilters();
                scanner.doScan(StringUtils.toStringArray(packages));
            } catch (IllegalStateException ex) {
                log.debug("Could not determine auto-configuration package, automatic mapper scanning disabled.", ex);
            }
        }
    }

    /**
     * 自动扫描 Mapper Interface
     *
     * @see MapperScannerConfigurer
     */
    @ConditionalOnExpression("#{'${" + MybatisProperties.MYBATIS_MAPPER_SCANNER_PREFIX
            + ".base-package:}'.length() > 0}")
    @Configuration
    public static class AutoConfiguredMapperScannerRegistrar2 {
        public AutoConfiguredMapperScannerRegistrar2() {
            log.debug(MybatisProperties.MYBATIS_MAPPER_SCANNER_PREFIX + ".base-package} is config [{}].",
                    AutoConfiguredMapperScannerRegistrar2.class.getName());
        }

        @ConditionalOnMissingBean
        @Bean
        public MapperScannerConfigurer mapperScannerConfigurer(Environment environment) {
            Binder binder = Binder.get(environment);
            MapperScanner scanner = binder.bind(MybatisProperties.MYBATIS_MAPPER_SCANNER_PREFIX, MapperScanner.class)
                    .get();
            String basePackage = scanner.getBasePackage();
            Objects.requireNonNull(basePackage, "Property 'basePackage' is required");

            MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
            mapperScannerConfigurer.setBasePackage(org.apache.commons.lang3.StringUtils.defaultIfBlank(basePackage,
                    null));
            mapperScannerConfigurer.setSqlSessionFactoryBeanName(org.apache.commons.lang3.StringUtils.defaultIfBlank(
                    scanner.getSqlSessionFactoryName(), "sqlSessionFactory"));
            mapperScannerConfigurer.setSqlSessionTemplateBeanName(org.apache.commons.lang3.StringUtils.defaultIfBlank(
                    scanner.getSqlSessionTemplateBeanName(), "sqlSessionTemplate"));
            if (scanner.getAnnotationClass() != null) {
                mapperScannerConfigurer.setAnnotationClass(scanner.getAnnotationClass());
            }
            if (scanner.getMarkerInterface() != null) {
                mapperScannerConfigurer.setMarkerInterface(scanner.getMarkerInterface());
            }
            return mapperScannerConfigurer;
        }
    }
}
