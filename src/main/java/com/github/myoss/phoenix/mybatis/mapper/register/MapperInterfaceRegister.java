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

package com.github.myoss.phoenix.mybatis.mapper.register;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;

import com.github.myoss.phoenix.mybatis.mapper.annotation.RegisterMapper;
import com.github.myoss.phoenix.mybatis.mapper.template.AbstractMapperTemplate;
import com.github.myoss.phoenix.mybatis.table.TableConfig;
import com.github.myoss.phoenix.mybatis.table.TableInfo;
import com.github.myoss.phoenix.mybatis.table.TableMetaObject;

import lombok.Getter;
import lombok.Setter;

/**
 * 通用 Mapper 接口注册器，扫描接口是否有 {@link RegisterMapper} 注解，并自动注册接口方法，生成 sql 语句
 *
 * @author Jerry.Chen
 * @since 2018年4月25日 上午10:37:33
 * @see RegisterMapper
 * @see AbstractMapperTemplate
 */
public class MapperInterfaceRegister {
    /**
     * 通用Mapper接口实例对象
     */
    private Map<Class<?>, Future<AbstractMapperTemplate>> mapperTemplateCached;
    /**
     * 实体类对象和它的表结构等信息
     */
    @Getter
    private Map<Class<?>, TableInfo>                      registerEntityClass;
    /**
     * Table全局配置
     */
    @Setter
    @Getter
    private TableConfig                                   tableConfig;
    @Setter
    @Getter
    private Configuration                                 configuration;
    @Getter
    private Set<Class<? extends Annotation>>              sqlProviderAnnotationTypes;

    public MapperInterfaceRegister(TableConfig tableConfig) {
        Objects.requireNonNull(tableConfig, "tableConfig is null");
        this.tableConfig = tableConfig;
        this.sqlProviderAnnotationTypes = new HashSet<>();
        this.sqlProviderAnnotationTypes.add(SelectProvider.class);
        this.sqlProviderAnnotationTypes.add(InsertProvider.class);
        this.sqlProviderAnnotationTypes.add(UpdateProvider.class);
        this.sqlProviderAnnotationTypes.add(DeleteProvider.class);
        this.mapperTemplateCached = new ConcurrentHashMap<>();
        this.registerEntityClass = new ConcurrentHashMap<>();
    }

    public void executeRegister(Class<?> mapperInterface) {
        Class<?> entityClass = TableMetaObject.getEntityClassByMapperInterface(mapperInterface);
        if (entityClass == null || registerEntityClass.containsKey(entityClass)) {
            // entityClass = null, 它没有实体类泛型，则不去扫描方法
            return;
        }
        TableInfo tableInfo = TableMetaObject
                .getTableInfoByMapperInterface(mapperInterface, tableConfig, configuration);
        registerEntityClass.put(entityClass, tableInfo);
        scanRegisterMapper(tableInfo, mapperInterface);
    }

    /**
     * 扫描接口是否有 @RegisterMapper 注解，并自动注册
     *
     * @param mapperInterface mapper interface class
     */
    private void scanRegisterMapper(TableInfo tableInfo, Class<?> mapperInterface) {
        Class<?>[] interfaces = mapperInterface.getInterfaces();
        for (Class<?> item : interfaces) {
            // 自动注册标记了 @RegisterMapper 的接口
            if (item.isAnnotationPresent(RegisterMapper.class)) {
                processRegisterMapper(tableInfo, item);
            }
            // 扫描父接口
            scanRegisterMapper(tableInfo, item);
        }
    }

    /**
     * 注册通用Mapper接口
     *
     * @param tableInfo 数据库表结构信息
     * @param mapperClass mapper class
     */
    public void processRegisterMapper(TableInfo tableInfo, Class<?> mapperClass) {
        Method[] methods = mapperClass.getDeclaredMethods();
        if (methods.length == 0) {
            return;
        }
        Map<Class<?>, HashSet<String>> methodSet = new LinkedHashMap<>();
        for (Method method : methods) {
            String name = method.getName();
            int count = 0;
            for (Class<? extends Annotation> sqlProvider : sqlProviderAnnotationTypes) {
                Annotation provider = method.getAnnotation(sqlProvider);
                if (provider == null) {
                    continue;
                }
                Class<?> providerType;
                try {
                    providerType = (Class<?>) provider.getClass().getMethod("type").invoke(provider);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new BuilderException("Error creating SqlProvider.  Cause: ", e);
                }
                if (!AbstractMapperTemplate.class.isAssignableFrom(providerType)) {
                    continue;
                }
                if (count > 1) {
                    throw new BindingException("You cannot supply both more than one SqlProvider to method named "
                            + name);
                }
                methodSet.computeIfAbsent(providerType, k -> new HashSet<>()).add(name);
                count++;
            }
        }

        for (Entry<Class<?>, HashSet<String>> entry : methodSet.entrySet()) {
            Class<?> templateClass = entry.getKey();
            AbstractMapperTemplate templateInstance = getMapperTemplate(templateClass);
            HashSet<String> value = entry.getValue();
            String canonicalName = tableInfo.getMapperInterfaceClass().getCanonicalName();
            for (String methodName : value) {
                MappedStatement mappedStatement = configuration.getMappedStatement(canonicalName + "." + methodName);
                try {
                    Method method = templateClass.getMethod(methodName, TableInfo.class, MappedStatement.class);
                    method.invoke(templateInstance, tableInfo, mappedStatement);
                } catch (NoSuchMethodException e) {
                    throw new BindingException("not found method \"" + methodName + "\" in " + templateClass, e);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new BindingException(e);
                }
            }
        }
    }

    /**
     * 查找MapperTemplate实例对象，如果没有就创建。用一个Proxy对象来包装真正的对象，跟常见的lazy
     * load原理类似；使用FutureTask主要是为了保证同步，避免一个Proxy创建多个对象
     *
     * @param mapperTemplate mapper template class. see
     *            {@link AbstractMapperTemplate}
     * @return MapperTemplate实例对象
     */
    public AbstractMapperTemplate getMapperTemplate(Class<?> mapperTemplate) {
        Future<AbstractMapperTemplate> future = mapperTemplateCached.get(mapperTemplate);
        if (future == null) {
            FutureTask<AbstractMapperTemplate> task = new FutureTask<>(() -> {
                try {
                    return (AbstractMapperTemplate) mapperTemplate.getConstructor().newInstance();
                } catch (Exception e) {
                    throw new BindingException("new instance of " + mapperTemplate + " failed", e);
                }
            });
            future = mapperTemplateCached.putIfAbsent(mapperTemplate, task);
            if (future == null) {
                future = task;
                task.run();
            }
        }

        try {
            AbstractMapperTemplate method = future.get();
            if (method == null) {
                mapperTemplateCached.remove(mapperTemplate);
            }
            return method;
        } catch (InterruptedException | ExecutionException e) {
            mapperTemplateCached.remove(mapperTemplate);
            throw new BindingException("getMapperTemplate failed", e);
        }
    }
}
