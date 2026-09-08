package com.evrental.business.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evrental.business.entity.*;
import com.evrental.business.mapper.*;
import com.evrental.common.exception.BusinessException;
import com.evrental.system.entity.SysUser;
import com.evrental.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * 电子发票服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceMapper invoiceMapper;
    private final InvoiceItemMapper invoiceItemMapper;
    private final RentalOrderMapper orderMapper;
    private final SysUserMapper userMapper;
    private final VehicleMapper vehicleMapper;

    /**
     * 用户申请发票
     */
    @Transactional
    public void applyInvoice(Long userId, Long orderId, Integer invoiceType,
                             String invoiceTitle, String taxNumber, String email) {
        // 检查订单是否存在且属于当前用户
        RentalOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() < 5) {
            throw new BusinessException("订单尚未完成，无法申请发票");
        }

        // 检查是否已申请过
        Long count = invoiceMapper.selectCount(new LambdaQueryWrapper<Invoice>()
                .eq(Invoice::getOrderId, orderId)
                .eq(Invoice::getUserId, userId));
        if (count > 0) {
            throw new BusinessException("该订单已申请过发票");
        }

        // 企业发票必须填税号
        if (invoiceType == 2 && (taxNumber == null || taxNumber.trim().isEmpty())) {
            throw new BusinessException("企业发票必须填写税号");
        }

        Invoice invoice = new Invoice();
        invoice.setOrderId(orderId);
        invoice.setOrderNo(order.getOrderNo());
        invoice.setUserId(userId);
        invoice.setInvoiceType(invoiceType);
        invoice.setInvoiceTitle(invoiceTitle);
        invoice.setTaxNumber(taxNumber);
        invoice.setEmail(email);
        invoice.setAmount(order.getTotalAmount());
        invoice.setStatus(0);
        invoice.setApplyTime(LocalDateTime.now());
        invoiceMapper.insert(invoice);

        // 创建发票明细
        InvoiceItem item1 = new InvoiceItem();
        item1.setInvoiceId(invoice.getId());
        item1.setItemName("车辆租金");
        item1.setItemAmount(order.getTotalAmount());
        item1.setQuantity(order.getRentalDays());
        invoiceItemMapper.insert(item1);

        log.info("发票申请成功: invoiceId={}, orderNo={}", invoice.getId(), order.getOrderNo());
    }

    /**
     * 用户查询自己的发票列表
     */
    public Map<String, Object> getUserInvoices(Long userId, Integer page, Integer size, Integer status) {
        LambdaQueryWrapper<Invoice> qw = new LambdaQueryWrapper<Invoice>()
                .eq(Invoice::getUserId, userId)
                .orderByDesc(Invoice::getCreateTime);

        if (status != null) {
            qw.eq(Invoice::getStatus, status);
        }

        Page<Invoice> result = invoiceMapper.selectPage(new Page<>(page, size), qw);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("pages", result.getPages());
        return data;
    }

    /**
     * 管理员查询所有发票
     */
    public Map<String, Object> getAllInvoices(Integer page, Integer size, Integer status) {
        LambdaQueryWrapper<Invoice> qw = new LambdaQueryWrapper<Invoice>()
                .orderByDesc(Invoice::getCreateTime);

        if (status != null) {
            qw.eq(Invoice::getStatus, status);
        }

        Page<Invoice> result = invoiceMapper.selectPage(new Page<>(page, size), qw);

        // 填充用户名和车辆信息
        for (Invoice inv : result.getRecords()) {
            SysUser user = userMapper.selectById(inv.getUserId());
            if (user != null) {
                inv.setUsername(user.getUsername());
                inv.setPhone(user.getPhone());
            }
            RentalOrder order = orderMapper.selectById(inv.getOrderId());
            if (order != null) {
                Vehicle vehicle = vehicleMapper.selectById(order.getVehicleId());
                if (vehicle != null) {
                    inv.setVehicleModel(vehicle.getModel());
                }
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("pages", result.getPages());
        return data;
    }

    /**
     * 获取发票详情
     */
    public Invoice getInvoiceDetail(Long invoiceId) {
        Invoice invoice = invoiceMapper.selectById(invoiceId);
        if (invoice == null) {
            throw new BusinessException("发票不存在");
        }
        return invoice;
    }

    /**
     * 管理员审核开票
     */
    @Transactional
    public void auditInvoice(Long invoiceId, String invoiceNo, String invoiceFileUrl, boolean approved, String rejectReason) {
        Invoice invoice = invoiceMapper.selectById(invoiceId);
        if (invoice == null) {
            throw new BusinessException("发票不存在");
        }
        if (invoice.getStatus() != 0) {
            throw new BusinessException("发票状态异常，无法审核");
        }

        if (approved) {
            invoice.setStatus(1);
            invoice.setInvoiceNo(invoiceNo);
            invoice.setInvoiceFileUrl(invoiceFileUrl);
            invoice.setAuditTime(LocalDateTime.now());
            log.info("发票已开具: invoiceId={}, invoiceNo={}", invoiceId, invoiceNo);
        } else {
            invoice.setStatus(3);
            invoice.setRejectReason(rejectReason);
            invoice.setAuditTime(LocalDateTime.now());
            log.info("发票已驳回: invoiceId={}, reason={}", invoiceId, rejectReason);
        }
        invoiceMapper.updateById(invoice);
    }

    /**
     * 管理员标记已发送
     */
    @Transactional
    public void markSent(Long invoiceId) {
        Invoice invoice = invoiceMapper.selectById(invoiceId);
        if (invoice == null) {
            throw new BusinessException("发票不存在");
        }
        if (invoice.getStatus() != 1) {
            throw new BusinessException("发票状态异常，无法标记已发送");
        }
        invoice.setStatus(2);
        invoice.setSendTime(LocalDateTime.now());
        invoiceMapper.updateById(invoice);
        log.info("发票已发送: invoiceId={}", invoiceId);
    }

    /**
     * 获取发票明细列表
     */
    public List<InvoiceItem> getInvoiceItems(Long invoiceId) {
        return invoiceItemMapper.selectList(new LambdaQueryWrapper<InvoiceItem>()
                .eq(InvoiceItem::getInvoiceId, invoiceId));
    }
}
