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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.codehaus.commons.nullanalysis.NotNull;

import app.myoss.cloud.sequence.Sequence;
import app.myoss.cloud.sequence.SequenceLifecycle;
import app.myoss.cloud.sequence.SequenceRange;
import app.myoss.cloud.sequence.SequenceRepository;
import app.myoss.cloud.sequence.exception.SequenceException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * 默认的序列号生成器
 *
 * @author Jerry.Chen
 * @since 2018年7月27日 下午3:33:44
 */
public class DefaultSequenceImpl implements SequenceLifecycle, Sequence {
    private final Lock             lock = new ReentrantLock();
    @Setter
    @Getter
    private volatile SequenceRange sequenceRange;
    @Setter
    @Getter
    @NonNull
    private SequenceRepository     sequenceRepository;
    @Setter
    @Getter
    @NotNull
    private String                 name;

    @Override
    public void init() {
        if (sequenceRepository == null || name == null) {
            throw new NullPointerException("sequenceRepository or name is empty");
        }
        sequenceRepository.init();
        if (sequenceRepository instanceof RdsSequenceRepository) {
            RdsSequenceRepository rdsSequenceRepository = (RdsSequenceRepository) sequenceRepository;
            rdsSequenceRepository.adjust(name);
        }
    }

    @Override
    public long nextValue(Object... params) throws SequenceException {
        if (getSequenceRange() == null) {
            // 当前区间不存在，重新获取一个区间
            lock.lock();
            try {
                if (getSequenceRange() == null) {
                    setSequenceRange(sequenceRepository.nextRange(name));
                }
            } finally {
                lock.unlock();
            }
        }

        long value = getSequenceRange().getAndIncrement();
        if (value == -1) {
            // 当value值为-1时，表明区间的序列号已经分配完，需要重新获取区间
            lock.lock();
            try {
                for (;;) {
                    if (getSequenceRange().isOver()) {
                        setSequenceRange(sequenceRepository.nextRange(name));
                    }
                    value = getSequenceRange().getAndIncrement();
                    if (value == -1) {
                        continue;
                    }
                    break;
                }
            } finally {
                lock.unlock();
            }
        }

        if (value < 0) {
            throw new SequenceException("Sequence value overflow, value = " + value);
        }

        return value;
    }

    @Override
    public long nextValue(int size) throws SequenceException {
        if (size > sequenceRepository.getInnerStep()) {
            throw new SequenceException(
                    "batch size > sequence step step, please change batch size or sequence inner step");
        }
        if (getSequenceRange() == null) {
            // 当前区间不存在，重新获取一个区间
            lock.lock();
            try {
                if (getSequenceRange() == null) {
                    setSequenceRange(sequenceRepository.nextRange(name));
                }
            } finally {
                lock.unlock();
            }
        }

        long value = getSequenceRange().getBatch(size);
        if (value == -1) {
            // 当value值为-1时，表明区间的序列号已经分配完，需要重新获取区间
            lock.lock();
            try {
                for (;;) {
                    if (getSequenceRange().isOver()) {
                        setSequenceRange(sequenceRepository.nextRange(name));
                    }
                    value = getSequenceRange().getBatch(size);
                    if (value == -1) {
                        continue;
                    }
                    break;
                }
            } finally {
                lock.unlock();
            }
        }

        if (value < 0) {
            throw new SequenceException("Sequence value overflow, value = " + value);
        }

        return value;
    }
}
