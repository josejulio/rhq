/*
 * RHQ Management Platform
 * Copyright (C) 2005-2010 Red Hat, Inc.
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
package org.rhq.enterprise.gui.coregui.server.gwt;

import org.rhq.core.domain.auth.Subject;
import org.rhq.core.domain.criteria.SubjectCriteria;
import org.rhq.core.domain.util.PageList;
import org.rhq.enterprise.gui.coregui.client.gwt.SubjectGWTService;
import org.rhq.enterprise.gui.coregui.server.util.SerialUtility;
import org.rhq.enterprise.server.auth.SubjectManagerLocal;
import org.rhq.enterprise.server.exception.LoginException;
import org.rhq.enterprise.server.util.LookupUtil;


/**
 * @author Greg Hinkle
 */
public class SubjectGWTServiceImpl extends AbstractGWTServiceImpl implements SubjectGWTService {

    private SubjectManagerLocal subjectManager = LookupUtil.getSubjectManager();

    public void changePassword(String username, String password) {
        subjectManager.changePassword(getSessionSubject(), username, password);
    }

    public void createPrincipal(String username, String password) {
        subjectManager.createPrincipal(getSessionSubject(), username, password);
    }

    public Subject createSubject(Subject subjectToCreate) {
        return SerialUtility.prepare(subjectManager.createSubject(getSessionSubject(), subjectToCreate),
                "SubjectManager.createSubject");
    }

    public void deleteSubjects(int[] subjectIds) {
        subjectManager.deleteSubjects(getSessionSubject(),subjectIds);
    }

    public Subject login(String username, String password) {
        try {
            return SerialUtility.prepare(subjectManager.login(username, password), "SubjectManager.login");
        } catch (LoginException e) {
            throw new RuntimeException("LoginException: " + e.getMessage());
        }
    }

    public void logout(Subject subject) {
        subjectManager.logout(subject.getSessionId());
    }

    public Subject updateSubject(Subject subjectToModify) {
        return SerialUtility.prepare(subjectManager.updateSubject(getSessionSubject(), subjectToModify),
                "SubjectManager.updateSubject");
    }

    public PageList<Subject> findSubjectsByCriteria(SubjectCriteria criteria) {
        return SerialUtility.prepare(subjectManager.findSubjectsByCriteria(getSessionSubject(), criteria), "SubjectManager.findSubjectsByCriteria");
    }
}
