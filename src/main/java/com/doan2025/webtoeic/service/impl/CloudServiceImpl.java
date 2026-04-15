package com.doan2025.webtoeic.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.doan2025.webtoeic.constants.Constants;
import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.dto.request.FileRequest;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.service.CloudService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(rollbackOn = {WebToeicException.class, Exception.class})
public class CloudServiceImpl implements CloudService {
    private final Cloudinary cloudinary;

    /**
     * Upload tệp lên Cloudinary và tự động xác định loại tệp
     * param file Tệp được gửi từ client
     *
     * @return URL của tệp đã upload
     */
    public String uploadFile(MultipartFile file) {
        try {
            // Lấy tên tệp và phần mở rộng
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new WebToeicException(ResponseCode.IS_NULL, ResponseObject.FILE);
            }
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

            // Xác định loại tệp từ phần mở rộng
            String fileType = Constants.EXTENSION_TO_TYPE.get(extension);
            if (fileType == null) {
                throw new WebToeicException(ResponseCode.UNSUPPORTED, ResponseObject.FILE);
            }

            // Xác định thư mục lưu trữ trên Cloudinary
            String folder = fileType + "s"; // images, videos, pdfs, docs
            String baseName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
            String timestampedFilename = baseName + "_" + System.currentTimeMillis() + "." + extension;

            // Cấu hình upload
            Map<String, Object> uploadOptions = new HashMap<>();
            uploadOptions.put(Constants.FOLDER, folder);
            uploadOptions.put(Constants.USE_FILENAME, true);
            uploadOptions.put(Constants.UNIQUE_FILENAME, false);
            uploadOptions.put(Constants.FILENAME_OVERRIDE, timestampedFilename);
            uploadOptions.put(Constants.TYPE, Constants.UPLOAD);
            uploadOptions.put(Constants.ACCESS_MODE, Constants.MODE_PUBLIC);

            // Cấu hình tối ưu hóa theo loại tệp
            if (fileType.equals(Constants.IMAGE) || fileType.equals(Constants.VIDEO)) {
                uploadOptions.put(Constants.QUALITY, Constants.AUTO);
                uploadOptions.put(Constants.FETCH_FORMAT, Constants.AUTO);
            }
            uploadOptions.put(Constants.RESOURCE_TYPE,
                    fileType.equals(Constants.IMAGE) ? Constants.IMAGE :
                            fileType.equals(Constants.VIDEO) ? Constants.VIDEO : Constants.RAW);

            // Upload tệp lên Cloudinary
            Map data = this.cloudinary.uploader().upload(file.getBytes(), uploadOptions);

            // Trả về URL của tệp
            return (String) data.get(Constants.URL);
        } catch (IOException io) {
            throw new WebToeicException(ResponseCode.CANNOT_UPLOAD, ResponseObject.FILE);
        }
    }

    /**
     * Xóa tệp trên Cloudinary dựa trên URL
     * param url của tệp trên Cloudinary
     * param fileType Loại tệp: image, video, pdf, doc
     *
     * @return Map chứa kết quả xóa
     */
    public Map deleteFile(FileRequest dto) {
        try {
            // Kiểm tra loại tệp hợp lệ
            if (!Constants.ALLOWED_EXTENSIONS.containsKey(dto.getFileType())) {
                throw new WebToeicException(ResponseCode.INVALID, ResponseObject.FILE);
            }

            // Trích xuất public_id từ URL
            String publicId = extractPublicId(dto.getUrl());
            // Xác định resource_type
            String resourceType = dto.getFileType().equals(Constants.IMAGE) ? Constants.IMAGE :
                    dto.getFileType().equals(Constants.VIDEO) ? Constants.VIDEO : Constants.RAW;
            // Xóa tệp theo public_id
            Map result = this.cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap(Constants.RESOURCE_TYPE, resourceType));
            return result;
        } catch (IOException e) {
            throw new WebToeicException(ResponseCode.CANNOT_DELETE, ResponseObject.FILE);
        }
    }

    public InputStream getFileInputStream(String publicId, String format) throws IOException {
        // Xây dựng URL công khai
        String urlString = this.cloudinary.url()
                .resourceType(Constants.RAW) // Hoặc "raw" tùy cấu hình upload ban đầu của bạn
                .type(Constants.UPLOAD)
                .format(format)
                .secure(true) // Đảm bảo dùng HTTPS
                .generate(publicId);

        // Mở kết nối và trả về InputStream
        URL url = new URL(urlString);
        return url.openStream();
    }

    public Double getVideoDuration(String url) {
        try {
            String publicId = extractPublicId(url);
            publicId = publicId.substring(0, publicId.lastIndexOf("."));
            Map res = cloudinary.api().resource(publicId, ObjectUtils.asMap(
                    "resource_type", "video", "media_metadata", true
            ));
            return (Double) res.get(Constants.DURATION);
        } catch (Exception e) {
            throw new WebToeicException(ResponseCode.UNSUPPORTED, ResponseObject.FILE);
        }
    }

    /**
     * Trích xuất public_id từ URL của tệp trên Cloudinary
     *
     * @param fileUrl URL của tệp
     * @return public_id của tệp
     */
    public String extractPublicId(String fileUrl) {
        fileUrl = fileUrl.replace("upload/", "upload/q_RAW,f_RAW/");
        String[] urlParts = fileUrl.split(Constants.REGEX_FILE_TYPE);
        if (urlParts.length > 1) {
            return urlParts[1].replaceAll(Constants.REGEX_FILE, "");
        }
        throw new WebToeicException(ResponseCode.INVALID, ResponseObject.URL);
    }

    public String getFileFormat(String publicId) {
        try {
            // Bằng cách chỉ định resource_type="raw", bạn buộc Cloudinary Admin API
            // tìm kiếm tài nguyên ở đúng vị trí (không phải trong thư mục "image").
            Map res = cloudinary.api().resource(extractPublicId(publicId), ObjectUtils.asMap(
                    Constants.RESOURCE_TYPE, Constants.RAW
            ));

            // 'format' là trường chứa định dạng tệp (ví dụ: "pdf", "docx")
            return (String) res.get(Constants.FORMAT);

        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thông tin resource từ Cloudinary: " + e.getMessage());
            throw new WebToeicException(
                    ResponseCode.NOT_EXISTED,
                    ResponseObject.FILE,
                    "Public ID không hợp lệ hoặc lỗi API: " + e.getMessage()
            );
        }
    }

    @Override
    public Map getFileInfo(String publicId) {
        try {
            return cloudinary.api().resource(publicId, ObjectUtils.asMap(
                    "resource_type", "raw"
            ));
        } catch (Exception e) {
            throw new WebToeicException(ResponseCode.CANNOT_READ, ResponseObject.FILE, e.getMessage());
        }
    }

    private String normalizePublicId(String publicId) {
        int dotIndex = publicId.lastIndexOf('.');
        if (dotIndex > 0) {
            // Loại bỏ phần mở rộng tệp (ví dụ: .pdf)
            return publicId.substring(0, dotIndex);
        }
        return publicId;
    }

}
