package com.bcp.training.conference;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ScheduleResourceTest {

    private static final int GIVEN_ID = 101;
    private static final int GIVEN_VENUE_ID = 101;

    @LocalServerPort
    int port;

    @Autowired
    ScheduleRepository scheduleRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/schedule";
    }

    @Test
    public void testRetrieve() {
        given()
                .when()
                .get("/" + GIVEN_ID)
                .then()
                .statusCode(200)
                .body("id", equalTo(GIVEN_ID))
                .body("venueId", equalTo(GIVEN_VENUE_ID));
    }

    @Test
    public void testAdd() {
        given()
                .when()
                .body("{\"venueId\":1010,\"date\":\"2020-03-20\"}")
                .contentType(ContentType.JSON)
                .post()
                .then()
                .statusCode(201)
                .header("Location", not(emptyOrNullString()))
                .body("venueId", equalTo(1010));
    }

    @Test
    public void testAllSchedules() {
        long count = scheduleRepository.count();
        List<Schedule> schedules = given()
                .when()
                .get("/all")
                .thenReturn().as(List.class);
        assertThat(schedules, hasSize((int) count));
    }

    @Test
    public void testRetrieveByVenue() {
        long count = scheduleRepository.findByVenueId(101).size();
        List<Schedule> scheds = given().when()
                .get("/venue/101")
                .thenReturn().as(List.class);
        assertThat(scheds, hasSize((int) count));
    }
}
