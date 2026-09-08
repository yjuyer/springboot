-- 更新车辆图片数据
USE nev_rental;

-- 比亚迪车型
UPDATE vehicle SET image = '/images/byd-hanev.png' WHERE model = '汉EV';
UPDATE vehicle SET image = '/images/byd-tangev.png' WHERE model = '唐EV';
UPDATE vehicle SET image = '/images/byd-hanev.png' WHERE model = '海豹';
UPDATE vehicle SET image = '/images/byd-PLUS-ev.png' WHERE model = '秦PLUS EV';
UPDATE vehicle SET image = '/images/byd-tangev.png' WHERE model = '宋PLUS EV';
UPDATE vehicle SET image = '/images/byd-PLUS-ev.png' WHERE model = '元PLUS';

-- 特斯拉车型
UPDATE vehicle SET image = '/images/tesla-m3.png' WHERE model = 'Model 3';
UPDATE vehicle SET image = '/images/tesla-my.png' WHERE model = 'Model Y';
UPDATE vehicle SET image = '/images/tesla-m3.png' WHERE model = 'Model S';
UPDATE vehicle SET image = '/images/tesla-my.png' WHERE model = 'Model X';

-- 蔚来车型
UPDATE vehicle SET image = '/images/nio-es6.png' WHERE model = 'ES6';
UPDATE vehicle SET image = '/images/nio-et5.png' WHERE model = 'ET5';
UPDATE vehicle SET image = '/images/nio-es6.png' WHERE model = 'ES8';
UPDATE vehicle SET image = '/images/nio-et5.png' WHERE model = 'ET7';

-- 小鹏车型
UPDATE vehicle SET image = '/images/xp-p7.png' WHERE model = 'P7';
UPDATE vehicle SET image = '/images/xp-g9.png' WHERE model = 'G9';
UPDATE vehicle SET image = '/images/xp-p7.png' WHERE model = 'P5';
UPDATE vehicle SET image = '/images/xp-g9.png' WHERE model = 'G6';

-- 理想车型
UPDATE vehicle SET image = '/images/li-l7.png' WHERE model = 'L7';
UPDATE vehicle SET image = '/images/li-l7.png' WHERE model = 'L9';
UPDATE vehicle SET image = '/images/li-l7.png' WHERE model = 'MEGA';
UPDATE vehicle SET image = '/images/li-l7.png' WHERE model = 'L6';

-- 问界车型（使用比亚迪图片作为替代）
UPDATE vehicle SET image = '/images/byd-hanev.png' WHERE model = 'M5 EV';
UPDATE vehicle SET image = '/images/byd-tangev.png' WHERE model = 'M7';
UPDATE vehicle SET image = '/images/byd-hanev.png' WHERE model = 'M9';
UPDATE vehicle SET image = '/images/byd-tangev.png' WHERE model = 'M5 增程';

-- 极氪车型（使用蔚来图片作为替代）
UPDATE vehicle SET image = '/images/nio-es6.png' WHERE model = '001';
UPDATE vehicle SET image = '/images/nio-et5.png' WHERE model = '007';
UPDATE vehicle SET image = '/images/nio-es6.png' WHERE model = '009';
UPDATE vehicle SET image = '/images/nio-et5.png' WHERE model = 'X';

-- 零跑车型（使用小鹏图片作为替代）
UPDATE vehicle SET image = '/images/xp-g9.png' WHERE model = 'C11';
UPDATE vehicle SET image = '/images/xp-p7.png' WHERE model = 'C01';
UPDATE vehicle SET image = '/images/xp-g9.png' WHERE model = 'C10';
UPDATE vehicle SET image = '/images/xp-p7.png' WHERE model = 'S01';

-- 验证更新结果
SELECT id, model, image FROM vehicle ORDER BY id;
