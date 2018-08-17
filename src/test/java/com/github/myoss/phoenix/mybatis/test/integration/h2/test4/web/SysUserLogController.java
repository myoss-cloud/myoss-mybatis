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

package com.github.myoss.phoenix.mybatis.test.integration.h2.test4.web;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.myoss.phoenix.core.lang.dto.Page;
import com.github.myoss.phoenix.core.lang.dto.Result;
import com.github.myoss.phoenix.core.log.method.aspectj.annotation.LogMethodAround;
import com.github.myoss.phoenix.mybatis.test.integration.h2.test4.entity.SysUserLog;
import com.github.myoss.phoenix.mybatis.test.integration.h2.test4.service.SysUserLogService;

/**
 * This web rest api access the database table t_sys_user_log
 * <p>
 * Database Table Remarks: 系统用户日志记录表
 * </p>
 *
 * @author jerry
 * @since 2018年5月14日 下午10:33:55
 */
@RequestMapping("/sysUserLog")
@RestController
public class SysUserLogController {
    @Autowired
    private SysUserLogService sysUserLogService;

    /**
     * 创建新的记录
     *
     * @param record 待保存的实体对象
     * @param <I> 主键类型
     * @return 主键id
     */
    @LogMethodAround
    // @PostMapping("/create")
    public <I> Result<I> create(@RequestBody SysUserLog record) {
        return sysUserLogService.create(record);
    }

    /**
     * 根据主键id更新记录（只会更新有值的字段）
     *
     * @param record 待更新的实体对象
     * @return 是否操作成功
     */
    @LogMethodAround
    // @PostMapping("/updateByPrimaryKey")
    public Result<Boolean> updateByPrimaryKey(@RequestBody SysUserLog record) {
        return sysUserLogService.updateByPrimaryKey(record);
    }

    /**
     * 根据主键id删除记录，如果是审计字段的实体，使用逻辑删除，而不是物理删除
     *
     * @param condition 匹配的条件
     * @return 是否操作成功
     */
    @LogMethodAround
    // @PostMapping("/deleteByPrimaryKey")
    public Result<Boolean> deleteByPrimaryKey(@RequestBody SysUserLog condition) {
        return sysUserLogService.deleteByPrimaryKey(condition);
    }

    /**
     * 根据主键id查询实体对象
     *
     * @param id 主键id
     * @return 对应的实体对象
     */
    // @RequestMapping("/findByPrimaryKey")
    public Result<SysUserLog> findByPrimaryKey(@RequestParam("id") Serializable id) {
        return sysUserLogService.findByPrimaryKey(id);
    }

    /**
     * 根据主键id查询实体对象
     *
     * @param condition 主键id
     * @return 对应的实体对象
     */
    // @PostMapping("/findByPrimaryKey")
    public Result<SysUserLog> findByPrimaryKey(@RequestBody SysUserLog condition) {
        return sysUserLogService.findByPrimaryKey(condition);
    }

    /**
     * 根据条件查询匹配的实体对象
     *
     * @param condition 匹配的条件
     * @return 匹配的实体对象
     */
    // @PostMapping("/findList")
    public Result<List<SysUserLog>> findList(@RequestBody SysUserLog condition) {
        return sysUserLogService.findList(condition);
    }

    /**
     * 根据条件查询匹配的实体对象，并支持字段排序
     *
     * @param condition 匹配的条件和排序字段
     * @return 匹配的实体对象
     */
    // @PostMapping("/findListWithSort")
    public Result<List<SysUserLog>> findListWithSort(@RequestBody Page<SysUserLog> condition) {
        return sysUserLogService.findListWithSort(condition);
    }

    /**
     * 根据条件查询匹配的实体对象，并进行分页
     *
     * @param condition 匹配的条件和排序字段
     * @return 匹配的实体对象
     */
    // @PostMapping("/findPage")
    public Page<SysUserLog> findPage(@RequestBody Page<SysUserLog> condition) {
        return sysUserLogService.findPage(condition);
    }
}
