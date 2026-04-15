package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.Attendance;
import com.doan2025.webtoeic.dto.response.DetailStatisticAttendance;
import com.doan2025.webtoeic.dto.response.OverviewStatisticAttendance;
import com.doan2025.webtoeic.dto.response.OverviewStudentAttendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    @Query("""
            SELECT new com.doan2025.webtoeic.dto.response.DetailStatisticAttendance(
                    st.id, c.id, cs.id, st.firstName, st.lastName, st.email, st.phone, st.address,
                    CASE WHEN att.status = :#{T(com.doan2025.webtoeic.constants.enums.EAttendanceStatus).PRESENT} THEN true ELSE false END,
                    CASE WHEN att.status = :#{T(com.doan2025.webtoeic.constants.enums.EAttendanceStatus).ABSENT} THEN true ELSE false END,
                    CASE WHEN att.status = :#{T(com.doan2025.webtoeic.constants.enums.EAttendanceStatus).LATE} THEN true ELSE false END,
                    att.checkIn,
                    att.id
            
                )
                FROM ClassSchedule cs
                JOIN Attendance  att ON cs.id = att.schedule.id
                JOIN cs.clazz c
                LEFT JOIN att.student st
                WHERE cs.id = :scheduleId
                GROUP BY st.id, c.id, cs.id, st.firstName, st.lastName, st.email, st.phone, st.address, att.status, att.checkIn, att.id
            """)
    Page<DetailStatisticAttendance> detailStatisticAttendance(Long scheduleId, Pageable pageable);

    @Query("""
            SELECT new com.doan2025.webtoeic.dto.response.OverviewStatisticAttendance(
                cs.id, c.id, cs.title, cs.startAt, cs.endAt,
                SUM(CASE WHEN att.status = :#{T(com.doan2025.webtoeic.constants.enums.EAttendanceStatus).PRESENT} THEN 1 ELSE 0 END),
                SUM(CASE WHEN att.status = :#{T(com.doan2025.webtoeic.constants.enums.EAttendanceStatus).ABSENT} THEN 1 ELSE 0 END),
                SUM(CASE WHEN att.status = :#{T(com.doan2025.webtoeic.constants.enums.EAttendanceStatus).LATE} THEN 1 ELSE 0 END)
            )
            FROM ClassSchedule cs
            JOIN Attendance  att ON cs.id = att.schedule.id
            JOIN cs.clazz c
            WHERE c.id = :classId
            GROUP BY cs.id
            """)
    Page<OverviewStatisticAttendance> overviewStatisticAttendance(Long classId, Pageable pageable);

    @Query("""
            SELECT new com.doan2025.webtoeic.dto.response.OverviewStudentAttendance(
                u.id, CONCAT(u.firstName, ' ', u.lastName), u.email,
                SUM(CASE WHEN att.status = :#{T(com.doan2025.webtoeic.constants.enums.EAttendanceStatus).PRESENT} THEN 1 ELSE 0 END),
                SUM(CASE WHEN att.status = :#{T(com.doan2025.webtoeic.constants.enums.EAttendanceStatus).ABSENT} THEN 1 ELSE 0 END),
                SUM(CASE WHEN att.status = :#{T(com.doan2025.webtoeic.constants.enums.EAttendanceStatus).LATE} THEN 1 ELSE 0 END)
            )
            FROM ClassSchedule cs
            JOIN Attendance  att ON cs.id = att.schedule.id
            JOIN User u ON u.id = att.student.id
            WHERE cs.clazz.id = :classId
            GROUP BY u.id
            """)
    Page<OverviewStudentAttendance> overviewStudentAttendance(Long classId, Pageable pageable);

    @Query("""
            SELECT att.id FROM Attendance att
            WHERE att.schedule.id = :scheduleId
            """)
    List<Long> findByScheduleId(Long scheduleId);
}
