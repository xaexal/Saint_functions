package com.xaexal.app.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xaexal.app.DTO.MessageDetail;
import com.xaexal.app.DTO.MessageInboxItem;
import com.xaexal.app.DTO.MessageOutboxItem;
import com.xaexal.app.DTO.MessageReceiverInfo;
import com.xaexal.app.Entity.Message;
import com.xaexal.app.Repository.MessageRep.ReceiverView;
import com.xaexal.app.Entity.MessageRecipient;
import com.xaexal.app.Repository.MemberRep;
import com.xaexal.app.Repository.MessageRecipientRep;
import com.xaexal.app.Repository.MessageRep;

@Service
public class _Message_ {

    @Autowired private MessageRep _msg;
    @Autowired private MessageRecipientRep _msgRcp;
    @Autowired private MemberRep _member;

    @Transactional
    public void send(Integer senderId, String title, String content, List<Integer> receiverIds) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setTitle(title);
        message.setContent(content);
        message.setWriter(senderId);
        Message saved = _msg.save(message);

        List<MessageRecipient> recipients = receiverIds.stream().map(rid -> {
            MessageRecipient mr = new MessageRecipient();
            mr.setMessageId(saved.getId());
            mr.setReceiverId(rid);
            mr.setIsRead(false);
            mr.setWriter(senderId);
            return mr;
        }).collect(Collectors.toList());
        _msgRcp.saveAll(recipients);
    }

    public List<MessageInboxItem> inbox(Integer receiverId) {
        return _msg.findInbox(receiverId);
    }

    public List<MessageOutboxItem> outbox(Integer senderId) {
        return _msg.findOutbox(senderId);
    }

    @Transactional
    public MessageDetail detail(Integer messageId, Integer currentUserId) {
        Message message = _msg.findById(messageId)
            .orElseThrow(() -> new RuntimeException("쪽지를 찾을 수 없습니다."));

        boolean isSender = message.getSenderId().equals(currentUserId);
        MessageRecipient myRecipient = null;

        if (!isSender) {
            myRecipient = _msgRcp.findByMessageIdAndReceiverId(messageId, currentUserId)
                .orElseThrow(() -> new RuntimeException("접근 권한이 없습니다."));
            if (myRecipient.getDeletedAt() != null)
                throw new RuntimeException("삭제된 쪽지입니다.");
        } else {
            if (message.getSenderDeletedAt() != null)
                throw new RuntimeException("삭제된 쪽지입니다.");
        }

        String senderName = _member.findById(message.getSenderId())
            .map(m -> m.getName()).orElse("(알 수 없음)");

        MessageDetail detail = new MessageDetail();
        detail.setId(message.getId());
        detail.setTitle(message.getTitle());
        detail.setContent(message.getContent());
        detail.setSenderId(message.getSenderId());
        detail.setSenderName(senderName);
        detail.setCreated(message.getCreated());

        if (isSender) {
            detail.setRecipients(_msgRcp.findRecipientsWithNames(messageId));
        } else {
            if (!myRecipient.getIsRead()) {
                myRecipient.setIsRead(true);
                myRecipient.setReadAt(LocalDateTime.now());
                _msgRcp.save(myRecipient);
            }
            detail.setIsRead(true);
            detail.setReadAt(myRecipient.getReadAt() != null ? myRecipient.getReadAt() : LocalDateTime.now());
        }

        return detail;
    }

    @Transactional
    public void deleteInbox(Integer messageId, Integer receiverId) {
        _msgRcp.findByMessageIdAndReceiverId(messageId, receiverId).ifPresent(mr -> {
            mr.setDeletedAt(LocalDateTime.now());
            _msgRcp.save(mr);
        });
    }

    @Transactional
    public void deleteOutbox(Integer messageId, Integer senderId) {
        _msg.findById(messageId).ifPresent(m -> {
            if (m.getSenderId().equals(senderId)) {
                m.setSenderDeletedAt(LocalDateTime.now());
                _msg.save(m);
            }
        });
    }

    public long unreadCount(Integer receiverId) {
        return _msgRcp.countByReceiverIdAndIsReadFalseAndDeletedAtIsNull(receiverId);
    }

    public List<MessageReceiverInfo> searchReceivers(Integer churchId, String name) {
        List<ReceiverView> views = _msg.searchReceivers(churchId, name);
        return views.stream()
            .map(v -> new MessageReceiverInfo(v.getId(), v.getName(), v.getGender(), v.getMobile(), v.getAddress()))
            .collect(Collectors.toList());
    }
}
