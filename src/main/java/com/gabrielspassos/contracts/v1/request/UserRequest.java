package com.gabrielspassos.contracts.v1.request;

import java.util.Objects;

public class UserRequest {

    private String ssn;

    public UserRequest() {
    }

    public UserRequest(String ssn) {
        this.ssn = ssn;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserRequest that = (UserRequest) o;
        return Objects.equals(ssn, that.ssn);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(ssn);
    }

    @Override
    public String toString() {
        return "UserRequest{" +
                "ssn='" + ssn + '\'' +
                '}';
    }
}
