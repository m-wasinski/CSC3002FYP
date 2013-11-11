using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;

namespace FindNDriveInfrastructureCore
{
    /// <summary>
    /// Used to determine whether object is valid or not with an optional list of error messages.
    /// </summary>
    public class ValidatedObject
    {
        public bool IsValid { get; set; }
        public List<string> ErrorMessages { get; set; }
    }

    /// <summary>
    /// Performs recursive validation on data annotations on the wcf layer.  Iterates through all data annotations for a given object as well as data annotations present
    /// in that object's members.
    /// 
    /// Reference:
    /// This code is a modified version of the following article: http://www.tsjensen.com/blog/post/2011/12/23/Custom+Recursive+Model+Validation+In+NET+Using+Data+Annotations.aspx
    /// Tyler Jensen is the original author of the recursive validation algorithm.
    /// </summary>
    public static class ValidationHelper
    {
        public static ValidatedObject Validate(object instance)
        {
            var results = new List<ValidationResult>();
            var isValid = TryValidateObjectRecursively(instance, results);
            var errors = results.ConvertAll(_ => _.ToString());
            return new ValidatedObject() { IsValid = isValid, ErrorMessages = errors };
        }

        private static bool TryValidateObject(object obj, ICollection<ValidationResult> results)
        {
            return Validator.TryValidateObject(obj, new ValidationContext(obj, null, null), results, true);
        }

        private static bool TryValidateObjectRecursively<T>(T obj, List<ValidationResult> results)
        {
            bool result = TryValidateObject(obj, results);

            var properties = obj.GetType().GetProperties().Where(prop => Attribute.IsDefined(prop, typeof(ValidateObjectAttribute)));

            foreach (var property in properties)
            {
                var valAttrib = property.GetCustomAttributes(typeof(ValidateObjectAttribute), true).FirstOrDefault() as ValidateObjectAttribute;
                var value = obj.GetPropertyValue(property.Name);

                if (value == null || valAttrib == null) continue;

                var asEnumerable = value as IEnumerable;
                if (asEnumerable != null)
                {
                    var items = new List<object>();
                    foreach (var enumObj in asEnumerable) items.Add(enumObj);
                        foreach (var enumObj in items)
                        {
                            result = TryValidateObjectRecursively(enumObj, results) && result;
                        }
                    if (items.Count < valAttrib.MinOccursOnEnumerable)
                    {
                        string errorMessage = valAttrib.ErrorMessage ?? "MinOccursOnEnumerable validation failed.";
                        results.Add(new ValidationResult(errorMessage));
                        result = false;
                    }
                }
                else
                {
                    result = TryValidateObjectRecursively(value, results) && result;
                }
            }

            return result;
        }
    }

    public static class ObjectExtensions
    {
        public static object GetPropertyValue(this object o, string propertyName)
        {
            object objValue = string.Empty;

            var propertyInfo = o.GetType().GetProperty(propertyName);
            if (propertyInfo != null)
            {
                objValue = propertyInfo.GetValue(o, null);
            }
            return objValue;
        }
    }

    [AttributeUsage(AttributeTargets.Property | AttributeTargets.Field, Inherited = false, AllowMultiple = false)]
    public class ValidateObjectAttribute : Attribute
    {
        int _minOccurs;
        //marker for object properties that need to be recursively validated

        public int MinOccursOnEnumerable { get { return _minOccurs; } set { _minOccurs = value; } }

        public string ErrorMessage { get; set; }
    }
}
