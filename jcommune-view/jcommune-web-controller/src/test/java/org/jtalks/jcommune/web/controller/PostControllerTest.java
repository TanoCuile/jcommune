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
package org.jtalks.jcommune.web.controller;

import org.jtalks.jcommune.model.entity.Branch;
import org.jtalks.jcommune.model.entity.CodeReview;
import org.jtalks.jcommune.model.entity.JCUser;
import org.jtalks.jcommune.model.entity.Post;
import org.jtalks.jcommune.model.entity.Topic;
import org.jtalks.jcommune.service.*;
import org.jtalks.jcommune.service.exceptions.NotFoundException;
import org.jtalks.jcommune.service.nontransactional.BBCodeService;
import org.jtalks.jcommune.web.dto.Breadcrumb;
import org.jtalks.jcommune.web.dto.PostDto;
import org.jtalks.jcommune.web.util.BreadcrumbBuilder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.ModelAndViewAssert.assertAndReturnModelAttributeOfType;
import static org.springframework.test.web.ModelAndViewAssert.assertModelAttributeAvailable;
import static org.springframework.test.web.ModelAndViewAssert.assertViewName;
import static org.testng.Assert.assertEquals;

/**
 * This is test for <code>PostController<code> class.
 * Test should cover view resolution and logic validation.
 *
 * @author Osadchuck Eugeny
 * @author Evgeniy Naumenko
 */
public class PostControllerTest {

    @Mock
    private PostService postService;
    @Mock
    private BBCodeService bbCodeService;
    @Mock
    private TopicModificationService topicModificationService;
    @Mock
    private TopicFetchService topicFetchService;
    @Mock
    private BreadcrumbBuilder breadcrumbBuilder;
    @Mock
    private LastReadPostService lastReadPostService;
    @Mock
    private UserService userService;

    public static final long POST_ID = 1;
    public static final long TOPIC_ID = 1L;
    private static final Long BRANCH_ID = 1L;
    public static final long PAGE = 1L;
    private final String POST_CONTENT = "postContent";
    private Post post;
    private JCUser user = new JCUser("username", "email@mail.com", "password");

    private PostController controller;

    @BeforeMethod
    public void init() throws NotFoundException {
        initMocks(this);

        Branch branch = new Branch("branch", "branch");
        branch.setId(BRANCH_ID);


        Topic topic = mock(Topic.class);
        when(topic.getBranch()).thenReturn(branch);
        when(topic.getId()).thenReturn(TOPIC_ID);
        when(topic.getTitle()).thenReturn("title");
        when(topic.getTopicStarter()).thenReturn(user);

        post = mock(Post.class);
        when(post.getId()).thenReturn(POST_ID);
        when(post.getUserCreated()).thenReturn(user);
        when(post.getPostContent()).thenReturn(POST_CONTENT);
        when(post.getTopic()).thenReturn(topic);
        List<Post> posts = new ArrayList<Post>();
        posts.add(post);
        when(topic.getPosts()).thenReturn(posts);

        when(postService.get(POST_ID)).thenReturn(post);

        when(topicFetchService.get(TOPIC_ID)).thenReturn(topic);

        when(breadcrumbBuilder.getForumBreadcrumb(topic)).thenReturn(new ArrayList<Breadcrumb>());

        controller = new PostController(
                postService, breadcrumbBuilder, topicFetchService, topicModificationService,
                bbCodeService, lastReadPostService, userService);
    }

    @Test
    public void testInitBinder() {
        WebDataBinder binder = mock(WebDataBinder.class);
        controller.initBinder(binder);
        verify(binder).registerCustomEditor(eq(String.class), any(StringTrimmerEditor.class));
    }

