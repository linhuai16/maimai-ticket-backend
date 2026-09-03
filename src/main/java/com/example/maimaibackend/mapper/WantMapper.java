package com.example.maimaibackend.mapper;

import com.example.maimaibackend.vo.want.WantPerformanceItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WantMapper {

    List<WantPerformanceItemVO> selectWantList(@Param("userId") Long userId);

    int countProjectCanShow(@Param("projectId") Long projectId);

    int insertWantIfAbsent(@Param("userId") Long userId, @Param("projectId") Long projectId);

    int deleteWant(@Param("userId") Long userId, @Param("projectId") Long projectId);

    int increaseWantCount(@Param("projectId") Long projectId);

    int decreaseWantCount(@Param("projectId") Long projectId);
}
