package com.example.camera2_v04;

import java.util.Map;

public class CustomURL {
    String url;

    public CustomURL(String base_url, Map<String, String> params) {
        this.url = base_url+"?";

        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                this.url = this.url + "&";
            this.url = this.url + entry.getKey() + "=" + entry.getValue();
        }
    }

    public String get_url() {
        return this.url;
    }
}
