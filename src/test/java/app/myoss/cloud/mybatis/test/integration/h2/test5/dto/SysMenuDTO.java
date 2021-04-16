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

package app.myoss.cloud.mybatis.test.integration.h2.test5.dto;

import app.myoss.cloud.mybatis.test.integration.h2.test5.entity.SysMenu;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 系统菜单DTO
 *
 * @author Jerry.Chen
 * @since 2021年4月15日 下午10:38:49
 */
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class SysMenuDTO extends SysMenu {
    private static final long serialVersionUID = -9190004404904080616L;

    private String            menuPathLike;

    private String            menuIconLike;
}
