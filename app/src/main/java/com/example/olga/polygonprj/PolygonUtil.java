package com.example.olga.polygonprj;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.kml.KmlContainer;
import com.google.maps.android.kml.KmlPlacemark;
import com.google.maps.android.kml.KmlPolygon;

import java.util.ArrayList;


public class PolygonUtil {

    /// Taken from (and modified) stackoverflow
    /// http://stackoverflow.com/questions/35896698/how-to-get-arraypoints-from-kml-file-in-android
    // Gets Polygon from KML container
    public static KmlPolygon getPolygon(KmlContainer container) {
        KmlPolygon polygon = null;
        if (container == null) {
            return polygon;
        }

        // Check all placemarks to find a polygon
        Iterable<KmlPlacemark> placemarks = container.getPlacemarks();
        if (placemarks != null) {
            for (KmlPlacemark placemark : placemarks) {
                if (placemark.getGeometry() instanceof KmlPolygon) {
                    polygon = (KmlPolygon) placemark.getGeometry();
                    break;
                }
            }
        }
        return polygon;
    }

    /// Taken from (and modified) stackoverflow
    /// http://stackoverflow.com/questions/35896698/how-to-get-arraypoints-from-kml-file-in-android
    // Checks if current location inside or outside the polygon
    public static boolean liesOnPolygon(KmlPolygon polygon, LatLng currentLocation) {
        boolean lies = false;

        if (polygon == null || currentLocation == null) {
            return lies;
        }

        // Get the outer boundary and check if the test location lies inside
        ArrayList<LatLng> outerBoundary = polygon.getOuterBoundaryCoordinates();
        lies = PolyUtil.containsLocation(currentLocation, outerBoundary, true);

        if (lies) {
            // Get the inner boundaries and check if the test location lies inside
            ArrayList<ArrayList<LatLng>> innerBoundaries = polygon.getInnerBoundaryCoordinates();
            if (innerBoundaries != null) {
                for (ArrayList<LatLng> innerBoundary : innerBoundaries) {
                    // If the current location lies in a hole, the polygon doesn't contain the location
                    if (PolyUtil.containsLocation(currentLocation, innerBoundary, true)) {
                        lies = false;
                        break;
                    }
                }
            }
        }
        return lies;
    }


    // Finds the shortest distance from current location to polygon
    public static double findShortestDistance(KmlPolygon polygon, LatLng myLocation) {
        double minDistance = -1;

        if (polygon == null || myLocation == null) {
            return minDistance;
        }

        // Finds the shortest distance on outer boundary of polygon
        ArrayList<LatLng> polygonOuterCoordinates = polygon.getOuterBoundaryCoordinates();
        minDistance = distanceToLine(polygonOuterCoordinates, myLocation);

        // Finds the shortest distance on inner boundary of polygon (if exist one or more)
        ArrayList<ArrayList<LatLng>> polygonInnerCoordinates = polygon.getInnerBoundaryCoordinates();
        if (polygonInnerCoordinates != null) {
            double minInnerDistance;

            for (ArrayList<LatLng> innerCoordinates : polygonInnerCoordinates) {
                minInnerDistance = distanceToLine(innerCoordinates, myLocation);

                if (minInnerDistance < minDistance && minInnerDistance != -1) {
                    minDistance = minInnerDistance;
                }
            }
        }

        return minDistance;
    }

    // Finds the shortest distance from current location to polygon lines as coordinates array list
    private static double distanceToLine(ArrayList<LatLng> polygonCoordinates, LatLng currentLocation) {
        double minDistance = -1;

        if (polygonCoordinates == null || currentLocation == null) {
            return minDistance;
        }

        // Find shortest distance from current location to line between two points of polygon
        int size = polygonCoordinates.size();
        if (size == 0) {
            return minDistance;
        } else {
            double distance;
            for (int i = 0; i < size - 1; i++) {
                distance = PolyUtil.distanceToLine(currentLocation,
                        polygonCoordinates.get(i),
                        polygonCoordinates.get(i + 1));
                if (distance < minDistance || minDistance == -1) {
                    minDistance = distance;
                }
            }

            // If the last point of polygon does not equal to the first point
            if (polygonCoordinates.get(0) != polygonCoordinates.get(size - 1)) {
                distance = PolyUtil.distanceToLine(currentLocation,
                        polygonCoordinates.get(0),
                        polygonCoordinates.get(size - 1));
                if (distance < minDistance) {
                    minDistance = distance;
                }
            }
        }

        return minDistance;
    }
}

