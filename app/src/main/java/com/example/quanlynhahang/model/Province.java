package com.example.quanlynhahang.model;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Province {
    @SerializedName("name")
    private String name;
    @SerializedName("code")
    private String code;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Province province = (Province) o;
        return Objects.equals(name, province.name);
    }
    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class District {
        @SerializedName("name")
        private String name;
        @SerializedName("code")
        private String code;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            District district = (District) o;
            return Objects.equals(name, district.name);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name);
        }
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Ward{
        @SerializedName("name")
        private String name;
        @SerializedName("code")
        private String code;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Ward ward = (Ward) o;
            return Objects.equals(name, ward.name);
        }
        @Override
        public int hashCode() {
            return Objects.hashCode(name);
        }

    }
}
