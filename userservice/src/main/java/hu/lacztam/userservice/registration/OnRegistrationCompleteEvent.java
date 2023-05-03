package hu.lacztam.userservice.registration;

import hu.lacztam.userservice.model.ApplicationUser;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

public class OnRegistrationCompleteEvent extends ApplicationEvent {

    private String appUrl;
    private Locale locale;
    private ApplicationUser applicationUser;

    public OnRegistrationCompleteEvent(ApplicationUser applicationUser, Locale locale, String appUrl) {
        super(applicationUser);

        this.appUrl = appUrl;
        this.locale = locale;
        this.applicationUser = applicationUser;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public Locale getLocale() {
        return locale;
    }

    public ApplicationUser getApplicationUser() {
        return applicationUser;
    }
}