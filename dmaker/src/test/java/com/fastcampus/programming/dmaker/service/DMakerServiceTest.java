package com.fastcampus.programming.dmaker.service;

import com.fastcampus.programming.dmaker.code.StatusCode;
import com.fastcampus.programming.dmaker.constant.DMakerConstant;
import com.fastcampus.programming.dmaker.dto.CreateDeveloper;
import com.fastcampus.programming.dmaker.dto.DeveloperDetailDto;
import com.fastcampus.programming.dmaker.entity.Developer;
import com.fastcampus.programming.dmaker.exception.DMakerErrorCode;
import com.fastcampus.programming.dmaker.exception.DMakerException;
import com.fastcampus.programming.dmaker.repository.DeveloperRepository;
import com.fastcampus.programming.dmaker.type.DeveloperLevel;
import com.fastcampus.programming.dmaker.type.DeveloperSkillType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.fastcampus.programming.dmaker.constant.DMakerConstant.*;
import static com.fastcampus.programming.dmaker.exception.DMakerErrorCode.*;
import static com.fastcampus.programming.dmaker.type.DeveloperLevel.*;
import static com.fastcampus.programming.dmaker.type.DeveloperSkillType.BACK_END;
import static com.fastcampus.programming.dmaker.type.DeveloperSkillType.FRONT_END;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DMakerServiceTest {
    @Mock
    private DeveloperRepository developerRepository;

//    @Mock
//    private RetiredDeveloperRepository retiredDeveloperRepository;

    @InjectMocks
    private DMakerService dMakerService;


    private final Developer defaultDeveloper = Developer.builder()
            .developerLevel(SENIOR)
            .developerSkillType(BACK_END)
            .experienceYears(12)
            .memberId("test")
            .statusCode(StatusCode.EMPLOYED)
            .name("name")
            .age(11)
            .build();

    private CreateDeveloper.Request getCreateRequest (
            DeveloperLevel developerLevel,
            DeveloperSkillType developerSkillType,
            Integer experienceYears
    ) {

        return CreateDeveloper.Request.builder()
                .developerLevel(developerLevel)
                .developerSkillType(developerSkillType)
                .experienceYears(experienceYears)
                .memberId("test")
                .name("name")
                .age(32)
                .build();
    }

    @Test
    public void testSomething() {
        //given
        given(developerRepository.findByMemberId(anyString()))
                .willReturn(Optional.of(defaultDeveloper));

        //when
        DeveloperDetailDto developerDetail = dMakerService.getDeveloperDetail("test");

        //then
        assertEquals(SENIOR, developerDetail.getDeveloperLevel());
        assertEquals(DeveloperSkillType.BACK_END, developerDetail.getDeveloperSkillType());
        assertEquals(11, developerDetail.getAge());
    }

    @Test
    void createDeveloperTest_success() {
        //given
        given(developerRepository.findByMemberId(anyString()))
                .willReturn(Optional.empty());
        given(developerRepository.save(any()))
                .willReturn(defaultDeveloper);
        ArgumentCaptor<Developer> captor = ArgumentCaptor.forClass(Developer.class);

        //when
        dMakerService.createDeveloper(getCreateRequest(SENIOR, FRONT_END, MIN_SENIOR_EXPERIENCE_YEARS));

        //then
        verify(developerRepository, times(1)).save(captor.capture());
        Developer savedDeveloper = captor.getValue();
        assertEquals(SENIOR, savedDeveloper.getDeveloperLevel());
        assertEquals(FRONT_END, savedDeveloper.getDeveloperSkillType());
        assertEquals(MIN_SENIOR_EXPERIENCE_YEARS, savedDeveloper.getExperienceYears());
    }

    @Test
    void createDeveloperTest_fail_with_unmatched_level() {
        //given
        //when
        //then
        DMakerException dMakerException = assertThrows(DMakerException.class,
                () -> dMakerService.createDeveloper(
                        getCreateRequest(JUNIOR, BACK_END,
                                MAX_JUNIOR_EXPERIENCE_YEARS + 1)));

        assertEquals(LEVEL_EXPERIENCE_YEARS_NOT_MATCHED, dMakerException.getDMakerErrorCode());

        dMakerException = assertThrows(DMakerException.class,
                () -> dMakerService.createDeveloper(
                        getCreateRequest(JUNGNIOR, BACK_END,
                                MIN_SENIOR_EXPERIENCE_YEARS + 1)));

        assertEquals(LEVEL_EXPERIENCE_YEARS_NOT_MATCHED, dMakerException.getDMakerErrorCode());

        dMakerException = assertThrows(DMakerException.class,
                () -> dMakerService.createDeveloper(
                        getCreateRequest(SENIOR, BACK_END,
                                MIN_SENIOR_EXPERIENCE_YEARS - 1)));

        assertEquals(LEVEL_EXPERIENCE_YEARS_NOT_MATCHED, dMakerException.getDMakerErrorCode());
    }


    @Test
    void createDeveloperTest_failed_with_duplicated() {
        //given
        given(developerRepository.findByMemberId(anyString()))
                .willReturn(Optional.of(defaultDeveloper));
        ArgumentCaptor.forClass(Developer.class);

        //when
        //then
        DMakerException dMakerException = assertThrows(DMakerException.class,
                () -> dMakerService.createDeveloper(
                        getCreateRequest(SENIOR, BACK_END, MIN_SENIOR_EXPERIENCE_YEARS)));

        assertEquals(DUPLICATED_MEMBER_ID, dMakerException.getDMakerErrorCode());

    }

}