package com.walkin.dto;

import com.walkin.entity.NotificationChannel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class CandidateRegistrationRequest {

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Size(max = 100)
    private String email;

    @Size(max = 15)
    private String contactNumber;

    @NotNull
    private NotificationChannel notificationChannel;

    @NotBlank
    @Size(max = 100)
    private String notificationDestination;

    @NotNull
    @Min(5)
    @Max(240)
    private Integer advanceNoticeMinutes;

    private MultipartFile resume;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public NotificationChannel getNotificationChannel() {
        return notificationChannel;
    }

    public void setNotificationChannel(NotificationChannel notificationChannel) {
        this.notificationChannel = notificationChannel;
    }

    public String getNotificationDestination() {
        return notificationDestination;
    }

    public void setNotificationDestination(String notificationDestination) {
        this.notificationDestination = notificationDestination;
    }

    public Integer getAdvanceNoticeMinutes() {
        return advanceNoticeMinutes;
    }

    public void setAdvanceNoticeMinutes(Integer advanceNoticeMinutes) {
        this.advanceNoticeMinutes = advanceNoticeMinutes;
    }

    public MultipartFile getResume() {
        return resume;
    }

    public void setResume(MultipartFile resume) {
        this.resume = resume;
    }
}
