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

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.pagehelper.PageInterceptor;

import lombok.extern.slf4j.Slf4j;

/**
 * MyBatis Page Helper Spring Boot项目自动配置
 *
 * @author Jerry.Chen
 * @since 2020年9月6日 上午12:20:15
 */
@Slf4j
@ConditionalOnProperty(prefix = MybatisProperties.MYBATIS_PREFIX, value = "enabled", matchIfMissing = true)
@EnableConfigurationProperties({ MybatisPageHelperProperties.class })
@ConditionalOnClass({ PageInterceptor.class })
@ConditionalOnBean(DataSource.class)
@AutoConfigureBefore(MybatisAutoConfiguration.class)
@Configuration
public class MybatisPageHelperAutoConfiguration {
    /**
     * <a href="https://github.com/pagehelper/Mybatis-PageHelper">mybatis
     * 分页插件</a>
     *
     * @param pageHelperProperties Mybatis page helper 属性配置
     * @return PageInterceptor
     */
    @ConditionalOnMissingBean(PageInterceptor.class)
    @Bean
    public PageInterceptor pageInterceptor(MybatisPageHelperProperties pageHelperProperties) {
        PageInterceptor pageInterceptor = new PageInterceptor();
        pageInterceptor.setProperties(pageHelperProperties);
        return pageInterceptor;
    }
}
