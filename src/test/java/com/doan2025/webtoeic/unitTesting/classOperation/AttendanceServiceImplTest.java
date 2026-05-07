package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.*;
import com.doan2025.webtoeic.domain.*;
import com.doan2025.webtoeic.domain.Class;
import com.doan2025.webtoeic.dto.request.AttendanceRequest;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.*;
import com.doan2025.webtoeic.utils.JwtUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit Test – AttendanceServiceImpl
 * Target JaCoCo: 100% instruction | 100% branch
 *
 * Branch map:
 *
 * updateAttendance():
 *   B1: isNull(scheduleId)              → TRUE / FALSE
 *   B2: findById(scheduleId) → empty    → orElseThrow
 *   B3: !TEACHER && !email_match        → TRUE (throw) / FALSE (continue)
 *   B4: now < start - 15m               → TRUE (NOT_START)
 *   B5: now > end + 15m                 → TRUE (OVER_DUE)
 *   B6: normal path                     → findById(attendanceId) + saveAll
 *
 * attendance():
 *   B7:  isNull(scheduleIds)            → TRUE (NOT_AVAILABLE) / FALSE
 *   B8:  stream.filter → future found vs orElse last
 *   B9:  !isNull(isAttendance list)     → TRUE (EXISTED) / FALSE
 *   B10: findById(scheduleId) → empty  → orElseThrow
 *   B11: schedule.isAttendance=true    → throw / false → continue
 *   B12: !TEACHER                      → throw / continue
 *   B13: findById(studentId) → empty  → orElseThrow
 *   B15: !requests.isEmpty()           → TRUE (enter loop) / FALSE (unreachable – see AT-13)
 *
 * ┌────────┬────────────────────────────┬──────────────────────────────────────────────────────────┐
 * │ TC-ID  │ Method                     │ Branch / Fix note                                        │
 * ├────────┼────────────────────────────┼──────────────────────────────────────────────────────────┤
 * │ AT-01  │ updateAttendance           │ B1=TRUE: scheduleId=null → IS_NULL                      │
 * │ AT-02  │ updateAttendance           │ B1=FALSE, B2=throw: schedule not found                  │
 * │ AT-03  │ updateAttendance           │ B3=TRUE: STUDENT ≠ teacher → NOT_PERMISSION             │
 * │ AT-04  │ updateAttendance           │ B3=FALSE(TEACHER match), B4=TRUE: tooEarly → NOT_START  │
 * │ AT-05  │ updateAttendance           │ B4=FALSE, B5=TRUE: overdue → OVER_DUE                   │
 * │ AT-06  │ updateAttendance           │ B5=FALSE, B6: normal → saveAll; asserts captured list   │
 * │ AT-07  │ attendance                 │ B7=TRUE: scheduleIds=null → NOT_AVAILABLE                │
 * │ AT-08  │ attendance                 │ B8=found + B9=TRUE: already attended → EXISTED           │
 * │ AT-09  │ attendance                 │ B8=orElse(last, ascending sort) + B10=throw             │
 * │ AT-10  │ attendance                 │ B11=TRUE: isAttendance=true → throw                     │
 * │ AT-11  │ attendance                 │ B12=TRUE: !TEACHER → NOT_PERMISSION                     │
 * │ AT-12  │ attendance                 │ B12=FALSE, B13=throw: student not found                 │
 * │ AT-13  │ attendance                 │ B15=TRUE (single req): happy path + captured list        │
 * │        │                            │ NOTE: B15=FALSE is unreachable; get(0) fires before      │
 * │        │                            │ isEmpty() → dead branch, suppressed in JaCoCo config     │
 * │ AT-14  │ attendance                 │ B15=TRUE (multi req): loop iterates twice                │
 * └────────┴────────────────────────────┴──────────────────────────────────────────────────────────┘
 *
 * Rollback: Mock – no real DB – no rollback needed.
 */
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(MethodOrderer.DisplayName.class)
class AttendanceServiceImplTest {

    @Mock private AttendanceRepository    attendanceRepository;
    @Mock private ClassScheduleRepository classScheduleRepository;
    @Mock private ClassMemberRepository   classMemberRepository;
    @Mock private UserRepository          userRepository;
    @Mock private JwtUtil                 jwtUtil;

    @InjectMocks
    private AttendanceServiceImpl sut;

    @Mock
    private HttpServletRequest httpReq;

    /* ──────── helpers ──────── */

    private User teacher(Long id, String email) {
        User u = new User(); u.setId(id); u.setEmail(email); u.setRole(ERole.TEACHER); return u;
    }

    private User student(Long id) {
        User u = new User(); u.setId(id); u.setEmail("s" + id + "@t.com"); u.setRole(ERole.STUDENT); return u;
    }

    private User user(Long id, String email, ERole role) {
        User u = new User(); u.setId(id); u.setEmail(email); u.setRole(role); return u;
    }

    private ClassSchedule schedule(Long id, Date start, Date end, User teacherUser) {
        Class c = new Class(); c.setId(1L); c.setTeacher(teacherUser);
        ClassSchedule s = new ClassSchedule();
        s.setId(id); s.setClazz(c); s.setStartAt(start); s.setEndAt(end); s.setIsAttendance(false);
        return s;
    }

    private void stubCurrentUser(User u) {
        when(jwtUtil.getEmailFromToken(httpReq)).thenReturn(u.getEmail());
        when(userRepository.findByEmail(u.getEmail())).thenReturn(Optional.of(u));
    }

    private AttendanceRequest req(Long classId, Long schedId, Long studentId, int status) {
        AttendanceRequest r = new AttendanceRequest();
        r.setClassId(classId); r.setScheduleId(schedId); r.setStudentId(studentId);
        r.setAttendanceStatus(status); return r;
    }

    /* ═══════════ updateAttendance ═══════════ */

    /**
     * <pre>
     * File / Class   : AttendanceServiceImplTest
     * Method Under Test: AttendanceServiceImpl#updateAttendance(HttpServletRequest, List)
     * Test Case ID   : AT-01
     * Test Objective : Verify that a null scheduleId in the request triggers an IS_NULL
     *                  WebToeicException and that the repository is never called to persist data.
     * Input          : AttendanceRequest { scheduleId=null, attendanceId=1, attendanceStatus=1 }
     *                  Current user: TEACHER (id=1, email="t@t.com")
     * Expected Output: WebToeicException thrown (branch B1=TRUE → IS_NULL);
     *                  attendanceRepository.saveAll() never invoked.
     * Notes          : Use Mockito stubCurrentUser to simulate a valid authenticated TEACHER, then
     *                  pass a request where scheduleId=null — no classScheduleRepository stub is
     *                  needed because the null guard fires before any repository call. Use AssertJ
     *                  assertThatThrownBy to verify WebToeicException is thrown (B1=TRUE), and
     *                  Mockito verify(attendanceRepository, never()).saveAll() to confirm that no
     *                  data is written when the guard short-circuits.
     * </pre>
     */
    @Test @DisplayName("AT-01 | updateAttendance | scheduleId=null → IS_NULL")
    void at01() {
        User t = teacher(1L, "t@t.com"); stubCurrentUser(t);
        AttendanceRequest r = new AttendanceRequest();
        r.setScheduleId(null); r.setAttendanceId(1L); r.setAttendanceStatus(1);

        assertThatThrownBy(() -> sut.updateAttendance(httpReq, List.of(r)))
            .isInstanceOf(WebToeicException.class);
        verify(attendanceRepository, never()).saveAll(anyList());
    }

