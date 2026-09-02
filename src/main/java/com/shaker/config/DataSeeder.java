package com.shaker.config;

import com.shaker.guideline.Guideline;
import com.shaker.guideline.GuidelineRepository;
import com.shaker.user.Role;
import com.shaker.user.User;
import com.shaker.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Idempotent seed data for the configured (cloud) database. Disable entirely with
 * {@code app.seed=false}. The admin user is only created when {@code app.admin-password}
 * is set, so a fresh Atlas cluster never gets weak default credentials.
 */
@Component
@ConditionalOnProperty(name = "app.seed", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository users;
    private final GuidelineRepository guidelines;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public DataSeeder(UserRepository users, GuidelineRepository guidelines, PasswordEncoder passwordEncoder,
                      @Value("${app.admin-username:admin}") String adminUsername,
                      @Value("${app.admin-password:}") String adminPassword) {
        this.users = users;
        this.guidelines = guidelines;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername.trim().toLowerCase();
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        seedGuidelines();
    }

    private void seedAdmin() {
        if (!StringUtils.hasText(adminPassword)) {
            log.info("app.admin-password not set - skipping admin user seed");
            return;
        }
        if (users.existsByUsername(adminUsername)) {
            return;
        }
        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setEmail(adminUsername + "@shaker.local");
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setDisplayName("Shaker Admin");
        admin.setRoles(new LinkedHashSet<>(Set.of(Role.USER, Role.ADMIN)));
        admin.setCreatedAt(Instant.now());
        users.save(admin);
        log.info("Seeded admin user '{}'", adminUsername);
    }

    private void seedGuidelines() {
        if (guidelines.count() > 0) {
            return;
        }
        guidelines.saveAll(List.of(
                guideline("bar-basics", "Bar Basics", "Fundamentals", 1,
                        "Chill your glassware. Use fresh citrus. Measure everything - "
                                + "consistency is what makes a drink repeatable."),
                guideline("shake-vs-stir", "Shake vs. Stir", "Technique", 2,
                        "Stir drinks that are all spirits (Martini, Negroni, Manhattan). "
                                + "Shake anything with juice, dairy, or egg."),
                guideline("measurements", "Measurement Conversions", "Reference", 3,
                        "1 oz = 30 ml. 1 dash ~= 0.8 ml. A standard cocktail is 2 oz spirit, "
                                + "0.75 oz citrus, 0.75 oz sweetener.")
        ));
        log.info("Seeded {} guidelines", guidelines.count());
    }

    private static Guideline guideline(String slug, String title, String category, int order, String body) {
        Guideline g = new Guideline();
        g.setSlug(slug);
        g.setTitle(title);
        g.setCategory(category);
        g.setSortOrder(order);
        g.setBody(body);
        return g;
    }
}
