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

package com.github.myoss.phoenix.mybatis.spring.mapper;

import static org.springframework.util.Assert.notNull;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import com.github.myoss.phoenix.mybatis.mapper.register.MapperInterfaceRegister;
import com.github.myoss.phoenix.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration.AutoConfiguredMapperScannerRegistrar2;

import lombok.Data;

/**
 * 配置扫描Class Path目录Mapper Interface，实现
 * {@link org.mybatis.spring.mapper.MapperScannerConfigurer} 类似的功能，但是用的是自己的
 * {@link ClassPathMapperScanner}
 *
 * @author Jerry.Chen
 * @since 2018年5月20日 下午12:47:18
 * @see ClassPathMapperScanner
 * @see MapperFactoryBean
 * @see AutoConfiguredMapperScannerRegistrar2
 * @see org.mybatis.spring.mapper.MapperScannerConfigurer
 */
@Data
public class MapperScannerConfigurer
        implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware, BeanNameAware {
    /**
     * basePackage base package name
     * <p>
     * This property lets you set the base package for your mapper interface
     * files.
     * <p>
     * You can set more than one package by using a semicolon or comma as a
     * separator.
     * <p>
     * Mappers will be searched for recursively starting in the specified
     * package(s).
     */
    private String                      basePackage;
    /**
     * Same as {@link MapperFactoryBean#setAddToConfig(boolean)}
     */
    private boolean                     addToConfig = true;
    /**
     * Bean name of the {@code SqlSessionFactory}
     * <p>
     * Specifies which {@code SqlSessionFactory} to use in the case that there
     * is more than one in the spring context. Usually this is only needed when
     * you have more than one datasource.
     * <p>
     * Note bean names are used, not bean references. This is because the
     * scanner loads early during the start process and it is too early to build
     * mybatis object instances.
     */
    private String                      sqlSessionFactoryBeanName;
    /**
     * Bean name of the {@code SqlSessionTemplate}
     * <p>
     * Specifies which {@code SqlSessionTemplate} to use in the case that there
     * is more than one in the spring context. Usually this is only needed when
     * you have more than one datasource.
     * <p>
     * Note bean names are used, not bean references. This is because the
     * scanner loads early during the start process and it is too early to build
     * mybatis object instances.
     */
    private String                      sqlSessionTemplateBeanName;
    /**
     * annotation class
     * <p>
     * This property specifies the annotation that the scanner will search for.
     * <p>
     * The scanner will register all interfaces in the base package that also
     * have the specified annotation.
     * <p>
     * Note this can be combined with markerInterface.
     */
    private Class<? extends Annotation> annotationClass;
    /**
     * parent class
     * <p>
     * This property specifies the parent that the scanner will search for.
     * <p>
     * The scanner will register all interfaces in the base package that also
     * have the specified interface class as a parent.
     * <p>
     * Note this can be combined with annotationClass.
     */
    private Class<?>                    markerInterface;

    /**
     * BeanFactory that enables injection of MyBatis mapper interfaces
     */
    private MapperFactoryBean<?>        mapperFactoryBean;
    /**
     * 通用 Mapper 接口注册器
     */
    private MapperInterfaceRegister     mapperInterfaceRegister;
    /**
     * Bean name of the {@code MapperInterfaceRegister}
     */
    private String                      mapperInterfaceRegisterBeanName;

    private ApplicationContext          applicationContext;

    private String                      beanName;

    private boolean                     processPropertyPlaceHolders;
    /**
     * the beanNameGenerator BeanNameGenerator that has been configured
     * <p>
     * Gets beanNameGenerator to be used while running the scanner.
     */
    private BeanNameGenerator           nameGenerator;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        notNull(this.basePackage, "Property 'basePackage' is required");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // left intentionally blank
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        if (this.processPropertyPlaceHolders) {
            processPropertyPlaceHolders();
        }

        ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
        scanner.setAddToConfig(this.addToConfig);
        scanner.setAnnotationClass(this.annotationClass);
        scanner.setMarkerInterface(this.markerInterface);
        scanner.setSqlSessionFactoryBeanName(this.sqlSessionFactoryBeanName);
        scanner.setSqlSessionTemplateBeanName(this.sqlSessionTemplateBeanName);
        if (this.mapperFactoryBean != null) {
            scanner.setMapperFactoryBean(this.mapperFactoryBean);
        }
        if (this.mapperInterfaceRegister != null) {
            scanner.setMapperInterfaceRegister(this.mapperInterfaceRegister);
        }
        if (!StringUtils.isEmpty(this.mapperInterfaceRegisterBeanName)) {
            scanner.setMapperInterfaceRegisterBeanName(this.mapperInterfaceRegisterBeanName);
        }
        scanner.setResourceLoader(this.applicationContext);
        scanner.setBeanNameGenerator(this.nameGenerator);
        scanner.registerFilters();
        String[] basePackages = StringUtils.tokenizeToStringArray(this.basePackage,
                ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
        scanner.scan(basePackages);
    }

    /**
     * BeanDefinitionRegistries are called early in application startup, before
     * BeanFactoryPostProcessors. This means that PropertyResourceConfigurers
     * will not have been loaded and any property substitution of this class'
     * properties will fail. To avoid this, find any PropertyResourceConfigurers
     * defined in the context and run them on this class' bean definition. Then
     * update the values.
     */
    private void processPropertyPlaceHolders() {
        Map<String, PropertyResourceConfigurer> prcs = applicationContext
                .getBeansOfType(PropertyResourceConfigurer.class);

        if (!prcs.isEmpty() && applicationContext instanceof ConfigurableApplicationContext) {
            BeanDefinition mapperScannerBean = ((ConfigurableApplicationContext) applicationContext).getBeanFactory()
                    .getBeanDefinition(beanName);

            // PropertyResourceConfigurer does not expose any methods to explicitly perform
            // property placeholder substitution. Instead, create a BeanFactory that just
            // contains this mapper scanner and post process the factory.
            DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
            factory.registerBeanDefinition(beanName, mapperScannerBean);

            for (PropertyResourceConfigurer prc : prcs.values()) {
                prc.postProcessBeanFactory(factory);
            }

            PropertyValues values = mapperScannerBean.getPropertyValues();

            this.basePackage = updatePropertyValue("basePackage", values);
            this.sqlSessionFactoryBeanName = updatePropertyValue("sqlSessionFactoryBeanName", values);
            this.sqlSessionTemplateBeanName = updatePropertyValue("sqlSessionTemplateBeanName", values);
        }
    }

    private String updatePropertyValue(String propertyName, PropertyValues values) {
        PropertyValue property = values.getPropertyValue(propertyName);

        if (property == null) {
            return null;
        }

        Object value = property.getValue();

        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return value.toString();
        } else if (value instanceof TypedStringValue) {
            return ((TypedStringValue) value).getValue();
        } else {
            return null;
        }
    }
}
