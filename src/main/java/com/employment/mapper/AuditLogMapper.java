package com.employment.mapper;

import com.employment.entity.AuditLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 审核操作日志数据访问接口。
 */
public interface AuditLogMapper {

    @Insert("INSERT INTO audit_log(biz_type, biz_id, action, action_label, from_status, to_status, " +
            "operator_id, operator_name, comment) VALUES(#{log.bizType}, #{log.bizId}, #{log.action}, " +
            "#{log.actionLabel}, #{log.fromStatus}, #{log.toStatus}, #{log.operatorId}, " +
            "#{log.operatorName}, #{log.comment})")
    @Options(useGeneratedKeys = true, keyProperty = "log.id")
    int insert(@Param("log") AuditLog log);

    @Select("SELECT * FROM audit_log WHERE biz_type = #{bizType} AND biz_id = #{bizId} ORDER BY created_at, id")
    List<AuditLog> findByBiz(@Param("bizType") String bizType, @Param("bizId") Long bizId);

    @Select("SELECT COUNT(*) FROM audit_log WHERE biz_type = #{bizType} AND biz_id = #{bizId}")
    long countByBiz(@Param("bizType") String bizType, @Param("bizId") Long bizId);
}
