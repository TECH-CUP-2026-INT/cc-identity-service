package co.edu.escuelaing.techcup.identity.service;

import org.springframework.stereotype.Component;

@Component
public class FullNameSplitter {

    public String[] split(String fullName) {
        String trimmed = fullName.trim();
        int firstSpace = trimmed.indexOf(' ');
        if (firstSpace == -1) {
            return new String[] { trimmed, "" };
        }
        return new String[] { trimmed.substring(0, firstSpace), trimmed.substring(firstSpace + 1) };
    }
}