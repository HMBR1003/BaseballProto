package org.androidtown.baseballproto.BusinessMan;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Administrator on 2017-05-22-022.
 */

@IgnoreExtraProperties
public class MarketInfo {


    public String manName;
    public String manTel;
    public String businessRegisterNum;
    public String marketAddress1;
    public String marketAddress2;
    public String marketName;
    public String marketTel;
    public String marketImageUrl;
    public long handleFood;

    public MarketInfo () {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
}