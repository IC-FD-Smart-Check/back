package org.fdsmartcheck.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fdsmartcheck.model.enums.Semester;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassGroupResponse {
    private String id;
    private String name;
    private String externalCode;
    private Semester semester;
    private Integer semesterNumber;
    private String courseId;
    private String courseName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
