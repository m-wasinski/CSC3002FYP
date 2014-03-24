// --------------------------------------------------------------------------------------------------------------------
// <copyright file="Haversine.cs" company="">
//   
// </copyright>
// <summary>
//   The distance type to return the results in.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace Services.ServiceUtils
{
    using System;

    using DomainObjects.Domains;

    /// <summary>
    /// Uses the haversine formula to work out distance between two GeoAddresses, either in kilometers or miles.
    /// </summary>
    public enum DistanceType
    {
        /// <summary>
        /// The miles.
        /// </summary>
        Miles,

        /// <summary>
        /// The kilometers.
        /// </summary>
        Kilometers
    }

    /// <summary>
    /// The haversine.
    /// </summary>
    public static class HaversineCalculator
    {
        /// <summary>
        /// Calculates the distance between two GeoAddresses.
        /// </summary>
        /// <param name="geoAddress1">
        /// The geo address 1.
        /// </param>
        /// <param name="geoAddress2">
        /// The geo address 2.
        /// </param>
        /// <param name="type">
        /// The type.
        /// </param>
        /// <returns>
        /// The <see cref="double"/>.
        /// </returns>
        public static double CalculateDistance(GeoAddress geoAddress1, GeoAddress geoAddress2, DistanceType type)
        {
             var radius = (type == DistanceType.Miles) ? 3960 : 6371;

             var distancelatitude = ConvertToRadian(geoAddress2.Latitude - geoAddress1.Latitude);

             var distancelongitude = ConvertToRadian(geoAddress2.Longitude - geoAddress1.Longitude);

             var a = 
                Math.Sin(distancelatitude / 2) * Math.Sin(distancelatitude / 2) +
                Math.Cos(ConvertToRadian(geoAddress1.Latitude)) * Math.Cos(ConvertToRadian(geoAddress2.Latitude)) *
                Math.Sin(distancelongitude / 2) * Math.Sin(distancelongitude / 2);

            var c = 2 * Math.Asin(Math.Min(1, Math.Sqrt(a)));
            var distance = radius * c;

            return distance;
        }

        /// <summary>
        /// Converts given double to radian.
        /// </summary>
        /// <param name="val">
        /// The val.
        /// </param>
        /// <returns>
        /// The <see cref="double"/>.
        /// </returns>
        private static double ConvertToRadian(double val)
        {
            return (Math.PI / 180) * val;
        }
    }
}