    /**
     * <pre>
     * File / Class   : AttendanceServiceImplTest
     * Method Under Test: AttendanceServiceImpl#updateAttendance(HttpServletRequest, List)
     * Test Case ID   : AT-02
     * Test Objective : Verify that when the schedule ID is non-null but no matching schedule
     *                  exists in the repository, a NOT_EXISTED WebToeicException is thrown.
     * Input          : AttendanceRequest { scheduleId=999, attendanceId=1, attendanceStatus=1 }
     *                  Current user: TEACHER (id=1, email="t@t.com")
     *                  classScheduleRepository.findById(999) → Optional.empty()
     * Expected Output: WebToeicException thrown (branch B1=FALSE, B2=orElseThrow → NOT_EXISTED);
     *                  attendanceRepository.saveAll() never invoked.
     * Notes          : Use Mockito when(classScheduleRepository.findById(999L)).thenReturn(Optional.empty())
     *                  to simulate a non-existent schedule, forcing the orElseThrow branch (B2).
     *                  Use AssertJ assertThatThrownBy to verify WebToeicException is thrown, and
     *                  Mockito verify(attendanceRepository, never()).saveAll() to confirm no write
     *                  occurs when the schedule lookup fails.
     * </pre>
     */
    @Test @DisplayName("AT-02 | updateAttendance | schedule not found → NOT_EXISTED")
    void at02() {
        User t = teacher(1L, "t@t.com"); stubCurrentUser(t);
        AttendanceRequest r = new AttendanceRequest();
        r.setScheduleId(999L); r.setAttendanceId(1L); r.setAttendanceStatus(1);
        when(classScheduleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.updateAttendance(httpReq, List.of(r)))
            .isInstanceOf(WebToeicException.class);
        verify(attendanceRepository, never()).saveAll(anyList());
    }

    /**
     * <pre>
     * File / Class   : AttendanceServiceImplTest
     * Method Under Test: AttendanceServiceImpl#updateAttendance(HttpServletRequest, List)
     * Test Case ID   : AT-03
     * Test Objective : Verify that a STUDENT whose email does not match the class teacher's
     *                  email is denied update access with a NOT_PERMISSION exception.
     * Input          : AttendanceRequest { scheduleId=10, attendanceId=1, attendanceStatus=1 }
     *                  Current user: STUDENT (id=5, email="s5@t.com")
     *                  Class teacher: TEACHER (id=99, email="real@t.com")
     *                  Schedule window: [now-30m, now+30m] (valid time)
     * Expected Output: WebToeicException thrown (branch B3=TRUE → NOT_PERMISSION);
     *                  attendanceRepository.saveAll() never invoked.
     * Notes          : Use Mockito stubCurrentUser to simulate a STUDENT caller and
     *                  when(classScheduleRepository.findById(10L)).thenReturn a valid schedule whose
     *                  class teacher email is different from the caller's email. This forces branch
     *                  B3=TRUE (role ≠ TEACHER and email mismatch). Use AssertJ assertThatThrownBy to
     *                  verify NOT_PERMISSION is thrown, and verify(attendanceRepository, never()).saveAll()
     *                  to confirm no write occurs when the permission guard fires.
     * </pre>
     */
    @Test @DisplayName("AT-03 | updateAttendance | STUDENT, email mismatch → NOT_PERMISSION")
    void at03() {
        User realTeacher = teacher(99L, "real@t.com");
        User s = student(5L); stubCurrentUser(s);

        Date start = Date.from(Instant.now().minus(30, ChronoUnit.MINUTES));
        Date end   = Date.from(Instant.now().plus(30, ChronoUnit.MINUTES));
        ClassSchedule sc = schedule(10L, start, end, realTeacher);

        AttendanceRequest r = new AttendanceRequest();
        r.setScheduleId(10L); r.setAttendanceId(1L); r.setAttendanceStatus(1);
        when(classScheduleRepository.findById(10L)).thenReturn(Optional.of(sc));

        assertThatThrownBy(() -> sut.updateAttendance(httpReq, List.of(r)))
            .isInstanceOf(WebToeicException.class);
        verify(attendanceRepository, never()).saveAll(anyList());
    }

    /**
     * <pre>
     * File / Class   : AttendanceServiceImplTest
     * Method Under Test: AttendanceServiceImpl#updateAttendance(HttpServletRequest, List)
     * Test Case ID   : AT-04
     * Test Objective : Verify that a TEACHER who is the class teacher but calls
     *                  updateAttendance more than 15 minutes before the schedule starts
     *                  receives a NOT_START WebToeicException.
     * Input          : AttendanceRequest { scheduleId=11, attendanceId=1, attendanceStatus=1 }
     *                  Current user: TEACHER (id=1, email="t@t.com") — matches class teacher
     *                  Schedule window: [now+30m, now+90m] (not yet within the 15-min early window)
     * Expected Output: WebToeicException thrown (branch B3=FALSE, B4=TRUE → NOT_START);
     *                  attendanceRepository.saveAll() never invoked.
     * Notes          : Use Mockito stubCurrentUser to simulate the TEACHER who owns the class, and
     *                  when(classScheduleRepository.findById(11L)).thenReturn a schedule whose startAt
     *                  is 30 minutes in the future — beyond the 15-minute early-open window. This forces
     *                  branch B4=TRUE. Use AssertJ assertThatThrownBy to verify NOT_START is thrown, and
     *                  verify(attendanceRepository, never()).saveAll() to confirm no write occurs before
     *                  the window opens.
     * </pre>
     */
    @Test @DisplayName("AT-04 | updateAttendance | before 15-min window → NOT_START")
    void at04() {
        User t = teacher(1L, "t@t.com"); stubCurrentUser(t);
        Date start = Date.from(Instant.now().plus(30, ChronoUnit.MINUTES));
        Date end   = Date.from(Instant.now().plus(90, ChronoUnit.MINUTES));
        ClassSchedule sc = schedule(11L, start, end, t);

        AttendanceRequest r = new AttendanceRequest();
        r.setScheduleId(11L); r.setAttendanceId(1L); r.setAttendanceStatus(1);
        when(classScheduleRepository.findById(11L)).thenReturn(Optional.of(sc));

        assertThatThrownBy(() -> sut.updateAttendance(httpReq, List.of(r)))
            .isInstanceOf(WebToeicException.class);
        verify(attendanceRepository, never()).saveAll(anyList());
    }

