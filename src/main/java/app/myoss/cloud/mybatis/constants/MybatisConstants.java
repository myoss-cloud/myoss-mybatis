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

package app.myoss.cloud.mybatis.constants;

/**
 * Mybatis 项目常量
 *
 * @author Jerry.Chen
 * @since 2018年7月24日 上午1:18:12
 */
public class MybatisConstants {
    /**
     * 字段或者实体对象没有值
     */
    public static final String VALUE_IS_BLANK    = "valueIsBlank";
    /**
     * 匹配到了多条的记录
     */
    public static final String MORE_RECORDS      = "moreRecords";
    /**
     * 数据库插入失败
     */
    public static final String INSERT_DB_FAILED  = "insertDBFailed";
    /**
     * 未匹配到相应的记录
     */
    public static final String NOT_MATCH_RECORDS = "notMatchRecords";
}
