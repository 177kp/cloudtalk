package com.zhangwuji.im.imcore.event;


import com.zhangwuji.im.DB.entity.Group;

import java.util.List;

/**
 * @author : yingmu on 14-12-30.
 * @email : yingmu@mogujie.com.
 */
public class GroupEvent {

    private Group groupEntity;
    private Event event;

    /**很多的场景只是关心改变的类型以及change的Ids*/
    private int changeType;
    private List<Integer> changeList;

    public GroupEvent(Event event){
        this.event = event;
    }

    public GroupEvent(Event event,Group groupEntity){
        this.groupEntity = groupEntity;
        this.event = event;
    }

    public enum Event{
        NONE,

        GROUP_INFO_OK,
        GROUP_INFO_UPDATED,

        CHANGE_GROUP_MEMBER_SUCCESS,
        CHANGE_GROUP_MEMBER_FAIL,
        CHANGE_GROUP_MEMBER_TIMEOUT,

        CREATE_GROUP_OK,
        CREATE_GROUP_FAIL,
        CREATE_GROUP_TIMEOUT,

        SHIELD_GROUP_OK,
        SHIELD_GROUP_TIMEOUT,
        SHIELD_GROUP_FAIL


    }

    public int getChangeType() {
        return changeType;
    }

    public void setChangeType(int changeType) {
        this.changeType = changeType;
    }

    public List<Integer> getChangeList() {
        return changeList;
    }

    public void setChangeList(List<Integer> changeList) {
        this.changeList = changeList;
    }

    public Group getGroupEntity() {
        return groupEntity;
    }
    public void setGroupEntity(Group groupEntity) {
        this.groupEntity = groupEntity;
    }

    public Event getEvent() {
        return event;
    }
    public void setEvent(Event event) {
        this.event = event;
    }
}
