package com.example.maimaibackend.mapper.admin;

import com.example.maimaibackend.dto.admin.AdminVenueSaveDTO;
import com.example.maimaibackend.vo.admin.AdminVenueVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminVenueMapper {
    Integer countVenueList(@Param("keyword") String keyword,
                           @Param("cityName") String cityName);

    List<AdminVenueVO> selectVenueList(@Param("keyword") String keyword,
                                       @Param("cityName") String cityName,
                                       @Param("limit") Integer limit,
                                       @Param("offset") Integer offset);

    String selectCanonicalCityName(@Param("cityName") String cityName);

    String selectMatchedCityNameByStationName(@Param("stationName") String stationName);

    List<AdminVenueVO> selectVenueOptions(@Param("cityName") String cityName,
                                          @Param("limit") Integer limit);

    AdminVenueVO selectVenueDetail(@Param("venueId") Long venueId);

    Integer countVenueById(@Param("venueId") Long venueId);

    Integer countDuplicateVenue(@Param("cityName") String cityName,
                                @Param("venueName") String venueName,
                                @Param("address") String address,
                                @Param("excludeVenueId") Long excludeVenueId);

    Integer insertVenue(AdminVenueSaveDTO dto);

    Integer updateVenue(AdminVenueSaveDTO dto);

    Integer countSessionsByVenueId(@Param("venueId") Long venueId);

    Integer deleteVenue(@Param("venueId") Long venueId);
}
