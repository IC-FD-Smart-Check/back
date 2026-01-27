package org.fdsmartcheck.util;

import org.springframework.stereotype.Component;

@Component
public class GeoUtils {

    private static final int EARTH_RADIUS_METERS = 6371000;

    /**
     * Calcula a distância entre duas coordenadas usando a fórmula de Haversine
     *
     * @param lat1 Latitude do ponto 1
     * @param lng1 Longitude do ponto 1
     * @param lat2 Latitude do ponto 2
     * @param lng2 Longitude do ponto 2
     * @return Distância em metros
     */
    public double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }

    /**
     * Verifica se um ponto está dentro do raio de outro ponto
     *
     * @param userLat Latitude do usuário
     * @param userLng Longitude do usuário
     * @param eventLat Latitude do evento
     * @param eventLng Longitude do evento
     * @param radiusMeters Raio permitido em metros
     * @return true se está dentro do raio, false caso contrário
     */
    public boolean isWithinRadius(
            double userLat,
            double userLng,
            double eventLat,
            double eventLng,
            double radiusMeters
    ) {
        double distance = calculateDistance(userLat, userLng, eventLat, eventLng);
        return distance <= radiusMeters;
    }
}