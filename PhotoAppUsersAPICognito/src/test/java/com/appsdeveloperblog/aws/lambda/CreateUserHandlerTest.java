package com.appsdeveloperblog.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.appsdeveloperblog.aws.lambda.service.CognitoUserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(MockitoExtension.class)
public class CreateUserHandlerTest {

    @Mock
    CognitoUserService cognitoUserService;

    @Mock
    APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent;

    @Mock
    Context context;

    @InjectMocks
    CreateUserHandler handler;

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
