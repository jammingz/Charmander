package com.example.jamin.charmander;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jamin on 8/25/15.
 */
public class RoutePointsSet {

    private List<RoutePoint> set;
    private int groupNum;

    public RoutePointsSet() {
        set = new ArrayList<RoutePoint>();
        groupNum = -1;
    }

    public RoutePointsSet(int num) {
        set = new ArrayList<RoutePoint>();
        groupNum = num;
    }

    public void addPoint(RoutePoint point) {
        set.add(point);
    }

    public List<RoutePoint> getSet() {
        return set;
    }

    public int getGroupNum() {
        return groupNum;
    }

    public int getSize() {
        return set.size();
    }

    public RoutePoint get(int n) {
        return set.get(n);
    }
}
