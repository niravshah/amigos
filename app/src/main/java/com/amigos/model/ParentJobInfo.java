package com.amigos.model;

import com.bignerdranch.expandablerecyclerview.Model.ParentObject;

import java.util.List;

/**
 * Created by Nirav on 16/12/2015.
 */
public class ParentJobInfo implements ParentObject {

    private List<Object> mChildrenList;
    private String title;
    private String rphoto;
    private String rname;
    private String rid;

    @Override
    public List<Object> getChildObjectList() {
        return mChildrenList;
    }

    @Override
    public void setChildObjectList(List<Object> list) {
        this.mChildrenList = list;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setRphoto(String rphoto) {
        this.rphoto = rphoto;
    }

    public String getRphoto() {
        return rphoto;
    }

    public void setRname(String rname) {
        this.rname = rname;
    }

    public String getRname() {
        return rname;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public String getRid() {
        return rid;
    }
}
