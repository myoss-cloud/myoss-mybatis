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

package app.myoss.cloud.mybatis.repository.service.impl;

import java.util.Map;

import org.apache.ibatis.mapping.SqlCommandType;

import app.myoss.cloud.core.lang.dto.Result;
import app.myoss.cloud.mybatis.mapper.template.CrudMapper;

/**
 * 实现数据库表增、删、改、查常用操作的基类，不会检查通用查询条件字段是否为空，只要查询条件对象不为 {@code null}
 * 即可。切记对于数据库表数据较多的时候，请不要使用此基类，应该使用 {@link BaseCrudServiceImpl}，去重写
 * {@link BaseCrudServiceImpl#checkCommonQueryConditionIsAllNull(SqlCommandType, Result, Object, Map)}
 * 方法，这才是比较安全的做法，不然很可能被人使用为直接查询全表的数据。
 *
 * @param <M> "实体类"的 Mapper Interface 接口
 * @param <T> 实体类
 * @author Jerry.Chen
 * @since 2018年5月20日 下午5:46:59
 * @see BaseCrudServiceImpl
 */
public class UncheckQueryConditionCrudServiceImpl<M extends CrudMapper<T>, T> extends BaseCrudServiceImpl<M, T> {
    @Override
    protected boolean checkCommonQueryConditionIsAllNull(SqlCommandType sqlCommandType, Result<?> result, T condition,
                                                         Map<String, Object> extraCondition) {
        if (!result.isSuccess()) {
            return false;
        }
        if (condition == null) {
            result.setSuccess(false).setErrorCode("valueIsBlank").setErrorMsg("查询条件不能为空");
            return false;
        }
        return result.isSuccess();
    }
}
