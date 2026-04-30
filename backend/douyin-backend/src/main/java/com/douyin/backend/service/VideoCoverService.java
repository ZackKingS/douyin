package com.douyin.backend.service;

import com.douyin.backend.config.AppProperties;
import com.douyin.backend.dto.upload.FileUploadResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VideoCoverService {

    private static final Logger log = LoggerFactory.getLogger(VideoCoverService.class);
    private static final int GENERATE_TIMEOUT_SECONDS = 20;

    private final AppProperties appProperties;
    private final FileStorageService fileStorageService;

    public VideoCoverService(AppProperties appProperties, FileStorageService fileStorageService) {
        this.appProperties = appProperties;
        this.fileStorageService = fileStorageService;
    }

    public String generateCoverUrl(Path videoPath, Long userId) {
        Path tempCover = null;
        try {
            tempCover = Files.createTempFile("douyin-cover-", ".jpg");
            List<String> command = List.of(
                appProperties.getFfmpegPath(),
                "-y",
                "-ss",
                String.valueOf(appProperties.getCoverFrameSecond()),
                "-i",
                videoPath.toString(),
                "-frames:v",
                "1",
                "-q:v",
                "2",
                tempCover.toString()
            );
            Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .start();
            boolean finished = process.waitFor(GENERATE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                log.warn("Timed out while generating cover for video: {}", videoPath);
                return null;
            }
            if (process.exitValue() != 0 || Files.size(tempCover) == 0) {
                log.warn("Failed to generate cover for video: {}, exitCode={}", videoPath, process.exitValue());
                return null;
            }
            FileUploadResponse storedCover = fileStorageService.storeGeneratedFile(tempCover, "cover", userId, "jpg");
            return storedCover.fileUrl();
        } catch (IOException ex) {
            log.warn("Unable to run ffmpeg for video cover generation: {}", ex.getMessage());
            return null;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while generating cover for video: {}", videoPath);
            return null;
        } finally {
            if (tempCover != null) {
                try {
                    Files.deleteIfExists(tempCover);
                } catch (IOException ex) {
                    log.debug("Failed to delete temporary cover file: {}", tempCover, ex);
                }
            }
        }
    }
}
