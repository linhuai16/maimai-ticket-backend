package com.example.maimaibackend.mapper;

import com.example.maimaibackend.vo.search.SearchHistoryItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SearchMapper {

    List<SearchHistoryItemVO> selectSearchHistory(@Param("userId") Long userId);

    int upsertSearchHistory(@Param("userId") Long userId, @Param("keyword") String keyword);

    int deleteSearchHistory(@Param("userId") Long userId, @Param("historyId") Long historyId);

    int clearSearchHistory(@Param("userId") Long userId);
}
