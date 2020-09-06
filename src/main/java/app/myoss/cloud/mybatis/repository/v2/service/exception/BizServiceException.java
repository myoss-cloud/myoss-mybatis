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

package app.myoss.cloud.mybatis.repository.v2.service.exception;

import lombok.Getter;

/**
 * 业务异常
 *
 * @author Jerry.Chen
 * @since 2020年9月6日 下午2:21:33
 */
public class BizServiceException extends RuntimeException {
    private static final long serialVersionUID = -4114410088312593494L;
    @SuppressWarnings("checkstyle:MutableException")
    @Getter
    private final String      errorCode;
    @SuppressWarnings("checkstyle:MutableException")
    @Getter
    private final String      errorMessage;

    /**
     * 业务运行时异常
     *
     * @param errorCode 错误代码
     * @param errorMessage 错误信息
     */
    public BizServiceException(String errorCode, String errorMessage) {
        super("{\"errorCode\": " + errorCode + ", \"errorMessage\":" + errorMessage + "}");
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * 业务运行时异常
     *
     * @param errorCode 错误代码
     * @param errorMessage 错误信息
     * @param cause 异常信息
     */
    public BizServiceException(String errorCode, String errorMessage, Throwable cause) {
        super("{\"errorCode\": " + errorCode + ", \"errorMessage\":" + errorMessage + "}", cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
