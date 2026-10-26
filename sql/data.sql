-- =====================================================
-- 就业管理系统 初始化数据
-- 说明：默认密码 123456 为明文占位，应用首次启动时
--       PasswordMigrationRunner 会自动将其升级为 BCrypt 密文。
-- =====================================================
USE employment_db;

-- 学校负责人（默认账号：admin / 123456）
INSERT INTO admin (username, password, name) VALUES ('admin', '123456', '就业负责人');

-- 学生（默认账号：学号 / 123456）
INSERT INTO student (student_no, password, name, major, phone, email) VALUES
('20230001', '123456', '张三', '软件工程', '13800000001', 'zhangsan@example.com'),
('20230002', '123456', '李四', '计算机科学与技术', '13800000002', 'lisi@example.com');

-- 招聘单位
INSERT INTO company (name, type, contact, phone, address, description) VALUES
('某信息技术有限公司', '民营企业', '王经理', '010-12345678', '北京市海淀区', '专注于软件开发与技术服务。'),
('某网络科技有限公司', '民营企业', '刘经理', '021-87654321', '上海市浦东新区', '从事互联网平台开发。');

-- 岗位
INSERT INTO job (company_id, title, description, requirement, headcount, salary, status) VALUES
(1, 'Java 开发工程师', '负责后端系统开发与维护。', '熟悉 Java、Spring Boot、MySQL。', 3, '8000-12000', '招聘中'),
(1, 'Web 前端工程师', '负责前端页面开发。', '熟悉 HTML/CSS/JavaScript。', 2, '7000-10000', '招聘中'),
(2, '软件测试工程师', '负责产品测试与质量保障。', '熟悉测试流程与用例设计。', 2, '6000-9000', '招聘中');
