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

package app.myoss.cloud.mybatis.spring.boot.autoconfigure;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

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
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
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

import app.myoss.cloud.mybatis.mapper.register.MapperInterfaceRegister;
import app.myoss.cloud.mybatis.plugin.ParameterHandlerCustomizer;
import app.myoss.cloud.mybatis.plugin.ParameterHandlerInterceptor;
import app.myoss.cloud.mybatis.spring.boot.autoconfigure.MybatisProperties.MapperScanner;
import app.myoss.cloud.mybatis.spring.mapper.ClassPathMapperScanner;
import app.myoss.cloud.mybatis.spring.mapper.MapperScannerConfigurer;
import app.myoss.cloud.mybatis.table.Sequence;
import app.myoss.cloud.mybatis.table.TableConfig;
import app.myoss.cloud.mybatis.table.TableMetaObject;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * MyBatis Spring Boot项目自动配置
 *
 * @author Jerry.Chen
 * @since 2018年4月23日 上午11:07:07
 */
@Slf4j
@ConditionalOnProperty(prefix = MybatisProperties.MYBATIS_PREFIX, value = "enabled", matchIfMissing = true)
@EnableConfigurationProperties({ MybatisProperties.class })
@ConditionalOnClass({ SqlSessionFactory.class, SqlSessionFactoryBean.class })
@ConditionalOnBean(DataSource.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@Configuration
public class MybatisAutoConfiguration {
    /**
     * MyBatis Spring Boot项目配置属性
     */
    private final MybatisProperties             properties;
    /**
     * Mybatis Interceptor Spring Bean【可选】
     */
    private final Interceptor[]                 interceptors;
    /**
     * 自定义配置 Spring Bean【可选】
     */
    private final List<ConfigurationCustomizer> configurationCustomizers;
    /**
     * Spring Application Context
     */
    private final ApplicationContext            applicationContext;
    /**
     * resource loader
     */
    private final ResourceLoader                resourceLoader;

    /**
     * 初始化 MyBatis Spring Boot 项目自动配置
     *
     * @param properties MyBatis Spring Boot项目配置属性
     * @param interceptorsProvider Mybatis Interceptor Spring Bean【可选】
     * @param configurationCustomizersProvider 自定义配置 Spring Bean【可选】
     * @param applicationContext Spring Application Context
     * @param resourceLoader resource loader
     */
    public MybatisAutoConfiguration(MybatisProperties properties, ObjectProvider<Interceptor[]> interceptorsProvider,
                                    ObjectProvider<List<ConfigurationCustomizer>> configurationCustomizersProvider,
                                    ApplicationContext applicationContext, ResourceLoader resourceLoader) {
        this.properties = properties;
        if (this.properties.getTableConfig() == null) {
            log.debug("MybatisProperties.tableConfig not config, initialize default");
            this.properties.setTableConfig(new TableConfig());
        }
        this.interceptors = interceptorsProvider.getIfAvailable();
        this.resourceLoader = resourceLoader;
        this.configurationCustomizers = configurationCustomizersProvider.getIfAvailable();
        this.applicationContext = applicationContext;
    }

    /**
     * 检查配置文件是否存在。当前对象被 Spring 创建之后，会调用此方法。
     */
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

    /**
     * 初始化 SqlSessionFactory
     *
     * @param dataSource 数据源
     * @return SqlSessionFactory 实例对象
     * @throws Exception 异常信息
     */
    @Bean
    @ConditionalOnMissingBean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factory = createSqlSessionFactoryBean(this.applicationContext, this.resourceLoader,
                dataSource, this.properties, this.configurationCustomizers, this.interceptors);
        return factory.getObject();
    }

    /**
     * 创建 SqlSessionFactory
     *
     * @param applicationContext 自定义配置 Spring Bean【可选】
     * @param resourceLoader resource loader
     * @param dataSource 数据源
     * @param properties MyBatis Spring Boot项目配置属性
     * @param configurationCustomizers 自定义配置 Spring Bean【可选】
     * @param interceptors Mybatis Interceptor Spring Bean【可选】
     * @return SqlSessionFactory 实例对象
     */
    public static SqlSessionFactoryBean createSqlSessionFactoryBean(ApplicationContext applicationContext,
                                                                    ResourceLoader resourceLoader,
                                                                    DataSource dataSource, MybatisProperties properties,
                                                                    List<ConfigurationCustomizer> configurationCustomizers,
                                                                    Interceptor[] interceptors) {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        if (StringUtils.hasText(properties.getConfigLocation())) {
            factory.setConfigLocation(resourceLoader.getResource(properties.getConfigLocation()));
        }
        applyConfiguration(factory, properties, configurationCustomizers);
        if (properties.getConfigurationProperties() != null) {
            factory.setConfigurationProperties(properties.getConfigurationProperties());
        }
        applyPlugins(applicationContext, factory, interceptors);
        Map<String, DatabaseIdProvider> databaseIdProviderMap = applicationContext
                .getBeansOfType(DatabaseIdProvider.class);
        if (!databaseIdProviderMap.isEmpty()) {
            DatabaseIdProvider databaseIdProvider = databaseIdProviderMap.values().iterator().next();
            factory.setDatabaseIdProvider(databaseIdProvider);
        }
        if (StringUtils.hasLength(properties.getTypeAliasesPackage())) {
            factory.setTypeAliasesPackage(properties.getTypeAliasesPackage());
        }
        if (StringUtils.hasLength(properties.getTypeHandlersPackage())) {
            factory.setTypeHandlersPackage(properties.getTypeHandlersPackage());
        }
        Resource[] mapperLocations = properties.resolveMapperLocations();
        if (!ObjectUtils.isEmpty(mapperLocations)) {
            factory.setMapperLocations(mapperLocations);
        }
        return factory;
    }

    /**
     * 为 SqlSessionFactory 配置 Mybatis 拦截器插件
     *
     * @param applicationContext 自定义配置 Spring Bean【可选】
     * @param factory SqlSessionFactory 实例对象
     * @param interceptors Mybatis Interceptor 实例对象【可选】
     */
    public static void applyPlugins(ApplicationContext applicationContext, SqlSessionFactoryBean factory,
                                    Interceptor[] interceptors) {
        Map<String, ParameterHandlerCustomizer> beanMap = applicationContext
                .getBeansOfType(ParameterHandlerCustomizer.class);
        boolean emptyInterceptors = ObjectUtils.isEmpty(interceptors);
        if (CollectionUtils.isEmpty(beanMap)) {
            if (!emptyInterceptors) {
                // 如果没有 ParameterHandlerCustomizer Bean对象，则直接设置
                factory.setPlugins(interceptors);
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
            factory.setPlugins(interceptors);
        } else {
            // 添加新的 ParameterHandlerInterceptor
            ParameterHandlerInterceptor interceptor = new ParameterHandlerInterceptor(parameterHandlerCustomizer);
            Interceptor[] plugins = ArrayUtils.add(interceptors, interceptor);
            factory.setPlugins(plugins);
        }
    }

    /**
     * 为 SqlSessionFactory 配置"自定义配置"插件
     *
     * @param factory SqlSessionFactory 实例对象
     * @param properties MyBatis Spring Boot项目配置属性
     * @param configurationCustomizers 自定义配置【可选】
     */
    public static void applyConfiguration(SqlSessionFactoryBean factory, MybatisProperties properties,
                                          List<ConfigurationCustomizer> configurationCustomizers) {
        org.apache.ibatis.session.Configuration configuration = properties.getConfiguration();
        if (configuration == null && !StringUtils.hasText(properties.getConfigLocation())) {
            configuration = new org.apache.ibatis.session.Configuration();
        }
        if (configuration != null && !CollectionUtils.isEmpty(configurationCustomizers)) {
            for (ConfigurationCustomizer customizer : configurationCustomizers) {
                customizer.customize(configuration);
            }
        }
        factory.setConfiguration(configuration);
    }

    /**
     * 初始化 SqlSessionTemplate
     *
     * @param sqlSessionFactory SqlSessionFactory 实例对象
     * @return SqlSessionTemplate 实例对象
     */
    @Bean
    @ConditionalOnMissingBean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return createSqlSessionTemplate(sqlSessionFactory, this.properties);
    }

    /**
     * 创建 SqlSessionTemplate
     *
     * @param sqlSessionFactory SqlSessionFactory 实例对象
     * @param properties MyBatis Spring Boot项目配置属性
     * @return SqlSessionTemplate 实例对象
     */
    public static SqlSessionTemplate createSqlSessionTemplate(SqlSessionFactory sqlSessionFactory,
                                                              MybatisProperties properties) {
        ExecutorType executorType = properties.getExecutorType();
        if (executorType != null) {
            return new SqlSessionTemplate(sqlSessionFactory, executorType);
        } else {
            return new SqlSessionTemplate(sqlSessionFactory);
        }
    }

    /**
     * 初始化通用 Mapper 接口注册器
     *
     * @return MapperInterfaceRegister 实例对象
     */
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

        /**
         * 属性配置
         */
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
    public static class AutoConfiguredMapperScannerRegistrar
            implements BeanFactoryAware, ImportBeanDefinitionRegistrar, ResourceLoaderAware {
        @Setter
        private BeanFactory    beanFactory;
        @Setter
        private ResourceLoader resourceLoader;

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                            BeanDefinitionRegistry registry) {
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
                log.warn("Could not determine auto-configuration package, automatic mapper scanning disabled.", ex);
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
        /**
         * 初始化自动扫描 Mapper Interface
         */
        public AutoConfiguredMapperScannerRegistrar2() {
            log.debug(MybatisProperties.MYBATIS_MAPPER_SCANNER_PREFIX + ".base-package} is config [{}].",
                    AutoConfiguredMapperScannerRegistrar2.class.getName());
        }

        /**
         * 初始化 MapperScannerConfigurer
         *
         * @param environment Spring environment
         * @return MapperScannerConfigurer 实例对象
         */
        @ConditionalOnMissingBean
        @Bean
        public MapperScannerConfigurer mapperScannerConfigurer(Environment environment) {
            Binder binder = Binder.get(environment);
            MapperScanner scanner = binder.bind(MybatisProperties.MYBATIS_MAPPER_SCANNER_PREFIX, MapperScanner.class)
                    .get();
            String basePackage = scanner.getBasePackage();
            Objects.requireNonNull(basePackage, "Property 'basePackage' is required");

            MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
            mapperScannerConfigurer.setBasePackage(basePackage);
            mapperScannerConfigurer.setSqlSessionFactoryBeanName(scanner.getSqlSessionFactoryName());
            mapperScannerConfigurer.setSqlSessionTemplateBeanName(scanner.getSqlSessionTemplateBeanName());
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
