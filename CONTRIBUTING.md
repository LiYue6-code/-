# 协作规范（CONTRIBUTING）

就业管理系统 · 软件工程综合实训 · 第 22 组

> 这份文档只解决一件事：**四个人同时改一个仓库，怎么不互相踩脚、不把 main 弄坏。**
> 3 分钟能读完，建议开工前看一遍。

---

## 0. 拉取后必做（先看这个）

```bash
git pull
mysql -u root -p employment_db < sql/migration_v2.sql   # 只执行一次！
```

- **不跑迁移会启动报错**（`audit_log` 表/`reviewer_id` 等字段不存在）
- **协作者邀请要去 GitHub 通知里点 Accept**，否则 `pull` 正常但 `push` 报 `403 denied`
  自查：`gh api repos/LiYue6-code/- --jq .permissions` → `push` 要为 `true`
- 细节见 [README 顶部「拉取后必做」](README.md) 与 [交付说明](docs/交付说明_审核与统计模块.md) 第 0 节

## 1. 环境（版本必须一致，否则构建结果不同）

| 项 | 版本 | 备注 |
|---|---|---|
| JDK | **17** | 用 IDEA 自带 JDK 也行；本机默认 `java -version` 可能是 1.8，注意切 |
| Maven | 3.8+ | 没有独立安装可用 IDEA 内置 Maven |
| MySQL | 8.0 | 库名 `employment_db`，连接配置在 `application.yml` |

```bash
mvn clean test      # 提交前必须能过
mvn spring-boot:run # 本地起服务，默认 8080
```

## 2. 分支与提交

### 分支

- `main` 始终保持**能编译、能启动**
- 开发新功能：`git checkout -b feature/<模块>-<简述>`，如 `feature/review-interview`
- 修 bug：`fix/<简述>`，如 `fix/duplicate-apply`
- 合并前必须：`git pull --rebase origin main` → 解决冲突 → `mvn clean test` 通过 → 再推

> 赶进度时允许直接改 `main`，但**改公共文件前必须 `git pull`**（见第 4 节），并且推之前保证本地能起来。

### 提交信息

格式：`type(scope): 中文描述`

| type | 用途 |
|---|---|
| `feat` | 新功能 |
| `fix` | 修 bug |
| `docs` | 文档 |
| `refactor` | 重构（不改行为） |
| `test` | 测试 |
| `chore` | 构建/配置 |

scope 用模块名：`db` `auth` `student` `job` `review` `stats` `ui` `docs`

示例：

```
feat(review): 申请审核增加面试时间登记
fix(stats): 就业率分母为 0 时返回 0 而不是 NaN
docs: 补充数据库迁移执行顺序
```

规则：

- **一次提交只做一件事**，不要把「改样式 + 加接口 + 改表结构」塞进一个提交
- 不要提交：`target/`、`uploads/`、`.idea/`、`*.iml`、`application-local.yml`（已在 `.gitignore`）
- 不要提交真实数据库密码；本机配置写 `application-local.yml`

## 3. 数据库变更（最容易出事的地方）

1. **不许改别人已经执行过的脚本**（`schema.sql`、`data.sql`、`migration.sql`、`migration_v2.sql`）——别人本地已经跑过，改了他不知道，两边库结构就不一致了
2. 任何表结构变更 → **新建 `sql/migration_vN.sql`**，内容只包含本次的 `ALTER/CREATE`
3. 脚本必须是**幂等或只执行一次可接受**的；MySQL 8.0 没有 `ADD COLUMN IF NOT EXISTS`，所以在文件头写清「只能执行一次」
4. 改完在群里发一句：**「新增 migration_v3.sql，拉完先执行，加了 x 表/字段」**
5. 脚本末尾附回滚语句（注释状态）

状态字段取值是**中文固定值**，不要自造新值：

| 业务 | 字段 | 合法值 |
|---|---|---|
| 岗位申请 | `status` | 待审核 / 通过 / 未通过 |
| 岗位申请 | `interview_result` | 未安排 / 待面试 / 面试通过 / 面试未通过 |
| 岗位申请 | `hire_status` | 待定 / 已录用 / 未录用 |
| 就业上报 | `status` | 待审核 / 已确认 / 已退回 |

合法流转由 `ApplicationFlow` / `EmploymentFlow` 统一校验，**不要在 Controller 里自己写 if 判断状态**。

## 4. 公共文件（冲突热点，改前先 pull）

以下文件被多人共用，改动前必须 `git pull`，改完在群里说一声：

| 文件 | 冲突风险 | 约定 |
|---|---|---|
| `static/css/style.css` | 高 | 全局主题在上，各模块样式**追加在末尾**并写明模块名；冲突时**两边保留**，不要整段覆盖 |
| `templates/fragments.html` | 中 | 导航栏片段名为 `nav`，不要改名字（所有页面都引用它） |
| `application.yml` | 中 | `mybatis.mapper-locations` 和 `app.upload-dir` **不能删**，删了统计页/上传直接报错 |
| `pom.xml` | 中 | 加依赖先在群里说，避免两人同时改依赖版本 |
| `sql/schema.sql` | 高 | 见第 3 节，用 migration 脚本 |

