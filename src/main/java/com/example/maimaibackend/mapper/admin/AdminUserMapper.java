package com.example.maimaibackend.mapper.admin;

import com.example.maimaibackend.vo.admin.AdminUserDetailVO;
import com.example.maimaibackend.vo.admin.AdminUserItemVO;
import com.example.maimaibackend.vo.admin.AdminUserWantVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminUserMapper {
    Integer countUserList(@Param("keyword") String keyword,
                          @Param("accountStatus") String accountStatus);

    List<AdminUserItemVO> selectUserList(@Param("keyword") String keyword,
                                         @Param("accountStatus") String accountStatus,
                                         @Param("limit") Integer limit,
                                         @Param("offset") Integer offset);

    AdminUserDetailVO selectUserDetail(@Param("userId") Long userId);
    List<AdminUserWantVO> selectUserWants(@Param("userId") Long userId);
    String selectUserStatus(@Param("userId") Long userId);
    Integer updateUserStatusIfCurrent(@Param("userId") Long userId,
                                      @Param("currentStatus") String currentStatus,
                                      @Param("targetStatus") String targetStatus);
}
