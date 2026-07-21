package backend.com.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "app_users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"company_id", "username"})
})
@Data
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    private String passwordHash;

    @Column(nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.STAFF;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    private boolean enabled = true;

    // Absolute path to this user's uploaded signature image (PNG), set via
    // POST /api/users/me/signature. Only meaningful for admins - it's what
    // gets stamped on an invoice's PDF/preview when THEY approve it.
    private String signatureImagePath;
}