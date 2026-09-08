package com.evrental.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evrental.business.entity.DepositRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DepositRecordMapper extends BaseMapper<DepositRecord> {

    @Select("<script>" +
            "SELECT d.*, u.username, u.phone, " +
            "v.model AS vehicle_info " +
            "FROM deposit_record d " +
            "LEFT JOIN sys_user u ON d.user_id = u.id " +
            "LEFT JOIN vehicle v ON d.vehicle_id = v.id " +
            "WHERE d.deleted = 0 " +
            "<if test='status != null'> AND d.status = #{status}</if>" +
            "<if test='orderNo != null and orderNo != \"\"'> AND d.order_no LIKE CONCAT('%',#{orderNo},'%')</if>" +
            " ORDER BY d.create_time DESC" +
            "</script>")
    IPage<DepositRecord> selectDepositPage(Page<DepositRecord> page,
                                           @Param("status") Integer status,
                                           @Param("orderNo") String orderNo);
}