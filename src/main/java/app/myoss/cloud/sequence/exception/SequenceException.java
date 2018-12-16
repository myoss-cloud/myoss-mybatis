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

package app.myoss.cloud.sequence.exception;

/**
 * 序列异常信息
 *
 * @author Jerry.Chen
 * @since 2018年12月16日 下午12:04:09
 */
public class SequenceException extends RuntimeException {
    private static final long serialVersionUID = -998302090887430605L;

    /**
     * 序列异常
     */
    public SequenceException() {
        super();
    }

    /**
     * 序列异常
     *
     * @param message 错误信息
     */
    public SequenceException(String message) {
        super(message);
    }

    /**
     * 序列异常
     *
     * @param cause 异常信息
     */
    public SequenceException(Throwable cause) {
        super(cause);
    }

    /**
     * 序列异常
     *
     * @param message 错误信息
     * @param cause 异常信息
     */
    public SequenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
