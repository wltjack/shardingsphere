/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sharding.merge.dal.show;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereConstraint;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Merged result for show create table.
 */
public final class ShowCreateTableMergedResult extends LogicTablesMergedResult {
    
    public ShowCreateTableMergedResult(final ShardingRule shardingRule,
                                       final SQLStatementContext sqlStatementContext, final ShardingSphereSchema schema, final List<QueryResult> queryResults) throws SQLException {
        super(shardingRule, sqlStatementContext, schema, queryResults);
    }
    
    @Override
    protected void setCellValue(final MemoryQueryResultRow memoryResultSetRow, final String logicTableName, final String actualTableName,
                                final ShardingSphereTable table, final ShardingRule shardingRule) {
        memoryResultSetRow.setCell(2, memoryResultSetRow.getCell(2).toString().replaceFirst(actualTableName, logicTableName));
        for (ShardingSphereIndex each : table.getIndexes()) {
            String actualIndexName = IndexMetaDataUtils.getActualIndexName(each.getName(), actualTableName);
            memoryResultSetRow.setCell(2, memoryResultSetRow.getCell(2).toString().replace(actualIndexName, each.getName()));
        }
        for (ShardingSphereConstraint each : table.getConstraints()) {
            String actualIndexName = IndexMetaDataUtils.getActualIndexName(each.getName(), actualTableName);
            memoryResultSetRow.setCell(2, memoryResultSetRow.getCell(2).toString().replace(actualIndexName, each.getName()));
            Optional<TableRule> tableRule = shardingRule.findTableRule(each.getReferencedTableName());
            if (!tableRule.isPresent()) {
                continue;
            }
            for (DataNode dataNode : tableRule.get().getActualDataNodes()) {
                memoryResultSetRow.setCell(2, memoryResultSetRow.getCell(2).toString().replace(dataNode.getTableName(), each.getReferencedTableName()));
            }
        }
    }
}
