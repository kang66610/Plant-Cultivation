package com.plantcultivation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantcultivation.entity.Plant;
import com.plantcultivation.entity.Post;
import com.plantcultivation.entity.PostComment;
import com.plantcultivation.entity.PostLike;
import com.plantcultivation.entity.User;
import com.plantcultivation.mapper.PlantMapper;
import com.plantcultivation.mapper.PostCommentMapper;
import com.plantcultivation.mapper.PostLikeMapper;
import com.plantcultivation.mapper.PostMapper;
import com.plantcultivation.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostCommentMapper postCommentMapper;
    private final UserMapper userMapper;
    private final PlantMapper plantMapper;

    public Page<Post> listPosts(int page, int size, String categorySlug, String keyword, String currentUser) {
        Page<Post> pageObj = new Page<>(page, size);
        QueryWrapper<Post> qw = new QueryWrapper<>();
        if (categorySlug != null && !categorySlug.isBlank()) {
            qw.eq("category_slug", categorySlug);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like("content", keyword)
                    .or().like("plant_slug", keyword));
        }
        qw.orderByDesc("created_at");
        Page<Post> result = postMapper.selectPage(pageObj, qw);
        enrichPosts(result.getRecords(), currentUser);
        return result;
    }

    @Transactional
    public Post createPost(Post post) {
        post.setLikeCount(0);
        post.setCommentCount(0);
        postMapper.insert(post);

        // 批量查询用户信息
        List<User> users = userMapper.selectByAccounts(List.of(post.getUserAccount()));
        if (!users.isEmpty()) {
            post.setUsername(users.get(0).getUsername());
            post.setAvatarUrl(users.get(0).getAvatarUrl());
        }
        if (post.getPlantSlug() != null && !post.getPlantSlug().isBlank()) {
            QueryWrapper<Plant> pqw = new QueryWrapper<>();
            pqw.eq("slug", post.getPlantSlug());
            Plant plant = plantMapper.selectOne(pqw);
            if (plant != null) {
                post.setPlantName(plant.getCommonName());
            }
        }
        post.setLiked(false);
        return post;
    }

    @Transactional
    public void deletePost(Long postId, String account) {
        Post post = postMapper.selectById(postId);
        if (post == null || !post.getUserAccount().equals(account)) {
            throw new RuntimeException("UNAUTHORIZED");
        }
        postMapper.deleteById(postId);
    }

    @Transactional
    public boolean toggleLike(Long postId, String account) {
        QueryWrapper<PostLike> qw = new QueryWrapper<>();
        qw.eq("post_id", postId).eq("user_account", account);
        PostLike existing = postLikeMapper.selectOne(qw);
        Post post = postMapper.selectById(postId);
        if (post == null) return false;

        if (existing != null) {
            postLikeMapper.delete(qw);
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            postMapper.updateById(post);
            return false;
        } else {
            PostLike like = new PostLike();
            like.setPostId(postId);
            like.setUserAccount(account);
            postLikeMapper.insert(like);
            post.setLikeCount(post.getLikeCount() + 1);
            postMapper.updateById(post);
            return true;
        }
    }

    @Transactional
    public PostComment addComment(Long postId, String account, String content) {
        PostComment comment = new PostComment();
        comment.setPostId(postId);
        comment.setUserAccount(account);
        comment.setContent(content);
        postCommentMapper.insert(comment);

        Post post = postMapper.selectById(postId);
        if (post != null) {
            post.setCommentCount(post.getCommentCount() + 1);
            postMapper.updateById(post);
        }

        // 批量查询用户信息
        List<User> users = userMapper.selectByAccounts(List.of(account));
        if (!users.isEmpty()) {
            comment.setUsername(users.get(0).getUsername());
            comment.setAvatarUrl(users.get(0).getAvatarUrl());
        }
        return comment;
    }

    public Page<PostComment> listComments(Long postId, int page, int size) {
        Page<PostComment> pageObj = new Page<>(page, size);
        QueryWrapper<PostComment> qw = new QueryWrapper<>();
        qw.eq("post_id", postId).orderByAsc("created_at");
        Page<PostComment> result = postCommentMapper.selectPage(pageObj, qw);

        // 批量查询所有评论者的用户信息
        List<String> accounts = result.getRecords().stream()
                .map(PostComment::getUserAccount)
                .distinct()
                .collect(Collectors.toList());
        if (!accounts.isEmpty()) {
            Map<String, User> userMap = userMapper.selectByAccounts(accounts).stream()
                    .collect(Collectors.toMap(User::getAccount, u -> u));
            for (PostComment c : result.getRecords()) {
                User user = userMap.get(c.getUserAccount());
                if (user != null) {
                    c.setUsername(user.getUsername());
                    c.setAvatarUrl(user.getAvatarUrl());
                }
            }
        }
        return result;
    }

    private void enrichPosts(List<Post> posts, String currentUser) {
        // 批量查询所有发帖者的用户信息
        List<String> accounts = posts.stream()
                .map(Post::getUserAccount)
                .distinct()
                .collect(Collectors.toList());
        Map<String, User> userMap = Collections.emptyMap();
        if (!accounts.isEmpty()) {
            userMap = userMapper.selectByAccounts(accounts).stream()
                    .collect(Collectors.toMap(User::getAccount, u -> u));
        }

        // 批量查询所有关联植物
        List<String> plantSlugs = posts.stream()
                .map(Post::getPlantSlug)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .collect(Collectors.toList());
        Map<String, Plant> plantMap = Collections.emptyMap();
        if (!plantSlugs.isEmpty()) {
            QueryWrapper<Plant> pqw = new QueryWrapper<>();
            pqw.in("slug", plantSlugs);
            plantMap = plantMapper.selectList(pqw).stream()
                    .collect(Collectors.toMap(Plant::getSlug, p -> p));
        }

        // 批量查询当前用户的点赞状态
        Set<Long> likedPostIds = Collections.emptySet();
        if (currentUser != null && !posts.isEmpty()) {
            List<Long> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());
            QueryWrapper<PostLike> lqw = new QueryWrapper<>();
            lqw.eq("user_account", currentUser).in("post_id", postIds);
            likedPostIds = postLikeMapper.selectList(lqw).stream()
                    .map(PostLike::getPostId)
                    .collect(Collectors.toSet());
        }

        for (Post p : posts) {
            User user = userMap.get(p.getUserAccount());
            if (user != null) {
                p.setUsername(user.getUsername());
                p.setAvatarUrl(user.getAvatarUrl());
            }
            if (p.getPlantSlug() != null && !p.getPlantSlug().isBlank()) {
                Plant plant = plantMap.get(p.getPlantSlug());
                if (plant != null) {
                    p.setPlantName(plant.getCommonName());
                }
            }
            p.setLiked(likedPostIds.contains(p.getId()));
        }
    }
}