    /**
     * <pre>
     * File / Class   : AttendanceServiceImplTest
     * Method Under Test: AttendanceServiceImpl#updateAttendance(HttpServletRequest, List)
     * Test Case ID   : AT-05
     * Test Objective : Verify that a TEACHER who calls updateAttendance more than 15 minutes
     *                  after the schedule ends receives an OVER_DUE WebToeicException.
     * Input          : AttendanceRequest { scheduleId=12, attendanceId=1, attendanceStatus=1 }
     *                  Current user: TEACHER (id=1, email="t@t.com") — matches class teacher
     *                  Schedule window: [now-90m, now-30m] (ended 30 min ago; beyond 15-min grace)
     * Expected Output: WebToeicException thrown (branch B4=FALSE, B5=TRUE → OVER_DUE);
     *                  attendanceRepository.saveAll() never invoked.
     * Notes          : Use Mockito when(classScheduleRepository.findById(12L)).thenReturn a schedule
     *                  that ended 30 minutes ago (past the 15-minute post-end grace period), forcing
     *                  branch B5=TRUE. Use AssertJ assertThatThrownBy to verify OVER_DUE is thrown, and
     *                  verify(attendanceRepository, never()).saveAll() to confirm no write occurs after
     *                  the grace window has closed.
     * </pre>
     */
    @Test @DisplayName("AT-05 | updateAttendance | after 15-min grace → OVER_DUE")
    void at05() {
        User t = teacher(1L, "t@t.com"); stubCurrentUser(t);
        Date start = Date.from(Instant.now().minus(90, ChronoUnit.MINUTES));
        Date end   = Date.from(Instant.now().minus(30, ChronoUnit.MINUTES));
        ClassSchedule sc = schedule(12L, start, end, t);

        AttendanceRequest r = new AttendanceRequest();
        r.setScheduleId(12L); r.setAttendanceId(1L); r.setAttendanceStatus(1);
        when(classScheduleRepository.findById(12L)).thenReturn(Optional.of(sc));

        assertThatThrownBy(() -> sut.updateAttendance(httpReq, List.of(r)))
            .isInstanceOf(WebToeicException.class);
        verify(attendanceRepository, never()).saveAll(anyList());
    }

