package com.doan2025.webtoeic.service;

public interface ReaderService {
    String readContentOfFile(String publicId);

    String readDocxFromCloud(String publicId);

    String readPdfFromCloud(String publicId);

}