    @Test
    public void testDeletePost() throws NotFoundException {
        //invoke the object under test
        String view = controller.delete(POST_ID);

        //check expectations
        verify(postService).deletePost(post);

        //check result
        assertEquals(view, "redirect:/topics/" + TOPIC_ID);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testDeleteUnexistingPost() throws NotFoundException {
        doThrow(new NotFoundException()).when(postService).get(anyLong());
        controller.delete(POST_ID);
    }

    @Test
    public void editPost() throws NotFoundException {
        //invoke the object under test
        ModelAndView actualMav = controller.editPage(POST_ID);
        //check result
        this.assertEditPostFormMavIsCorrect(actualMav);

        PostDto dto = assertAndReturnModelAttributeOfType(actualMav, "postDto", PostDto.class);
        assertEquals(dto.getId(), TOPIC_ID);

        assertModelAttributeAvailable(actualMav, "breadcrumbList");
    }

    @Test
    public void testUpdatePost() throws NotFoundException {
        PostDto dto = getDto();
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "postDto");
        ModelAndView mav = controller.update(dto, bindingResult, POST_ID);
        assertViewName(mav, "redirect:/posts/" + dto.getId());
        verify(postService).updatePost(Matchers.<Post>any(), anyString());
    }

    @Test
    public void updateWithError() throws NotFoundException {
        PostDto dto = this.getDto();
        BeanPropertyBindingResult resultWithErrors = mock(BeanPropertyBindingResult.class);

        when(resultWithErrors.hasErrors()).thenReturn(true);

        ModelAndView mav = controller.update(dto, resultWithErrors, POST_ID);

        this.assertEditPostFormMavIsCorrect(mav);

        verify(postService, never()).updatePost(Matchers.<Post>any(), anyString());
    }

    @Test
    public void testAnswer() throws NotFoundException {
        //invoke the object under test
        ModelAndView mav = controller.addPost(TOPIC_ID);

        //check expectations
        verify(topicFetchService).get(TOPIC_ID);
        verify(breadcrumbBuilder).getForumBreadcrumb(Matchers.<Topic>any());

        //check result
        this.assertAnswerMavIsCorrect(mav);
    }
    
