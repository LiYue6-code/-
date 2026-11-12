-- =====================================================
-- 就业管理系统 数据库增量迁移脚本 V3
-- 模块：简历与求职意向、岗位审核/上下架/有效期、单位资质审核、站内消息通知（第一优先级）
-- 适用：已经执行过 migration.sql + migration_v2.sql 的数据库
-- 执行方式：mysql -u root -p employment_db < migration_v3.sql
-- 注意：MySQL 8.0 不支持 ADD COLUMN IF NOT EXISTS，本脚本只能执行一次。
-- =====================================================
USE employment_db;

-- -----------------------------------------------------
-- 1. 招聘单位表：资质材料与资质审核、信用状态
--    资质材料沿用就业证明材料的安全存储约定（FileStorageService），
--    qualification_file 存 UUID 文件名，qualification_name 存原始文件名用于展示/下载。
-- -----------------------------------------------------
ALTER TABLE company
    ADD COLUMN qualification_file VARCHAR(200) DEFAULT NULL COMMENT '资质材料文件名（UUID 存储名）' AFTER description,
    ADD COLUMN qualification_name VARCHAR(200) DEFAULT NULL COMMENT '资质材料原始文件名（展示用）' AFTER qualification_file,
    ADD COLUMN review_status VARCHAR(20) NOT NULL DEFAULT '待审核' COMMENT '资质审核状态：待审核/通过/驳回' AFTER qualification_name,
    ADD COLUMN review_comment VARCHAR(500) DEFAULT NULL COMMENT '资质审核意见' AFTER review_status,
    ADD COLUMN credit_status VARCHAR(20) NOT NULL DEFAULT '正常' COMMENT '信用状态：正常/异常' AFTER review_comment;

-- 存量单位视为已通过资质审核，避免历史数据被卡住
UPDATE company SET review_status = '通过';

-- -----------------------------------------------------
-- 2. 岗位表：岗位审核 + 有效期
--    status 仍沿用原「招聘中/已截止」，作为上架/下架状态；新增 review_status 表示审核状态。
-- -----------------------------------------------------
ALTER TABLE job
    ADD COLUMN review_status VARCHAR(20) NOT NULL DEFAULT '待审核' COMMENT '审核状态：待审核/通过/驳回' AFTER status,
    ADD COLUMN review_comment VARCHAR(500) DEFAULT NULL COMMENT '审核意见' AFTER review_status,
    ADD COLUMN valid_until DATETIME DEFAULT NULL COMMENT '有效期截止时间，过期自动下架' AFTER review_comment;

-- 存量岗位视为已审核通过
UPDATE job SET review_status = '通过';

-- -----------------------------------------------------
-- 3. 岗位申请表：简历关联投递
-- -----------------------------------------------------
ALTER TABLE job_application
    ADD COLUMN resume_id BIGINT DEFAULT NULL COMMENT '关联简历ID' AFTER student_id;

-- -----------------------------------------------------
-- 4. 学生简历表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS resume (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    student_id    BIGINT       NOT NULL COMMENT '学生ID',
    title         VARCHAR(100) NOT NULL COMMENT '简历标题',
    education     VARCHAR(50)  DEFAULT NULL COMMENT '学历',
    school        VARCHAR(100) DEFAULT NULL COMMENT '毕业院校',
    major         VARCHAR(50)  DEFAULT NULL COMMENT '专业',
    graduate_year VARCHAR(10)  DEFAULT NULL COMMENT '毕业年份',
    experience    TEXT         COMMENT '实习/项目经历',
    skills        VARCHAR(500) DEFAULT NULL COMMENT '技能标签',
    self_eval     TEXT         COMMENT '自我评价',
    default_flag  TINYINT      NOT NULL DEFAULT 0 COMMENT '是否默认简历：0否/1是',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_resume_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生简历';

-- -----------------------------------------------------
-- 5. 求职意向表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS job_intention (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    student_id  BIGINT       NOT NULL COMMENT '学生ID',
    industry    VARCHAR(50)  DEFAULT NULL COMMENT '意向行业',
    position    VARCHAR(100) DEFAULT NULL COMMENT '意向岗位',
    city        VARCHAR(50)  DEFAULT NULL COMMENT '意向城市',
    salary      VARCHAR(50)  DEFAULT NULL COMMENT '期望薪资',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_intention_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='求职意向';

-- -----------------------------------------------------
-- 6. 站内消息表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS message (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    receiver_type VARCHAR(20)  NOT NULL COMMENT '接收方类型：student=学生，admin=负责人',
    receiver_id   BIGINT       NOT NULL COMMENT '接收方ID（学生ID或负责人ID）',
    title         VARCHAR(100) NOT NULL COMMENT '消息标题',
    content       VARCHAR(500) DEFAULT NULL COMMENT '消息内容',
    read_flag     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读：0未读/1已读',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (id),
    KEY idx_message_receiver (receiver_type, receiver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息';

-- =====================================================
-- 回滚（如需撤销本次迁移，取消下面注释后执行）
-- =====================================================
-- ALTER TABLE company
--     DROP COLUMN qualification_file, DROP COLUMN qualification_name,
--     DROP COLUMN review_status, DROP COLUMN review_comment, DROP COLUMN credit_status;
-- ALTER TABLE job
--     DROP COLUMN review_status, DROP COLUMN review_comment, DROP COLUMN valid_until;
-- ALTER TABLE job_application DROP COLUMN resume_id;
-- DROP TABLE IF EXISTS resume;
-- DROP TABLE IF EXISTS job_intention;
-- DROP TABLE IF EXISTS message;