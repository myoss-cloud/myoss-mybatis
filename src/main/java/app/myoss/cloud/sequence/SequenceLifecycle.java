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

/**
 * 序列生成器生气周期接口
 *
 * @author Jerry.Chen
 * @since 2018年12月16日 下午3:15:32
 */
public interface SequenceLifecycle {
    /**
     * 初始化
     */
    default void init() {
        // do nothing
    }

    /**
     * 销毁
     */
    default void destroy() {
        // do nothing
    }
}
