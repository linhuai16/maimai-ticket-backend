package com.example.maimaibackend.mapper;

import com.example.maimaibackend.dto.audience.AudienceSaveDTO;
import com.example.maimaibackend.vo.audience.AudienceDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AudienceMapper {

    List<AudienceDetailVO> selectAudienceList(@Param("userId") Long userId);

    AudienceDetailVO selectAudienceDetail(@Param("userId") Long userId, @Param("audienceId") Long audienceId);

    int countByUserId(@Param("userId") Long userId);

    int countDefaultByUserId(@Param("userId") Long userId);

    int countDuplicateCertificate(@Param("userId") Long userId, @Param("certificateNoHash") String certificateNoHash);

    int countDuplicateCertificateExcludeSelf(@Param("userId") Long userId,
                                             @Param("audienceId") Long audienceId,
                                             @Param("certificateNoHash") String certificateNoHash);

    int clearDefault(@Param("userId") Long userId);

    int insertAudience(AudienceSaveDTO audience);

    int updateAudience(AudienceSaveDTO audience);

    int deleteAudience(@Param("userId") Long userId, @Param("audienceId") Long audienceId);

    int setDefault(@Param("userId") Long userId, @Param("audienceId") Long audienceId);

    Long selectFirstAudienceId(@Param("userId") Long userId);
}
