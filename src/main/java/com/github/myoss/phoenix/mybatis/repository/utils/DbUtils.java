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

package com.github.myoss.phoenix.mybatis.repository.utils;

/**
 * 数据库常用操作方法工具类
 *
 * @author Jerry.Chen 2018年5月10日 上午1:19:14
 */
public class DbUtils {
    /**
     * 判断数据库操作是否成功
     *
     * @param result 数据库操作返回影响的行数
     * @return true or false
     */
    public static boolean checkDBResult(Integer result) {
        return null != result && result >= 1;
    }
}
