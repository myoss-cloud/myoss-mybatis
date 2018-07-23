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
 *   &lt;bean id=&quot;baseMapper&quot; class=&quot;org.mybatis.spring.mapper.MapperFactoryBean&quot; abstract=&quot;true&quot; lazy-init=&quot;true&quot;&gt;
 *     &lt;property name=&quot;sqlSessionFactory&quot; ref=&quot;sqlSessionFactory&quot; /&gt;
 *   &lt;/bean&gt;
 *
 *   &lt;bean id=&quot;oneMapper&quot; parent=&quot;baseMapper&quot;&gt;
 *     &lt;property name=&quot;mapperInterface&quot; value=&quot;my.package.MyMapperInterface&quot; /&gt;
 *   &lt;/bean&gt;
 *
 *   &lt;bean id=&quot;anotherMapper&quot; parent=&quot;baseMapper&quot;&gt;
 *     &lt;property name=&quot;mapperInterface&quot; value=&quot;my.package.MyAnotherMapperInterface&quot; /&gt;
 *   &lt;/bean&gt;
 * </pre>
 * <p>
 * Note that this factory can only inject <em>interfaces</em>, not concrete
 * classes.
 *
 * @param <T> 实体类
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

    /**
     * 初始化 MapperFactoryBean
     */
    public MapperFactoryBean() {
        super();
    }

    /**
     * 初始化 MapperFactoryBean
     *
     * @param mapperInterface mapper interface class
     */
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
