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

package app.myoss.cloud.mybatis.test.integration.h2.test1;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import app.myoss.cloud.core.constants.MyossConstants;
import app.myoss.cloud.core.lang.dto.Page;
import app.myoss.cloud.core.lang.dto.Result;
import app.myoss.cloud.core.lang.dto.Sort;
import app.myoss.cloud.mybatis.mapper.template.CrudMapper;
import app.myoss.cloud.mybatis.plugin.ParameterHandlerCustomizer;
import app.myoss.cloud.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import app.myoss.cloud.mybatis.spring.mapper.MapperFactoryBean;
import app.myoss.cloud.mybatis.test.integration.h2.H2DataBaseIntTest.IntAutoConfig;
import app.myoss.cloud.mybatis.test.integration.h2.test1.UserControllerIntTests.MyConfig1;
import app.myoss.cloud.mybatis.test.integration.h2.test1.constants.UserStatusEnum;
import app.myoss.cloud.mybatis.test.integration.h2.test1.entity.User;
import app.myoss.cloud.mybatis.test.integration.h2.test1.mapper.UserMapper;
import app.myoss.cloud.mybatis.test.integration.h2.test1.service.UserService;
import app.myoss.cloud.mybatis.test.integration.h2.test1.web.UserController;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link UserController } 集成测试，使用H2内存数据库，测试 Mybatis 功能
 *
 * @author Jerry.Chen
 * @since 2018年5月11日 上午10:45:16
 */
@MapperScan(basePackageClasses = UserControllerIntTests.class, factoryBean = MapperFactoryBean.class, markerInterface = CrudMapper.class)
@ActiveProfiles({ "h2-test1", "UserControllerIntTests" })
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { DataSourceAutoConfiguration.class, IntAutoConfig.class, MybatisAutoConfiguration.class,
        MyConfig1.class })
public class UserControllerIntTests {
    @Autowired
    private UserController userController;
    @Autowired
    private UserService    userService;
    @Autowired
    private UserMapper     userMapper;
    @Autowired
    private JdbcTemplate   jdbcTemplate;

    public Long maxId() {
        Long value = jdbcTemplate.queryForObject("select max(id) from t_sys_user", Long.class);
        return (value != null ? value : 0L);
    }

    /**
     * 增删改查测试案例1
     */
    @Test
    public void crudTest1() {
        Long exceptedId = maxId() + 1;
        User record = new User();
        record.setEmployeeNumber("10000");
        record.setName("Jerry");
        record.setStatus(UserStatusEnum.NORMAL);

        // 创建记录
        Result<Long> createResult = userController.create(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(createResult).isNotNull();
            softly.assertThat(createResult.isSuccess()).isTrue();
            softly.assertThat(createResult.getErrorCode()).isNull();
            softly.assertThat(createResult.getErrorMsg()).isNull();
            softly.assertThat(createResult.getValue()).isNotNull().isEqualTo(exceptedId);
        });

