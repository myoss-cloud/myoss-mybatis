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

package app.myoss.cloud.sequence.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

import org.springframework.util.CollectionUtils;

import app.myoss.cloud.sequence.SequenceRange;
import app.myoss.cloud.sequence.SequenceRepository;
import app.myoss.cloud.sequence.exception.SequenceException;
import app.myoss.cloud.sequence.utils.RandomSequence;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 使用数据库生成序列
 *
 * @author Jerry.Chen
 * @since 2018年12月16日 下午12:12:06
 */
@Slf4j
public class RdsSequenceRepository implements SequenceRepository {
    /**
     * 默认内步长
     */
    public static final int         DEFAULT_INNER_STEP               = 1000;
    /**
     * 默认重试次数
     */
    public static final int         DEFAULT_RETRY_TIMES              = 2;

    /**
     * 默认序列数据库表名
     */
    public static final String      DEFAULT_TABLE_NAME               = "sequence";
    /**
     * 默认存储序列名称的列名
     */
    public static final String      DEFAULT_NAME_COLUMN_NAME         = "name";
    /**
     * 默认存储序列值的列名
     */
    public static final String      DEFAULT_VALUE_COLUMN_NAME        = "value";
    /**
     * 默认存储序列创建时间的列名
     */
    public static final String      DEFAULT_GMT_CREATED_COLUMN_NAME  = "gmt_created";
    /**
     * 默认存储序列最后更新时间的列名
     */
    public static final String      DEFAULT_GMT_MODIFIED_COLUMN_NAME = "gmt_modified";

    /**
     * 默认数据源个数
     */
    public static final int         DEFAULT_DATA_SOURCE_COUNT        = 2;
    /**
     * 默认自适应开关
     */
    public static final Boolean     DEFAULT_ADJUST                   = false;

    private static final long       DELTA                            = 100000000L;

    /**
     * group阵列
     */
    @Setter
    @Getter
    private List<String>            dbGroupKeys;
    /**
     * 数据源
     */
    @Setter
    @Getter
    private Map<String, DataSource> dataSourceMap;
    /**
     * 数据源个数
     */
    @Setter
    @Getter
    private int                     dataSourceCount                  = DEFAULT_DATA_SOURCE_COUNT;

    /**
     * 自适应开关
     */
    @Setter
    @Getter
    private boolean                 adjust                           = DEFAULT_ADJUST;
    /**
     * 重试次数
     */
    @Setter
    @Getter
    private int                     retryTimes                       = DEFAULT_RETRY_TIMES;

    /**
     * 内步长
     */
    @Setter
    @Getter
    private int                     innerStep                        = DEFAULT_INNER_STEP;

    /**
     * 外步长
     */
    private int                     outStep                          = DEFAULT_INNER_STEP;

    /**
     * 序列所在的表名
     */
    @Setter
    @Getter
    private String                  tableName                        = DEFAULT_TABLE_NAME;
    /**
     * 存储序列名称的列名
     */
    @Setter
    @Getter
    private String                  nameColumnName                   = DEFAULT_NAME_COLUMN_NAME;
    /**
     * 存储序列值的列名
     */
    @Setter
    @Getter
    private String                  valueColumnName                  = DEFAULT_VALUE_COLUMN_NAME;
    /**
     * 存储序列创建时间的列名
     */
    @Setter
    @Getter
    private String                  gmtCreatedColumnName             = DEFAULT_GMT_CREATED_COLUMN_NAME;
    /**
     * 存储序列最后更新时间的列名
     */
    @Setter
    @Getter
    private String                  gmtModifiedColumnName            = DEFAULT_GMT_MODIFIED_COLUMN_NAME;

    private final ReentrantLock     lock                             = new ReentrantLock();
    protected volatile boolean      isInit                           = false;
    private volatile String         selectSql;
    private volatile String         updateSql;
    private volatile String         insertSql;

    @Override
    public void init() {
        if (isInit) {
            return;
        }
        try {
            lock.lockInterruptibly();
        } catch (InterruptedException e) {
            throw new SequenceException("interrupt", e);
        }

        try {
            if (isInit) {
                return;
            }
            if (CollectionUtils.isEmpty(dataSourceMap)) {
                throw new NullPointerException("dataSourceMap is empty");
            }
            if (CollectionUtils.isEmpty(dbGroupKeys)) {
                dbGroupKeys = new ArrayList<>(dataSourceMap.keySet());
            }
            if (dbGroupKeys.size() >= dataSourceCount) {
                dataSourceCount = dbGroupKeys.size();
            } else {
                for (int i = dbGroupKeys.size(); i < dataSourceCount; i++) {
                    dbGroupKeys.add(dataSourceCount + "-OFF");
                }
            }
            // 计算外步长
            outStep = innerStep * dataSourceCount;

            StringBuilder sb = new StringBuilder();
            sb.append(this.getClass().getSimpleName());
            sb.append("初始化完成：").append(System.lineSeparator());
            sb.append("innerStep: ").append(this.innerStep).append(System.lineSeparator());
            sb.append("dataSource: ").append(dataSourceCount).append("个: ");
            for (String str : dbGroupKeys) {
                sb.append("[").append(str).append("]、");
            }
            sb.append(System.lineSeparator());
            sb.append("adjust：").append(adjust).append(System.lineSeparator());
            sb.append("retryTimes: ").append(retryTimes).append(System.lineSeparator());
            sb.append("tableName: ").append(tableName).append(System.lineSeparator());
            sb.append("nameColumnName: ").append(nameColumnName).append(System.lineSeparator());
            sb.append("valueColumnName: ").append(valueColumnName).append(System.lineSeparator());
            sb.append("gmtCreatedColumnName: ").append(gmtCreatedColumnName).append(System.lineSeparator());
            sb.append("gmtModifiedColumnName: ").append(gmtModifiedColumnName).append(System.lineSeparator());
            log.info(sb.toString());
        } finally {
            isInit = true;
            lock.unlock();
        }
    }

