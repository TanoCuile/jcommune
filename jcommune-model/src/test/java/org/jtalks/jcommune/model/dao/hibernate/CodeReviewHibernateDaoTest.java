/**
 * Copyright (C) 2011  JTalks.org Team
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.jtalks.jcommune.model.dao.hibernate;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jtalks.common.model.dao.ChildRepository;
import org.jtalks.jcommune.model.PersistedObjectsFactory;
import org.jtalks.jcommune.model.entity.CodeReview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(locations = {"classpath:/org/jtalks/jcommune/model/entity/applicationContext-dao.xml"})
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class CodeReviewHibernateDaoTest extends AbstractTransactionalTestNGSpringContextTests {

    @Autowired
    private SessionFactory sessionFactory;
    @Autowired
    private ChildRepository<CodeReview> codeReviewDao;    
    private Session session;

    @BeforeMethod
    public void setUp() throws Exception {
        session = sessionFactory.getCurrentSession();
        PersistedObjectsFactory.setSession(session);
    }
    
    /*===== Common methods =====*/

    @Test
    public void testGet() {
        CodeReview review = PersistedObjectsFactory.getDefaultCodeReview();
        session.save(review);

        CodeReview result = codeReviewDao.get(review.getId());

        assertNotNull(result);
        assertEquals(result.getId(), review.getId());
        assertEquals(result.getComments().size(), 2);
    }


    @Test
    public void testGetInvalidId() {
        CodeReview result = codeReviewDao.get(-567890L);

        assertNull(result);
    }

    @Test
    public void testUpdate() {
        String newUuid = "1234-1231-1231";
        CodeReview review = PersistedObjectsFactory.getDefaultCodeReview();
        session.save(review);
        review.setUuid(newUuid);

        codeReviewDao.update(review);
        session.evict(review);
        CodeReview result = (CodeReview) session.get(CodeReview.class, review.getId());

        assertEquals(result.getUuid(), newUuid);
    }

    @Test(expectedExceptions = Exception.class)
    public void testUpdateNotNullViolation() {
        CodeReview review = PersistedObjectsFactory.getDefaultCodeReview();
        session.save(review);
        review.setUuid(null);

        codeReviewDao.update(review);
    }
    
    @Test
    public void testOrphanRemoving() {
        CodeReview review = PersistedObjectsFactory.getDefaultCodeReview();
        
        review.getComments().remove(0);
        codeReviewDao.update(review);
        session.evict(review);
        
        assertEquals(codeReviewDao.get(review.getId()).getComments().size(), 1);
    }

}
