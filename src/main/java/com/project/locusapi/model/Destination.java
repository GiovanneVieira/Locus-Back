package com.project.locusapi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(name = "destination_table")
@Table(name = "destination_table")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Destination {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String country;

    @Column(nullable = false, length = 120, unique = true)
    private String city;

    @Builder.Default
    @OneToMany(mappedBy = "destination", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TouristPoint> touristPoints = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void replaceTouristPoints(List<TouristPoint> touristPoints) {
        this.touristPoints.clear();
        touristPoints.forEach(this::addTouristPoint);
    }

    public void addTouristPoint(TouristPoint touristPoint) {
        touristPoint.setDestination(this);
        this.touristPoints.add(touristPoint);
    }
}
