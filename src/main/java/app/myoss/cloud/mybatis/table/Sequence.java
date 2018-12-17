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

package app.myoss.cloud.mybatis.table;

import app.myoss.cloud.mybatis.executor.keygen.SequenceKeyGenerator;
import app.myoss.cloud.mybatis.table.annotation.SequenceKey;

/**
 * 序列生成器接口
 *
 * @author Jerry.Chen
 * @since 2018年4月29日 下午12:59:19
 * @see SequenceKey
 * @see SequenceKeyGenerator
 */
public interface Sequence {
    /**
     * 设置数据库表结构信息，会在生成 {@link SequenceKeyGenerator} 初始化的时候调用
     *
     * @param tableInfo 数据库表结构信息
     */
    default void setTableInfo(TableInfo tableInfo) {
        // do nothing
    }

    /**
     * 获取数据库表结构信息
     *
     * @return 数据库表结构信息
     */
    default TableInfo getTableInfo() {
        // do nothing
        return null;
    }

    /**
     * 设置代理的 "序列生成器" class 对象
     *
     * @param clazz 序列生成器 class 对象
     */
    default void setSequenceDelegateClass(Class clazz) {
        // do nothing
    }

    /**
     * 获取代理的 "序列生成器" class 对象
     *
     * @return 序列生成器 class 对象
     */
    default Class getSequenceDelegateClass() {
        return null;
    }

    /**
     * 设置代理的 "序列生成器" 对象
     *
     * @param delegate 序列生成器
     */
    default void setSequenceDelegate(Object delegate) {
        // do nothing
    }

    /**
     * 获取代理的 "序列生成器" 对象
     *
     * @param <T> 范型
     * @return 序列生成器
     */
    default <T> T getSequenceDelegate() {
        return null;
    }

    /**
     * 生成下一个序列值
     *
     * @param parameter 待保存的实体对象
     * @return 下一个序列值
     */
    Object nextValue(Object parameter);
}
