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

import java.util.concurrent.atomic.AtomicLong;

import lombok.Getter;

/**
 * 序列区间
 *
 * @author Jerry.Chen
 * @since 2018年12月16日 上午11:55:13
 */
public class SequenceRange {
    @Getter
    private final long       min;
    @Getter
    private final long       max;

    private final AtomicLong value;
    @Getter
    private volatile boolean over = false;

    /**
     * 创建序列区间
     *
     * @param min 序列最小值
     * @param max 序列最大值
     */
    public SequenceRange(long min, long max) {
        this.min = min;
        this.max = max;
        this.value = new AtomicLong(min);
    }

    /**
     * 一次性获取多少个序列
     *
     * @param size 获取多少个序列
     * @return 返回序列下一个值。 则可以使用的序列值为： (returnValue - size - 1) ~ returnValue
     */
    public long getBatch(int size) {
        long currentValue = value.getAndAdd(size) + size - 1;
        if (currentValue > max) {
            over = true;
            return -1;
        }
        return currentValue;
    }

    /**
     * 获取下一个序列值
     *
     * @return 下一个序列值
     */
    public long getAndIncrement() {
        long currentValue = value.getAndIncrement();
        if (currentValue > max) {
            over = true;
            return -1;
        }
        return currentValue;
    }

    @Override
    public String toString() {
        return "max: " + max + ", min: " + min + ", value: " + value;
    }
}
