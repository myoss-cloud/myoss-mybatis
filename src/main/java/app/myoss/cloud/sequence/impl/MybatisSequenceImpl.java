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

package app.myoss.cloud.sequence.impl;

import app.myoss.cloud.mybatis.table.TableInfo;
import app.myoss.cloud.sequence.Sequence;
import lombok.Data;

/**
 * Mybatis序列生成器实现类，用于代理 "分布式序列生成器" {@link Sequence}
 *
 * @author Jerry.Chen
 * @since 2018年12月17日 下午3:10:00
 */
@Data
public class MybatisSequenceImpl implements app.myoss.cloud.mybatis.table.Sequence {
    private TableInfo tableInfo;
    private Sequence  sequence;

    @Override
    public Class getSequenceDelegateClass() {
        return Sequence.class;
    }

    @Override
    public void setSequenceDelegate(Object delegate) {
        this.sequence = (Sequence) delegate;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getSequenceDelegate() {
        return (T) sequence;
    }

    @Override
    public Object nextValue(Object parameter) {
        return sequence.nextValue();
    }
}
