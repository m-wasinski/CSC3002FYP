namespace Tests
{
    using System;
    using System.Linq;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using WebMatrix.WebData;

    public static class TestFactories
    {   
        public static User GetUser()
        {
            const string RandomChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

            var randomString =
                new string(Enumerable.Repeat(RandomChars, 8).Select(s => s[new Random().Next(s.Length)]).ToArray())
                + DateTime.Now.Millisecond;

            // Initialise the WebSecurity module.
            if (!WebSecurity.Initialized)
            {
                WebSecurity.InitializeDatabaseConnection("TestConnectionString", "User", "Id", "UserName", true);
            }

            WebSecurity.CreateUserAndAccount(randomString, "password");
            var id = WebSecurity.GetUserId(randomString);

            return new User
            {
                UserId = id,
                UserName = randomString,
                FirstName = randomString,
                LastName = randomString,
                EmailAddress = randomString + "@domain.com",
                Gender = Gender.Male,
                DateOfBirth = DateTime.Now,
                Status = Status.Offline,
                PhoneNumber = randomString,
                GCMRegistrationID = "0",
                PrivacySettings = new PrivacySettings
                                      {
                                          DateOfBirthPrivacyLevel = PrivacyLevel.Private,
                                          EmailPrivacyLevel = PrivacyLevel.Private,
                                          GenderPrivacyLevel = PrivacyLevel.Private,
                                          JourneysPrivacyLevel = PrivacyLevel.Private,
                                          PhoneNumberPrivacyLevel = PrivacyLevel.Private,
                                          RatingPrivacyLevel = PrivacyLevel.Private
                                      },
                                      AverageRating = 0,
                                      ProfilePicture = new ProfilePicture {ProfilePictureBytes = null, ProfilePictureId = id},
                                      MemberSince = DateTime.Now,
                                      
                                      
            };
        }
    }
}
