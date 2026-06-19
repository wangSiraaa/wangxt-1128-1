# 广告屏刊播排期系统 - 启动与业务回归验证指南

## 一、系统概述

广告屏刊播排期系统是一套面向销售、内容审核和运维人员的协同工作平台，核心功能包括：
- 销售提交客户素材和投放时段
- 审核员确认内容合规性
- 运维人员回传刊播证明
- 排期冲突自动校验
- 已回传证明的排期禁止修改播放时长

## 二、技术栈

### 后端
- Java 17 (Zulu 17.0.18)
- Spring Boot 3.2.5
- MyBatis-Plus 3.5.6
- MySQL 8.0
- Lombok 1.18.36
- HikariCP 连接池

### 前端
- Vue 3
- Vite 4
- Element Plus 2
- Vue Router 4
- Axios
- Day.js

## 三、环境准备

### 3.1 JDK 配置
系统默认 JDK 可能是 21，需要切换到 JDK 17：

```bash
# 查看可用 JDK
/usr/libexec/java_home -V

# 设置 JDK 17（Zulu）
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# 验证
java -version
```

### 3.2 MySQL 配置
确保 MySQL 服务已启动，并创建数据库：

```sql
CREATE DATABASE ad_schedule DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

数据库连接配置在 `backend/src/main/resources/application.yml` 中：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ad_schedule?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: root
```

### 3.3 数据库初始化
建表脚本位于 `backend/src/main/resources/db/schema.sql`，包含 5 张核心表：
- `ad_screen` - 广告屏表
- `ad_material` - 素材表
- `ad_schedule` - 排期表
- `ad_audit_log` - 审核日志表
- `ad_proof` - 刊播证明表

首次启动时会自动执行建表脚本，并插入 3 条广告屏测试数据。

## 四、启动步骤

### 4.1 启动后端

```bash
# 进入后端目录
cd backend

# 设置 JDK 17
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# 编译并启动
mvn spring-boot:run

# 或指定 JVM 参数（推荐，避免内存溢出）
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xms256m -Xmx512m"
```

后端启动成功后，访问：http://localhost:8080/api

### 4.2 启动前端

```bash
# 进入前端目录
cd frontend

# 安装依赖（首次启动）
npm install

# 启动开发服务器
npm run dev
```

前端启动成功后，访问：http://localhost:5173/ （或提示的其他端口）

前端通过 Vite 代理将 `/api` 请求转发到后端 `http://localhost:8080`。

## 五、核心业务流程验证

### 5.1 验证清单

| 序号 | 业务场景 | 预期结果 | 验证状态 |
|------|----------|----------|----------|
| 1 | 销售提交素材 | 素材创建成功，auditStatus=0（待审核） | ✅ 通过 |
| 2 | 审核员审核通过 | 素材 auditStatus=1（已通过） | ✅ 通过 |
| 3 | 审核员审核驳回 | 素材 auditStatus=2（已驳回） | ✅ 通过 |
| 4 | 未审核素材创建排期 | 返回错误："素材未通过审核，无法排期" | ✅ 通过 |
| 5 | 已审核素材创建排期 | 排期创建成功，scheduleStatus=1（已排期） | ✅ 通过 |
| 6 | 同一屏同一时段重复排期 | 返回错误："该广告屏在此时段已有排期，请调整时间" | ✅ 通过 |
| 7 | 运维回传刊播证明 | 证明创建成功，排期 proofStatus=1（已回传） | ✅ 通过 |
| 8 | 已回传证明修改播放时长 | 返回错误："已回传刊播证明的排期不能修改播放时长" | ✅ 通过 |
| 9 | 已回传证明修改备注 | 修改成功 | ✅ 通过 |
| 10 | 已回传证明取消排期 | 返回错误："已回传刊播证明的排期不能取消" | ✅ 通过 |

### 5.2 API 接口验证脚本

以下是使用 curl 进行业务回归验证的完整脚本：

```bash
# 基础 URL
BASE_URL="http://localhost:8080/api"

echo "=== 1. 提交素材 ==="
curl -s -X POST $BASE_URL/material/submit \
  -H "Content-Type: application/json" \
  -d '{
    "materialCode": "MAT-TEST-001",
    "materialName": "测试广告素材",
    "materialType": 1,
    "customerName": "测试客户",
    "fileUrl": "https://example.com/test.mp4",
    "fileName": "test.mp4",
    "fileSize": 1024000,
    "duration": 30,
    "remark": "测试用素材",
    "createBy": "销售小王"
  }'

echo ""
echo ""

echo "=== 2. 审核通过 ==="
curl -s -X POST $BASE_URL/material/audit \
  -H "Content-Type: application/json" \
  -d '{
    "materialId": 1,
    "auditStatus": 1,
    "auditRemark": "内容合规，审核通过",
    "auditBy": "审核员小李"
  }'

echo ""
echo ""

echo "=== 3. 创建排期 ==="
curl -s -X POST $BASE_URL/schedule \
  -H "Content-Type: application/json" \
  -d '{
    "screenId": 1,
    "materialId": 1,
    "playDate": "2026-06-30",
    "startTime": "10:00:00",
    "endTime": "10:30:00",
    "duration": 30,
    "remark": "测试排期",
    "createBy": "销售小王"
  }'

echo ""
echo ""

echo "=== 4. 排期冲突校验（重复排期） ==="
curl -s -X POST $BASE_URL/schedule \
  -H "Content-Type: application/json" \
  -d '{
    "screenId": 1,
    "materialId": 1,
    "playDate": "2026-06-30",
    "startTime": "10:15:00",
    "endTime": "10:45:00",
    "duration": 30,
    "remark": "冲突测试排期",
    "createBy": "销售小王"
  }'

echo ""
echo ""

echo "=== 5. 回传刊播证明 ==="
curl -s -X POST $BASE_URL/proof/submit \
  -H "Content-Type: application/json" \
  -d '{
    "scheduleId": 1,
    "proofType": 1,
    "fileUrl": "https://example.com/proof-001.jpg",
    "fileName": "刊播证明.jpg",
    "fileSize": 1024000,
    "actualStartTime": "2026-06-30T10:00:00",
    "actualEndTime": "2026-06-30T10:30:00",
    "actualDuration": 30,
    "remark": "正常播放",
    "submitBy": "运维小张"
  }'

echo ""
echo ""

echo "=== 6. 已回传证明修改播放时长（应该失败） ==="
curl -s -X PUT $BASE_URL/schedule \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "duration": 60,
    "remark": "修改时长测试",
    "updateBy": "测试员"
  }'

echo ""
echo ""

echo "=== 7. 已回传证明修改备注（应该成功） ==="
curl -s -X PUT $BASE_URL/schedule \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "remark": "仅修改备注",
    "updateBy": "测试员"
  }'

echo ""
```