审核/统计模块的样式块在 `style.css` 末尾，从 `/* ===== 审核与统计模块新增样式（张伟旗） ===== */` 开始。

## 5. 模块边界（谁的文件谁负责）

| 模块 | 负责人 | 主要文件 |
|---|---|---|
| 架构、登录权限、学生信息 | 李越 | `AuthController`、`StudentController`、`common/LoginInterceptor`、`common/CsrfInterceptor`、`config/*`、`StudentMapper`、`sql/schema.sql` |
| 学生端（岗位查询申请、就业上报、进度） | 杨丰隆 | `JobController`（学生部分）、`EmploymentController`（学生部分）、`templates/job-*`、`templates/my-*` |
| 单位与岗位（负责人端） | 王海宁 | `CompanyController`、`JobController`（负责人部分）、`templates/admin/company-*`、`templates/admin/job-*` |
| 审核与统计（负责人端） | 张伟旗 | `ApplicationController`、`EmploymentController`（审核部分）、`StatisticsController`、`service/*Flow`、`AuditService`、`FileStorageService`、`resources/mapper/*.xml`、`templates/admin/application-*`、`templates/admin/employment-*`、`templates/admin/statistics` |
| 公共 | 全体 | `style.css`、`fragments.html`、`application.yml`、`pom.xml` |

> ⚠️ `EmploymentController.java` 是**共享文件**：上半部分「学生端」归杨丰隆，下半部分「负责人端」归张伟旗。改的时候只动自己那半，动手前先 `git pull`。

**跨模块改动**（比如改别人的 Mapper 或模板）：先在群里说，或者由负责人来改，别直接覆盖。

## 6. 代码约定

- **分层**：`Controller` 只做参数接收与页面跳转；业务判断放 `Service`；SQL 放 `Mapper`
- **复杂 SQL 用 XML**（`resources/mapper/*.xml`），简单的单表增删改查用注解，两者不要重复定义同一个方法
- **业务错误统一抛 `BizException`**（带中文提示），由 `GlobalExceptionHandler` 转错误页；**不要**在 Controller 里 `try/catch` 后返回 500
- **分页统一用 `PageResult`**，查询条件用 `query/` 下的对象，不要给 Controller 塞十几个 `@RequestParam`
- **时间字段统一 `LocalDateTime`**，前端 `datetime-local` 提交的字符串用 `DateTimes.parse()` 解析
- **不要再引入第二套状态字段**，也不要改状态中文值（会影响统计口径）
- 文件上传一律走 `FileStorageService`（UUID 重命名 + 扩展名白名单 + 大小限制），**不要**自己拼 `originalFilename`

## 7. 提交前自检清单

推之前逐条过一遍：

- [ ] `git pull --rebase origin main` 已执行，没有冲突残留（`git status` 干净）
- [ ] `mvn clean test` 全部通过
- [ ] 本地能启动，并且**点通了自己改动涉及的所有页面**（模板错误只有运行时才暴露）
- [ ] 没有提交 `target/`、`uploads/`、`.idea/`、本地数据库配置
- [ ] 有表结构变更 → 已新建 `migration_vN.sql` 并在群里通知
- [ ] 改了公共文件（`style.css` / `fragments.html` / `application.yml`）→ 已在群里说
- [ ] 提交信息符合 `type(scope): 描述`

## 8. 常见报错速查

| 现象 | 原因 | 解决 |
|---|---|---|
| `remote: Permission to LiYue6-code/-.git denied to xxx` + `403` | 协作者邀请没接受 | 去 GitHub 通知/邮件点 Accept |
| 启动报 `Table 'employment_db.audit_log' doesn't exist` | 没跑 `migration_v2.sql` | 执行该脚本（只跑一次） |
| 启动报 `Unknown column 'reviewer_id'` | 同上 | 同上 |
| `Invalid bound statement (not found)` | `application.yml` 里 `mybatis.mapper-locations` 被删了 | 恢复该配置 |
| 页面 500，日志是模板解析错误 | 用了 `'文字' + ${expr}` 这类混写 | 改用 `|文字 ${expr}|` 字面量替换 |
| `Web server failed to start. Port 8080 was already in use` | 端口占用 | `--server.port=8081` |
| `invalid target release: 17` / 编译报 `Release 17` 不支持 | JDK 还是 1.8 | IDEA 里切 JDK 17 / `JAVA_HOME` |
| 启动后所有页面 403 | 表单少了 `_csrf` 隐藏域 | POST 表单必须加 `<input type="hidden" name="_csrf" th:value="${_csrf}">` |
| 中文日志乱码 | 控制台是 GBK | 不影响功能；要看日志加 `-Dfile.encoding=UTF-8` |

## 9. 谁负责什么流程

- **需求/进度对外沟通**：李越（对老师、对用户）
- **代码评审**：组长抽检 + 各模块负责人互查
- **测试与验收**：张伟旗牵头，全体执行
- **合并冲突处理**：谁的分支谁解决；公共文件冲突找组长拍板
