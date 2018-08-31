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

package app.myoss.cloud.mybatis.test.integration.h2.test4.mapper;

import app.myoss.cloud.mybatis.mapper.template.CrudMapper;
import app.myoss.cloud.mybatis.mapper.template.select.SelectListIncludeLogicDeleteMapper;
import app.myoss.cloud.mybatis.mapper.template.select.SelectPageIncludeLogicDeleteMapper;
import app.myoss.cloud.mybatis.test.integration.h2.test4.entity.SysUserLog;

/**
 * This mapper interface access the database table t_sys_user_log
 * <p>
 * Database Table Remarks: 系统用户日志记录表
 * </p>
 *
 * @author jerry
 * @since 2018年5月14日 下午10:33:55
 */
public interface SysUserLogMapper extends CrudMapper<SysUserLog>, SelectListIncludeLogicDeleteMapper<SysUserLog>,
        SelectPageIncludeLogicDeleteMapper<SysUserLog> {

}
