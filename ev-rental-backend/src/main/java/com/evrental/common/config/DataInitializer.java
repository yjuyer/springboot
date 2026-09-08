package com.evrental.common.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.evrental.business.entity.Vehicle;
import com.evrental.business.mapper.VehicleMapper;
import com.evrental.common.enums.VehicleStatusEnum;
import com.evrental.common.lock.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

/**
 * 数据初始化器 - 启动时自动执行SQL初始化 + Redis库存同步
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DataSource dataSource;
    private final VehicleMapper vehicleMapper;
    private final StockService stockService;

    @Override
    public void run(String... args) {
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            // 创建图片表
            populator.addScript(new ClassPathResource("sql/image_blob.sql"));
            // 更新车辆图片路径
            populator.addScript(new ClassPathResource("sql/data-update.sql"));
            populator.setContinueOnError(true);
            populator.execute(dataSource);
            log.info("数据库初始化完成");
        } catch (Exception e) {
            log.warn("数据库初始化跳过: {}", e.getMessage());
        }

        // 同步车辆库存到Redis
        initVehicleStock();
    }

    /**
     * 初始化所有车辆的Redis库存状态
     * 空闲状态的车辆设为可租(1)，其他状态设为不可租(0)
     */
    private void initVehicleStock() {
        try {
            List<Vehicle> vehicles = vehicleMapper.selectList(
                    new LambdaQueryWrapper<Vehicle>().eq(Vehicle::getDeleted, 0)
            );
            for (Vehicle vehicle : vehicles) {
                boolean available = vehicle.getVehicleStatus() != null
                        && vehicle.getVehicleStatus() == VehicleStatusEnum.IDLE.getCode();
                stockService.initStock(vehicle.getId(), available);
            }
            log.info("Redis车辆库存初始化完成，共{}辆车", vehicles.size());
        } catch (Exception e) {
            log.error("Redis车辆库存初始化失败: {}", e.getMessage());
        }
    }
}
