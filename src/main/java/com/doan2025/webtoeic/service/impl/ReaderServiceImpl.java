package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.service.CloudService;
import com.doan2025.webtoeic.service.ReaderService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = {Exception.class, WebToeicException.class})
public class ReaderServiceImpl implements ReaderService {
    private final CloudService cloudService;

    @Override
    public String readContentOfFile(String url) {
        // 1. Lấy định dạng tệp từ Cloudinary (giả sử cloudService là dependency đã được inject)
        String format = getExtension(url);
        String publicId = cloudService.extractPublicId(url);

        // 2. Phân trường hợp dựa trên định dạng tệp
        switch (format.toLowerCase()) {
            case "docx":
                return readDocxFromCloud(publicId);
            case "pdf":
                return readPdfFromCloud(publicId);
            default:
                // Xử lý trường hợp không hỗ trợ định dạng
                throw new WebToeicException(
                        ResponseCode.UNSUPPORTED,
                        ResponseObject.FILE,
                        "Định dạng tệp không được hỗ trợ để đọc nội dung: " + format
                );
        }
    }

    private String getExtension(String url) {
        int dotIndex = url.lastIndexOf('.');
        if (dotIndex > 0) {
            return url.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * Hàm đọc nội dung văn bản từ một file DOCX (.docx) trên Cloudinary.
     *
     * @param publicId ID công khai của file DOCX trên Cloudinary.
     * @return Toàn bộ nội dung văn bản của file.
     */
    public String readDocxFromCloud(String publicId) {
        try (
                // 1. Lấy InputStream từ Cloudinary
                InputStream is = cloudService.getFileInputStream(publicId, "docx");
                // 2. Chuyển InputStream vào XWPFDocument
                XWPFDocument document = new XWPFDocument(is);
                // 3. Trích xuất văn bản
                XWPFWordExtractor extractor = new XWPFWordExtractor(document)
        ) {
            return extractor.getText();

        } catch (IOException e) {
            // Xử lý lỗi khi không thể tải hoặc đọc file
            System.err.println("Lỗi khi đọc file DOCX từ Cloudinary: " + e.getMessage());
            throw new WebToeicException(ResponseCode.CANNOT_READ, ResponseObject.FILE, "DOCX: " + e.getMessage());
        }
    }

    //-------------------------------------------------------------------------

    /**
     * Hàm đọc nội dung văn bản từ một file PDF (.pdf) trên Cloudinary.
     *
     * @param publicId ID công khai của file PDF trên Cloudinary.
     * @return Toàn bộ nội dung văn bản của file.
     */
    public String readPdfFromCloud(String publicId) {
        try (
                // 1. Lấy InputStream từ Cloudinary
                InputStream is = cloudService.getFileInputStream(publicId, "pdf");
                // 2. Chuyển InputStream vào PDDocument
                PDDocument document = PDDocument.load(is)
        ) {
            // 3. Trích xuất văn bản
            PDFTextStripper pdfStripper = new PDFTextStripper();

            return pdfStripper.getText(document);

        } catch (IOException e) {
            // Xử lý lỗi khi không thể tải hoặc đọc file
            System.err.println("Lỗi khi đọc file PDF từ Cloudinary: " + e.getMessage());
            throw new WebToeicException(ResponseCode.CANNOT_READ, ResponseObject.FILE, "PDF: " + e.getMessage());
        }
    }
}