## 六、核心 API 接口列表

### 6.1 广告屏管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/screen/page | 分页查询广告屏 |
| GET | /api/screen/{id} | 获取广告屏详情 |
| POST | /api/screen | 新增广告屏 |
| PUT | /api/screen | 修改广告屏 |
| DELETE | /api/screen/{id} | 删除广告屏 |
| POST | /api/screen/{id}/enable | 启用广告屏 |
| POST | /api/screen/{id}/disable | 停用广告屏 |

### 6.2 素材管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/material/page | 分页查询素材 |
| GET | /api/material/{id} | 获取素材详情 |
| POST | /api/material/submit | 提交素材 |
| POST | /api/material/audit | 审核素材 |
| DELETE | /api/material/{id} | 删除素材 |

### 6.3 排期管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/schedule/page | 分页查询排期 |
| GET | /api/schedule/{id} | 获取排期详情 |
| POST | /api/schedule | 创建排期 |
| PUT | /api/schedule | 修改排期 |
| POST | /api/schedule/cancel/{id} | 取消排期 |
| GET | /api/schedule/check-conflict | 检查排期冲突 |
| GET | /api/schedule/list-by-screen-date | 按屏和日期查询排期 |

### 6.4 刊播证明
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/proof/page | 分页查询证明 |
| GET | /api/proof/{id} | 获取证明详情 |
| POST | /api/proof/submit | 回传证明 |
| GET | /api/proof/list-by-schedule/{scheduleId} | 按排期查询证明 |

## 七、业务规则说明

### 7.1 审核状态机
- `0` - 待审核
- `1` - 审核通过
- `2` - 审核驳回
- 只有审核通过的素材才能创建排期

### 7.2 排期状态机
- `0` - 待排期
- `1` - 已排期
- `2` - 已取消
- `3` - 已完成
- 已取消的排期不能修改
- 已回传证明的排期自动变为"已完成"

### 7.3 证明状态
- `0` - 未回传
- `1` - 已回传
- 已回传证明的排期不能修改播放时长、播放日期、播放时段、广告屏、素材
- 已回传证明的排期不能取消

### 7.4 排期冲突检测算法
同一广告屏、同一播放日期，时间段有重叠即为冲突：
- 条件：`existing.startTime < new.endTime AND existing.endTime > new.startTime`
- 排除自身：`id != excludeId`

## 八、常见问题

### 8.1 Lombok 编译失败
**问题**：`Fatal error compiling: java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN`

**原因**：JDK 版本与 Lombok 版本不兼容（JDK 21 + Lombok 1.18.34）

**解决方案**：
1. 升级 Lombok 到 1.18.36 或更高版本
2. 使用 JDK 17 进行编译
3. 确保 `pom.xml` 中配置了 `annotationProcessorPaths`

### 8.2 数据库连接失败
**问题**：`Access denied for user 'root'@'localhost'`

**原因**：数据库用户名或密码错误

**解决方案**：修改 `application.yml` 中的数据库连接配置

### 8.3 后端服务 OOM
**问题**：进程退出码 137（被系统杀死）

**原因**：内存不足

**解决方案**：启动时限制 JVM 内存
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xms256m -Xmx512m"
```

### 8.4 前端跨域问题
**问题**：前端请求后端报 CORS 错误

**解决方案**：
- 开发环境：使用 Vite 代理（已配置）
- 生产环境：后端配置 CORS（CorsConfig 已配置）

## 九、项目结构

```
.
├── backend/                    # 后端项目
│   ├── src/main/java/com/ad/schedule/
│   │   ├── common/            # 公共类（Result、异常处理）
│   │   ├── config/            # 配置类（MyBatis-Plus、CORS）
│   │   ├── controller/        # 控制器层
│   │   ├── dto/               # 数据传输对象
│   │   ├── entity/            # 实体类
│   │   ├── enums/             # 枚举类
│   │   ├── mapper/            # Mapper 接口
│   │   ├── service/           # 服务层
│   │   │   └── impl/          # 服务实现
│   │   ├── vo/                # 视图对象
│   │   └── AdScheduleApplication.java
│   ├── src/main/resources/
│   │   ├── application.yml    # 应用配置
│   │   └── db/schema.sql      # 建表脚本
│   └── pom.xml
│
└── frontend/                   # 前端项目
    ├── src/
    │   ├── api/               # API 接口
    │   ├── router/            # 路由配置
    │   ├── styles/            # 全局样式
    │   ├── utils/             # 工具类
    │   ├── views/             # 页面组件
    │   ├── App.vue
    │   └── main.js
    ├── index.html
    ├── package.json
    └── vite.config.js
```
