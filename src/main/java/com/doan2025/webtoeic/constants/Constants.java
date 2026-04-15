package com.doan2025.webtoeic.constants;

import com.doan2025.webtoeic.constants.enums.ERole;

import java.util.*;

public class Constants {

    public static final String UPDATE_IN_CLASS_CONTENT = "Lớp học của bạn có một cập nhật mới";
    public static final String NEW_QUIZ_IN_CLASS_CONTENT = "Lớp học của bạn có một bài kiểm tra mới.";
    public static final String ADD_TO_CLASS_CONTENT = "Bạn đã được thêm vào một lớp học.";
    public static final String NEW_COURSE_CONTENT = "Có một khóa học mới có lẽ bạn sẽ quan tâm. Hãy xem ngay để không bỏ lỡ.";

    public static final String PROMPT_AI = "You are LearnEZ.AI, an expert in English grammar quizzes. Analyze the input text, which contains multiple-choice questions. Extract each question into a JSON array of QuestionResponse objects. For each question: "
            + " - questionContent: Full question text (e.g., 'She ____ to the gym every morning.')."
            + " - category: Grammar topic {category_topic}."
            + " - difficulty: {difficulty_level}."
            + " - answers: Array of AnswerResponse {content: 'a) go', correct: true/false}."
            + " - explanation: ExplanationQuestionResponse {explanationEnglish: Brief English explanation, explanationVietnamese: Translation in Vietnamese}."
            + " Output ONLY a valid JSON array, no extra text.";
    public static final String CATEGORY_TOPIC = "category_topic";
    public static final String DIFFICULTY_LEVEL = "difficulty_level";
    public static final String USER_PROMPT = "Analyze this quiz data and output as JSON array:\n\n";
    public static final String PROJECT_ID = "gen-lang-client-0999014597";
    public static final String PROJECT_NAME = "learnEZ";
    public static final String LOCATION = "asia-southeast1";
    public static final String MODEL_AI = "gemini-2.5-flash";
    public static final String SUCCESS = "success";
    public static final String FAIL = "fail";

    public static final Long TWO_HOURS_MILLIS = 7200000L;

    public static final List<ERole> ROLE_BELOW_MANAGER = List.of(ERole.CONSULTANT, ERole.TEACHER, ERole.STUDENT);
    public static final List<ERole> ROLE_BELOW_CONSULTANT = List.of(ERole.TEACHER, ERole.STUDENT);

    public static final String PRE_CODE_STUDENT = "STU";
    public static final String PRE_CODE_TEACHER = "TEA";
    public static final String PRE_CODE_CONSULTANT = "CON";
    public static final String PRE_CODE_MANAGER = "MAN";

    public static final String USERNAME = "username";
    public static final String OTP_CODE = "OTP_Code";
    public static final String SUBJECT_RESET_PASSWORD = "[Hệ thống LEARNEZ] Mã xác thực đặt lại mật khẩu của bạn";
    public static final String BODY_REST_PASSWORD = "Xin chào username,\n" +
            "\n" +
            "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản của mình. Vui lòng sử dụng mã OTP bên dưới để hoàn tất quá trình đặt lại mật khẩu.\n" +
            "\n" +
            "\uD83D\uDD12 Mã OTP của bạn là: OTP_Code\n" +
            "⏰ Mã có hiệu lực trong vòng 5 phút kể từ thời điểm gửi.\n" +
            "\n" +
            "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này hoặc liên hệ với chúng tôi để được hỗ trợ.\n" +
            "\n" +
            "Trân trọng,\n" +
            "Hệ thống luyện thi TOEIC";

    public static final String REGEX_FILE_TYPE = "/v[0-9]+/";
    public static final String REGEX_FILE = "\\.[a-zA-Z]{3,4}$";
    public static final String QUALITY = "quality";
    public static final String FETCH_FORMAT = "fetch_format";
    public static final String AUTO = "auto";
    public static final String RESOURCE_TYPE = "resource_type";
    public static final String DURATION = "duration";
    public static final String RAW = "raw";
    public static final String FORMAT = "format";
    public static final String USE_FILENAME = "use_filename";
    public static final String UNIQUE_FILENAME = "unique_filename";
    public static final String FILENAME_OVERRIDE = "filename_override";
    public static final String TYPE = "type";
    public static final String UPLOAD = "upload";
    public static final String ACCESS_MODE = "access_mode";
    public static final String MODE_PUBLIC = "public";

    public static final String URL = "url";
    public static final String VIDEO = "video";
    public static final String IMAGE = "image";
    public static final String DOCUMENT = "document";

    public static final String RESOURCE_VIDEO = "videos";
    public static final String RESOURCE_IMAGE = "images";
    public static final String RESOURCE_DOCUMENT = "documents";

    public static final String FOLDER = "folder";

    public static final String JPG = "jpg";
    public static final String PNG = "png";
    public static final String GIF = "gif";
    public static final String BMP = "bmp";
    public static final String JPEG = "jpeg";

