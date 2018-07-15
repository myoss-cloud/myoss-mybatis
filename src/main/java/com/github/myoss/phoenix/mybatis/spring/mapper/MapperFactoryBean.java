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

import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.SqlSessionTemplate;

import com.github.myoss.phoenix.mybatis.mapper.register.MapperInterfaceRegister;
import com.github.myoss.phoenix.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;

import lombok.Getter;
import lombok.Setter;

/**
 * BeanFactory that enables injection of MyBatis mapper interfaces. It can be
 * set up with a SqlSessionFactory or a pre-configured SqlSessionTemplate.
 * <p>
 * Sample configuration:
 *
 * <pre class="code">
 * {@code
 *   <bean id="baseMapper" class="org.mybatis.spring.mapper.MapperFactoryBean" abstract="true" lazy-init="true">
 *     <property name="sqlSessionFactory" ref="sqlSessionFactory" />
 *   </bean>
 *
 *   <bean id="oneMapper" parent="baseMapper">
 *     <property name="mapperInterface" value="my.package.MyMapperInterface" />
 *   </bean>
 *
 *   <bean id="anotherMapper" parent="baseMapper">
 *     <property name="mapperInterface" value="my.package.MyAnotherMapperInterface" />
 *   </bean>
 * }
 * </pre>
 * <p>
 * Note that this factory can only inject <em>interfaces</em>, not concrete
 * classes.
 *
 * @see SqlSessionTemplate
 * @author Jerry.Chen
 * @since 2018年4月24日 下午6:18:05
 */
@Setter
@Getter
public class MapperFactoryBean<T> extends org.mybatis.spring.mapper.MapperFactoryBean<T> {
    /**
     * 通用Mapper接口注册器
     * <p>
     * 默认实例对象: {@link MybatisAutoConfiguration#mapperInterfaceRegister()}
     * <p>
     * 可以被它覆盖：
     * {@link ClassPathMapperScanner#setMapperInterfaceRegister(MapperInterfaceRegister)}
     * 和
     * {@link ClassPathMapperScanner#setMapperInterfaceRegisterBeanName(String)}
     */
    private MapperInterfaceRegister mapperInterfaceRegister;

    public MapperFactoryBean() {
        super();
    }

    public MapperFactoryBean(Class<T> mapperInterface) {
        super(mapperInterface);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkDaoConfig() {
        super.checkDaoConfig();

        notNull(this.mapperInterfaceRegister, "Property 'mapperInterfaceRegister' is required");
        if (mapperInterfaceRegister.getConfiguration() == null) {
            Configuration configuration = getSqlSession().getConfiguration();
            mapperInterfaceRegister.setConfiguration(configuration);
        }
        mapperInterfaceRegister.executeRegister(getMapperInterface());
    }
}