    /**
     * 检查 groupKey 对象是否已经关闭
     *
     * @param groupKey groupKey
     * @return 是否已经关闭
     */
    protected boolean isOffState(String groupKey) {
        return groupKey.toUpperCase().endsWith("-OFF");
    }

    /**
     * 校验值是否正确
     *
     * @param index group内的序号，从0开始
     * @param value 当前取的值
     * @return 是否正确
     */
    protected boolean check(int index, long value) {
        return (value % outStep) == (index * innerStep);
    }

    /**
     * 检查并调整某个 sequence name 的值
     *
     * <pre>
     * 1、如果 sequence 不存在，插入值，并初始化值。
     * 2、如果已经存在，但有重叠，重新生成。
     * 3、如果已经存在，且无重叠。
     * </pre>
     *
     * @param name sequence name
     */
    @Override
    public void adjust(String name) {
        Objects.requireNonNull(name, "name is empty");
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        for (int i = 0; i < dbGroupKeys.size(); i++) {
            String key = dbGroupKeys.get(i);
            if (isOffState(key)) {
                // 已经关掉，不处理
                continue;
            }
            try {
                DataSource dataSource = dataSourceMap.get(key);
                conn = dataSource.getConnection();
                stmt = conn.prepareStatement(getSelectSql());
                stmt.setString(1, name);
                rs = stmt.executeQuery();
                int item = 0;
                while (rs.next()) {
                    item++;
                    long value = rs.getLong(this.getValueColumnName());
                    if (!check(i, value)) {
                        // 检验初值
                        if (this.isAdjust()) {
                            this.adjustUpdate(i, value, name);
                        } else {
                            throw new SequenceException(
                                    "数据库中配置的初值出错！请调整你的数据库，或者启动adjust开关。name = " + name + ", value = " + value);
                        }
                    }
                }
                if (item == 0) {
                    if (this.isAdjust()) {
                        this.adjustInsert(i, name);
                    } else {
                        throw new SequenceException("数据库中未配置该sequence！请往数据库中插入sequence记录，或者启动adjust开关。name = " + name);
                    }
                }
            } catch (SQLException e) {
                throw new SequenceException("初值校验和自适应过程中出错.", e);
            } finally {
                closeDbResource(rs, stmt, conn);
            }
        }
    }

