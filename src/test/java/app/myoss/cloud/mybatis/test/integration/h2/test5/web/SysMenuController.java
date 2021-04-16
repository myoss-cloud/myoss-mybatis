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

package app.myoss.cloud.mybatis.test.integration.h2.test5.web;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.myoss.cloud.apm.log.method.aspectj.annotation.LogMethodAround;
import app.myoss.cloud.core.lang.dto.Page;
import app.myoss.cloud.core.lang.dto.Result;
import app.myoss.cloud.mybatis.test.integration.h2.test5.entity.SysMenu;
import app.myoss.cloud.mybatis.test.integration.h2.test5.service.SysMenuService;

/**
 * This web rest api access the database table t_sys_menu
 * <p>
 * Database Table Remarks: 系统菜单表
 * </p>
 *
 * @author jerry
 * @since 2021年4月15日 下午10:33:55
 */
@RequestMapping("/sysMenu")
@RestController
public class SysMenuController {
    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 创建新的记录
     *
     * @param record 待保存的实体对象
     * @param <I> 主键类型
     * @return 主键id
     */
    @LogMethodAround
    // @PostMapping("/create")
    public <I> Result<I> create(@RequestBody SysMenu record) {
        return sysMenuService.create(record);
    }

    /**
     * 根据主键id更新记录（只会更新有值的字段）
     *
     * @param record 待更新的实体对象
     * @return 是否操作成功
     */
    @LogMethodAround
    // @PostMapping("/updateByPrimaryKey")
    public Result<Boolean> updateByPrimaryKey(@RequestBody SysMenu record) {
        return sysMenuService.updateByPrimaryKey(record);
    }

    /**
     * 根据主键id删除记录，如果是审计字段的实体，使用逻辑删除，而不是物理删除
     *
     * @param condition 匹配的条件
     * @return 是否操作成功
     */
    @LogMethodAround
    // @PostMapping("/deleteByPrimaryKey")
    public Result<Boolean> deleteByPrimaryKey(@RequestBody SysMenu condition) {
        return sysMenuService.deleteByPrimaryKey(condition);
    }

    /**
     * 根据主键id查询实体对象
     *
     * @param id 主键id
     * @return 对应的实体对象
     */
    // @RequestMapping("/findByPrimaryKey")
    public Result<SysMenu> findByPrimaryKey(@RequestParam("id") Serializable id) {
        return sysMenuService.findByPrimaryKey(id);
    }

    /**
     * 根据主键id查询实体对象
     *
     * @param condition 主键id
     * @return 对应的实体对象
     */
    // @PostMapping("/findByPrimaryKey")
    public Result<SysMenu> findByPrimaryKey(@RequestBody SysMenu condition) {
        return sysMenuService.findByPrimaryKey(condition);
    }

    /**
     * 根据条件查询匹配的实体对象
     *
     * @param condition 匹配的条件
     * @return 匹配的实体对象
     */
    // @PostMapping("/findList")
    public Result<List<SysMenu>> findList(@RequestBody SysMenu condition) {
        return sysMenuService.findList(condition);
    }

    /**
     * 根据条件查询匹配的实体对象，并支持字段排序
     *
     * @param condition 匹配的条件和排序字段
     * @return 匹配的实体对象
     */
    // @PostMapping("/findListWithSort")
    public Result<List<SysMenu>> findListWithSort(@RequestBody Page<SysMenu> condition) {
        return sysMenuService.findListWithSort(condition);
    }

    /**
     * 根据条件查询匹配的实体对象，并进行分页
     *
     * @param condition 匹配的条件和排序字段
     * @return 匹配的实体对象
     */
    // @PostMapping("/findPage")
    public Page<SysMenu> findPage(@RequestBody Page<SysMenu> condition) {
        return sysMenuService.findPage(condition);
    }
}
