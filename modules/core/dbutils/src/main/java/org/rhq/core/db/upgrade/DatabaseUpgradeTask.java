/*
 * RHQ Management Platform
 * Copyright (C) 2005-2008 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.rhq.core.db.upgrade;

import java.sql.Connection;
import java.sql.SQLException;

import org.rhq.core.db.DatabaseType;
import org.rhq.core.db.ant.dbupgrade.SST_JavaTask;

/**
 * Implement this interface if you wish to write database upgrade steps as {@link SST_JavaTask}s. 
 * 
 * @author Joseph Marques
 */
public interface DatabaseUpgradeTask {
    public void execute(DatabaseType type, Connection connection) throws SQLException;
}