        // 使用各种查询 API 查询数据库中的记录，和保存之后的记录进行比较
        Result<User> idResult = userController.findByPrimaryKey(exceptedId);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult).isNotNull();
            softly.assertThat(idResult.isSuccess()).isTrue();
            softly.assertThat(idResult.getErrorCode()).isNull();
            softly.assertThat(idResult.getErrorMsg()).isNull();
            softly.assertThat(idResult.getValue()).isNotNull().isEqualTo(record);
        });

        Result<User> idResult2 = userController.findByPrimaryKey(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult2).isNotNull();
            softly.assertThat(idResult2.isSuccess()).isTrue();
            softly.assertThat(idResult2.getErrorCode()).isNull();
            softly.assertThat(idResult2.getErrorMsg()).isNull();
            softly.assertThat(idResult2.getValue()).isNotNull().isEqualTo(record);
        });

        Result<List<User>> listResult = userController.findList(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(listResult).isNotNull();
            softly.assertThat(listResult.isSuccess()).isTrue();
            softly.assertThat(listResult.getErrorCode()).isNull();
            softly.assertThat(listResult.getErrorMsg()).isNull();
            softly.assertThat(listResult.getValue()).isNotEmpty().hasSize(1);
            softly.assertThat(listResult.getValue().get(0)).isEqualTo(record);
        });

        Result<User> oneResult = userService.findOne(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(oneResult).isNotNull();
            softly.assertThat(oneResult.isSuccess()).isTrue();
            softly.assertThat(oneResult.getErrorCode()).isNull();
            softly.assertThat(oneResult.getErrorMsg()).isNull();
            softly.assertThat(oneResult.getValue()).isNotNull().isEqualTo(record);
        });

        Page<User> pageCondition = new Page<>();
        pageCondition.setParam(record);
        pageCondition.setSort(new Sort("id", "name"));
        Result<List<User>> sortResult = userController.findListWithSort(pageCondition);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(sortResult).isNotNull();
            softly.assertThat(sortResult.isSuccess()).isTrue();
            softly.assertThat(sortResult.getErrorCode()).isNull();
            softly.assertThat(sortResult.getErrorMsg()).isNull();
            softly.assertThat(sortResult.getValue()).isNotEmpty().hasSize(1);
            softly.assertThat(sortResult.getValue().get(0)).isEqualTo(record);
        });

        Page<User> pageResult = userController.findPage(pageCondition);
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
        User updateUser = new User();
        updateUser.setId(exceptedId);
        updateUser.setAccount("10000");
        updateUser.setName("Leo");
        updateUser.setIsDeleted(MyossConstants.N);
        updateUser.setCreator(MyossConstants.SYSTEM);
        updateUser.setModifier("jerry");
        updateUser.setGmtCreated(record.getGmtCreated());
        updateUser.setGmtModified(new Date());
        Result<Boolean> updateResult = userController.updateByPrimaryKey(updateUser);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(updateResult).isNotNull();
            softly.assertThat(updateResult.isSuccess()).isTrue();
            softly.assertThat(updateResult.getErrorCode()).isNull();
            softly.assertThat(updateResult.getErrorMsg()).isNull();
            softly.assertThat(updateResult.getValue()).isNotNull().isEqualTo(true);
        });
        record.setModifier(updateUser.getModifier());
        record.setGmtModified(updateUser.getGmtModified());

        Result<User> idResult3 = userService.findByPrimaryKey(exceptedId);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult3).isNotNull();
            softly.assertThat(idResult3.isSuccess()).isTrue();
            softly.assertThat(idResult3.getErrorCode()).isNull();
            softly.assertThat(idResult3.getErrorMsg()).isNull();
            softly.assertThat(idResult3.getValue()).isNotNull().isNotEqualTo(record);

            User target = new User();
            BeanUtils.copyProperties(record, target);
            target.setAccount(updateUser.getAccount());
            target.setName(updateUser.getName());
            softly.assertThat(idResult3.getValue()).isNotNull().isEqualTo(target);
        });

        // 删除数据
        User deleteUser = new User();
        deleteUser.setId(exceptedId);
        deleteUser.setAccount("10000");
        Result<Boolean> deleteResult = userController.deleteByPrimaryKey(deleteUser);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(deleteResult).isNotNull();
            softly.assertThat(deleteResult.isSuccess()).isTrue();
            softly.assertThat(deleteResult.getErrorCode()).isNull();
            softly.assertThat(deleteResult.getErrorMsg()).isNull();
            softly.assertThat(deleteResult.getValue()).isNotNull().isEqualTo(true);
        });

        Result<User> idResult4 = userService.findByPrimaryKey(exceptedId);
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
        User record = new User();
        record.setEmployeeNumber("10001");
        record.setName("Jerry");
        record.setStatus(UserStatusEnum.LOCKED);

        // 创建记录
        Result<Long> createResult = userService.create(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(createResult).isNotNull();
            softly.assertThat(createResult.isSuccess()).isTrue();
            softly.assertThat(createResult.getErrorCode()).isNull();
            softly.assertThat(createResult.getErrorMsg()).isNull();
            softly.assertThat(createResult.getValue()).isNotNull();
        });

        // 使用各种查询 API 查询数据库中的记录，和保存之后的记录进行比较
        Result<Integer> countResult = userService.findCount(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(countResult).isNotNull();
            softly.assertThat(countResult.isSuccess()).isTrue();
            softly.assertThat(countResult.getErrorCode()).isNull();
            softly.assertThat(countResult.getErrorMsg()).isNull();
            softly.assertThat(countResult.getValue()).isNotNull().isEqualTo(1);
        });

        Result<User> idResult = userService.findByPrimaryKey(createResult.getValue());
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult).isNotNull();
            softly.assertThat(idResult.isSuccess()).isTrue();
            softly.assertThat(idResult.getErrorCode()).isNull();
            softly.assertThat(idResult.getErrorMsg()).isNull();
            softly.assertThat(idResult.getValue()).isNotNull().isEqualTo(record);
        });

        // 删除数据
        User deleteUser = new User();
        deleteUser.setId(record.getId());
        Result<Boolean> deleteResult = userService.deleteByCondition(deleteUser);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(deleteResult).isNotNull();
            softly.assertThat(deleteResult.isSuccess()).isTrue();
            softly.assertThat(deleteResult.getErrorCode()).isNull();
            softly.assertThat(deleteResult.getErrorMsg()).isNull();
            softly.assertThat(deleteResult.getValue()).isNotNull().isEqualTo(true);
        });

        Result<User> idResult4 = userService.findByPrimaryKey(record.getId());
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult4).isNotNull();
            softly.assertThat(idResult4.isSuccess()).isTrue();
            softly.assertThat(idResult4.getErrorCode()).isNull();
            softly.assertThat(idResult4.getErrorMsg()).isNull();
            softly.assertThat(idResult4.getValue()).isNull();
        });
    }

    /**
     * "自定义通用SQL查询条件"测试案例1
     */
    @Test
    public void createBatchAndWhereExtraConditionTest1() {
        List<User> exceptedList = new ArrayList<>();
        List<User> allList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            User record = new User();
            record.setEmployeeNumber("10000_" + 1);
            record.setName("Jerry_" + i);
            record.setStatus(UserStatusEnum.NORMAL);
            if (i >= 5) {
                exceptedList.add(record);
            }
            allList.add(record);
        }
        // 创建记录
        Result<Boolean> createResult = userService.createBatch(allList);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(createResult).isNotNull();
            softly.assertThat(createResult.isSuccess()).isTrue();
            softly.assertThat(createResult.getErrorCode()).isNull();
            softly.assertThat(createResult.getErrorMsg()).isNull();
            softly.assertThat(createResult.getValue()).isTrue();
        });

        Page<User> pageCondition2 = new Page<>();
        HashMap<String, Object> extraInfo = new HashMap<>();
        extraInfo.put("nameLike", "erry_");
        pageCondition2.setExtraInfo(extraInfo);
        pageCondition2.setPageNum(2);
        pageCondition2.setPageSize(exceptedList.size());
        Page<User> pageResult2 = userController.findPage(pageCondition2);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(pageResult2).isNotNull();
            softly.assertThat(pageResult2.isSuccess()).isTrue();
            softly.assertThat(pageResult2.getErrorCode()).isNull();
            softly.assertThat(pageResult2.getErrorMsg()).isNull();
            softly.assertThat(pageResult2.getPageNum()).isEqualTo(2);
            softly.assertThat(pageResult2.getPageSize()).isEqualTo(exceptedList.size());
            softly.assertThat(pageResult2.getTotalCount()).isEqualTo(exceptedList.size() * 2);
            softly.assertThat(pageResult2.getValue()).isNotEmpty().hasSize(exceptedList.size());
            softly.assertThat(pageResult2.getValue()).isEqualTo(exceptedList);
        });

        List<Long> ids = pageResult2.getValue().stream().map(User::getId).collect(Collectors.toList());
        List<User> users = userMapper.selectListByPrimaryKey(ids);
        Assertions.assertThat(users).isNotEmpty().hasSize(exceptedList.size());

        List<User> userIds = pageResult2.getValue()
                .stream()
                .map(user -> new User().setId(user.getId()))
                .collect(Collectors.toList());
        List<User> users2 = userMapper.selectListWithPrimaryKey(userIds);
        Assertions.assertThat(users2).isNotEmpty().hasSize(exceptedList.size());

        Assertions.assertThat(users).isEqualTo(users2);

        List<User> userList = userMapper.selectLikeName("Jerry");
        Assertions.assertThat(userList.size()).isEqualTo(exceptedList.size() * 2);
        for (User user : userList) {
            Assertions.assertThat(user.getStatus()).isEqualTo(UserStatusEnum.NORMAL);
        }
    }

    @ComponentScan(basePackageClasses = UserControllerIntTests.class)
    @Profile("UserControllerIntTests")
    @Configuration
    public static class MyConfig1 {
        @Bean
        public ParameterHandlerCustomizer persistenceParameterHandler() {
            return new ParameterHandlerCustomizer() {
                @Override
                public void handlerInsert(MappedStatement mappedStatement, BoundSql boundSql, Object parameterObject) {
                    MetaObject metaObject = mappedStatement.getConfiguration().newMetaObject(parameterObject);
                    metaObject.setValue("isDeleted", MyossConstants.N);
                    metaObject.setValue("creator", "system");
                    metaObject.setValue("modifier", "system");
                    metaObject.setValue("gmtCreated", new Date());
                    metaObject.setValue("gmtModified", new Date());
                }
            };
        }
    }
}
