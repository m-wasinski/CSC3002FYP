using System;

namespace FindNDriveServices2
{
    using DomainObjects.Domains;

    /// <summary>
    /// The distance type to return the results in.
    /// </summary>
    public enum DistanceType { Miles, Kilometers };
 
    public class Haversine
    {
         /// <summary>
         /// Returns the distance in miles or kilometers of any two
         /// latitude / longitude points.
         /// </summary>
         /// <param name="geoAddress1"></param>
         /// <param name="geoAddress2"></param>
         /// <param name="type"></param>
         /// <returns></returns>
         public double Distance(GeoAddress geoAddress1, GeoAddress geoAddress2, DistanceType type)
        {
            var r = (type == DistanceType.Miles) ? 3960 : 6371;

             var dLat = this.toRadian(geoAddress2.Latitude - geoAddress1.Latitude);

             var dLon = this.toRadian(geoAddress2.Longitude - geoAddress1.Longitude);

             var a = Math.Sin(dLat / 2) * Math.Sin(dLat / 2) +
                Math.Cos(this.toRadian(geoAddress1.Latitude)) *Math.Cos(this.toRadian(geoAddress2.Latitude)) *
                Math.Sin(dLon / 2) * Math.Sin(dLon / 2);
            var c = 2 * Math.Asin(Math.Min(1, Math.Sqrt(a)));
            var d = r * c;
 
            return d;
        }

         /// <summary>
         /// Convert to Radians.
         /// </summary>
         /// <param name=”val”></param>
         /// <returns></returns>
         private double toRadian(double val)
        {
            return (Math.PI / 180) * val;
        }
    }
}