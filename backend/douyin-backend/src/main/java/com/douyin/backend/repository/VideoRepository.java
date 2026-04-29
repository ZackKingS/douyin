package com.douyin.backend.repository;

import com.douyin.backend.entity.Video;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Video, Long> {
    Optional<Video> findByVideoId(String videoId);
    List<Video> findByStatusOrderByCreateTimeDesc(Integer status, Pageable pageable);
    List<Video> findByAuthorIdOrderByCreateTimeDesc(Long authorId, Pageable pageable);
    List<Video> findByAuthorIdOrderByCreateTimeDesc(Long authorId);
    List<Video> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByCreateTimeDesc(
        String titleKeyword,
        String descriptionKeyword,
        Pageable pageable
    );
    long countByAuthorId(Long authorId);
}
