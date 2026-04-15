package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.constants.enums.ERole;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.SearchBaseDto;
import com.doan2025.webtoeic.dto.request.UserRequest;
import com.doan2025.webtoeic.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = """
                    SELECT new com.doan2025.webtoeic.dto.response.UserResponse( u.id,
                                        u.firstName, u.lastName, u.phone, u.address,
                                        u.dob, u.gender, u.avatarUrl, u.isActive, u.isDelete,
                                        s.education, s.major, u.role, u.createdAt, u.updatedAt, u.code)
                    FROM User u
                    LEFT JOIN u.student s
                    LEFT JOIN u.consultant c
                    LEFT JOIN u.teacher t
                    LEFT JOIN u.manager m
                    WHERE
                        ( COALESCE(:#{#dto.searchString}, null ) is null
                            OR lower(cast(u.email as string)) like lower(concat('%', :#{#dto.searchString}, '%'))
                            OR lower(cast( u.phone as string))  like  lower(concat('%', :#{#dto.searchString}, '%'))
                            OR lower(cast(concat(u.firstName, ' ', u.lastName) as string) )  like lower(concat('%', :#{#dto.searchString}, '%'))
                        )
                        AND (
                          ( (COALESCE(:#{#dto.fromDate}, NULL) IS NULL) OR (COALESCE(:#{#dto.toDate}, NULL) IS NULL))
                          OR u.createdAt BETWEEN :#{#dto.fromDate} AND :#{#dto.toDate}
                        )
                        AND (COALESCE(:roles, null) is null or u.role IN (:roles) )
                        AND (COALESCE(:#{#dto.isActive}, null ) is null or u.isActive = :#{#dto.isActive} )
                        AND (COALESCE(:#{#dto.isDelete}, null ) is null or u.isDelete = :#{#dto.isDelete} )
                        AND (COALESCE(:#{#dto.userRoles}, null ) is null or u.role IN (:#{#dto.userRoles}) )
            """)
    Page<UserResponse> findListUserFilter(SearchBaseDto dto, List<ERole> roles, Pageable pageable);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Query(value = """
                        SELECT new com.doan2025.webtoeic.dto.response.UserResponse(
                                        u.firstName, u.lastName, u.phone, u.address,
                                        u.dob, u.gender, u.avatarUrl, u.isActive, u.isDelete,
                                        s.education, s.major, u.createdAt, u.updatedAt, u.code)
                        FROM User u
                        LEFT JOIN u.student s
                        LEFT JOIN u.consultant c
                        LEFT JOIN u.teacher t
                        LEFT JOIN u.manager m
                        WHERE u.email = :email
                            AND u.isActive = true AND u.isDelete = false
            
            """)
    Optional<UserResponse> findUser(String email);

    @Query(value = """
                        SELECT u
                        FROM User u
                        LEFT JOIN u.student s
                        LEFT JOIN u.consultant c
                        LEFT JOIN u.teacher t
                        LEFT JOIN u.manager m
                        WHERE u.isActive = true AND u.isDelete = false
            
            """)
    List<User> findUserOnlyStudent();

    @Query(value = """
                        SELECT new com.doan2025.webtoeic.dto.response.UserResponse(
                                        u.firstName, u.lastName, u.phone, u.address,
                                        u.dob, u.gender, u.avatarUrl, u.isActive, u.isDelete,
                                        s.education, s.major, u.createdAt, u.updatedAt, u.code)
                        FROM User u
                        LEFT JOIN u.student s
                        LEFT JOIN u.consultant c
                        LEFT JOIN u.teacher t
                        LEFT JOIN u.manager m
                        WHERE u.id = :#{#request.id}
                            AND u.isActive = true AND u.isDelete = false
            
            """)
    Optional<UserResponse> findUserById(UserRequest request);

    Optional<User> findById(Long id);

    boolean existsByCode(String code);
}
