-- =====================================================
-- 就业管理系统 数据库增量迁移脚本 V2
-- 模块：申请审核 / 面试 / 录用、就业审核、查询统计（张伟旗）
-- 适用：已经执行过 schema.sql（含 migration.sql）的数据库
-- 执行方式：mysql -u root -p employment_db < migration_v2.sql
-- 注意：MySQL 8.0 不支持 ADD COLUMN IF NOT EXISTS，本脚本只能执行一次。
-- =====================================================
USE employment_db;

-- -----------------------------------------------------
-- 1. 审核操作日志表
--    每发生一次审核动作（初审、安排面试、面试结果、录用、就业确认/退回、重新上报）
--    都写入一条记录，用于“处理进度/时间线”查询与责任追溯。
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_log (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    biz_type      VARCHAR(20)  NOT NULL COMMENT '业务类型：APPLICATION=岗位申请，EMPLOYMENT=就业上报',
    biz_id        BIGINT       NOT NULL COMMENT '业务主键',
    action        VARCHAR(30)  NOT NULL COMMENT '动作编码：SUBMIT/APPROVE/REJECT/SCHEDULE/INTERVIEW/PASS_HIRE/...',
    action_label  VARCHAR(50)  DEFAULT NULL COMMENT '动作名称（中文，直接展示）',
    from_status   VARCHAR(80)  DEFAULT NULL COMMENT '变更前状态描述',
    to_status     VARCHAR(80)  DEFAULT NULL COMMENT '变更后状态描述',
    operator_id   BIGINT       DEFAULT NULL COMMENT '操作人ID',
    operator_name VARCHAR(50)  DEFAULT NULL COMMENT '操作人名称',
    comment       VARCHAR(500) DEFAULT NULL COMMENT '审核意见/备注',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_biz (biz_type, biz_id),
    KEY idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核操作日志（处理进度）';

-- -----------------------------------------------------
-- 2. 岗位申请表：审核/面试/录用过程字段
-- -----------------------------------------------------
ALTER TABLE job_application
    ADD COLUMN reviewer_id    BIGINT       DEFAULT NULL COMMENT '审核人ID' AFTER hire_status,
    ADD COLUMN reviewer_name  VARCHAR(50)  DEFAULT NULL COMMENT '审核人姓名' AFTER reviewer_id,
    ADD COLUMN review_time    DATETIME     DEFAULT NULL COMMENT '最近审核时间' AFTER reviewer_name,
    ADD COLUMN reject_reason  VARCHAR(500) DEFAULT NULL COMMENT '初审不通过原因' AFTER review_time,
    ADD COLUMN interview_place VARCHAR(200) DEFAULT NULL COMMENT '面试地点/方式' AFTER reject_reason,
    ADD COLUMN remark         VARCHAR(500) DEFAULT NULL COMMENT '负责人备注' AFTER interview_place,
    ADD COLUMN update_time    DATETIME     DEFAULT NULL COMMENT '最后更新时间' AFTER remark;

ALTER TABLE job_application
    ADD KEY idx_status (status),
    ADD KEY idx_apply_time (apply_time);

-- -----------------------------------------------------
-- 3. 就业信息表：审核过程字段 + 重新上报次数
-- -----------------------------------------------------
ALTER TABLE employment_info
    ADD COLUMN proof_name  VARCHAR(200) DEFAULT NULL COMMENT '证明材料原始文件名（展示用）' AFTER proof_file,
    ADD COLUMN reviewer_id   BIGINT       DEFAULT NULL COMMENT '审核人ID' AFTER review_comment,
    ADD COLUMN reviewer_name VARCHAR(50)  DEFAULT NULL COMMENT '审核人姓名' AFTER reviewer_id,
    ADD COLUMN review_time   DATETIME     DEFAULT NULL COMMENT '审核时间' AFTER reviewer_name,
    ADD COLUMN resubmit_count INT         NOT NULL DEFAULT 0 COMMENT '退回后重新上报次数' AFTER review_time,
    ADD COLUMN update_time   DATETIME     DEFAULT NULL COMMENT '最后更新时间' AFTER resubmit_count;

ALTER TABLE employment_info
    ADD KEY idx_status (status),
    ADD KEY idx_submit_time (submit_time);

-- -----------------------------------------------------
-- 4. 历史数据处理
--    旧数据中 interview_result / hire_status 可能为 NULL，统一为“未安排/待定”
-- -----------------------------------------------------
UPDATE job_application SET interview_result = '未安排' WHERE interview_result IS NULL OR interview_result = '';
UPDATE job_application SET hire_status      = '待定'   WHERE hire_status      IS NULL OR hire_status      = '';

-- =====================================================
-- 回滚（如需撤销本次迁移，取消下面注释后执行）
-- =====================================================
-- DROP TABLE IF EXISTS audit_log;
-- ALTER TABLE job_application
--     DROP COLUMN reviewer_id, DROP COLUMN reviewer_name, DROP COLUMN review_time,
--     DROP COLUMN reject_reason, DROP COLUMN interview_place, DROP COLUMN remark,
--     DROP COLUMN update_time, DROP KEY idx_status, DROP KEY idx_apply_time;
-- ALTER TABLE employment_info
--     DROP COLUMN reviewer_id, DROP COLUMN reviewer_name, DROP COLUMN review_time,
--     DROP COLUMN resubmit_count, DROP COLUMN update_time,
--     DROP KEY idx_status, DROP KEY idx_submit_time;
