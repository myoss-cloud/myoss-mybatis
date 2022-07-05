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

package app.myoss.cloud.mybatis.test.integration.h2.test5;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.OutputCaptureRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import app.myoss.cloud.core.constants.MyossConstants;
import app.myoss.cloud.core.lang.dto.Direction;
import app.myoss.cloud.core.lang.dto.Page;
import app.myoss.cloud.core.lang.dto.Result;
import app.myoss.cloud.core.lang.dto.Sort;
import app.myoss.cloud.core.lang.json.JsonApi;
import app.myoss.cloud.core.lang.json.JsonObject;
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
import app.myoss.cloud.mybatis.test.integration.h2.test5.SysMenuControllerIntTests.MyConfig5;
import app.myoss.cloud.mybatis.test.integration.h2.test5.dto.SysMenuDTO;
import app.myoss.cloud.mybatis.test.integration.h2.test5.entity.SysMenu;
import app.myoss.cloud.mybatis.test.integration.h2.test5.mapper.SysMenuMapper;
import app.myoss.cloud.mybatis.test.integration.h2.test5.service.SysMenuService;
import app.myoss.cloud.mybatis.test.integration.h2.test5.web.SysMenuController;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link SysMenuController } 集成测试，使用H2内存数据库，测试 Mybatis 功能
 * <p>
 * 主要验证这2个自定义查询条件:
 *
 * <pre>
 * &lt;sql id=&quot;Where_Extend&quot;&gt;
 * &lt;/sql&gt;
 *
 * &lt;sql id=&quot;Where_Extend_Condition&quot;&gt;
 * &lt;/sql&gt;
 * </pre>
 *
 * @author Jerry.Chen
 * @since 2021年4月15日 下午10:33:55
 */
@MapperScan(basePackageClasses = SysMenuControllerIntTests.class, factoryBean = MapperFactoryBean.class, markerInterface = CrudMapper.class)
@ActiveProfiles({ "h2-test5", "SysUserLogControllerIntTests" })
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { DataSourceAutoConfiguration.class, IntAutoConfig.class, MybatisAutoConfiguration.class,
        MyConfig5.class })
public class SysMenuControllerIntTests {
    @Autowired
    private SysMenuController sysMenuController;
    @Autowired
    private SysMenuService    sysMenuService;
    @Autowired
    private SysMenuMapper     sysMenuMapper;
    @Rule
    public OutputCaptureRule  output = new OutputCaptureRule();
    @Autowired
    private JdbcTemplate      jdbcTemplate;

    public Long maxId() {
        Long value = jdbcTemplate.queryForObject("select max(id) from t_sys_menu", Long.class);
        return (value != null ? value : 0L);
    }

    /**
     * 增删改查测试案例1
     */
    @Test
    public void crudTest1() {
        Long exceptedId = maxId() + 1;
        SysMenu record = new SysMenu();
        record.setMenuName("系统设置");
        record.setMenuPath("/system/setting");
        record.setMenuIcon("setting.icon");
        JsonObject content = new JsonObject();
        content.put("remark", "这是一个备注信息");
        record.setContent(content);

        // 创建记录
        Result<Long> createResult = sysMenuController.create(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(createResult).isNotNull();
            softly.assertThat(createResult.isSuccess()).isTrue();
            softly.assertThat(createResult.getErrorCode()).isNull();
            softly.assertThat(createResult.getErrorMsg()).isNull();
            softly.assertThat(createResult.getValue()).isNotNull().isEqualTo(exceptedId);
        });

