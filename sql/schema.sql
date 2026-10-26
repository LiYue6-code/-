-- =====================================================
-- 就业管理系统 数据库初始化脚本
-- 数据库：employment_db
-- 执行方式：mysql -u root -p < schema.sql
-- =====================================================

CREATE DATABASE IF NOT EXISTS employment_db
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE employment_db;

-- 学生表
DROP TABLE IF EXISTS student;
CREATE TABLE student (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    student_no  VARCHAR(20)  NOT NULL COMMENT '学号（登录账号）',
    password    VARCHAR(100) NOT NULL COMMENT '密码',
    name        VARCHAR(50)  NOT NULL COMMENT '姓名',
    major       VARCHAR(50)  DEFAULT NULL COMMENT '专业',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    email       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_no (student_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生';

-- 学校负责人表
DROP TABLE IF EXISTS admin;
CREATE TABLE admin (
    id       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username VARCHAR(50)  NOT NULL COMMENT '登录账号',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    name     VARCHAR(50)  NOT NULL COMMENT '姓名',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学校负责人';

-- 招聘单位表
DROP TABLE IF EXISTS company;
CREATE TABLE company (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(100) NOT NULL COMMENT '单位名称',
    type        VARCHAR(50)  DEFAULT NULL COMMENT '单位性质',
    contact     VARCHAR(50)  DEFAULT NULL COMMENT '联系人',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    address     VARCHAR(200) DEFAULT NULL COMMENT '单位地址',
    description VARCHAR(500) DEFAULT NULL COMMENT '单位简介',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招聘单位';

-- 岗位表
DROP TABLE IF EXISTS job;
CREATE TABLE job (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    company_id   BIGINT       NOT NULL COMMENT '招聘单位ID',
    title        VARCHAR(100) NOT NULL COMMENT '岗位名称',
    description  TEXT         COMMENT '岗位描述',
    requirement  VARCHAR(500) DEFAULT NULL COMMENT '任职要求',
    headcount    INT          DEFAULT 1 COMMENT '招聘人数',
    salary       VARCHAR(50)  DEFAULT NULL COMMENT '薪资待遇',
    status       VARCHAR(20)  NOT NULL DEFAULT '招聘中' COMMENT '状态：招聘中/已截止',
    publish_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    PRIMARY KEY (id),
    KEY idx_company (company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位';

-- 申请表
DROP TABLE IF EXISTS job_application;
CREATE TABLE job_application (
    id              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    job_id          BIGINT      NOT NULL COMMENT '岗位ID',
    student_id      BIGINT      NOT NULL COMMENT '学生ID',
    apply_time      DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    status          VARCHAR(20) NOT NULL DEFAULT '待审核' COMMENT '审核状态：待审核/通过/未通过',
    interview_time  DATETIME    DEFAULT NULL COMMENT '面试时间',
    interview_result VARCHAR(50) DEFAULT NULL COMMENT '面试结果',
    hire_status     VARCHAR(20) DEFAULT NULL COMMENT '录用状态：已录用/未录用',
    PRIMARY KEY (id),
    UNIQUE KEY uk_job_student (job_id, student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位申请';

-- 就业信息表
DROP TABLE IF EXISTS employment_info;
CREATE TABLE employment_info (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    student_id    BIGINT       NOT NULL COMMENT '学生ID',
    company_name  VARCHAR(100) NOT NULL COMMENT '就业单位名称',
    position      VARCHAR(100) DEFAULT NULL COMMENT '岗位',
    salary        VARCHAR(50)  DEFAULT NULL COMMENT '薪资待遇',
    proof_file    VARCHAR(200) DEFAULT NULL COMMENT '证明材料文件名',
    status        VARCHAR(20)  NOT NULL DEFAULT '待审核' COMMENT '状态：待审核/已确认/已退回',
    review_comment VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    submit_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '上报时间',
    PRIMARY KEY (id),
    KEY idx_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='就业信息';
