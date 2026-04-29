package com.douyin.backend.dto.upload;

import jakarta.validation.constraints.NotBlank;

public class UploadTokenRequest {

    @NotBlank(message = "filename不能为空")
    private String filename;
    @NotBlank(message = "contentType不能为空")
    private String contentType;
    private long fileSize;
    @NotBlank(message = "type不能为空")
    private String type;

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
