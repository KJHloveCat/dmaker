package com.fastcampus.programming.dmaker.service;

import com.fastcampus.programming.dmaker.code.StatusCode;
import com.fastcampus.programming.dmaker.dto.CreateDeveloper;
import com.fastcampus.programming.dmaker.dto.DeveloperDetailDto;
import com.fastcampus.programming.dmaker.dto.DeveloperDto;
import com.fastcampus.programming.dmaker.dto.EditDeveloper;
import com.fastcampus.programming.dmaker.entity.Developer;
import com.fastcampus.programming.dmaker.entity.RetiredDeveloper;
import com.fastcampus.programming.dmaker.exception.DMakerException;
import com.fastcampus.programming.dmaker.repository.DeveloperRepository;
import com.fastcampus.programming.dmaker.repository.RetiredDeveloperRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.fastcampus.programming.dmaker.exception.DMakerErrorCode.DUPLICATED_MEMBER_ID;
import static com.fastcampus.programming.dmaker.exception.DMakerErrorCode.NO_DEVELOPER;

@Service
@Slf4j
public class DMakerService {
    private final DeveloperRepository developerRepository;
    private final RetiredDeveloperRepository retiredDeveloperRepository;

    public DMakerService(DeveloperRepository developerRepository, RetiredDeveloperRepository retiredDeveloperRepository) {
        this.developerRepository = developerRepository;
        this.retiredDeveloperRepository = retiredDeveloperRepository;
    }

    // ACID
    // Atomic 원자성 (모두 처리 되거나 처리가 아예 안 되거나)
    // Consistency 일관성 (commit 끝난 시점 정해진 규칙)
    // Isolation 고립성
    // Durability 지속성 (commit 된 이력이 무조건 남아 있어야 함)
    @Transactional
    public CreateDeveloper.Response createDeveloper(CreateDeveloper.Request request) {
        validateCreateDeveloperRequest(request);

        // business logic start
        return CreateDeveloper.Response
                .fromEntity(developerRepository.save(createDeveloperFromRequest(request)));
    }

    private Developer createDeveloperFromRequest(CreateDeveloper.Request request) {
        return Developer.builder()
                .developerLevel(request.getDeveloperLevel())
                .developerSkillType(request.getDeveloperSkillType())
                .experienceYears(request.getExperienceYears())
                .memberId(request.getMemberId())
                .statusCode(StatusCode.EMPLOYED)
                .name(request.getName())
                .age(request.getAge())
                .build();
    }

    private void validateCreateDeveloperRequest(@NonNull CreateDeveloper.Request request) {
        // business validation
        request.getDeveloperLevel().validateExperienceYears(
                request.getExperienceYears());

        developerRepository.findByMemberId(request.getMemberId())
                .ifPresent((developer -> {
                    throw new DMakerException(DUPLICATED_MEMBER_ID);
                }));
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<DeveloperDto> getAllEmployedDevelopers() {
        return developerRepository.findDevelopersByStatusCodeEquals(StatusCode.EMPLOYED)
                .stream().map(DeveloperDto::fromEntity)
                .collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public DeveloperDetailDto getDeveloperDetail(String memberId) {
        return DeveloperDetailDto.fromEntity(getDeveloperByMemberId(memberId));
    }

    private Developer getDeveloperByMemberId(String memberId) {
        return developerRepository.findByMemberId(memberId).orElseThrow(
                () -> new DMakerException(NO_DEVELOPER)
        );
    }

    @Transactional
    public DeveloperDetailDto editDeveloper(String memberId, EditDeveloper.Request request) {
        request.getDeveloperLevel().validateExperienceYears(
                request.getExperienceYears());

        return DeveloperDetailDto.
                fromEntity(getUpdatedDeveloperFromRequest(request, getDeveloperByMemberId(memberId)));
    }

    private Developer getUpdatedDeveloperFromRequest(EditDeveloper.Request request,
                                                     Developer developer) {
        developer.setDeveloperLevel(request.getDeveloperLevel());
        developer.setDeveloperSkillType(request.getDeveloperSkillType());
        developer.setExperienceYears(request.getExperienceYears());

        return developer;
    }

    @Transactional
    public DeveloperDetailDto deleteDeveloper(String memberId) {
        // 1. EMPLOYED -> RETIRED
        Developer developer = developerRepository.findByMemberId(memberId)
                .orElseThrow(() ->
                        new DMakerException(NO_DEVELOPER));
        developer.setStatusCode(StatusCode.RETIRED);

        // 2. save into RetiredDeveloper
        RetiredDeveloper retiredDeveloper = RetiredDeveloper.builder()
                .memberId(memberId)
                .name(developer.getName())
                .build();

        retiredDeveloperRepository.save(retiredDeveloper);
        return DeveloperDetailDto.fromEntity(developer);
    }
}
