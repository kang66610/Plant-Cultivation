package com.plantcultivation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantcultivation.entity.Post;
import com.plantcultivation.entity.PostComment;
import com.plantcultivation.exception.BusinessException;
import com.plantcultivation.service.PostService;
import com.plantcultivation.util.SecurityUtil;
import com.plantcultivation.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResultVO<Page<Post>> listPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false) String keyword) {
        page = Math.max(page, 1);
        size = Math.min(Math.max(size, 1), 100);
        String currentUser = SecurityUtil.currentAccount();
        Page<Post> result = postService.listPosts(page, size, categorySlug, keyword, currentUser);
        return ResultVO.success(result);
    }

    @PostMapping
    public ResultVO<Post> createPost(@RequestBody Post post) {
        String account = requireLogin();
        post.setUserAccount(account);
        return ResultVO.success(postService.createPost(post));
    }

    @DeleteMapping("/{postId}")
    public ResultVO<Void> deletePost(@PathVariable Long postId) {
        String account = requireLogin();
        try {
            postService.deletePost(postId, account);
        } catch (BusinessException e) {
            return ResultVO.error(e.getStatus(), e.getMessage());
        }
        return ResultVO.success();
    }

    @PostMapping("/{postId}/like")
    public ResultVO<Map<String, Boolean>> toggleLike(@PathVariable Long postId) {
        String account = requireLogin();
        boolean liked = postService.toggleLike(postId, account);
        return ResultVO.success(Map.of("liked", liked));
    }

    @GetMapping("/{postId}/comments")
    public ResultVO<Page<PostComment>> listComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        page = Math.max(page, 1);
        size = Math.min(Math.max(size, 1), 100);
        Page<PostComment> result = postService.listComments(postId, page, size);
        return ResultVO.success(result);
    }

    @PostMapping("/{postId}/comments")
    public ResultVO<PostComment> addComment(@PathVariable Long postId,
                                             @RequestBody Map<String, String> body) {
        String account = requireLogin();
        String content = body.get("content");
        if (content == null || content.isBlank()) {
            return ResultVO.error(400, "评论内容不能为空");
        }
        PostComment comment = postService.addComment(postId, account, content);
        return ResultVO.success(comment);
    }

    private String requireLogin() {
        String account = SecurityUtil.currentAccount();
        if (account == null) {
            throw new BusinessException("请先登录", 401);
        }
        return account;
    }
}
