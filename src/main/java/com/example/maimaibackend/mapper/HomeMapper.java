package com.example.maimaibackend.mapper;

import com.example.maimaibackend.vo.home.BannerItemVO;
import com.example.maimaibackend.vo.home.CategoryItemVO;
import com.example.maimaibackend.vo.home.HomeRecommendItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface HomeMapper {

    List<BannerItemVO> selectActiveBanners();

    List<CategoryItemVO> selectHomeCategories();

    List<HomeRecommendItemVO> selectHomeRecommendList(
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );
}