    public static final String MP4 = "mp4";
    public static final String MOV = "mov";
    public static final String AVI = "avi";
    public static final String MKV = "mkv";

    public static final String PDF = "pdf";

    public static final String DOC = "doc";
    public static final String DOCX = "docx";
    public static final String XLS = "xls";
    public static final String XLSX = "xlsx";
    public static final String PPT = "ppt";
    public static final String PPTX = "pptx";

    public static final List<String> IMAGE_TYPES = Arrays.asList(JPG, PNG, GIF, BMP, JPEG);
    public static final List<String> VIDEO_TYPES = Arrays.asList(MP4, MOV, AVI, MKV);
    public static final List<String> DOCUMENT_TYPES = Arrays.asList(PDF, DOCX, DOC, XLS, XLSX, PPT, PPTX);

    public static final List<String> UPLOAD_TYPES = new ArrayList<>();
    public static final Map<String, List<String>> ALLOWED_EXTENSIONS = new HashMap<>();
    public static final Map<String, String> EXTENSION_TO_TYPE = new HashMap<>();

    static {
        UPLOAD_TYPES.addAll(IMAGE_TYPES);
        UPLOAD_TYPES.addAll(VIDEO_TYPES);
        UPLOAD_TYPES.addAll(DOCUMENT_TYPES);

        ALLOWED_EXTENSIONS.put(VIDEO, VIDEO_TYPES);
        ALLOWED_EXTENSIONS.put(IMAGE, IMAGE_TYPES);
        ALLOWED_EXTENSIONS.put(DOCUMENT, DOCUMENT_TYPES);

        EXTENSION_TO_TYPE.put(JPG, IMAGE);
        EXTENSION_TO_TYPE.put(JPEG, IMAGE);
        EXTENSION_TO_TYPE.put(PNG, IMAGE);
        EXTENSION_TO_TYPE.put(GIF, IMAGE);
        EXTENSION_TO_TYPE.put(BMP, IMAGE);
        EXTENSION_TO_TYPE.put(MP4, VIDEO);
        EXTENSION_TO_TYPE.put(MOV, VIDEO);
        EXTENSION_TO_TYPE.put(AVI, VIDEO);
        EXTENSION_TO_TYPE.put(MKV, VIDEO);
        EXTENSION_TO_TYPE.put(PDF, DOCUMENT);
        EXTENSION_TO_TYPE.put(DOC, DOCUMENT);
        EXTENSION_TO_TYPE.put(DOCX, DOCUMENT);
        EXTENSION_TO_TYPE.put(XLSX, DOCUMENT);
        EXTENSION_TO_TYPE.put(XLS, DOCUMENT);
        EXTENSION_TO_TYPE.put(PPT, DOCUMENT);
        EXTENSION_TO_TYPE.put(PPTX, DOCUMENT);
    }

    public static class DATE_FORMAT {
        public static String YYYY_MM_DD = "yyyy-MM-dd";
        public static String TIME_ZONE = "Asia/Ho_Chi_Minh";
        public static String YYYY_MM_DD_HH_MM_SS = "yyyyMMddHHmmss";

        public DATE_FORMAT() {
        }
    }

    public static class PAYMENT {
        public static final String NCB = "NCB";
        public static final String VN = "VN";
        public static final String VND = "VND";
        public static final String VNP_VERSION = "vnp_Version";
        public static final String VNP_COMMAND = "vnp_Command";
        public static final String VNP_TMN_CODE = "vnp_TmnCode";
        public static final String VNP_AMOUNT = "vnp_Amount";
        public static final String VNP_CURR_CODE = "vnp_CurrCode";
        public static final String VNP_BANK_CODE = "vnp_BankCode";
        public static final String VNP_TXN_REF = "vnp_TxnRef";
        public static final String VNP_ORDER_INFO = "vnp_OrderInfo";
        public static final String VNP_ORDER_TYPE = "vnp_OrderType";
        public static final String VNP_LOCALE = "vnp_Locale";
        public static final String VNP_RETURN_URL = "vnp_ReturnUrl";
        public static final String VNP_IP_ADDR = "vnp_IpAddr";
        public static final String VNP_CREATE_DATE = "vnp_CreateDate";
        public static final String VNP_EXPIRE_DATE = "vnp_ExpireDate";
        public static final String VPN_SECURE_HASH = "&vnp_SecureHash=";
        public static final String VNP_RESPONSE_CODE = "vnp_ResponseCode";
        public static final String TXN_REF = "&txnRef=";
        public static final String TRANSACTION_NO = "&transactionNo=";
        public static final String VNP_TRANSACTION_NO = "vnp_TransactionNo";
        public static final String PAY_DATE = "&payDate=";
        public static final String VPN_PAY_DATE = "vnp_PayDate";
        public static final String AMOUNT = "&amount=";

        public PAYMENT() {
        }
    }

}
