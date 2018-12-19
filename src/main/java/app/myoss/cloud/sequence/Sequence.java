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

package app.myoss.cloud.sequence;

import app.myoss.cloud.sequence.exception.SequenceException;

/**
 * 分布式序列生成器接口
 *
 * @author Jerry.Chen
 * @since 2018年7月27日 下午3:31:15
 */
public interface Sequence extends SequenceLifecycle {
    /**
     * 生成下一个序列值
     *
     * @param params 可选参数（由具体的实现类决定是否需要）
     * @return 返回序列下一个值
     * @throws SequenceException 序列异常信息
     */
    long nextValue(Object... params) throws SequenceException;

    /**
     * 返回 size 大小后的值，比如针对 batch 拿到 size 大小的值，自己内存中顺序分配
     *
     * <pre>
     * 举例：
     * 1. 一次性申请 size = 500 个序列
     * 2. 返回 2500
     * 3. 那么序列值可以使用的范围为：2001 ~ 2500
     * </pre>
     *
     * @param size 一次性获取多少个序列
     * @return 返回序列下一个值。 则可以使用的序列值为： (returnValue - size - 1) ~ returnValue
     * @throws SequenceException 序列异常信息
     */
    long nextValue(int size) throws SequenceException;
}
