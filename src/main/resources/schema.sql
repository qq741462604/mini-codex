CREATE TABLE IF NOT EXISTS agent_memory (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    memory_key VARCHAR(128) NOT NULL COMMENT '记忆唯一标识',
    content LONGTEXT NOT NULL COMMENT '记忆内容',
    memory_type VARCHAR(32) NOT NULL COMMENT '记忆类型',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_agent_memory_key (memory_key),
    KEY idx_agent_memory_type_created_at (memory_type, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent 记忆记录';
