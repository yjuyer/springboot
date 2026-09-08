package com.evrental.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evrental.business.entity.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {
}
