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

package com.github.myoss.phoenix.mybatis.test.integration.h2.test2;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.assertj.core.api.SoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.myoss.phoenix.core.constants.PhoenixConstants;
import com.github.myoss.phoenix.core.lang.dto.Page;
import com.github.myoss.phoenix.core.lang.dto.Result;
import com.github.myoss.phoenix.core.lang.dto.Sort;
import com.github.myoss.phoenix.mybatis.mapper.template.CrudMapper;
import com.github.myoss.phoenix.mybatis.plugin.ParameterHandlerCustomizer;
import com.github.myoss.phoenix.mybatis.repository.entity.AuditIdEntity;
import com.github.myoss.phoenix.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import com.github.myoss.phoenix.mybatis.spring.mapper.MapperFactoryBean;
import com.github.myoss.phoenix.mybatis.test.integration.h2.H2DataBaseIntTest.IntAutoConfig;
import com.github.myoss.phoenix.mybatis.test.integration.h2.test2.entity.UserHistory;
import com.github.myoss.phoenix.mybatis.test.integration.h2.test2.service.UserHistoryService;
import com.github.myoss.phoenix.mybatis.test.integration.h2.test2.web.UserHistoryController;

/**
 * {@link UserHistoryController } 集成测试，使用H2内存数据库，测试 Mybatis 功能
 *
 * @author Jerry.Chen 2018年5月11日 上午10:45:16
 */
@MapperScan(basePackageClasses = UserHistoryControllerIntTests.class, factoryBean = MapperFactoryBean.class, markerInterface = CrudMapper.class)
@ActiveProfiles({ "h2-test2", "UserHistoryControllerIntTests" })
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { DataSourceAutoConfiguration.class, IntAutoConfig.class, MybatisAutoConfiguration.class })
public class UserHistoryControllerIntTests {
    @Autowired
    private UserHistoryController userHistoryController;
    @Autowired
    private UserHistoryService    userHistoryService;
    @Rule
    public OutputCapture          output = new OutputCapture();
    @Autowired
    private JdbcTemplate          jdbcTemplate;

    public Long maxId() {
        Long value = jdbcTemplate.queryForObject("select max(id) from t_sys_user_history", Long.class);
        return value == null ? 0L : value;
    }

    @ComponentScan(basePackageClasses = UserHistoryControllerIntTests.class)
    @Profile("UserHistoryControllerIntTests")
    @Configuration
    public static class MyConfig {
        @Bean
        public ParameterHandlerCustomizer persistenceParameterHandler() {
            return new ParameterHandlerCustomizer() {
                @Override
                public void handlerInsert(MappedStatement mappedStatement, BoundSql boundSql, Object parameterObject) {
                    AuditIdEntity metaObject = (AuditIdEntity) parameterObject;
                    metaObject.setIsDeleted(PhoenixConstants.N);
                    metaObject.setCreator("system");
                    metaObject.setModifier("system");
                    metaObject.setGmtCreated(new Date());
                    metaObject.setGmtModified(new Date());
                }

                @Override
                public void handlerUpdate(MappedStatement mappedStatement, BoundSql boundSql, Object parameterObject) {
                    AuditIdEntity metaObject = (AuditIdEntity) parameterObject;
                    metaObject.setModifier("system");
                    metaObject.setGmtModified(new Date());
                }
            };
        }
    }

    /**
     * 增删改查测试案例1
     */
    @Test
    public void crudTest1() {
        Long exceptedId = maxId() + 1;
        UserHistory record = new UserHistory();
        record.setEmployeeNumber("10000");
        record.setName("Jerry");

        // 创建记录
        Result<Long> createResult = userHistoryController.create(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(createResult).isNotNull();
            softly.assertThat(createResult.isSuccess()).isTrue();
            softly.assertThat(createResult.getErrorCode()).isNull();
            softly.assertThat(createResult.getErrorMsg()).isNull();
            softly.assertThat(createResult.getValue()).isNotNull().isEqualTo(exceptedId);
        });

