// --------------------------------------------------------------------------------------------------------------------
// <copyright file="LoadRangeHelper.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the LoadRangeHelper type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2
{
    using System;
    using System.Collections.Generic;

    /// <summary>
    /// The load range helper.
    /// </summary>
    /// <typeparam name="T">
    /// </typeparam>
    public static class LoadRangeHelper<T> where T : class 
    {
        /// <summary>
        /// The get valid range.
        /// </summary>
        /// <param name="list">
        /// The list.
        /// </param>
        /// <param name="index">
        /// The index.
        /// </param>
        /// <param name="count">
        /// The count.
        /// </param>
        /// <param name="loadMoreData"></param>
        /// <returns>
        /// The <see cref="List"/>.
        /// </returns>
        public static List<T> GetValidRange(List<T> list, int index, int count, bool loadMoreData)
        {
            if (list.Count - 1 <= count)
            {
                return list;
            }

            if (!loadMoreData)
            {
                return list.GetRange(0, index == 0 ? 10 : index);
            }

            var length = (index + count) >= list.Count - 1
                            ? list.Count - index
                            : count;

            var start = index;

            if (start >= list.Count - 1)
            {
                start = 0;
                length = 0;
            }

            return list.GetRange(start, length);
        }
    }
}