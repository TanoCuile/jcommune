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
package org.jtalks.jcommune.model.entity;

import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Evgeniy Naumenko
 */
public class PostTest {

    private Post post;

    @BeforeMethod
    public void init() {
        Topic topic = new Topic(null, "header");
        post = new Post(null, "content");
        topic.addPost(post);
    }

    @Test
    public void testGetPostIndex() {
        assertEquals(0, post.getPostIndexInTopic());
    }

    @Test
    public void testUpdatePostModificationDate() throws InterruptedException {
        post.updateModificationDate();
        DateTime prevDate = post.getModificationDate();
        Thread.sleep(25); // to catch the date difference

        post.updateModificationDate();

        assertTrue(post.getModificationDate().isAfter(prevDate));
    }
    
    @Test
    public void testGetLastTouchedDatePostWasNotModified() {
        DateTime createdDate = new DateTime();
        post.setCreationDate(createdDate);
        post.setModificationDate(null);
        
        assertEquals(post.getLastTouchedDate(), createdDate);
    }
    
    @Test
    public void testGetLastTouchedDatePostWasModified() {
        DateTime modifiedDate = new DateTime();
        post.setCreationDate(modifiedDate.minusDays(1));
        post.setModificationDate(modifiedDate);
        
        assertEquals(post.getLastTouchedDate(), modifiedDate);
    }
}
