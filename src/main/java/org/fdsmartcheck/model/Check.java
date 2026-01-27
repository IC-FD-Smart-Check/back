package org.fdsmartcheck.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "checks", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"sub_event_id", "user_id"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Check {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_event_id", nullable = false)
    private SubEvent subEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "checkin_time", nullable = false)
    private LocalDateTime checkinTime;

    @Column(name = "checkout_time")
    private LocalDateTime checkoutTime;

    @Column(name = "checkin_latitude")
    private Double checkinLatitude;

    @Column(name = "checkin_longitude")
    private Double checkinLongitude;

    @Column(name = "checkout_latitude")
    private Double checkoutLatitude;

    @Column(name = "checkout_longitude")
    private Double checkoutLongitude;

    @Column(name = "is_present", nullable = false)
    private Boolean isPresent = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}