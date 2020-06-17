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

package app.myoss.cloud.mybatis.test.integration.h2.test4;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.Assert;
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

import app.myoss.cloud.core.constants.MyossConstants;
import app.myoss.cloud.core.lang.dto.Page;
import app.myoss.cloud.core.lang.dto.Result;
import app.myoss.cloud.core.lang.dto.Sort;
import app.myoss.cloud.core.lang.json.JsonApi;
import app.myoss.cloud.mybatis.executor.keygen.SequenceKeyGenerator;
import app.myoss.cloud.mybatis.mapper.template.CrudMapper;
import app.myoss.cloud.mybatis.plugin.ParameterHandlerCustomizer;
import app.myoss.cloud.mybatis.plugin.impl.DefaultParameterHandlerCustomizer;
import app.myoss.cloud.mybatis.repository.utils.DbUtils;
import app.myoss.cloud.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import app.myoss.cloud.mybatis.spring.mapper.MapperFactoryBean;
import app.myoss.cloud.mybatis.table.Sequence;
import app.myoss.cloud.mybatis.table.TableMetaObject;
import app.myoss.cloud.mybatis.test.integration.h2.H2DataBaseIntTest.IntAutoConfig;
import app.myoss.cloud.mybatis.test.integration.h2.test4.SysUserLogControllerIntTests.MyConfig;
import app.myoss.cloud.mybatis.test.integration.h2.test4.entity.SysUserLog;
import app.myoss.cloud.mybatis.test.integration.h2.test4.mapper.SysUserLogMapper;
import app.myoss.cloud.mybatis.test.integration.h2.test4.service.SysUserLogService;
import app.myoss.cloud.mybatis.test.integration.h2.test4.web.SysUserLogController;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link SysUserLogController } 集成测试，使用H2内存数据库，测试 Mybatis 功能
 *
 * @author Jerry.Chen
 * @since 2018年5月11日 下午10:33:55
 */
@MapperScan(basePackageClasses = SysUserLogControllerIntTests.class, factoryBean = MapperFactoryBean.class, markerInterface = CrudMapper.class)
@ActiveProfiles({ "h2-test4", "SysUserLogControllerIntTests" })
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { DataSourceAutoConfiguration.class, IntAutoConfig.class, MybatisAutoConfiguration.class,
        MyConfig.class })
public class SysUserLogControllerIntTests {
    @Autowired
    private SysUserLogController userLogController;
    @Autowired
    private SysUserLogService    userLogService;
    @Autowired
    private SysUserLogMapper     userLogMapper;
    @Rule
    public OutputCapture         output = new OutputCapture();
    @Autowired
    private JdbcTemplate         jdbcTemplate;

    public Long maxId() {
        Long value = jdbcTemplate.queryForObject("select max(id) from t_sys_user_log", Long.class);
        return (value != null ? value : 0L);
    }

    /**
     * 增删改查测试案例1
     */
    @Test
    public void crudTest1() {
        Long exceptedId = maxId() + 1;
        SysUserLog record = new SysUserLog();
        record.setEmployeeNumber("10000");
        record.setInfo("第一次记录");

        // 创建记录
        Result<Long> createResult = userLogController.create(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(createResult).isNotNull();
            softly.assertThat(createResult.isSuccess()).isTrue();
            softly.assertThat(createResult.getErrorCode()).isNull();
            softly.assertThat(createResult.getErrorMsg()).isNull();
            softly.assertThat(createResult.getValue()).isNotNull().isEqualTo(exceptedId);
        });

