package com.xaexal.app.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.xaexal.app.DTO.MessageInboxItem;
import com.xaexal.app.DTO.MessageOutboxItem;
import com.xaexal.app.Entity.Message;

public interface MessageRep extends JpaRepository<Message, Integer> {

    @Query("""
        SELECT new com.xaexal.app.DTO.MessageInboxItem(m.id, m.title, mb.name, mr.isRead, m.created)
        FROM Message m
        JOIN MessageRecipient mr ON mr.messageId = m.id
        JOIN Member mb ON mb.id = m.senderId
        WHERE mr.receiverId = :receiverId AND mr.deletedAt IS NULL
        ORDER BY m.created DESC
        """)
    List<MessageInboxItem> findInbox(@Param("receiverId") Integer receiverId);

    @Query("""
        SELECT new com.xaexal.app.DTO.MessageOutboxItem(
            m.id, m.title, COUNT(mr.id),
            SUM(CASE WHEN mr.isRead = false THEN 1L ELSE 0L END),
            m.created)
        FROM Message m
        JOIN MessageRecipient mr ON mr.messageId = m.id
        WHERE m.senderId = :senderId AND m.senderDeletedAt IS NULL
        GROUP BY m.id, m.title, m.created
        ORDER BY m.created DESC
        """)
    List<MessageOutboxItem> findOutbox(@Param("senderId") Integer senderId);

    interface ReceiverView {
        Integer getId();
        String getName();
        String getGender();
        String getMobile();
        String getAddress();
    }

    @Query(value = """
        SELECT m.id AS id, m.name AS name, m.gender AS gender, m.mobile AS mobile,
               LEFT(m.address, 20) AS address
        FROM member m
        JOIN saint s ON s.member_id = m.id
        WHERE s.church_id = :churchId
          AND m.name LIKE CONCAT('%', :name, '%')
          AND s.active = '1'
        ORDER BY m.name
        """, nativeQuery = true)
    List<ReceiverView> searchReceivers(@Param("churchId") Integer churchId, @Param("name") String name);
}
