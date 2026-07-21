package backend.com.repository;

import backend.com.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByCompany_CodeAndUsername(String companyCode, String username);
    Optional<AppUser> findByCompany_IdAndUsername(Long companyId, String username);
    List<AppUser> findByCompany_IdOrderByUsernameAsc(Long companyId);
}