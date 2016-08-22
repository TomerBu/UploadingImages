package tomerbu.edu.uploadingimages.models;

/**
 * Created by tomerbuzaglo on 22/08/2016.
 * Copyright 2016 tomerbuzaglo. All Rights Reserved
 * <p/>
 * Licensed under the Apache License, Version 2.0
 * you may not use this file except
 * in compliance with the License
 */
public class Image {
    private String  uri;

    public Image(String uri) {
        this.uri = uri;
    }

    public Image() {
    }


    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }


}