        // 使用各种查询 API 查询数据库中的记录，和保存之后的记录进行比较
        Result<UserHistory> idResult = userHistoryController.findByPrimaryKey(exceptedId);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult).isNotNull();
            softly.assertThat(idResult.isSuccess()).isTrue();
            softly.assertThat(idResult.getErrorCode()).isNull();
            softly.assertThat(idResult.getErrorMsg()).isNull();
            softly.assertThat(idResult.getValue()).isNotNull().isEqualTo(record);
        });

        Result<UserHistory> idResult2 = userHistoryController.findByPrimaryKey(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult2).isNotNull();
            softly.assertThat(idResult2.isSuccess()).isTrue();
            softly.assertThat(idResult2.getErrorCode()).isNull();
            softly.assertThat(idResult2.getErrorMsg()).isNull();
            softly.assertThat(idResult2.getValue()).isNotNull().isEqualTo(record);
        });

        Result<List<UserHistory>> listResult = userHistoryController.findList(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(listResult).isNotNull();
            softly.assertThat(listResult.isSuccess()).isTrue();
            softly.assertThat(listResult.getErrorCode()).isNull();
            softly.assertThat(listResult.getErrorMsg()).isNull();
            softly.assertThat(listResult.getValue()).isNotEmpty().hasSize(1);
            softly.assertThat(listResult.getValue().get(0)).isEqualTo(record);
        });

        Result<UserHistory> oneResult = userHistoryService.findOne(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(oneResult).isNotNull();
            softly.assertThat(oneResult.isSuccess()).isTrue();
            softly.assertThat(oneResult.getErrorCode()).isNull();
            softly.assertThat(oneResult.getErrorMsg()).isNull();
            softly.assertThat(oneResult.getValue()).isNotNull().isEqualTo(record);
        });

        Page<UserHistory> pageCondition = new Page<>();
        pageCondition.setParam(record);
        pageCondition.setSort(new Sort("id", "name"));
        Result<List<UserHistory>> sortResult = userHistoryController.findListWithSort(pageCondition);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(sortResult).isNotNull();
            softly.assertThat(sortResult.isSuccess()).isTrue();
            softly.assertThat(sortResult.getErrorCode()).isNull();
            softly.assertThat(sortResult.getErrorMsg()).isNull();
            softly.assertThat(sortResult.getValue()).isNotEmpty().hasSize(1);
            softly.assertThat(sortResult.getValue().get(0)).isEqualTo(record);
        });

        Page<UserHistory> pageResult = userHistoryController.findPage(pageCondition);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(pageResult).isNotNull();
            softly.assertThat(pageResult.isSuccess()).isTrue();
            softly.assertThat(pageResult.getErrorCode()).isNull();
            softly.assertThat(pageResult.getErrorMsg()).isNull();
            softly.assertThat(pageResult.getPageNum()).isEqualTo(1);
            softly.assertThat(pageResult.getPageSize()).isEqualTo(Page.DEFAULT_PAGE_SIZE);
            softly.assertThat(pageResult.getTotalCount()).isEqualTo(1);
            softly.assertThat(pageResult.getValue()).isNotEmpty().hasSize(1);
            softly.assertThat(pageResult.getValue().get(0)).isEqualTo(record);
        });

        // 更新数据
        UserHistory updateUser = new UserHistory();
        updateUser.setId(exceptedId);
        updateUser.setAccount("10000");
        updateUser.setName("LEO");
        Result<Boolean> updateResult = userHistoryController.updateByPrimaryKey(updateUser);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(updateResult).isNotNull();
            softly.assertThat(updateResult.isSuccess()).isTrue();
            softly.assertThat(updateResult.getErrorCode()).isNull();
            softly.assertThat(updateResult.getErrorMsg()).isNull();
            softly.assertThat(updateResult.getValue()).isNotNull().isEqualTo(true);
            softly.assertThat(updateUser.getModifier()).isNotNull();
            softly.assertThat(updateUser.getGmtModified()).isNotNull();
        });
        record.setModifier(updateUser.getModifier());
        record.setGmtModified(updateUser.getGmtModified());

        Result<UserHistory> idResult3 = userHistoryService.findByPrimaryKey(exceptedId);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult3).isNotNull();
            softly.assertThat(idResult3.isSuccess()).isTrue();
            softly.assertThat(idResult3.getErrorCode()).isNull();
            softly.assertThat(idResult3.getErrorMsg()).isNull();
            softly.assertThat(idResult3.getValue()).isNotNull().isNotEqualTo(record);

            UserHistory target = new UserHistory();
            BeanUtils.copyProperties(record, target);
            target.setAccount(updateUser.getAccount());
            target.setName(updateUser.getName());
            softly.assertThat(idResult3.getValue()).isNotNull().isEqualTo(target);
        });

        // 删除数据
        UserHistory deleteUser = new UserHistory();
        deleteUser.setId(exceptedId);
        deleteUser.setAccount("10000");
        Result<Boolean> deleteResult = userHistoryController.deleteByPrimaryKey(deleteUser);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(deleteResult).isNotNull();
            softly.assertThat(deleteResult.isSuccess()).isTrue();
            softly.assertThat(deleteResult.getErrorCode()).isNull();
            softly.assertThat(deleteResult.getErrorMsg()).isNull();
            softly.assertThat(deleteResult.getValue()).isNotNull().isEqualTo(true);
        });

        Result<UserHistory> idResult4 = userHistoryService.findByPrimaryKey(exceptedId);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult4).isNotNull();
            softly.assertThat(idResult4.isSuccess()).isTrue();
            softly.assertThat(idResult4.getErrorCode()).isNull();
            softly.assertThat(idResult4.getErrorMsg()).isNull();
            softly.assertThat(idResult4.getValue()).isNull();
        });
    }

    /**
     * 增删改查测试案例2
     */
    @Test
    public void crudTest2() {
        Long exceptedId = maxId() + 1;
        UserHistory record = new UserHistory();
        record.setEmployeeNumber("10001");
        record.setName("Jerry");

        // 创建记录
        Result<Long> createResult = userHistoryService.create(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(createResult).isNotNull();
            softly.assertThat(createResult.isSuccess()).isTrue();
            softly.assertThat(createResult.getErrorCode()).isNull();
            softly.assertThat(createResult.getErrorMsg()).isNull();
            softly.assertThat(createResult.getValue()).isNotNull().isEqualTo(exceptedId);
        });

        // 使用各种查询 API 查询数据库中的记录，和保存之后的记录进行比较
        Result<Integer> countResult = userHistoryService.findCount(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(countResult).isNotNull();
            softly.assertThat(countResult.isSuccess()).isTrue();
            softly.assertThat(countResult.getErrorCode()).isNull();
            softly.assertThat(countResult.getErrorMsg()).isNull();
            softly.assertThat(countResult.getValue()).isNotNull().isEqualTo(1);
        });

        // 删除数据
        UserHistory deleteUser = new UserHistory();
        deleteUser.setId(exceptedId);
        Result<Boolean> deleteResult = userHistoryService.deleteByCondition(deleteUser);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(deleteResult).isNotNull();
            softly.assertThat(deleteResult.isSuccess()).isTrue();
            softly.assertThat(deleteResult.getErrorCode()).isNull();
            softly.assertThat(deleteResult.getErrorMsg()).isNull();
            softly.assertThat(deleteResult.getValue()).isNotNull().isEqualTo(true);
        });

        Result<UserHistory> idResult4 = userHistoryService.findByPrimaryKey(exceptedId);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult4).isNotNull();
            softly.assertThat(idResult4.isSuccess()).isTrue();
            softly.assertThat(idResult4.getErrorCode()).isNull();
            softly.assertThat(idResult4.getErrorMsg()).isNull();
            softly.assertThat(idResult4.getValue()).isNull();
        });

        String printLog = this.output.toString();
        assertThat(printLog).isNotBlank().doesNotContain(" delete ", " DELETE ").contains("INSERT", " UPDATE ");
    }
}