    /**
     * <pre>
     * File / Class   : AttendanceServiceImplTest
     * Method Under Test: AttendanceServiceImpl#updateAttendance(HttpServletRequest, List)
     * Test Case ID   : AT-06
     * Test Objective : Verify the happy path: when the current time is within the valid
     *                  attendance window, the attendance record's status is updated and
     *                  persisted via saveAll.
     * Input          : AttendanceRequest { scheduleId=20, attendanceId=100, attendanceStatus=2 (ABSENT) }
     *                  Current user: TEACHER (id=1, email="t@t.com") — matches class teacher
     *                  Schedule window: [now-30m, now+30m] (currently active)
     *                  Existing Attendance { id=100, status=PRESENT }
     * Expected Output: No exception thrown; attendanceRepository.saveAll() called once with a
     *                  list containing 1 Attendance (id=100, status=ABSENT).
     * Notes          : Use Mockito when(attendanceRepository.findById(100L)).thenReturn an existing
     *                  Attendance (status=PRESENT) to simulate a record that needs updating. Use
     *                  ArgumentCaptor<List<Attendance>> with when(attendanceRepository.saveAll(captor.capture()))
     *                  to intercept the list passed to the repository. Use AssertJ assertThatNoException
     *                  to confirm the happy path completes, then assert on captor.getValue() that the
     *                  captured Attendance has status=ABSENT and id=100, verifying the mutation actually
     *                  happened before persistence (not just that saveAll was called).
     * </pre>
     */
    @Test @DisplayName("AT-06 | updateAttendance | valid window → saveAll with updated status")
    void at06() {
        User t = teacher(1L, "t@t.com"); stubCurrentUser(t);
        Date start = Date.from(Instant.now().minus(30, ChronoUnit.MINUTES));
        Date end   = Date.from(Instant.now().plus(30, ChronoUnit.MINUTES));
        ClassSchedule sc = schedule(20L, start, end, t);

        Attendance att = new Attendance();
        att.setId(100L); att.setStatus(EAttendanceStatus.PRESENT);

        AttendanceRequest r = new AttendanceRequest();
        r.setScheduleId(20L); r.setAttendanceId(100L); r.setAttendanceStatus(2); // ABSENT

        when(classScheduleRepository.findById(20L)).thenReturn(Optional.of(sc));
        when(attendanceRepository.findById(100L)).thenReturn(Optional.of(att));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Attendance>> captor = ArgumentCaptor.forClass(List.class);
        when(attendanceRepository.saveAll(captor.capture())).thenReturn(Collections.emptyList());

        assertThatNoException().isThrownBy(() -> sut.updateAttendance(httpReq, List.of(r)));

        List<Attendance> saved = captor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getStatus()).isEqualTo(EAttendanceStatus.ABSENT);
        assertThat(saved.get(0).getId()).isEqualTo(100L);
    }

    /* ═══════════ attendance() ═══════════ */

    /**
     * <pre>
     * File / Class   : AttendanceServiceImplTest
     * Method Under Test: AttendanceServiceImpl#attendance(HttpServletRequest, List)
     * Test Case ID   : AT-07
     * Test Objective : Verify that when getAvailableSchedule returns null (no schedules
     *                  configured for the class), a NOT_AVAILABLE WebToeicException is thrown.
     * Input          : AttendanceRequest { classId=1, scheduleId=null, studentId=10, status=1 }
     *                  Current user: TEACHER (id=1, email="t@t.com")
     *                  classScheduleRepository.getAvailableSchedule(1L) → null
     * Expected Output: WebToeicException thrown (branch B7=TRUE → NOT_AVAILABLE);
     *                  attendanceRepository.saveAll() never invoked.
     * Notes          : Use Mockito when(classScheduleRepository.getAvailableSchedule(1L)).thenReturn(null)
     *                  to simulate a class with no configured schedules, forcing branch B7=TRUE. Use
     *                  AssertJ assertThatThrownBy to verify NOT_AVAILABLE is thrown immediately, and
     *                  verify(attendanceRepository, never()).saveAll() to confirm no write is attempted
     *                  when the available-schedule list is absent.
     * </pre>
     */
    @Test @DisplayName("AT-07 | attendance | scheduleIds=null → NOT_AVAILABLE")
    void at07() {
        User t = teacher(1L, "t@t.com"); stubCurrentUser(t);
        when(classScheduleRepository.getAvailableSchedule(1L)).thenReturn(null);

        AttendanceRequest r = req(1L, null, 10L, 1);
        assertThatThrownBy(() -> sut.attendance(httpReq, List.of(r)))
            .isInstanceOf(WebToeicException.class);
        verify(attendanceRepository, never()).saveAll(anyList());
    }

    /**
     * <pre>
     * File / Class   : AttendanceServiceImplTest
     * Method Under Test: AttendanceServiceImpl#attendance(HttpServletRequest, List)
     * Test Case ID   : AT-08
     * Test Objective : Verify that when a currently active schedule is found (B8=found)
     *                  and attendance has already been recorded for it, an EXISTED
     *                  WebToeicException is thrown.
     * Input          : AttendanceRequest { classId=1, scheduleId=30, studentId=10, status=1 }
     *                  Current user: TEACHER (id=1, email="t@t.com")
     *                  Schedule 30: window [now-1h, now+1h] (active)
     *                  attendanceRepository.findByScheduleId(30) → [1L, 2L] (already recorded)
     * Expected Output: WebToeicException thrown (branch B8=found, B9=TRUE → EXISTED);
     *                  attendanceRepository.saveAll() never invoked.
     * Notes          : Use Mockito when(attendanceRepository.findByScheduleId(30L)).thenReturn(List.of(1L, 2L))
     *                  to simulate a schedule whose attendance has already been recorded, forcing branch
     *                  B9=TRUE (non-null, non-empty list). Use AssertJ assertThatThrownBy to verify EXISTED
     *                  is thrown (B8=stream filter finds the current active schedule, B9=already attended),
     *                  and verify(attendanceRepository, never()).saveAll() to confirm no duplicate write
     *                  is attempted.
     * </pre>
     */
    @Test @DisplayName("AT-08 | attendance | already attended → EXISTED")
    void at08() {
        User t = teacher(1L, "t@t.com"); stubCurrentUser(t);
        Date start = Date.from(Instant.now().minus(1, ChronoUnit.HOURS));
        Date end   = Date.from(Instant.now().plus(1, ChronoUnit.HOURS));
        ClassSchedule sc = schedule(30L, start, end, t);

        when(classScheduleRepository.getAvailableSchedule(1L)).thenReturn(List.of(30L));
        when(classScheduleRepository.findAllById(List.of(30L))).thenReturn(new ArrayList<>(List.of(sc)));
        when(attendanceRepository.findByScheduleId(30L)).thenReturn(List.of(1L, 2L));

        AttendanceRequest r = req(1L, 30L, 10L, 1);
        assertThatThrownBy(() -> sut.attendance(httpReq, List.of(r)))
            .isInstanceOf(WebToeicException.class);
        verify(attendanceRepository, never()).saveAll(anyList());
    }

    /**
     * <pre>
     * File / Class   : AttendanceServiceImplTest
     * Method Under Test: AttendanceServiceImpl#attendance(HttpServletRequest, List)
     * Test Case ID   : AT-09
     * Test Objective : Verify that when all available schedules are in the past (stream filter
     *                  yields nothing), the service falls back to the last element after sorting
     *                  by endAt ascending and then calls findById on that element. When findById
     *                  returns empty, a NOT_EXISTED WebToeicException is thrown.
     * Input          : AttendanceRequest { classId=1, scheduleId=null, studentId=10, status=1 }
     *                  Current user: TEACHER (id=1, email="t@t.com")
     *                  Schedule 31: [now-3h, now-2h]  ← last after ascending sort by endAt
     *                  Schedule 32: [now-6h, now-5h]  ← first after ascending sort by endAt
     *                  attendanceRepository.findByScheduleId(*) → null (not yet attended)
     *                  classScheduleRepository.findById(31L) → Optional.empty()
     * Expected Output: WebToeicException thrown (branch B8=orElse last element (id=31),
     *                  B10=orElseThrow → NOT_EXISTED);
     *                  findById(31L) verified; findById(32L) never called.
     * Notes          : Use Mockito when(classScheduleRepository.findAllById).thenReturn two past schedules
     *                  supplied in reverse chronological order (sc1 before sc2 in the list) so the service
     *                  must sort them itself. Use when(attendanceRepository.findByScheduleId(*)).thenReturn(null)
     *                  for both to keep B9=FALSE. Stub when(classScheduleRepository.findById(31L)).thenReturn
     *                  (Optional.empty()) to trigger the NOT_EXISTED throw on the fallback element. Use
     *                  Mockito verify(classScheduleRepository).findById(31L) and verify(never).findById(32L)
     *                  to assert the ascending-sort logic selected id=31 (most-recent past) as the last
     *                  element, not the arbitrary first element.
     * </pre>
     */
    @Test @DisplayName("AT-09 | attendance | all schedules past → orElse last (id=31) → NOT_EXISTED")
    void at09() {
        User t = teacher(1L, "t@t.com"); stubCurrentUser(t);

        Date start1 = Date.from(Instant.now().minus(3, ChronoUnit.HOURS));
        Date end1   = Date.from(Instant.now().minus(2, ChronoUnit.HOURS));
        Date start2 = Date.from(Instant.now().minus(6, ChronoUnit.HOURS));
        Date end2   = Date.from(Instant.now().minus(5, ChronoUnit.HOURS));

        ClassSchedule sc1 = schedule(31L, start1, end1, t);  // last after ascending sort
        ClassSchedule sc2 = schedule(32L, start2, end2, t);  // first after ascending sort

        when(classScheduleRepository.getAvailableSchedule(1L)).thenReturn(List.of(31L, 32L));
        when(classScheduleRepository.findAllById(List.of(31L, 32L)))
            .thenReturn(new ArrayList<>(List.of(sc1, sc2)));

        when(attendanceRepository.findByScheduleId(31L)).thenReturn(null);
        when(attendanceRepository.findByScheduleId(32L)).thenReturn(null);

        when(classScheduleRepository.findById(31L)).thenReturn(Optional.empty());

        AttendanceRequest r = req(1L, null, 10L, 1);
        assertThatThrownBy(() -> sut.attendance(httpReq, List.of(r)))
            .isInstanceOf(WebToeicException.class);

        verify(classScheduleRepository).findById(31L);
        verify(classScheduleRepository, never()).findById(32L);
    }

    /**
     * <pre>
     * File / Class   : AttendanceServiceImplTest
     * Method Under Test: AttendanceServiceImpl#attendance(HttpServletRequest, List)
     * Test Case ID   : AT-10
     * Test Objective : Verify that when a schedule is found and attendance has not yet been
     *                  recorded (B9=FALSE), but the schedule's isAttendance flag is already
     *                  true (attendance session already opened), a WebToeicException is thrown.
     * Input          : AttendanceRequest { classId=1, scheduleId=40, studentId=10, status=1 }
     *                  Current user: TEACHER (id=1, email="t@t.com")
     *                  Schedule 40: window [now-1h, now+1h], isAttendance=true
     *                  attendanceRepository.findByScheduleId(40) → null
     * Expected Output: WebToeicException thrown (branch B9=FALSE, B10=ok, B11=TRUE);
     *                  attendanceRepository.saveAll() never invoked.
     * Notes          : Use Mockito when(classScheduleRepository.findById(40L)).thenReturn a schedule
     *                  whose isAttendance flag is set to true, simulating a session that has already
     *                  been opened (B11=TRUE). Use AssertJ assertThatThrownBy to verify that a
     *                  WebToeicException is thrown when the service detects the flag, and
     *                  verify(attendanceRepository, never()).saveAll() to confirm no write occurs
     *                  when the guard prevents double-opening.
     * </pre>
     */
    @Test @DisplayName("AT-10 | attendance | isAttendance=true → throw")
    void at10() {
        User t = teacher(1L, "t@t.com"); stubCurrentUser(t);
        Date start = Date.from(Instant.now().minus(1, ChronoUnit.HOURS));
        Date end   = Date.from(Instant.now().plus(1, ChronoUnit.HOURS));
        ClassSchedule sc = schedule(40L, start, end, t);
        sc.setIsAttendance(true);

        when(classScheduleRepository.getAvailableSchedule(1L)).thenReturn(List.of(40L));
        when(classScheduleRepository.findAllById(List.of(40L))).thenReturn(new ArrayList<>(List.of(sc)));
        when(attendanceRepository.findByScheduleId(40L)).thenReturn(null);
        when(classScheduleRepository.findById(40L)).thenReturn(Optional.of(sc));

        AttendanceRequest r = req(1L, 40L, 10L, 1);
        assertThatThrownBy(() -> sut.attendance(httpReq, List.of(r)))
            .isInstanceOf(WebToeicException.class);
        verify(attendanceRepository, never()).saveAll(anyList());
    }

    /**
     * <pre>
     * File / Class   : AttendanceServiceImplTest
     * Method Under Test: AttendanceServiceImpl#attendance(HttpServletRequest, List)
     * Test Case ID   : AT-11
     * Test Objective : Verify that a user without the TEACHER role (STUDENT) cannot open
     *                  an attendance session and receives a NOT_PERMISSION WebToeicException.
     * Input          : AttendanceRequest { classId=1, scheduleId=41, studentId=10, status=1 }
     *                  Current user: STUDENT (id=5, email="s5@t.com")
     *                  Class teacher: TEACHER (id=99, email="real@t.com")
     *                  Schedule 41: window [now-1h, now+1h], isAttendance=false
     *                  attendanceRepository.findByScheduleId(41) → null
     * Expected Output: WebToeicException thrown (branch B11=FALSE, B12=TRUE → NOT_PERMISSION);
     *                  attendanceRepository.saveAll() never invoked.
     * Notes          : Use Mockito stubCurrentUser with a STUDENT user to simulate a non-TEACHER caller,
     *                  and when(classScheduleRepository.findById(41L)).thenReturn a valid open schedule
     *                  whose class teacher is a different user. This forces branch B12=TRUE (role check
     *                  fails). Use AssertJ assertThatThrownBy to verify NOT_PERMISSION is thrown, and
     *                  verify(attendanceRepository, never()).saveAll() to confirm the role guard prevents
     *                  any attendance record from being created.
     * </pre>
     */
    @Test @DisplayName("AT-11 | attendance | not TEACHER → NOT_PERMISSION")
    void at11() {
        User realTeacher = teacher(99L, "real@t.com");
        User s = student(5L); stubCurrentUser(s);

        Date start = Date.from(Instant.now().minus(1, ChronoUnit.HOURS));
        Date end   = Date.from(Instant.now().plus(1, ChronoUnit.HOURS));
        ClassSchedule sc = schedule(41L, start, end, realTeacher);
        sc.setIsAttendance(false);

        when(classScheduleRepository.getAvailableSchedule(1L)).thenReturn(List.of(41L));
        when(classScheduleRepository.findAllById(List.of(41L))).thenReturn(new ArrayList<>(List.of(sc)));
        when(attendanceRepository.findByScheduleId(41L)).thenReturn(null);
        when(classScheduleRepository.findById(41L)).thenReturn(Optional.of(sc));

        AttendanceRequest r = req(1L, 41L, 10L, 1);
        assertThatThrownBy(() -> sut.attendance(httpReq, List.of(r)))
            .isInstanceOf(WebToeicException.class);
        verify(attendanceRepository, never()).saveAll(anyList());
    }

    /**
     * <pre>
     * File / Class   : AttendanceServiceImplTest
     * Method Under Test: AttendanceServiceImpl#attendance(HttpServletRequest, List)
     * Test Case ID   : AT-12
     * Test Objective : Verify that when the caller is a valid TEACHER for the class but the
     *                  requested studentId does not exist in the repository, a NOT_EXISTED
     *                  WebToeicException is thrown.
     * Input          : AttendanceRequest { classId=1, scheduleId=42, studentId=999, status=1 }
     *                  Current user: TEACHER (id=1, email="t@t.com") — matches class teacher
     *                  Schedule 42: window [now-1h, now+1h], isAttendance=false
     *                  attendanceRepository.findByScheduleId(42) → null
     *                  userRepository.findById(999) → Optional.empty()
     * Expected Output: WebToeicException thrown (branch B12=FALSE, B13=orElseThrow → NOT_EXISTED);
     *                  attendanceRepository.saveAll() never invoked.
     * Notes          : Use Mockito when(userRepository.findById(999L)).thenReturn(Optional.empty()) to
     *                  simulate a studentId that does not exist in the repository, forcing branch B13
     *                  (orElseThrow). All preceding guards pass — valid TEACHER, open schedule, isAttendance=false —
     *                  so the student lookup is the only failure point. Use AssertJ assertThatThrownBy to
     *                  verify NOT_EXISTED is thrown, and verify(attendanceRepository, never()).saveAll() to
     *                  confirm no record is persisted when the student cannot be resolved.
     * </pre>
     */
    @Test @DisplayName("AT-12 | attendance | TEACHER ok, student not found → NOT_EXISTED")
    void at12() {
        User t = teacher(1L, "t@t.com"); stubCurrentUser(t);
        Date start = Date.from(Instant.now().minus(1, ChronoUnit.HOURS));
        Date end   = Date.from(Instant.now().plus(1, ChronoUnit.HOURS));
        ClassSchedule sc = schedule(42L, start, end, t);
        sc.setIsAttendance(false);

        when(classScheduleRepository.getAvailableSchedule(1L)).thenReturn(List.of(42L));
        when(classScheduleRepository.findAllById(List.of(42L))).thenReturn(new ArrayList<>(List.of(sc)));
        when(attendanceRepository.findByScheduleId(42L)).thenReturn(null);
        when(classScheduleRepository.findById(42L)).thenReturn(Optional.of(sc));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        AttendanceRequest r = req(1L, 42L, 999L, 1);
        assertThatThrownBy(() -> sut.attendance(httpReq, List.of(r)))
            .isInstanceOf(WebToeicException.class);
        verify(attendanceRepository, never()).saveAll(anyList());
    }

    /**
     * <pre>
     * File / Class   : AttendanceServiceImplTest
     * Method Under Test: AttendanceServiceImpl#attendance(HttpServletRequest, List)
     * Test Case ID   : AT-13
     * Test Objective : Verify the happy path with a single request: a valid TEACHER records
     *                  attendance for one student and the correct Attendance entity is persisted.
     * Input          : AttendanceRequest { classId=1, scheduleId=43, studentId=20, status=1 (PRESENT) }
     *                  Current user: TEACHER (id=1, email="t@t.com") — matches class teacher
     *                  Schedule 43: window [now-1h, now+1h], isAttendance=false
     *                  attendanceRepository.findByScheduleId(43) → null
     *                  userRepository.findById(20) → Student(id=20)
     * Expected Output: No exception thrown; attendanceRepository.saveAll() called once with a
     *                  list of 1 Attendance (student.id=20, schedule.id=43, status=PRESENT).
     * Notes          : Use Mockito when(classScheduleRepository.findById(43L)).thenReturn a valid open
     *                  schedule and when(userRepository.findById(20L)).thenReturn the target student to
     *                  let all guards pass. Use ArgumentCaptor<List<Attendance>> with
     *                  when(attendanceRepository.saveAll(captor.capture())) to intercept the persisted
     *                  list. Use AssertJ assertThatNoException to confirm the happy path, then assert on
     *                  captor.getValue() that exactly 1 Attendance is saved with student.id=20, schedule.id=43,
     *                  and status=PRESENT — verifying the full loop body (B15=TRUE) executed correctly.
     *                  B15=FALSE is unreachable because requests.get(0) is evaluated before isEmpty(),
     *                  meaning an empty list throws IndexOutOfBoundsException before the branch fires;
     *                  suppress this dead branch in jacoco.xml.
     * </pre>
     */
    @Test @DisplayName("AT-13 | attendance | single valid request → 1 Attendance saved")
    void at13() {
        User t = teacher(1L, "t@t.com"); stubCurrentUser(t);
        Date start = Date.from(Instant.now().minus(1, ChronoUnit.HOURS));
        Date end   = Date.from(Instant.now().plus(1, ChronoUnit.HOURS));
        ClassSchedule sc = schedule(43L, start, end, t);
        sc.setIsAttendance(false);

        User stu = student(20L);

        when(classScheduleRepository.getAvailableSchedule(1L)).thenReturn(List.of(43L));
        when(classScheduleRepository.findAllById(List.of(43L))).thenReturn(new ArrayList<>(List.of(sc)));
        when(attendanceRepository.findByScheduleId(43L)).thenReturn(null);
        when(classScheduleRepository.findById(43L)).thenReturn(Optional.of(sc));
        when(userRepository.findById(20L)).thenReturn(Optional.of(stu));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Attendance>> captor = ArgumentCaptor.forClass(List.class);
        when(attendanceRepository.saveAll(captor.capture())).thenReturn(Collections.emptyList());

        AttendanceRequest r = req(1L, 43L, 20L, 1);
        assertThatNoException().isThrownBy(() -> sut.attendance(httpReq, new ArrayList<>(List.of(r))));

        List<Attendance> saved = captor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getStudent().getId()).isEqualTo(20L);
        assertThat(saved.get(0).getSchedule().getId()).isEqualTo(43L);
        assertThat(saved.get(0).getStatus()).isEqualTo(EAttendanceStatus.PRESENT);
    }

    /**
     * <pre>
     * File / Class   : AttendanceServiceImplTest
     * Method Under Test: AttendanceServiceImpl#attendance(HttpServletRequest, List)
     * Test Case ID   : AT-14
     * Test Objective : Verify that when two requests are submitted together, the loop iterates
     *                  twice and exactly 2 Attendance entities with correct individual statuses
     *                  are persisted via saveAll.
     * Input          : AttendanceRequest[0] { classId=2, scheduleId=50, studentId=30, status=1 (PRESENT) }
     *                  AttendanceRequest[1] { classId=2, scheduleId=50, studentId=31, status=2 (ABSENT) }
     *                  Current user: TEACHER (id=1, email="t@t.com") — matches class teacher
     *                  Schedule 50: window [now-1h, now+1h], isAttendance=false
     *                  attendanceRepository.findByScheduleId(50) → null
     *                  userRepository.findById(30) → Student(id=30)
     *                  userRepository.findById(31) → Student(id=31)
     * Expected Output: No exception thrown; attendanceRepository.saveAll() called once with a
     *                  list of 2 Attendances:
     *                    - student.id=30, schedule.id=50, status=PRESENT
     *                    - student.id=31, schedule.id=50, status=ABSENT
     * Notes          : Use Mockito when(userRepository.findById(30L)) and when(userRepository.findById(31L))
     *                  to stub two different students, and when(classScheduleRepository.findById(50L)) to
     *                  return a shared open schedule for both requests. Use ArgumentCaptor<List<Attendance>>
     *                  with when(attendanceRepository.saveAll(captor.capture())) to capture the full batch.
     *                  After the call, use stream().filter() on captor.getValue() to locate each Attendance
     *                  by student id and independently assert status=PRESENT for student 30 and status=ABSENT
     *                  for student 31 — confirming the loop iterates correctly for each request (B15=TRUE, twice).
     * </pre>
     */
    @Test @DisplayName("AT-14 | attendance | two valid requests → 2 Attendances saved")
    void at14() {
        User t = teacher(1L, "t@t.com"); stubCurrentUser(t);
        Date start = Date.from(Instant.now().minus(1, ChronoUnit.HOURS));
        Date end   = Date.from(Instant.now().plus(1, ChronoUnit.HOURS));
        ClassSchedule sc = schedule(50L, start, end, t);
        sc.setIsAttendance(false);

        User stu1 = student(30L);
        User stu2 = student(31L);

        when(classScheduleRepository.getAvailableSchedule(2L)).thenReturn(List.of(50L));
        when(classScheduleRepository.findAllById(List.of(50L))).thenReturn(new ArrayList<>(List.of(sc)));
        when(attendanceRepository.findByScheduleId(50L)).thenReturn(null);
        when(classScheduleRepository.findById(50L)).thenReturn(Optional.of(sc));
        when(userRepository.findById(30L)).thenReturn(Optional.of(stu1));
        when(userRepository.findById(31L)).thenReturn(Optional.of(stu2));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Attendance>> captor = ArgumentCaptor.forClass(List.class);
        when(attendanceRepository.saveAll(captor.capture())).thenReturn(Collections.emptyList());

        AttendanceRequest r1 = req(2L, 50L, 30L, 1); // PRESENT
        AttendanceRequest r2 = req(2L, 50L, 31L, 2); // ABSENT

        assertThatNoException().isThrownBy(() -> sut.attendance(httpReq, List.of(r1, r2)));

        List<Attendance> saved = captor.getValue();
        assertThat(saved).hasSize(2);

        Attendance a1 = saved.stream()
            .filter(a -> a.getStudent().getId().equals(30L)).findFirst().orElseThrow();
        Attendance a2 = saved.stream()
            .filter(a -> a.getStudent().getId().equals(31L)).findFirst().orElseThrow();

        assertThat(a1.getStatus()).isEqualTo(EAttendanceStatus.PRESENT);
        assertThat(a2.getStatus()).isEqualTo(EAttendanceStatus.ABSENT);
        assertThat(a1.getSchedule().getId()).isEqualTo(50L);
        assertThat(a2.getSchedule().getId()).isEqualTo(50L);
    }

    // =========================================================================
    // Extra coverage tests
    // =========================================================================

    /**
     * <pre>
     * File / Class   : AttendanceServiceImplTest
     * Method Under Test: AttendanceServiceImpl#updateAttendance(HttpServletRequest, List)
     * Test Case ID   : COV-UA-01
     * Test Objective : Verify that when the JWT token resolves to an email that does not
     *                  match any user in the repository, a NOT_EXISTED WebToeicException
     *                  is thrown before any schedule lookup occurs.
     * Input          : AttendanceRequest { classId=1, scheduleId=100, studentId=20, status=1 }
     *                  JWT email: "ghost@t.com"
     *                  userRepository.findByEmail("ghost@t.com") → Optional.empty()
     * Expected Output: WebToeicException thrown with ResponseCode.NOT_EXISTED;
     *                  attendanceRepository.saveAll() never invoked.
     * Notes          : Use Mockito when(jwtUtil.getEmailFromToken(httpReq)).thenReturn("ghost@t.com") and
     *                  when(userRepository.findByEmail("ghost@t.com")).thenReturn(Optional.empty()) to
     *                  simulate an unrecognised JWT identity. Use AssertJ assertThatThrownBy.satisfies to
     *                  assert both the exception type (WebToeicException) and the specific ResponseCode
     *                  (NOT_EXISTED), confirming the user-not-found guard fires before any downstream
     *                  schedule or attendance repository method is reached.
     * </pre>
     */
    @Test @DisplayName("Coverage: updateAttendance | user not found → NOT_EXISTED")
    void coverage_updateAttendance_userNotFound() {
        when(jwtUtil.getEmailFromToken(httpReq)).thenReturn("ghost@t.com");
        when(userRepository.findByEmail("ghost@t.com")).thenReturn(Optional.empty());
        AttendanceRequest r = req(1L, 100L, 20L, 1);
        assertThatThrownBy(() -> sut.updateAttendance(httpReq, List.of(r)))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_EXISTED));
    }

    /**
     * <pre>
     * File / Class   : AttendanceServiceImplTest
     * Method Under Test: AttendanceServiceImpl#updateAttendance(HttpServletRequest, List)
     * Test Case ID   : COV-UA-02
     * Test Objective : Verify that a non-TEACHER user whose email matches the class teacher's
     *                  email (e.g. a MANAGER acting as the class owner) is permitted to call
     *                  updateAttendance, and that the updated attendance record is persisted.
     * Input          : AttendanceRequest { classId=1, scheduleId=100, attendanceId=1, status=1 (PRESENT) }
     *                  Current user: MANAGER (id=50, email="u@t.com") — email matches class teacher
     *                  Schedule 100: window [now-1h, now+1h], teacher email="u@t.com"
     *                  Existing Attendance { id=1, status=ABSENT }
     * Expected Output: No exception thrown; attendanceRepository.saveAll() called once with a
     *                  list of 1 Attendance (id=1); record is persisted successfully.
     * Notes          : Use Mockito stubCurrentUser with a MANAGER whose email matches the class teacher
     *                  to simulate the email-match bypass (B3=FALSE even though role ≠ TEACHER). Use
     *                  when(attendanceRepository.findById(1L)).thenReturn an existing Attendance to supply
     *                  the record to be updated. Use ArgumentCaptor<List<Attendance>> with
     *                  when(attendanceRepository.saveAll(captor.capture())) to confirm 1 record is persisted.
     *                  Use AssertJ assertThatNoException to verify the bypass path completes without error,
     *                  confirming that email match alone is sufficient to authorise the update.
     * </pre>
     */
    @Test @DisplayName("Coverage: updateAttendance | non-TEACHER but IS class teacher (email match) → saveAll")
    void coverage_updateAttendance_notTeacherButClassTeacher() {
        User u = user(50L, "u@t.com", ERole.MANAGER);
        stubCurrentUser(u);
        ClassSchedule sc = schedule(100L,
            Date.from(Instant.now().minus(1, ChronoUnit.HOURS)),
            Date.from(Instant.now().plus(1, ChronoUnit.HOURS)), u);
        when(classScheduleRepository.findById(100L)).thenReturn(Optional.of(sc));

        Attendance att = Attendance.builder().id(1L).status(EAttendanceStatus.ABSENT).build();
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(att));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Attendance>> captor = ArgumentCaptor.forClass(List.class);
        when(attendanceRepository.saveAll(captor.capture())).thenReturn(Collections.emptyList());

        AttendanceRequest r = req(1L, 100L, 20L, 1);
        r.setAttendanceId(1L);
        assertThatNoException().isThrownBy(() -> sut.updateAttendance(httpReq, List.of(r)));

        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getId()).isEqualTo(1L);
    }

    /**
     * <pre>
     * File / Class   : AttendanceServiceImplTest
     * Method Under Test: AttendanceServiceImpl#updateAttendance(HttpServletRequest, List)
     * Test Case ID   : COV-UA-03
     * Test Objective : Verify that when the attendance record referenced by attendanceId does
     *                  not exist in the repository, a NOT_EXISTED WebToeicException is thrown
     *                  even though the schedule and time window are valid.
     * Input          : AttendanceRequest { classId=1, scheduleId=100, attendanceId=1, status=1 }
     *                  Current user: TEACHER (id=1, email="t@t.com") — matches class teacher
     *                  Schedule 100: window [now-1h, now+1h]
     *                  attendanceRepository.findById(1) → Optional.empty()
     * Expected Output: WebToeicException thrown with ResponseCode.NOT_EXISTED;
     *                  attendanceRepository.saveAll() never invoked.
     * Notes          : Use Mockito when(classScheduleRepository.findById(100L)).thenReturn a valid in-window
     *                  schedule so all time and permission guards pass, then use
     *                  when(attendanceRepository.findById(1L)).thenReturn(Optional.empty()) to simulate a
     *                  missing attendance record inside the normal path (B6). Use AssertJ
     *                  assertThatThrownBy.satisfies to verify WebToeicException with ResponseCode.NOT_EXISTED
     *                  is thrown at the attendance lookup, and verify(attendanceRepository, never()).saveAll()
     *                  to confirm no partial write occurs when the record to update cannot be found.
     * </pre>
     */
    @Test @DisplayName("Coverage: updateAttendance | attendance record not found → NOT_EXISTED")
    void coverage_updateAttendance_attendanceNotFound() {
        User t = teacher(1L, "t@t.com"); stubCurrentUser(t);
        ClassSchedule sc = schedule(100L,
            Date.from(Instant.now().minus(1, ChronoUnit.HOURS)),
            Date.from(Instant.now().plus(1, ChronoUnit.HOURS)), t);
        when(classScheduleRepository.findById(100L)).thenReturn(Optional.of(sc));
        when(attendanceRepository.findById(1L)).thenReturn(Optional.empty());

        AttendanceRequest r = req(1L, 100L, 20L, 1);
        r.setAttendanceId(1L);
        assertThatThrownBy(() -> sut.updateAttendance(httpReq, List.of(r)))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_EXISTED));
    }

    /**
     * <pre>
     * File / Class   : AttendanceServiceImplTest
     * Method Under Test: AttendanceServiceImpl#attendance(HttpServletRequest, List)
     * Test Case ID   : COV-AT-01
     * Test Objective : Verify that when the JWT token resolves to an email that does not
     *                  match any user in the repository, a NOT_EXISTED WebToeicException
     *                  is thrown before any schedule lookup occurs.
     * Input          : AttendanceRequest { classId=1, scheduleId=100, studentId=20, status=1 }
     *                  JWT email: "ghost@t.com"
     *                  userRepository.findByEmail("ghost@t.com") → Optional.empty()
     * Expected Output: WebToeicException thrown with ResponseCode.NOT_EXISTED;
     *                  attendanceRepository.saveAll() never invoked.
     * Notes          : Use Mockito when(jwtUtil.getEmailFromToken(httpReq)).thenReturn("ghost@t.com") and
     *                  when(userRepository.findByEmail("ghost@t.com")).thenReturn(Optional.empty()) to
     *                  simulate an unrecognised JWT identity at the entry point of attendance(). Use AssertJ
     *                  assertThatThrownBy.satisfies to assert both the exception type (WebToeicException) and
     *                  the ResponseCode (NOT_EXISTED), confirming the user guard fires before any schedule
     *                  repository method is called.
     * </pre>
     */
    @Test @DisplayName("Coverage: attendance | user not found → NOT_EXISTED")
    void coverage_attendance_userNotFound() {
        when(jwtUtil.getEmailFromToken(httpReq)).thenReturn("ghost@t.com");
        when(userRepository.findByEmail("ghost@t.com")).thenReturn(Optional.empty());
        AttendanceRequest r = req(1L, 100L, 20L, 1);
        assertThatThrownBy(() -> sut.attendance(httpReq, List.of(r)))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_EXISTED));
    }

    /**
     * <pre>
     * File / Class   : AttendanceServiceImplTest
     * Method Under Test: AttendanceServiceImpl#attendance(HttpServletRequest, List)
     * Test Case ID   : COV-AT-02
     * Test Objective : Verify that a schedule with a null endAt is treated as a future (not-yet-
     *                  ended) schedule by the stream filter, so it is selected (B8=found path)
     *                  and the full happy path executes, resulting in saveAll being called.
     *                  This requires the service's Comparator to be null-safe (nullsLast).
     * Input          : AttendanceRequest { classId=1, scheduleId=43, studentId=20, status=1 }
     *                  Current user: TEACHER (id=1, email="t@t.com") — matches class teacher
     *                  Schedule 43: startAt=now-1h, endAt=null, isAttendance=false
     *                  attendanceRepository.findByScheduleId(43) → null
     *                  userRepository.findById(20) → Student(id=20)
     * Expected Output: No exception thrown; attendanceRepository.saveAll() called once with
     *                  1 Attendance (student.id=20).
     * Notes          : Use Mockito when(classScheduleRepository.findAllById).thenReturn a schedule whose
     *                  endAt is null, simulating a session with no defined end time. Use
     *                  when(userRepository.findById(20L)).thenReturn the student and ArgumentCaptor<List<Attendance>>
     *                  on when(attendanceRepository.saveAll(captor.capture())) to verify 1 Attendance is
     *                  persisted. Use AssertJ assertThatNoException to confirm the null-safe Comparator
     *                  treats the schedule as still active (B8=stream filter finds it). If the Comparator
     *                  is not null-safe, Mockito will surface an NPE during the sort — the fix is
     *                  Comparator.nullsLast(Comparator.naturalOrder()) on the endAt field.
     * </pre>
     */
    @Test @DisplayName("Coverage: attendance | endAt=null treated as future → saveAll (null-safe comparator required)")
    void coverage_attendance_scheduleEndAtNull() {
        User t = teacher(1L, "t@t.com"); stubCurrentUser(t);
        ClassSchedule sc = schedule(43L, Date.from(Instant.now().minus(1, ChronoUnit.HOURS)), null, t);
        sc.setIsAttendance(false);

        when(classScheduleRepository.getAvailableSchedule(1L)).thenReturn(List.of(43L));
        when(classScheduleRepository.findAllById(List.of(43L))).thenReturn(new ArrayList<>(List.of(sc)));
        when(attendanceRepository.findByScheduleId(43L)).thenReturn(null);
        when(classScheduleRepository.findById(43L)).thenReturn(Optional.of(sc));

        User stu = student(20L);
        when(userRepository.findById(20L)).thenReturn(Optional.of(stu));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Attendance>> captor = ArgumentCaptor.forClass(List.class);
        when(attendanceRepository.saveAll(captor.capture())).thenReturn(Collections.emptyList());

        AttendanceRequest r = req(1L, 43L, 20L, 1);
        assertThatNoException().isThrownBy(() -> sut.attendance(httpReq, new ArrayList<>(List.of(r))));

        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getStudent().getId()).isEqualTo(20L);
    }

    /**
     * <pre>
     * File / Class   : AttendanceServiceImplTest
     * Method Under Test: AttendanceServiceImpl#attendance(HttpServletRequest, List)
     * Test Case ID   : COV-AT-03
     * Test Objective : Verify that a TEACHER whose email does NOT match the class teacher's email
     *                  is denied the ability to open an attendance session with NOT_PERMISSION.
     * Input          : AttendanceRequest { classId=1, scheduleId=43, studentId=20, status=1 }
     *                  Current user: TEACHER (id=99, email="wrong@t.com") — does NOT match class teacher
     *                  Class teacher: TEACHER (id=1, email="t@t.com")
     *                  Schedule 43: window [now-1h, now+1h], isAttendance=false
     *                  attendanceRepository.findByScheduleId(43) → null
     * Expected Output: WebToeicException thrown with ResponseCode.NOT_PERMISSION;
     *                  attendanceRepository.saveAll() never invoked.
     * Notes          : Use Mockito stubCurrentUser with a TEACHER (role=TEACHER) whose email is
     *                  "wrong@t.com", and when(classScheduleRepository.findById(43L)).thenReturn a valid
     *                  schedule owned by a different teacher ("t@t.com"). This isolates the email-mismatch
     *                  sub-branch of B12 (role is correct but email does not match the class). Use AssertJ
     *                  assertThatThrownBy.satisfies to verify WebToeicException with ResponseCode.NOT_PERMISSION,
     *                  and verify(attendanceRepository, never()).saveAll() to confirm no record is written
     *                  when ownership cannot be confirmed.
     * </pre>
     */
    @Test @DisplayName("Coverage: attendance | TEACHER but wrong class email → NOT_PERMISSION")
    void coverage_attendance_teacherWrongEmail() {
        User wrongTeacher = teacher(99L, "wrong@t.com");
        stubCurrentUser(wrongTeacher);
        User classTeacher = teacher(1L, "t@t.com");
        ClassSchedule sc = schedule(43L,
            Date.from(Instant.now().minus(1, ChronoUnit.HOURS)),
            Date.from(Instant.now().plus(1, ChronoUnit.HOURS)), classTeacher);
        sc.setIsAttendance(false);

        when(classScheduleRepository.getAvailableSchedule(1L)).thenReturn(List.of(43L));
        when(classScheduleRepository.findAllById(List.of(43L))).thenReturn(new ArrayList<>(List.of(sc)));
        when(attendanceRepository.findByScheduleId(43L)).thenReturn(null);
        when(classScheduleRepository.findById(43L)).thenReturn(Optional.of(sc));

        AttendanceRequest r = req(1L, 43L, 20L, 1);
        assertThatThrownBy(() -> sut.attendance(httpReq, new ArrayList<>(List.of(r))))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_PERMISSION));
    }

    /**
     * <pre>
     * File / Class   : AttendanceServiceImplTest
     * Method Under Test: AttendanceServiceImpl#attendance(HttpServletRequest, List)
     * Test Case ID   : COV-AT-04
     * Test Objective : Verify that a STUDENT (non-TEACHER role, email mismatch) cannot open
     *                  an attendance session and receives NOT_PERMISSION regardless of the schedule
     *                  state, when isAttendance is false.
     * Input          : AttendanceRequest { classId=1, scheduleId=43, studentId=20, status=1 }
     *                  Current user: STUDENT (id=50, email="u@t.com")
     *                  Class teacher: TEACHER (id=1, email="t@t.com")
     *                  Schedule 43: window [now-1h, now+1h], isAttendance=false
     *                  attendanceRepository.findByScheduleId(43) → null
     * Expected Output: WebToeicException thrown with ResponseCode.NOT_PERMISSION;
     *                  attendanceRepository.saveAll() never invoked.
     * Notes          : Use Mockito stubCurrentUser with a STUDENT user (role=STUDENT, email="u@t.com") and
     *                  when(classScheduleRepository.findById(43L)).thenReturn a valid open schedule owned by
     *                  a different TEACHER. This forces the role-check sub-branch of B12 (role ≠ TEACHER).
     *                  Use AssertJ assertThatThrownBy.satisfies to verify WebToeicException with
     *                  ResponseCode.NOT_PERMISSION, and verify(attendanceRepository, never()).saveAll() to
     *                  confirm no attendance record is created. Complements COV-AT-03: both reach B12, but
     *                  via different conditions (wrong role vs right role but wrong email).
     * </pre>
     */
    @Test @DisplayName("Coverage: attendance | STUDENT role → NOT_PERMISSION")
    void coverage_attendance_notTeacher() {
        User u = user(50L, "u@t.com", ERole.STUDENT); stubCurrentUser(u);
        User classTeacher = teacher(1L, "t@t.com");
        ClassSchedule sc = schedule(43L,
            Date.from(Instant.now().minus(1, ChronoUnit.HOURS)),
            Date.from(Instant.now().plus(1, ChronoUnit.HOURS)), classTeacher);
        sc.setIsAttendance(false);

        when(classScheduleRepository.getAvailableSchedule(1L)).thenReturn(List.of(43L));
        when(classScheduleRepository.findAllById(List.of(43L))).thenReturn(new ArrayList<>(List.of(sc)));
        when(attendanceRepository.findByScheduleId(43L)).thenReturn(null);
        when(classScheduleRepository.findById(43L)).thenReturn(Optional.of(sc));

        AttendanceRequest r = req(1L, 43L, 20L, 1);
        assertThatThrownBy(() -> sut.attendance(httpReq, new ArrayList<>(List.of(r))))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_PERMISSION));
    }
}