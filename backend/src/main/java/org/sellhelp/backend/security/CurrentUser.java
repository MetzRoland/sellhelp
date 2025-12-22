package org.sellhelp.backend.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    public UserDetails getCurrentlyLoggedUserDetails(){
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public String getCurrentlyLoggedUserEmail(){
        return getCurrentlyLoggedUserDetails().getUsername();
    }
}
