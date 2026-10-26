-- =====================================================
-- 就业管理系统 数据库增量迁移脚本（登录安全加固）
-- 适用：已经执行过旧版 schema.sql 的数据库
-- 执行方式：mysql -u root -p employment_db < migration.sql
-- =====================================================
USE employment_db;

-- 学生表增加登录失败锁定字段
ALTER TABLE student
    ADD COLUMN failed_count INT NOT NULL DEFAULT 0 COMMENT '连续登录失败次数' AFTER email,
    ADD COLUMN locked_until DATETIME DEFAULT NULL COMMENT '账号锁定截止时间' AFTER failed_count;

-- 学生表增加账号状态字段（学生信息维护模块）
ALTER TABLE student
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT '正常' COMMENT '状态：正常/禁用' AFTER email;

-- 负责人表增加登录失败锁定字段
ALTER TABLE admin
    ADD COLUMN failed_count INT NOT NULL DEFAULT 0 COMMENT '连续登录失败次数' AFTER name,
    ADD COLUMN locked_until DATETIME DEFAULT NULL COMMENT '账号锁定截止时间' AFTER failed_count;

-- 说明：已存在的明文密码会在应用首次启动时由 PasswordMigrationRunner 自动升级为 BCrypt 密文，无需手动处理。
