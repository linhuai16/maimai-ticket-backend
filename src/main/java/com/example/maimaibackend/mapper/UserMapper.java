package com.example.maimaibackend.mapper;

import com.example.maimaibackend.vo.user.MineUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    int countUserById(@Param("userId") Long userId);

    MineUserVO selectUserById(@Param("userId") Long userId);

    MineUserVO selectUserByPhone(@Param("phone") String phone);

    int countPhoneUsedByOther(@Param("phone") String phone, @Param("userId") Long userId);

    int insertUser(MineUserVO user);

    int updateUserProfile(@Param("userId") Long userId,
                          @Param("nickname") String nickname,
                          @Param("phone") String phone,
                          @Param("avatarUrl") String avatarUrl);
}
