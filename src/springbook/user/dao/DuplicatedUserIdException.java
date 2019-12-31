package springbook.user.dao;

public class DuplicatedUserIdException extends RuntimeException {
    public DuplicatedUserIdException(Throwable cause){
        super(cause);
    }
}
