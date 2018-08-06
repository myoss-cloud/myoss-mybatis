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

package com.github.myoss.phoenix.mybatis.repository.entity;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.Labels;
import com.github.myoss.phoenix.core.constants.PhoenixConstants;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 测试 {@link AuditEntity}、{@link AuditIdEntity}、{@link LogicDeleteEntity}
 *
 * @author Jerry.Chen
 * @since 2018年5月18日 下午1:47:19
 */
public class EntityTests {
    /**
     * 测试实体类基类 fast json label 功能
     */
    @Test
    public void labelTest1() {
        LocalDateTime localDateTime = LocalDateTime.of(2018, 5, 1, 10, 0, 0);
        ZoneOffset offset = ZoneOffset.ofHours(8);
        Date date = Date.from(localDateTime.toInstant(offset));

        MyEntityTest1 entity1 = new MyEntityTest1();
        entity1.setId("1000");
        entity1.setIsDeleted(PhoenixConstants.N);
        entity1.setGmtCreated(date);
        entity1.setGmtModified(date);
        entity1.setCreator("system");
        entity1.setModifier("system");
        entity1.setUserName("jerry");
        entity1.setJoinTime(date);
        entity1.setUserId(8888L);

        String actual = JSON.toJSONString(entity1);
        String excepted = "{\"creator\":\"system\",\"gmtCreated\":1525140000000,\"gmtModified\":1525140000000,\"id\":\"1000\",\"isDeleted\":\"N\",\"joinTime\":1525140000000,\"modifier\":\"system\",\"userId\":8888,\"userName\":\"jerry\"}";
        assertEquals(excepted, actual);

        String actual2 = JSON.toJSONString(entity1, Labels.excludes(LogicDeleteEntity.LABEL_LOGIC_DELETE_ENTITY));
        String excepted2 = "{\"creator\":\"system\",\"gmtCreated\":1525140000000,\"gmtModified\":1525140000000,\"id\":\"1000\",\"joinTime\":1525140000000,\"modifier\":\"system\",\"userId\":8888,\"userName\":\"jerry\"}";
        assertEquals(excepted2, actual2);

        String actual3 = JSON.toJSONString(entity1, Labels.excludes(AuditIdEntity.LABEL_AUDIT_ID_ENTITY));
        String excepted3 = "{\"creator\":\"system\",\"gmtCreated\":1525140000000,\"gmtModified\":1525140000000,\"isDeleted\":\"N\",\"joinTime\":1525140000000,\"modifier\":\"system\",\"userId\":8888,\"userName\":\"jerry\"}";
        assertEquals(excepted3, actual3);

        String actual4 = JSON.toJSONString(entity1, Labels.excludes(AuditEntity.LABEL_AUDIT_ENTITY));
        String excepted4 = "{\"id\":\"1000\",\"isDeleted\":\"N\",\"joinTime\":1525140000000,\"userId\":8888,\"userName\":\"jerry\"}";
        assertEquals(excepted4, actual4);

        String actual5 = JSON.toJSONString(entity1,
                Labels.excludes(AuditEntity.LABEL_AUDIT_ENTITY, AuditIdEntity.LABEL_AUDIT_ID_ENTITY));
        String excepted5 = "{\"isDeleted\":\"N\",\"joinTime\":1525140000000,\"userId\":8888,\"userName\":\"jerry\"}";
        assertEquals(excepted5, actual5);

        String actual6 = JSON.toJSONString(entity1, Labels.excludes(AuditEntity.LABEL_AUDIT_ENTITY,
                AuditIdEntity.LABEL_AUDIT_ID_ENTITY, LogicDeleteEntity.LABEL_LOGIC_DELETE_ENTITY));
        String excepted6 = "{\"joinTime\":1525140000000,\"userId\":8888,\"userName\":\"jerry\"}";
        assertEquals(excepted6, actual6);

    }

    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class MyEntityTest1 extends AuditIdEntity<String> {
        private static final long serialVersionUID = -664927562242392093L;
        private String            userName;
        private Date              joinTime;
        private Long              userId;
    }
}
