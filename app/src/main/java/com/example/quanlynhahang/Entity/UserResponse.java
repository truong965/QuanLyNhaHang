package com.example.quanlynhahang.Entity;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
public class UserResponse {
    @SerializedName("results")
    private List<User> results;
    public List<User> getResults() {
        return results;
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class User {
        @SerializedName("picture")
        private Picture picture;
        @SerializedName("name")
        private Name name;
        @SerializedName("gender")
        private String gender;
        @SerializedName("location")
        private LocationUser location;
        @SerializedName("email")
        private String email;
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor

    public static class Name {
        @SerializedName("title")
        private String title;

        @SerializedName("first")
        private String first;

        @SerializedName("last")
        private String last;

        @NonNull
        @Override
        public String toString() {
            return title + " " + first + " " + last;
        }
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class LocationUser {
        @SerializedName("city")
        private String city;
        @SerializedName("ward")
        private String ward;
        @SerializedName("state")
        private String state;
        @SerializedName("street")
        private Street street;
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Street {
        @SerializedName("number")
        private Integer number;
        @SerializedName("name")
        private String name;
        @NonNull
        @Override
        public String toString() {
            return number + " " + name;
        }
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Picture {
        @SerializedName("large")
        private String large;
        public String getLarge() {
            return large;
        }
    }
}
