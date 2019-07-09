package com.app.amber.chat.pojo;

public class files {
    String fileName;
    String filePath;
    String fileSize;
    String fileType;
    String lastModidfied;

    public String getMain_type() {
        return main_type;
    }

    public void setMain_type(String main_type) {
        this.main_type = main_type;
    }

    String main_type;
    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return this.fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileSize() {
        return this.fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getLastModidfied() {
        return this.lastModidfied;
    }

    public void setLastModidfied(String lastModidfied) {
        this.lastModidfied = lastModidfied;
    }

    public files(String filePath, String fileName, String fileType, String fileSize, String lastModidfied,String main_type) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.lastModidfied = lastModidfied;
        this.main_type=main_type;
    }
}
