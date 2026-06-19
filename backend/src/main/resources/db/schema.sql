-- ============================================================
-- 广告屏刊播排期系统 数据库表结构
-- ============================================================

-- 广告屏表
CREATE TABLE IF NOT EXISTS ad_screen (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    screen_code VARCHAR(64) NOT NULL UNIQUE COMMENT '广告屏编码',
    screen_name VARCHAR(128) NOT NULL COMMENT '广告屏名称',
    business_district VARCHAR(128) COMMENT '所属商圈',
    location VARCHAR(256) COMMENT '安装位置',
    resolution VARCHAR(64) COMMENT '分辨率 如1920x1080',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1-启用 0-停用',
    description VARCHAR(512) COMMENT '描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(64) COMMENT '创建人',
    update_by VARCHAR(64) COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    INDEX idx_screen_code (screen_code),
    INDEX idx_status (status),
    INDEX idx_business_district (business_district)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='广告屏表';

-- 广告素材表
CREATE TABLE IF NOT EXISTS ad_material (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    material_code VARCHAR(64) NOT NULL UNIQUE COMMENT '素材编码',
    material_name VARCHAR(128) NOT NULL COMMENT '素材名称',
    customer_name VARCHAR(128) NOT NULL COMMENT '客户名称',
    material_type TINYINT NOT NULL COMMENT '素材类型 1-图片 2-视频 3-文字',
    file_url VARCHAR(512) NOT NULL COMMENT '素材文件地址',
    file_size BIGINT COMMENT '文件大小(字节)',
    duration INT COMMENT '播放时长(秒)',
    audit_status TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态 0-待审核 1-审核通过 2-审核未通过',
    submit_time DATETIME COMMENT '提交审核时间',
    audit_time DATETIME COMMENT '审核时间',
    auditor VARCHAR(64) COMMENT '审核人',
    audit_remark VARCHAR(512) COMMENT '审核意见',
    description VARCHAR(512) COMMENT '素材描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(64) COMMENT '创建人(销售人员)',
    update_by VARCHAR(64) COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    INDEX idx_material_code (material_code),
    INDEX idx_audit_status (audit_status),
    INDEX idx_customer_name (customer_name),
    INDEX idx_create_by (create_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='广告素材表';

-- 排期表
CREATE TABLE IF NOT EXISTS ad_schedule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    schedule_code VARCHAR(64) NOT NULL UNIQUE COMMENT '排期编码',
    screen_id BIGINT NOT NULL COMMENT '广告屏ID',
    material_id BIGINT NOT NULL COMMENT '素材ID',
    play_date DATE NOT NULL COMMENT '播放日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    duration INT NOT NULL COMMENT '播放时长(秒)',
    play_order INT DEFAULT 0 COMMENT '当日播放顺序',
    customer_priority TINYINT NOT NULL DEFAULT 5 COMMENT '客户优先级 1-10，数值越大优先级越高',
    contract_amount DECIMAL(18,2) DEFAULT 0 COMMENT '合同金额(元)',
    schedule_status TINYINT NOT NULL DEFAULT 1 COMMENT '排期状态 1-待刊播 2-刊播中 3-已完成 4-已取消 5-待补播',
    proof_status TINYINT NOT NULL DEFAULT 0 COMMENT '证明状态 0-未回传 1-已回传',
    replay_of_id BIGINT COMMENT '补播来源排期ID，指向原始被补播的排期',
    remark VARCHAR(512) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(64) COMMENT '创建人(销售人员)',
    update_by VARCHAR(64) COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    INDEX idx_schedule_code (schedule_code),
    INDEX idx_screen_date (screen_id, play_date),
    INDEX idx_material_id (material_id),
    INDEX idx_schedule_status (schedule_status),
    INDEX idx_proof_status (proof_status),
    INDEX idx_replay_of_id (replay_of_id),
    CONSTRAINT fk_schedule_screen FOREIGN KEY (screen_id) REFERENCES ad_screen(id),
    CONSTRAINT fk_schedule_material FOREIGN KEY (material_id) REFERENCES ad_material(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='广告排期表';

-- 审核记录表
CREATE TABLE IF NOT EXISTS ad_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    material_id BIGINT NOT NULL COMMENT '素材ID',
    screen_id BIGINT COMMENT '屏幕ID，按屏幕拆分审核时使用',
    audit_status TINYINT NOT NULL COMMENT '审核状态 1-通过 2-未通过',
    audit_remark VARCHAR(512) COMMENT '审核意见',
    audit_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    auditor VARCHAR(64) NOT NULL COMMENT '审核人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_material_id (material_id),
    INDEX idx_auditor (auditor),
    INDEX idx_screen_id (screen_id),
    CONSTRAINT fk_audit_material FOREIGN KEY (material_id) REFERENCES ad_material(id),
    CONSTRAINT fk_audit_screen FOREIGN KEY (screen_id) REFERENCES ad_screen(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材审核记录表';

-- 素材屏幕审核表 - 记录素材在每个屏幕/商圈的审核结论
CREATE TABLE IF NOT EXISTS ad_material_screen_audit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    material_id BIGINT NOT NULL COMMENT '素材ID',
    screen_id BIGINT COMMENT '屏幕ID（精确到单屏）',
    business_district VARCHAR(128) COMMENT '商圈（按商圈批量审核时使用）',
    audit_status TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态 0-待审核 1-审核通过 2-审核未通过',
    audit_remark VARCHAR(512) COMMENT '审核意见',
    compliance_rule VARCHAR(512) COMMENT '合规限制说明',
    audit_time DATETIME COMMENT '审核时间',
    auditor VARCHAR(64) COMMENT '审核人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    UNIQUE KEY uk_material_screen (material_id, screen_id),
    INDEX idx_material_id (material_id),
    INDEX idx_business_district (business_district),
    CONSTRAINT fk_msa_material FOREIGN KEY (material_id) REFERENCES ad_material(id),
    CONSTRAINT fk_msa_screen FOREIGN KEY (screen_id) REFERENCES ad_screen(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材屏幕审核表';

-- 刊播证明表
CREATE TABLE IF NOT EXISTS ad_proof (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    proof_code VARCHAR(64) NOT NULL UNIQUE COMMENT '证明编码',
    schedule_id BIGINT NOT NULL COMMENT '排期ID',
    screen_id BIGINT NOT NULL COMMENT '广告屏ID',
    material_id BIGINT NOT NULL COMMENT '素材ID',
    proof_type TINYINT NOT NULL COMMENT '证明类型 1-截图 2-视频 3-日志',
    file_url VARCHAR(512) NOT NULL COMMENT '证明文件地址',
    file_name VARCHAR(256) COMMENT '文件名称',
    file_size BIGINT COMMENT '文件大小(字节)',
    actual_start_time DATETIME COMMENT '实际开始时间',
    actual_end_time DATETIME COMMENT '实际结束时间',
    actual_duration INT COMMENT '实际播放时长(秒)',
    watermark_sn VARCHAR(128) COMMENT '截图水印编号（锁定后不可修改）',
    screen_code_snapshot VARCHAR(64) COMMENT '屏幕编号快照（锁定时写入，不可修改）',
    duration_snapshot INT COMMENT '播放时长快照（锁定时写入，不可修改）',
    is_locked TINYINT NOT NULL DEFAULT 0 COMMENT '是否锁定 0-未锁定 1-已锁定，锁定后核心字段不可修改',
    lock_time DATETIME COMMENT '锁定时间',
    locked_by VARCHAR(64) COMMENT '锁定操作人',
    replay_type TINYINT DEFAULT 0 COMMENT '补播类型 0-正常刊播 1-补播记录',
    replay_remark VARCHAR(512) COMMENT '补播说明',
    remark VARCHAR(512) COMMENT '备注',
    submit_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '回传时间',
    submit_by VARCHAR(64) NOT NULL COMMENT '回传人(运维人员)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    INDEX idx_proof_code (proof_code),
    INDEX idx_schedule_id (schedule_id),
    INDEX idx_submit_by (submit_by),
    INDEX idx_is_locked (is_locked),
    CONSTRAINT fk_proof_schedule FOREIGN KEY (schedule_id) REFERENCES ad_schedule(id),
    CONSTRAINT fk_proof_screen FOREIGN KEY (screen_id) REFERENCES ad_screen(id),
    CONSTRAINT fk_proof_material FOREIGN KEY (material_id) REFERENCES ad_material(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='刊播证明表';

-- 排期冲突方案表 - 存储冲突检测时生成的补播方案
CREATE TABLE IF NOT EXISTS ad_schedule_conflict_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    conflict_schedule_id BIGINT NOT NULL COMMENT '被冲突的排期ID（已有排期）',
    new_schedule_id BIGINT COMMENT '新排期ID（如果创建了新排期）',
    plan_type TINYINT NOT NULL COMMENT '方案类型 1-原排期优先 2-新排期优先 3-拆分时段 4-推荐补播',
    plan_detail TEXT COMMENT '方案详细说明(JSON格式)',
    customer_priority_compare VARCHAR(32) COMMENT '优先级对比: higher/lower/equal',
    contract_amount_compare VARCHAR(32) COMMENT '合同金额对比: higher/lower/equal',
    recommended_replay_date DATE COMMENT '推荐补播日期',
    recommended_replay_time VARCHAR(64) COMMENT '推荐补播时段',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '方案状态 0-待确认 1-已采纳 2-已拒绝',
    create_by VARCHAR(64) COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_conflict_schedule (conflict_schedule_id),
    INDEX idx_new_schedule (new_schedule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排期冲突方案表';

-- 初始化测试数据
INSERT INTO ad_screen (screen_code, screen_name, business_district, location, resolution, status, description) VALUES
('S001', '中关村大屏', '中关村商圈', '北京市海淀区中关村大街', '1920x1080', 1, '中关村核心商圈户外大屏'),
('S002', '国贸LED屏', '国贸CBD商圈', '北京市朝阳区国贸CBD', '3840x2160', 1, '国贸商圈LED显示屏'),
('S003', '望京SOHO屏', '望京商圈', '北京市朝阳区望京SOHO', '1920x1080', 1, '望京写字楼电子屏');
