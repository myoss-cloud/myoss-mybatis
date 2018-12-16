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
 * Sequence Data repository
 *
 * @author Jerry.Chen
 * @since 2018年12月16日 上午11:58:47
 */
public interface SequenceRepository extends SequenceLifecycle {
    /**
     * 检查并调整某个 sequence name 的值
     *
     * @param name 序列名称
     * @throws SequenceException 序列异常信息
     */
    void adjust(String name) throws SequenceException;

    /**
     * 取得下一个可用的序列区间
     *
     * @param name 序列名称
     * @return 返回下一个可用的序列区间
     * @throws SequenceException 序列异常信息
     */
    SequenceRange nextRange(String name) throws SequenceException;

    /**
     * 内步长
     *
     * @return 返回内步长
     */
    int getInnerStep();
}
