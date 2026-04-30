package com.douyin.backend.repository;

import com.douyin.backend.entity.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByVideoIdAndStatusOrderByCreateTimeDesc(Long videoId, Integer status);
    List<Comment> findByParentIdAndStatusOrderByCreateTimeAsc(Long parentId, Integer status);
    long countByVideoIdAndStatus(Long videoId, Integer status);
    void deleteByVideoId(Long videoId);
}
