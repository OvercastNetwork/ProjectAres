package tc.oc.commons.core.exception;

import java.lang.reflect.Member;
import javax.annotation.Nullable;

import tc.oc.commons.core.reflect.ReflectionFormatting;

/**
 * Exception indicating that a {@link Member} definition does not meet some requirement
 */
public class InvalidMemberException extends Error {

    private final Member member;

    public InvalidMemberException(Member member, @Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
        this.member = member;
    }

    public InvalidMemberException(Member member, @Nullable Throwable cause) {
        this(member, null, cause);
    }

    public InvalidMemberException(Member member, @Nullable String message) {
        this(member, message, null);
    }

    public InvalidMemberException(Member member) {
        this(member, null, null);
    }

    public Member getMember() {
        return member;
    }

    public String getMemberDescription() {
        return ReflectionFormatting.multilineDescription(member);
    }

    @Override
    public String toString() {
        return super.toString() + "\n  in " + getMemberDescription();
    }
}