    private void adjustUpdate(int index, long value, String name) throws SequenceException {
        long newValue = (value - value % outStep) + outStep + index * innerStep;
        Connection conn = null;
        PreparedStatement stmt = null;
        String key = dbGroupKeys.get(index);
        try {
            DataSource dataSource = dataSourceMap.get(key);
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(getUpdateSql());
            stmt.setLong(1, newValue);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.setString(3, name);
            stmt.setLong(4, value);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SequenceException("failed to auto adjust init value at " + name + " update affectedRow =0");
            }
            log.info(key + "更新初值成功！sequence Name：" + name + "更新过程：" + value + "-->" + newValue);
        } catch (SQLException e) {
            throw new SequenceException("出现SQLException，更新初值自适应失败！dbGroupIndex: " + key + "，sequence Name：" + name
                    + "更新过程：" + value + "-->" + newValue, e);
        } finally {
            closeDbResource(null, stmt, conn);
        }
    }

    private void adjustInsert(int index, String name) throws SequenceException {
        long newValue = index * innerStep;
        Connection conn = null;
        PreparedStatement stmt = null;
        String key = dbGroupKeys.get(index);
        try {
            DataSource dataSource = dataSourceMap.get(key);
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(getInsertSql());
            stmt.setString(1, name);
            stmt.setLong(2, newValue);
            Timestamp now = new Timestamp(System.currentTimeMillis());
            stmt.setTimestamp(3, now);
            stmt.setTimestamp(4, now);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SequenceException("failed to auto adjust init value at " + name + " update affectedRow =0");
            }
            log.info(key + "插入初值成功！sequence Name: " + name + ", value: " + newValue);
        } catch (SQLException e) {
            throw new SequenceException(
                    "出现SQLException，插入初值自适应失败！dbGroupIndex: " + key + "，sequence Name：" + name + "，value:" + newValue,
                    e);
        } finally {
            closeDbResource(null, stmt, conn);
        }
    }

    @Override
    public SequenceRange nextRange(String name) throws SequenceException {
        Objects.requireNonNull(name, "name is empty");
        long oldValue;
        long newValue;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int[] randomIntSequence = RandomSequence.randomIntSequence(dataSourceCount);
        for (int i = 0; i < retryTimes; i++) {
            for (int j = 0; j < dataSourceCount; j++) {
                int index = randomIntSequence[j];
                String key = dbGroupKeys.get(index);
                if (isOffState(key)) {
                    continue;
                }

                DataSource dataSource = dataSourceMap.get(key);
                try {
                    conn = dataSource.getConnection();
                    stmt = conn.prepareStatement(getSelectSql());
                    stmt.setString(1, name);
                    rs = stmt.executeQuery();
                    if (!rs.next()) {
                        throw new SequenceException(
                                "数据库中找不到对应的 sequence 记录，dbGroupIndex = " + key + ", name = " + name);
                    }
                    oldValue = rs.getLong(1);
                    if (oldValue < 0) {
                        log.warn("Sequence value cannot be less than zero, value = {}, , please check table ", oldValue,
                                getTableName());
                        continue;
                    }
                    if (oldValue > Long.MAX_VALUE - DELTA) {
                        log.warn("Sequence value overflow, value = {}, please check table ", oldValue, getTableName());
                        continue;
                    }
                    newValue = oldValue + outStep;
                    if (!check(index, newValue)) {
                        // 新算出来的值有问题
                        if (this.isAdjust()) {
                            // 设置成新的调整值
                            newValue = (newValue - newValue % outStep) + outStep + index * innerStep;
                        } else {
                            throw new SequenceException("数据库中配置的初值出错！请调整你的数据库，或者启动adjust开关。dbGroupIndex = " + key
                                    + ", name = " + name + ", value = " + oldValue);
                        }
                    }
                } catch (SQLException e) {
                    log.error("取范围过程中--查询出错！" + key + ": " + name, e);
                    continue;
                } finally {
                    closeDbResource(rs, stmt, conn);
                }

                try {
                    conn = dataSource.getConnection();
                    stmt = conn.prepareStatement(getUpdateSql());
                    stmt.setLong(1, newValue);
                    stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                    stmt.setString(3, name);
                    stmt.setLong(4, oldValue);
                    int affectedRows = stmt.executeUpdate();
                    if (affectedRows == 0) {
                        continue;
                    }
                } catch (SQLException e) {
                    log.error("取范围过程中--更新出错！" + key + ":" + name, e);
                    continue;
                } finally {
                    closeDbResource(null, stmt, conn);
                }

                SequenceRange sequenceRange = new SequenceRange(newValue + 1, newValue + innerStep);
                log.debug("get new range, sequence name is: {}, range info: [{}]", name, sequenceRange);
                return sequenceRange;
            }
        }
        log.error("所有数据源都不可用！且重试" + this.retryTimes + "次后，仍然失败!");
        throw new SequenceException(
                "All dataSource failed to get value, retried too many times, retryTimes = " + retryTimes);
    }

    private String getInsertSql() {
        if (insertSql == null) {
            synchronized (this) {
                if (insertSql == null) {
                    insertSql = "insert into " + getTableName() + "(" + getNameColumnName() + "," + getValueColumnName()
                            + "," + getGmtCreatedColumnName() + "," + getGmtModifiedColumnName() + ") values(?,?,?,?);";
                }
            }
        }
        return insertSql;
    }

    private String getSelectSql() {
        if (selectSql == null) {
            synchronized (this) {
                if (selectSql == null) {
                    selectSql = "select " + getValueColumnName() + " from " + getTableName() + " where "
                            + getNameColumnName() + " = ?";
                }
            }
        }
        return selectSql;
    }

    private String getUpdateSql() {
        if (updateSql == null) {
            synchronized (this) {
                if (updateSql == null) {
                    updateSql = "update " + getTableName() + " set " + getValueColumnName() + " = ?, "
                            + getGmtModifiedColumnName() + " = ? where " + getNameColumnName() + " = ? and "
                            + getValueColumnName() + " = ?";
                }
            }
        }
        return updateSql;
    }

    private static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.warn("Could not close JDBC ResultSet", e);
            } catch (Throwable e) {
                log.warn("Unexpected exception on closing JDBC ResultSet", e);
            }
        }
    }

    private static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.warn("Could not close JDBC Statement", e);
            } catch (Throwable e) {
                log.warn("Unexpected exception on closing JDBC Statement", e);
            }
        }
    }

    private static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.warn("Could not close JDBC Connection", e);
            } catch (Throwable e) {
                log.warn("Unexpected exception on closing JDBC Connection", e);
            }
        }
    }

    private static void closeDbResource(ResultSet rs, Statement stmt, Connection conn) {
        closeResultSet(rs);
        closeStatement(stmt);
        closeConnection(conn);
    }
}
