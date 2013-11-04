namespace FindNDriveInfrastructureCore.Util
{
    /// <summary>
    /// Represents a generic Factory class.
    /// </summary>
    /// <typeparam name="T">The genric type of the object which will be returned by the factory
    /// </typeparam>
    public interface IFactory<T>
    {
        /// <summary>
        /// Creates a new instance of T with the required dependencies.
        /// </summary>
        /// <returns>
        /// The new <see cref="T"/> instance.
        /// </returns>
        T GetNewInstance();
    }
}
