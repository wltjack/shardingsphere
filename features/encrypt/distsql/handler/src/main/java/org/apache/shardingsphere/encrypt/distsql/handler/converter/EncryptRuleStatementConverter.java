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

package org.apache.shardingsphere.encrypt.distsql.handler.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.parser.segment.EncryptColumnSegment;
import org.apache.shardingsphere.encrypt.distsql.parser.segment.EncryptRuleSegment;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Encrypt rule statement converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EncryptRuleStatementConverter {
    
    /**
     * Convert encrypt rule segments to encrypt rule configuration.
     *
     * @param ruleSegments encrypt rule segments
     * @return encrypt rule configuration
     */
    public static EncryptRuleConfiguration convert(final Collection<EncryptRuleSegment> ruleSegments) {
        Collection<EncryptTableRuleConfiguration> tables = new LinkedList<>();
        Map<String, AlgorithmConfiguration> encryptors = new HashMap<>();
        for (EncryptRuleSegment each : ruleSegments) {
            tables.add(createEncryptTableRuleConfiguration(each));
            encryptors.putAll(createEncryptorConfigurations(each));
        }
        return new EncryptRuleConfiguration(tables, encryptors);
    }
    
    private static EncryptTableRuleConfiguration createEncryptTableRuleConfiguration(final EncryptRuleSegment ruleSegment) {
        Collection<EncryptColumnRuleConfiguration> columns = new LinkedList<>();
        for (EncryptColumnSegment each : ruleSegment.getColumns()) {
            columns.add(createEncryptColumnRuleConfiguration(ruleSegment.getTableName(), each));
        }
        return new EncryptTableRuleConfiguration(ruleSegment.getTableName(), columns);
    }
    
    private static EncryptColumnRuleConfiguration createEncryptColumnRuleConfiguration(final String tableName, final EncryptColumnSegment columnSegment) {
        EncryptColumnItemRuleConfiguration cipherColumnConfig = new EncryptColumnItemRuleConfiguration(columnSegment.getCipherColumn(), getEncryptorName(tableName, columnSegment.getName()));
        EncryptColumnRuleConfiguration result = new EncryptColumnRuleConfiguration(columnSegment.getName(), cipherColumnConfig);
        String assistedQueryEncryptorName = null == columnSegment.getAssistedQueryEncryptor() ? null : getAssistedQueryEncryptorName(tableName, columnSegment.getName());
        EncryptColumnItemRuleConfiguration assistedQueryColumnConfig = new EncryptColumnItemRuleConfiguration(columnSegment.getAssistedQueryColumn(), assistedQueryEncryptorName);
        result.setAssistedQuery(assistedQueryColumnConfig);
        String likeQueryEncryptorName = null == columnSegment.getLikeQueryEncryptor() ? null : getLikeQueryEncryptorName(tableName, columnSegment.getName());
        EncryptColumnItemRuleConfiguration likeQueryColumnConfig = new EncryptColumnItemRuleConfiguration(columnSegment.getLikeQueryColumn(), likeQueryEncryptorName);
        result.setLikeQuery(likeQueryColumnConfig);
        return result;
    }
    
    private static Map<String, AlgorithmConfiguration> createEncryptorConfigurations(final EncryptRuleSegment ruleSegment) {
        Map<String, AlgorithmConfiguration> result = new HashMap<>(ruleSegment.getColumns().size(), 1);
        for (EncryptColumnSegment each : ruleSegment.getColumns()) {
            result.put(getEncryptorName(ruleSegment.getTableName(), each.getName()), createEncryptorConfiguration(each));
            if (null != each.getAssistedQueryEncryptor()) {
                result.put(getAssistedQueryEncryptorName(ruleSegment.getTableName(), each.getName()), createAssistedQueryEncryptorConfiguration(each));
            }
            if (null != each.getLikeQueryEncryptor()) {
                result.put(getLikeQueryEncryptorName(ruleSegment.getTableName(), each.getName()), createLikeQueryEncryptorConfiguration(each));
            }
        }
        return result;
    }
    
    private static AlgorithmConfiguration createEncryptorConfiguration(final EncryptColumnSegment columnSegment) {
        return new AlgorithmConfiguration(columnSegment.getEncryptor().getName(), columnSegment.getEncryptor().getProps());
    }
    
    private static AlgorithmConfiguration createAssistedQueryEncryptorConfiguration(final EncryptColumnSegment columnSegment) {
        return new AlgorithmConfiguration(columnSegment.getAssistedQueryEncryptor().getName(), columnSegment.getAssistedQueryEncryptor().getProps());
    }
    
    private static AlgorithmConfiguration createLikeQueryEncryptorConfiguration(final EncryptColumnSegment columnSegment) {
        return new AlgorithmConfiguration(columnSegment.getLikeQueryEncryptor().getName(), columnSegment.getLikeQueryEncryptor().getProps());
    }
    
    private static String getEncryptorName(final String tableName, final String columnName) {
        return String.format("%s_%s", tableName, columnName);
    }
    
    private static String getAssistedQueryEncryptorName(final String tableName, final String columnName) {
        return String.format("assist_%s_%s", tableName, columnName);
    }
    
    private static String getLikeQueryEncryptorName(final String tableName, final String columnName) {
        return String.format("like_%s_%s", tableName, columnName);
    }
}
