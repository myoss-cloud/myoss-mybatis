/*
 * Copyright 2018-2020 https://github.com/myoss
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

import java.util.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Mybatis page helper 属性配置
 *
 * @author Jerry.Chen
 * @since 2020年9月6日 下午12:35:42
 */
@ConfigurationProperties(prefix = MybatisPageHelperProperties.MYBATIS_PAGE_HELPER_PREFIX)
public class MybatisPageHelperProperties extends Properties {
    private static final long  serialVersionUID           = -3941227379560902428L;

    /**
     * Mybatis page helper 属性配置的前缀名
     */
    public static final String MYBATIS_PAGE_HELPER_PREFIX = "mybatis.page-helper";
}
