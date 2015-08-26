package com.example.jamin.charmander;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jamin on 8/25/15.
 */
public class Route {
    private List<RoutePoint> pointsList;

    public Route() {
        pointsList = new ArrayList<RoutePoint>();
    }

    public void addPoint(RoutePoint point) {
        pointsList.add(point);
    }

    public List<RoutePoint> getRoute() {
        return pointsList;
    }

}