        // 使用各种查询 API 查询数据库中的记录，和保存之后的记录进行比较
        Result<SysMenu> idResult = sysMenuController.findByPrimaryKey(exceptedId);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult).isNotNull();
            softly.assertThat(idResult.isSuccess()).isTrue();
            softly.assertThat(idResult.getErrorCode()).isNull();
            softly.assertThat(idResult.getErrorMsg()).isNull();
            softly.assertThat(idResult.getValue()).isNotNull().isEqualTo(record);
        });

        Result<SysMenu> idResult2 = sysMenuController.findByPrimaryKey(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult2).isNotNull();
            softly.assertThat(idResult2.isSuccess()).isTrue();
            softly.assertThat(idResult2.getErrorCode()).isNull();
            softly.assertThat(idResult2.getErrorMsg()).isNull();
            softly.assertThat(idResult2.getValue()).isNotNull().isEqualTo(record);
        });

        Result<List<SysMenu>> listResult = sysMenuController.findList(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(listResult).isNotNull();
            softly.assertThat(listResult.isSuccess()).isTrue();
            softly.assertThat(listResult.getErrorCode()).isNull();
            softly.assertThat(listResult.getErrorMsg()).isNull();
            softly.assertThat(listResult.getValue()).isNotEmpty().hasSize(1);
            softly.assertThat(listResult.getValue().get(0)).isEqualTo(record);
        });

        Result<SysMenu> oneResult = sysMenuService.findOne(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(oneResult).isNotNull();
            softly.assertThat(oneResult.isSuccess()).isTrue();
            softly.assertThat(oneResult.getErrorCode()).isNull();
            softly.assertThat(oneResult.getErrorMsg()).isNull();
            softly.assertThat(oneResult.getValue()).isNotNull().isEqualTo(record);
        });

        Page<SysMenu> pageCondition = new Page<>();
        pageCondition.setParam(record);
        pageCondition.setSort(new Sort("id", "name"));
        Result<List<SysMenu>> sortResult = sysMenuController.findListWithSort(pageCondition);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(sortResult).isNotNull();
            softly.assertThat(sortResult.isSuccess()).isTrue();
            softly.assertThat(sortResult.getErrorCode()).isNull();
            softly.assertThat(sortResult.getErrorMsg()).isNull();
            softly.assertThat(sortResult.getValue()).isNotEmpty().hasSize(1);
            softly.assertThat(sortResult.getValue().get(0)).isEqualTo(record);
        });

        Page<SysMenu> pageResult = sysMenuController.findPage(pageCondition);
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
        SysMenu updateUser = new SysMenu();
        updateUser.setId(exceptedId);
        updateUser.setMenuPath("/system/setting/v2");
        Result<Boolean> updateResult = sysMenuController.updateByPrimaryKey(updateUser);
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

        Result<SysMenu> idResult3 = sysMenuService.findByPrimaryKey(exceptedId);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult3).isNotNull();
            softly.assertThat(idResult3.isSuccess()).isTrue();
            softly.assertThat(idResult3.getErrorCode()).isNull();
            softly.assertThat(idResult3.getErrorMsg()).isNull();
            softly.assertThat(idResult3.getValue()).isNotNull().isNotEqualTo(record);

            SysMenu target = new SysMenu();
            BeanUtils.copyProperties(record, target);
            target.setMenuPath(updateUser.getMenuPath());
            softly.assertThat(idResult3.getValue()).isNotNull().isEqualTo(target);
        });

        // 更新数据
        SysMenu updateUser2 = new SysMenu();
        updateUser2.setMenuPath("测试 updateByCondition 更新");
        updateUser2.setMenuName("123456");
        int count = sysMenuMapper.updateByCondition(updateUser2, updateUser);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(count).isEqualTo(1);
            softly.assertThat(updateUser2.getModifier()).isNotNull();
            softly.assertThat(updateUser2.getGmtModified()).isNotNull();
        });
        record.setModifier(updateUser2.getModifier());
        record.setGmtModified(updateUser2.getGmtModified());

        Result<SysMenu> idResult4 = sysMenuService.findByPrimaryKey(exceptedId);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult4).isNotNull();
            softly.assertThat(idResult4.isSuccess()).isTrue();
            softly.assertThat(idResult4.getErrorCode()).isNull();
            softly.assertThat(idResult4.getErrorMsg()).isNull();
            softly.assertThat(idResult4.getValue()).isNotNull().isNotEqualTo(record);

            SysMenu target = new SysMenu();
            BeanUtils.copyProperties(record, target);
            target.setMenuPath(updateUser2.getMenuPath());
            target.setMenuName(updateUser2.getMenuName());
            softly.assertThat(idResult4.getValue()).isNotNull().isEqualTo(target);
        });

        // 删除数据
        SysMenu deleteUser = new SysMenu();
        deleteUser.setId(exceptedId);
        deleteUser.setMenuPath("10000");
        Result<Boolean> deleteResult = sysMenuController.deleteByPrimaryKey(deleteUser);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(deleteResult).isNotNull();
            softly.assertThat(deleteResult.isSuccess()).isTrue();
            softly.assertThat(deleteResult.getErrorCode()).isNull();
            softly.assertThat(deleteResult.getErrorMsg()).isNull();
            softly.assertThat(deleteResult.getValue()).isNotNull().isEqualTo(true);
        });

        Result<SysMenu> idResult5 = sysMenuService.findByPrimaryKey(exceptedId);
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
        SysMenu record = new SysMenu();
        record.setMenuName("10001");
        record.setMenuPath("第一次记录日志信息");

        // 创建记录
        Result<Long> createResult = sysMenuService.create(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(createResult).isNotNull();
            softly.assertThat(createResult.isSuccess()).isTrue();
            softly.assertThat(createResult.getErrorCode()).isNull();
            softly.assertThat(createResult.getErrorMsg()).isNull();
            softly.assertThat(createResult.getValue()).isNotNull().isEqualTo(exceptedId);
        });

        // 使用各种查询 API 查询数据库中的记录，和保存之后的记录进行比较
        Result<Integer> countResult = sysMenuService.findCount(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(countResult).isNotNull();
            softly.assertThat(countResult.isSuccess()).isTrue();
            softly.assertThat(countResult.getErrorCode()).isNull();
            softly.assertThat(countResult.getErrorMsg()).isNull();
            softly.assertThat(countResult.getValue()).isNotNull().isEqualTo(1);
        });

        // 删除数据
        SysMenu deleteUser = new SysMenu();
        deleteUser.setId(exceptedId);
        Result<Boolean> deleteResult = sysMenuService.deleteByCondition(deleteUser);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(deleteResult).isNotNull();
            softly.assertThat(deleteResult.isSuccess()).isTrue();
            softly.assertThat(deleteResult.getErrorCode()).isNull();
            softly.assertThat(deleteResult.getErrorMsg()).isNull();
            softly.assertThat(deleteResult.getValue()).isNotNull().isEqualTo(true);
        });

        Result<SysMenu> idResult4 = sysMenuService.findByPrimaryKey(exceptedId);
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
        SysMenu record = new SysMenu();
        record.setMenuName("10002");
        record.setMenuPath("第一次记录日志信息");

        // 创建记录
        Result<Long> createResult = sysMenuService.create(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(createResult).isNotNull();
            softly.assertThat(createResult.isSuccess()).isTrue();
            softly.assertThat(createResult.getErrorCode()).isNull();
            softly.assertThat(createResult.getErrorMsg()).isNull();
            softly.assertThat(createResult.getValue()).isNotNull();
        });
        Long exceptedId = createResult.getValue();

        // 使用各种查询 API 查询数据库中的记录，和保存之后的记录进行比较
        Result<SysMenu> countResult = sysMenuService.findByPrimaryKey(exceptedId);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(countResult).isNotNull();
            softly.assertThat(countResult.isSuccess()).isTrue();
            softly.assertThat(countResult.getErrorCode()).isNull();
            softly.assertThat(countResult.getErrorMsg()).isNull();
            softly.assertThat(countResult.getValue()).isNotNull().isEqualTo(record);
        });

        // 删除数据
        boolean checkDBResult = DbUtils.checkDBResult(sysMenuMapper.deleteByPrimaryKey(exceptedId));
        Assert.assertTrue(checkDBResult);

        Result<SysMenu> idResult4 = sysMenuService.findByPrimaryKey(exceptedId);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult4).isNotNull();
            softly.assertThat(idResult4.isSuccess()).isTrue();
            softly.assertThat(idResult4.getErrorCode()).isNull();
            softly.assertThat(idResult4.getErrorMsg()).isNull();
            softly.assertThat(idResult4.getValue()).isNull();
        });

        // 查询逻辑删除的数据
        SysMenu condition1 = new SysMenu();
        condition1.setId(exceptedId);
        List<SysMenu> conditionResult1 = sysMenuMapper.selectListIncludeLogicDelete(condition1);
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
        SysMenu record = new SysMenu();
        record.setMenuName("10003");
        record.setMenuPath("第一次记录日志信息");

        // 创建记录
        Result<Long> createResult = sysMenuService.create(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(createResult).isNotNull();
            softly.assertThat(createResult.isSuccess()).isTrue();
            softly.assertThat(createResult.getErrorCode()).isNull();
            softly.assertThat(createResult.getErrorMsg()).isNull();
            softly.assertThat(createResult.getValue()).isNotNull();
        });
        Long exceptedId = createResult.getValue();

        // 使用各种查询 API 查询数据库中的记录，和保存之后的记录进行比较
        Result<SysMenu> countResult = sysMenuService.findByPrimaryKey(exceptedId);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(countResult).isNotNull();
            softly.assertThat(countResult.isSuccess()).isTrue();
            softly.assertThat(countResult.getErrorCode()).isNull();
            softly.assertThat(countResult.getErrorMsg()).isNull();
            softly.assertThat(countResult.getValue()).isNotNull().isEqualTo(record);
        });

        // 删除数据
        SysMenu deleteCondition = new SysMenu();
        deleteCondition.setId(exceptedId);
        deleteCondition.setMenuPath(record.getMenuPath());
        boolean checkDBResult = DbUtils.checkDBResult(sysMenuMapper.deleteByPrimaryKey(deleteCondition));
        Assert.assertTrue(checkDBResult);

        Result<SysMenu> idResult4 = sysMenuService.findByPrimaryKey(exceptedId);
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
        SysMenu record = new SysMenu();
        record.setMenuName("10004");
        record.setMenuPath("第一次记录日志信息");

        // 创建记录
        Result<Long> createResult = sysMenuService.create(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(createResult).isNotNull();
            softly.assertThat(createResult.isSuccess()).isTrue();
            softly.assertThat(createResult.getErrorCode()).isNull();
            softly.assertThat(createResult.getErrorMsg()).isNull();
            softly.assertThat(createResult.getValue()).isNotNull();
        });
        Long exceptedId = createResult.getValue();

        // 使用各种查询 API 查询数据库中的记录，和保存之后的记录进行比较
        Result<SysMenu> countResult = sysMenuService.findByPrimaryKey(exceptedId);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(countResult).isNotNull();
            softly.assertThat(countResult.isSuccess()).isTrue();
            softly.assertThat(countResult.getErrorCode()).isNull();
            softly.assertThat(countResult.getErrorMsg()).isNull();
            softly.assertThat(countResult.getValue()).isNotNull().isEqualTo(record);
        });

        // 更新数据
        Map<String, Object> updateUseMap = new HashMap<>();
        updateUseMap.put("menuName", null);
        updateUseMap.put("menuPath", "我是更新记录日志信息");
        SysMenu updateCondition = new SysMenu();
        updateCondition.setId(exceptedId);
        updateCondition.setMenuPath(record.getMenuPath());
        boolean checkDBResult = sysMenuService.updateUseMapByCondition(updateUseMap, updateCondition).getValue();
        Assert.assertTrue(checkDBResult);

        Result<SysMenu> idResult4 = sysMenuService.findByPrimaryKey(exceptedId);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult4).isNotNull();
            softly.assertThat(idResult4.isSuccess()).isTrue();
            softly.assertThat(idResult4.getErrorCode()).isNull();
            softly.assertThat(idResult4.getErrorMsg()).isNull();
            softly.assertThat(idResult4.getValue()).isNotNull().isNotEqualTo(record);

            SysMenu target = new SysMenu();
            BeanUtils.copyProperties(record, target);
            target.setMenuPath((String) updateUseMap.get("menuPath"));
            target.setMenuName((String) updateUseMap.get("menuName"));
            softly.assertThat(idResult4.getValue()).isNotNull().isEqualTo(target);
        });
    }

    /**
     * "自定义通用SQL查询条件"测试案例1
     */
    @Transactional
    @Test
    public void createBatchAndWhereExtraConditionTest1() {
        List<SysMenu> exceptedList = new ArrayList<>();
        List<SysMenu> allList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            SysMenu record = new SysMenu();
            record.setMenuIcon("10000_" + 1);
            record.setMenuName("Jerry_" + i);
            if (i >= 5) {
                exceptedList.add(record);
            } else {
                // 干扰查询数据，检验查询条件正确
                record.setMenuName("GoodBody_" + i);
            }
            allList.add(record);
        }
        // 创建记录
        Result<Boolean> createResult = sysMenuService.createBatch(allList);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(createResult).isNotNull();
            softly.assertThat(createResult.isSuccess()).isTrue();
            softly.assertThat(createResult.getErrorCode()).isNull();
            softly.assertThat(createResult.getErrorMsg()).isNull();
            softly.assertThat(createResult.getValue()).isTrue();
        });

        Page<SysMenu> pageCondition2 = new Page<>();
        HashMap<String, Object> extraInfo = new HashMap<>();
        extraInfo.put("menuNameLike", "erry_");
        pageCondition2.setExtraInfo(extraInfo);
        pageCondition2.setPageNum(1);
        pageCondition2.setPageSize(exceptedList.size());
        Page<SysMenu> pageResult2 = sysMenuController.findPage(pageCondition2);
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
        List<Long> ids = pageResult2.getValue().stream().map(SysMenu::getId).collect(Collectors.toList());
        List<SysMenu> users = sysMenuMapper.selectListByPrimaryKey(ids);
        Assertions.assertThat(users).isNotEmpty().hasSize(exceptedList.size());

        List<SysMenu> userIds = pageResult2.getValue().stream().map(user -> {
            SysMenu sysMenu = new SysMenu();
            sysMenu.setId(user.getId());
            return sysMenu;
        }).collect(Collectors.toList());
        List<SysMenu> users2 = sysMenuMapper.selectListWithPrimaryKey(userIds);
        Assertions.assertThat(users2).isNotEmpty().hasSize(exceptedList.size());

        Assertions.assertThat(users).isEqualTo(users2);

        // 删除记录
        int deleteCount = sysMenuMapper.deleteByPrimaryKey(ids.get(0));
        Assertions.assertThat(deleteCount).isEqualTo(1);

        // 再次查询，获取逻辑删除记录
        List<SysMenu> users3 = sysMenuMapper.selectListByPrimaryKey(ids);
        SysMenu sysMenuDeleted = pageResult2.getValue().get(0);
        Assertions.assertThat(users3).isNotEmpty().hasSize(exceptedList.size() - 1).doesNotContain(sysMenuDeleted);
        List<SysMenu> users4 = sysMenuMapper.selectListByPrimaryKeyIncludeLogicDelete(ids);
        Assertions.assertThat(users4).isNotEmpty().hasSize(exceptedList.size());
        Assertions.assertThat(users4.get(0))
                .isEqualToComparingOnlyGivenFields(sysMenuDeleted, "id", "menuName", "menuPath");
    }

    /**
     * "自定义通用SQL查询条件"测试案例2
     */
    @Transactional
    @Test
    public void createBatchAndWhereExtraConditionTest2() {
        List<SysMenu> exceptedList = new ArrayList<>();
        List<SysMenu> exceptedList2 = new ArrayList<>();
        List<SysMenu> allList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            SysMenu record = new SysMenu();
            record.setMenuIcon("iconImage_" + i);
            record.setMenuName("DashBoard_" + i);
            if (i % 2 == 0) {
                record.setMenuPath("/setting_" + i);
                exceptedList2.add(record);
            } else {
                record.setMenuPath("/user_" + i);
            }
            if (i >= 5) {
                if (i % 2 == 0) {
                    exceptedList.add(record);
                }
            } else {
                // 干扰查询数据，检验查询条件正确
                record.setMenuName("Publish_" + i);
            }
            allList.add(record);
        }
        // 创建记录
        Result<Boolean> createResult = sysMenuService.createBatch(allList);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(createResult).isNotNull();
            softly.assertThat(createResult.isSuccess()).isTrue();
            softly.assertThat(createResult.getErrorCode()).isNull();
            softly.assertThat(createResult.getErrorMsg()).isNull();
            softly.assertThat(createResult.getValue()).isTrue();
        });

        Page<SysMenu> pageCondition2 = new Page<>();
        HashMap<String, Object> extraInfo = new HashMap<>();
        extraInfo.put("menuNameLike", "Board_");
        SysMenuDTO param = new SysMenuDTO();
        param.setMenuPathLike("setting");
        pageCondition2.setParam(param);
        pageCondition2.setExtraInfo(extraInfo);
        pageCondition2.setPageNum(1);
        pageCondition2.setPageSize(exceptedList.size());
        Page<SysMenu> pageResult2 = sysMenuController.findPage(pageCondition2);
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

        Result<List<SysMenu>> result1 = sysMenuController.findList(param);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result1).isNotNull();
            softly.assertThat(result1.isSuccess()).isTrue();
            softly.assertThat(result1.getErrorCode()).isNull();
            softly.assertThat(result1.getErrorMsg()).isNull();
            softly.assertThat(result1.getValue()).isNotEmpty().hasSize(exceptedList2.size());
            softly.assertThat(result1.getValue()).isEqualTo(exceptedList2);
        });

        Page<SysMenu> pageCondition3 = new Page<>();
        pageCondition3.setParam(param);
        pageCondition3.setSort(new Sort(Direction.ASC, "id"));
        Result<List<SysMenu>> result2 = sysMenuController.findListWithSort(pageCondition3);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result2).isNotNull();
            softly.assertThat(result2.isSuccess()).isTrue();
            softly.assertThat(result2.getErrorCode()).isNull();
            softly.assertThat(result2.getErrorMsg()).isNull();
            softly.assertThat(result2.getValue()).isNotEmpty().hasSize(exceptedList2.size());
            softly.assertThat(result2.getValue()).isEqualTo(exceptedList2);
        });

        // 使用主键id查询
        List<Long> ids = pageResult2.getValue().stream().map(SysMenu::getId).collect(Collectors.toList());
        List<SysMenu> users = sysMenuMapper.selectListByPrimaryKey(ids);
        Assertions.assertThat(users).isNotEmpty().hasSize(exceptedList.size());

        List<SysMenu> userIds = pageResult2.getValue().stream().map(user -> {
            SysMenu sysMenu = new SysMenu();
            sysMenu.setId(user.getId());
            return sysMenu;
        }).collect(Collectors.toList());
        List<SysMenu> users2 = sysMenuMapper.selectListWithPrimaryKey(userIds);
        Assertions.assertThat(users2).isNotEmpty().hasSize(exceptedList.size());

        Assertions.assertThat(users).isEqualTo(users2);

        // 删除记录
        int deleteCount = sysMenuMapper.deleteByCondition(param);
        Assertions.assertThat(deleteCount).isEqualTo(exceptedList2.size());

        // 再次查询，获取剩余5条记录
        List<SysMenu> users3 = sysMenuMapper.selectList(null);
        Assertions.assertThat(users3.size()).isEqualTo(5);

        // 更新删除记录
        int toUpdate = sysMenuMapper.deleteByConditionAndUpdate(new SysMenu().setMenuIcon("toUpdate"),
                new SysMenuDTO().setMenuIconLike("iconImage"));
        Assertions.assertThat(toUpdate).isEqualTo(5);

        // 查询记录，检查数据
        List<SysMenu> users4 = sysMenuMapper.selectList(null);
        Assertions.assertThat(users4.size()).isEqualTo(0);
        Integer count = jdbcTemplate.queryForObject("select count(*) from t_sys_menu where menu_icon = 'toUpdate'",
                Integer.class);
        Assertions.assertThat(count).isEqualTo(5);
    }

    /**
     * <ul>
     * <li>第一步：会在 {@link SequenceKeyGenerator} 中初始化此 class
     * <li>第二步：会在 {@link MyConfig5#initSequence(ApplicationReadyEvent)} 中初始化它的代理
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

    @ComponentScan(basePackageClasses = SysMenuControllerIntTests.class)
    @Profile("SysUserLogControllerIntTests")
    @Configuration
    public static class MyConfig5 {
        @Bean
        public ParameterHandlerCustomizer persistenceParameterHandler() {
            return new DefaultParameterHandlerCustomizer();
        }

        @EventListener
        public void initSequence(ApplicationReadyEvent event) {
            JdbcTemplate jdbcTemplate = event.getApplicationContext().getBean(JdbcTemplate.class);
            Sequence sequenceBean = parameter -> {
                Long nextId = jdbcTemplate.queryForObject("select ifnull(max(`id`) ,0) + 1 from t_sys_menu",
                        Long.class);
                log.info("nextId: {}, parameter: {}", nextId, JsonApi.toJson(parameter));
                return nextId;
            };

            Map<String, Sequence> sequenceBeanMap = TableMetaObject.getSequenceBeanMap();
            for (Entry<String, Sequence> entry : sequenceBeanMap.entrySet()) {
                Sequence entryValue = entry.getValue();
                if (entryValue instanceof SequenceCustomizer) {
                    SequenceCustomizer value = (SequenceCustomizer) entryValue;
                    value.setSequence(sequenceBean);
                } else {
                    log.warn("entryValue is not instanceof SequenceCustomizer: {}", entryValue);
                }
            }
        }
    }
}
