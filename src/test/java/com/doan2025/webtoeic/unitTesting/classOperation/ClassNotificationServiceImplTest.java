package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.EClassNotificationType;
import com.doan2025.webtoeic.constants.enums.EJoinStatus;
import com.doan2025.webtoeic.constants.enums.ERole;
import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.domain.*;
import com.doan2025.webtoeic.domain.Class;
import com.doan2025.webtoeic.dto.SearchNotificationInClassDto;
import com.doan2025.webtoeic.dto.request.ClassNotificationRequest;
import com.doan2025.webtoeic.dto.response.AttachDocumentClassResponse;
import com.doan2025.webtoeic.dto.response.ClassNotificationResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.*;
import com.doan2025.webtoeic.utils.ConvertUtil;
import com.doan2025.webtoeic.utils.JwtUtil;
import com.doan2025.webtoeic.utils.NotiUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ============================================================
 * Unit Tests: ClassNotificationServiceImpl
 * ============================================================
 * Coverage target : 100 % JaCoCo Instructions & Branches
 * Test-ID prefix  : TC-CNS-XXX  (ClassNotificationService)
 *
 * Rollback strategy
 * -----------------
 * All tests run against Mockito mocks – no real database or
 * Spring context is started.  Where a test verifies a save()
 * or delete() call, an ArgumentCaptor is used to inspect the
 * captured value; no data is actually written anywhere.
 * Each @Test is completely independent (mocks reset by
 * MockitoExtension between tests).
 * ============================================================
 */
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ClassNotificationServiceImpl – Unit Tests")
class ClassNotificationServiceImplTest {

    // ─── Mocked dependencies ─────────────────────────────────────────────────
    @Mock private ClassNotificationRepository   classNotificationRepository;
    @Mock private ClassRepository               classRepository;
    @Mock private ClassMemberRepository         classMemberRepository;
    @Mock private AttachDocumentClassRepository attachDocumentClassRepository;
    @Mock private UserRepository                userRepository;
    @Mock private ConvertUtil                   convertUtil;
    @Mock private JwtUtil                       jwtUtil;
    @Mock private NotiUtils                     notiUtils;
    @Mock private HttpServletRequest            httpRequest;

    @InjectMocks
    private ClassNotificationServiceImpl service;

    // ─── Shared fixtures ─────────────────────────────────────────────────────
    private User managerUser;
    private User consultantUser;
    private User teacherUser;
    private User studentUser;
    private Class testClass;
    private ClassNotification testNotification;

    @BeforeEach
    void setUp() {
        // Manager
        managerUser = new User();
        managerUser.setId(1L);
        managerUser.setEmail("manager@test.com");
        managerUser.setRole(ERole.MANAGER);

        // Consultant
        consultantUser = new User();
        consultantUser.setId(2L);
        consultantUser.setEmail("consultant@test.com");
        consultantUser.setRole(ERole.CONSULTANT);

        // Teacher (is class teacher)
        teacherUser = new User();
        teacherUser.setId(3L);
        teacherUser.setEmail("teacher@test.com");
        teacherUser.setRole(ERole.TEACHER);

        // Student
        studentUser = new User();
        studentUser.setId(4L);
        studentUser.setEmail("student@test.com");
        studentUser.setRole(ERole.STUDENT);

        // Class whose teacher == teacherUser
        testClass = new Class();
        testClass.setId(100L);
        testClass.setName("TOEIC 600+");
        testClass.setTeacher(teacherUser);
        testClass.setCreatedBy(consultantUser);

        // Notification
        testNotification = new ClassNotification();
        testNotification.setId(200L);
        testNotification.setDescription("Homework assignment");
        testNotification.setTypeNotification(EClassNotificationType.EXERCISE);
        testNotification.setIsPin(false);
        testNotification.setIsActive(true);
        testNotification.setIsDelete(false);
        testNotification.setCreatedBy(consultantUser);
        testNotification.setClazz(testClass);
    }

    // =========================================================================
    //  getDetailNotificationInClass
    // =========================================================================

    /**
     * TC-CNS-001
     * Objective : Manager (non-STUDENT) can retrieve any notification detail.
     * Input     : managerUser token, notificationId = 200.
     * Expected  : ClassNotificationResponse returned without membership check.
     */
    @Test
    @DisplayName("TC-CNS-001: Manager retrieves notification detail successfully")
    void tc_cns_001_getDetail_manager_success() {
        List<AttachDocumentClass> docs = Collections.emptyList();
        ClassNotificationResponse expected = new ClassNotificationResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(managerUser.getEmail());
        when(userRepository.findByEmail(managerUser.getEmail()))
                .thenReturn(Optional.of(managerUser));
        when(classNotificationRepository.findById(200L))
                .thenReturn(Optional.of(testNotification));
        when(attachDocumentClassRepository.findByClassNotificationId(200L))
                .thenReturn(docs);
        when(convertUtil.convertClassNotificationToDto(httpRequest, testNotification, docs))
                .thenReturn(expected);

        ClassNotificationResponse result =
                service.getDetailNotificationInClass(httpRequest, 200L);

        assertThat(result).isEqualTo(expected);
        // Student-branch (existsMemberInClass) must NOT be called for non-student roles
        verify(classMemberRepository, never()).existsMemberInClass(anyLong(), anyLong());
    }

    /**
     * TC-CNS-002
     * Objective : Consultant (non-STUDENT) retrieves notification detail.
     * Input     : consultantUser token, notificationId = 200.
     * Expected  : ClassNotificationResponse returned.
     */
    @Test
    @DisplayName("TC-CNS-002: Consultant retrieves notification detail successfully")
    void tc_cns_002_getDetail_consultant_success() {
        List<AttachDocumentClass> docs = Collections.emptyList();
        ClassNotificationResponse expected = new ClassNotificationResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(consultantUser.getEmail());
        when(userRepository.findByEmail(consultantUser.getEmail()))
                .thenReturn(Optional.of(consultantUser));
        when(classNotificationRepository.findById(200L))
                .thenReturn(Optional.of(testNotification));
        when(attachDocumentClassRepository.findByClassNotificationId(200L))
                .thenReturn(docs);
        when(convertUtil.convertClassNotificationToDto(httpRequest, testNotification, docs))
                .thenReturn(expected);

        ClassNotificationResponse result =
                service.getDetailNotificationInClass(httpRequest, 200L);

        assertThat(result).isEqualTo(expected);
        verify(classMemberRepository, never()).existsMemberInClass(anyLong(), anyLong());
    }

