package com.xaexal.app.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.xaexal.app.DTO.RecipientStatus;
import com.xaexal.app.Entity.MessageRecipient;

public interface MessageRecipientRep extends JpaRepository<MessageRecipient, Integer> {

    Optional<MessageRecipient> findByMessageIdAndReceiverId(Integer messageId, Integer receiverId);

    long countByReceiverIdAndIsReadFalseAndDeletedAtIsNull(Integer receiverId);

    @Query("""
        SELECT new com.xaexal.app.DTO.RecipientStatus(mr.receiverId, mb.name, mr.isRead, mr.readAt)
        FROM MessageRecipient mr
        JOIN Member mb ON mb.id = mr.receiverId
        WHERE mr.messageId = :messageId
        """)
    List<RecipientStatus> findRecipientsWithNames(@Param("messageId") Integer messageId);
}
