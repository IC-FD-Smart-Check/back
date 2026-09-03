package org.fdsmartcheck.model;

import jakarta.persistence.*;
import lombok.*;
import org.fdsmartcheck.model.enums.Semester;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "class_groups",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"course_id", "name"})
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    /**
     * Identificador da turma no sistema externo que exporta o relatório de alunos.
     * Definido pelo administrador e usado para vincular a turma automaticamente na importação.
     * Opcional, mas único quando informado.
     */
    @Column(name = "external_code", length = 100, unique = true)
    private String externalCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Semester semester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
