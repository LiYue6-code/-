package com.employment.service;

import com.employment.entity.AuditLog;
import com.employment.mapper.AuditLogMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 审核日志服务：统一记录每次状态变更，支撑“处理进度/时间线”与责任追溯。
 */
@Service
public class AuditService {

    /** 业务类型：岗位申请 */
    public static final String TYPE_APPLICATION = "APPLICATION";
    /** 业务类型：就业上报 */
    public static final String TYPE_EMPLOYMENT = "EMPLOYMENT";

    private final AuditLogMapper auditLogMapper;

    public AuditService(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    /** 记录一条审核日志。 */
    public void record(String bizType, Long bizId, String action, String actionLabel,
                       String fromStatus, String toStatus,
                       Long operatorId, String operatorName, String comment) {
        AuditLog log = new AuditLog();
        log.setBizType(bizType);
        log.setBizId(bizId);
        log.setAction(action);
        log.setActionLabel(actionLabel);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setComment(comment);
        auditLogMapper.insert(log);
    }

    /** 查询某条业务的完整处理时间线（按时间正序）。 */
    public List<AuditLog> timeline(String bizType, Long bizId) {
        return auditLogMapper.findByBiz(bizType, bizId);
    }
}
