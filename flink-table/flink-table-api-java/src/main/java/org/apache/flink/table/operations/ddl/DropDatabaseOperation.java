/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.table.operations.ddl;

import org.apache.flink.table.api.TableException;
import org.apache.flink.table.api.ValidationException;
import org.apache.flink.table.api.internal.TableResultImpl;
import org.apache.flink.table.api.internal.TableResultInternal;
import org.apache.flink.table.catalog.Catalog;
import org.apache.flink.table.catalog.exceptions.DatabaseNotEmptyException;
import org.apache.flink.table.catalog.exceptions.DatabaseNotExistException;

/** Operation to describe a DROP DATABASE statement. */
public class DropDatabaseOperation implements DropOperation {
    private final String catalogName;
    private final String databaseName;
    private final boolean ifExists;
    private final boolean cascade;

    public DropDatabaseOperation(
            String catalogName, String databaseName, boolean ifExists, boolean cascade) {
        this.catalogName = catalogName;
        this.databaseName = databaseName;
        this.ifExists = ifExists;
        this.cascade = cascade;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public boolean isCascade() {
        return cascade;
    }

    public boolean isIfExists() {
        return ifExists;
    }

    @Override
    public String asSummaryString() {
        StringBuilder summaryString = new StringBuilder("DROP DATABASE");
        summaryString.append(ifExists ? " IF EXISTS " : "");
        summaryString.append(" " + catalogName + "." + databaseName);
        summaryString.append(cascade ? " CASCADE" : " RESTRICT");
        return summaryString.toString();
    }

    @Override
    public TableResultInternal execute(Context ctx) {
        Catalog catalog = ctx.getCatalogManager().getCatalogOrThrowException(getCatalogName());
        try {
            catalog.dropDatabase(getDatabaseName(), isIfExists(), isCascade());
            return TableResultImpl.TABLE_RESULT_OK;
        } catch (DatabaseNotExistException | DatabaseNotEmptyException e) {
            throw new ValidationException(
                    String.format("Could not execute %s", asSummaryString()), e);
        } catch (Exception e) {
            throw new TableException(String.format("Could not execute %s", asSummaryString()), e);
        }
    }
}
