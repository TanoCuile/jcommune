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
package org.jtalks.jcommune.service.transactional;

import org.jtalks.common.model.dao.ChildRepository;
import org.jtalks.jcommune.model.dao.BranchDao;
import org.jtalks.jcommune.model.dao.TopicDao;
import org.jtalks.jcommune.model.entity.Branch;
import org.jtalks.jcommune.model.entity.CodeReview;
import org.jtalks.jcommune.model.entity.JCUser;
import org.jtalks.jcommune.model.entity.Topic;
import org.jtalks.jcommune.service.UserService;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Evgeniy Naumenko
 */
public class TransactionalSubscriptionServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private BranchDao branchDao;
    @Mock
    private TopicDao topicDao;
    @Mock
    private ChildRepository<CodeReview> codeReviewDao;

    private TransactionalSubscriptionService service;

    JCUser user = new JCUser("username", "email", "password");
    Branch branch;
    Topic topic;
    CodeReview codeReview;

    @BeforeMethod
    public void setUp() {
        initMocks(this);
        service = new TransactionalSubscriptionService(userService, branchDao, topicDao, codeReviewDao);
        branch = new Branch("name", "description");
        topic = new Topic(user, "title");
        codeReview = new CodeReview();
        topic.setCodeReview(codeReview);
        codeReview.setTopic(topic);
        when(userService.getCurrentUser()).thenReturn(user);
    }

    @Test
    public void testTopicSubscription() {
        service.toggleTopicSubscription(topic);

        assertTrue(topic.getSubscribers().contains(user));
        verify(topicDao).update(topic);
    }

    @Test
    public void testBranchSubscription() {
        service.toggleBranchSubscription(branch);

        assertTrue(branch.getSubscribers().contains(user));
    }

    @Test
    public void testTopicUnsubscription() {
        topic.getSubscribers().add(user);

        service.toggleTopicSubscription(topic);

        assertFalse(topic.getSubscribers().contains(user));
        verify(topicDao).update(topic);
    }

    @Test
    public void testBranchUnsubscription() {
        branch.getSubscribers().add(user);

        service.toggleBranchSubscription(branch);

        assertFalse(branch.getSubscribers().contains(user));
    }

    @Test
    public void testToggleSubscriptionCodeReviewSubscribeCase() {
        service.toggleSubscription(codeReview);
        assertTrue(codeReview.getSubscribers().contains(user));
        verify(codeReviewDao).update(codeReview);
    }

    @Test
    public void testToggleSubscriptionCodeReviewUnsubscribeCase() {
        codeReview.getSubscribers().add(user);
        service.toggleSubscription(codeReview);
        assertFalse(codeReview.getSubscribers().contains(user));
        verify(codeReviewDao).update(codeReview);
    }
}
