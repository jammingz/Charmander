package com.example.jamin.charmander;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jamin on 8/25/15.
 */
public class Route {
    private List<RoutePointsSet> pointsList; // List of a Set(List) of points

    public Route() {
        pointsList = new ArrayList<RoutePointsSet>();
    }

    public void addSet(RoutePointsSet set) {
        pointsList.add(set);
    }

    // Appends route to current route
    public void appendRoute(Route route) {
        pointsList.addAll(route.getRoute());
    }

    public List<RoutePointsSet> getRoute() {
        return pointsList;
    }

    public int getSize() {
        return pointsList.size();
    }

    public RoutePointsSet get(int n) {
        return pointsList.get(n);
    }

}
