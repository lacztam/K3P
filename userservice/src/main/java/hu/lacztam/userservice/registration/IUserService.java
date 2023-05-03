package hu.lacztam.userservice.registration;


import hu.lacztam.userservice.dto.RegisterModelDto;
import hu.lacztam.userservice.exception.UserAlreadyExistException;
import hu.lacztam.userservice.model.ApplicationUser;
import hu.lacztam.userservice.model.VerificationToken;

public interface IUserService {

    ApplicationUser createApplicationUserModel(RegisterModelDto userDto)
            throws UserAlreadyExistException;

    ApplicationUser getUser(String verificationToken);

    void createVerificationToken(ApplicationUser user, String token);

    VerificationToken getVerificationToken(String VerificationToken);
}