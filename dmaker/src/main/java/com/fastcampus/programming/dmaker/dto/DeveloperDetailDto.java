package com.fastcampus.programming.dmaker.dto;

import com.fastcampus.programming.dmaker.code.StatusCode;
import com.fastcampus.programming.dmaker.entity.Developer;
import com.fastcampus.programming.dmaker.type.DeveloperLevel;
import com.fastcampus.programming.dmaker.type.DeveloperSkillType;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeveloperDetailDto {
    private DeveloperLevel developerLevel;
    private DeveloperSkillType developerSkillType;
    private Integer experienceYears;
    private StatusCode statusCode;
    private String memberId;
    private String name;
    private Integer age;

    public static DeveloperDetailDto fromEntity(Developer developer) {
        return DeveloperDetailDto.builder()
                .developerLevel(developer.getDeveloperLevel())
                .developerSkillType(developer.getDeveloperSkillType())
                .experienceYears(developer.getExperienceYears())
                .memberId(developer.getMemberId())
                .statusCode(developer.getStatusCode())
                .name(developer.getName())
                .age(developer.getAge())
                .build();
    }
}
