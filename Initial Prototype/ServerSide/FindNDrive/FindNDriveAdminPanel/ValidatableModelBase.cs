using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;

namespace FindNDriveAdminPanel
{
    using System.ComponentModel;
    using System.ComponentModel.DataAnnotations;
    using System.Reflection;

    /// <summary>
    ///     Represents the base viewmodel which can be validated against
    /// </summary>
    public class ValiditableModelBase : NotifyPropertyChangedBase, IDataErrorInfo
    {
        /// <summary>
        /// The backing field for whether or not the current model is valid
        /// </summary>
        private bool? _isValid;

        /// <summary>
        ///     True if all public properties in this data model are correctly validated.
        ///     False otherwise.
        /// </summary>
        public bool IsValid
        {
            get { return EvaluateAllProperties(); }
        }

        /// <summary>
        /// Evaluates all properties within the extending class for correctness
        /// </summary>
        /// <returns>Whether all fields are valid</returns>
        private bool EvaluateAllProperties()
        {
                 // Validate all properties declared by the class, IE, none defined within the base class
                var properties =
                    GetType().GetProperties(BindingFlags.Public | BindingFlags.Instance | BindingFlags.DeclaredOnly);
                var allPropertiesValid = properties
                    .All(property =>
                    {
                        IEnumerable<string> errors;
                        var isValid = TryValidateProperty(property, out errors);
                        return isValid;
                    });
                TriggerIsValidEvent(allPropertiesValid);
                return allPropertiesValid;
        }

        /// <summary>
        /// Provides index accessor for validating a specific property name
        /// </summary>
        /// <param name="propertyName">
        /// The property name.
        /// </param>
        /// <returns>
        /// The <see cref="string"/>.
        /// </returns>
        string IDataErrorInfo.this[string propertyName]
        {
            get
            {
                var prop = GetType().GetProperty(propertyName);
                IEnumerable<string> errors;
                var isFieldValid = TryValidateProperty(prop, out errors);

                // Note, we can verify that the field is valid, however we should also test all fields
                // are valid to retrigger the appropiate Delegate event
                EvaluateAllProperties();

                // Returning null shows that there is no validation error
                // So we can not simply pass back an empty string.
                return isFieldValid ? null : string.Join("\n", errors);
            }
        }

        /// <summary>
        ///     This is not used, as it is not required. All error logic will be accessible through
        ///     IDataErrorInfo.this[propertyName]
        /// </summary>
        string IDataErrorInfo.Error
        {
            get { return null; }
        }

        /// <summary>
        ///     Listener event for when the given object switches from a valid state to an invalid state,
        ///     or vice versa.
        /// </summary>
        public event Action<bool> ErrorStateChanged = delegate { };

        /// <summary>
        ///     Try to attempt to validate the given property. Note this follows the `Try*` pattern in which
        ///     a true boolean implies a valid property, and a false implies an invalid property.
        /// </summary>
        /// <param name="propertyInfo">The property of this object</param>
        /// <param name="errors">
        ///     The list of errors to be returned after this method completes.
        ///     This will be initialised to an empty list, so no NPE will occur.
        /// </param>
        /// <returns>
        ///     True if this property is valid, false otherwise.
        ///     In both cases the errors param will be non-null
        /// </returns>
        public bool TryValidateProperty(PropertyInfo propertyInfo, out IEnumerable<string> errors)
        {
            var value = propertyInfo.GetValue(this);
            var validationContext = new ValidationContext(this)
            {
                MemberName = propertyInfo.Name
            };

            var validationResults = new List<ValidationResult>();
            var isValid = Validator.TryValidateProperty(value, validationContext, validationResults);
            errors = validationResults.Select(_ => _.ErrorMessage);

            return isValid;
        }

        /// <summary>
        /// Fires an event if the state overall state of isValid has changed.
        /// </summary>
        /// <param name="newIsValid">The new valid state</param>
        private void TriggerIsValidEvent(bool newIsValid)
        {
            // Fire the appropiate event if this state changes
            if (_isValid.HasValue && (newIsValid == _isValid.Value)) return;
        
            // Update the isValid field and trigger the event
            _isValid = newIsValid;
            ErrorStateChanged(newIsValid);
        }
    }

    [AttributeUsage(AttributeTargets.Method, AllowMultiple = false, Inherited = true)]
    public sealed class NotifyPropertyChangedInvocatorAttribute : Attribute
    {
        public NotifyPropertyChangedInvocatorAttribute()
        {
        }

        public NotifyPropertyChangedInvocatorAttribute(string parameterName)
        {
            ParameterName = parameterName;
        }

        public string ParameterName { get; private set; }
    }

    public class NotifyPropertyChangedBase : INotifyPropertyChanged
    {
        public event PropertyChangedEventHandler PropertyChanged = delegate { };

        [NotifyPropertyChangedInvocator]
        protected virtual void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChangedEventHandler handler = PropertyChanged;
            if (handler != null) handler(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}
