package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.AttachDocumentLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachDocumentLessonRepository extends JpaRepository<AttachDocumentLesson, Long> {

    void deleteAttachDocumentLessonsByLesson_Id(Long lessonId);

    List<AttachDocumentLesson> findAllByLessonId(Long lessonId);
}