        // 使用各种查询 API 查询数据库中的记录，和保存之后的记录进行比较
        Result<SysUserLog> idResult = userLogController.findByPrimaryKey(exceptedId);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult).isNotNull();
            softly.assertThat(idResult.isSuccess()).isTrue();
            softly.assertThat(idResult.getErrorCode()).isNull();
            softly.assertThat(idResult.getErrorMsg()).isNull();
            softly.assertThat(idResult.getValue()).isNotNull().isEqualTo(record);
        });

        Result<SysUserLog> idResult2 = userLogController.findByPrimaryKey(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult2).isNotNull();
            softly.assertThat(idResult2.isSuccess()).isTrue();
            softly.assertThat(idResult2.getErrorCode()).isNull();
            softly.assertThat(idResult2.getErrorMsg()).isNull();
            softly.assertThat(idResult2.getValue()).isNotNull().isEqualTo(record);
        });

        Result<List<SysUserLog>> listResult = userLogController.findList(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(listResult).isNotNull();
            softly.assertThat(listResult.isSuccess()).isTrue();
            softly.assertThat(listResult.getErrorCode()).isNull();
            softly.assertThat(listResult.getErrorMsg()).isNull();
            softly.assertThat(listResult.getValue()).isNotEmpty().hasSize(1);
            softly.assertThat(listResult.getValue().get(0)).isEqualTo(record);
        });

        Result<SysUserLog> oneResult = userLogService.findOne(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(oneResult).isNotNull();
            softly.assertThat(oneResult.isSuccess()).isTrue();
            softly.assertThat(oneResult.getErrorCode()).isNull();
            softly.assertThat(oneResult.getErrorMsg()).isNull();
            softly.assertThat(oneResult.getValue()).isNotNull().isEqualTo(record);
        });

        Page<SysUserLog> pageCondition = new Page<>();
        pageCondition.setParam(record);
        pageCondition.setSort(new Sort("id", "name"));
        Result<List<SysUserLog>> sortResult = userLogController.findListWithSort(pageCondition);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(sortResult).isNotNull();
            softly.assertThat(sortResult.isSuccess()).isTrue();
            softly.assertThat(sortResult.getErrorCode()).isNull();
            softly.assertThat(sortResult.getErrorMsg()).isNull();
            softly.assertThat(sortResult.getValue()).isNotEmpty().hasSize(1);
            softly.assertThat(sortResult.getValue().get(0)).isEqualTo(record);
        });

        Page<SysUserLog> pageResult = userLogController.findPage(pageCondition);
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
        SysUserLog updateUser = new SysUserLog();
        updateUser.setId(exceptedId);
        updateUser.setInfo("第二次记录");
        Result<Boolean> updateResult = userLogController.updateByPrimaryKey(updateUser);
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

        Result<SysUserLog> idResult3 = userLogService.findByPrimaryKey(exceptedId);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult3).isNotNull();
            softly.assertThat(idResult3.isSuccess()).isTrue();
            softly.assertThat(idResult3.getErrorCode()).isNull();
            softly.assertThat(idResult3.getErrorMsg()).isNull();
            softly.assertThat(idResult3.getValue()).isNotNull().isNotEqualTo(record);

            SysUserLog target = new SysUserLog();
            BeanUtils.copyProperties(record, target);
            target.setInfo(updateUser.getInfo());
            softly.assertThat(idResult3.getValue()).isNotNull().isEqualTo(target);
        });

        // 更新数据
        SysUserLog updateUser2 = new SysUserLog();
        updateUser2.setInfo("测试 updateByCondition 更新");
        updateUser2.setEmployeeNumber("123456");
        int count = userLogMapper.updateByCondition(updateUser2, updateUser);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(count).isEqualTo(1);
            softly.assertThat(updateUser2.getModifier()).isNotNull();
            softly.assertThat(updateUser2.getGmtModified()).isNotNull();
        });
        record.setModifier(updateUser2.getModifier());
        record.setGmtModified(updateUser2.getGmtModified());

        Result<SysUserLog> idResult4 = userLogService.findByPrimaryKey(exceptedId);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult4).isNotNull();
            softly.assertThat(idResult4.isSuccess()).isTrue();
            softly.assertThat(idResult4.getErrorCode()).isNull();
            softly.assertThat(idResult4.getErrorMsg()).isNull();
            softly.assertThat(idResult4.getValue()).isNotNull().isNotEqualTo(record);

            SysUserLog target = new SysUserLog();
            BeanUtils.copyProperties(record, target);
            target.setInfo(updateUser2.getInfo());
            target.setEmployeeNumber(updateUser2.getEmployeeNumber());
            softly.assertThat(idResult4.getValue()).isNotNull().isEqualTo(target);
        });

        // 删除数据
        SysUserLog deleteUser = new SysUserLog();
        deleteUser.setId(exceptedId);
        deleteUser.setInfo("10000");
        Result<Boolean> deleteResult = userLogController.deleteByPrimaryKey(deleteUser);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(deleteResult).isNotNull();
            softly.assertThat(deleteResult.isSuccess()).isTrue();
            softly.assertThat(deleteResult.getErrorCode()).isNull();
            softly.assertThat(deleteResult.getErrorMsg()).isNull();
            softly.assertThat(deleteResult.getValue()).isNotNull().isEqualTo(true);
        });

        Result<SysUserLog> idResult5 = userLogService.findByPrimaryKey(exceptedId);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult5).isNotNull();
            softly.assertThat(idResult5.isSuccess()).isTrue();
            softly.assertThat(idResult5.getErrorCode()).isNull();
            softly.assertThat(idResult5.getErrorMsg()).isNull();
            softly.assertThat(idResult5.getValue()).isNull();
        });
    }

    /**
     * 增删改查测试案例2
     */
    @Test
    public void crudTest2() {
        Long exceptedId = maxId() + 1;
        SysUserLog record = new SysUserLog();
        record.setEmployeeNumber("10001");
        record.setInfo("第一次记录日志信息");

        // 创建记录
        Result<Long> createResult = userLogService.create(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(createResult).isNotNull();
            softly.assertThat(createResult.isSuccess()).isTrue();
            softly.assertThat(createResult.getErrorCode()).isNull();
            softly.assertThat(createResult.getErrorMsg()).isNull();
            softly.assertThat(createResult.getValue()).isNotNull().isEqualTo(exceptedId);
        });

        // 使用各种查询 API 查询数据库中的记录，和保存之后的记录进行比较
        Result<Integer> countResult = userLogService.findCount(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(countResult).isNotNull();
            softly.assertThat(countResult.isSuccess()).isTrue();
            softly.assertThat(countResult.getErrorCode()).isNull();
            softly.assertThat(countResult.getErrorMsg()).isNull();
            softly.assertThat(countResult.getValue()).isNotNull().isEqualTo(1);
        });

        // 删除数据
        SysUserLog deleteUser = new SysUserLog();
        deleteUser.setId(exceptedId);
        Result<Boolean> deleteResult = userLogService.deleteByCondition(deleteUser);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(deleteResult).isNotNull();
            softly.assertThat(deleteResult.isSuccess()).isTrue();
            softly.assertThat(deleteResult.getErrorCode()).isNull();
            softly.assertThat(deleteResult.getErrorMsg()).isNull();
            softly.assertThat(deleteResult.getValue()).isNotNull().isEqualTo(true);
        });

        Result<SysUserLog> idResult4 = userLogService.findByPrimaryKey(exceptedId);
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

    /**
     * 增删改查测试案例3
     */
    @Test
    public void crudTest3() {
        SysUserLog record = new SysUserLog();
        record.setEmployeeNumber("10002");
        record.setInfo("第一次记录日志信息");

        // 创建记录
        Result<Long> createResult = userLogService.create(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(createResult).isNotNull();
            softly.assertThat(createResult.isSuccess()).isTrue();
            softly.assertThat(createResult.getErrorCode()).isNull();
            softly.assertThat(createResult.getErrorMsg()).isNull();
            softly.assertThat(createResult.getValue()).isNotNull();
        });
        Long exceptedId = createResult.getValue();

        // 使用各种查询 API 查询数据库中的记录，和保存之后的记录进行比较
        Result<SysUserLog> countResult = userLogService.findByPrimaryKey(exceptedId);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(countResult).isNotNull();
            softly.assertThat(countResult.isSuccess()).isTrue();
            softly.assertThat(countResult.getErrorCode()).isNull();
            softly.assertThat(countResult.getErrorMsg()).isNull();
            softly.assertThat(countResult.getValue()).isNotNull().isEqualTo(record);
        });

        // 删除数据
        boolean checkDBResult = DbUtils.checkDBResult(userLogMapper.deleteByPrimaryKey(exceptedId));
        Assert.assertTrue(checkDBResult);

        Result<SysUserLog> idResult4 = userLogService.findByPrimaryKey(exceptedId);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult4).isNotNull();
            softly.assertThat(idResult4.isSuccess()).isTrue();
            softly.assertThat(idResult4.getErrorCode()).isNull();
            softly.assertThat(idResult4.getErrorMsg()).isNull();
            softly.assertThat(idResult4.getValue()).isNull();
        });

        // 查询逻辑删除的数据
        SysUserLog condition1 = new SysUserLog();
        condition1.setId(exceptedId);
        List<SysUserLog> conditionResult1 = userLogMapper.selectListIncludeLogicDelete(condition1);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(conditionResult1).isNotNull();
            softly.assertThat(conditionResult1.size()).isEqualTo(1);
            softly.assertThat(conditionResult1.get(0)).isNotNull();
            softly.assertThat(conditionResult1.get(0).getIsDeleted()).isEqualTo(MyossConstants.Y);
        });

        String printLog = this.output.toString();
        assertThat(printLog).isNotBlank().doesNotContain(" delete ", " DELETE ").contains("INSERT", " UPDATE ");
    }

    /**
     * 增删改查测试案例4
     */
    @Test
    public void crudTest4() {
        SysUserLog record = new SysUserLog();
        record.setEmployeeNumber("10003");
        record.setInfo("第一次记录日志信息");

        // 创建记录
        Result<Long> createResult = userLogService.create(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(createResult).isNotNull();
            softly.assertThat(createResult.isSuccess()).isTrue();
            softly.assertThat(createResult.getErrorCode()).isNull();
            softly.assertThat(createResult.getErrorMsg()).isNull();
            softly.assertThat(createResult.getValue()).isNotNull();
        });
        Long exceptedId = createResult.getValue();

        // 使用各种查询 API 查询数据库中的记录，和保存之后的记录进行比较
        Result<SysUserLog> countResult = userLogService.findByPrimaryKey(exceptedId);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(countResult).isNotNull();
            softly.assertThat(countResult.isSuccess()).isTrue();
            softly.assertThat(countResult.getErrorCode()).isNull();
            softly.assertThat(countResult.getErrorMsg()).isNull();
            softly.assertThat(countResult.getValue()).isNotNull().isEqualTo(record);
        });

        // 删除数据
        SysUserLog deleteCondition = new SysUserLog();
        deleteCondition.setId(exceptedId);
        deleteCondition.setInfo(record.getInfo());
        boolean checkDBResult = DbUtils.checkDBResult(userLogMapper.deleteByPrimaryKey(deleteCondition));
        Assert.assertTrue(checkDBResult);

        Result<SysUserLog> idResult4 = userLogService.findByPrimaryKey(exceptedId);
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

    /**
     * 增删改查测试案例5
     */
    @Test
    public void crudTest5() {
        SysUserLog record = new SysUserLog();
        record.setEmployeeNumber("10004");
        record.setInfo("第一次记录日志信息");

        // 创建记录
        Result<Long> createResult = userLogService.create(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(createResult).isNotNull();
            softly.assertThat(createResult.isSuccess()).isTrue();
            softly.assertThat(createResult.getErrorCode()).isNull();
            softly.assertThat(createResult.getErrorMsg()).isNull();
            softly.assertThat(createResult.getValue()).isNotNull();
        });
        Long exceptedId = createResult.getValue();

        // 使用各种查询 API 查询数据库中的记录，和保存之后的记录进行比较
        Result<SysUserLog> countResult = userLogService.findByPrimaryKey(exceptedId);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(countResult).isNotNull();
            softly.assertThat(countResult.isSuccess()).isTrue();
            softly.assertThat(countResult.getErrorCode()).isNull();
            softly.assertThat(countResult.getErrorMsg()).isNull();
            softly.assertThat(countResult.getValue()).isNotNull().isEqualTo(record);
        });

        // 更新数据
        Map<String, Object> updateUseMap = new HashMap<>();
        updateUseMap.put("employeeNumber", null);
        updateUseMap.put("info", "我是更新记录日志信息");
        SysUserLog updateCondition = new SysUserLog();
        updateCondition.setId(exceptedId);
        updateCondition.setInfo(record.getInfo());
        boolean checkDBResult = userLogService.updateUseMapByCondition(updateUseMap, updateCondition).getValue();
        Assert.assertTrue(checkDBResult);

        Result<SysUserLog> idResult4 = userLogService.findByPrimaryKey(exceptedId);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult4).isNotNull();
            softly.assertThat(idResult4.isSuccess()).isTrue();
            softly.assertThat(idResult4.getErrorCode()).isNull();
            softly.assertThat(idResult4.getErrorMsg()).isNull();
            softly.assertThat(idResult4.getValue()).isNotNull().isNotEqualTo(record);

            SysUserLog target = new SysUserLog();
            BeanUtils.copyProperties(record, target);
            target.setInfo((String) updateUseMap.get("info"));
            target.setEmployeeNumber((String) updateUseMap.get("employeeNumber"));
            softly.assertThat(idResult4.getValue()).isNotNull().isEqualTo(target);
        });
    }

    /**
     * "自定义通用SQL查询条件"测试案例1
     */
    @Test
    public void createBatchAndWhereExtraConditionTest1() {
        List<SysUserLog> exceptedList = new ArrayList<>();
        List<SysUserLog> allList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            SysUserLog record = new SysUserLog();
            record.setEmployeeNumber("10000_" + 1);
            record.setInfo("Jerry_" + i);
            if (i >= 5) {
                exceptedList.add(record);
            } else {
                // 干扰查询数据，检验查询条件正确
                record.setInfo("GoodBody_" + i);
            }
            allList.add(record);
        }
        // 创建记录
        Result<Boolean> createResult = userLogService.createBatch(allList);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(createResult).isNotNull();
            softly.assertThat(createResult.isSuccess()).isTrue();
            softly.assertThat(createResult.getErrorCode()).isNull();
            softly.assertThat(createResult.getErrorMsg()).isNull();
            softly.assertThat(createResult.getValue()).isTrue();
        });

        Page<SysUserLog> pageCondition2 = new Page<>();
        HashMap<String, Object> extraInfo = new HashMap<>();
        extraInfo.put("infoLike", "erry_");
        pageCondition2.setExtraInfo(extraInfo);
        pageCondition2.setPageNum(1);
        pageCondition2.setPageSize(exceptedList.size());
        Page<SysUserLog> pageResult2 = userLogController.findPage(pageCondition2);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(pageResult2).isNotNull();
            softly.assertThat(pageResult2.isSuccess()).isTrue();
            softly.assertThat(pageResult2.getErrorCode()).isNull();
            softly.assertThat(pageResult2.getErrorMsg()).isNull();
            softly.assertThat(pageResult2.getPageNum()).isEqualTo(1);
            softly.assertThat(pageResult2.getPageSize()).isEqualTo(exceptedList.size());
            softly.assertThat(pageResult2.getTotalCount()).isEqualTo(exceptedList.size());
            softly.assertThat(pageResult2.getValue()).isNotEmpty().hasSize(exceptedList.size());
            softly.assertThat(pageResult2.getValue()).isEqualTo(exceptedList);
        });

        // 使用主键id查询
        List<Long> ids = pageResult2.getValue().stream().map(SysUserLog::getId).collect(Collectors.toList());
        List<SysUserLog> users = userLogMapper.selectListByPrimaryKey(ids);
        Assertions.assertThat(users).isNotEmpty().hasSize(exceptedList.size());

        List<SysUserLog> userIds = pageResult2.getValue().stream().map(user -> {
            SysUserLog sysUserLog = new SysUserLog();
            sysUserLog.setId(user.getId());
            return sysUserLog;
        }).collect(Collectors.toList());
        List<SysUserLog> users2 = userLogMapper.selectListWithPrimaryKey(userIds);
        Assertions.assertThat(users2).isNotEmpty().hasSize(exceptedList.size());

        Assertions.assertThat(users).isEqualTo(users2);

        // 删除记录
        int deleteCount = userLogMapper.deleteByPrimaryKey(ids.get(0));
        Assertions.assertThat(deleteCount).isEqualTo(1);

        // 再次查询，获取逻辑删除记录
        List<SysUserLog> users3 = userLogMapper.selectListByPrimaryKey(ids);
        SysUserLog sysUserLogDeleted = pageResult2.getValue().get(0);
        Assertions.assertThat(users3).isNotEmpty().hasSize(exceptedList.size() - 1).doesNotContain(sysUserLogDeleted);
        List<SysUserLog> users4 = userLogMapper.selectListByPrimaryKeyIncludeLogicDelete(ids);
        Assertions.assertThat(users4).isNotEmpty().hasSize(exceptedList.size());
        Assertions.assertThat(users4.get(0))
                .isEqualToComparingOnlyGivenFields(sysUserLogDeleted, "id", "employeeNumber", "info");
    }

    /**
     * <ul>
     * <li>第一步：会在 {@link SequenceKeyGenerator} 中初始化此 class
     * <li>第二步：会在 {@link MyConfig#initSequence(JdbcTemplate)} 中初始化它的代理
     * {@link SequenceCustomizer#sequence} 对象
     * <li>
     * </ul>
     */
    @Data
    public static class SequenceCustomizer implements Sequence {

        /**
         * 代理对象
         */
        private Sequence sequence;

        public SequenceCustomizer() {
            log.info("init " + this.getClass());
        }

        @Override
        public Object nextValue(Object parameter) {
            return sequence.nextValue(parameter);
        }

    }

    @ComponentScan(basePackageClasses = SysUserLogControllerIntTests.class)
    @Profile("SysUserLogControllerIntTests")
    @Configuration
    public static class MyConfig {
        @Bean
        public ParameterHandlerCustomizer persistenceParameterHandler() {
            return new DefaultParameterHandlerCustomizer();
        }

        @Bean
        public Optional initSequence(JdbcTemplate jdbcTemplate) {
            Sequence sequenceBean = parameter -> {
                Long nextId = jdbcTemplate.queryForObject("select ifnull(max(`id`) ,0) + 1 from t_sys_user_log",
                        Long.class);
                log.info("nextId: {}, parameter: {}", nextId, JsonApi.toJson(parameter));
                return nextId;
            };

            Map<String, Sequence> sequenceBeanMap = TableMetaObject.getSequenceBeanMap();
            for (Entry<String, Sequence> entry : sequenceBeanMap.entrySet()) {
                SequenceCustomizer value = (SequenceCustomizer) entry.getValue();
                value.setSequence(sequenceBean);
            }
            return Optional.empty();
        }
    }
}
