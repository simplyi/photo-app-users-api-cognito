package com.appsdeveloperblog.aws.lambda;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.fail;

public class CreateUserHandlerTest {

    @BeforeEach
    public void runBeforeEachTestMethod() {
        System.out.println("Executing @BeforeEach method");
    }

    @AfterEach
    public void runAfterEachTestMethod() {
       System.out.println("Executing @AfterEach method");
    }

    @BeforeAll
    public static void runBeforeAllTestMethods()  {
        System.out.println("Executing @BeforeAll method");
    }

    @AfterAll
    public static void runAfterAllTestMethods() {
        System.out.println("Executing @AfterAll method");
    }

    @Test
    public void testHandleRequest_whenValidDetailsProvided_returnsSuccessfulResponse() {
        // Arrange or Given

        // Act or When

        // Assert or Then

    }
}