    @Test(expectedExceptions=AccessDeniedException.class)
    public void testAnswerForCodeReview() throws NotFoundException {
        Topic topic = new Topic();
        topic.setCodeReview(new CodeReview());        
        when(topicFetchService.get(TOPIC_ID)).thenReturn(topic);
        
        controller.addPost(TOPIC_ID);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testAnswerForUnexistingTopic() throws NotFoundException {
        doThrow(new NotFoundException()).when(topicFetchService).get(TOPIC_ID);
        controller.addPost(TOPIC_ID);
    }

    @Test
    public void testQuotedAnswer() throws NotFoundException {
        String expected = "[quote=\"user\"]" + POST_CONTENT + "[/quote]";
        when(bbCodeService.quote(POST_CONTENT, user)).thenReturn(expected);

        ModelAndView mav = controller.addPostWithQuote(post.getId(), null);
        //check expectations

        PostDto actual = assertAndReturnModelAttributeOfType(mav, "postDto", PostDto.class);
        assertEquals(actual.getBodyText(), expected);
    }
    
    @Test(expectedExceptions=AccessDeniedException.class)
    public void testQuotedAnswerForCodeReview() throws NotFoundException {
        Post post = mock(Post.class);
        Topic topic = new Topic();
        topic.setId(TOPIC_ID);
        topic.setCodeReview(new CodeReview());        
        
        when(topicFetchService.get(TOPIC_ID)).thenReturn(topic);
        when(postService.get(POST_ID)).thenReturn(post);
        when(post.getTopic()).thenReturn(topic);
        
        controller.addPostWithQuote(POST_ID, null);
    }

    @Test
    public void testPartialQuotedAnswer() throws NotFoundException {
        String selection = "selected content";
        String expected = "[quote=\"user\"]" + selection + "[/quote]";
        when(postService.get(anyLong())).thenReturn(post);
        when(bbCodeService.quote(selection, user)).thenReturn(expected);

        ModelAndView mav = controller.addPostWithQuote(TOPIC_ID, selection);
        //check expectations

        PostDto actual = assertAndReturnModelAttributeOfType(mav, "postDto", PostDto.class);
        assertEquals(actual.getBodyText(), expected);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testQuotedAnswerForUnexistingTopic() throws NotFoundException {
        doThrow(new NotFoundException()).when(postService).get(anyLong());
        controller.addPostWithQuote(TOPIC_ID, "");
    }

    @Test
    public void testSubmitAnswerValidationPass() throws NotFoundException {
        BeanPropertyBindingResult resultWithoutErrors = mock(BeanPropertyBindingResult.class);
        when(resultWithoutErrors.hasErrors()).thenReturn(false);
        when(topicModificationService.replyToTopic(anyLong(), Matchers.<String>any(), eq(BRANCH_ID))).thenReturn(post);
        when(postService.calculatePageForPost(post)).thenReturn(1);
        //invoke the object under test
        ModelAndView mav = controller.create(getDto(), resultWithoutErrors);

        //check expectations
        verify(topicModificationService).replyToTopic(TOPIC_ID, POST_CONTENT, BRANCH_ID);

        //check result
        assertViewName(mav, "redirect:/topics/" + TOPIC_ID + "?page=1#" + POST_ID);
    }

    @Test
    public void testSubmitAnswerValidationFail() throws NotFoundException {
        BeanPropertyBindingResult resultWithErrors = mock(BeanPropertyBindingResult.class);
        when(resultWithErrors.hasErrors()).thenReturn(true);
        //invoke the object under test
        ModelAndView mav = controller.create(getDto(), resultWithErrors);

        //check expectations
        verify(topicModificationService, never()).replyToTopic(anyLong(), anyString(), eq(BRANCH_ID));

        //check result
        assertViewName(mav, "answer");
    }

    @Test
    public void testRedirectToPageWithPost() throws NotFoundException {
        when(postService.calculatePageForPost(post)).thenReturn(5);

        String result = controller.redirectToPageWithPost(POST_ID);

        assertEquals(result, "redirect:/topics/" + TOPIC_ID + "?page=5#" + POST_ID);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testRedirectToPageWithPostNotFound() throws NotFoundException {
        doThrow(new NotFoundException()).when(postService).get(anyLong());

        controller.redirectToPageWithPost(POST_ID);
    }

/*    @Test
    public void testPreview() {
        String postText = "[code]123[/code]";
        String html = "<code>123</code>";
        when(bbCodeService.convertBbToHtml(anyString())).thenReturn(html);
        when(userService.getCurrentUser()).thenReturn(user);

        String result = controller.preview(postText).getBody();

        assertEquals(result, html);
        verify(bbCodeService).convertBbToHtml(postText);
    }*/

    private void assertAnswerMavIsCorrect(ModelAndView mav) {
        assertViewName(mav, "answer");
        assertAndReturnModelAttributeOfType(mav, "topic", Topic.class);
        long topicId = assertAndReturnModelAttributeOfType(mav, "topicId", Long.class);
        assertEquals(topicId, TOPIC_ID);
        PostDto dto = assertAndReturnModelAttributeOfType(mav, "postDto", PostDto.class);
        assertEquals(dto.getId(), 0);
        assertModelAttributeAvailable(mav, "breadcrumbList");
    }

    private void assertEditPostFormMavIsCorrect(ModelAndView mav) {
        assertViewName(mav, "editPost");
        long topicId = assertAndReturnModelAttributeOfType(mav, "topicId", Long.class);
        long postId = assertAndReturnModelAttributeOfType(mav, "postId", Long.class);
        assertEquals(topicId, TOPIC_ID);
        assertEquals(postId, POST_ID);
    }

    private PostDto getDto() {
        PostDto dto = new PostDto();
        dto.setId(POST_ID);
        dto.setBodyText(POST_CONTENT);
        dto.setTopicId(TOPIC_ID);
        return dto;
    }
}
