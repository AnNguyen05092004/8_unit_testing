package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.*;
import com.doan2025.webtoeic.domain.*;
import com.doan2025.webtoeic.domain.Class;
import com.doan2025.webtoeic.dto.SearchMemberInClassDto;
import com.doan2025.webtoeic.dto.request.ClassRequest;
import com.doan2025.webtoeic.dto.response.ClassMemberResponse;
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
import static org.mockito.Mockito.*;

/**
 * Unit Test - ClassMemberServiceImpl
 * Target JaCoCo: 100% instruction | 100% branch
 */
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(MethodOrderer.DisplayName.class)
class ClassMemberServiceImplTest {

    @Mock
    private ClassMemberRepository classMemberRepository;
    @Mock
    private ClassRepository classRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotiUtils notiUtils;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private ConvertUtil convertUtil;

    @InjectMocks
    private ClassMemberServiceImpl sut;

    @Mock
    private HttpServletRequest httpReq;

    /* builders */

    private User user(Long id, String email, ERole role) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setRole(role);
        u.setFirstName("F");
        u.setLastName("L");
        return u;
    }

    private Class clazz(Long id) {
        Class c = new Class();
        c.setId(id);
        c.setName("C" + id);
        return c;
    }

    private ClassMember member(User u, Class c, EJoinStatus s) {
        ClassMember m = new ClassMember();
        m.setMember(u);
        m.setClazz(c);
        m.setStatus(s);
        return m;
    }

    private void stubUser(User u) {
        when(jwtUtil.getEmailFromToken(httpReq)).thenReturn(u.getEmail());
        when(userRepository.findByEmail(u.getEmail())).thenReturn(Optional.of(u));
    }

    /* getMemberInClass */

    /**
     * <pre>
     * File / Class     : ClassMemberServiceImplTest
     * Method Under Test: ClassMemberServiceImpl#getMemberInClass(HttpServletRequest, SearchMemberInClassDto, Pageable)
     * Test Case ID     : CM-01
     * Test Objective   : Verify that when the JWT token resolves to an email with no matching
     *                    user record, a NOT_EXISTED WebToeicException is thrown and no
     *                    classMemberRepository call is made.
     * Input            : JWT email: "ghost@t.com"
     *                    userRepository.findByEmail("ghost@t.com") -> Optional.empty()
     *                    SearchMemberInClassDto (empty), PageRequest(0, 10)
     * Expected Output  : WebToeicException thrown (orElseThrow -> NOT_EXISTED);
     *                    classMemberRepository has zero interactions.
     * Notes            : Use Mockito when(jwtUtil.getEmailFromToken(httpReq)).thenReturn("ghost@t.com")
     *                    and when(userRepository.findByEmail("ghost@t.com")).thenReturn(Optional.empty())
     *                    to simulate an unrecognised caller. Use assertThatThrownBy to verify
     *                    WebToeicException is thrown, and verifyNoInteractions(classMemberRepository)
     *                    to confirm the repository layer is never reached.
     * </pre>
     */
    @Test
    @DisplayName("CM-01 | getMemberInClass | user not found -> NOT_EXISTED")
    void cm01() {
        when(jwtUtil.getEmailFromToken(httpReq)).thenReturn("ghost@t.com");
        when(userRepository.findByEmail("ghost@t.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.getMemberInClass(httpReq, new SearchMemberInClassDto(), PageRequest.of(0, 10)))
                .isInstanceOf(WebToeicException.class);
        verifyNoInteractions(classMemberRepository);
    }

    /**
     * <pre>
     * File / Class     : ClassMemberServiceImplTest
     * Method Under Test: ClassMemberServiceImpl#getMemberInClass(HttpServletRequest, SearchMemberInClassDto, Pageable)
     * Test Case ID     : CM-02
     * Test Objective   : Verify that a null status field is kept null (isNull branch=TRUE),
     *                    that the STUDENT role causes the caller's email to be passed to the
     *                    repository, and that the converter is called with the exact ClassMember
     *                    instance returned by the repository.
     * Input            : Current user: STUDENT (id=1, email="s@t.com")
     *                    SearchMemberInClassDto { status=null }
     *                    classMemberRepository.findMembersInClass -> Page[ClassMember cm]
     *                    convertUtil.convertClassMemberToDto(httpReq, cm) -> ClassMemberResponse
     * Expected Output  : dto.status remains null; result has 1 element equal to the returned
     *                    ClassMemberResponse; convertUtil called with the exact cm instance.
     * Notes            : Use Mockito when(classMemberRepository.findMembersInClass(dto, "s@t.com", pg))
     *                    .thenReturn(new PageImpl<>(List.of(cm))) to supply a non-empty page, and
     *                    when(convertUtil.convertClassMemberToDto(eq(httpReq), eq(cm))).thenReturn(resp)
     *                    to supply the DTO. Use verify(convertUtil).convertClassMemberToDto(eq(httpReq), eq(cm))
     *                    with a pinned argument matcher (eq) to catch any regression where the wrong
     *                    object is passed to the converter (FIX-3).
     * </pre>
     */
    @Test
    @DisplayName("CM-02 | getMemberInClass | status=null, STUDENT -> set null + email + convert verified")
    void cm02() {
        User s = user(1L, "s@t.com", ERole.STUDENT);
        stubUser(s);
        Class c = clazz(1L);
        ClassMember cm = member(s, c, EJoinStatus.ACTIVE);

        SearchMemberInClassDto dto = new SearchMemberInClassDto();
        dto.setStatus(null);
        Pageable pg = PageRequest.of(0, 10);

        ClassMemberResponse resp = new ClassMemberResponse();
        when(classMemberRepository.findMembersInClass(dto, "s@t.com", pg))
                .thenReturn(new PageImpl<>(List.of(cm)));
        when(convertUtil.convertClassMemberToDto(eq(httpReq), eq(cm))).thenReturn(resp);

        Page<ClassMemberResponse> result = sut.getMemberInClass(httpReq, dto, pg);

        assertThat(dto.getStatus()).isNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0)).isSameAs(resp);
        verify(convertUtil).convertClassMemberToDto(eq(httpReq), eq(cm));
    }

    /**
     * <pre>
     * File / Class     : ClassMemberServiceImplTest
     * Method Under Test: ClassMemberServiceImpl#getMemberInClass(HttpServletRequest, SearchMemberInClassDto, Pageable)
     * Test Case ID     : CM-03
     * Test Objective   : Verify that an empty status list is treated the same as null (isEmpty
     *                    branch=TRUE -> setNull), and that the STUDENT role routes the caller's
     *                    email to the repository query.
     * Input            : Current user: STUDENT (id=2, email="s2@t.com")
     *                    SearchMemberInClassDto { status=[] (empty list) }
     *                    classMemberRepository.findMembersInClass -> Page.empty()
     * Expected Output  : dto.status is null after the call; repository invoked with email="s2@t.com".
     * Notes            : Use Mockito when(classMemberRepository.findMembersInClass(any(), eq("s2@t.com"), eq(pg)))
     *                    .thenReturn(Page.empty(pg)) to stub the query. Use AssertJ assertThat(dto.getStatus())
     *                    .isNull() to confirm the isEmpty guard set status to null, and
     *                    verify(classMemberRepository).findMembersInClass(any(), eq("s2@t.com"), eq(pg))
     *                    to confirm the STUDENT email branch was taken.
     * </pre>
     */
    @Test
    @DisplayName("CM-03 | getMemberInClass | status=[], STUDENT -> set null + email branch")
    void cm03() {
        User s = user(2L, "s2@t.com", ERole.STUDENT);
        stubUser(s);
        SearchMemberInClassDto dto = new SearchMemberInClassDto();
        dto.setStatus(new ArrayList<>());
        Pageable pg = PageRequest.of(0, 10);
        when(classMemberRepository.findMembersInClass(any(), eq("s2@t.com"), eq(pg))).thenReturn(Page.empty(pg));

        sut.getMemberInClass(httpReq, dto, pg);

        assertThat(dto.getStatus()).isNull();
        verify(classMemberRepository).findMembersInClass(any(), eq("s2@t.com"), eq(pg));
    }

    /**
     * <pre>
     * File / Class     : ClassMemberServiceImplTest
     * Method Under Test: ClassMemberServiceImpl#getMemberInClass(HttpServletRequest, SearchMemberInClassDto, Pageable)
     * Test Case ID     : CM-04
     * Test Objective   : Verify that a non-null, non-empty status list is left unchanged
     *                    (both isNull and isEmpty branches = FALSE), that the STUDENT email
     *                    branch is taken, and that the page map() / converter lambda actually
     *                    executes for each returned ClassMember.
     * Input            : Current user: STUDENT (id=3, email="s3@t.com")
     *                    SearchMemberInClassDto { status=["ACTIVE"] }
     *                    classMemberRepository.findMembersInClass -> Page[ClassMember cm]
     *                    convertUtil.convertClassMemberToDto(httpReq, cm) -> ClassMemberResponse
     * Expected Output  : dto.status is not null; result has 1 element; convertUtil called once
     *                    with the exact cm instance (map() lambda ran).
     * Notes            : Use Mockito when(classMemberRepository.findMembersInClass(any(), eq("s3@t.com"), eq(pg)))
     *                    .thenReturn(new PageImpl<>(List.of(cm))) to return a non-empty page, ensuring
     *                    the map() lambda actually runs (FIX-4 — returning Page.empty() would leave the
     *                    converter instruction dead and miss the branch). Use
     *                    verify(convertUtil).convertClassMemberToDto(eq(httpReq), eq(cm)) to confirm
     *                    both the STUDENT email branch and the converter invocation.
     * </pre>
     */
    @Test
    @DisplayName("CM-04 | getMemberInClass | status=[ACTIVE], STUDENT -> keep + email + map() runs")
    void cm04() {
        User s = user(3L, "s3@t.com", ERole.STUDENT);
        stubUser(s);
        Class c = clazz(1L);
        ClassMember cm = member(s, c, EJoinStatus.ACTIVE);

        SearchMemberInClassDto dto = new SearchMemberInClassDto();
        dto.setStatus(List.of("ACTIVE"));
        Pageable pg = PageRequest.of(0, 10);

        ClassMemberResponse resp = new ClassMemberResponse();
        when(classMemberRepository.findMembersInClass(any(), eq("s3@t.com"), eq(pg)))
                .thenReturn(new PageImpl<>(List.of(cm)));
        when(convertUtil.convertClassMemberToDto(eq(httpReq), eq(cm))).thenReturn(resp);

        Page<ClassMemberResponse> result = sut.getMemberInClass(httpReq, dto, pg);

        assertThat(dto.getStatus()).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(convertUtil).convertClassMemberToDto(eq(httpReq), eq(cm));
    }

    /**
     * <pre>
     * File / Class     : ClassMemberServiceImplTest
     * Method Under Test: ClassMemberServiceImpl#getMemberInClass(HttpServletRequest, SearchMemberInClassDto, Pageable)
     * Test Case ID     : CM-05
     * Test Objective   : Verify that a MANAGER caller triggers the else branch, passing null
     *                    as the email argument to the repository (no personal email filter).
     * Input            : Current user: MANAGER (id=4, email="m@t.com")
     *                    SearchMemberInClassDto (empty), PageRequest(0, 10)
     *                    classMemberRepository.findMembersInClass(any(), null, pg) -> Page.empty()
     * Expected Output  : classMemberRepository called with email=null; never called with "m@t.com".
     * Notes            : Use Mockito when(classMemberRepository.findMembersInClass(any(), isNull(), eq(pg)))
     *                    .thenReturn(Page.empty(pg)) to stub the null-email path. Use
     *                    verify(classMemberRepository).findMembersInClass(any(), isNull(), eq(pg)) to
     *                    confirm null was passed, and verify(classMemberRepository, never())
     *                    .findMembersInClass(any(), eq("m@t.com"), any()) to confirm the MANAGER role
     *                    never leaks the caller's personal email into the query.
     * </pre>
     */
    @Test
    @DisplayName("CM-05 | getMemberInClass | MANAGER -> null email branch")
    void cm05() {
        User m = user(4L, "m@t.com", ERole.MANAGER);
        stubUser(m);
        Pageable pg = PageRequest.of(0, 10);
        when(classMemberRepository.findMembersInClass(any(), isNull(), eq(pg))).thenReturn(Page.empty(pg));

        sut.getMemberInClass(httpReq, new SearchMemberInClassDto(), pg);

        verify(classMemberRepository).findMembersInClass(any(), isNull(), eq(pg));
        verify(classMemberRepository, never()).findMembersInClass(any(), eq("m@t.com"), any());
    }

    /**
     * <pre>
     * File / Class     : ClassMemberServiceImplTest
     * Method Under Test: ClassMemberServiceImpl#getMemberInClass(HttpServletRequest, SearchMemberInClassDto, Pageable)
     * Test Case ID     : CM-06
     * Test Objective   : Verify that a CONSULTANT caller triggers the else branch, passing null
     *                    as the email argument to the repository.
     * Input            : Current user: CONSULTANT (id=5, email="c@t.com")
     *                    SearchMemberInClassDto (empty), PageRequest(0, 10)
     *                    classMemberRepository.findMembersInClass(any(), null, pg) -> Page.empty()
     * Expected Output  : classMemberRepository called with email=null.
     * Notes            : Use Mockito when(classMemberRepository.findMembersInClass(any(), isNull(), eq(pg)))
     *                    .thenReturn(Page.empty(pg)) to stub the null-email path. Use
     *                    verify(classMemberRepository).findMembersInClass(any(), isNull(), eq(pg))
     *                    to confirm the CONSULTANT role follows the same else branch as MANAGER,
     *                    passing null instead of the caller's own email into the query.
     * </pre>
     */
    @Test
    @DisplayName("CM-06 | getMemberInClass | CONSULTANT -> null email branch")
    void cm06() {
        User c = user(5L, "c@t.com", ERole.CONSULTANT);
        stubUser(c);
        Pageable pg = PageRequest.of(0, 10);
        when(classMemberRepository.findMembersInClass(any(), isNull(), eq(pg))).thenReturn(Page.empty(pg));

        sut.getMemberInClass(httpReq, new SearchMemberInClassDto(), pg);

        verify(classMemberRepository).findMembersInClass(any(), isNull(), eq(pg));
    }

    /**
     * <pre>
     * File / Class     : ClassMemberServiceImplTest
     * Method Under Test: ClassMemberServiceImpl#getMemberInClass(HttpServletRequest, SearchMemberInClassDto, Pageable)
     * Test Case ID     : CM-07
     * Test Objective   : Verify that a TEACHER caller triggers the else branch (email=null) and
     *                    that the converter is invoked for each ClassMember in the returned page.
     * Input            : Current user: TEACHER (id=6, email="t@t.com")
     *                    SearchMemberInClassDto (empty), PageRequest(0, 10)
     *                    classMemberRepository.findMembersInClass(any(), null, pg) -> Page[ClassMember cm]
     *                    convertUtil.convertClassMemberToDto(any(), cm) -> ClassMemberResponse
     * Expected Output  : classMemberRepository called with email=null; convertUtil called once
     *                    with the exact cm instance.
     * Notes            : Use Mockito when(classMemberRepository.findMembersInClass(any(), isNull(), eq(pg)))
     *                    .thenReturn(new PageImpl<>(List.of(cm))) to return a non-empty page.
     *                    Use when(convertUtil.convertClassMemberToDto(any(), eq(cm))).thenReturn(new ClassMemberResponse())
     *                    and verify(convertUtil).convertClassMemberToDto(any(), eq(cm)) to confirm both
     *                    the null-email branch (TEACHER role -> else) and that the map() lambda ran
     *                    and invoked the converter exactly once.
     * </pre>
     */
    @Test
    @DisplayName("CM-07 | getMemberInClass | TEACHER -> null email + convert called")
    void cm07() {
        User t = user(6L, "t@t.com", ERole.TEACHER);
        stubUser(t);
        Class c = clazz(1L);
        ClassMember cm = new ClassMember();
        Pageable pg = PageRequest.of(0, 10);

        when(classMemberRepository.findMembersInClass(any(), isNull(), eq(pg)))
                .thenReturn(new PageImpl<>(List.of(cm)));
        when(convertUtil.convertClassMemberToDto(any(), eq(cm))).thenReturn(new ClassMemberResponse());

        sut.getMemberInClass(httpReq, new SearchMemberInClassDto(), pg);

        verify(classMemberRepository).findMembersInClass(any(), isNull(), eq(pg));
        verify(convertUtil).convertClassMemberToDto(any(), eq(cm));
    }

    /* addUserToClass */

    /**
     * <pre>
     * File / Class     : ClassMemberServiceImplTest
     * Method Under Test: ClassMemberServiceImpl#addUserToClass(HttpServletRequest, ClassRequest)
     * Test Case ID     : CM-08
     * Test Objective   : Verify that when the requested class ID does not exist in the repository,
     *                    a NOT_EXISTED WebToeicException is thrown before any save occurs.
     * Input            : ClassRequest { id=999, memberIds=[1] }
     *                    classRepository.findById(999) -> Optional.empty()
     * Expected Output  : WebToeicException thrown (orElseThrow -> NOT_EXISTED);
     *                    classMemberRepository.save() never invoked.
     * Notes            : Use Mockito when(classRepository.findById(999L)).thenReturn(Optional.empty())
     *                    to simulate a non-existent class. Use assertThatThrownBy to verify
     *                    WebToeicException is thrown, and verify(classMemberRepository, never()).save(any())
     *                    to confirm no membership record is written when the class lookup fails.
     * </pre>
     */
    @Test
    @DisplayName("CM-08 | addUserToClass | class not found -> NOT_EXISTED")
    void cm08() {
        ClassRequest cr = new ClassRequest();
        cr.setId(999L);
        cr.setMemberIds(List.of(1L));
        when(classRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.addUserToClass(httpReq, cr)).isInstanceOf(WebToeicException.class);
        verify(classMemberRepository, never()).save(any());
    }

    /**
     * <pre>
     * File / Class     : ClassMemberServiceImplTest
     * Method Under Test: ClassMemberServiceImpl#addUserToClass(HttpServletRequest, ClassRequest)
     * Test Case ID     : CM-09
     * Test Objective   : Verify that when a user ID inside the memberIds loop does not exist,
     *                    a NOT_EXISTED WebToeicException is thrown before any save occurs.
     * Input            : ClassRequest { id=1, memberIds=[777] }
     *                    classRepository.findById(1) -> Class(id=1)
     *                    userRepository.findById(777) -> Optional.empty()
     * Expected Output  : WebToeicException thrown (orElseThrow inside loop -> NOT_EXISTED);
     *                    classMemberRepository.save() never invoked.
     * Notes            : Use Mockito when(classRepository.findById(1L)).thenReturn(Optional.of(c))
     *                    to pass the class guard, then when(userRepository.findById(777L)).thenReturn
     *                    (Optional.empty()) to fail inside the loop. Use assertThatThrownBy to verify
     *                    the exception and verify(classMemberRepository, never()).save(any()) to confirm
     *                    no partial write occurs when any member lookup fails.
     * </pre>
     */
    @Test
    @DisplayName("CM-09 | addUserToClass | userId not found -> NOT_EXISTED")
    void cm09() {
        Class c = clazz(1L);
        when(classRepository.findById(1L)).thenReturn(Optional.of(c));
        ClassRequest cr = new ClassRequest();
        cr.setId(1L);
        cr.setMemberIds(List.of(777L));
        when(userRepository.findById(777L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.addUserToClass(httpReq, cr)).isInstanceOf(WebToeicException.class);
        verify(classMemberRepository, never()).save(any());
    }

    /**
     * <pre>
     * File / Class     : ClassMemberServiceImplTest
     * Method Under Test: ClassMemberServiceImpl#addUserToClass(HttpServletRequest, ClassRequest)
     * Test Case ID     : CM-10
     * Test Objective   : Verify that when an existing ClassMember with status=DROPPED is found,
     *                    its status is mutated to ACTIVE and saved, and that sendNoti is NOT
     *                    called (notification is only for newly created members).
     * Input            : ClassRequest { id=1, memberIds=[10] }
     *                    classRepository.findById(1) -> Class(id=1)
     *                    userRepository.findById(10) -> User(id=10)
     *                    classMemberRepository.findByClassAndMember(10, 1) -> ClassMember(status=DROPPED)
     * Expected Output  : existing.status == ACTIVE; classMemberRepository.save(existing) called once;
     *                    notiUtils.sendNoti() never invoked.
     * Notes            : Use Mockito when(classMemberRepository.findByClassAndMember(10L, 1L)).thenReturn
     *                    (existingDropped) to simulate a DROPPED record. Use AssertJ
     *                    assertThat(existing.getStatus()).isEqualTo(EJoinStatus.ACTIVE) to verify the
     *                    in-place status mutation, verify(classMemberRepository).save(existing) to
     *                    confirm the save, and verify(notiUtils, never()).sendNoti(...) to confirm no
     *                    notification is sent for reactivated members (FIX-1: production only notifies
     *                    brand-new members; the dead doNothing() stub was removed).
     * </pre>
     */
    @Test
    @DisplayName("CM-10 | addUserToClass | DROPPED -> reactivate + save; sendNoti NOT called")
    void cm10() {
        Class c = clazz(1L);
        User u = user(10L, "u@t.com", ERole.STUDENT);
        ClassMember existing = member(u, c, EJoinStatus.DROPPED);

        when(classRepository.findById(1L)).thenReturn(Optional.of(c));
        when(userRepository.findById(10L)).thenReturn(Optional.of(u));
        when(classMemberRepository.findByClassAndMember(10L, 1L)).thenReturn(existing);

        ClassRequest cr = new ClassRequest();
        cr.setId(1L);
        cr.setMemberIds(List.of(10L));
        sut.addUserToClass(httpReq, cr);

        assertThat(existing.getStatus()).isEqualTo(EJoinStatus.ACTIVE);
        verify(classMemberRepository).save(existing);
        verify(notiUtils, never()).sendNoti(anyList(), any(), anyString(), anyString(), anyLong());
    }

    /**
     * <pre>
     * File / Class     : ClassMemberServiceImplTest
     * Method Under Test: ClassMemberServiceImpl#addUserToClass(HttpServletRequest, ClassRequest)
     * Test Case ID     : CM-11
     * Test Objective   : Verify that when an existing ClassMember with status=ACTIVE is found,
     *                    no save is performed and sendNoti is not called (already enrolled).
     * Input            : ClassRequest { id=1, memberIds=[11] }
     *                    classRepository.findById(1) -> Class(id=1)
     *                    userRepository.findById(11) -> User(id=11)
     *                    classMemberRepository.findByClassAndMember(11, 1) -> ClassMember(status=ACTIVE)
     * Expected Output  : classMemberRepository.save() never called for the existing record;
     *                    notiUtils.sendNoti() never invoked.
     * Notes            : Use Mockito when(classMemberRepository.findByClassAndMember(11L, 1L)).thenReturn
     *                    (existingActive) to simulate an already-active membership. Use
     *                    verify(classMemberRepository, never()).save(existing) to confirm the skip-save
     *                    branch is taken, and verify(notiUtils, never()).sendNoti(...) to confirm no
     *                    notification is sent for already-ACTIVE members (FIX-1: dead doNothing() stub removed).
     * </pre>
     */
    @Test
    @DisplayName("CM-11 | addUserToClass | ACTIVE -> skip save; sendNoti NOT called")
    void cm11() {
        Class c = clazz(1L);
        User u = user(11L, "u2@t.com", ERole.STUDENT);
        ClassMember existing = member(u, c, EJoinStatus.ACTIVE);

        when(classRepository.findById(1L)).thenReturn(Optional.of(c));
        when(userRepository.findById(11L)).thenReturn(Optional.of(u));
        when(classMemberRepository.findByClassAndMember(11L, 1L)).thenReturn(existing);

        ClassRequest cr = new ClassRequest();
        cr.setId(1L);
        cr.setMemberIds(List.of(11L));
        sut.addUserToClass(httpReq, cr);

        verify(classMemberRepository, never()).save(existing);
        verify(notiUtils, never()).sendNoti(anyList(), any(), anyString(), anyString(), anyLong());
    }

    /**
     * <pre>
     * File / Class     : ClassMemberServiceImplTest
     * Method Under Test: ClassMemberServiceImpl#addUserToClass(HttpServletRequest, ClassRequest)
     * Test Case ID     : CM-12
     * Test Objective   : Verify that when no existing ClassMember is found for a user, a new
     *                    ACTIVE record is created and saved, and sendNoti is called with the
     *                    correct class ID.
     * Input            : ClassRequest { id=1, memberIds=[12] }
     *                    classRepository.findById(1) -> Class(id=1)
     *                    userRepository.findById(12) -> User(id=12)
     *                    classMemberRepository.findByClassAndMember(12, 1) -> null
     *                    classMemberRepository.save(any) -> returns the saved entity
     * Expected Output  : classMemberRepository.save() called with ClassMember { member.id=12,
     *                    status=ACTIVE, clazz.id=1 }; notiUtils.sendNoti() called once with classId=1.
     * Notes            : Use Mockito when(classMemberRepository.findByClassAndMember(12L, 1L)).thenReturn(null)
     *                    to simulate no prior membership. Use when(classMemberRepository.save(any()))
     *                    .thenAnswer(i -> i.getArgument(0)) to return the saved entity.
     *                    Use ArgumentCaptor<Long> with doNothing().when(notiUtils).sendNoti(anyList(), any(),
     *                    anyString(), anyString(), classIdCaptor.capture()) to capture the classId argument
     *                    and assert it equals 1L (FIX-2), catching regressions where the wrong class ID
     *                    is passed to sendNoti. Use verify(classMemberRepository).save(argThat(...)) to
     *                    assert the correct entity state before persistence.
     * </pre>
     */
    @Test
    @DisplayName("CM-12 | addUserToClass | null existing -> new ACTIVE + save + sendNoti with classId verified")
    void cm12() {
        Class c = clazz(1L);
        User u = user(12L, "u3@t.com", ERole.STUDENT);

        when(classRepository.findById(1L)).thenReturn(Optional.of(c));
        when(userRepository.findById(12L)).thenReturn(Optional.of(u));
        when(classMemberRepository.findByClassAndMember(12L, 1L)).thenReturn(null);
        when(classMemberRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Long> classIdCaptor = ArgumentCaptor.forClass(Long.class);
        doNothing().when(notiUtils).sendNoti(anyList(), any(), anyString(), anyString(), classIdCaptor.capture());

        ClassRequest cr = new ClassRequest();
        cr.setId(1L);
        cr.setMemberIds(List.of(12L));
        sut.addUserToClass(httpReq, cr);

        verify(classMemberRepository).save(argThat(m -> m.getMember().getId().equals(12L)
                && m.getStatus() == EJoinStatus.ACTIVE
                && m.getClazz().getId().equals(1L)));
        verify(notiUtils).sendNoti(anyList(), any(), anyString(), anyString(), anyLong());
        assertThat(classIdCaptor.getValue()).isEqualTo(1L);
    }

    /* removeUserFromClass */

    /**
     * <pre>
     * File / Class     : ClassMemberServiceImplTest
     * Method Under Test: ClassMemberServiceImpl#removeUserFromClass(HttpServletRequest, ClassRequest)
     * Test Case ID     : CM-13
     * Test Objective   : Verify that when the repository returns an empty list for the given
     *                    class and user IDs, a NOT_EXISTED WebToeicException is thrown before
     *                    any save occurs.
     * Input            : ClassRequest { id=1, memberIds=[99] }
     *                    classMemberRepository.findByClassAndUser(cr) -> [] (empty)
     * Expected Output  : WebToeicException thrown (isEmpty=TRUE -> NOT_EXISTED);
     *                    classMemberRepository.save() never invoked.
     * Notes            : Use Mockito when(classMemberRepository.findByClassAndUser(cr)).thenReturn
     *                    (Collections.emptyList()) to simulate no matching members found. Use
     *                    assertThatThrownBy to verify WebToeicException is thrown on the isEmpty guard,
     *                    and verify(classMemberRepository, never()).save(any()) to confirm no DROPPED
     *                    status write occurs when the member list is empty.
     * </pre>
     */
    @Test
    @DisplayName("CM-13 | removeUserFromClass | empty list -> NOT_EXISTED")
    void cm13() {
        ClassRequest cr = new ClassRequest();
        cr.setId(1L);
        cr.setMemberIds(List.of(99L));
        when(classMemberRepository.findByClassAndUser(cr)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> sut.removeUserFromClass(httpReq, cr)).isInstanceOf(WebToeicException.class);
        verify(classMemberRepository, never()).save(any());
    }

    /**
     * <pre>
     * File / Class     : ClassMemberServiceImplTest
     * Method Under Test: ClassMemberServiceImpl#removeUserFromClass(HttpServletRequest, ClassRequest)
     * Test Case ID     : CM-14
     * Test Objective   : Verify that when the repository returns a non-empty list, every
     *                    ClassMember's status is mutated to DROPPED and each is individually saved.
     * Input            : ClassRequest { id=1, memberIds=[20, 21] }
     *                    classMemberRepository.findByClassAndUser(cr) ->
     *                      [ClassMember(u1, ACTIVE), ClassMember(u2, ACTIVE)]
     * Expected Output  : m1.status == DROPPED; m2.status == DROPPED;
     *                    classMemberRepository.save() called exactly 2 times.
     * Notes            : Use Mockito when(classMemberRepository.findByClassAndUser(cr)).thenReturn(List.of(m1, m2))
     *                    to supply two ACTIVE members. Use AssertJ assertThat(m1.getStatus()).isEqualTo(DROPPED)
     *                    and assertThat(m2.getStatus()).isEqualTo(DROPPED) to verify the in-place status
     *                    mutation applied to each element by the forEach, and
     *                    verify(classMemberRepository, times(2)).save(any(ClassMember.class)) to confirm
     *                    the loop called save exactly once per member — not batched, not skipped.
     * </pre>
     */
    @Test
    @DisplayName("CM-14 | removeUserFromClass | non-empty list -> forEach DROPPED + save x2")
    void cm14() {
        Class c = clazz(1L);
        User u1 = user(20L, "a@t.com", ERole.STUDENT);
        User u2 = user(21L, "b@t.com", ERole.STUDENT);
        ClassMember m1 = member(u1, c, EJoinStatus.ACTIVE);
        ClassMember m2 = member(u2, c, EJoinStatus.ACTIVE);

        ClassRequest cr = new ClassRequest();
        cr.setId(1L);
        cr.setMemberIds(List.of(20L, 21L));
        when(classMemberRepository.findByClassAndUser(cr)).thenReturn(List.of(m1, m2));

        sut.removeUserFromClass(httpReq, cr);

        assertThat(m1.getStatus()).isEqualTo(EJoinStatus.DROPPED);
        assertThat(m2.getStatus()).isEqualTo(EJoinStatus.DROPPED);
        verify(classMemberRepository, times(2)).save(any(ClassMember.class));
    }

    /**
     * <pre>
     * File / Class     : ClassMemberServiceImplTest
     * Method Under Test: ClassMemberServiceImpl#addUserToClass(HttpServletRequest, ClassRequest)
     * Test Case ID     : CM-15
     * Test Objective   : Verify that a single call with a mixed memberIds list correctly handles
     *                    both a DROPPED member (reactivate) and a new member (create) in one loop,
     *                    with save called twice and sendNoti called exactly once for the new member only.
     * Input            : ClassRequest { id=2, memberIds=[30, 31] }
     *                    classRepository.findById(2) -> Class(id=2)
     *                    userRepository.findById(30) -> User(id=30)
     *                    userRepository.findById(31) -> User(id=31)
     *                    classMemberRepository.findByClassAndMember(30, 2) -> ClassMember(status=DROPPED)
     *                    classMemberRepository.findByClassAndMember(31, 2) -> null
     *                    classMemberRepository.save(any) -> returns the saved entity
     * Expected Output  : existingDropped.status == ACTIVE; classMemberRepository.save() called x2;
     *                    new ClassMember for id=31 saved with status=ACTIVE and clazz.id=2;
     *                    notiUtils.sendNoti() called exactly x1 with classId=2.
     * Notes            : Use Mockito to stub both user lookups (findById(30L), findById(31L)) and both
     *                    findByClassAndMember calls — returning existingDropped for id=30 and null for id=31.
     *                    Use when(classMemberRepository.save(any())).thenAnswer(i -> i.getArgument(0)) so
     *                    both saves succeed. Use ArgumentCaptor<Long> on sendNoti's classId argument and
     *                    assert classId=2 (FIX-2 pattern). Use verify(classMemberRepository, times(2)).save(any())
     *                    to confirm both loop iterations wrote, verify(notiUtils, times(1)).sendNoti(...)
     *                    to confirm exactly one notification was sent (only for new member id=31, not for
     *                    the reactivated DROPPED member id=30). Covers FIX-5 mixed-branch loop scenario.
     * </pre>
     */
    @Test
    @DisplayName("CM-15 | addUserToClass | mixed list: DROPPED reactivate + new create -> save x2, sendNoti x1")
    void cm15() {
        Class c = clazz(2L);
        User dropped = user(30L, "d@t.com", ERole.STUDENT);
        User newUser = user(31L, "n@t.com", ERole.STUDENT);
        ClassMember existingDropped = member(dropped, c, EJoinStatus.DROPPED);

        when(classRepository.findById(2L)).thenReturn(Optional.of(c));
        when(userRepository.findById(30L)).thenReturn(Optional.of(dropped));
        when(userRepository.findById(31L)).thenReturn(Optional.of(newUser));
        when(classMemberRepository.findByClassAndMember(30L, 2L)).thenReturn(existingDropped);
        when(classMemberRepository.findByClassAndMember(31L, 2L)).thenReturn(null);
        when(classMemberRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Long> classIdCaptor = ArgumentCaptor.forClass(Long.class);
        doNothing().when(notiUtils).sendNoti(anyList(), any(), anyString(), anyString(), classIdCaptor.capture());

        ClassRequest cr = new ClassRequest();
        cr.setId(2L);
        cr.setMemberIds(List.of(30L, 31L));
        sut.addUserToClass(httpReq, cr);

        assertThat(existingDropped.getStatus()).isEqualTo(EJoinStatus.ACTIVE);

        verify(classMemberRepository, times(2)).save(any(ClassMember.class));

        verify(classMemberRepository).save(argThat(m -> m.getMember().getId().equals(31L)
                && m.getStatus() == EJoinStatus.ACTIVE
                && m.getClazz().getId().equals(2L)));

        verify(notiUtils, times(1)).sendNoti(anyList(), any(), anyString(), anyString(), anyLong());
        assertThat(classIdCaptor.getValue()).isEqualTo(2L);
    }
}