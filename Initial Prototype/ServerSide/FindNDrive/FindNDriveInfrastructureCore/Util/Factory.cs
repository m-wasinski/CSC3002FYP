// --------------------------------------------------------------------------------------------------------------------
// <copyright file="Factory.cs" company="">
//   
// </copyright>
// <summary>
//   A concrete implementation of a factory which relies on a function expression to create a new instance.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveInfrastructureCore.Util
{
    using System;

    /// <summary>
    /// A concrete implementation of a factory which relies on a function expression to create a new instance.
    /// </summary>
    /// <typeparam name="T">The generic type of the object created by this factory.
    /// </typeparam>
    public class Factory<T> : IFactory<T>
    {
        /// <summary>
        /// The function which will generate a new instance of type T
        /// </summary>
        private readonly Func<T> func;

        /// <summary>
        /// Initializes a new instance of the <see cref="Factory{T}"/> class.
        /// </summary>
        /// <param name="func">
        /// The function which will generate a new instance of type T
        /// </param>
        public Factory(Func<T> func)
        {
            this.func = func;
        }

        /// <summary>
        /// Creates a new instance of T with the required dependencies.
        /// </summary>
        /// <returns>
        /// The new <see cref="T"/> instance.
        /// </returns>
        public T GetNewInstance()
        {
            return this.func();
        }
    }
}
