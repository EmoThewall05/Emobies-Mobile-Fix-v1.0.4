class ChatRoom {
  final String id;
  final String name;
  final String type;
  final List<String> participants;
  final String? complaintId;
  final bool isMonitored;
  final DateTime createdAt;
  final DateTime updatedAt;
  final ChatMessage? lastMessage;
  final int unreadCount;

  ChatRoom({
    required this.id,
    required this.name,
    required this.type,
    required this.participants,
    this.complaintId,
    this.isMonitored = true,
    required this.createdAt,
    required this.updatedAt,
    this.lastMessage,
    this.unreadCount = 0,
  });

  factory ChatRoom.fromJson(Map<String, dynamic> json) {
    return ChatRoom(
      id: json['id'] ?? '',
      name: json['name'] ?? '',
      type: json['type'] ?? 'direct',
      participants: json['participants'] != null
          ? List<String>.from(json['participants'])
          : [],
      complaintId: json['complaint_id'],
      isMonitored: json['is_monitored'] ?? true,
      createdAt: json['created_at'] != null
          ? DateTime.parse(json['created_at'])
          : DateTime.now(),
      updatedAt: json['updated_at'] != null
          ? DateTime.parse(json['updated_at'])
          : DateTime.now(),
      lastMessage: json['last_message'] != null
          ? ChatMessage.fromJson(json['last_message'])
          : null,
      unreadCount: json['unread_count'] ?? 0,
    );
  }

  Map<String, dynamic> toJson() => {
    'id': id,
    'name': name,
    'type': type,
    'participants': participants,
    'complaint_id': complaintId,
    'is_monitored': isMonitored,
    'created_at': createdAt.toIso8601String(),
    'updated_at': updatedAt.toIso8601String(),
    'last_message': lastMessage?.toJson(),
    'unread_count': unreadCount,
  };
}

class ChatMessage {
  final String id;
  final String roomId;
  final String senderId;
  final String senderName;
  final String content;
  final MessageType type;
  final String? mediaUrl;
  final DateTime createdAt;
  final bool isRead;
  final bool isAiGenerated;

  ChatMessage({
    required this.id,
    required this.roomId,
    required this.senderId,
    required this.senderName,
    required this.content,
    this.type = MessageType.text,
    this.mediaUrl,
    required this.createdAt,
    this.isRead = false,
    this.isAiGenerated = false,
  });

  factory ChatMessage.fromJson(Map<String, dynamic> json) {
    return ChatMessage(
      id: json['id'] ?? '',
      roomId: json['room_id'] ?? '',
      senderId: json['sender_id'] ?? '',
      senderName: json['sender_name'] ?? '',
      content: json['content'] ?? '',
      type: MessageType.values.firstWhere(
        (e) => e.name == json['type'],
        orElse: () => MessageType.text,
      ),
      mediaUrl: json['media_url'],
      createdAt: json['created_at'] != null
          ? DateTime.parse(json['created_at'])
          : DateTime.now(),
      isRead: json['is_read'] ?? false,
      isAiGenerated: json['is_ai_generated'] ?? false,
    );
  }

  Map<String, dynamic> toJson() => {
    'id': id,
    'room_id': roomId,
    'sender_id': senderId,
    'sender_name': senderName,
    'content': content,
    'type': type.name,
    'media_url': mediaUrl,
    'created_at': createdAt.toIso8601String(),
    'is_read': isRead,
    'is_ai_generated': isAiGenerated,
  };
}

enum MessageType { text, image, file, system, ai }