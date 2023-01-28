package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }


    public String createUser(String name, String mobile) throws Exception {
        //If the mobile number exists in database, throw "User already exists" exception
        //Otherwise, create the user and return "SUCCESS"
        if(userMobile.contains(mobile)){
            throw new Exception("User already exists");
        }
        User user = new User(name,mobile);
        userMobile.add(mobile);
        return "SUCCESS";
    }
    public Group createGroup(List<User> users){
        // The list contains at least 2 users where the first user is the admin. A group has exactly one admin.
        // If there are only 2 users, the group is a personal chat and the group name should be kept as the name of the second user(other than admin)
        // If there are 2+ users, the name of group should be "Group count". For example, the name of first group would be "Group 1", second would be "Group 2" and so on.
        // Note that a personal chat is not considered a group and the count is not updated for personal chats.
        // If group is successfully created, return group.

        //For example: Consider userList1 = {Alex, Bob, Charlie}, userList2 = {Dan, Evan}, userList3 = {Felix, Graham, Hugh}.
        //If createGroup is called for these userLists in the same order, their group names would be "Group 1", "Evan", and "Group 2" respectively.
        if(users.size() == 2){
            User admin = users.get(0);
            User man = users.get(1);
            Group g = new Group(man.getName(),2);
            groupUserMap.put(g,users);
            groupMessageMap.put(g,new ArrayList<Message>());
            adminMap.put(g,admin);
            return g;
        }
        customGroupCount++;
        User admin = users.get(0);
        int n = users.size();
        String groupName = "Group " + customGroupCount;
        Group g = new Group(groupName,n);
        groupUserMap.put(g,users);
        groupMessageMap.put(g,new ArrayList<Message>());
        adminMap.put(g,admin);
        return g;
    }

    public int createMessage(String content){
        // The 'i^th' created message has message id 'i'.
        // Return the message id.
        messageId++;
        Message m = new Message(messageId,content);
        return m.getId();
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.
        if(!adminMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        List<User> ls = groupUserMap.get(group);
        Boolean f = false;
        for(int i=0;i<ls.size();i++){
            if(ls.get(i).getName().equals(sender.getName())){
                f = true;
                break;
            }
        }
        if(!f){
            throw new Exception("You are not allowed to send message");
        }
        senderMap.put(message,sender);
        List<Message> l = groupMessageMap.get(group);
        l.add(message);
        groupMessageMap.put(group,l);
        return l.size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS". Note that at one time there is only one admin and the admin rights are transferred from approver to user.
        if(!adminMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        User admin = adminMap.get(group);
        if(!approver.getName().equals(admin.getName())){
            throw new Exception("Approver does not have rights");
        }
        List<User> ls = groupUserMap.get(group);
        Boolean f = false;
        for(int i=0;i<ls.size();i++){
            if(user.getName().equals(ls.get(i).getName())){
                f = true;
                break;
            }
        }
        if(!f){
            throw new Exception("User is not a participant");
        }
        adminMap.put(group,user);
        return "SUCCESS";
    }

//    public int removeUser(User user) throws Exception{
//        Boolean userFound = false;
//        Group userGroup = null;
//        for(Group group: groupUserMap.keySet()){
//            List<User> ls = groupUserMap.get(group);
//            for(User u: ls){
//                if(u.equals(user)){
//                    if(adminMap.get(group).equals(user)){
//                        throw new Exception("Cannot remove admin");
//                    }
//                    userGroup = group;
//                    userFound = true;
//                    break;
//                }
//            }
//            if(userFound){
//                break;
//            }
//        }
//        if(userFound){
//            List<User> users = groupUserMap.get(userGroup);
//            List<User> updatedUsers = new ArrayList<>();
//            for(User participant: users){
//                if(participant.equals(user)){
//                    continue;
//                }
//                updatedUsers.add(participant);
//            }
//            groupUserMap.put(userGroup,updatedUsers);
//            List<Message> m = groupMessageMap.get(userGroup);
//            List<Message> updatedm = new ArrayList<>();
//            for(Message message : m){
//                if(senderMap.get(message).equals(user)){
//                    continue;
//                }
//                updatedm.add(message);
//            }
//            groupMessageMap.put(userGroup,updatedm);
//
//            HashMap<Message,User> updatedSenderMap = new HashMap<>();
//            for(Message message: senderMap.keySet()){
//                if(senderMap.get(message).equals(user)){
//                    continue;
//                }
//                updatedSenderMap.put(message,senderMap.get(message));
//            }
//            senderMap = updatedSenderMap;
//            return updatedUsers.size() + updatedm.size() + updatedSenderMap.size();
//        }
//        throw new Exception("User not found");
//    }
//
//    public String findMessage(Date start, Date end,int K) throws Exception{
//        List<Message> messages = new ArrayList<>();
//        for(Group group: groupMessageMap.keySet()){
//            messages.addAll(groupMessageMap.get(group));
//        }
//        List<Message> filteredMessages = new ArrayList<>();
//        for(Message message: messages){
//            if(message.getTimestamp().after(start) && message.getTimestamp().before(end)){
//                filteredMessages.add(message);
//            }
//        }
//        if(filteredMessages.size()< K){
//            throw new Exception("K is greater than the number of messages");
//        }
//        Collections.sort(filteredMessages, new Comparator<Message>() {
//            @Override
//            public int compare(Message o1, Message o2) {
//                return o2.getTimestamp().compareTo(o1.getTimestamp());
//            }
//        });
//        return filteredMessages.get(K-1).getContent();
//    }
}
