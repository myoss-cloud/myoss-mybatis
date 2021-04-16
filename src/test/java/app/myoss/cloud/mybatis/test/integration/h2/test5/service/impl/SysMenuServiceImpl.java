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

package app.myoss.cloud.mybatis.test.integration.h2.test5.service.impl;

import java.util.Map;

import org.apache.ibatis.mapping.SqlCommandType;
import org.springframework.stereotype.Service;

import app.myoss.cloud.core.lang.dto.Result;
import app.myoss.cloud.mybatis.repository.service.impl.BaseCrudServiceImpl;
import app.myoss.cloud.mybatis.test.integration.h2.test5.entity.SysMenu;
import app.myoss.cloud.mybatis.test.integration.h2.test5.mapper.SysMenuMapper;
import app.myoss.cloud.mybatis.test.integration.h2.test5.service.SysMenuService;

/**
 * This service implement access the database table t_sys_menu
 * <p>
 * Database Table Remarks: 系统菜单表
 * </p>
 *
 * @author jerry
 * @since 2021年4月15日 下午10:33:55
 */
@Service
public class SysMenuServiceImpl extends BaseCrudServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {
    @Override
    protected boolean checkCommonQueryConditionIsAllNull(SqlCommandType sqlCommandType, Result<?> result,
                                                         SysMenu condition, Map<String, Object> extraCondition) {
        return true;
    }
}
