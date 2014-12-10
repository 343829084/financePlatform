package com.sunlights.customer.dal;

import models.HoldReward;

import java.util.List;

/**
 * Created by tangweiqun on 2014/11/19.
 */
public interface HoldRewardDao {

    public List<HoldReward> findByCustIdAndRewardType(String custId, String rewardType, String activityType);

    public HoldReward findByCustIdAndRewardType(String custId, String rewardType);

    public List<HoldReward> findListByCustIdAndRewardType(String custId, String rewardType);

    public HoldReward findByCondition(String custId, String rewardType, String activityType);

    public void doInsert(HoldReward holdReward);

    public void doUpdate(HoldReward holdReward);

}
