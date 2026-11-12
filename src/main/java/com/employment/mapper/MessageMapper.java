package com.employment.mapper;

import com.employment.entity.Message;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 站内消息表数据访问接口。
 */
public interface MessageMapper {

    @Select("SELECT * FROM message WHERE receiver_type = #{type} AND receiver_id = #{id} ORDER BY create_time DESC")
    List<Message> findByReceiver(@Param("type") String type, @Param("id") Long id);

    @Select("SELECT COUNT(*) FROM message WHERE receiver_type = #{type} AND receiver_id = #{id} AND read_flag = 0")
    long countUnread(@Param("type") String type, @Param("id") Long id);

    @Insert("INSERT INTO message(receiver_type, receiver_id, title, content) " +
            "VALUES(#{receiverType}, #{receiverId}, #{title}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Message message);

    /** 单条标记已读（带归属校验，防止越权）。 */
    @Update("UPDATE message SET read_flag = 1 WHERE id = #{id} AND receiver_type = #{type} AND receiver_id = #{receiverId}")
    int markRead(@Param("id") Long id, @Param("type") String type, @Param("receiverId") Long receiverId);

    /** 全部标记已读。 */
    @Update("UPDATE message SET read_flag = 1 WHERE receiver_type = #{type} AND receiver_id = #{id}")
    int markAllRead(@Param("type") String type, @Param("id") Long id);
}