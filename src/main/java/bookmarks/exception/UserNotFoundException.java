package bookmarks.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends ApiBaseException {

    public UserNotFoundException(String userId) {
        super(userId +  " not found");
    }

}