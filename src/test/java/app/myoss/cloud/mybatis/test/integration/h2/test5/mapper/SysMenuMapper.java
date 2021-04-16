/*
 * Copyright 2018-2021 https://github.com/myoss
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

package app.myoss.cloud.mybatis.test.integration.h2.test5.mapper;

import org.springframework.stereotype.Repository;

import app.myoss.cloud.mybatis.mapper.template.CrudMapper;
import app.myoss.cloud.mybatis.mapper.template.select.SelectListIncludeLogicDeleteMapper;
import app.myoss.cloud.mybatis.mapper.template.select.SelectPageIncludeLogicDeleteMapper;
import app.myoss.cloud.mybatis.test.integration.h2.test5.entity.SysMenu;

/**
 * This mapper interface access the database table t_sys_menu
 * <p>
 * Database Table Remarks: 系统菜单表
 * </p>
 *
 * @author jerry
 * @since 2021年4月15日 下午10:33:55
 */
@Repository
public interface SysMenuMapper extends CrudMapper<SysMenu>, SelectListIncludeLogicDeleteMapper<SysMenu>,
        SelectPageIncludeLogicDeleteMapper<SysMenu> {

}
