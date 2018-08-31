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

package app.myoss.cloud.mybatis.table.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import app.myoss.cloud.mybatis.table.Sequence;

/**
 * 序列生成器规则
 *
 * @author Jerry.Chen
 * @since 2018年4月29日 上午11:21:53
 */
@Target({ TYPE, FIELD, METHOD })
@Retention(RUNTIME)
@Documented
public @interface SequenceGenerator {
    /**
     * 序列生成策略
     *
     * @return 序列生成策略
     */
    GenerationType strategy();

    /**
     * (Optional) 用于 {@link GenerationType#SELECT_KEY} 策略
     *
     * @return select sql 策略
     */
    SelectKey selectKey() default @SelectKey(sql = "");

    /**
     * (Optional) 用于 {@link GenerationType#SEQUENCE_KEY} 策略
     *
     * @return sequence 策略
     */
    SequenceKey sequenceKey() default @SequenceKey(sequenceClass = Sequence.class);

    /**
     * 用于 {@link GenerationType#SELECT_KEY} 和
     * {@link GenerationType#SEQUENCE_KEY} 策略
     */
    enum Order {
        /**
         * 在 INSERT 之前执行
         */
        BEFORE,
        /**
         * 在 INSERT 之后执行
         */
        AFTER
    }
}
