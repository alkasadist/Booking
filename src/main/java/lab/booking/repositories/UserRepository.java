package lab.booking.repositories;

import lab.booking.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByName(String name);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.name = :newName WHERE u.id = :userId")
    int updateUserName(@Param("userId") Integer userId, @Param("newName") String newName);
}
