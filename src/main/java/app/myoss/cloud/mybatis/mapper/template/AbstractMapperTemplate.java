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

package app.myoss.cloud.mybatis.mapper.template;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import app.myoss.cloud.core.exception.BizRuntimeException;

/**
 * 生成通用 insert/update/delete/select MappedStatement 模版基类
 *
 * @author Jerry.Chen
 * @since 2018年4月25日 下午6:50:36
 */
public abstract class AbstractMapperTemplate {
    protected XMLLanguageDriver xmlLanguageDriver = new XMLLanguageDriver();

    /**
     * 用来初始化 {@link ProviderSqlSource}
     *
     * @param record 实体对象
     * @return 动态sql语句
     */
    public String dynamicSql(Object record) {
        return "dynamicSql";
    }

    /**
     * 获取"自定义通用SQL查询条件"
     *
     * <pre>
     *     &lt;sql id=&quot;Where_Extend&quot;&gt;
     *         &lt;if test=&quot;nameLike != null&quot;&gt;
     *             AND name LIKE CONCAT('%', #{nameLike,jdbcType=VARCHAR}, '%')
     *         &lt;/if&gt;
     *     &lt;/sql&gt;
     * </pre>
     *
     * @param ms sql语句节点信息
     * @return 自定义通用SQL查询条件
     */
    public String getWhereExtend(MappedStatement ms) {
        Configuration configuration = ms.getConfiguration();
        String namespace = StringUtils.substringBeforeLast(ms.getId(), ".");
        String sqlId = namespace + ".Where_Extend";
        if (!configuration.getSqlFragments().containsKey(sqlId)) {
            return null;
        }

        XNode node = configuration.getSqlFragments().get(sqlId);
        try {
            StringWriter nodeContent = getNodeContent(node.getNode());
            return nodeContent.toString();
        } catch (TransformerException e) {
            throw new BizRuntimeException("get sqlFragments content failed, sqlId: " + sqlId, e);
        }
    }

    /**
     * 获取"自定义通用SQL查询条件"
     *
     * <pre>
     *     &lt;sql id=&quot;Where_Extend_Condition&quot;&gt;
     *         &lt;if test=&quot;condition.nameLike != null&quot;&gt;
     *             AND name LIKE CONCAT('%', #{condition.nameLike,jdbcType=VARCHAR}, '%')
     *         &lt;/if&gt;
     *     &lt;/sql&gt;
     * </pre>
     *
     * @param ms sql语句节点信息
     * @return 自定义通用SQL查询条件
     */
    public StringBuilder getWhereExtendCondition(MappedStatement ms) {
        Configuration configuration = ms.getConfiguration();
        String namespace = StringUtils.substringBeforeLast(ms.getId(), ".");
        String sqlId = namespace + ".Where_Extend_Condition";
        if (!configuration.getSqlFragments().containsKey(sqlId)) {
            return null;
        }

        XNode node = configuration.getSqlFragments().get(sqlId);
        StringBuilder sb = new StringBuilder();
        sb.append("  <if test=\"condition != null\">\n    ");
        try {
            StringWriter nodeContent = getNodeContent(node.getNode());
            sb.append(nodeContent.toString());
            sb.append("\n  </if>\n");
        } catch (TransformerException e) {
            throw new BizRuntimeException("get sqlFragments content failed, sqlId: " + sqlId, e);
        }
        return sb;
    }

    /**
     * 获取"自定义通用SQL查询条件"
     *
     * @param ms sql语句节点信息
     * @return 自定义通用SQL查询条件
     */
    public StringBuilder getWhereExtraCondition(MappedStatement ms) {
        Configuration configuration = ms.getConfiguration();
        String namespace = StringUtils.substringBeforeLast(ms.getId(), ".");
        String sqlId = namespace + ".Where_Extra_Condition";
        if (!configuration.getSqlFragments().containsKey(sqlId)) {
            return null;
        }

        XNode node = configuration.getSqlFragments().get(sqlId);
        StringBuilder sb = new StringBuilder();
        sb.append("  <if test=\"extraCondition != null\">\n    ");
        try {
            StringWriter nodeContent = getNodeContent(node.getNode());
            sb.append(nodeContent.toString());
        } catch (TransformerException e) {
            throw new BizRuntimeException("get sqlFragments content failed, sqlId: " + sqlId, e);
        }
        sb.append("\n  </if>\n");
        return sb;
    }

    /**
     * 获取 XML Node 节点的内容，转换为普通文本内容
     *
     * @param node XML Node 节点
     * @return XML Node 节点的内容
     * @throws TransformerException 转换异常信息
     */
    public static StringWriter getNodeContent(Node node) throws TransformerException {
        StringWriter writer = new StringWriter();
        StreamResult streamResult = new StreamResult(writer);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            DOMSource domSource = new DOMSource(item);
            transformer.transform(domSource, streamResult);
        }
        return writer;
    }
}
