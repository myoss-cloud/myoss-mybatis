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

package app.myoss.cloud.sequence.constants;

import app.myoss.cloud.core.constants.MyossConstants;

/**
 * 序列生成器常量
 *
 * @author Jerry.Chen
 * @since 2018年12月17日 下午2:30:43
 */
public class SequenceConstants {
    /**
     * 序列生成器配置前缀
     */
    public static final String CONFIG_PREFIX     = MyossConstants.CONFIG_PREFIX + ".sequence";
    /**
     * 关系型数据库序列生成器配置前缀
     */
    public static final String RDS_CONFIG_PREFIX = CONFIG_PREFIX + ".rds";
}
