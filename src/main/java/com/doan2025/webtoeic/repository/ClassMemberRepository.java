package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.ClassMember;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.SearchMemberInClassDto;
import com.doan2025.webtoeic.dto.request.ClassRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassMemberRepository extends JpaRepository<ClassMember, Long> {

    @Query("""
                    SELECT cm FROM ClassMember cm
                    JOIN cm.clazz c
                    JOIN cm.member m
                    WHERE c.id = :#{#request.classId}
                    AND (COALESCE(:#{#request.searchString}, null) IS NULL OR LOWER(CAST(CONCAT(m.firstName, ' ', m.lastName) AS string )) LIKE CONCAT('%', :#{#request.searchString}, '%') )
                    AND (COALESCE(:#{#request.joinDate}, NULL ) IS NULL  OR cm.joinDate <= :#{#request.joinDate})
                    AND (COALESCE(:#{#request.status}, NULL ) IS NULL  OR cm.status IN (:#{#request.status}) )
                    AND (COALESCE(:email, NULL ) IS NULL OR m.email = :email AND cm.status = :#{T(com.doan2025.webtoeic.constants.enums.EJoinStatus).ACTIVE} )
                    AND cm.roleInClass = :#{T(com.doan2025.webtoeic.constants.enums.ERole).STUDENT}
            """)
    Page<ClassMember> findMembersInClass(SearchMemberInClassDto request, String email, Pageable pageable);

    @Query("""
                    SELECT cm FROM ClassMember cm
                    JOIN cm.clazz c
                    JOIN cm.member m
                    WHERE c.id = :#{#request.classId}
                    AND (COALESCE(:#{#request.searchString}, null) IS NULL OR LOWER(CAST(CONCAT(m.firstName, ' ', m.lastName) AS string )) LIKE CONCAT('%', :#{#request.searchString}, '%') )
                    AND (COALESCE(:#{#request.joinDate}, NULL ) IS NULL  OR cm.joinDate <= :#{#request.joinDate})
                    AND (COALESCE(:#{#request.status}, NULL ) IS NULL  OR cm.status IN (:#{#request.status}) )
                    AND cm.roleInClass = :#{T(com.doan2025.webtoeic.constants.enums.ERole).STUDENT}
            """)
    List<ClassMember> findMembersInClass(SearchMemberInClassDto request);

    @Query("""
                SELECT m FROM ClassMember cm
                JOIN cm.clazz c
                JOIN cm.member m
                WHERE c.id = :classId
                AND ( cm.status = :#{T(com.doan2025.webtoeic.constants.enums.EJoinStatus).ACTIVE} )
                AND cm.roleInClass = :#{T(com.doan2025.webtoeic.constants.enums.ERole).STUDENT}
            """)
    List<User> findMembersInClass(Long classId);

    @Query("""
                    SELECT clazz.id
                    FROM ClassMember
                    WHERE member.email = :email
            """)
    List<Long> findClassOfMember(String email);

    @Query("""
                    SELECT cm
                    FROM ClassMember cm
                    WHERE cm.clazz.id = :#{#classRequest.id} AND cm.member.id IN (:#{#classRequest.memberIds})
            """)
    List<ClassMember> findByClassAndUser(ClassRequest classRequest);

    @Query("""
                    SELECT cm
                    FROM ClassMember cm
                    WHERE cm.clazz.id = :clazzId AND cm.member.id = :userId
            """)
    ClassMember findByClassAndMember(Long userId, Long clazzId);

    @Query("""
                    SELECT CASE WHEN COUNT(cm) > 0 THEN true ELSE false END FROM ClassMember cm
                    WHERE cm.clazz.id = :classId AND cm.member.id = :userId
                    AND cm.status = :#{T(com.doan2025.webtoeic.constants.enums.EJoinStatus).ACTIVE}
            """)
    Boolean existsMemberInClass(Long classId, Long userId);
}
