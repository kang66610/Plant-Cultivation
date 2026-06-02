package com.plantcultivation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantcultivation.entity.Post;
import com.plantcultivation.entity.PostComment;
import com.plantcultivation.entity.User;
import com.plantcultivation.service.AuthService;
import com.plantcultivation.service.PostService;
import com.plantcultivation.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final AuthService authService;

    @GetMapping
    public ResultVO<Page<Post>> listPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false) String keyword,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        String currentUser = resolveAccount(auth);
        Page<Post> result = postService.listPosts(page, size, categorySlug, keyword, currentUser);
        return ResultVO.success(result);
    }

    @PostMapping
    public ResultVO<Post> createPost(@RequestHeader("Authorization") String auth,
                                      @RequestBody Post post) {
        try {
            User user = authService.getUserByToken(auth.substring(7));
            if (user == null) return ResultVO.error(401, "请先登录");
            post.setUserAccount(user.getAccount());
            Post created = postService.createPost(post);
            return ResultVO.success(created);
        } catch (Exception e) {
            return ResultVO.error(401, "请先登录");
        }
    }

    @DeleteMapping("/{postId}")
    public ResultVO<Void> deletePost(@RequestHeader("Authorization") String auth,
                                      @PathVariable Long postId) {
        try {
            User user = authService.getUserByToken(auth.substring(7));
            postService.deletePost(postId, user.getAccount());
            return ResultVO.success();
        } catch (RuntimeException e) {
            if ("UNAUTHORIZED".equals(e.getMessage())) {
                return ResultVO.error(403, "无权删除");
            }
            return ResultVO.error(500, "删除失败");
        } catch (Exception e) {
            return ResultVO.error(401, "请先登录");
        }
    }

    @PostMapping("/{postId}/like")
    public ResultVO<Map<String, Boolean>> toggleLike(@RequestHeader("Authorization") String auth,
                                                      @PathVariable Long postId) {
        try {
            User user = authService.getUserByToken(auth.substring(7));
            boolean liked = postService.toggleLike(postId, user.getAccount());
            return ResultVO.success(Map.of("liked", liked));
        } catch (Exception e) {
            return ResultVO.error(401, "请先登录");
        }
    }

    @GetMapping("/{postId}/comments")
    public ResultVO<Page<PostComment>> listComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<PostComment> result = postService.listComments(postId, page, size);
        return ResultVO.success(result);
    }

    @PostMapping("/{postId}/comments")
    public ResultVO<PostComment> addComment(@RequestHeader("Authorization") String auth,
                                             @PathVariable Long postId,
                                             @RequestBody Map<String, String> body) {
        try {
            User user = authService.getUserByToken(auth.substring(7));
            String content = body.get("content");
            if (content == null || content.isBlank()) {
                return ResultVO.error(400, "评论内容不能为空");
            }
            PostComment comment = postService.addComment(postId, user.getAccount(), content);
            return ResultVO.success(comment);
        } catch (Exception e) {
            return ResultVO.error(401, "请先登录");
        }
    }

    private String resolveAccount(String auth) {
        if (auth != null && auth.startsWith("Bearer ")) {
            try {
                User user = authService.getUserByToken(auth.substring(7));
                return user != null ? user.getAccount() : null;
            } catch (Exception ignored) {}
        }
        return null;
    }
}
