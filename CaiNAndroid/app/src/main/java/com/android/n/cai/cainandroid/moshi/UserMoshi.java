package com.android.n.cai.cainandroid.moshi;

import java.util.Date;

/**
 * Created by Kuriboh on 2017/10/16.
 * E-Mail Address: cai_android@163.com
 */

public class UserMoshi extends BaseMoshi{

    private String name;
    private IdMoshi id;
    private int phone;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IdMoshi getId() {
        return id;
    }

    public void setId(IdMoshi id) {
        this.id = id;
    }

    public int getPhone() {
        return phone;
    }

    public void setPhone(int phone) {
        this.phone = phone;
    }

    private class IdMoshi {
        private String idNumber;
        private Date birthday;

        public String getIdNumber() {
            return idNumber;
        }

        public void setIdNumber(String idNumber) {
            this.idNumber = idNumber;
        }

        public Date getBirthday() {
            return birthday;
        }

        public void setBirthday(Date birthday) {
            this.birthday = birthday;
        }

        @Override
        public String toString() {
            return "IdMoshi{" +
                    "idNumber='" + idNumber + '\'' +
                    ", birthday=" + birthday +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "UserMoshi{" +
                "name='" + name + '\'' +
                ", id=" + id.toString() +
                ", phone=" + phone +
                '}';
    }
}
