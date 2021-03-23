Simple Notification service
===
____

# Concept

Simple Notification service where users can be notified by group/root

# Details

1. We have **users** , **groups** the mapping between user and grouping can be changed anytime
2. Notification can be created by calling rest api and sending message to a group. All group members will get the
   notification
3. Users can be added to and removed from group anytime, and it will cause immediately change on their subscriptions
4. Support for both **Server Sent Events** and **Long Polling** techniques
5. API for detecting which message is read and which message is delivered