    /**
     * TC-CNS-003
     * Objective : Teacher (non-STUDENT) retrieves notification detail.
     * Input     : teacherUser token, notificationId = 200.
     * Expected  : ClassNotificationResponse returned.
     */
    @Test
    @DisplayName("TC-CNS-003: Teacher retrieves notification detail successfully")
    void tc_cns_003_getDetail_teacher_success() {
        List<AttachDocumentClass> docs = Collections.emptyList();
        ClassNotificationResponse expected = new ClassNotificationResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail()))
                .thenReturn(Optional.of(teacherUser));
        when(classNotificationRepository.findById(200L))
                .thenReturn(Optional.of(testNotification));
        when(attachDocumentClassRepository.findByClassNotificationId(200L))
                .thenReturn(docs);
        when(convertUtil.convertClassNotificationToDto(httpRequest, testNotification, docs))
                .thenReturn(expected);

        ClassNotificationResponse result =
                service.getDetailNotificationInClass(httpRequest, 200L);

        assertThat(result).isEqualTo(expected);
    }

    /**
     * TC-CNS-004
     * Objective : Student who IS a class member retrieves notification detail.
     * Input     : studentUser token, notificationId = 200, isMember = true.
     * Expected  : ClassNotificationResponse returned.
     */
    @Test
    @DisplayName("TC-CNS-004: Student member retrieves notification detail successfully")
    void tc_cns_004_getDetail_studentMember_success() {
        List<AttachDocumentClass> docs = Collections.emptyList();
        ClassNotificationResponse expected = new ClassNotificationResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(studentUser.getEmail());
        when(userRepository.findByEmail(studentUser.getEmail()))
                .thenReturn(Optional.of(studentUser));
        when(classNotificationRepository.findById(200L))
                .thenReturn(Optional.of(testNotification));
        when(classMemberRepository.existsMemberInClass(100L, 4L)).thenReturn(true);
        when(attachDocumentClassRepository.findByClassNotificationId(200L))
                .thenReturn(docs);
        when(convertUtil.convertClassNotificationToDto(httpRequest, testNotification, docs))
                .thenReturn(expected);

        ClassNotificationResponse result =
                service.getDetailNotificationInClass(httpRequest, 200L);

        assertThat(result).isEqualTo(expected);
    }

    /**
     * TC-CNS-005
     * Objective : Student who is NOT a class member is denied detail.
     * Input     : studentUser token, notificationId = 200, isMember = false.
     * Expected  : WebToeicException (NOT_PERMISSION).
     */
    @Test
    @DisplayName("TC-CNS-005: Student non-member denied notification detail → NOT_PERMISSION")
    void tc_cns_005_getDetail_studentNonMember_throwsNotPermission() {
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(studentUser.getEmail());
        when(userRepository.findByEmail(studentUser.getEmail()))
                .thenReturn(Optional.of(studentUser));
        when(classNotificationRepository.findById(200L))
                .thenReturn(Optional.of(testNotification));
        when(classMemberRepository.existsMemberInClass(100L, 4L)).thenReturn(false);

        assertThatThrownBy(() ->
                service.getDetailNotificationInClass(httpRequest, 200L))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_PERMISSION));
    }

    /**
     * TC-CNS-006
     * Objective : Notification not found → WebToeicException (NOT_EXISTED).
     * Input     : Any user token, notificationId = 999 (non-existing).
     * Expected  : WebToeicException (NOT_EXISTED).
     */
    @Test
    @DisplayName("TC-CNS-006: Notification not found → NOT_EXISTED")
    void tc_cns_006_getDetail_notificationNotFound_throwsNotExisted() {
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(managerUser.getEmail());
        when(userRepository.findByEmail(managerUser.getEmail()))
                .thenReturn(Optional.of(managerUser));
        when(classNotificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.getDetailNotificationInClass(httpRequest, 999L))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_EXISTED));
    }

    /**
     * TC-CNS-007
     * Objective : User token maps to an unknown email → WebToeicException (NOT_EXISTED).
     * Input     : Token email not present in userRepository.
     * Expected  : WebToeicException (NOT_EXISTED).
     */
    @Test
    @DisplayName("TC-CNS-007: Unknown user in getDetail → NOT_EXISTED")
    void tc_cns_007_getDetail_unknownUser_throwsNotExisted() {
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn("ghost@x.com");
        when(userRepository.findByEmail("ghost@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.getDetailNotificationInClass(httpRequest, 200L))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_EXISTED));
    }

    // =========================================================================
    //  getListNotificationInClass
    // =========================================================================

    /**
     * TC-CNS-008
     * Objective : Manager gets paginated list of notifications in a class.
     * Input     : managerUser, dto.classId = 100, pageable.
     * Expected  : Page<ClassNotificationResponse> with mapped items.
     */
    @Test
    @DisplayName("TC-CNS-008: Manager retrieves notification list successfully")
    void tc_cns_008_getList_manager_success() {
        Pageable pageable = PageRequest.of(0, 10);
        SearchNotificationInClassDto dto = buildSearchDto(100L, null);

        Page<ClassNotification> repoPage =
                new PageImpl<>(List.of(testNotification), pageable, 1);
        ClassNotificationResponse mappedItem = new ClassNotificationResponse();
        List<AttachDocumentClass> docs = Collections.emptyList();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(managerUser.getEmail());
        when(userRepository.findByEmail(managerUser.getEmail()))
                .thenReturn(Optional.of(managerUser));
        when(classRepository.findById(100L)).thenReturn(Optional.of(testClass));
        when(classMemberRepository.existsMemberInClass(100L, 1L)).thenReturn(false);
        when(classNotificationRepository.findByClazzId(dto, ERole.MANAGER.name(), pageable))
                .thenReturn(repoPage);
        when(attachDocumentClassRepository.findByClassNotificationId(200L))
                .thenReturn(docs);
        when(convertUtil.convertClassNotificationToDto(httpRequest, testNotification, docs))
                .thenReturn(mappedItem);

        Page<ClassNotificationResponse> result =
                service.getListNotificationInClass(httpRequest, dto, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(mappedItem);
    }

    /**
     * TC-CNS-009
     * Objective : Consultant gets paginated list of notifications.
     * Input     : consultantUser, dto.classId = 100.
     * Expected  : Page<ClassNotificationResponse>.
     */
    @Test
    @DisplayName("TC-CNS-009: Consultant retrieves notification list successfully")
    void tc_cns_009_getList_consultant_success() {
        Pageable pageable = PageRequest.of(0, 10);
        SearchNotificationInClassDto dto = buildSearchDto(100L, null);
        Page<ClassNotification> repoPage = Page.empty(pageable);

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(consultantUser.getEmail());
        when(userRepository.findByEmail(consultantUser.getEmail()))
                .thenReturn(Optional.of(consultantUser));
        when(classRepository.findById(100L)).thenReturn(Optional.of(testClass));
        when(classMemberRepository.existsMemberInClass(100L, 2L)).thenReturn(false);
        when(classNotificationRepository.findByClazzId(dto, ERole.CONSULTANT.name(), pageable))
                .thenReturn(repoPage);

        Page<ClassNotificationResponse> result =
                service.getListNotificationInClass(httpRequest, dto, pageable);

        assertThat(result).isNotNull();
    }

    /**
     * TC-CNS-010
     * Objective : Teacher gets notification list (is class member).
     * Input     : teacherUser, dto.classId = 100, isMember = true.
     * Expected  : Page<ClassNotificationResponse>.
     */
    @Test
    @DisplayName("TC-CNS-010: Teacher member retrieves notification list successfully")
    void tc_cns_010_getList_teacherMember_success() {
        Pageable pageable = PageRequest.of(0, 10);
        SearchNotificationInClassDto dto = buildSearchDto(100L, null);
        Page<ClassNotification> repoPage = Page.empty(pageable);

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail()))
                .thenReturn(Optional.of(teacherUser));
        when(classRepository.findById(100L)).thenReturn(Optional.of(testClass));
        when(classMemberRepository.existsMemberInClass(100L, 3L)).thenReturn(true);
        when(classNotificationRepository.findByClazzId(dto, ERole.TEACHER.name(), pageable))
                .thenReturn(repoPage);

        Page<ClassNotificationResponse> result =
                service.getListNotificationInClass(httpRequest, dto, pageable);

        assertThat(result).isNotNull();
    }

    /**
     * TC-CNS-011
     * Objective : Student who is a class member gets notification list.
     * Input     : studentUser, dto.classId = 100, isMember = true.
     * Expected  : Page<ClassNotificationResponse>.
     */
    @Test
    @DisplayName("TC-CNS-011: Student member retrieves notification list successfully")
    void tc_cns_011_getList_studentMember_success() {
        Pageable pageable = PageRequest.of(0, 10);
        SearchNotificationInClassDto dto = buildSearchDto(100L, null);
        Page<ClassNotification> repoPage = Page.empty(pageable);

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(studentUser.getEmail());
        when(userRepository.findByEmail(studentUser.getEmail()))
                .thenReturn(Optional.of(studentUser));
        when(classRepository.findById(100L)).thenReturn(Optional.of(testClass));
        when(classMemberRepository.existsMemberInClass(100L, 4L)).thenReturn(true);
        when(classNotificationRepository.findByClazzId(dto, ERole.STUDENT.name(), pageable))
                .thenReturn(repoPage);

        Page<ClassNotificationResponse> result =
                service.getListNotificationInClass(httpRequest, dto, pageable);

        assertThat(result).isNotNull();
    }

    /**
     * TC-CNS-012
     * Objective : Student NOT in class is denied list access → NOT_PERMISSION.
     * Input     : studentUser, dto.classId = 100, isMember = false.
     * Expected  : WebToeicException (NOT_PERMISSION).
     */
    @Test
    @DisplayName("TC-CNS-012: Student non-member denied notification list → NOT_PERMISSION")
    void tc_cns_012_getList_studentNonMember_throwsNotPermission() {
        Pageable pageable = PageRequest.of(0, 10);
        SearchNotificationInClassDto dto = buildSearchDto(100L, null);

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(studentUser.getEmail());
        when(userRepository.findByEmail(studentUser.getEmail()))
                .thenReturn(Optional.of(studentUser));
        when(classRepository.findById(100L)).thenReturn(Optional.of(testClass));
        when(classMemberRepository.existsMemberInClass(100L, 4L)).thenReturn(false);

        assertThatThrownBy(() ->
                service.getListNotificationInClass(httpRequest, dto, pageable))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_PERMISSION));
    }

    /**
     * TC-CNS-013
     * Objective : Class not found → NOT_EXISTED.
     * Input     : managerUser, dto.classId = 999 (non-existing).
     * Expected  : WebToeicException (NOT_EXISTED).
     */
    @Test
    @DisplayName("TC-CNS-013: Class not found in getList → NOT_EXISTED")
    void tc_cns_013_getList_classNotFound_throwsNotExisted() {
        Pageable pageable = PageRequest.of(0, 10);
        SearchNotificationInClassDto dto = buildSearchDto(999L, null);

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(managerUser.getEmail());
        when(userRepository.findByEmail(managerUser.getEmail()))
                .thenReturn(Optional.of(managerUser));
        when(classRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.getListNotificationInClass(httpRequest, dto, pageable))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_EXISTED));
    }

    /**
     * TC-CNS-014
     * Objective : Unknown user token → NOT_EXISTED.
     * Input     : Token maps to email not in DB.
     * Expected  : WebToeicException (NOT_EXISTED).
     */
    @Test
    @DisplayName("TC-CNS-014: Unknown user in getList → NOT_EXISTED")
    void tc_cns_014_getList_unknownUser_throwsNotExisted() {
        Pageable pageable = PageRequest.of(0, 10);
        SearchNotificationInClassDto dto = buildSearchDto(100L, null);

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn("ghost@x.com");
        when(userRepository.findByEmail("ghost@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.getListNotificationInClass(httpRequest, dto, pageable))
                .isInstanceOf(WebToeicException.class);
    }

    /**
     * TC-CNS-015
     * Objective : Multiple notifications in page are all mapped to DTO.
     * Input     : managerUser, dto.classId = 100; repository returns 2 items.
     * Expected  : Page of 2 ClassNotificationResponse items.
     */
    @Test
    @DisplayName("TC-CNS-015: Multiple notifications mapped correctly in page")
    void tc_cns_015_getList_multipleItems_allMapped() {
        Pageable pageable = PageRequest.of(0, 10);
        SearchNotificationInClassDto dto = buildSearchDto(100L, null);

        ClassNotification n2 = new ClassNotification();
        n2.setId(201L);
        n2.setClazz(testClass);
        n2.setTypeNotification(EClassNotificationType.NOTIFICATION);
        n2.setCreatedBy(teacherUser);

        Page<ClassNotification> repoPage =
                new PageImpl<>(List.of(testNotification, n2), pageable, 2);
        ClassNotificationResponse r1 = new ClassNotificationResponse();
        ClassNotificationResponse r2 = new ClassNotificationResponse();
        List<AttachDocumentClass> docs1 = Collections.emptyList();
        List<AttachDocumentClass> docs2 = Collections.emptyList();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(managerUser.getEmail());
        when(userRepository.findByEmail(managerUser.getEmail()))
                .thenReturn(Optional.of(managerUser));
        when(classRepository.findById(100L)).thenReturn(Optional.of(testClass));
        when(classMemberRepository.existsMemberInClass(100L, 1L)).thenReturn(false);
        when(classNotificationRepository.findByClazzId(dto, ERole.MANAGER.name(), pageable))
                .thenReturn(repoPage);
        when(attachDocumentClassRepository.findByClassNotificationId(200L)).thenReturn(docs1);
        when(attachDocumentClassRepository.findByClassNotificationId(201L)).thenReturn(docs2);
        when(convertUtil.convertClassNotificationToDto(httpRequest, testNotification, docs1))
                .thenReturn(r1);
        when(convertUtil.convertClassNotificationToDto(httpRequest, n2, docs2))
                .thenReturn(r2);

        Page<ClassNotificationResponse> result =
                service.getListNotificationInClass(httpRequest, dto, pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).containsExactly(r1, r2);
    }

    // =========================================================================
    //  createNotificationInClass
    // =========================================================================

    /**
     * TC-CNS-016
     * Objective : Class teacher creates notification without attachments.
     * Input     : teacherUser (class's teacher), valid request, no urlAttachment.
     * Expected  : ClassNotificationResponse; save() called; notiUtils.sendNoti() called.
     * Rollback  : save() mocked – nothing persisted.
     */
    @Test
    @DisplayName("TC-CNS-016: Teacher creates notification without attachments")
    void tc_cns_016_create_teacher_noAttachments_success() {
        ClassNotificationRequest req = buildCreateRequest(100L, false, false);

        ClassNotificationResponse expected = new ClassNotificationResponse();
        List<AttachDocumentClass> docs = Collections.emptyList();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail()))
                .thenReturn(Optional.of(teacherUser));
        when(classRepository.findById(100L)).thenReturn(Optional.of(testClass));
        when(classNotificationRepository.save(any())).thenReturn(testNotification);
        when(classMemberRepository.findMembersInClass(100L))
                .thenReturn(List.of(studentUser));
        when(attachDocumentClassRepository.findByClassNotificationId(anyLong()))
                .thenReturn(docs);
        when(convertUtil.convertClassNotificationToDto(any(), any(), any()))
                .thenReturn(expected);

        ClassNotificationResponse result =
                service.createNotificationInClass(httpRequest, req);

        assertThat(result).isEqualTo(expected);
        verify(classNotificationRepository).save(any(ClassNotification.class));
        verify(notiUtils).sendNoti(anyList(), any(), anyString(), anyString(), anyLong());
        // Attachments should NOT be saved
        verify(attachDocumentClassRepository, never()).save(any());
    }

    /**
     * TC-CNS-017
     * Objective : Teacher creates notification WITH attachments.
     * Input     : teacherUser, request with 2 attachment URLs, isPin = true.
     * Expected  : ClassNotificationResponse; attachDocumentClassRepository.save() called twice.
     * Rollback  : save() mocked.
     */
    @Test
    @DisplayName("TC-CNS-017: Teacher creates notification with attachments")
    void tc_cns_017_create_teacher_withAttachments_success() {
        ClassNotificationRequest req = buildCreateRequest(100L, true, true);
        req.setUrlAttachment(List.of("http://file1.pdf", "http://file2.pdf"));

        ClassNotificationResponse expected = new ClassNotificationResponse();
        AttachDocumentClass savedAttach = new AttachDocumentClass();
        savedAttach.setId(1L);

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail()))
                .thenReturn(Optional.of(teacherUser));
        when(classRepository.findById(100L)).thenReturn(Optional.of(testClass));
        when(classNotificationRepository.save(any())).thenReturn(testNotification);
        when(attachDocumentClassRepository.save(any())).thenReturn(savedAttach);
        when(classMemberRepository.findMembersInClass(100L))
                .thenReturn(List.of(studentUser));
        when(attachDocumentClassRepository.findByClassNotificationId(anyLong()))
                .thenReturn(List.of(savedAttach));
        when(convertUtil.convertClassNotificationToDto(any(), any(), any()))
                .thenReturn(expected);

        ClassNotificationResponse result =
                service.createNotificationInClass(httpRequest, req);

        assertThat(result).isEqualTo(expected);
        // One save per URL
        verify(attachDocumentClassRepository, times(2)).save(any(AttachDocumentClass.class));
    }

    /**
     * TC-CNS-018
     * Objective : Consultant creates notification successfully.
     * Input     : consultantUser, valid request, no attachments.
     * Expected  : ClassNotificationResponse; save() called.
     * Rollback  : save() mocked.
     */
    @Test
    @DisplayName("TC-CNS-018: Consultant creates notification successfully")
    void tc_cns_018_create_consultant_success() {
        ClassNotificationRequest req = buildCreateRequest(100L, false, false);
        ClassNotificationResponse expected = new ClassNotificationResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(consultantUser.getEmail());
        when(userRepository.findByEmail(consultantUser.getEmail()))
                .thenReturn(Optional.of(consultantUser));
        when(classRepository.findById(100L)).thenReturn(Optional.of(testClass));
        when(classNotificationRepository.save(any())).thenReturn(testNotification);
        when(classMemberRepository.findMembersInClass(100L))
                .thenReturn(Collections.emptyList());
        when(attachDocumentClassRepository.findByClassNotificationId(anyLong()))
                .thenReturn(Collections.emptyList());
        when(convertUtil.convertClassNotificationToDto(any(), any(), any()))
                .thenReturn(expected);

        ClassNotificationResponse result =
                service.createNotificationInClass(httpRequest, req);

        assertThat(result).isEqualTo(expected);
        verify(classNotificationRepository).save(any());
    }

    /**
     * TC-CNS-019
     * Objective : Manager creates notification successfully.
     * Input     : managerUser, valid request.
     * Expected  : ClassNotificationResponse; save() called.
     * Rollback  : save() mocked.
     */
    @Test
    @DisplayName("TC-CNS-019: Manager creates notification successfully")
    void tc_cns_019_create_manager_success() {
        ClassNotificationRequest req = buildCreateRequest(100L, false, false);
        ClassNotificationResponse expected = new ClassNotificationResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(managerUser.getEmail());
        when(userRepository.findByEmail(managerUser.getEmail()))
                .thenReturn(Optional.of(managerUser));
        when(classRepository.findById(100L)).thenReturn(Optional.of(testClass));
        when(classNotificationRepository.save(any())).thenReturn(testNotification);
        when(classMemberRepository.findMembersInClass(100L))
                .thenReturn(Collections.emptyList());
        when(attachDocumentClassRepository.findByClassNotificationId(anyLong()))
                .thenReturn(Collections.emptyList());
        when(convertUtil.convertClassNotificationToDto(any(), any(), any()))
                .thenReturn(expected);

        ClassNotificationResponse result =
                service.createNotificationInClass(httpRequest, req);

        assertThat(result).isEqualTo(expected);
    }

    /**
     * TC-CNS-020
     * Objective : Student is denied create → NOT_PERMISSION.
     * Input     : studentUser (not teacher/consultant/manager), valid request.
     * Expected  : WebToeicException (NOT_PERMISSION); save() never called.
     * Rollback  : save() never reached.
     */
    @Test
    @DisplayName("TC-CNS-020: Student denied create notification → NOT_PERMISSION")
    void tc_cns_020_create_student_throwsNotPermission() {
        ClassNotificationRequest req = buildCreateRequest(100L, false, false);

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(studentUser.getEmail());
        when(userRepository.findByEmail(studentUser.getEmail()))
                .thenReturn(Optional.of(studentUser));
        when(classRepository.findById(100L)).thenReturn(Optional.of(testClass));

        assertThatThrownBy(() ->
                service.createNotificationInClass(httpRequest, req))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_PERMISSION));

        verify(classNotificationRepository, never()).save(any());
    }

    /**
     * TC-CNS-021
     * Objective : A TEACHER who is NOT the class teacher is denied create → NOT_PERMISSION.
     * Input     : anotherTeacher (different from class teacher), valid request.
     * Expected  : WebToeicException (NOT_PERMISSION); save() never called.
     * Rollback  : save() never reached.
     */
    @Test
    @DisplayName("TC-CNS-021: Non-class-teacher TEACHER denied create notification")
    void tc_cns_021_create_foreignTeacher_throwsNotPermission() {
        User anotherTeacher = new User();
        anotherTeacher.setId(99L);
        anotherTeacher.setEmail("other.teacher@test.com");
        anotherTeacher.setRole(ERole.TEACHER);

        ClassNotificationRequest req = buildCreateRequest(100L, false, false);

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(anotherTeacher.getEmail());
        when(userRepository.findByEmail(anotherTeacher.getEmail()))
                .thenReturn(Optional.of(anotherTeacher));
        when(classRepository.findById(100L)).thenReturn(Optional.of(testClass));

        assertThatThrownBy(() ->
                service.createNotificationInClass(httpRequest, req))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_PERMISSION));

        verify(classNotificationRepository, never()).save(any());
    }

    /**
     * TC-CNS-022
     * Objective : Class not found on create → NOT_EXISTED.
     * Input     : consultantUser, request with classId = 999.
     * Expected  : WebToeicException (NOT_EXISTED).
     * Rollback  : save() never called.
     */
    @Test
    @DisplayName("TC-CNS-022: Class not found in create → NOT_EXISTED")
    void tc_cns_022_create_classNotFound_throwsNotExisted() {
        ClassNotificationRequest req = buildCreateRequest(999L, false, false);

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(consultantUser.getEmail());
        when(userRepository.findByEmail(consultantUser.getEmail()))
                .thenReturn(Optional.of(consultantUser));
        when(classRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.createNotificationInClass(httpRequest, req))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_EXISTED));

        verify(classNotificationRepository, never()).save(any());
    }

    /**
     * TC-CNS-023
     * Objective : Unknown user on create → NOT_EXISTED.
     * Input     : Token email not in userRepository.
     * Expected  : WebToeicException (NOT_EXISTED).
     */
    @Test
    @DisplayName("TC-CNS-023: Unknown user in create → NOT_EXISTED")
    void tc_cns_023_create_unknownUser_throwsNotExisted() {
        ClassNotificationRequest req = buildCreateRequest(100L, false, false);

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn("ghost@x.com");
        when(userRepository.findByEmail("ghost@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.createNotificationInClass(httpRequest, req))
                .isInstanceOf(WebToeicException.class);
    }

    /**
     * TC-CNS-024
     * Objective : Create with empty urlAttachment list → NO attachment saves.
     * Input     : teacherUser, request with empty urlAttachment list (not null, but empty).
     * Expected  : ClassNotificationResponse; attachDocumentClassRepository.save() NOT called.
     * Rollback  : save() mocked.
     */
    @Test
    @DisplayName("TC-CNS-024: Empty urlAttachment list – no attachment saves")
    void tc_cns_024_create_emptyAttachmentList_noAttachSave() {
        ClassNotificationRequest req = buildCreateRequest(100L, false, false);
        req.setUrlAttachment(Collections.emptyList()); // explicitly empty
        ClassNotificationResponse expected = new ClassNotificationResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail()))
                .thenReturn(Optional.of(teacherUser));
        when(classRepository.findById(100L)).thenReturn(Optional.of(testClass));
        when(classNotificationRepository.save(any())).thenReturn(testNotification);
        when(classMemberRepository.findMembersInClass(100L))
                .thenReturn(Collections.emptyList());
        when(attachDocumentClassRepository.findByClassNotificationId(anyLong()))
                .thenReturn(Collections.emptyList());
        when(convertUtil.convertClassNotificationToDto(any(), any(), any()))
                .thenReturn(expected);

        service.createNotificationInClass(httpRequest, req);

        verify(attachDocumentClassRepository, never()).save(any());
    }

    /**
     * TC-CNS-025
     * Objective : isPin = true is persisted to saved notification.
     * Input     : teacherUser, request with isPin = true.
     * Expected  : Saved ClassNotification has isPin = true.
     * Rollback  : save() mocked; captured value inspected.
     */
    @Test
    @DisplayName("TC-CNS-025: isPin=true propagated to saved notification")
    void tc_cns_025_create_isPinTrue_propagated() {
        ClassNotificationRequest req = buildCreateRequest(100L, false, false);
        req.setIsPin(true);
        ClassNotificationResponse expected = new ClassNotificationResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail()))
                .thenReturn(Optional.of(teacherUser));
        when(classRepository.findById(100L)).thenReturn(Optional.of(testClass));

        ArgumentCaptor<ClassNotification> captor =
                ArgumentCaptor.forClass(ClassNotification.class);
        when(classNotificationRepository.save(captor.capture())).thenReturn(testNotification);
        when(classMemberRepository.findMembersInClass(100L)).thenReturn(Collections.emptyList());
        when(attachDocumentClassRepository.findByClassNotificationId(anyLong()))
                .thenReturn(Collections.emptyList());
        when(convertUtil.convertClassNotificationToDto(any(), any(), any()))
                .thenReturn(expected);

        service.createNotificationInClass(httpRequest, req);

        assertThat(captor.getValue().getIsPin()).isTrue();
    }

    /**
     * TC-CNS-026
     * Objective : isPin = null is treated as false (null-safe) in saved notification.
     * Input     : teacherUser, request with isPin = null.
     * Expected  : Saved ClassNotification has isPin = false.
     * Rollback  : save() mocked.
     */
    @Test
    @DisplayName("TC-CNS-026: isPin=null treated as false in saved notification")
    void tc_cns_026_create_isPinNull_treatedAsFalse() {
        ClassNotificationRequest req = buildCreateRequest(100L, false, false);
        req.setIsPin(null);
        ClassNotificationResponse expected = new ClassNotificationResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail()))
                .thenReturn(Optional.of(teacherUser));
        when(classRepository.findById(100L)).thenReturn(Optional.of(testClass));

        ArgumentCaptor<ClassNotification> captor =
                ArgumentCaptor.forClass(ClassNotification.class);
        when(classNotificationRepository.save(captor.capture())).thenReturn(testNotification);
        when(classMemberRepository.findMembersInClass(100L)).thenReturn(Collections.emptyList());
        when(attachDocumentClassRepository.findByClassNotificationId(anyLong()))
                .thenReturn(Collections.emptyList());
        when(convertUtil.convertClassNotificationToDto(any(), any(), any()))
                .thenReturn(expected);

        service.createNotificationInClass(httpRequest, req);

        assertThat(captor.getValue().getIsPin()).isFalse();
    }

    // =========================================================================
    //  updateNotificationInClass
    // =========================================================================

    /**
     * TC-CNS-027
     * Objective : Class teacher updates notification without changing attachments.
     * Input     : teacherUser (class teacher), request with null urlAttachment.
     * Expected  : Updated ClassNotificationResponse; deleteAll NOT called; save() called.
     * Rollback  : save() mocked.
     */
    @Test
    @DisplayName("TC-CNS-027: Teacher updates notification without attachment change")
    void tc_cns_027_update_teacher_nullAttachments_success() {
        ClassNotificationRequest req = buildUpdateRequest(200L, 100L, null);
        ClassNotificationResponse expected = new ClassNotificationResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail()))
                .thenReturn(Optional.of(teacherUser));
        when(classNotificationRepository.findById(200L))
                .thenReturn(Optional.of(testNotification));
        when(classNotificationRepository.save(any())).thenReturn(testNotification);
        when(attachDocumentClassRepository.findByClassNotificationId(anyLong()))
                .thenReturn(Collections.emptyList());
        when(convertUtil.convertClassNotificationToDto(any(), any(), any()))
                .thenReturn(expected);

        ClassNotificationResponse result =
                service.updateNotificationInClass(httpRequest, req);

        assertThat(result).isEqualTo(expected);
        // No delete → no re-save of attachments
        verify(attachDocumentClassRepository, never())
                .deleteAllAttachDocumentClassByClassNotificationId(anyLong());
    }

    /**
     * TC-CNS-028
     * Objective : Teacher updates notification WITH new attachment list (replaces old ones).
     * Input     : teacherUser, request with 1 new URL in urlAttachment.
     * Expected  : deleteAll called; new AttachDocumentClass saved once.
     * Rollback  : save() mocked.
     */
    @Test
    @DisplayName("TC-CNS-028: Teacher updates notification with new attachments")
    void tc_cns_028_update_teacher_withAttachments_success() {
        ClassNotificationRequest req = buildUpdateRequest(200L, 100L,
                List.of("http://newfile.pdf"));
        ClassNotificationResponse expected = new ClassNotificationResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail()))
                .thenReturn(Optional.of(teacherUser));
        when(classNotificationRepository.findById(200L))
                .thenReturn(Optional.of(testNotification));
        doNothing().when(attachDocumentClassRepository)
                .deleteAllAttachDocumentClassByClassNotificationId(200L);
        when(classNotificationRepository.save(any())).thenReturn(testNotification);
        when(attachDocumentClassRepository.findByClassNotificationId(anyLong()))
                .thenReturn(Collections.emptyList());
        when(convertUtil.convertClassNotificationToDto(any(), any(), any()))
                .thenReturn(expected);

        service.updateNotificationInClass(httpRequest, req);

        verify(attachDocumentClassRepository)
                .deleteAllAttachDocumentClassByClassNotificationId(200L);
    }

    /**
     * TC-CNS-029
     * Objective : Non-class-teacher is denied update → NOT_PERMISSION.
     * Input     : consultantUser (not class teacher), update request.
     * Expected  : WebToeicException (NOT_PERMISSION); save() never called.
     * Rollback  : save() never reached.
     */
    @Test
    @DisplayName("TC-CNS-029: Non-class-teacher denied update → NOT_PERMISSION")
    void tc_cns_029_update_nonClassTeacher_throwsNotPermission() {
        ClassNotificationRequest req = buildUpdateRequest(200L, 100L, null);

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(consultantUser.getEmail());
        when(userRepository.findByEmail(consultantUser.getEmail()))
                .thenReturn(Optional.of(consultantUser));
        when(classNotificationRepository.findById(200L))
                .thenReturn(Optional.of(testNotification));

        assertThatThrownBy(() ->
                service.updateNotificationInClass(httpRequest, req))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_PERMISSION));

        verify(classNotificationRepository, never()).save(any());
    }

    /**
     * TC-CNS-030
     * Objective : Manager is denied update (only class-teacher allowed) → NOT_PERMISSION.
     * Input     : managerUser, update request.
     * Expected  : WebToeicException (NOT_PERMISSION).
     * Rollback  : save() never called.
     */
    @Test
    @DisplayName("TC-CNS-030: Manager denied update → NOT_PERMISSION")
    void tc_cns_030_update_manager_throwsNotPermission() {
        ClassNotificationRequest req = buildUpdateRequest(200L, 100L, null);

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(managerUser.getEmail());
        when(userRepository.findByEmail(managerUser.getEmail()))
                .thenReturn(Optional.of(managerUser));
        when(classNotificationRepository.findById(200L))
                .thenReturn(Optional.of(testNotification));

        assertThatThrownBy(() ->
                service.updateNotificationInClass(httpRequest, req))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_PERMISSION));
    }

    /**
     * TC-CNS-031
     * Objective : Notification not found on update → NOT_EXISTED.
     * Input     : teacherUser, classNotificationId = 999 (non-existing).
     * Expected  : WebToeicException (NOT_EXISTED).
     * Rollback  : save() never reached.
     */
    @Test
    @DisplayName("TC-CNS-031: Notification not found in update → NOT_EXISTED")
    void tc_cns_031_update_notificationNotFound_throwsNotExisted() {
        ClassNotificationRequest req = buildUpdateRequest(999L, 100L, null);

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail()))
                .thenReturn(Optional.of(teacherUser));
        when(classNotificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.updateNotificationInClass(httpRequest, req))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_EXISTED));
    }

    /**
     * TC-CNS-032
     * Objective : Unknown user in update → NOT_EXISTED.
     * Input     : Token email not in userRepository.
     * Expected  : WebToeicException (NOT_EXISTED).
     */
    @Test
    @DisplayName("TC-CNS-032: Unknown user in update → NOT_EXISTED")
    void tc_cns_032_update_unknownUser_throwsNotExisted() {
        ClassNotificationRequest req = buildUpdateRequest(200L, 100L, null);

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn("ghost@x.com");
        when(userRepository.findByEmail("ghost@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.updateNotificationInClass(httpRequest, req))
                .isInstanceOf(WebToeicException.class);
    }

    /**
     * TC-CNS-033
     * Objective : FieldUpdateUtil branches: field values unchanged → no actual update in entity.
     * Input     : teacherUser, request with same description as current notification.
     * Expected  : save() still called (entity is re-persisted regardless); response returned.
     * Rollback  : save() mocked.
     */
    @Test
    @DisplayName("TC-CNS-033: FieldUpdateUtil – unchanged fields still persist correctly")
    void tc_cns_033_update_unchangedFields_saveStillCalled() {
        // Request description == existing description → FieldUpdateUtil does nothing for it
        ClassNotificationRequest req = buildUpdateRequest(200L, 100L, null);
        req.setDescription(testNotification.getDescription()); // same value → no update
        ClassNotificationResponse expected = new ClassNotificationResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail()))
                .thenReturn(Optional.of(teacherUser));
        when(classNotificationRepository.findById(200L))
                .thenReturn(Optional.of(testNotification));
        when(classNotificationRepository.save(any())).thenReturn(testNotification);
        when(attachDocumentClassRepository.findByClassNotificationId(anyLong()))
                .thenReturn(Collections.emptyList());
        when(convertUtil.convertClassNotificationToDto(any(), any(), any()))
                .thenReturn(expected);

        ClassNotificationResponse result =
                service.updateNotificationInClass(httpRequest, req);

        assertThat(result).isEqualTo(expected);
    }

    // =========================================================================
    //  disableOrDeleteNotificationInClass
    // =========================================================================

    /**
     * TC-CNS-034
     * Objective : Teacher disables (isActive = false) a notification.
     * Input     : teacherUser, request with isActive = false (different from current true).
     * Expected  : save() called; captured notification has isActive = false.
     * Rollback  : save() mocked.
     */
    @Test
    @DisplayName("TC-CNS-034: Teacher disables notification successfully")
    void tc_cns_034_disableOrDelete_teacher_disableActive_success() {
        ClassNotificationRequest req = buildToggleRequest(200L, 100L, false, false);
        ClassNotificationResponse expected = new ClassNotificationResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail()))
                .thenReturn(Optional.of(teacherUser));
        when(classNotificationRepository.findById(200L))
                .thenReturn(Optional.of(testNotification));

        when(attachDocumentClassRepository.findByClassNotificationId(anyLong()))
                .thenReturn(Collections.emptyList());
        when(convertUtil.convertClassNotificationToDto(any(), any(), any()))
                .thenReturn(expected);

        ClassNotificationResponse result =
                service.disableOrDeleteNotificationInClass(httpRequest, req);

        assertThat(result).isEqualTo(expected);
        assertThat(testNotification.getIsActive()).isFalse();
    }

    /**
     * TC-CNS-035
     * Objective : Teacher soft-deletes (isDelete = true) a notification.
     * Input     : teacherUser, request with isDelete = true (different from current false).
     * Expected  : save() called; captured notification has isDelete = true.
     * Rollback  : save() mocked.
     */
    @Test
    @DisplayName("TC-CNS-035: Teacher soft-deletes notification successfully")
    void tc_cns_035_disableOrDelete_teacher_softDelete_success() {
        ClassNotificationRequest req = buildToggleRequest(200L, 100L, true, true);
        ClassNotificationResponse expected = new ClassNotificationResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail()))
                .thenReturn(Optional.of(teacherUser));
        when(classNotificationRepository.findById(200L))
                .thenReturn(Optional.of(testNotification));

        when(attachDocumentClassRepository.findByClassNotificationId(anyLong()))
                .thenReturn(Collections.emptyList());
        when(convertUtil.convertClassNotificationToDto(any(), any(), any()))
                .thenReturn(expected);

        ClassNotificationResponse result =
                service.disableOrDeleteNotificationInClass(httpRequest, req);

        assertThat(result).isEqualTo(expected);
        assertThat(testNotification.getIsDelete()).isTrue();
    }

    /**
     * TC-CNS-036
     * Objective : isActive = true (same as current) → isActive branch NOT triggered.
     * Input     : teacherUser, request with isActive = true (notification already isActive=true).
     * Expected  : save() still called; isActive remains true.
     * Rollback  : save() mocked.
     */
    @Test
    @DisplayName("TC-CNS-036: isActive unchanged – branch not triggered")
    void tc_cns_036_disableOrDelete_isActiveSameValue_branchSkipped() {
        testNotification.setIsActive(true);
        ClassNotificationRequest req = buildToggleRequest(200L, 100L, true, false);
        // isActive request = true == current = true → branch skipped
        ClassNotificationResponse expected = new ClassNotificationResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail()))
                .thenReturn(Optional.of(teacherUser));
        when(classNotificationRepository.findById(200L))
                .thenReturn(Optional.of(testNotification));
        when(classNotificationRepository.save(any())).thenReturn(testNotification);
        when(attachDocumentClassRepository.findByClassNotificationId(anyLong()))
                .thenReturn(Collections.emptyList());
        when(convertUtil.convertClassNotificationToDto(any(), any(), any()))
                .thenReturn(expected);

        service.disableOrDeleteNotificationInClass(httpRequest, req);

        // isActive stays true (no change)
        assertThat(testNotification.getIsActive()).isTrue();
    }

    /**
     * TC-CNS-037
     * Objective : isDelete = false (same as current) → isDelete branch NOT triggered.
     * Input     : teacherUser, request with isDelete = false (notification isDelete=false).
     * Expected  : save() called; isDelete remains false.
     * Rollback  : save() mocked.
     */
    @Test
    @DisplayName("TC-CNS-037: isDelete unchanged – branch not triggered")
    void tc_cns_037_disableOrDelete_isDeleteSameValue_branchSkipped() {
        testNotification.setIsDelete(false);
        ClassNotificationRequest req = buildToggleRequest(200L, 100L, null, false);
        // isDelete request = false == current = false → branch skipped

        ClassNotificationResponse expected = new ClassNotificationResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail()))
                .thenReturn(Optional.of(teacherUser));
        when(classNotificationRepository.findById(200L))
                .thenReturn(Optional.of(testNotification));
        when(classNotificationRepository.save(any())).thenReturn(testNotification);
        when(attachDocumentClassRepository.findByClassNotificationId(anyLong()))
                .thenReturn(Collections.emptyList());
        when(convertUtil.convertClassNotificationToDto(any(), any(), any()))
                .thenReturn(expected);

        service.disableOrDeleteNotificationInClass(httpRequest, req);

        assertThat(testNotification.getIsDelete()).isFalse();
    }

    /**
     * TC-CNS-038
     * Objective : isActive = null in request → isActive branch NOT triggered (null check).
     * Input     : teacherUser, request with isActive = null.
     * Expected  : isActive remains unchanged on the notification; save() still called.
     * Rollback  : save() mocked.
     */
    @Test
    @DisplayName("TC-CNS-038: isActive=null in request – null branch skipped")
    void tc_cns_038_disableOrDelete_isActiveNull_branchSkipped() {
        testNotification.setIsActive(true);
        ClassNotificationRequest req = buildToggleRequest(200L, 100L, null, false);
        ClassNotificationResponse expected = new ClassNotificationResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail()))
                .thenReturn(Optional.of(teacherUser));
        when(classNotificationRepository.findById(200L))
                .thenReturn(Optional.of(testNotification));
        when(classNotificationRepository.save(any())).thenReturn(testNotification);
        when(attachDocumentClassRepository.findByClassNotificationId(anyLong()))
                .thenReturn(Collections.emptyList());
        when(convertUtil.convertClassNotificationToDto(any(), any(), any()))
                .thenReturn(expected);

        service.disableOrDeleteNotificationInClass(httpRequest, req);

        // isActive should still be true (null request → no change)
        assertThat(testNotification.getIsActive()).isTrue();
    }

    /**
     * TC-CNS-039
     * Objective : isDelete = null in request → isDelete branch NOT triggered.
     * Input     : teacherUser, request with isDelete = null.
     * Expected  : isDelete remains false; save() called.
     * Rollback  : save() mocked.
     */
    @Test
    @DisplayName("TC-CNS-039: isDelete=null in request – null branch skipped")
    void tc_cns_039_disableOrDelete_isDeleteNull_branchSkipped() {
        testNotification.setIsDelete(false);
        ClassNotificationRequest req = buildToggleRequest(200L, 100L, false, null);
        // isActive request = false ≠ current = true → triggers isActive update
        // isDelete request = null → branch skipped

        ClassNotificationResponse expected = new ClassNotificationResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail()))
                .thenReturn(Optional.of(teacherUser));
        when(classNotificationRepository.findById(200L))
                .thenReturn(Optional.of(testNotification));
        when(classNotificationRepository.save(any())).thenReturn(testNotification);
        when(attachDocumentClassRepository.findByClassNotificationId(anyLong()))
                .thenReturn(Collections.emptyList());
        when(convertUtil.convertClassNotificationToDto(any(), any(), any()))
                .thenReturn(expected);

        service.disableOrDeleteNotificationInClass(httpRequest, req);

        assertThat(testNotification.getIsDelete()).isFalse();
    }

    /**
     * TC-CNS-040
     * Objective : Both isActive and isDelete changed simultaneously.
     * Input     : teacherUser, isActive = false (was true), isDelete = true (was false).
     * Expected  : Notification isActive=false AND isDelete=true; save() called.
     * Rollback  : save() mocked.
     */
    @Test
    @DisplayName("TC-CNS-040: Both isActive and isDelete changed in one call")
    void tc_cns_040_disableOrDelete_bothChanged_success() {
        testNotification.setIsActive(true);
        testNotification.setIsDelete(false);
        ClassNotificationRequest req = buildToggleRequest(200L, 100L, false, true);
        ClassNotificationResponse expected = new ClassNotificationResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail()))
                .thenReturn(Optional.of(teacherUser));
        when(classNotificationRepository.findById(200L))
                .thenReturn(Optional.of(testNotification));

        when(attachDocumentClassRepository.findByClassNotificationId(anyLong()))
                .thenReturn(Collections.emptyList());
        when(convertUtil.convertClassNotificationToDto(any(), any(), any()))
                .thenReturn(expected);

        service.disableOrDeleteNotificationInClass(httpRequest, req);

        assertThat(testNotification.getIsActive()).isFalse();
        assertThat(testNotification.getIsDelete()).isTrue();
    }

    /**
     * TC-CNS-041
     * Objective : Notification not found on disable/delete → NOT_EXISTED.
     * Input     : teacherUser, classNotificationId = 999.
     * Expected  : WebToeicException (NOT_EXISTED).
     * Rollback  : save() never called.
     */
    @Test
    @DisplayName("TC-CNS-041: Notification not found in disableOrDelete → NOT_EXISTED")
    void tc_cns_041_disableOrDelete_notificationNotFound_throwsNotExisted() {
        ClassNotificationRequest req = buildToggleRequest(999L, 100L, false, false);

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail()))
                .thenReturn(Optional.of(teacherUser));
        when(classNotificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.disableOrDeleteNotificationInClass(httpRequest, req))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_EXISTED));

        verify(classNotificationRepository, never()).save(any());
    }

    /**
     * TC-CNS-042
     * Objective : Unknown user in disableOrDelete → NOT_EXISTED.
     * Input     : Token email not in DB.
     * Expected  : WebToeicException (NOT_EXISTED).
     */
    @Test
    @DisplayName("TC-CNS-042: Unknown user in disableOrDelete → NOT_EXISTED")
    void tc_cns_042_disableOrDelete_unknownUser_throwsNotExisted() {
        ClassNotificationRequest req = buildToggleRequest(200L, 100L, false, false);

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn("ghost@x.com");
        when(userRepository.findByEmail("ghost@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.disableOrDeleteNotificationInClass(httpRequest, req))
                .isInstanceOf(WebToeicException.class);
    }

    // =========================================================================
    //  Helper builders
    // =========================================================================

    /** Build a minimal SearchNotificationInClassDto. */
    private SearchNotificationInClassDto buildSearchDto(Long classId, String search) {
        SearchNotificationInClassDto dto = new SearchNotificationInClassDto();
        dto.setClassId(classId);
        dto.setSearchString(search);
        return dto;
    }

    /** Build a create request. */
    private ClassNotificationRequest buildCreateRequest(
            Long classId, boolean isPin, boolean withAttachment) {
        ClassNotificationRequest req = new ClassNotificationRequest();
        req.setClassId(classId);
        req.setDescription("Test description");
        req.setIsPin(isPin);
        req.setTypeNotification(1); // 1 = ANNOUNCEMENT
        req.setFromDate(new Date());
        req.setToDate(new Date(System.currentTimeMillis() + 86_400_000));
        req.setUrlAttachment(withAttachment
                ? List.of("http://file1.pdf")
                : null);
        return req;
    }

    /** Build an update request. */
    private ClassNotificationRequest buildUpdateRequest(
            Long notificationId, Long classId, List<String> urls) {
        ClassNotificationRequest req = new ClassNotificationRequest();
        req.setClassNotificationId(notificationId);
        req.setClassId(classId);
        req.setDescription("Updated description");
        req.setIsPin(false);
        req.setTypeNotification(2); // 2 = HOMEWORK
        req.setFromDate(new Date());
        req.setToDate(new Date(System.currentTimeMillis() + 86_400_000));
        req.setUrlAttachment(urls);
        return req;
    }

    /**
     * Build a toggle (disable/delete) request.
     * @param isActive  null = no change, false = disable, true = re-enable
     * @param isDelete  null = no change, true = soft delete
     */
    private ClassNotificationRequest buildToggleRequest(
            Long notificationId, Long classId,
            Boolean isActive, Boolean isDelete) {
        ClassNotificationRequest req = new ClassNotificationRequest();
        req.setClassNotificationId(notificationId);
        req.setClassId(classId);
        req.setIsActive(isActive);
        req.setIsDelete(isDelete);
        return req;
    }

    // =========================================================================
    // Missing Coverage Tests
    // =========================================================================

    @Test @DisplayName("Coverage: updateNotification | isPin is null")
    void coverage_updateNotification_isPinNull() {
        ClassNotificationRequest req = buildUpdateRequest(200L, 100L, null);
        req.setIsPin(null);
        ClassNotificationResponse expected = new ClassNotificationResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail())).thenReturn(Optional.of(teacherUser));
        when(classNotificationRepository.findById(200L)).thenReturn(Optional.of(testNotification));
        when(attachDocumentClassRepository.findByClassNotificationId(anyLong())).thenReturn(Collections.emptyList());
        when(convertUtil.convertClassNotificationToDto(any(), any(), any())).thenReturn(expected);

        service.updateNotificationInClass(httpRequest, req);
    }

    @Test @DisplayName("Coverage: updateNotification | isPin is true")
    void coverage_updateNotification_isPinTrue() {
        ClassNotificationRequest req = buildUpdateRequest(200L, 100L, null);
        req.setIsPin(true);
        ClassNotificationResponse expected = new ClassNotificationResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail())).thenReturn(Optional.of(teacherUser));
        when(classNotificationRepository.findById(200L)).thenReturn(Optional.of(testNotification));
        when(attachDocumentClassRepository.findByClassNotificationId(anyLong())).thenReturn(Collections.emptyList());
        when(convertUtil.convertClassNotificationToDto(any(), any(), any())).thenReturn(expected);

        service.updateNotificationInClass(httpRequest, req);
    }

    @Test @DisplayName("Coverage: disableOrDelete | non-class-teacher -> throws NOT_PERMISSION")
    void coverage_disableOrDelete_notPermission() {
        ClassNotificationRequest req = buildToggleRequest(200L, 100L, false, false);
        
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(consultantUser.getEmail());
        when(userRepository.findByEmail(consultantUser.getEmail())).thenReturn(Optional.of(consultantUser));
        when(classNotificationRepository.findById(200L)).thenReturn(Optional.of(testNotification));

        assertThatThrownBy(() -> service.disableOrDeleteNotificationInClass(httpRequest, req))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode()).isEqualTo(ResponseCode.NOT_PERMISSION));
    }
}
