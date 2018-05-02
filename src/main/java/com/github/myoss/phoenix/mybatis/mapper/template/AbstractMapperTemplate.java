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

package com.github.myoss.phoenix.mybatis.mapper.template;

import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;

/**
 * 生成通用 insert/update/delete/select MappedStatement 模版基类
 *
 * @author Jerry.Chen 2018年4月25日 下午6:50:36
 */
public abstract class AbstractMapperTemplate {
    protected XMLLanguageDriver xmlLanguageDriver = new XMLLanguageDriver();

    /**
     * 用来初始化 {@link ProviderSqlSource}
     *
     * @param record 实体对象
     * @return 动态sql语句
     */
    public String dynamicSql(Object record) {
        return "dynamicSql";
    }

}
