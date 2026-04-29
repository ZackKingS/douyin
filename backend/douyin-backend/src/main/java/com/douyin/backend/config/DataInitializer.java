package com.douyin.backend.config;

import com.douyin.backend.entity.User;
import com.douyin.backend.entity.Video;
import com.douyin.backend.repository.UserRepository;
import com.douyin.backend.repository.VideoRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, VideoRepository videoRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.videoRepository = videoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        User user1 = new User();
        user1.setUsername("zhangsan");
        user1.setNickname("张三");
        user1.setPasswordHash(passwordEncoder.encode("Abc123456"));
        user1.setPhone("13800138000");
        user1.setSignature("记录搞笑日常");
        user1.setAvatar("https://dummyimage.com/200x200/222/fff&text=ZS");
        user1.setFansCount(1200L);
        user1.setFollowCount(120L);
        user1.setLikeCount(5600L);
        user1.setVideoCount(2L);

        User user2 = new User();
        user2.setUsername("lisi");
        user2.setNickname("李四");
        user2.setPasswordHash(passwordEncoder.encode("Abc123456"));
        user2.setPhone("13800138001");
        user2.setSignature("旅行和探店");
        user2.setAvatar("https://dummyimage.com/200x200/444/fff&text=LS");
        user2.setFansCount(860L);
        user2.setFollowCount(98L);
        user2.setLikeCount(3200L);
        user2.setVideoCount(1L);

        userRepository.saveAll(List.of(user1, user2));

        Video video1 = new Video();
        video1.setVideoId("v_demo001");
        video1.setAuthorId(user1.getId());
        video1.setTitle("今日份快乐喷泉");
        video1.setDescription("#搞笑 #日常 今天笑到停不下来");
        video1.setVideoUrl("https://samplelib.com/lib/preview/mp4/sample-5s.mp4");
        video1.setCoverUrl("https://dummyimage.com/720x1280/111/fff&text=video1");
        video1.setTopicIds("搞笑,日常");
        video1.setMusicId("m_123");
        video1.setLikeCount(5000L);
        video1.setCommentCount(12L);
        video1.setShareCount(34L);
        video1.setViewCount(50000L);
        video1.setLocation("北京");

        Video video2 = new Video();
        video2.setVideoId("v_demo002");
        video2.setAuthorId(user2.getId());
        video2.setTitle("周末探店合集");
        video2.setDescription("#探店 #旅行");
        video2.setVideoUrl("https://samplelib.com/lib/preview/mp4/sample-10s.mp4");
        video2.setCoverUrl("https://dummyimage.com/720x1280/333/fff&text=video2");
        video2.setTopicIds("探店,旅行");
        video2.setLikeCount(1800L);
        video2.setCommentCount(6L);
        video2.setShareCount(18L);
        video2.setViewCount(16000L);
        video2.setLocation("上海");

        videoRepository.saveAll(List.of(video1, video2));
    }
}
