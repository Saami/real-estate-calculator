package com.saami.realestate.service.api;

import com.saami.realestate.model.ZillowData;

/**
 * Created by sasiddi on 5/1/17.
 */
public interface ZillowService {
    ZillowData getZillowData(String address, String city, String state);
}
