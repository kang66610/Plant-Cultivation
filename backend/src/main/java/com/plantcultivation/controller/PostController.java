package com.plantcultivation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantcultivation.dto.CreateCommentRequest;
import com.plantcultivation.dto.CreatePostRequest;
import com.plantcultivation.entity.Post;
import com.plantcultivation.entity.PostComment;
import com.plantcultivation.exception.BusinessException;
import com.plantcultivation.service.PostService;
import com.plantcultivation.util.SecurityUtil;
import com.plantcultivation.vo.PageResultVO;
import com.plantcultivation.vo.ResultVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResultVO<PageResultVO<Post>> listPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime cursorCreatedAt,
            @RequestParam(required = false) Long cursorId) {
        page = Math.max(page, 1);
        size = Math.min(Math.max(size, 1), 100);
        String currentUser = SecurityUtil.currentAccount();

        if (cursorCreatedAt != null && cursorId != null) {
            List<Post> records = postService.listPostsAfterCursor(
                    size + 1, categorySlug, keyword, currentUser, cursorCreatedAt, cursorId);
            boolean hasMore = records.size() > size;
            if (hasMore) {
                records = records.subList(0, size);
            }
            Post next = records.isEmpty() ? null : records.get(records.size() - 1);
            return ResultVO.success(PageResultVO.ofCursor(
                    records,
                    size,
                    hasMore,
                    next == null ? null : next.getId(),
                    next == null || next.getCreatedAt() == null ? null : next.getCreatedAt().toString()));
        }

        Page<Post> result = postService.listPosts(page, size, categorySlug, keyword, currentUser);
        return ResultVO.success(PageResultVO.of(
                result.getRecords(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize()));
    }

    @PostMapping
    public ResultVO<Post> createPost(@Valid @RequestBody CreatePostRequest body) {
        String account = requireLogin();
        Post post = new Post();
        post.setUserAccount(account);
        post.setContent(body.content());
        post.setImages(blankToNull(body.images()));
        post.setPlantSlug(blankToNull(body.plantSlug()));
        post.setCategorySlug(blankToNull(body.categorySlug()));
        return ResultVO.success(postService.createPost(post));
    }

    @DeleteMapping("/{postId}")
    public ResultVO<Void> deletePost(@PathVariable Long postId) {
        String account = requireLogin();
        postService.deletePost(postId, account);
        return ResultVO.success();
    }

    @PostMapping("/{postId}/like")
    public ResultVO<Map<String, Boolean>> toggleLike(@PathVariable Long postId) {
        String account = requireLogin();
        boolean liked = postService.toggleLike(postId, account);
        return ResultVO.success(Map.of("liked", liked));
    }

    @GetMapping("/{postId}/comments")
    public ResultVO<PageResultVO<PostComment>> listComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        page = Math.max(page, 1);
        size = Math.min(Math.max(size, 1), 100);
        Page<PostComment> result = postService.listComments(postId, page, size);
        return ResultVO.success(PageResultVO.of(
                result.getRecords(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize()));
    }

    @PostMapping("/{postId}/comments")
    public ResultVO<PostComment> addComment(@PathVariable Long postId,
                                            @Valid @RequestBody CreateCommentRequest body) {
        String account = requireLogin();
        PostComment comment = postService.addComment(postId, account, body.content());
        return ResultVO.success(comment);
    }

    private String requireLogin() {
        String account = SecurityUtil.currentAccount();
        if (account == null) {
            throw new BusinessException("请先登录", 401);
        }
        return account;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
