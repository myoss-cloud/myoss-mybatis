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

package com.github.myoss.phoenix.mybatis.test.integration.h2.test2.web;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.myoss.phoenix.core.lang.dto.Page;
import com.github.myoss.phoenix.core.lang.dto.Result;
import com.github.myoss.phoenix.core.log.method.aspectj.annotation.LogMethodAround;
import com.github.myoss.phoenix.mybatis.test.integration.h2.test2.entity.UserHistory;
import com.github.myoss.phoenix.mybatis.test.integration.h2.test2.service.UserHistoryService;

/**
 * This web rest api access the database table t_sys_user_history
 * <p>
 * Database Table Remarks: 系统用户信息历史备份表
 * </p>
 *
 * @author jerry
 * @since 2018年5月14日 下午3:39:43
 */
@RequestMapping("/userHistory")
@RestController
public class UserHistoryController {
    @Autowired
    private UserHistoryService userHistoryService;

    /**
     * 创建新的记录
     *
     * @param record 待保存的实体对象
     * @return 主键id
     */
    @LogMethodAround
    @PostMapping("/create")
    public <I> Result<I> create(@RequestBody UserHistory record) {
        return userHistoryService.create(record);
    }

    /**
     * 根据主键id更新记录（只会更新有值的字段）
     *
     * @param record 待更新的实体对象
     * @return 是否操作成功
     */
    @LogMethodAround
    @PostMapping("/updateByPrimaryKey")
    public Result<Boolean> updateByPrimaryKey(@RequestBody UserHistory record) {
        return userHistoryService.updateByPrimaryKey(record);
    }

    /**
     * 根据主键id删除记录，如果是审计字段的实体，使用逻辑删除，而不是物理删除
     *
     * @param condition 匹配的条件
     * @return 是否操作成功
     */
    @LogMethodAround
    @PostMapping("/deleteByPrimaryKey")
    public Result<Boolean> deleteByPrimaryKey(@RequestBody UserHistory condition) {
        return userHistoryService.deleteByPrimaryKey(condition);
    }

    /**
     * 根据主键id查询实体对象
     *
     * @param id 主键id
     * @return 对应的实体对象
     */
    @RequestMapping("/findByPrimaryKey")
    public Result<UserHistory> findByPrimaryKey(@RequestParam("id") Serializable id) {
        return userHistoryService.findByPrimaryKey(id);
    }

    /**
     * 根据主键id查询实体对象
     *
     * @param condition 主键id
     * @return 对应的实体对象
     */
    @PostMapping("/findByPrimaryKey")
    public Result<UserHistory> findByPrimaryKey(@RequestBody UserHistory condition) {
        return userHistoryService.findByPrimaryKey(condition);
    }

    /**
     * 根据条件查询匹配的实体对象
     *
     * @param condition 匹配的条件
     * @return 匹配的实体对象
     */
    @PostMapping("/findList")
    public Result<List<UserHistory>> findList(@RequestBody UserHistory condition) {
        return userHistoryService.findList(condition);
    }

    /**
     * 根据条件查询匹配的实体对象，并支持字段排序
     *
     * @param condition 匹配的条件和排序字段
     * @return 匹配的实体对象
     */
    @PostMapping("/findListWithSort")
    public Result<List<UserHistory>> findListWithSort(@RequestBody Page<UserHistory> condition) {
        return userHistoryService.findListWithSort(condition);
    }

    /**
     * 根据条件查询匹配的实体对象，并进行分页
     *
     * @param condition 匹配的条件和排序字段
     * @return 匹配的实体对象
     */
    @PostMapping("/findPage")
    public Page<UserHistory> findPage(@RequestBody Page<UserHistory> condition) {
        return userHistoryService.findPage(condition);
    }
}
