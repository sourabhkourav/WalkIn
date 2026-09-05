package com.walkin.entity;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HiringDriveSerializationTests {

    @Test
    void registrationTokenHashIsNeverSerialized() throws Exception {
        HiringDrive drive = new HiringDrive();
        drive.setRegistrationTokenHash("stored-registration-token-hash");

        String json = JsonMapper.builder().build().writeValueAsString(drive);

        assertThat(json)
                .doesNotContain("registrationTokenHash")
                .doesNotContain("stored-registration-token-hash");
    }
}
